package com.axelor.apps.bike.service;

import static java.util.stream.Collectors.*;

import com.axelor.apps.base.db.*;
import com.axelor.apps.base.db.repo.*;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.ProductVariantService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  public static final String JSON_TEXT_DECO = "\"";
  public static final String TEXT_LINE_SEP = "\\\\n";
  public static final String URL_ATTR_SEP = "&";
  public static final String URL_VAL_SEP = "=";
  public static final String VARIANT_SEP = ";";
  public static final String VARIANT_IDX = "variant_idx";
  public static final String VARIANT_URL = "variant_url";
  public static final String VARIANT_OBJECT = "variant_object";
  public static final String VARIANT_SEARCH = "variant_search";
  public static final String VARIANT_EXCLUDE = "variant_exclude";
  public static final String VARIANT_EXPAND = "variant_expand";
  public static final String VARIANT_NAME = "variant_name";
  public static final String VARIANT_FAVORITE = "variant_favorite";

  @Inject private ProductRepository productRepo;
  @Inject private ProductService productService;
  @Inject private ProductVariantRepository variantRepo;
  @Inject private ProductVariantValueRepository productVariantValueRepo;
  @Inject private ProductVariantService variantService;
  @Inject private BillOfMaterialService billOfMaterialService;
  @Inject private BillOfMaterialRepository billOfMaterialRepository;
  @Inject private MetaFiles metaFiles;

  private static final Logger LOG = LoggerFactory.getLogger(ProductGeneratorController.class);

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  @Override
  public void setFavorite(Product product, Boolean favorite) {
    JsonObject srcJson = getJsonAttr(product);
    JsonObject resJson = enrich(srcJson, VARIANT_FAVORITE, String.valueOf(favorite));
    product.setAttrs(resJson.toString());
    productRepo.save(product);
  }

  @Transactional
  @Override
  public void renameVariant(Product model) throws AxelorException {
    JsonObject object = getJsonAttr(model);
    if (!object.containsKey(VARIANT_NAME)) {
      throw new AxelorException(4, "No name for variants");
    }
    List<String> variantNamesCfg =
        Stream.of(object.getString(VARIANT_NAME).replace(JSON_TEXT_DECO, "").split(VARIANT_SEP))
            .collect(Collectors.toList());
    List<Product> childs = getChilds(model);
    childs.forEach(
        child -> {
          Pattern namePattern = Pattern.compile("(PF-RVXX-(\\d.*))");
          Matcher matcher = namePattern.matcher(child.getCode());
          if (matcher.matches()) {
            String codePrefix = matcher.group(2);
            String codeIndex = matcher.group(3);
            List<String> name = new ArrayList<>();
            name.add(codePrefix);
            variantNamesCfg.forEach(
                s -> {
                  if (object.containsKey(s)) {
                    name.add(object.getString(s).replace(JSON_TEXT_DECO, ""));
                  }
                });
            name.add(codeIndex);
            child.setCode(name.stream().collect(joining("-")));
            productRepo.save(child);
          }
        });
  }

  @Transactional
  @Override
  public void deleteBOMVariant(Product model) throws AxelorException {
    List<Product> childs = getChilds(model);
    childs.forEach(
        child -> {
          BillOfMaterial bomToDelete = null;
          if (child.getDefaultBillOfMaterial() != null) {
            bomToDelete = child.getDefaultBillOfMaterial();
            child.setDefaultBillOfMaterial(null);
          }
          productRepo.save(child);
          if (bomToDelete != null) {
            billOfMaterialRepository.remove(bomToDelete);
          }

          ProductVariant pv = child.getProductVariant();
          ProductVariantValue pvv = pv.getProductVariantValue1();

          productRepo.remove(child);

          if (pv != null) {
            variantRepo.remove(pv);
          }

          if (pvv != null) {
            productVariantValueRepo.remove(pvv);
          }
        });
  }

  @Transactional
  @Override
  public int generateProductSmartVariants(Product model, Boolean debug) throws AxelorException {

    LOG.debug("generateProductSmartVariants");

    JsonObject attrJsonObject = getJsonAttr(model);

    // Get the filtered list of variant according the config
    List<ProductVariant> productVariants =
        filterVariants(model, getVarianCardinal(model.getProductVariantConfig()));

    // Index and count
    final AtomicInteger index = new AtomicInteger(getChildCount(model));
    final AtomicInteger count = new AtomicInteger(0);

    final Map<String, Map<String, String>> extendConfig = getProductExtenderConfig(attrJsonObject);

    productVariants.forEach(
        productVariant -> {
          variantRepo.save(productVariant);
          Product copy =
              productService.createProduct(model, productVariant, index.incrementAndGet());
          // Update default partner
          updateProductCopy(
              null, model, copy, index.get(), getVariantMapCode(productVariant), extendConfig);
          LOG.debug(copy.getName());
          // Save copy
          productRepo.save(copy);
          count.incrementAndGet();
        });
    if (debug) {
      throw new AxelorException(4, "Bike application in debug mode");
    }
    return count.get();
  }

  /**
   * @param model
   * @param debug
   * @throws AxelorException
   */
  @Override
  public int generateProductBOMVariants(Product model, Boolean debug) throws AxelorException {

    LOG.debug("generateProductBOMVariants for: " + model.getCode());
    // Control if product model have a valid BOM
    BillOfMaterial bomModel = model.getDefaultBillOfMaterial();
    if (bomModel == null) {
      throw new AxelorException(4, "Product model have no BOM");
    }

    Set<BillOfMaterial> bomMembers = bomModel.getBillOfMaterialSet();
    if (bomMembers.size() == 0) {
      throw new AxelorException(4, "BOM is Empty");
    }

    // Get the list of variant combinations
    Map<BillOfMaterial, List<Product>> productMap = getBomMembersWithVariants(bomModel);
    if (productMap.size() == 0) {
      throw new AxelorException(4, "BOM does not have variants");
    }

    // All variants
    Map<String, List<String>> attrMap = getVariantMapList(productMap);
    attrMap
        .entrySet()
        .forEach(
            e -> {
              System.out.println(
                  String.format("Attribute %s has %d values", e.getKey(), e.getValue().size()));
            });
    LOG.debug("Getting list of variants");
    // List of existing variants
    List<String> childUrl =
        getChilds(model)
            .stream()
            .map(child -> getJsonAttr(child).get(VARIANT_URL).toString().replace("\"", ""))
            .collect(Collectors.toList());

    LOG.debug(childUrl.size() + " variants found");

    // ============ CARDINAL ============//
    LOG.debug("Computing cardinal...");
    // Cardinal of variants filtered with childs
    List<String> cardinal =
        ofCombinations(attrMap.values())
            .map(l -> l.stream().collect(Collectors.joining("&")))
            .collect(Collectors.toList());
    LOG.debug(String.format("Absolute size is %d", cardinal.size()));

    // Remove double
    LOG.debug("Remove values in double...");
    cardinal = cardinal.stream().distinct().collect(Collectors.toList());
    LOG.debug(String.format("Filtered with double size is %d", cardinal.size()));

    // Filter with regex
    LOG.debug("Filter cardinal...");
    cardinal = filterCardinal(model, cardinal);
    LOG.debug(String.format("Filtered size is %d", cardinal.size()));

    // Remove existing variants
    LOG.debug("Remove existing variants...");
    cardinal =
        cardinal.stream().filter(url -> !childUrl.contains(url)).collect(Collectors.toList());
    LOG.debug(String.format("Final cardinal size is %d", cardinal.size()));
    // The number of products we have to find
    int targetSize = productMap.size();

    // Index, starting at child count + 1
    final AtomicInteger index = new AtomicInteger(getChildCount(model) + 1);
    final AtomicInteger count = new AtomicInteger(0);

    final AtomicInteger progress = new AtomicInteger(0);
    final AtomicInteger cardinalSize = new AtomicInteger(cardinal.size());
    LOG.debug("Cardinal size " + cardinalSize.get());
    // Iterate the cardinal to match a valid combination
    cardinal.forEach(
        url -> {
          // if (true) throw new RuntimeException("Stop");
          LOG.debug(
              String.format(
                  "Testing %d / %d | %d",
                  progress.incrementAndGet(), cardinalSize.get(), count.get()));
          // LOG.debug(String.format(url));
          // Selected
          Map<BillOfMaterial, Product> selectedSet = findProductVariantSet(url, productMap);
          LOG.debug(String.format("Found %d / %d", selectedSet.size(), targetSize));
          if (selectedSet.size() == targetSize) {
            LOG.debug(String.format("Url is matched"));
            // Copy the product
            Product productCopy = createProductVariant(model, url, index.incrementAndGet());
            // Get the map of variant in the URL
            Map<String, String> variantMap =
                Stream.of(url.split("&"))
                    .map(str -> str.split("="))
                    .collect(toMap(entry -> entry[0], entry -> entry[1]));
            // Update the product values
            final Map<String, Map<String, String>> extendConfig =
                getProductExtenderConfig(getJsonAttr(model));
            // Create a BOM version
            BillOfMaterial bomCopy = generateNewVersion(bomModel, index.get());
            updateProductCopy(url, model, productCopy, index.get(), variantMap, extendConfig);
            productCopy.setDefaultBillOfMaterial(bomCopy);

            copyBOM(productCopy, bomCopy, selectedSet);
            count.incrementAndGet();
          } else {
            LOG.debug(String.format("Url is rejected"));
          }
        });
    // model.setDefaultBillOfMaterial(bomModel);
    if (debug) {
      throw new AxelorException(4, "Bike application in debug mode");
    }
    return count.get();
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void copyBOM(
      Product productCopy, BillOfMaterial bomCopy, Map<BillOfMaterial, Product> selectedSet) {
    // Update the bom with variant members
    selectedSet
        .entrySet()
        .forEach(
            prodBom -> {
              bomCopy.removeBillOfMaterialSetItem(prodBom.getKey());
              BillOfMaterial newBOM = null;
              // If Product has a default bom include it as subbom
              if (prodBom.getValue().getDefaultBillOfMaterial() != null) {
                newBOM = prodBom.getValue().getDefaultBillOfMaterial();
                newBOM.setDefineSubBillOfMaterial(true);
              } else {
                newBOM = new BillOfMaterial();
                newBOM.setProduct(prodBom.getValue());
                newBOM.setDefineSubBillOfMaterial(false);
              }
              newBOM.setUnit(prodBom.getKey().getUnit());
              newBOM.setQty(prodBom.getKey().getQty());
              newBOM.setPriority(prodBom.getKey().getPriority());
              bomCopy.addBillOfMaterialSetItem(newBOM);
            });
    // Save the bom and objects
    bomCopy.setProduct(productCopy);
    billOfMaterialRepository.save(bomCopy);
    productRepo.save(productCopy);
  }

  public BillOfMaterial generateNewVersion(BillOfMaterial billOfMaterial, int index) {

    BillOfMaterial copy = billOfMaterialRepository.copy(billOfMaterial, true);

    copy.setOriginalBillOfMaterial(billOfMaterial);
    copy.clearCostSheetList();
    copy.setCostPrice(BigDecimal.ZERO);
    copy.setOriginalBillOfMaterial(billOfMaterial);
    copy.setVersionNumber(index);

    return copy;
  }

  /**
   * Search products in the url
   *
   * @param url
   * @param productMap
   * @return
   */
  public Map<BillOfMaterial, Product> findProductVariantSet(
      String url, Map<BillOfMaterial, List<Product>> productMap) {

    LOG.debug(String.format("Search product set ..."));

    Map<BillOfMaterial, Product> result = new LinkedHashMap<>();

    for (Map.Entry<BillOfMaterial, List<Product>> e : productMap.entrySet()) {

      List<Product> search =
          e.getValue()
              .stream()
              .filter(
                  p -> {
                    return url.matches(
                        getJsonAttr(p).get(VARIANT_SEARCH).toString().replace("\"", ""));
                  })
              .collect(Collectors.toList());
      int searchCount = search.size();
      if (searchCount > 1) {
        throw new RuntimeException(
            "Multiple variant found for " + e.getKey().getName() + " in " + url);
      } else if (searchCount == 1) {
        result.put(e.getKey(), search.get(0));
      } else {
        LOG.debug(String.format("Nothing found for %s", e.getKey().getName()));
        break;
      }
    }
    return result;
  }

  /**
   * Filter the cardinal
   *
   * @param model
   * @param cardinal
   * @return
   */
  private List<String> filterCardinal(Product model, List<String> cardinal) {
    // Filter the cardinal, retrieve filters from Json object of attrs
    String attrs = model.getAttrs();
    String[] regexFilter = new String[0];
    // Construct de regex filter
    if (attrs != null) {
      JsonObject attrJsonObject = getJsonAttr(model);
      if (attrJsonObject.containsKey(VARIANT_EXCLUDE)) {
        regexFilter =
            attrJsonObject
                .get(VARIANT_EXCLUDE)
                .toString()
                .replace("\"", "")
                .trim()
                .split("[\\\\n]+");
      }
    } else {
      return cardinal;
    }
    final List<String> regex = Stream.of(regexFilter).collect(Collectors.toList());
    // Filter:
    return cardinal
        .stream()
        .filter(
            url -> {
              return regex.stream().noneMatch(r -> url.matches(r));
            })
        .collect(Collectors.toList());
  }

  /**
   * @param product
   * @param url
   * @param index
   * @return
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Product createProductVariant(Product product, String url, int index) {
    // Check if product variant exist
    String productCode = String.format("%s-%04d", product.getCode(), index);
    ProductVariantAttr pvAttr =
        Query.of(ProductVariantAttr.class).filter("self.name = ?1", product.getCode()).fetchOne();
    if (pvAttr == null) {
      pvAttr = new ProductVariantAttr();
      pvAttr.setCode(product.getCode());
      pvAttr.setName(product.getCode());
      Beans.get(ProductVariantAttrRepository.class).save(pvAttr);
    }
    ProductVariantValue pvValue =
        variantService.createProductVariantValue(pvAttr, url, productCode, BigDecimal.ZERO);
    Beans.get(ProductVariantValueRepository.class).save(pvValue);
    ProductVariant pv =
        variantService.createProductVariant(
            pvAttr, null, null, null, pvValue, null, null, null, true);
    Product p = productService.createProduct(product, pv, index);
    p.setCode(productCode);
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
      String url,
      Product model,
      Product copy,
      int index,
      Map<String, String> variantMap,
      Map<String, Map<String, String>> extendConfig) {

    copy.setDefaultSupplierPartner(model.getDefaultSupplierPartner());
    // Update code
    copy.setCode(String.format("%s-%04d", model.getCode(), index));
    // Update description (replace <br>)
    copy.setName(String.format("%s-%04d", model.getName(), index));

    // Update internal description (replace <br>)
    copy.setInternalDescription(copy.getInternalDescription().replace("<br>", ""));
    //
    final Map<String, String> properties = new TreeMap<>();
    final Map<String, String> descriptionMap = new TreeMap<>();
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
                descriptionMap.put(pva.getName(), value.get().getName());
                properties.put(e.getKey(), value.get().getName());
              }
            });

    // Set IDX
    properties.put(VARIANT_IDX, String.format("%04d", index));
    String vurl = url == null ? getVariantURL(variantMap) : url;
    // Expand variants URL for grouping
    // Permits a single product share multiple variant values
    properties.put(VARIANT_URL, vurl);
    // Extend the productMap according extend configuration
    final Map<String, String> objectVariants = new TreeMap<>(variantMap);

    if (extendConfig != null) {

      extendConfig
          .entrySet()
          .forEach(
              e -> {
                if (vurl.matches(e.getKey())) {
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
        VARIANT_SEARCH, String.format("(%s){%d}", search, StringUtils.countMatches(search, "=")));

    String descriptionTable =
        descriptionMap
            .entrySet()
            .stream()
            .map(
                e ->
                    String.format(
                        "<tr><td><span style=\"font-weight: 700;\">%s</span></td><td>%s</td></tr>",
                        e.getKey(), e.getValue()))
            .collect(joining());
    copy.setDescription(
        String.format("%s<table>%s</table>", model.getDescription(), descriptionTable));
    // Create variant obj
    JsonObjectBuilder builder = Json.createObjectBuilder();
    variantMap.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
    properties.put(VARIANT_OBJECT, builder.build().toString());
    // Set json
    JsonObject json = enrich(getJsonAttr(model), properties);
    copy.setAttrs(json.toString());
  }

  private void checkVariantSearch(Stream<Product> stream) throws Exception {
    if (stream.count() > 1) {
      throw new Exception();
    }
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
              List<Product> childProductVariants = getChilds(e.getProduct());
              if (childProductVariants.size() > 0) {
                // res.add(e.getProduct());
                // productRepo.all().filter("self.parentProduct = ?1", e.getProduct()).fetch();
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
    Map<String, List<String>> res = new TreeMap<>();
    products
        .values()
        .forEach(
            prods ->
                prods.forEach(
                    prod -> {
                      String str =
                          getJsonAttr(prod).getString(VARIANT_OBJECT); // .replace("\"", "");
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
    res.entrySet().forEach(e -> Collections.sort(e.getValue()));
    return res;
  }

  /**
   * @param variant
   * @return
   */
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

    // Get the Existing Variants
    List<Product> childs = getChilds(model);

    // Retrieve filters from Json object of attrs
    String attrs = model.getAttrs();
    String[] regexFiler = new String[0];
    // Construct de regex filter
    if (attrs != null) {
      JsonObject attrJsonObject = getJsonAttr(model);
      if (attrJsonObject.containsKey(VARIANT_EXCLUDE)) {
        regexFiler =
            attrJsonObject
                .get(VARIANT_EXCLUDE)
                .toString()
                .replace("\"", "")
                .trim()
                .split("[\\\\n]+");
      }
    }
    final List<String> regex = Stream.of(regexFiler).collect(Collectors.toList());
    // Childs url
    final List<String> childUrls =
        childs
            .stream()
            .map(
                child -> {
                  return getVariantURL(child.getProductVariant());
                })
            .collect(Collectors.toList());
    // Filter: is not in child url and does not match regex
    return variants
        .stream()
        .filter(
            pv -> {
              String url = getVariantURL(pv);
              return !childUrls.contains(url) && !regex.stream().anyMatch(r -> url.matches(r));
            })
        .collect(Collectors.toList());
  }

  /**
   * Get the product child list of the model
   *
   * @param model
   * @return
   */
  private List<Product> getChilds(Product model) {
    return productRepo.all().filter("self.parentProduct = ?1", model).fetch();
  }

  /**
   * Get the variants count of the model
   *
   * @param model
   * @return
   */
  private int getChildCount(Product model) {
    return getChilds(model).size();
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

  private Map<String, String> getVariantMapCode(JsonObject productJsonObject) {

    Map<String, String> res = new LinkedHashMap<>();
    productJsonObject.entrySet().forEach(e -> res.put(e.getKey(), e.getValue().toString()));
    return res;
  }

  public String getVariantURL(ProductVariant variant) {
    return getVariantURL(getVariantMapCode(variant));
  }

  public String getVariantURL(Map<String, String> variantMap) {
    Map<String, String> result = new TreeMap<>();
    variantMap.entrySet().forEach(e -> result.put(e.getKey(), e.getValue()));
    return result
        .entrySet()
        .stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(joining("&"));
  }

  /**
   * Return a map as an expander list, used to extend the variant of a product Format is
   * [regex];PROP1=VAL1&PROP2=VAL2
   *
   * @param object Json object in key VARIANT_EXPAND
   * @return a Map with key as the regex pattern, the value is a map of {variant attribute, variant
   *     value}
   */
  public Map<String, Map<String, String>> getProductExtenderConfig(JsonObject object) {
    if (!object.containsKey(VARIANT_EXPAND) || object.get(VARIANT_EXPAND).toString().length() < 5)
      return null;
    // System.out.println(object.get(VARIANT_EXPAND).toString());
    String expander = (object.get(VARIANT_EXPAND).toString().replace("\"", "").trim());
    return Stream.of(
            object.get(VARIANT_EXPAND).toString().replace("\"", "").trim().split("[\\\\n]+"))
        .map(str -> str.split(";"))
        .collect(
            toMap(
                map -> map[0].trim(),
                map ->
                    Stream.of(map[1].split("&"))
                        .map(str2 -> str2.split("="))
                        .collect(toMap(map2 -> map2[0], map2 -> map2[1]))));
  }

  /**
   * @param variant
   * @return
   */
  public Map<String, String> getVariantMapCode(ProductVariant variant) {

    Map<String, String> res = new LinkedHashMap<>();
    if (variant == null) {
      return res;
    }
    if (variant.getProductVariantAttr1() != null)
      res.put(
          variant.getProductVariantAttr1().getCode(), variant.getProductVariantValue1().getCode());
    if (variant.getProductVariantAttr2() != null)
      res.put(
          variant.getProductVariantAttr2().getCode(), variant.getProductVariantValue2().getCode());
    if (variant.getProductVariantAttr3() != null)
      res.put(
          variant.getProductVariantAttr3().getCode(), variant.getProductVariantValue3().getCode());
    if (variant.getProductVariantAttr4() != null)
      res.put(
          variant.getProductVariantAttr4().getCode(), variant.getProductVariantValue4().getCode());
    return res;
  }

  /**
   * @param variant
   * @return
   */
  public Map<String, String> getVariantMapName(ProductVariant variant) {

    Map<String, String> res = new LinkedHashMap<>();
    if (variant == null) {
      return res;
    }
    if (variant.getProductVariantAttr1() != null)
      res.put(
          variant.getProductVariantAttr1().getCode(), variant.getProductVariantValue1().getName());
    if (variant.getProductVariantAttr2() != null)
      res.put(
          variant.getProductVariantAttr2().getCode(), variant.getProductVariantValue2().getName());
    if (variant.getProductVariantAttr3() != null)
      res.put(
          variant.getProductVariantAttr3().getCode(), variant.getProductVariantValue3().getName());
    if (variant.getProductVariantAttr4() != null)
      res.put(
          variant.getProductVariantAttr4().getCode(), variant.getProductVariantValue4().getName());
    return res;
  }

  /**
   * @param collections
   * @param <T>
   * @return
   */
  public <T> Stream<List<T>> ofCombinations(Collection<? extends Collection<T>> collections) {
    return ofCombinations(new ArrayList<Collection<T>>(collections), Collections.emptyList());
  }

  /**
   * @param collections
   * @param current
   * @param <T>
   * @return
   */
  private <T> Stream<List<T>> ofCombinations(
      List<? extends Collection<T>> collections, List<T> current) {
    return collections.isEmpty()
        ? Stream.of(current)
        : collections
            .get(0)
            .stream()
            .flatMap(
                e -> {
                  List<T> list = new ArrayList<T>(current);
                  list.add(e);
                  return ofCombinations(collections.subList(1, collections.size()), list);
                });
  }

  /**
   * Return the cardinal of every variants, original implementation
   *
   * @param productVariantConfig
   * @return
   */
  public List<ProductVariant> getVarianCardinal(ProductVariantConfig productVariantConfig) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr1() != null
        && productVariantConfig.getProductVariantValue1Set() != null) {

      for (ProductVariantValue productVariantValue1 :
          productVariantConfig.getProductVariantValue1Set()) {

        productVariantList.addAll(
            getProductVariantList(productVariantConfig, productVariantValue1));
      }
    }

    return productVariantList;
  }

  /**
   * Sub implementation of variant cardinal, attr1
   *
   * @param productVariantConfig
   * @param productVariantValue1
   * @return
   */
  private List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr2() != null
        && productVariantConfig.getProductVariantValue2Set() != null) {

      for (ProductVariantValue productVariantValue2 :
          productVariantConfig.getProductVariantValue2Set()) {

        productVariantList.addAll(
            getProductVariantList(
                productVariantConfig, productVariantValue1, productVariantValue2));
      }
    } else {

      productVariantList.add(
          createProductVariant(productVariantConfig, productVariantValue1, null, null, null));
    }

    return productVariantList;
  }

  /**
   * Sub implementation of variant cardinal, attr2
   *
   * @param productVariantConfig
   * @param productVariantValue1
   * @param productVariantValue2
   * @return
   */
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
            getProductVariantList(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3));
      }
    } else {

      productVariantList.add(
          createProductVariant(
              productVariantConfig, productVariantValue1, productVariantValue2, null, null));
    }

    return productVariantList;
  }

  /**
   * Sub implementation of variant cardinal, attr2
   *
   * @param productVariantConfig
   * @param productVariantValue1
   * @param productVariantValue2
   * @param productVariantValue3
   * @return
   */
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
            createProductVariant(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3,
                productVariantValue4));
      }
    } else {

      productVariantList.add(
          createProductVariant(
              productVariantConfig,
              productVariantValue1,
              productVariantValue2,
              productVariantValue3,
              null));
    }

    return productVariantList;
  }

  /**
   * Sub implementation of variant cardinal, attr4
   *
   * @param productVariantConfig
   * @param productVariantValue1
   * @param productVariantValue2
   * @param productVariantValue3
   * @param productVariantValue4
   * @return
   */
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

    return createProductVariant(
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

  /**
   * Create the product variant
   *
   * @param productVariantAttr1
   * @param productVariantAttr2
   * @param productVariantAttr3
   * @param productVariantAttr4
   * @param productVariantValue1
   * @param productVariantValue2
   * @param productVariantValue3
   * @param productVariantValue4
   * @param usedForStock
   * @return
   */
  private ProductVariant createProductVariant(
      ProductVariantAttr productVariantAttr1,
      ProductVariantAttr productVariantAttr2,
      ProductVariantAttr productVariantAttr3,
      ProductVariantAttr productVariantAttr4,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2,
      ProductVariantValue productVariantValue3,
      ProductVariantValue productVariantValue4,
      boolean usedForStock) {

    ProductVariant productVariant = new ProductVariant();
    productVariant.setProductVariantAttr1(productVariantAttr1);
    productVariant.setProductVariantAttr2(productVariantAttr2);
    productVariant.setProductVariantAttr3(productVariantAttr3);
    productVariant.setProductVariantAttr4(productVariantAttr4);

    productVariant.setProductVariantValue1(productVariantValue1);
    productVariant.setProductVariantValue2(productVariantValue2);
    productVariant.setProductVariantValue3(productVariantValue3);
    productVariant.setProductVariantValue4(productVariantValue4);

    productVariant.setUsedForStock(usedForStock);

    return productVariant;
  }
}
