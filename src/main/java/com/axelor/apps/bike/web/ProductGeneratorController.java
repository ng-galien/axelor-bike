package com.axelor.apps.bike.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.bike.exceptions.IExceptionMessage;
import com.axelor.apps.bike.service.ComponentGeneratorService;
import com.axelor.apps.bike.service.app.AppBikeService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProductGeneratorController {
  private static final Logger LOG = LoggerFactory.getLogger(ProductGeneratorController.class);

  @Inject private AppBikeService bikeService;
  @Inject private ComponentGeneratorService componentGeneratorService;
  @Inject private ProductRepository productRepo;

  public void generateSmartVariants(ActionRequest request, ActionResponse response)
      throws AxelorException {

    LOG.debug("generateSmartVariants");

    Boolean debug = bikeService.getAppBike().getDebug();

    Product product = request.getContext().asType(Product.class);
    product = productRepo.find(product.getId());

    int count = 0;
    String message = IExceptionMessage.NONE_GENERATED;

    if (product.getProductVariantConfig() != null) {

      count = componentGeneratorService.generateProductSmartVariants(product, debug);
      message = IExceptionMessage.PRODUCT_GENERATED;

    } else if (product.getDefaultBillOfMaterial() != null) {

      count = componentGeneratorService.generateProductBOMVariants(product, debug);
      message = IExceptionMessage.BOM_GENERATED;
    }
    response.setFlash(String.format(message, count));
    if (count > 0) {
      response.setReload(true);
    }
  }

  public void testQuery(ActionRequest request, ActionResponse response) throws AxelorException {
    Query.of(Product.class).filter("json_text(self.attrs, 'variant_id') != null").fetch();
  }
}
