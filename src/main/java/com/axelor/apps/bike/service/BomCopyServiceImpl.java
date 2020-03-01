package com.axelor.apps.bike.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BomCopyServiceImpl implements BomCopyService {

  private static final Logger LOG = LoggerFactory.getLogger(BomCopyServiceImpl.class);

  @Inject private ProductRepository productRepository;
  @Inject private BillOfMaterialRepository billOfMaterialRepository;
  @Inject private ComponentGeneratorService componentGeneratorService;

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void createBOMVariant(BillOfMaterial billOfMaterial) {
    billOfMaterialRepository.save(billOfMaterial);

    // Selected
    /*Map<BillOfMaterial, Product> selectedSet = componentGeneratorService.searchUrl(url, productMap);
    LOG.debug(String.format("Found %d / %d", selectedSet.size(), targetSize));
    if (selectedSet.size() == targetSize) {

      LOG.debug(String.format("Url is matched"));
      // Copy the product
      Product productCopy = createProductVariant(model, url, index.incrementAndGet());
      // Get the map of variant in the URL
      Map<String, String> variantMap =
        Stream.of(url.split("&"))
          .map(str -> str.split("="))
          .collect(toMap(entry -> entry[0], entry -> entry[1]));
      // Update the product values
      final Map<String, Map<String, String>> extendConfig =
        getProductExtenderConfig(getJsonAttr(model));
      // Create a BOM version
      BillOfMaterial bomCopy = generateNewVersion(bomModel, index.get());
      updateProductCopy(url, model, productCopy, index.get(), variantMap, extendConfig);
      productCopy.setDefaultBillOfMaterial(bomCopy);
      productRepository.save(productCopy);

      // Update the bom with variant members
      selectedSet
        .entrySet()
        .forEach(
          prodBom -> {
            bomCopy.removeBillOfMaterialSetItem(prodBom.getKey());
            BillOfMaterial newBOM = null;
            // If Product has a default bom include it as subbom
            if (prodBom.getValue().getDefaultBillOfMaterial() != null) {
              newBOM = prodBom.getValue().getDefaultBillOfMaterial();
              newBOM.setDefineSubBillOfMaterial(true);
            } else {
              newBOM = new BillOfMaterial();
              newBOM.setProduct(prodBom.getValue());
              newBOM.setDefineSubBillOfMaterial(false);
            }
            newBOM.setUnit(prodBom.getKey().getUnit());
            newBOM.setQty(prodBom.getKey().getQty());
            newBOM.setPriority(prodBom.getKey().getPriority());
            bomCopy.addBillOfMaterialSetItem(newBOM);
          });
      // Save the bom and objects
      bomCopy.setProduct(productCopy);
      billOfMaterialRepository.save(bomCopy);
    } else {
      LOG.debug(String.format("Url is rejected", url));
    }*/
  }

  public BillOfMaterial generateNewVersion(BillOfMaterial billOfMaterial, int index) {

    BillOfMaterial copy = billOfMaterialRepository.copy(billOfMaterial, true);

    copy.setOriginalBillOfMaterial(billOfMaterial);
    copy.clearCostSheetList();
    copy.setCostPrice(BigDecimal.ZERO);
    copy.setOriginalBillOfMaterial(billOfMaterial);
    copy.setVersionNumber(index);

    return copy;
  }
}
