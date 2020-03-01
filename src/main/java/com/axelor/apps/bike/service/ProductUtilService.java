package com.axelor.apps.bike.service;

import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface ProductUtilService {
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void executeUtilMethod(String method, Long id, String filter) throws AxelorException;
}
