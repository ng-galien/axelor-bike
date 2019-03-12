package com.axelor.apps.bike.util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttr;
import com.axelor.apps.base.db.ProductVariantConfig;
import com.axelor.apps.base.db.ProductVariantValue;
import java.util.*;
import java.util.stream.Stream;
import javax.json.JsonObject;

public class VariantUtils {

  public static final String VARIANT_IDX = "variant_idx";
  public static final String VARIANT_URL = "variant_url";
  public static final String VARIANT_OBJECT = "variant_object";
  public static final String VARIANT_SEARCH = "variant_search";
  public static final String VARIANT_EXCLUDE = "variant_exclude";
  public static final String VARIANT_EXPAND = "variant_expand";

  public static Map<String, String> getVariantMapCode(JsonObject productJsonObject) {

    Map<String, String> res = new LinkedHashMap<>();
    productJsonObject.entrySet().forEach(e -> res.put(e.getKey(), e.getValue().toString()));
    return res;
  }

  public static String getVariantURL(ProductVariant variant) {
    return VariantUtils.getVariantURL(getVariantMapCode(variant));
  }

  public static String getVariantURL(Map<String, String> variantMap) {
    return variantMap
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
  public static Map<String, Map<String, String>> getProductExtenderConfig(JsonObject object) {
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
  public static Map<String, String> getVariantMapCode(ProductVariant variant) {

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
  public static Map<String, String> getVariantMapName(ProductVariant variant) {

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
  public static <T> Stream<List<T>> ofCombinations(
      Collection<? extends Collection<T>> collections) {
    return ofCombinations(new ArrayList<Collection<T>>(collections), Collections.emptyList());
  }

  /**
   * @param collections
   * @param current
   * @param <T>
   * @return
   */
  private static <T> Stream<List<T>> ofCombinations(
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
  public static List<ProductVariant> getVarianCardinal(ProductVariantConfig productVariantConfig) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr1() != null
        && productVariantConfig.getProductVariantValue1Set() != null) {

      for (ProductVariantValue productVariantValue1 :
          productVariantConfig.getProductVariantValue1Set()) {

        productVariantList.addAll(
            VariantUtils.getProductVariantList(productVariantConfig, productVariantValue1));
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
  private static List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr2() != null
        && productVariantConfig.getProductVariantValue2Set() != null) {

      for (ProductVariantValue productVariantValue2 :
          productVariantConfig.getProductVariantValue2Set()) {

        productVariantList.addAll(
            VariantUtils.getProductVariantList(
                productVariantConfig, productVariantValue1, productVariantValue2));
      }
    } else {

      productVariantList.add(
          VariantUtils.createProductVariant(
              productVariantConfig, productVariantValue1, null, null, null));
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
  private static List<ProductVariant> getProductVariantList(
      ProductVariantConfig productVariantConfig,
      ProductVariantValue productVariantValue1,
      ProductVariantValue productVariantValue2) {

    List<ProductVariant> productVariantList = new ArrayList<>();

    if (productVariantConfig.getProductVariantAttr3() != null
        && productVariantConfig.getProductVariantValue3Set() != null) {

      for (ProductVariantValue productVariantValue3 :
          productVariantConfig.getProductVariantValue3Set()) {

        productVariantList.addAll(
            VariantUtils.getProductVariantList(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3));
      }
    } else {

      productVariantList.add(
          VariantUtils.createProductVariant(
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
  private static List<ProductVariant> getProductVariantList(
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
            VariantUtils.createProductVariant(
                productVariantConfig,
                productVariantValue1,
                productVariantValue2,
                productVariantValue3,
                productVariantValue4));
      }
    } else {

      productVariantList.add(
          VariantUtils.createProductVariant(
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
  private static ProductVariant createProductVariant(
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

    return VariantUtils.createProductVariant(
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
  private static ProductVariant createProductVariant(
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
