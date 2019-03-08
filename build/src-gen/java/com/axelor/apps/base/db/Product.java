/*
 * Axelor Business Solutions
 * 
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 * 
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheetGroup;
import com.axelor.apps.production.db.ProductApproval;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.CustomerCatalog;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.stock.db.CustomsCodeNomenclature;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.supplychain.db.MrpFamily;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Track;
import com.axelor.db.annotations.TrackEvent;
import com.axelor.db.annotations.TrackField;
import com.axelor.db.annotations.TrackMessage;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "BASE_PRODUCT", indexes = { @Index(columnList = "name"), @Index(columnList = "picture"), @Index(columnList = "product_category"), @Index(columnList = "product_family"), @Index(columnList = "unit"), @Index(columnList = "sale_currency"), @Index(columnList = "purchase_currency"), @Index(columnList = "product_variant_config"), @Index(columnList = "product_variant"), @Index(columnList = "parent_product"), @Index(columnList = "default_supplier_partner"), @Index(columnList = "bar_code"), @Index(columnList = "barcode_type_config"), @Index(columnList = "fullName"), @Index(columnList = "mass_unit"), @Index(columnList = "length_unit"), @Index(columnList = "purchases_unit"), @Index(columnList = "sales_unit"), @Index(columnList = "tracking_number_configuration"), @Index(columnList = "customs_code_nomenclature"), @Index(columnList = "country_of_origin"), @Index(columnList = "mrp_family"), @Index(columnList = "default_bill_of_material"), @Index(columnList = "cost_sheet_group"), @Index(columnList = "product_approval") })
@Track(on = TrackEvent.UPDATE, fields = { @TrackField(name = "productCategory"), @TrackField(name = "productFamily"), @TrackField(name = "saleSupplySelect"), @TrackField(name = "sellable"), @TrackField(name = "salePrice"), @TrackField(name = "saleCurrency"), @TrackField(name = "unit"), @TrackField(name = "startDate"), @TrackField(name = "endDate"), @TrackField(name = "purchasable"), @TrackField(name = "purchasePrice"), @TrackField(name = "defaultSupplierPartner"), @TrackField(name = "purchaseCurrency"), @TrackField(name = "supplierDeliveryTime"), @TrackField(name = "costPrice"), @TrackField(name = "managPriceCoef"), @TrackField(name = "costTypeSelect"), @TrackField(name = "hasWarranty"), @TrackField(name = "warrantyNbrOfMonths"), @TrackField(name = "isPerishable"), @TrackField(name = "perishableNbrOfMonths"), @TrackField(name = "purchasesUnit"), @TrackField(name = "trackingNumberConfiguration") }, messages = { @TrackMessage(message = "Product updated", condition = "true", on = TrackEvent.UPDATE) })
public class Product extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BASE_PRODUCT_SEQ")
	@SequenceGenerator(name = "BASE_PRODUCT_SEQ", sequenceName = "BASE_PRODUCT_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name", translatable = true)
	@NotNull
	private String name;

	@Widget(title = "Serial Nbr")
	private String serialNumber;

	@Widget(title = "Product N°", readonly = true)
	private String sequence;

	@HashKey
	@Widget(title = "Code")
	@NotNull
	@Column(unique = true)
	private String code;

	@Widget(title = "Description", translatable = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String description;

	@Widget(title = "Internal description")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String internalDescription;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaFile picture;

	@Widget(title = "Product category", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ProductCategory productCategory;

	@Widget(title = "Family", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ProductFamily productFamily;

	@Widget(title = "Unit", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Unit unit;

	@Widget(title = "Sale supply default method on sale order", selection = "product.sale.supply.select", massUpdate = true)
	private Integer saleSupplySelect = 0;

	@Widget(title = "Type", selection = "product.product.type.select", massUpdate = true)
	@NotNull
	private String productTypeSelect;

	@Widget(title = "Procurement method", selection = "product.procurement.method.select")
	private String procurementMethodSelect;

	@Widget(title = "Prototype")
	private Boolean isPrototype = Boolean.FALSE;

	@Widget(title = "Unrenewed")
	private Boolean isUnrenewed = Boolean.FALSE;

	@Widget(title = "Product Subtype", selection = "product.sub.type.product.select")
	private Integer productSubTypeSelect = 0;

	@Widget(title = "Inventory type", selection = "product.inventory.type.select")
	private Integer inventoryTypeSelect = 0;

	@Widget(title = "Sale price W.T.", massUpdate = true)
	@Digits(integer = 10, fraction = 10)
	private BigDecimal salePrice = BigDecimal.ZERO;

	@Widget(title = "Sale currency")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Currency saleCurrency;

	@Widget(title = "Purchase price W.T.", massUpdate = true)
	@Digits(integer = 10, fraction = 10)
	private BigDecimal purchasePrice = BigDecimal.ZERO;

	@Widget(title = "Purchase / Cost currency")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Currency purchaseCurrency;

	@Widget(title = "Update sale price from cost price")
	private Boolean autoUpdateSalePrice = Boolean.FALSE;

	@Widget(title = "Cost price", massUpdate = true)
	@Digits(integer = 10, fraction = 10)
	private BigDecimal costPrice = BigDecimal.ZERO;

	@Widget(title = "Management coef.")
	private BigDecimal managPriceCoef = BigDecimal.ZERO;

	@Widget(title = "Shipping Coef.")
	private BigDecimal shippingCoef = BigDecimal.ZERO;

	@Widget(title = "Define the shipping coef by partner")
	private Boolean defShipCoefByPartner = Boolean.FALSE;

	@Widget(title = "Product launch Date")
	private LocalDate startDate;

	@Widget(title = "Product pulled off market Date")
	private LocalDate endDate;

	@Widget(title = "Warranty")
	private Boolean hasWarranty = Boolean.FALSE;

	@Widget(title = "Perishable")
	private Boolean isPerishable = Boolean.FALSE;

	@Widget(title = "Warranty length (in months)")
	private Integer warrantyNbrOfMonths = 0;

	@Widget(title = "Time before expiry (in months)")
	private Integer perishableNbrOfMonths = 0;

	private Boolean checkExpirationDateAtStockMoveRealization = Boolean.FALSE;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ProductVariantConfig productVariantConfig;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ProductVariant productVariant;

	@Widget(title = "Parent product")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Product parentProduct;

	@Widget(title = "Is model")
	private Boolean isModel = Boolean.FALSE;

	@Widget(title = "Manage prices for product variants")
	private Boolean manageVariantPrice = Boolean.FALSE;

	@Widget(title = "Default supplier")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Partner defaultSupplierPartner;

	@Widget(title = "Accounts configuration")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccountManagement> accountManagementList;

	@Widget(title = "Sellable")
	private Boolean sellable = Boolean.TRUE;

	@Widget(title = "Purchasable")
	private Boolean purchasable = Boolean.TRUE;

	@Widget(title = "In ATI")
	private Boolean inAti = Boolean.FALSE;

	@Widget(title = "Cost type", selection = "base.product.cost.type.select")
	private Integer costTypeSelect = 1;

	@Widget(title = "Supplier delivery time (days)")
	private Integer supplierDeliveryTime = 0;

	@Widget(title = "Barcode")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaFile barCode;

	@Widget(title = "Barcode Type")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private BarcodeTypeConfig barcodeTypeConfig;

	@Widget(title = "Full name", translatable = true)
	@NameColumn
	private String fullName;

	@Widget(title = "Unit of mass")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Unit massUnit;

	@Widget(title = "Gross mass")
	@Digits(integer = 17, fraction = 3)
	private BigDecimal grossMass = BigDecimal.ZERO;

	@Widget(title = "Net mass")
	@Digits(integer = 17, fraction = 3)
	private BigDecimal netMass = BigDecimal.ZERO;

	@Widget(title = "Unit of length")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Unit lengthUnit;

	@Widget(title = "Length")
	private BigDecimal length = new BigDecimal("0");

	@Widget(title = "Width")
	private BigDecimal width = new BigDecimal("0");

	@Widget(title = "Height")
	private BigDecimal height = new BigDecimal("0");

	@Widget(title = "Diameter")
	private BigDecimal diameter = BigDecimal.ZERO;

	@Widget(title = "Article volume")
	private BigDecimal articleVolume = BigDecimal.ZERO;

	@Widget(title = "Economic manuf. qty")
	private BigDecimal economicManufOrderQty = BigDecimal.ZERO;

	@Widget(title = "Is shipping costs product")
	private Boolean isShippingCostsProduct = Boolean.FALSE;

	@Widget(title = "Multiple quantities")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "saleProduct", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("multipleQty")
	private List<ProductMultipleQty> saleProductMultipleQtyList;

	@Widget(title = "Allow to force sales quantities")
	private Boolean allowToForceSaleQty = Boolean.FALSE;

	@Widget(title = "Multiple quantities")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "purchaseProduct", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("multipleQty")
	private List<ProductMultipleQty> purchaseProductMultipleQtyList;

	@Widget(title = "Allow to force purchases quantities")
	private Boolean allowToForcePurchaseQty = Boolean.FALSE;

	@Widget(title = "Purchases unit", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Unit purchasesUnit;

	@Widget(title = "Supplier Catalog Lines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SupplierCatalog> supplierCatalogList;

	@Widget(title = "Sales unit", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Unit salesUnit;

	@Widget(title = "Is pack")
	private Boolean isFromConfigurator = Boolean.FALSE;

	@Widget(title = "Pack lines")
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<PackLine> packLines;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private Configurator configurator;

	@Widget(title = "Pack price select", selection = "product.pack.price.select")
	private Integer packPriceSelect = 0;

	@Widget(title = "Customer catalog lines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CustomerCatalog> customerCatalogList;

	@Widget(title = "Average Price", readonly = true)
	@Digits(integer = 10, fraction = 10)
	private BigDecimal avgPrice = BigDecimal.ZERO;

	@Widget(title = "Tracking Nbr. Config.", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private TrackingNumberConfiguration trackingNumberConfiguration;

	@Widget(title = "Customs code", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private CustomsCodeNomenclature customsCodeNomenclature;

	@Widget(title = "Control on Receipt", massUpdate = true)
	private Boolean controlOnReceipt = Boolean.FALSE;

	@Widget(title = "Used in DEB/DES", massUpdate = true)
	private Boolean usedInDEB = Boolean.FALSE;

	@Widget(title = "Country of origin", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Country countryOfOrigin;

	@Widget(massUpdate = true)
	private Boolean stockManaged = Boolean.TRUE;

	@Widget(title = "Use for Analytic Capture")
	private Boolean isAnalyticCapture = Boolean.FALSE;

	@Widget(title = "MRP family", massUpdate = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MrpFamily mrpFamily;

	@Widget(title = "Exclude from MRP", massUpdate = true)
	private Boolean excludeFromMrp = Boolean.FALSE;

	@Widget(title = "Standard delay (days)")
	private Integer standardDelay = 0;

	@Widget(title = "Last production price")
	@Digits(integer = 10, fraction = 10)
	private BigDecimal lastProductionPrice = BigDecimal.ZERO;

	@Widget(title = "Default BOM")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private BillOfMaterial defaultBillOfMaterial;

	@Widget(title = "Version", selection = "base.product.version.select")
	private Integer versionSelect = 0;

	@Widget(title = "Cost sheet group")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private CostSheetGroup costSheetGroup;

	@Widget(title = "MO's valuation method", selection = "production.product.price.method.select")
	private Integer realOrEstimatedPriceSelect = 1;

	@Widget(title = "Components valuation method", selection = "production.product.components.valuation.select")
	private Integer componentsValuationMethod = 0;

	@Widget(title = "Production.productStandard")
	private String productStandard;

	@Widget(title = "Approval")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ProductApproval productApproval;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public Product() {
	}

	public Product(String name, String code, String description, String internalDescription, MetaFile picture, ProductCategory productCategory, ProductFamily productFamily, Unit unit, Integer saleSupplySelect, String productTypeSelect, String procurementMethodSelect, Currency saleCurrency, Currency purchaseCurrency, LocalDate startDate, LocalDate endDate) {
		this.name = name;
		this.code = code;
		this.description = description;
		this.internalDescription = internalDescription;
		this.picture = picture;
		this.productCategory = productCategory;
		this.productFamily = productFamily;
		this.unit = unit;
		this.saleSupplySelect = saleSupplySelect;
		this.productTypeSelect = productTypeSelect;
		this.procurementMethodSelect = procurementMethodSelect;
		this.saleCurrency = saleCurrency;
		this.purchaseCurrency = purchaseCurrency;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInternalDescription() {
		return internalDescription;
	}

	public void setInternalDescription(String internalDescription) {
		this.internalDescription = internalDescription;
	}

	public MetaFile getPicture() {
		return picture;
	}

	public void setPicture(MetaFile picture) {
		this.picture = picture;
	}

	public ProductCategory getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(ProductCategory productCategory) {
		this.productCategory = productCategory;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Integer getSaleSupplySelect() {
		return saleSupplySelect == null ? 0 : saleSupplySelect;
	}

	public void setSaleSupplySelect(Integer saleSupplySelect) {
		this.saleSupplySelect = saleSupplySelect;
	}

	public String getProductTypeSelect() {
		return productTypeSelect;
	}

	public void setProductTypeSelect(String productTypeSelect) {
		this.productTypeSelect = productTypeSelect;
	}

	public String getProcurementMethodSelect() {
		return procurementMethodSelect;
	}

	public void setProcurementMethodSelect(String procurementMethodSelect) {
		this.procurementMethodSelect = procurementMethodSelect;
	}

	public Boolean getIsPrototype() {
		return isPrototype == null ? Boolean.FALSE : isPrototype;
	}

	public void setIsPrototype(Boolean isPrototype) {
		this.isPrototype = isPrototype;
	}

	public Boolean getIsUnrenewed() {
		return isUnrenewed == null ? Boolean.FALSE : isUnrenewed;
	}

	public void setIsUnrenewed(Boolean isUnrenewed) {
		this.isUnrenewed = isUnrenewed;
	}

	public Integer getProductSubTypeSelect() {
		return productSubTypeSelect == null ? 0 : productSubTypeSelect;
	}

	public void setProductSubTypeSelect(Integer productSubTypeSelect) {
		this.productSubTypeSelect = productSubTypeSelect;
	}

	public Integer getInventoryTypeSelect() {
		return inventoryTypeSelect == null ? 0 : inventoryTypeSelect;
	}

	public void setInventoryTypeSelect(Integer inventoryTypeSelect) {
		this.inventoryTypeSelect = inventoryTypeSelect;
	}

	public BigDecimal getSalePrice() {
		return salePrice == null ? BigDecimal.ZERO : salePrice;
	}

	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}

	public Currency getSaleCurrency() {
		return saleCurrency;
	}

	public void setSaleCurrency(Currency saleCurrency) {
		this.saleCurrency = saleCurrency;
	}

	public BigDecimal getPurchasePrice() {
		return purchasePrice == null ? BigDecimal.ZERO : purchasePrice;
	}

	public void setPurchasePrice(BigDecimal purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public Currency getPurchaseCurrency() {
		return purchaseCurrency;
	}

	public void setPurchaseCurrency(Currency purchaseCurrency) {
		this.purchaseCurrency = purchaseCurrency;
	}

	public Boolean getAutoUpdateSalePrice() {
		return autoUpdateSalePrice == null ? Boolean.FALSE : autoUpdateSalePrice;
	}

	public void setAutoUpdateSalePrice(Boolean autoUpdateSalePrice) {
		this.autoUpdateSalePrice = autoUpdateSalePrice;
	}

	public BigDecimal getCostPrice() {
		return costPrice == null ? BigDecimal.ZERO : costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public BigDecimal getManagPriceCoef() {
		return managPriceCoef == null ? BigDecimal.ZERO : managPriceCoef;
	}

	public void setManagPriceCoef(BigDecimal managPriceCoef) {
		this.managPriceCoef = managPriceCoef;
	}

	public BigDecimal getShippingCoef() {
		return shippingCoef == null ? BigDecimal.ZERO : shippingCoef;
	}

	public void setShippingCoef(BigDecimal shippingCoef) {
		this.shippingCoef = shippingCoef;
	}

	public Boolean getDefShipCoefByPartner() {
		return defShipCoefByPartner == null ? Boolean.FALSE : defShipCoefByPartner;
	}

	public void setDefShipCoefByPartner(Boolean defShipCoefByPartner) {
		this.defShipCoefByPartner = defShipCoefByPartner;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Boolean getHasWarranty() {
		return hasWarranty == null ? Boolean.FALSE : hasWarranty;
	}

	public void setHasWarranty(Boolean hasWarranty) {
		this.hasWarranty = hasWarranty;
	}

	public Boolean getIsPerishable() {
		return isPerishable == null ? Boolean.FALSE : isPerishable;
	}

	public void setIsPerishable(Boolean isPerishable) {
		this.isPerishable = isPerishable;
	}

	public Integer getWarrantyNbrOfMonths() {
		return warrantyNbrOfMonths == null ? 0 : warrantyNbrOfMonths;
	}

	public void setWarrantyNbrOfMonths(Integer warrantyNbrOfMonths) {
		this.warrantyNbrOfMonths = warrantyNbrOfMonths;
	}

	public Integer getPerishableNbrOfMonths() {
		return perishableNbrOfMonths == null ? 0 : perishableNbrOfMonths;
	}

	public void setPerishableNbrOfMonths(Integer perishableNbrOfMonths) {
		this.perishableNbrOfMonths = perishableNbrOfMonths;
	}

	public Boolean getCheckExpirationDateAtStockMoveRealization() {
		return checkExpirationDateAtStockMoveRealization == null ? Boolean.FALSE : checkExpirationDateAtStockMoveRealization;
	}

	public void setCheckExpirationDateAtStockMoveRealization(Boolean checkExpirationDateAtStockMoveRealization) {
		this.checkExpirationDateAtStockMoveRealization = checkExpirationDateAtStockMoveRealization;
	}

	public ProductVariantConfig getProductVariantConfig() {
		return productVariantConfig;
	}

	public void setProductVariantConfig(ProductVariantConfig productVariantConfig) {
		this.productVariantConfig = productVariantConfig;
	}

	public ProductVariant getProductVariant() {
		return productVariant;
	}

	public void setProductVariant(ProductVariant productVariant) {
		this.productVariant = productVariant;
	}

	public Product getParentProduct() {
		return parentProduct;
	}

	public void setParentProduct(Product parentProduct) {
		this.parentProduct = parentProduct;
	}

	public Boolean getIsModel() {
		return isModel == null ? Boolean.FALSE : isModel;
	}

	public void setIsModel(Boolean isModel) {
		this.isModel = isModel;
	}

	public Boolean getManageVariantPrice() {
		return manageVariantPrice == null ? Boolean.FALSE : manageVariantPrice;
	}

	public void setManageVariantPrice(Boolean manageVariantPrice) {
		this.manageVariantPrice = manageVariantPrice;
	}

	public Partner getDefaultSupplierPartner() {
		return defaultSupplierPartner;
	}

	public void setDefaultSupplierPartner(Partner defaultSupplierPartner) {
		this.defaultSupplierPartner = defaultSupplierPartner;
	}

	public List<AccountManagement> getAccountManagementList() {
		return accountManagementList;
	}

	public void setAccountManagementList(List<AccountManagement> accountManagementList) {
		this.accountManagementList = accountManagementList;
	}

	/**
	 * Add the given {@link AccountManagement} item to the {@code accountManagementList}.
	 *
	 * <p>
	 * It sets {@code item.product = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addAccountManagementListItem(AccountManagement item) {
		if (getAccountManagementList() == null) {
			setAccountManagementList(new ArrayList<>());
		}
		getAccountManagementList().add(item);
		item.setProduct(this);
	}

	/**
	 * Remove the given {@link AccountManagement} item from the {@code accountManagementList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeAccountManagementListItem(AccountManagement item) {
		if (getAccountManagementList() == null) {
			return;
		}
		getAccountManagementList().remove(item);
	}

	/**
	 * Clear the {@code accountManagementList} collection.
	 *
	 * <p>
	 * If you have to query {@link AccountManagement} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearAccountManagementList() {
		if (getAccountManagementList() != null) {
			getAccountManagementList().clear();
		}
	}

	public Boolean getSellable() {
		return sellable == null ? Boolean.FALSE : sellable;
	}

	public void setSellable(Boolean sellable) {
		this.sellable = sellable;
	}

	public Boolean getPurchasable() {
		return purchasable == null ? Boolean.FALSE : purchasable;
	}

	public void setPurchasable(Boolean purchasable) {
		this.purchasable = purchasable;
	}

	public Boolean getInAti() {
		return inAti == null ? Boolean.FALSE : inAti;
	}

	public void setInAti(Boolean inAti) {
		this.inAti = inAti;
	}

	public Integer getCostTypeSelect() {
		return costTypeSelect == null ? 0 : costTypeSelect;
	}

	public void setCostTypeSelect(Integer costTypeSelect) {
		this.costTypeSelect = costTypeSelect;
	}

	public Integer getSupplierDeliveryTime() {
		return supplierDeliveryTime == null ? 0 : supplierDeliveryTime;
	}

	public void setSupplierDeliveryTime(Integer supplierDeliveryTime) {
		this.supplierDeliveryTime = supplierDeliveryTime;
	}

	public MetaFile getBarCode() {
		return barCode;
	}

	public void setBarCode(MetaFile barCode) {
		this.barCode = barCode;
	}

	public BarcodeTypeConfig getBarcodeTypeConfig() {
		return barcodeTypeConfig;
	}

	public void setBarcodeTypeConfig(BarcodeTypeConfig barcodeTypeConfig) {
		this.barcodeTypeConfig = barcodeTypeConfig;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Unit getMassUnit() {
		return massUnit;
	}

	public void setMassUnit(Unit massUnit) {
		this.massUnit = massUnit;
	}

	public BigDecimal getGrossMass() {
		return grossMass == null ? BigDecimal.ZERO : grossMass;
	}

	public void setGrossMass(BigDecimal grossMass) {
		this.grossMass = grossMass;
	}

	public BigDecimal getNetMass() {
		return netMass == null ? BigDecimal.ZERO : netMass;
	}

	public void setNetMass(BigDecimal netMass) {
		this.netMass = netMass;
	}

	public Unit getLengthUnit() {
		return lengthUnit;
	}

	public void setLengthUnit(Unit lengthUnit) {
		this.lengthUnit = lengthUnit;
	}

	public BigDecimal getLength() {
		return length == null ? BigDecimal.ZERO : length;
	}

	public void setLength(BigDecimal length) {
		this.length = length;
	}

	public BigDecimal getWidth() {
		return width == null ? BigDecimal.ZERO : width;
	}

	public void setWidth(BigDecimal width) {
		this.width = width;
	}

	public BigDecimal getHeight() {
		return height == null ? BigDecimal.ZERO : height;
	}

	public void setHeight(BigDecimal height) {
		this.height = height;
	}

	public BigDecimal getDiameter() {
		return diameter == null ? BigDecimal.ZERO : diameter;
	}

	public void setDiameter(BigDecimal diameter) {
		this.diameter = diameter;
	}

	public BigDecimal getArticleVolume() {
		return articleVolume == null ? BigDecimal.ZERO : articleVolume;
	}

	public void setArticleVolume(BigDecimal articleVolume) {
		this.articleVolume = articleVolume;
	}

	public BigDecimal getEconomicManufOrderQty() {
		return economicManufOrderQty == null ? BigDecimal.ZERO : economicManufOrderQty;
	}

	public void setEconomicManufOrderQty(BigDecimal economicManufOrderQty) {
		this.economicManufOrderQty = economicManufOrderQty;
	}

	public Boolean getIsShippingCostsProduct() {
		return isShippingCostsProduct == null ? Boolean.FALSE : isShippingCostsProduct;
	}

	public void setIsShippingCostsProduct(Boolean isShippingCostsProduct) {
		this.isShippingCostsProduct = isShippingCostsProduct;
	}

	public List<ProductMultipleQty> getSaleProductMultipleQtyList() {
		return saleProductMultipleQtyList;
	}

	public void setSaleProductMultipleQtyList(List<ProductMultipleQty> saleProductMultipleQtyList) {
		this.saleProductMultipleQtyList = saleProductMultipleQtyList;
	}

	/**
	 * Add the given {@link ProductMultipleQty} item to the {@code saleProductMultipleQtyList}.
	 *
	 * <p>
	 * It sets {@code item.saleProduct = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addSaleProductMultipleQtyListItem(ProductMultipleQty item) {
		if (getSaleProductMultipleQtyList() == null) {
			setSaleProductMultipleQtyList(new ArrayList<>());
		}
		getSaleProductMultipleQtyList().add(item);
		item.setSaleProduct(this);
	}

	/**
	 * Remove the given {@link ProductMultipleQty} item from the {@code saleProductMultipleQtyList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeSaleProductMultipleQtyListItem(ProductMultipleQty item) {
		if (getSaleProductMultipleQtyList() == null) {
			return;
		}
		getSaleProductMultipleQtyList().remove(item);
	}

	/**
	 * Clear the {@code saleProductMultipleQtyList} collection.
	 *
	 * <p>
	 * If you have to query {@link ProductMultipleQty} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearSaleProductMultipleQtyList() {
		if (getSaleProductMultipleQtyList() != null) {
			getSaleProductMultipleQtyList().clear();
		}
	}

	public Boolean getAllowToForceSaleQty() {
		return allowToForceSaleQty == null ? Boolean.FALSE : allowToForceSaleQty;
	}

	public void setAllowToForceSaleQty(Boolean allowToForceSaleQty) {
		this.allowToForceSaleQty = allowToForceSaleQty;
	}

	public List<ProductMultipleQty> getPurchaseProductMultipleQtyList() {
		return purchaseProductMultipleQtyList;
	}

	public void setPurchaseProductMultipleQtyList(List<ProductMultipleQty> purchaseProductMultipleQtyList) {
		this.purchaseProductMultipleQtyList = purchaseProductMultipleQtyList;
	}

	/**
	 * Add the given {@link ProductMultipleQty} item to the {@code purchaseProductMultipleQtyList}.
	 *
	 * <p>
	 * It sets {@code item.purchaseProduct = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addPurchaseProductMultipleQtyListItem(ProductMultipleQty item) {
		if (getPurchaseProductMultipleQtyList() == null) {
			setPurchaseProductMultipleQtyList(new ArrayList<>());
		}
		getPurchaseProductMultipleQtyList().add(item);
		item.setPurchaseProduct(this);
	}

	/**
	 * Remove the given {@link ProductMultipleQty} item from the {@code purchaseProductMultipleQtyList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removePurchaseProductMultipleQtyListItem(ProductMultipleQty item) {
		if (getPurchaseProductMultipleQtyList() == null) {
			return;
		}
		getPurchaseProductMultipleQtyList().remove(item);
	}

	/**
	 * Clear the {@code purchaseProductMultipleQtyList} collection.
	 *
	 * <p>
	 * If you have to query {@link ProductMultipleQty} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearPurchaseProductMultipleQtyList() {
		if (getPurchaseProductMultipleQtyList() != null) {
			getPurchaseProductMultipleQtyList().clear();
		}
	}

	public Boolean getAllowToForcePurchaseQty() {
		return allowToForcePurchaseQty == null ? Boolean.FALSE : allowToForcePurchaseQty;
	}

	public void setAllowToForcePurchaseQty(Boolean allowToForcePurchaseQty) {
		this.allowToForcePurchaseQty = allowToForcePurchaseQty;
	}

	public Unit getPurchasesUnit() {
		return purchasesUnit;
	}

	public void setPurchasesUnit(Unit purchasesUnit) {
		this.purchasesUnit = purchasesUnit;
	}

	public List<SupplierCatalog> getSupplierCatalogList() {
		return supplierCatalogList;
	}

	public void setSupplierCatalogList(List<SupplierCatalog> supplierCatalogList) {
		this.supplierCatalogList = supplierCatalogList;
	}

	/**
	 * Add the given {@link SupplierCatalog} item to the {@code supplierCatalogList}.
	 *
	 * <p>
	 * It sets {@code item.product = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addSupplierCatalogListItem(SupplierCatalog item) {
		if (getSupplierCatalogList() == null) {
			setSupplierCatalogList(new ArrayList<>());
		}
		getSupplierCatalogList().add(item);
		item.setProduct(this);
	}

	/**
	 * Remove the given {@link SupplierCatalog} item from the {@code supplierCatalogList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeSupplierCatalogListItem(SupplierCatalog item) {
		if (getSupplierCatalogList() == null) {
			return;
		}
		getSupplierCatalogList().remove(item);
	}

	/**
	 * Clear the {@code supplierCatalogList} collection.
	 *
	 * <p>
	 * If you have to query {@link SupplierCatalog} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearSupplierCatalogList() {
		if (getSupplierCatalogList() != null) {
			getSupplierCatalogList().clear();
		}
	}

	public Unit getSalesUnit() {
		return salesUnit;
	}

	public void setSalesUnit(Unit salesUnit) {
		this.salesUnit = salesUnit;
	}

	public Boolean getIsFromConfigurator() {
		return isFromConfigurator == null ? Boolean.FALSE : isFromConfigurator;
	}

	public void setIsFromConfigurator(Boolean isFromConfigurator) {
		this.isFromConfigurator = isFromConfigurator;
	}

	public List<PackLine> getPackLines() {
		return packLines;
	}

	public void setPackLines(List<PackLine> packLines) {
		this.packLines = packLines;
	}

	/**
	 * Add the given {@link PackLine} item to the {@code packLines}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addPackLine(PackLine item) {
		if (getPackLines() == null) {
			setPackLines(new ArrayList<>());
		}
		getPackLines().add(item);
	}

	/**
	 * Remove the given {@link PackLine} item from the {@code packLines}.
	 *
	 * <p>
	 * It sets {@code item.null = null} to break the relationship.
	 * </p>
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removePackLine(PackLine item) {
		if (getPackLines() == null) {
			return;
		}
		getPackLines().remove(item);
	}

	/**
	 * Clear the {@code packLines} collection.
	 *
	 * <p>
	 * It sets {@code item.null = null} to break the relationship.
	 * </p>
	 */
	public void clearPackLines() {
		if (getPackLines() != null) {
			getPackLines().clear();
		}
	}

	public Configurator getConfigurator() {
		return configurator;
	}

	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}

	public Integer getPackPriceSelect() {
		return packPriceSelect == null ? 0 : packPriceSelect;
	}

	public void setPackPriceSelect(Integer packPriceSelect) {
		this.packPriceSelect = packPriceSelect;
	}

	public List<CustomerCatalog> getCustomerCatalogList() {
		return customerCatalogList;
	}

	public void setCustomerCatalogList(List<CustomerCatalog> customerCatalogList) {
		this.customerCatalogList = customerCatalogList;
	}

	/**
	 * Add the given {@link CustomerCatalog} item to the {@code customerCatalogList}.
	 *
	 * <p>
	 * It sets {@code item.product = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addCustomerCatalogListItem(CustomerCatalog item) {
		if (getCustomerCatalogList() == null) {
			setCustomerCatalogList(new ArrayList<>());
		}
		getCustomerCatalogList().add(item);
		item.setProduct(this);
	}

	/**
	 * Remove the given {@link CustomerCatalog} item from the {@code customerCatalogList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeCustomerCatalogListItem(CustomerCatalog item) {
		if (getCustomerCatalogList() == null) {
			return;
		}
		getCustomerCatalogList().remove(item);
	}

	/**
	 * Clear the {@code customerCatalogList} collection.
	 *
	 * <p>
	 * If you have to query {@link CustomerCatalog} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearCustomerCatalogList() {
		if (getCustomerCatalogList() != null) {
			getCustomerCatalogList().clear();
		}
	}

	public BigDecimal getAvgPrice() {
		return avgPrice == null ? BigDecimal.ZERO : avgPrice;
	}

	public void setAvgPrice(BigDecimal avgPrice) {
		this.avgPrice = avgPrice;
	}

	public TrackingNumberConfiguration getTrackingNumberConfiguration() {
		return trackingNumberConfiguration;
	}

	public void setTrackingNumberConfiguration(TrackingNumberConfiguration trackingNumberConfiguration) {
		this.trackingNumberConfiguration = trackingNumberConfiguration;
	}

	public CustomsCodeNomenclature getCustomsCodeNomenclature() {
		return customsCodeNomenclature;
	}

	public void setCustomsCodeNomenclature(CustomsCodeNomenclature customsCodeNomenclature) {
		this.customsCodeNomenclature = customsCodeNomenclature;
	}

	public Boolean getControlOnReceipt() {
		return controlOnReceipt == null ? Boolean.FALSE : controlOnReceipt;
	}

	public void setControlOnReceipt(Boolean controlOnReceipt) {
		this.controlOnReceipt = controlOnReceipt;
	}

	public Boolean getUsedInDEB() {
		return usedInDEB == null ? Boolean.FALSE : usedInDEB;
	}

	public void setUsedInDEB(Boolean usedInDEB) {
		this.usedInDEB = usedInDEB;
	}

	public Country getCountryOfOrigin() {
		return countryOfOrigin;
	}

	public void setCountryOfOrigin(Country countryOfOrigin) {
		this.countryOfOrigin = countryOfOrigin;
	}

	public Boolean getStockManaged() {
		return stockManaged == null ? Boolean.FALSE : stockManaged;
	}

	public void setStockManaged(Boolean stockManaged) {
		this.stockManaged = stockManaged;
	}

	public Boolean getIsAnalyticCapture() {
		return isAnalyticCapture == null ? Boolean.FALSE : isAnalyticCapture;
	}

	public void setIsAnalyticCapture(Boolean isAnalyticCapture) {
		this.isAnalyticCapture = isAnalyticCapture;
	}

	public MrpFamily getMrpFamily() {
		return mrpFamily;
	}

	public void setMrpFamily(MrpFamily mrpFamily) {
		this.mrpFamily = mrpFamily;
	}

	public Boolean getExcludeFromMrp() {
		return excludeFromMrp == null ? Boolean.FALSE : excludeFromMrp;
	}

	public void setExcludeFromMrp(Boolean excludeFromMrp) {
		this.excludeFromMrp = excludeFromMrp;
	}

	public Integer getStandardDelay() {
		return standardDelay == null ? 0 : standardDelay;
	}

	public void setStandardDelay(Integer standardDelay) {
		this.standardDelay = standardDelay;
	}

	public BigDecimal getLastProductionPrice() {
		return lastProductionPrice == null ? BigDecimal.ZERO : lastProductionPrice;
	}

	public void setLastProductionPrice(BigDecimal lastProductionPrice) {
		this.lastProductionPrice = lastProductionPrice;
	}

	public BillOfMaterial getDefaultBillOfMaterial() {
		return defaultBillOfMaterial;
	}

	public void setDefaultBillOfMaterial(BillOfMaterial defaultBillOfMaterial) {
		this.defaultBillOfMaterial = defaultBillOfMaterial;
	}

	public Integer getVersionSelect() {
		return versionSelect == null ? 0 : versionSelect;
	}

	public void setVersionSelect(Integer versionSelect) {
		this.versionSelect = versionSelect;
	}

	public CostSheetGroup getCostSheetGroup() {
		return costSheetGroup;
	}

	public void setCostSheetGroup(CostSheetGroup costSheetGroup) {
		this.costSheetGroup = costSheetGroup;
	}

	public Integer getRealOrEstimatedPriceSelect() {
		return realOrEstimatedPriceSelect == null ? 0 : realOrEstimatedPriceSelect;
	}

	public void setRealOrEstimatedPriceSelect(Integer realOrEstimatedPriceSelect) {
		this.realOrEstimatedPriceSelect = realOrEstimatedPriceSelect;
	}

	public Integer getComponentsValuationMethod() {
		return componentsValuationMethod == null ? 0 : componentsValuationMethod;
	}

	public void setComponentsValuationMethod(Integer componentsValuationMethod) {
		this.componentsValuationMethod = componentsValuationMethod;
	}

	public String getProductStandard() {
		return productStandard;
	}

	public void setProductStandard(String productStandard) {
		this.productStandard = productStandard;
	}

	public ProductApproval getProductApproval() {
		return productApproval;
	}

	public void setProductApproval(ProductApproval productApproval) {
		this.productApproval = productApproval;
	}

	public String getAttrs() {
		return attrs;
	}

	public void setAttrs(String attrs) {
		this.attrs = attrs;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof Product)) return false;

		final Product other = (Product) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getCode(), other.getCode())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(1355179215, this.getCode());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", getId())
			.add("name", getName())
			.add("serialNumber", getSerialNumber())
			.add("sequence", getSequence())
			.add("code", getCode())
			.add("saleSupplySelect", getSaleSupplySelect())
			.add("productTypeSelect", getProductTypeSelect())
			.add("procurementMethodSelect", getProcurementMethodSelect())
			.add("isPrototype", getIsPrototype())
			.add("isUnrenewed", getIsUnrenewed())
			.add("productSubTypeSelect", getProductSubTypeSelect())
			.add("inventoryTypeSelect", getInventoryTypeSelect())
			.omitNullValues()
			.toString();
	}
}
