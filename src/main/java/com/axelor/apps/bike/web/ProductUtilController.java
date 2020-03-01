package com.axelor.apps.bike.web;

import com.axelor.apps.bike.service.ProductUtilService;
import com.axelor.apps.bike.service.app.AppBikeService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProductUtilController {

  private static final Logger LOG = LoggerFactory.getLogger(ProductUtilController.class);
  @Inject private AppBikeService bikeService;
  @Inject private ProductUtilService productUtilService;

  public void call(ActionRequest request, ActionResponse response) throws AxelorException {
    String method = bikeService.getAppBike().getMethodName();
    Long arg = bikeService.getAppBike().getObjectId();
    String filter = bikeService.getAppBike().getSearchFilter();
    productUtilService.executeUtilMethod(method, arg, filter);

  }
}
