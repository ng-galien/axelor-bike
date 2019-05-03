package com.axelor.apps.bike.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.bike.web.ProductUtilController;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import groovy.json.JsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ProductUtilServiceImpl implements ProductUtilService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductUtilServiceImpl.class);

  @Inject
  private ProductRepository productRepository;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  @Override
  public void executeUtilMethod(String method, Long id) throws AxelorException {
    try {
      LOG.debug(String.format("executeUtilMethod %s %d", method, id));
      this.getClass().getMethod(method, Long.class).invoke(this, id);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
      throw new AxelorException(4, ex.getMessage());
    }
  }

  public void test(Long id) {
    LOG.debug("test "+id);
  }

  public void fixJson(Long id) {
    Product product = productRepository.find(id);
    String attr = product.getAttrs();
    if (attr == null) {
      attr = "{}";
    }
    JsonReader jsonReader = Json.createReader(new StringReader(attr));
    JsonObject attrJsonObject = jsonReader.readObject();
    String url = attrJsonObject.getString("variant_url");
    Map<String, String> variantMap =
      Stream.of(url.split("&"))
        .map(str -> str.split("="))
        .collect(toMap(entry -> entry[0], entry -> entry[1]));
    JsonObjectBuilder vairantObj = getBuilder();
    variantMap.entrySet().forEach(e -> {
      vairantObj.add(e.getKey(), e.getValue());
    });
    JsonObjectBuilder newAttrs = getBuilder();
    attrJsonObject.entrySet().forEach(e -> newAttrs.add(e.getKey(), e.getValue()));
    newAttrs.add("variant_object", vairantObj.build());
  }

  private JsonObjectBuilder getBuilder() {
    JsonBuilderFactory factory = Json.createBuilderFactory(new HashMap<>());
    return factory.createObjectBuilder();
  }
}
