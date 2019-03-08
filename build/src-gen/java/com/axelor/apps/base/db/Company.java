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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.crm.db.CrmConfig;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.purchase.db.PurchaseConfig;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.MoreObjects;

@Entity
@Cacheable
@Table(name = "BASE_COMPANY", indexes = { @Index(columnList = "address"), @Index(columnList = "partner"), @Index(columnList = "parent"), @Index(columnList = "logo"), @Index(columnList = "currency"), @Index(columnList = "default_bank_details"), @Index(columnList = "printing_settings"), @Index(columnList = "weekly_planning"), @Index(columnList = "public_holiday_events_planning") })
public class Company extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BASE_COMPANY_SEQ")
	@SequenceGenerator(name = "BASE_COMPANY_SEQ", sequenceName = "BASE_COMPANY_SEQ", allocationSize = 1)
	private Long id;

	@HashKey
	@Widget(title = "Name")
	@NotNull
	@Column(unique = true)
	private String name;

	@HashKey
	@Widget(title = "Code")
	@NotNull
	@Column(unique = true)
	private String code;

	@Widget(title = "Address")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Address address;

	@Widget(title = "Partner")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Partner partner;

	@Widget(title = "Parent company")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Company parent;

	@Widget(title = "Company departments")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyDepartment> companyDepartmentList;

	@Widget(title = "Notes")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String notes;

	@Widget(title = "Logo")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaFile logo;

	@Widget(title = "Currency")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Currency currency;

	@Widget(title = "Default Bank Account")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private BankDetails defaultBankDetails;

	@Widget(title = "Bank accounts")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<BankDetails> bankDetailsSet;

	@Widget(title = "Printing Settings")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PrintingSettings printingSettings;

	@Widget(title = "Partner Type", selection = "company.partner.type.select")
	private Integer defaultPartnerTypeSelect = 1;

	@Widget(title = "Trading names")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<TradingName> tradingNameSet;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TradingNamePrintingSettings> tradingNamePrintingSettingsList;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private WeeklyPlanning weeklyPlanning;

	@Widget(title = "Public Holiday Planning")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private EventsPlanning publicHolidayEventsPlanning;

	@Widget(title = "Purchase config")
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "company", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PurchaseConfig purchaseConfig;

	@Widget(title = "CRM config")
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "company", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private CrmConfig crmConfig;

	@Widget(title = "Sale config")
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "company", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private SaleConfig saleConfig;

	@Widget(multiline = true)
	private String orderBloquedMessage;

	@Widget(title = "Stock config")
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "company", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StockConfig stockConfig;

	@Widget(title = "Accounting configuration")
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "company", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private AccountConfig accountConfig;

	@Widget(title = "Supply chain config")
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "company", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private SupplyChainConfig supplyChainConfig;

	@Widget(title = "Production config")
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "company", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ProductionConfig productionConfig;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public Company() {
	}

	public Company(String name, String code) {
		this.name = name;
		this.code = code;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Company getParent() {
		return parent;
	}

	public void setParent(Company parent) {
		this.parent = parent;
	}

	public List<CompanyDepartment> getCompanyDepartmentList() {
		return companyDepartmentList;
	}

	public void setCompanyDepartmentList(List<CompanyDepartment> companyDepartmentList) {
		this.companyDepartmentList = companyDepartmentList;
	}

	/**
	 * Add the given {@link CompanyDepartment} item to the {@code companyDepartmentList}.
	 *
	 * <p>
	 * It sets {@code item.company = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addCompanyDepartmentListItem(CompanyDepartment item) {
		if (getCompanyDepartmentList() == null) {
			setCompanyDepartmentList(new ArrayList<>());
		}
		getCompanyDepartmentList().add(item);
		item.setCompany(this);
	}

	/**
	 * Remove the given {@link CompanyDepartment} item from the {@code companyDepartmentList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeCompanyDepartmentListItem(CompanyDepartment item) {
		if (getCompanyDepartmentList() == null) {
			return;
		}
		getCompanyDepartmentList().remove(item);
	}

	/**
	 * Clear the {@code companyDepartmentList} collection.
	 *
	 * <p>
	 * If you have to query {@link CompanyDepartment} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearCompanyDepartmentList() {
		if (getCompanyDepartmentList() != null) {
			getCompanyDepartmentList().clear();
		}
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public MetaFile getLogo() {
		return logo;
	}

	public void setLogo(MetaFile logo) {
		this.logo = logo;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public BankDetails getDefaultBankDetails() {
		return defaultBankDetails;
	}

	public void setDefaultBankDetails(BankDetails defaultBankDetails) {
		this.defaultBankDetails = defaultBankDetails;
	}

	public Set<BankDetails> getBankDetailsSet() {
		return bankDetailsSet;
	}

	public void setBankDetailsSet(Set<BankDetails> bankDetailsSet) {
		this.bankDetailsSet = bankDetailsSet;
	}

	/**
	 * Add the given {@link BankDetails} item to the {@code bankDetailsSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addBankDetailsSetItem(BankDetails item) {
		if (getBankDetailsSet() == null) {
			setBankDetailsSet(new HashSet<>());
		}
		getBankDetailsSet().add(item);
	}

	/**
	 * Remove the given {@link BankDetails} item from the {@code bankDetailsSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeBankDetailsSetItem(BankDetails item) {
		if (getBankDetailsSet() == null) {
			return;
		}
		getBankDetailsSet().remove(item);
	}

	/**
	 * Clear the {@code bankDetailsSet} collection.
	 *
	 */
	public void clearBankDetailsSet() {
		if (getBankDetailsSet() != null) {
			getBankDetailsSet().clear();
		}
	}

	public PrintingSettings getPrintingSettings() {
		return printingSettings;
	}

	public void setPrintingSettings(PrintingSettings printingSettings) {
		this.printingSettings = printingSettings;
	}

	public Integer getDefaultPartnerTypeSelect() {
		return defaultPartnerTypeSelect == null ? 0 : defaultPartnerTypeSelect;
	}

	public void setDefaultPartnerTypeSelect(Integer defaultPartnerTypeSelect) {
		this.defaultPartnerTypeSelect = defaultPartnerTypeSelect;
	}

	public Set<TradingName> getTradingNameSet() {
		return tradingNameSet;
	}

	public void setTradingNameSet(Set<TradingName> tradingNameSet) {
		this.tradingNameSet = tradingNameSet;
	}

	/**
	 * Add the given {@link TradingName} item to the {@code tradingNameSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addTradingNameSetItem(TradingName item) {
		if (getTradingNameSet() == null) {
			setTradingNameSet(new HashSet<>());
		}
		getTradingNameSet().add(item);
	}

	/**
	 * Remove the given {@link TradingName} item from the {@code tradingNameSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeTradingNameSetItem(TradingName item) {
		if (getTradingNameSet() == null) {
			return;
		}
		getTradingNameSet().remove(item);
	}

	/**
	 * Clear the {@code tradingNameSet} collection.
	 *
	 */
	public void clearTradingNameSet() {
		if (getTradingNameSet() != null) {
			getTradingNameSet().clear();
		}
	}

	public List<TradingNamePrintingSettings> getTradingNamePrintingSettingsList() {
		return tradingNamePrintingSettingsList;
	}

	public void setTradingNamePrintingSettingsList(List<TradingNamePrintingSettings> tradingNamePrintingSettingsList) {
		this.tradingNamePrintingSettingsList = tradingNamePrintingSettingsList;
	}

	/**
	 * Add the given {@link TradingNamePrintingSettings} item to the {@code tradingNamePrintingSettingsList}.
	 *
	 * <p>
	 * It sets {@code item.company = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addTradingNamePrintingSettingsListItem(TradingNamePrintingSettings item) {
		if (getTradingNamePrintingSettingsList() == null) {
			setTradingNamePrintingSettingsList(new ArrayList<>());
		}
		getTradingNamePrintingSettingsList().add(item);
		item.setCompany(this);
	}

	/**
	 * Remove the given {@link TradingNamePrintingSettings} item from the {@code tradingNamePrintingSettingsList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeTradingNamePrintingSettingsListItem(TradingNamePrintingSettings item) {
		if (getTradingNamePrintingSettingsList() == null) {
			return;
		}
		getTradingNamePrintingSettingsList().remove(item);
	}

	/**
	 * Clear the {@code tradingNamePrintingSettingsList} collection.
	 *
	 * <p>
	 * If you have to query {@link TradingNamePrintingSettings} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearTradingNamePrintingSettingsList() {
		if (getTradingNamePrintingSettingsList() != null) {
			getTradingNamePrintingSettingsList().clear();
		}
	}

	public WeeklyPlanning getWeeklyPlanning() {
		return weeklyPlanning;
	}

	public void setWeeklyPlanning(WeeklyPlanning weeklyPlanning) {
		this.weeklyPlanning = weeklyPlanning;
	}

	public EventsPlanning getPublicHolidayEventsPlanning() {
		return publicHolidayEventsPlanning;
	}

	public void setPublicHolidayEventsPlanning(EventsPlanning publicHolidayEventsPlanning) {
		this.publicHolidayEventsPlanning = publicHolidayEventsPlanning;
	}

	public PurchaseConfig getPurchaseConfig() {
		return purchaseConfig;
	}

	public void setPurchaseConfig(PurchaseConfig purchaseConfig) {
		this.purchaseConfig = purchaseConfig;
	}

	public CrmConfig getCrmConfig() {
		return crmConfig;
	}

	public void setCrmConfig(CrmConfig crmConfig) {
		this.crmConfig = crmConfig;
	}

	public SaleConfig getSaleConfig() {
		return saleConfig;
	}

	public void setSaleConfig(SaleConfig saleConfig) {
		this.saleConfig = saleConfig;
	}

	public String getOrderBloquedMessage() {
		return orderBloquedMessage;
	}

	public void setOrderBloquedMessage(String orderBloquedMessage) {
		this.orderBloquedMessage = orderBloquedMessage;
	}

	public StockConfig getStockConfig() {
		return stockConfig;
	}

	public void setStockConfig(StockConfig stockConfig) {
		this.stockConfig = stockConfig;
	}

	public AccountConfig getAccountConfig() {
		return accountConfig;
	}

	public void setAccountConfig(AccountConfig accountConfig) {
		this.accountConfig = accountConfig;
	}

	public SupplyChainConfig getSupplyChainConfig() {
		return supplyChainConfig;
	}

	public void setSupplyChainConfig(SupplyChainConfig supplyChainConfig) {
		this.supplyChainConfig = supplyChainConfig;
	}

	public ProductionConfig getProductionConfig() {
		return productionConfig;
	}

	public void setProductionConfig(ProductionConfig productionConfig) {
		this.productionConfig = productionConfig;
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
		if (!(obj instanceof Company)) return false;

		final Company other = (Company) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getName(), other.getName())) return false;
		if (!Objects.equals(getCode(), other.getCode())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(-1679829923, this.getName(), this.getCode());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", getId())
			.add("name", getName())
			.add("code", getCode())
			.add("defaultPartnerTypeSelect", getDefaultPartnerTypeSelect())
			.add("orderBloquedMessage", getOrderBloquedMessage())
			.omitNullValues()
			.toString();
	}
}
