package com.axelor.apps.bike.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.Map;

public interface ComponentGeneratorService {
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  int generateProductSmartVariants(Product model, Boolean debug) throws AxelorException;

  int generateProductBOMVariants(Product model, Boolean debug) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void copyBOM(
      Product productCopy, BillOfMaterial bomCopy, Map<BillOfMaterial, Product> selectedSet);

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void renameVariant(Product model) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void deleteBOMVariant(Product model) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  Product createProductVariant(Product product, String url, int index);

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void setFavorite(Product product, Boolean favorite);
}
