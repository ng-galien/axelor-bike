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
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProductGeneratorController {
  private static final Logger LOG = LoggerFactory.getLogger(ProductGeneratorController.class);

  @Inject private AppBikeService bikeService;
  @Inject private ComponentGeneratorService componentGeneratorService;
  @Inject private ProductRepository productRepo;
  @Inject private BillOfMaterialService billOfMaterialService;

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

  public void showStats(ActionRequest request, ActionResponse response) throws AxelorException {
    long id = (Long) request.getContext().get("id");
    Product product = productRepo.find(id);
    BillOfMaterial bom = product.getDefaultBillOfMaterial();
    if (bom == null) {
      response.setFlash("Pas de nomenclature pour le produit");
    } else {
      int queryResult;
      EntityTransaction transaction;

      // billOfMaterialService.generateTree(bom);
      String removeBomInfo = "delete from production_bom_info where product_root = ?1";
      LOG.debug("Remove old bom_info for " + product.getId());
      transaction = JPA.em().getTransaction();
      transaction.begin();
      queryResult =
          JPA.em()
              .createNativeQuery(removeBomInfo)
              .setParameter(1, product.getCode())
              .executeUpdate();
      transaction.commit();
      LOG.debug("Result " + queryResult);

      String populateBomInfo =
          "with recursive recursive_bom as\n"
              + "(\n"
              + "select \n"
              + "\tbom_set.bill_of_material_set as bom_id,\n"
              + "\tparent_product.code as p_product_code,\n"
              + "\tchild_product.id as c_product_id,\n"
              + "\tchild_product.code as c_product_code,\n"
              + "\tchild_product.name as c_product_name,\n"
              + "\tchild_bom.qty as c_product_qty\n"
              + "from production_bill_of_material_bill_of_material_set bom_set\n"
              + "join production_bill_of_material parent_bom on parent_bom.id = bom_set.production_bill_of_material\n"
              + "join base_product parent_product on parent_product.id = parent_bom.product\n"
              + "join production_bill_of_material child_bom on child_bom.id = bom_set.bill_of_material_set\n"
              + "join base_product child_product on child_product.id = child_bom.product\n"
              + "where parent_product.code = ?1\n"
              + "\n"
              + "union all\n"
              + "\n"
              + "select \n"
              + "\tbom_set.bill_of_material_set as bom_id,\n"
              + "\tparent_product.code as p_product_code,\n"
              + "\tchild_product.id as c_product_id,\n"
              + "\tchild_product.code as c_product_code,\n"
              + "\tchild_product.name as c_product_name,\n"
              + "\tchild_bom.qty as c_product_qty\n"
              + "\n"
              + "from production_bill_of_material_bill_of_material_set bom_set\n"
              + "join production_bill_of_material parent_bom on parent_bom.id = bom_set.production_bill_of_material\n"
              + "join base_product parent_product on parent_product.id = parent_bom.product\n"
              + "join production_bill_of_material child_bom on child_bom.id = bom_set.bill_of_material_set\n"
              + "join base_product child_product on child_product.id = child_bom.product\n"
              + "join recursive_bom recursive_bom on recursive_bom.bom_id = bom_set.production_bill_of_material\n"
              + "\n"
              + ")\n"
              + "insert into production_bom_info(id, product_root, product_code, product_name, supplier_name, quantity, stock, max_product, purchase_price, stock_value)\n"
              + "select\n"
              + "\tnextval('production_bom_info_seq'),\n"
              + "\t?1,\n"
              + "\tflat_bom.c_product_code,\n"
              + "\tflat_bom.c_product_name,\n"
              + "\tcoalesce(partner.\"name\", 'Aucun'),\n"
              + "\tsum(flat_bom.c_product_qty),\n"
              + "\tcoalesce(stock.current_qty, 0 ),\n"
              + "\tcoalesce(floor(stock.current_qty/sum(flat_bom.c_product_qty)), 0),\n"
              + "\tproduct.purchase_price,\n"
              + "\tproduct.purchase_price * coalesce(stock.current_qty, 0 )\n"
              + "from recursive_bom flat_bom\n"
              + "\n"
              + "join base_product product on product.id = flat_bom.c_product_id\n"
              + "left join base_partner as partner on partner.id = product.default_supplier_partner\n"
              + "left join stock_stock_location_line stock on stock.product = product.id and stock.stock_location = 10\n"
              + "where \n"
              + "\tproduct.code like 'COMP-%'\n"
              + "group by \n"
              + "\tflat_bom.c_product_code,\n"
              + "\tflat_bom.c_product_name,\n"
              + "\tpartner.\"name\",\n"
              + "\tstock.current_qty,\n"
              + "\tproduct.purchase_price\n"
              + "order by \n"
              + "\tflat_bom.c_product_code asc\n";
      /*populateBomInfo =
      "with recursive flat_bom as\n"
          + "(\n"
          + "select\n"
          + "\tptree.id as tree_id\n"
          + "\t,ptree.qty as qty\n"
          + "\tfrom production_temp_bom_tree ptree\n"
          + "\tinner join base_product base_prod on ptree.product = base_prod.id\n"
          + "\twhere base_prod.code = ?1\n"
          + "\n"
          + "union\n"
          + "select\n"
          + "\tctree.id as tree_id\n"
          + "\t,ctree.qty as qty\n"
          + "\tfrom production_temp_bom_tree ctree\n"
          + "\tinner join flat_bom bom on bom.tree_id = ctree.parent\n"
          + ")\n"
          + "insert into production_bom_info(id, product_root, product_code, product_name, supplier_name, quantity, stock, max_product, purchase_price, stock_value)\n"
          + "select\n"
          + "\tnextval('production_bom_info_seq')\n"
          + "\t,?1\n"
          + "\t,product.code\n"
          + "\t,product.\"name\"\n"
          + "\t,COALESCE(partner.\"name\", 'Aucun')\n"
          + "\t,sum(bom.qty)\n"
          + "\t,COALESCE(stock.current_qty, 0 )\n"
          + "\t,coalesce(floor(stock.current_qty/sum(bom.qty)), 0)\n"
          + "\t,product.purchase_price\n"
          + "\t,product.purchase_price * COALESCE(stock.current_qty, 0 )\n"
          + "\tfrom flat_bom bom \n"
          + "\tjoin production_temp_bom_tree tree on tree.id = tree_id\n"
          + "\tjoin base_product product on product.id = tree.product\n"
          + "\tleft join base_partner as partner on partner.id = product.default_supplier_partner\n"
          + "\tleft join stock_stock_location_line stock on stock.product = product.id and stock.stock_location = 10\n"
          + "\twhere \n"
          + "\t\tproduct.code like 'COMP-%'\n"
          + "\tgroup by \n"
          + "\t\tproduct.code\n"
          + "\t\t,product.\"name\"\n"
          + "\t\t,partner.\"name\"\n"
          + "\t\t,stock.current_qty\n"
          + "\t\t,product.purchase_price\n"
          + "\torder by product.code ASC";*/
      LOG.debug("Insert bom_info for " + product.getId());
      transaction = JPA.em().getTransaction();
      transaction.begin();
      queryResult =
          JPA.em()
              .createNativeQuery(populateBomInfo)
              .setParameter(1, product.getCode())
              .executeUpdate();
      LOG.debug("Result " + queryResult);
      transaction.commit();

      response.setView(
          // ActionView.define(I18n.get("Infos "))
          ActionView.define("Infos " + product.getName())
              .add("dashboard", "bike.dashboard.rvXX")
              .context("_root_id", product.getCode())
              .map());
    }
  }

  public void updateBom(ActionRequest request, ActionResponse response) throws AxelorException {
    Product product = request.getContext().asType(Product.class);
    product = productRepo.find(product.getId());
    componentGeneratorService.updateBOM(product);
  }

  public void addToFavorite(ActionRequest request, ActionResponse response) throws AxelorException {
    long id = (Long) request.getContext().get("id");
    Product product = productRepo.find(id);
    if (product != null) {
      componentGeneratorService.setFavorite(product, true);
    }
  }

  public void removeFavorite(ActionRequest request, ActionResponse response)
      throws AxelorException {
    long id = (Long) request.getContext().get("id");
    Product product = productRepo.find(id);
    if (product != null) {
      componentGeneratorService.setFavorite(product, false);
      response.setReload(true);
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
