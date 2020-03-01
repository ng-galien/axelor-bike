package com.axelor.apps.bike.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.bike.service.ComponentGeneratorService;
import com.axelor.apps.bike.service.ComponentGeneratorServiceImpl;
import com.axelor.apps.bike.service.ProductUtilService;
import com.axelor.apps.bike.service.ProductUtilServiceImpl;
import com.axelor.apps.bike.service.app.AppBikeService;
import com.axelor.apps.bike.service.app.AppBikeServiceImpl;

public class BikeModule extends AxelorModule {
  @Override
  protected void configure() {
    bind(AppBikeService.class).to(AppBikeServiceImpl.class);
    bind(ComponentGeneratorService.class).to(ComponentGeneratorServiceImpl.class);
    bind(ProductUtilService.class).to(ProductUtilServiceImpl.class);
  }
}
