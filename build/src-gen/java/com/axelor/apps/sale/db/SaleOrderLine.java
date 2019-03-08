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
package com.axelor.apps.sale.db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.VirtualColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "SALE_SALE_ORDER_LINE", indexes = { @Index(columnList = "fullName"), @Index(columnList = "sale_order"), @Index(columnList = "product"), @Index(columnList = "tax_line"), @Index(columnList = "tax_equiv"), @Index(columnList = "unit"), @Index(columnList = "supplier_partner"), @Index(columnList = "parent_line"), @Index(columnList = "analytic_distribution_template"), @Index(columnList = "bill_of_material") })
public class SaleOrderLine extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SALE_SALE_ORDER_LINE_SEQ")
	@SequenceGenerator(name = "SALE_SALE_ORDER_LINE_SEQ", sequenceName = "SALE_SALE_ORDER_LINE_SEQ", allocationSize = 1)
	private Long id;

	@NameColumn
	@VirtualColumn
	@Access(AccessType.PROPERTY)
	private String fullName;

	@Widget(title = "Sale order")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private SaleOrder saleOrder;

	@Widget(title = "Seq.")
	private Integer sequence = 0;

	@Widget(title = "Product")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Product product;

	@Widget(title = "Qty")
	private BigDecimal qty = new BigDecimal("1");

	@Widget(hidden = true)
	private BigDecimal oldQty = BigDecimal.ZERO;

	@Widget(title = "Print subtotal / line")
	private Boolean isToPrintLineSubTotal = Boolean.FALSE;

	@Widget(title = "Displayed Product name", translatable = true)
	@NotNull
	private String productName;

	@Widget(title = "Unit price W.T.")
	@Digits(integer = 10, fraction = 10)
	private BigDecimal price = BigDecimal.ZERO;

	@Widget(title = "Unit price A.T.I.")
	@Digits(integer = 10, fraction = 10)
	private BigDecimal inTaxPrice = BigDecimal.ZERO;

	@Widget(title = "Unit price discounted")
	@Digits(integer = 10, fraction = 10)
	private BigDecimal priceDiscounted = BigDecimal.ZERO;

	@Widget(title = "Tax")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private TaxLine taxLine;

	@Widget(title = "Tax Equiv")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private TaxEquiv taxEquiv;

	@Widget(title = "Total W.T.")
	private BigDecimal exTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Total A.T.I.")
	private BigDecimal inTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Description")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String description;

	@Widget(title = "Unit")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Unit unit;

	@Widget(title = "Supplier")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Partner supplierPartner;

	@Widget(title = "Discount amount")
	@Digits(integer = 10, fraction = 10)
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@Widget(title = "Discount Type", selection = "base.price.list.line.amount.type.select")
	private Integer discountTypeSelect = 3;

	@Widget(title = "Estimated shipping date")
	private LocalDate estimatedDelivDate;

	@Widget(title = "Desired delivery date")
	private LocalDate desiredDelivDate;

	@Widget(title = "Delivered quantity")
	private BigDecimal deliveredQty = BigDecimal.ZERO;

	@Widget(title = "SubTotal cost price")
	private BigDecimal subTotalCostPrice = BigDecimal.ZERO;

	@Widget(title = "Sub Total gross profit")
	private BigDecimal subTotalGrossMargin = BigDecimal.ZERO;

	@Widget(title = "Sub margin rate")
	private BigDecimal subMarginRate = BigDecimal.ZERO;

	@Widget(title = "Sub Total markup")
	private BigDecimal subTotalMarkup = BigDecimal.ZERO;

	@Widget(title = "Total W.T. in company currency", hidden = true)
	private BigDecimal companyExTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Sub line", readonly = true)
	private Boolean isSubLine = Boolean.FALSE;

	@Widget(title = "Unit cost price in company currency", hidden = true)
	private BigDecimal companyCostPrice = BigDecimal.ZERO;

	@Widget(title = "Total A.T.I. in company currency", hidden = true)
	private BigDecimal companyInTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Total cost in company currency", hidden = true)
	private BigDecimal companyCostTotal = BigDecimal.ZERO;

	@Widget(title = "Type", selection = "sale.order.line.type.select")
	private Integer typeSelect = 0;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private SaleOrderLine parentLine;

	@Widget(title = "Pack lines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentLine", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SaleOrderLine> subLineList;

	@Widget(title = "Total for pack lines")
	private BigDecimal totalPack = BigDecimal.ZERO;

	@Widget(title = "Pack price select", selection = "product.pack.price.select")
	private Integer packPriceSelect = 0;

	@Widget(title = "Supply method", selection = "product.sale.supply.select")
	private Integer saleSupplySelect = 0;

	@Widget(title = "Invoicing Date")
	private LocalDate invoicingDate;

	@Widget(title = "Amount invoiced W.T.", readonly = true)
	private BigDecimal amountInvoiced = BigDecimal.ZERO;

	@Widget(title = "Invoice controlled")
	private Boolean isInvoiceControlled = Boolean.FALSE;

	@Widget(title = "Analytic move lines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "saleOrderLine", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AnalyticMoveLine> analyticMoveLineList;

	@Widget(title = "Analytic distribution template")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private AnalyticDistributionTemplate analyticDistributionTemplate;

	@Widget(readonly = true)
	private Boolean invoiced = Boolean.FALSE;

	@Widget(title = "Picking Order Info")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String pickingOrderInfo;

	@Widget(title = "Allocated qty")
	private BigDecimal reservedQty = BigDecimal.ZERO;

	@Widget(title = "Requested reserved qty")
	private BigDecimal requestedReservedQty = BigDecimal.ZERO;

	@Widget(title = "Delivery state", readonly = true, selection = "sale.order.delivery.state")
	private Integer deliveryState = 0;

	@Widget(title = "Standard delay (days)")
	private Integer standardDelay = 0;

	@Widget(title = "BoM")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private BillOfMaterial billOfMaterial;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public SaleOrderLine() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		try {
			fullName = computeFullName();
		} catch (NullPointerException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("NPE in function field: getFullName()", e);
		}
		return fullName;
	}

	protected String computeFullName() {
		String fullName = "";
		if(saleOrder != null && saleOrder.getSaleOrderSeq() != null){
			fullName += saleOrder.getSaleOrderSeq();
		}
		if(productName != null)  {
			fullName += "-";
			if(productName.length() > 100)  {
				fullName += productName.substring(1, 100);
			}
			else  {  fullName += productName;  }
		}
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public SaleOrder getSaleOrder() {
		return saleOrder;
	}

	public void setSaleOrder(SaleOrder saleOrder) {
		this.saleOrder = saleOrder;
	}

	public Integer getSequence() {
		return sequence == null ? 0 : sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public BigDecimal getQty() {
		return qty == null ? BigDecimal.ZERO : qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public BigDecimal getOldQty() {
		return oldQty == null ? BigDecimal.ZERO : oldQty;
	}

	public void setOldQty(BigDecimal oldQty) {
		this.oldQty = oldQty;
	}

	public Boolean getIsToPrintLineSubTotal() {
		return isToPrintLineSubTotal == null ? Boolean.FALSE : isToPrintLineSubTotal;
	}

	public void setIsToPrintLineSubTotal(Boolean isToPrintLineSubTotal) {
		this.isToPrintLineSubTotal = isToPrintLineSubTotal;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public BigDecimal getPrice() {
		return price == null ? BigDecimal.ZERO : price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getInTaxPrice() {
		return inTaxPrice == null ? BigDecimal.ZERO : inTaxPrice;
	}

	public void setInTaxPrice(BigDecimal inTaxPrice) {
		this.inTaxPrice = inTaxPrice;
	}

	public BigDecimal getPriceDiscounted() {
		return priceDiscounted == null ? BigDecimal.ZERO : priceDiscounted;
	}

	public void setPriceDiscounted(BigDecimal priceDiscounted) {
		this.priceDiscounted = priceDiscounted;
	}

	public TaxLine getTaxLine() {
		return taxLine;
	}

	public void setTaxLine(TaxLine taxLine) {
		this.taxLine = taxLine;
	}

	public TaxEquiv getTaxEquiv() {
		return taxEquiv;
	}

	public void setTaxEquiv(TaxEquiv taxEquiv) {
		this.taxEquiv = taxEquiv;
	}

	public BigDecimal getExTaxTotal() {
		return exTaxTotal == null ? BigDecimal.ZERO : exTaxTotal;
	}

	public void setExTaxTotal(BigDecimal exTaxTotal) {
		this.exTaxTotal = exTaxTotal;
	}

	public BigDecimal getInTaxTotal() {
		return inTaxTotal == null ? BigDecimal.ZERO : inTaxTotal;
	}

	public void setInTaxTotal(BigDecimal inTaxTotal) {
		this.inTaxTotal = inTaxTotal;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Partner getSupplierPartner() {
		return supplierPartner;
	}

	public void setSupplierPartner(Partner supplierPartner) {
		this.supplierPartner = supplierPartner;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount == null ? BigDecimal.ZERO : discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public Integer getDiscountTypeSelect() {
		return discountTypeSelect == null ? 0 : discountTypeSelect;
	}

	public void setDiscountTypeSelect(Integer discountTypeSelect) {
		this.discountTypeSelect = discountTypeSelect;
	}

	public LocalDate getEstimatedDelivDate() {
		return estimatedDelivDate;
	}

	public void setEstimatedDelivDate(LocalDate estimatedDelivDate) {
		this.estimatedDelivDate = estimatedDelivDate;
	}

	public LocalDate getDesiredDelivDate() {
		return desiredDelivDate;
	}

	public void setDesiredDelivDate(LocalDate desiredDelivDate) {
		this.desiredDelivDate = desiredDelivDate;
	}

	public BigDecimal getDeliveredQty() {
		return deliveredQty == null ? BigDecimal.ZERO : deliveredQty;
	}

	public void setDeliveredQty(BigDecimal deliveredQty) {
		this.deliveredQty = deliveredQty;
	}

	public BigDecimal getSubTotalCostPrice() {
		return subTotalCostPrice == null ? BigDecimal.ZERO : subTotalCostPrice;
	}

	public void setSubTotalCostPrice(BigDecimal subTotalCostPrice) {
		this.subTotalCostPrice = subTotalCostPrice;
	}

	public BigDecimal getSubTotalGrossMargin() {
		return subTotalGrossMargin == null ? BigDecimal.ZERO : subTotalGrossMargin;
	}

	public void setSubTotalGrossMargin(BigDecimal subTotalGrossMargin) {
		this.subTotalGrossMargin = subTotalGrossMargin;
	}

	public BigDecimal getSubMarginRate() {
		return subMarginRate == null ? BigDecimal.ZERO : subMarginRate;
	}

	public void setSubMarginRate(BigDecimal subMarginRate) {
		this.subMarginRate = subMarginRate;
	}

	public BigDecimal getSubTotalMarkup() {
		return subTotalMarkup == null ? BigDecimal.ZERO : subTotalMarkup;
	}

	public void setSubTotalMarkup(BigDecimal subTotalMarkup) {
		this.subTotalMarkup = subTotalMarkup;
	}

	public BigDecimal getCompanyExTaxTotal() {
		return companyExTaxTotal == null ? BigDecimal.ZERO : companyExTaxTotal;
	}

	public void setCompanyExTaxTotal(BigDecimal companyExTaxTotal) {
		this.companyExTaxTotal = companyExTaxTotal;
	}

	public Boolean getIsSubLine() {
		return isSubLine == null ? Boolean.FALSE : isSubLine;
	}

	public void setIsSubLine(Boolean isSubLine) {
		this.isSubLine = isSubLine;
	}

	public BigDecimal getCompanyCostPrice() {
		return companyCostPrice == null ? BigDecimal.ZERO : companyCostPrice;
	}

	public void setCompanyCostPrice(BigDecimal companyCostPrice) {
		this.companyCostPrice = companyCostPrice;
	}

	public BigDecimal getCompanyInTaxTotal() {
		return companyInTaxTotal == null ? BigDecimal.ZERO : companyInTaxTotal;
	}

	public void setCompanyInTaxTotal(BigDecimal companyInTaxTotal) {
		this.companyInTaxTotal = companyInTaxTotal;
	}

	public BigDecimal getCompanyCostTotal() {
		return companyCostTotal == null ? BigDecimal.ZERO : companyCostTotal;
	}

	public void setCompanyCostTotal(BigDecimal companyCostTotal) {
		this.companyCostTotal = companyCostTotal;
	}

	public Integer getTypeSelect() {
		return typeSelect == null ? 0 : typeSelect;
	}

	public void setTypeSelect(Integer typeSelect) {
		this.typeSelect = typeSelect;
	}

	public SaleOrderLine getParentLine() {
		return parentLine;
	}

	public void setParentLine(SaleOrderLine parentLine) {
		this.parentLine = parentLine;
	}

	public List<SaleOrderLine> getSubLineList() {
		return subLineList;
	}

	public void setSubLineList(List<SaleOrderLine> subLineList) {
		this.subLineList = subLineList;
	}

	/**
	 * Add the given {@link SaleOrderLine} item to the {@code subLineList}.
	 *
	 * <p>
	 * It sets {@code item.parentLine = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addSubLineListItem(SaleOrderLine item) {
		if (getSubLineList() == null) {
			setSubLineList(new ArrayList<>());
		}
		getSubLineList().add(item);
		item.setParentLine(this);
	}

	/**
	 * Remove the given {@link SaleOrderLine} item from the {@code subLineList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeSubLineListItem(SaleOrderLine item) {
		if (getSubLineList() == null) {
			return;
		}
		getSubLineList().remove(item);
	}

	/**
	 * Clear the {@code subLineList} collection.
	 *
	 * <p>
	 * If you have to query {@link SaleOrderLine} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearSubLineList() {
		if (getSubLineList() != null) {
			getSubLineList().clear();
		}
	}

	public BigDecimal getTotalPack() {
		return totalPack == null ? BigDecimal.ZERO : totalPack;
	}

	public void setTotalPack(BigDecimal totalPack) {
		this.totalPack = totalPack;
	}

	public Integer getPackPriceSelect() {
		return packPriceSelect == null ? 0 : packPriceSelect;
	}

	public void setPackPriceSelect(Integer packPriceSelect) {
		this.packPriceSelect = packPriceSelect;
	}

	public Integer getSaleSupplySelect() {
		return saleSupplySelect == null ? 0 : saleSupplySelect;
	}

	public void setSaleSupplySelect(Integer saleSupplySelect) {
		this.saleSupplySelect = saleSupplySelect;
	}

	public LocalDate getInvoicingDate() {
		return invoicingDate;
	}

	public void setInvoicingDate(LocalDate invoicingDate) {
		this.invoicingDate = invoicingDate;
	}

	public BigDecimal getAmountInvoiced() {
		return amountInvoiced == null ? BigDecimal.ZERO : amountInvoiced;
	}

	public void setAmountInvoiced(BigDecimal amountInvoiced) {
		this.amountInvoiced = amountInvoiced;
	}

	public Boolean getIsInvoiceControlled() {
		return isInvoiceControlled == null ? Boolean.FALSE : isInvoiceControlled;
	}

	public void setIsInvoiceControlled(Boolean isInvoiceControlled) {
		this.isInvoiceControlled = isInvoiceControlled;
	}

	public List<AnalyticMoveLine> getAnalyticMoveLineList() {
		return analyticMoveLineList;
	}

	public void setAnalyticMoveLineList(List<AnalyticMoveLine> analyticMoveLineList) {
		this.analyticMoveLineList = analyticMoveLineList;
	}

	/**
	 * Add the given {@link AnalyticMoveLine} item to the {@code analyticMoveLineList}.
	 *
	 * <p>
	 * It sets {@code item.saleOrderLine = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addAnalyticMoveLineListItem(AnalyticMoveLine item) {
		if (getAnalyticMoveLineList() == null) {
			setAnalyticMoveLineList(new ArrayList<>());
		}
		getAnalyticMoveLineList().add(item);
		item.setSaleOrderLine(this);
	}

	/**
	 * Remove the given {@link AnalyticMoveLine} item from the {@code analyticMoveLineList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeAnalyticMoveLineListItem(AnalyticMoveLine item) {
		if (getAnalyticMoveLineList() == null) {
			return;
		}
		getAnalyticMoveLineList().remove(item);
	}

	/**
	 * Clear the {@code analyticMoveLineList} collection.
	 *
	 * <p>
	 * If you have to query {@link AnalyticMoveLine} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearAnalyticMoveLineList() {
		if (getAnalyticMoveLineList() != null) {
			getAnalyticMoveLineList().clear();
		}
	}

	public AnalyticDistributionTemplate getAnalyticDistributionTemplate() {
		return analyticDistributionTemplate;
	}

	public void setAnalyticDistributionTemplate(AnalyticDistributionTemplate analyticDistributionTemplate) {
		this.analyticDistributionTemplate = analyticDistributionTemplate;
	}

	public Boolean getInvoiced() {
		return invoiced == null ? Boolean.FALSE : invoiced;
	}

	public void setInvoiced(Boolean invoiced) {
		this.invoiced = invoiced;
	}

	public String getPickingOrderInfo() {
		return pickingOrderInfo;
	}

	public void setPickingOrderInfo(String pickingOrderInfo) {
		this.pickingOrderInfo = pickingOrderInfo;
	}

	public BigDecimal getReservedQty() {
		return reservedQty == null ? BigDecimal.ZERO : reservedQty;
	}

	public void setReservedQty(BigDecimal reservedQty) {
		this.reservedQty = reservedQty;
	}

	public BigDecimal getRequestedReservedQty() {
		return requestedReservedQty == null ? BigDecimal.ZERO : requestedReservedQty;
	}

	public void setRequestedReservedQty(BigDecimal requestedReservedQty) {
		this.requestedReservedQty = requestedReservedQty;
	}

	public Integer getDeliveryState() {
		return deliveryState == null ? 0 : deliveryState;
	}

	public void setDeliveryState(Integer deliveryState) {
		this.deliveryState = deliveryState;
	}

	public Integer getStandardDelay() {
		return standardDelay == null ? 0 : standardDelay;
	}

	public void setStandardDelay(Integer standardDelay) {
		this.standardDelay = standardDelay;
	}

	public BillOfMaterial getBillOfMaterial() {
		return billOfMaterial;
	}

	public void setBillOfMaterial(BillOfMaterial billOfMaterial) {
		this.billOfMaterial = billOfMaterial;
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
		if (!(obj instanceof SaleOrderLine)) return false;

		final SaleOrderLine other = (SaleOrderLine) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", getId())
			.add("sequence", getSequence())
			.add("qty", getQty())
			.add("oldQty", getOldQty())
			.add("isToPrintLineSubTotal", getIsToPrintLineSubTotal())
			.add("productName", getProductName())
			.add("price", getPrice())
			.add("inTaxPrice", getInTaxPrice())
			.add("priceDiscounted", getPriceDiscounted())
			.add("exTaxTotal", getExTaxTotal())
			.add("inTaxTotal", getInTaxTotal())
			.add("discountAmount", getDiscountAmount())
			.omitNullValues()
			.toString();
	}
}
