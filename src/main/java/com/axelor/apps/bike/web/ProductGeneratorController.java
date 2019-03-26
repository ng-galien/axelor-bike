package com.axelor.apps.bike.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.bike.exceptions.IExceptionMessage;
import com.axelor.apps.bike.service.ComponentGeneratorService;
import com.axelor.apps.bike.service.app.AppBikeService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

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

  public void deleteBomVariants(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Product product = request.getContext().asType(Product.class);
    product = productRepo.find(product.getId());
    componentGeneratorService.deleteBOMVariant(product);
    response.setReload(true);
  }

  public void testQuery(ActionRequest request, ActionResponse response) throws AxelorException {
    Query.of(Product.class).filter("json_text(self.attrs, 'variant_id') != null").fetch();
  }

  public void addToFavorite(ActionRequest request, ActionResponse response) throws AxelorException {
    long id = (Long)request.getContext().get("id");
    Product product = productRepo.find(id);
    if(product != null) {
      componentGeneratorService.setFavorite(product, true);
    }
  }

  public void removeFavorite(ActionRequest request, ActionResponse response) throws AxelorException {
    long id = (Long)request.getContext().get("id");
    Product product = productRepo.find(id);
    if(product != null) {
      componentGeneratorService.setFavorite(product, false);
    }
  }

  /**
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void printVariantCatalog(ActionRequest request, ActionResponse response)
      throws AxelorException {

    User user = Beans.get(UserService.class).getUser();

    Product product = request.getContext().asType(Product.class);

    int currentYear = Beans.get(AppBaseService.class).getTodayDateTime().getYear();

    String productIds = "";

    List<Long> lstSelectedProduct =
        productRepo
            .all()
            .filter("self.parentProduct = ?1", product)
            .fetch()
            .stream()
            .map(p -> p.getId())
            .collect(Collectors.toList());

    if (lstSelectedProduct != null) {
      productIds = Joiner.on(",").join(lstSelectedProduct);
    }

    String name = String.format("Variantes: %s", product.getName());

    String fileLink =
        ReportFactory.createReport(IReport.PRODUCT_CATALOG, name + "-${date}")
            .addParam("UserId", user.getId())
            .addParam("CurrYear", Integer.toString(currentYear))
            .addParam("ProductIds", productIds)
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .generate()
            .getFileLink();

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }
}
