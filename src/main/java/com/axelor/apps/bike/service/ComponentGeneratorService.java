package com.axelor.apps.bike.service;

import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface ComponentGeneratorService {
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public int generateProductSmartVariants(Product model, Boolean debug) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public int generateProductBOMVariants(Product model, Boolean debug) throws AxelorException;
}
