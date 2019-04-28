package com.axelor.apps.bike.service;

import com.axelor.apps.bike.web.ProductUtilController;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class ProductUtilServiceImpl implements ProductUtilService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductUtilServiceImpl.class);
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

  protected void fixJson(Long id) {

  }
}
