package com.axelor.apps.bike.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.bike.web.ProductUtilController;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import groovy.json.JsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class ProductUtilServiceImpl implements ProductUtilService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductUtilServiceImpl.class);

  @Inject
  private StockLocationLineService stockLocationLineService;
  @Inject
  private ProductRepository productRepository;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  @Override
  public void executeUtilMethod(String method, Long id, String filter) throws AxelorException {
    try {
      LOG.debug(String.format("executeUtilMethod %s %d %s", method, id, filter));
      this.getClass().getMethod(method, Long.class, String.class).invoke(this, id, filter);
      LOG.debug(String.format("end execution", method, id, filter));
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
      throw new AxelorException(4, ex.getMessage());
    }
  }

  public void test(Long id, String filter) {
    LOG.debug(String.format("test id=%d, filter=%s", id, filter));
    List<Product> products = new ArrayList<>();
    if(id > 0) {
      products.add( productRepository.find(id) );
    } else {
      products.addAll(productRepository.all().filter(filter).fetch());
    }
    LOG.debug(String.format("%d Products are found", products.size()));
  }

  /**
   * Reconstruct JSON object for product with the URL
   * @param id
   * @param filter
   */
  public void rebuildJson(Long id, String filter) {
    List<Product> products = new ArrayList<>();
    if(id > 0) {
      Product product = productRepository.find(id);
      if(product != null) {
        products.add( product );
      }

    } else {
      List<Product> prods = productRepository.all().filter(filter).fetch();
      if(prods != null && prods.size() > 0) {
        products.addAll(prods);
      }
    }
    final AtomicInteger count = new AtomicInteger(products.size());
    final AtomicInteger progress = new AtomicInteger(0);
    products.forEach( product -> {
      LOG.debug(String.format("Process %04d|%d %s", progress.incrementAndGet(), count.get(), product.getCode()));
      String attr = product.getAttrs();
      if (attr != null) {
        JsonReader jsonReader = Json.createReader(new StringReader(attr));
        JsonObject attrJsonObject = jsonReader.readObject();
        String url = attrJsonObject.getString("variant_url");
        if(url != null) {

          Map<String, String> variantMap =
            Stream.of(url.split("&"))
              .map(str -> str.split("="))
              .collect(toMap(entry -> entry[0], entry -> entry[1]));
          JsonObjectBuilder variantObj = getBuilder();
          variantMap.entrySet().forEach(e -> {
            String code = e.getValue();
            String name = attrJsonObject.getString(e.getKey());
            variantObj.add(e.getKey(),
              getBuilder().add("code", code).add("name", name).build());
          });
          JsonObjectBuilder newAttrs = getBuilder();
          attrJsonObject.entrySet().forEach(e -> newAttrs.add(e.getKey(), e.getValue()));
          newAttrs.add("variant_props", variantObj.build());
          //String attrsString = newAttrs.build().toString();
          product.setAttrs(newAttrs.build().toString());
          productRepository.save(product);
        }
      }
    });
  }

  /**
   *
   * @param id
   * @param filter
   */
  public void fixStock(Long id, String filter)  {
    List<Product> products = new ArrayList<>();
    if(id > 0) {
      Product product = productRepository.find(id);
      if(product != null) {
        products.add( product );
      }

    } else {
      List<Product> prods = productRepository.all().filter(filter).fetch();
      if(prods != null && prods.size() > 0) {
        products.addAll(prods);
      }
    }
    final AtomicInteger count = new AtomicInteger(products.size());
    final AtomicInteger progress = new AtomicInteger(0);
    products.forEach( product -> {
      LOG.debug(String.format("Process %04d|%d %s", progress.incrementAndGet(), count.get(), product.getCode()));

        List<StockLocationLine> stockLocationLines = stockLocationLineService.getStockLocationLines(product);
        stockLocationLines.forEach( line -> {
          try {
            //if(line.getCurrentQty() != line.getFutureQty())
            stockLocationLineService.updateStockLocationFromProduct(line, product);
          }catch (AxelorException e) {
            e.printStackTrace();
          }
        });
    });
  }

  /**
   * Fix product fullname with saving it
   * @param id
   * @param filter
   */
  public void fixFullName(Long id, String filter)  {
    List<Product> products = new ArrayList<>();
    if(id > 0) {
      Product product = productRepository.find(id);
      if(product != null) {
        products.add( product );
      }

    } else {
      List<Product> prods = productRepository.all().filter(filter).fetch();
      if(prods != null && prods.size() > 0) {
        products.addAll(prods);
      }
    }
    final AtomicInteger count = new AtomicInteger(products.size());
    final AtomicInteger progress = new AtomicInteger(0);
    products.forEach( product -> {
      LOG.debug(String.format("Process %04d|%d %s", progress.incrementAndGet(), count.get(), product.getCode()));
      productRepository.save(product);
    });
  }



  private JsonObjectBuilder getBuilder() {
    JsonBuilderFactory factory = Json.createBuilderFactory(new HashMap<>());
    return factory.createObjectBuilder();
  }
}
