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
package com.axelor.apps.stock.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.message.db.Template;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Cacheable
@Table(name = "STOCK_STOCK_CONFIG", indexes = { @Index(columnList = "receipt_default_stock_location"), @Index(columnList = "pickup_default_stock_location"), @Index(columnList = "quality_control_default_stock_location"), @Index(columnList = "customer_virtual_stock_location"), @Index(columnList = "supplier_virtual_stock_location"), @Index(columnList = "inventory_virtual_stock_location"), @Index(columnList = "customs_mass_unit"), @Index(columnList = "planned_stock_move_message_template"), @Index(columnList = "real_stock_move_message_template"), @Index(columnList = "signatory_user"), @Index(columnList = "direct_order_stock_location"), @Index(columnList = "production_virtual_stock_location"), @Index(columnList = "waste_stock_location"), @Index(columnList = "component_default_stock_location"), @Index(columnList = "finished_products_default_stock_location") })
public class StockConfig extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STOCK_STOCK_CONFIG_SEQ")
	@SequenceGenerator(name = "STOCK_STOCK_CONFIG_SEQ", sequenceName = "STOCK_STOCK_CONFIG_SEQ", allocationSize = 1)
	private Long id;

	@HashKey
	@Widget(title = "Company")
	@NotNull
	@JoinColumn(unique = true)
	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Company company;

	@Widget(title = "Receipt default stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation receiptDefaultStockLocation;

	@Widget(title = "Pickup default stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation pickupDefaultStockLocation;

	@Widget(title = "Quality control default stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation qualityControlDefaultStockLocation;

	@Widget(title = "Customer virtual stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation customerVirtualStockLocation;

	@Widget(title = "Supplier virtual stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation supplierVirtualStockLocation;

	@Widget(title = "Inventory virtual stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation inventoryVirtualStockLocation;

	@Widget(title = "Unit of mass")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Unit customsMassUnit;

	@Widget(title = "Realize stock moves upon parcel/pallet collection")
	private Boolean realizeStockMovesUponParcelPalletCollection = Boolean.FALSE;

	@Widget(title = "Send email when planning stock move")
	private Boolean plannedStockMoveAutomaticMail = Boolean.FALSE;

	@Widget(title = "Send email on stock move realization")
	private Boolean realStockMoveAutomaticMail = Boolean.FALSE;

	@Widget(title = "Message template")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Template plannedStockMoveMessageTemplate;

	@Widget(title = "Message template")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Template realStockMoveMessageTemplate;

	@Widget(title = "Customer account numbers to carriers")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stockConfig", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FreightCarrierCustomerAccountNumber> freightCarrierCustomerAccountNumberList;

	@Widget(title = "Certificate of conformity title", translatable = true)
	private String conformityCertificateTitle;

	@Widget(title = "Text in certificate of conformity", translatable = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String conformityCertificateDescription;

	@Widget(title = "Default signatory user")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User signatoryUser;

	@Widget(title = "Display tracking number details on Picking printing")
	private Boolean displayTrackNbrOnPickingPrinting = Boolean.FALSE;

	@Widget(title = "Display barcode on Picking printing")
	private Boolean displayBarcodeOnPickingPrinting = Boolean.FALSE;

	@Widget(title = "Display customer code on picking printing")
	private Boolean displayCustomerCodeOnPickingPrinting = Boolean.FALSE;

	@Widget(title = "Display partner sequence on printing")
	private Boolean displayPartnerSeqOnPrinting = Boolean.FALSE;

	@Widget(title = "Display line details on printing")
	private Boolean displayLineDetailsOnPrinting = Boolean.FALSE;

	@Widget(title = "Return Surplus")
	private Boolean isWithReturnSurplus = Boolean.FALSE;

	@Widget(title = "Manage backorder")
	private Boolean isWithBackorder = Boolean.FALSE;

	@Widget(title = "Picking order printing detailed")
	private Boolean pickingOrderPrintingDetailed = Boolean.FALSE;

	@Widget(title = "Direct order default stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation directOrderStockLocation;

	@Widget(title = "Production virtual stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation productionVirtualStockLocation;

	@Widget(title = "Waste stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation wasteStockLocation;

	@Widget(title = "Components default stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation componentDefaultStockLocation;

	@Widget(title = "Finished products default stock location")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockLocation finishedProductsDefaultStockLocation;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public StockConfig() {
	}

	public StockConfig(Boolean displayBarcodeOnPickingPrinting) {
		this.displayBarcodeOnPickingPrinting = displayBarcodeOnPickingPrinting;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public StockLocation getReceiptDefaultStockLocation() {
		return receiptDefaultStockLocation;
	}

	public void setReceiptDefaultStockLocation(StockLocation receiptDefaultStockLocation) {
		this.receiptDefaultStockLocation = receiptDefaultStockLocation;
	}

	public StockLocation getPickupDefaultStockLocation() {
		return pickupDefaultStockLocation;
	}

	public void setPickupDefaultStockLocation(StockLocation pickupDefaultStockLocation) {
		this.pickupDefaultStockLocation = pickupDefaultStockLocation;
	}

	public StockLocation getQualityControlDefaultStockLocation() {
		return qualityControlDefaultStockLocation;
	}

	public void setQualityControlDefaultStockLocation(StockLocation qualityControlDefaultStockLocation) {
		this.qualityControlDefaultStockLocation = qualityControlDefaultStockLocation;
	}

	public StockLocation getCustomerVirtualStockLocation() {
		return customerVirtualStockLocation;
	}

	public void setCustomerVirtualStockLocation(StockLocation customerVirtualStockLocation) {
		this.customerVirtualStockLocation = customerVirtualStockLocation;
	}

	public StockLocation getSupplierVirtualStockLocation() {
		return supplierVirtualStockLocation;
	}

	public void setSupplierVirtualStockLocation(StockLocation supplierVirtualStockLocation) {
		this.supplierVirtualStockLocation = supplierVirtualStockLocation;
	}

	public StockLocation getInventoryVirtualStockLocation() {
		return inventoryVirtualStockLocation;
	}

	public void setInventoryVirtualStockLocation(StockLocation inventoryVirtualStockLocation) {
		this.inventoryVirtualStockLocation = inventoryVirtualStockLocation;
	}

	public Unit getCustomsMassUnit() {
		return customsMassUnit;
	}

	public void setCustomsMassUnit(Unit customsMassUnit) {
		this.customsMassUnit = customsMassUnit;
	}

	public Boolean getRealizeStockMovesUponParcelPalletCollection() {
		return realizeStockMovesUponParcelPalletCollection == null ? Boolean.FALSE : realizeStockMovesUponParcelPalletCollection;
	}

	public void setRealizeStockMovesUponParcelPalletCollection(Boolean realizeStockMovesUponParcelPalletCollection) {
		this.realizeStockMovesUponParcelPalletCollection = realizeStockMovesUponParcelPalletCollection;
	}

	public Boolean getPlannedStockMoveAutomaticMail() {
		return plannedStockMoveAutomaticMail == null ? Boolean.FALSE : plannedStockMoveAutomaticMail;
	}

	public void setPlannedStockMoveAutomaticMail(Boolean plannedStockMoveAutomaticMail) {
		this.plannedStockMoveAutomaticMail = plannedStockMoveAutomaticMail;
	}

	public Boolean getRealStockMoveAutomaticMail() {
		return realStockMoveAutomaticMail == null ? Boolean.FALSE : realStockMoveAutomaticMail;
	}

	public void setRealStockMoveAutomaticMail(Boolean realStockMoveAutomaticMail) {
		this.realStockMoveAutomaticMail = realStockMoveAutomaticMail;
	}

	public Template getPlannedStockMoveMessageTemplate() {
		return plannedStockMoveMessageTemplate;
	}

	public void setPlannedStockMoveMessageTemplate(Template plannedStockMoveMessageTemplate) {
		this.plannedStockMoveMessageTemplate = plannedStockMoveMessageTemplate;
	}

	public Template getRealStockMoveMessageTemplate() {
		return realStockMoveMessageTemplate;
	}

	public void setRealStockMoveMessageTemplate(Template realStockMoveMessageTemplate) {
		this.realStockMoveMessageTemplate = realStockMoveMessageTemplate;
	}

	public List<FreightCarrierCustomerAccountNumber> getFreightCarrierCustomerAccountNumberList() {
		return freightCarrierCustomerAccountNumberList;
	}

	public void setFreightCarrierCustomerAccountNumberList(List<FreightCarrierCustomerAccountNumber> freightCarrierCustomerAccountNumberList) {
		this.freightCarrierCustomerAccountNumberList = freightCarrierCustomerAccountNumberList;
	}

	/**
	 * Add the given {@link FreightCarrierCustomerAccountNumber} item to the {@code freightCarrierCustomerAccountNumberList}.
	 *
	 * <p>
	 * It sets {@code item.stockConfig = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addFreightCarrierCustomerAccountNumberListItem(FreightCarrierCustomerAccountNumber item) {
		if (getFreightCarrierCustomerAccountNumberList() == null) {
			setFreightCarrierCustomerAccountNumberList(new ArrayList<>());
		}
		getFreightCarrierCustomerAccountNumberList().add(item);
		item.setStockConfig(this);
	}

	/**
	 * Remove the given {@link FreightCarrierCustomerAccountNumber} item from the {@code freightCarrierCustomerAccountNumberList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeFreightCarrierCustomerAccountNumberListItem(FreightCarrierCustomerAccountNumber item) {
		if (getFreightCarrierCustomerAccountNumberList() == null) {
			return;
		}
		getFreightCarrierCustomerAccountNumberList().remove(item);
	}

	/**
	 * Clear the {@code freightCarrierCustomerAccountNumberList} collection.
	 *
	 * <p>
	 * If you have to query {@link FreightCarrierCustomerAccountNumber} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearFreightCarrierCustomerAccountNumberList() {
		if (getFreightCarrierCustomerAccountNumberList() != null) {
			getFreightCarrierCustomerAccountNumberList().clear();
		}
	}

	public String getConformityCertificateTitle() {
		return conformityCertificateTitle;
	}

	public void setConformityCertificateTitle(String conformityCertificateTitle) {
		this.conformityCertificateTitle = conformityCertificateTitle;
	}

	public String getConformityCertificateDescription() {
		return conformityCertificateDescription;
	}

	public void setConformityCertificateDescription(String conformityCertificateDescription) {
		this.conformityCertificateDescription = conformityCertificateDescription;
	}

	public User getSignatoryUser() {
		return signatoryUser;
	}

	public void setSignatoryUser(User signatoryUser) {
		this.signatoryUser = signatoryUser;
	}

	public Boolean getDisplayTrackNbrOnPickingPrinting() {
		return displayTrackNbrOnPickingPrinting == null ? Boolean.FALSE : displayTrackNbrOnPickingPrinting;
	}

	public void setDisplayTrackNbrOnPickingPrinting(Boolean displayTrackNbrOnPickingPrinting) {
		this.displayTrackNbrOnPickingPrinting = displayTrackNbrOnPickingPrinting;
	}

	public Boolean getDisplayBarcodeOnPickingPrinting() {
		return displayBarcodeOnPickingPrinting == null ? Boolean.FALSE : displayBarcodeOnPickingPrinting;
	}

	public void setDisplayBarcodeOnPickingPrinting(Boolean displayBarcodeOnPickingPrinting) {
		this.displayBarcodeOnPickingPrinting = displayBarcodeOnPickingPrinting;
	}

	public Boolean getDisplayCustomerCodeOnPickingPrinting() {
		return displayCustomerCodeOnPickingPrinting == null ? Boolean.FALSE : displayCustomerCodeOnPickingPrinting;
	}

	public void setDisplayCustomerCodeOnPickingPrinting(Boolean displayCustomerCodeOnPickingPrinting) {
		this.displayCustomerCodeOnPickingPrinting = displayCustomerCodeOnPickingPrinting;
	}

	public Boolean getDisplayPartnerSeqOnPrinting() {
		return displayPartnerSeqOnPrinting == null ? Boolean.FALSE : displayPartnerSeqOnPrinting;
	}

	public void setDisplayPartnerSeqOnPrinting(Boolean displayPartnerSeqOnPrinting) {
		this.displayPartnerSeqOnPrinting = displayPartnerSeqOnPrinting;
	}

	public Boolean getDisplayLineDetailsOnPrinting() {
		return displayLineDetailsOnPrinting == null ? Boolean.FALSE : displayLineDetailsOnPrinting;
	}

	public void setDisplayLineDetailsOnPrinting(Boolean displayLineDetailsOnPrinting) {
		this.displayLineDetailsOnPrinting = displayLineDetailsOnPrinting;
	}

	public Boolean getIsWithReturnSurplus() {
		return isWithReturnSurplus == null ? Boolean.FALSE : isWithReturnSurplus;
	}

	public void setIsWithReturnSurplus(Boolean isWithReturnSurplus) {
		this.isWithReturnSurplus = isWithReturnSurplus;
	}

	public Boolean getIsWithBackorder() {
		return isWithBackorder == null ? Boolean.FALSE : isWithBackorder;
	}

	public void setIsWithBackorder(Boolean isWithBackorder) {
		this.isWithBackorder = isWithBackorder;
	}

	public Boolean getPickingOrderPrintingDetailed() {
		return pickingOrderPrintingDetailed == null ? Boolean.FALSE : pickingOrderPrintingDetailed;
	}

	public void setPickingOrderPrintingDetailed(Boolean pickingOrderPrintingDetailed) {
		this.pickingOrderPrintingDetailed = pickingOrderPrintingDetailed;
	}

	public StockLocation getDirectOrderStockLocation() {
		return directOrderStockLocation;
	}

	public void setDirectOrderStockLocation(StockLocation directOrderStockLocation) {
		this.directOrderStockLocation = directOrderStockLocation;
	}

	public StockLocation getProductionVirtualStockLocation() {
		return productionVirtualStockLocation;
	}

	public void setProductionVirtualStockLocation(StockLocation productionVirtualStockLocation) {
		this.productionVirtualStockLocation = productionVirtualStockLocation;
	}

	public StockLocation getWasteStockLocation() {
		return wasteStockLocation;
	}

	public void setWasteStockLocation(StockLocation wasteStockLocation) {
		this.wasteStockLocation = wasteStockLocation;
	}

	public StockLocation getComponentDefaultStockLocation() {
		return componentDefaultStockLocation;
	}

	public void setComponentDefaultStockLocation(StockLocation componentDefaultStockLocation) {
		this.componentDefaultStockLocation = componentDefaultStockLocation;
	}

	public StockLocation getFinishedProductsDefaultStockLocation() {
		return finishedProductsDefaultStockLocation;
	}

	public void setFinishedProductsDefaultStockLocation(StockLocation finishedProductsDefaultStockLocation) {
		this.finishedProductsDefaultStockLocation = finishedProductsDefaultStockLocation;
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
		if (!(obj instanceof StockConfig)) return false;

		final StockConfig other = (StockConfig) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getCompany(), other.getCompany())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(34399704, this.getCompany());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", getId())
			.add("realizeStockMovesUponParcelPalletCollection", getRealizeStockMovesUponParcelPalletCollection())
			.add("plannedStockMoveAutomaticMail", getPlannedStockMoveAutomaticMail())
			.add("realStockMoveAutomaticMail", getRealStockMoveAutomaticMail())
			.add("conformityCertificateTitle", getConformityCertificateTitle())
			.add("displayTrackNbrOnPickingPrinting", getDisplayTrackNbrOnPickingPrinting())
			.add("displayBarcodeOnPickingPrinting", getDisplayBarcodeOnPickingPrinting())
			.add("displayCustomerCodeOnPickingPrinting", getDisplayCustomerCodeOnPickingPrinting())
			.add("displayPartnerSeqOnPrinting", getDisplayPartnerSeqOnPrinting())
			.add("displayLineDetailsOnPrinting", getDisplayLineDetailsOnPrinting())
			.add("isWithReturnSurplus", getIsWithReturnSurplus())
			.add("isWithBackorder", getIsWithBackorder())
			.omitNullValues()
			.toString();
	}
}
