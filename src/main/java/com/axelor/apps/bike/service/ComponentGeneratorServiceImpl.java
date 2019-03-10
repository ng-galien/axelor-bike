package com.axelor.apps.bike.service;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import com.axelor.apps.base.db.*;
import com.axelor.apps.base.db.repo.*;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.bike.util.VariantUtils;
import com.axelor.apps.bike.web.ProductGeneratorController;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentGeneratorServiceImpl implements ComponentGeneratorService {

  @Inject private ProductRepository productRepo;
  @Inject private ProductService productService;
  @Inject private ProductVariantRepository variantRepo;
  @Inject private ProductVariantService variantService;
  @Inject private BillOfMaterialService billOfMaterialService;
  @Inject private BillOfMaterialRepository billOfMaterialRepository;
  @Inject private MetaFiles metaFiles;

  private static final Logger LOG = LoggerFactory.getLogger(ProductGeneratorController.class);

  @Transactional
  @Override
  public void generateProductSmartVariants(Product model, Boolean debug) throws AxelorException {

    LOG.debug("generateProductSmartVariants");
    JsonObject attrJsonObject = getJsonAttr(model);

    List<ProductVariant> productVariants =
        filterVariants(model, getProductVariantList(model.getProductVariantConfig()));
    final AtomicInteger index = new AtomicInteger(0);
    final Map<String, Map<String, String>> extendConfig =
        VariantUtils.getProductExtenderConfig(attrJsonObject);
    productVariants.forEach(
        productVariant -> {
          variantRepo.save(productVariant);
          Product copy =
              productService.createProduct(model, productVariant, index.incrementAndGet());
          // Update default partner
          updateProductCopy(
              model,
              copy,
              index.get(),
              VariantUtils.getVariantMapCode(productVariant),
              extendConfig);
          LOG.debug(copy.getName());
          // Save copy
          productRepo.save(copy);
        });
    if (debug) {
      throw new AxelorException(4, "Bike application in debug mode");
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void generateProductBOMVariants(Product model, Boolean debug) throws AxelorException {

    final AtomicInteger index = new AtomicInteger(0);

    BillOfMaterial bomModel = model.getDefaultBillOfMaterial();
    if (bomModel == null) {
      return;
    }

    Set<BillOfMaterial> bomMembers = bomModel.getBillOfMaterialSet();
    if (bomMembers.size() == 0) {
      return;
    }

    Map<BillOfMaterial, List<Product>> productMap = getBomMembersWithVariants(bomModel);
    // Get the list of variant combinations
    if (productMap.size() == 0) {
      return;
    }
    // All variants
    Map<String, List<String>> attrMap = getVariantMapList(productMap);
    // Cardinal of variants
    List<String> cardinal =
        VariantUtils.ofCombinations(attrMap.values())
            .map(l -> l.stream().collect(Collectors.joining("&")))
            .collect(Collectors.toList());

    int target = productMap.size();

    // Iterate the cardinal to match a valid combination
    cardinal.forEach(
        url -> {
          // Selected
          Map<BillOfMaterial, Product> selectedSet = new LinkedHashMap<>();
          productMap
              .entrySet()
              .forEach(
                  e -> {
                    Optional<Product> found =
                        e.getValue()
                            .stream()
                            .filter(
                                p -> {
                                  return url.matches(
                                      getJsonAttr(p)
                                          .get(VariantUtils.VARIANT_SEARCH)
                                          .toString()
                                          .replace("\"", ""));
                                })
                            .findFirst();
                    if (found.isPresent()) {
                      selectedSet.put(e.getKey(), found.get());
                    }
                  });
          // The combination is ok
          if (selectedSet.size() == target) {

            LOG.debug(String.format("Product and BOM with URL: %s created", url));
            // Copy the product
            Product productCopy = createProductVariant(model, url, index.incrementAndGet());
            // Get the map of variant in the URL
            Map<String, String> variantMap =
                Stream.of(url.split("&"))
                    .map(str -> str.split("="))
                    .collect(toMap(entry -> entry[0], entry -> entry[1]));
            // Update the product values
            updateProductCopy(model, productCopy, index.get(), variantMap, null);
            // Create a BOM version
            BillOfMaterial bomCopy = billOfMaterialService.generateNewVersion(bomModel);
            // Update the bom with variant members
            selectedSet
                .entrySet()
                .forEach(
                    prodBom -> {
                      bomCopy.removeBillOfMaterialSetItem(prodBom.getKey());
                      BillOfMaterial newBOM = new BillOfMaterial();
                      newBOM.setProduct(prodBom.getValue());
                      newBOM.setUnit(prodBom.getKey().getUnit());
                      newBOM.setQty(prodBom.getKey().getQty());
                      newBOM.setPriority(prodBom.getKey().getPriority());
                      bomCopy.addBillOfMaterialSetItem(newBOM);
                    });
            // Save the bom and objects
            productRepo.save(productCopy);
            bomCopy.setProduct(productCopy);
            billOfMaterialRepository.save(bomCopy);
            productCopy.setDefaultBillOfMaterial(bomCopy);
            billOfMaterialRepository.save(bomCopy);
          }
        });
    // model.setDefaultBillOfMaterial(bomModel);
    if (debug) {
      throw new AxelorException(4, "Bike application in debug mode");
    }
  }

  /**
   * @param product
   * @param url
   * @param index
   * @return
   */
  private Product createProductVariant(Product product, String url, int index) {
    // Check if product variant exist
    ProductVariantAttr pvAttr =
        Query.of(ProductVariantAttr.class).filter("self.name = ?1", product.getCode()).fetchOne();
    if (pvAttr == null) {
      pvAttr = new ProductVariantAttr();
      pvAttr.setCode(product.getCode());
      pvAttr.setName(product.getCode());
      Beans.get(ProductVariantAttrRepository.class).save(pvAttr);
    }
    ProductVariantValue pvValue =
        variantService.createProductVariantValue(pvAttr, url, url, BigDecimal.ZERO);
    Beans.get(ProductVariantValueRepository.class).save(pvValue);
    ProductVariant pv =
        variantService.createProductVariant(
            pvAttr, null, null, null, pvValue, null, null, null, true);
    Product p = productService.createProduct(product, pv, index);
    p.setCode(product.getCode() + "_" + url);
    return p;
  }

  /**
   * @param model
   * @param copy
   * @param index
   * @param variantMap
   * @param extendConfig
   */
  private void updateProductCopy(
      Product model,
      Product copy,
      int index,
      Map<String, String> variantMap,
      Map<String, Map<String, String>> extendConfig) {

    copy.setDefaultSupplierPartner(model.getDefaultSupplierPartner());
    // Update code
    copy.setCode(model.getCode() + getProductVariantName(copy.getProductVariant()));
    // Update description (replace <br>)
    copy.setDescription(copy.getDescription().replace("<br>", ""));
    // Update internal description (replace <br>)
    copy.setInternalDescription(copy.getInternalDescription().replace("<br>", ""));
    //
    final Map<String, String> properties = new HashMap<>();
    variantMap
        .entrySet()
        .forEach(
            e -> {
              ProductVariantAttr pva =
                  Query.of(ProductVariantAttr.class)
                      .filter("self.code = ?1", e.getKey())
                      .fetchOne();
              //      = variantAttrRepository.findByCode(e.getKey());
              List<ProductVariantValue> values = pva.getProductVariantValueList();
              Optional<ProductVariantValue> value =
                  values.stream().filter(v -> v.getCode().equals(e.getValue())).findFirst();
              if (value.isPresent()) {
                properties.put(e.getKey(), value.get().getName());
              }
            });
    // Set IDX
    properties.put(VariantUtils.VARIANT_IDX, String.format("%04d", index));

    String url =
        variantMap
            .entrySet()
            .stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(joining("&"));
    // Expand variants URL for grouping
    // Permits a single product share multiple variant values
    properties.put(VariantUtils.VARIANT_URL, url);
    // Extend the productMap according extend configuration
    final Map<String, String> objectVariants = new HashMap<>(variantMap);

    if (extendConfig != null) {

      extendConfig
          .entrySet()
          .forEach(
              e -> {
                if (url.matches(e.getKey())) {
                  e.getValue()
                      .entrySet()
                      .forEach(
                          e2 -> {
                            objectVariants.put(
                                e2.getKey(),
                                Arrays.asList(
                                        e2.getValue().toString(), objectVariants.get(e2.getKey()))
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .collect(joining("|")));
                          });
                }
              });
    }
    // Construct search string
    String search =
        objectVariants
            .entrySet()
            .stream()
            .map(
                e ->
                    String.format(
                        "(.*%s=%s.*)",
                        e.getKey(),
                        e.getValue().contains("|")
                            ? String.format("(%s)", e.getValue())
                            : e.getValue()))
            .collect(joining("|"));

    // Set the search regex value on the json attr
    properties.put(
        VariantUtils.VARIANT_SEARCH,
        String.format("(%s){%d}", search, StringUtils.countMatches(search, "=")));

    // Create variant obj
    JsonObjectBuilder builder = Json.createObjectBuilder();
    variantMap.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
    properties.put(VariantUtils.VARIANT_OBJECT, builder.build().toString());
    // Set json
    JsonObject json = enrich(getJsonAttr(model), properties);
    copy.setAttrs(json.toString());
  }

  /**
   * In a BOM, Get the list of product which have a variant
   *
   * @param bom
   * @return
   */
  private Map<BillOfMaterial, List<Product>> getBomMembersWithVariants(BillOfMaterial bom) {
    Map<BillOfMaterial, List<Product>> res = new LinkedHashMap<>();
    bom.getBillOfMaterialSet()
        .forEach(
            e -> {
              if (e.getProduct().getProductVariantConfig() != null) {
                // res.add(e.getProduct());
                List<Product> childProductVariants =
                    productRepo.all().filter("self.parentProduct = ?1", e.getProduct()).fetch();
                res.put(e, childProductVariants);
              }
            });
    return res;
  }

  /**
   * @param products
   * @return
   */
  private Map<String, List<String>> getVariantMapList(Map<BillOfMaterial, List<Product>> products) {
    Map<String, List<String>> res = new LinkedHashMap<>();
    products
        .values()
        .forEach(
            prods ->
                prods.forEach(
                    prod -> {
                      String str =
                          getJsonAttr(prod)
                              .getString(VariantUtils.VARIANT_OBJECT); // .replace("\"", "");
                      JsonObject object = Json.createReader(new StringReader(str)).readObject();
                      object
                          .entrySet()
                          .forEach(
                              e -> {
                                String val =
                                    e.getKey() + "=" + e.getValue().toString().replace("\"", "");
                                if (!res.keySet().contains(e.getKey())) {
                                  res.put(e.getKey(), new ArrayList<>());
                                }
                                if (!res.get(e.getKey()).contains(val)) {
                                  res.get(e.getKey()).add(val);
                                }
                              });
                    }));
    return res;
  }

  private String getProductVariantName(ProductVariant variant) {
    String res = "-";
    if (variant.getProductVariantAttr1() != null) {
      // First variant
      res += variant.getProductVariantValue1().getCode();

      if (variant.getProductVariantAttr2() != null) {
        // Second variant
        res += ":" + variant.getProductVariantValue2().getCode();

        if (variant.getProductVariantAttr3() != null) {
          // Third variant
          res += ":" + variant.getProductVariantValue3().getCode();

          if (variant.getProductVariantAttr4() != null) {
            // Last variant
            res += ":" + variant.getProductVariantValue4().getCode();
          }
        }
      }
    }
    return res;
  }

  /**
   * Filter the variant list with an array of filters configured in attrs
   *
   * @param model
   * @param variants
   * @return
   */
  private List<ProductVariant> filterVariants(Product model, List<ProductVariant> variants) {

    // Retrieve filters from Json object of attrs
    String attrs = model.getAttrs();
    String[] regexFiler = new String[0];

    if (attrs != null) {
      JsonObject attrJsonObject = getJsonAttr(model);
      if (attrJsonObject.containsKey(VariantUtils.VARIANT_EXCLUDE)) {
        regexFiler =
            attrJsonObject
                .get(VariantUtils.VARIANT_EXCLUDE)
                .toString()
                .replace("\"", "")
                .trim()
                .split("[\\\\n]+");
      }
    }
    if (regexFiler.length == 0) {
      return variants;
    }
    final List<String> regex = Stream.of(regexFiler).collect(Collectors.toList());
    List<ProductVariant> res = new ArrayList<>();
    variants.forEach(
        v -> {
          String url = VariantUtils.getVariantURL(v);
          if (!regex.stream().anyMatch(r -> url.matches(r))) {
            res.add(v);
          }
        });
    return res;
  }

  /**
   * @param src
   * @return
   */
  private JsonObject getJsonAttr(Product src) {
    String attr = src.getAttrs();
    if (attr == null) {
      attr = "{}";
    }
    JsonReader jsonReader = Json.createReader(new StringReader(attr));
    return jsonReader.readObject();
  }

  public JsonObject enrich(JsonObject source, String key, String value) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add(key, value);
    source.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
    return builder.build();
  }

  private JsonObject enrich(JsonObject source, Map<String, String> map) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    source.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
    map.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
    return builder.build();
  }

  private List<ProductVariant> getProductVariantList(ProductVariantConfig productVariantConfig) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr1() != null
        && productVariantConfig.getProductVariantValue1Set() != null) {

      for (ProductVariantValue productVariantValue1 :
          productVariantConfig.getProductVariantValue1Set()) {

        productVariantList.addAll(
            this.getProductVariantList(productVariantConfig, productVariantValue1));
      }
    }

    return productVariantList;
  }

  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr2() != null
        && productVariantConfig.getProductVariantValue2Set() != null) {

      for (ProductVariantValue productVariantValue2 :
          productVariantConfig.getProductVariantValue2Set()) {

        productVariantList.addAll(
            this.getProductVariantList(
                productVariantConfig, productVariantValue1, productVariantValue2));
      }
    } else {

      productVariantList.add(
          this.createProductVariant(productVariantConfig, productVariantValue1, null, null, null));
    }

    return productVariantList;
  }

  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr3() != null
        && productVariantConfig.getProductVariantValue3Set() != null) {

      for (ProductVariantValue productVariantValue3 :
          productVariantConfig.getProductVariantValue3Set()) {

        productVariantList.addAll(
            this.getProductVariantList(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3));
      }
    } else {

      productVariantList.add(
          this.createProductVariant(
              productVariantConfig, productVariantValue1, productVariantValue2, null, null));
    }

    return productVariantList;
  }

  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr4() != null
        && productVariantConfig.getProductVariantValue4Set() != null) {

      for (ProductVariantValue productVariantValue4 :
          productVariantConfig.getProductVariantValue4Set()) {

        productVariantList.add(
            this.createProductVariant(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3,
                productVariantValue4));
      }
    } else {

      productVariantList.add(
          this.createProductVariant(
              productVariantConfig,
              productVariantValue1,
              productVariantValue2,
              productVariantValue3,
              null));
    }

    return productVariantList;
  }

  private ProductVariant createProductVariant(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3,
      ProductVariantValue productVariantValue4) {

    ProductVariantAttr productVariantAttr1 = null,
        productVariantAttr2 = null,
        productVariantAttr3 = null,
        productVariantAttr4 = null;
    if (productVariantValue1 != null) {
      productVariantAttr1 = productVariantConfig.getProductVariantAttr1();
    }
    if (productVariantValue2 != null) {
      productVariantAttr2 = productVariantConfig.getProductVariantAttr2();
    }
    if (productVariantValue3 != null) {
      productVariantAttr3 = productVariantConfig.getProductVariantAttr3();
    }
    if (productVariantValue4 != null) {
      productVariantAttr4 = productVariantConfig.getProductVariantAttr4();
    }

    return variantService.createProductVariant(
        productVariantAttr1,
        productVariantAttr2,
        productVariantAttr3,
        productVariantAttr4,
        productVariantValue1,
        productVariantValue2,
        productVariantValue3,
        productVariantValue4,
        false);
  }

  public void copyProduct(Product product, Product copy) {
    copy.setBarCode(null);
    try {
      if (product.getPicture() != null) {
        File file = MetaFiles.getPath(product.getPicture()).toFile();
        copy.setPicture(metaFiles.upload(file));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    copy.setStartDate(null);
    copy.setEndDate(null);
    copy.setCostPrice(BigDecimal.ZERO);
    copy.setPurchasePrice(BigDecimal.ZERO);
  }
}
