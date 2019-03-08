package com.axelor.apps.bike.service;

import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface ComponentGeneratorService {
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void generateProductSmartVariants(Product model) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void generateProductBOMVariants(Product model) throws AxelorException;
}
