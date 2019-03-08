package com.axelor.apps.bike.util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import com.axelor.apps.base.db.ProductVariant;
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
    return getVariantMapCode(variant)
        .entrySet()
        .stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(joining("&"));
  }

  /**
   * Return a map as an expander list, used to extend the variant of a product
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

  public static <T> Stream<List<T>> ofCombinations(
      Collection<? extends Collection<T>> collections) {
    return ofCombinations(new ArrayList<Collection<T>>(collections), Collections.emptyList());
  }

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
}
