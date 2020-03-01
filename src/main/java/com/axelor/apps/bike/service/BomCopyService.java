package com.axelor.apps.bike.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface BomCopyService {

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void createBOMVariant(BillOfMaterial billOfMaterial);
}
