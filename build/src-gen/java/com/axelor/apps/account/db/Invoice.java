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
package com.axelor.apps.account.db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.StopReason;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Track;
import com.axelor.db.annotations.TrackEvent;
import com.axelor.db.annotations.TrackField;
import com.axelor.db.annotations.TrackMessage;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "ACCOUNT_INVOICE", uniqueConstraints = { @UniqueConstraint(columnNames = { "invoiceId", "company" }) }, indexes = { @Index(columnList = "canceled_payment_schedule"), @Index(columnList = "invoiceId"), @Index(columnList = "company"), @Index(columnList = "partner"), @Index(columnList = "contact_partner"), @Index(columnList = "payment_mode"), @Index(columnList = "payment_condition"), @Index(columnList = "bank_details"), @Index(columnList = "partner_account"), @Index(columnList = "address"), @Index(columnList = "price_list"), @Index(columnList = "payment_schedule"), @Index(columnList = "journal"), @Index(columnList = "move"), @Index(columnList = "old_move"), @Index(columnList = "currency"), @Index(columnList = "ventilated_by_user"), @Index(columnList = "original_invoice"), @Index(columnList = "management_object"), @Index(columnList = "standard_invoice"), @Index(columnList = "debit_blocking_reason"), @Index(columnList = "debit_blocking_by_user"), @Index(columnList = "debt_recovery_blocking_reason"), @Index(columnList = "debt_recovery_blocking_by_user"), @Index(columnList = "direct_debit_management"), @Index(columnList = "reject_move_line"), @Index(columnList = "interbank_code_line"), @Index(columnList = "payment_move"), @Index(columnList = "printedpdf"), @Index(columnList = "validated_by_user"), @Index(columnList = "company_bank_details"), @Index(columnList = "invoice_message_template"), @Index(columnList = "trading_name"), @Index(columnList = "printing_settings"), @Index(columnList = "sale_order"), @Index(columnList = "purchase_order") })
@Track(fields = { @TrackField(name = "invoiceId"), @TrackField(name = "partner"), @TrackField(name = "statusSelect", on = TrackEvent.UPDATE) }, messages = { @TrackMessage(message = "Invoice created", condition = "true", on = TrackEvent.CREATE), @TrackMessage(message = "Validated", condition = "statusSelect == 2", tag = "important"), @TrackMessage(message = "Ventilated", condition = "statusSelect == 3", tag = "success"), @TrackMessage(message = "Important", condition = "statusSelect == 3 && dueDate.isAfter(__date__)") })
public class Invoice extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_INVOICE_SEQ")
	@SequenceGenerator(name = "ACCOUNT_INVOICE_SEQ", sequenceName = "ACCOUNT_INVOICE_SEQ", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PaymentSchedule canceledPaymentSchedule;

	@Widget(title = "Invoice N°", readonly = true)
	@NameColumn
	private String invoiceId;

	@Widget(title = "Document Type", selection = "iinvoice.operation.type.select")
	@NotNull
	private Integer operationTypeSelect = 0;

	@Widget(title = "Document Subtype", selection = "iinvoice.operation.sub.type.select")
	@NotNull
	private Integer operationSubTypeSelect = 1;

	@Widget(title = "Customer tax number")
	private String partnerTaxNbr;

	@Widget(title = "Company")
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Company company;

	@Widget(title = "Partner")
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Partner partner;

	@Widget(title = "Contact")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Partner contactPartner;

	@Widget(title = "Payment mode")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PaymentMode paymentMode;

	@Widget(title = "Payment condition")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PaymentCondition paymentCondition;

	@Widget(title = "Bank details")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private BankDetails bankDetails;

	@Widget(title = "Partner account")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Account partnerAccount;

	@Widget(title = "Address")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Address address;

	@Widget(title = "Address", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String addressStr;

	@Widget(title = "Internal Ref.")
	private String internalReference;

	@Widget(title = "External Ref.")
	private String externalReference;

	@Widget(title = "Price list")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PriceList priceList;

	@Widget(title = "Hide Discount on prints")
	private Boolean hideDiscount = Boolean.FALSE;

	@Widget(title = "Invoice Date")
	private LocalDate invoiceDate;

	@Widget(title = "Due Date")
	private LocalDate dueDate;

	@Widget(title = "Origin date")
	private LocalDate originDate;

	@Widget(title = "Belong to a payment schedule")
	private Boolean schedulePaymentOk = Boolean.FALSE;

	@Widget(title = "Payment Schedule", readonly = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PaymentSchedule paymentSchedule;

	@Widget(title = "Journal")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Journal journal;

	@Widget(title = "Move", readonly = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Move move;

	@Widget(title = "Previous Move", readonly = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Move oldMove;

	@Widget(title = "Doubtful Customer")
	private Boolean doubtfulCustomerOk = Boolean.FALSE;

	@Widget(title = "Currency")
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Currency currency;

	@Widget(title = "Payments")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InvoicePayment> invoicePaymentList;

	@Widget(title = "Ventilated Date", readonly = true)
	private LocalDate ventilatedDate;

	@Widget(title = "Ventilated by", readonly = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User ventilatedByUser;

	@Widget(title = "Invoice Lines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InvoiceLine> invoiceLineList;

	@Widget(title = "Tax Lines", readonly = true)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InvoiceLineTax> invoiceLineTaxList;

	@Widget(title = "Already printed")
	private Boolean alreadyPrintedOk = Boolean.TRUE;

	@Widget(title = "Invoice of origin")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Invoice originalInvoice;

	@Widget(title = "Refunds")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "originalInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Invoice> refundInvoiceList;

	@Widget(title = "Irrecoverable status", readonly = true, selection = "iaccount.account.schedule.irrecoverable.status.select")
	private Integer irrecoverableStatusSelect = 0;

	@Widget(title = "Irrecoverable shifting reason")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ManagementObject managementObject;

	@Widget(title = "Standard Invoice")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Invoice standardInvoice;

	@Widget(title = "Batchs")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Batch> batchSet;

	@Widget(title = "Direct debit blocking")
	private Boolean debitBlockingOk = Boolean.FALSE;

	@Widget(title = "Blocking reason")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StopReason debitBlockingReason;

	@Widget(title = "Blocking until")
	private LocalDate debitBlockingToDate;

	@Widget(title = "Blocking done by")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User debitBlockingByUser;

	@Widget(title = "Debt recovery blocking")
	private Boolean debtRecoveryBlockingOk = Boolean.FALSE;

	@Widget(title = "Blocking reason")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private StopReason debtRecoveryBlockingReason;

	@Widget(title = "Blocking until")
	private LocalDate debtRecoveryBlockingToDate;

	@Widget(title = "Blocking done by")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User debtRecoveryBlockingByUser;

	@Widget(title = "Direct Debit N°")
	private String debitNumber;

	@Widget(title = "Direct debit list")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private DirectDebitManagement directDebitManagement;

	@Widget(title = "Rejection move line")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MoveLine rejectMoveLine;

	@Widget(title = "Transfer(ed) to usher")
	private Boolean usherPassageOk = Boolean.FALSE;

	@Widget(title = "Rejected amount")
	private BigDecimal amountRejected = BigDecimal.ZERO;

	@Widget(title = "Rejection date")
	private LocalDate rejectDate;

	@Widget(title = "Rejection reason")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private InterbankCodeLine interbankCodeLine;

	@Widget(title = "Debited amount", readonly = true)
	private BigDecimal directDebitAmount = BigDecimal.ZERO;

	@Widget(title = "Payment Move")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Move paymentMove;

	@Widget(selection = "grade.1.up.to.10.select")
	private Integer invoicesCopySelect = 1;

	@Widget(title = "Notes")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String note;

	@Widget(title = "Specific notes")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String specificNotes;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaFile printedPDF;

	@Widget(title = "Total W.T.", readonly = true)
	@Digits(integer = 18, fraction = 2)
	private BigDecimal exTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Total Tax", readonly = true)
	@Digits(integer = 18, fraction = 2)
	private BigDecimal taxTotal = BigDecimal.ZERO;

	@Widget(title = "Total A.T.I.", readonly = true)
	@Digits(integer = 18, fraction = 2)
	private BigDecimal inTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Amount paid", readonly = true)
	private BigDecimal amountPaid = BigDecimal.ZERO;

	@Widget(title = "Amount remaining", readonly = true)
	private BigDecimal amountRemaining = BigDecimal.ZERO;

	@Widget(title = "Total W.T.", readonly = true)
	@Digits(integer = 18, fraction = 2)
	private BigDecimal companyExTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Total Tax", readonly = true)
	@Digits(integer = 18, fraction = 2)
	private BigDecimal companyTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Total A.T.I.", readonly = true)
	@Digits(integer = 18, fraction = 2)
	private BigDecimal companyInTaxTotal = BigDecimal.ZERO;

	@Widget(title = "Remaining to pay", readonly = true)
	private BigDecimal companyInTaxTotalRemaining = BigDecimal.ZERO;

	@Widget(readonly = true)
	private Boolean hasPendingPayments = Boolean.FALSE;

	@Widget(title = "Status", readonly = true, selection = "iaccount.invoice.status.select")
	private Integer statusSelect = 1;

	@Widget(title = "Validated by", readonly = true)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User validatedByUser;

	@Widget(title = "Validated Date", readonly = true)
	private LocalDate validatedDate;

	@Widget(title = "In ATI")
	private Boolean inAti = Boolean.FALSE;

	@Widget(title = "Supplier invoice number")
	private String supplierInvoiceNb;

	@Widget(title = "Company bank")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private BankDetails companyBankDetails;

	@Widget(title = "Send email on invoice ventilation")
	private Boolean invoiceAutomaticMail = Boolean.FALSE;

	@Widget(title = "Message template")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Template invoiceMessageTemplate;

	@Widget(title = "Advance payments")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Invoice> advancePaymentInvoiceSet;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private TradingName tradingName;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PrintingSettings printingSettings;

	@Widget(title = "From")
	private LocalDate subscriptionFromDate;

	@Widget(title = "To")
	private LocalDate subscriptionToDate;

	@Widget(title = "Sale order")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private SaleOrder saleOrder;

	@Widget(title = "Purchase order")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private PurchaseOrder purchaseOrder;

	@Widget(title = "Interco")
	private Boolean interco = Boolean.FALSE;

	private Boolean createdByInterco = Boolean.FALSE;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public Invoice() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public PaymentSchedule getCanceledPaymentSchedule() {
		return canceledPaymentSchedule;
	}

	public void setCanceledPaymentSchedule(PaymentSchedule canceledPaymentSchedule) {
		this.canceledPaymentSchedule = canceledPaymentSchedule;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public Integer getOperationTypeSelect() {
		return operationTypeSelect == null ? 0 : operationTypeSelect;
	}

	public void setOperationTypeSelect(Integer operationTypeSelect) {
		this.operationTypeSelect = operationTypeSelect;
	}

	public Integer getOperationSubTypeSelect() {
		return operationSubTypeSelect == null ? 0 : operationSubTypeSelect;
	}

	public void setOperationSubTypeSelect(Integer operationSubTypeSelect) {
		this.operationSubTypeSelect = operationSubTypeSelect;
	}

	public String getPartnerTaxNbr() {
		return partnerTaxNbr;
	}

	public void setPartnerTaxNbr(String partnerTaxNbr) {
		this.partnerTaxNbr = partnerTaxNbr;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Partner getContactPartner() {
		return contactPartner;
	}

	public void setContactPartner(Partner contactPartner) {
		this.contactPartner = contactPartner;
	}

	public PaymentMode getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(PaymentMode paymentMode) {
		this.paymentMode = paymentMode;
	}

	public PaymentCondition getPaymentCondition() {
		return paymentCondition;
	}

	public void setPaymentCondition(PaymentCondition paymentCondition) {
		this.paymentCondition = paymentCondition;
	}

	public BankDetails getBankDetails() {
		return bankDetails;
	}

	public void setBankDetails(BankDetails bankDetails) {
		this.bankDetails = bankDetails;
	}

	public Account getPartnerAccount() {
		return partnerAccount;
	}

	public void setPartnerAccount(Account partnerAccount) {
		this.partnerAccount = partnerAccount;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getAddressStr() {
		return addressStr;
	}

	public void setAddressStr(String addressStr) {
		this.addressStr = addressStr;
	}

	public String getInternalReference() {
		return internalReference;
	}

	public void setInternalReference(String internalReference) {
		this.internalReference = internalReference;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
	}

	public PriceList getPriceList() {
		return priceList;
	}

	public void setPriceList(PriceList priceList) {
		this.priceList = priceList;
	}

	public Boolean getHideDiscount() {
		return hideDiscount == null ? Boolean.FALSE : hideDiscount;
	}

	public void setHideDiscount(Boolean hideDiscount) {
		this.hideDiscount = hideDiscount;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getOriginDate() {
		return originDate;
	}

	public void setOriginDate(LocalDate originDate) {
		this.originDate = originDate;
	}

	public Boolean getSchedulePaymentOk() {
		return schedulePaymentOk == null ? Boolean.FALSE : schedulePaymentOk;
	}

	public void setSchedulePaymentOk(Boolean schedulePaymentOk) {
		this.schedulePaymentOk = schedulePaymentOk;
	}

	public PaymentSchedule getPaymentSchedule() {
		return paymentSchedule;
	}

	public void setPaymentSchedule(PaymentSchedule paymentSchedule) {
		this.paymentSchedule = paymentSchedule;
	}

	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	public Move getOldMove() {
		return oldMove;
	}

	public void setOldMove(Move oldMove) {
		this.oldMove = oldMove;
	}

	public Boolean getDoubtfulCustomerOk() {
		return doubtfulCustomerOk == null ? Boolean.FALSE : doubtfulCustomerOk;
	}

	public void setDoubtfulCustomerOk(Boolean doubtfulCustomerOk) {
		this.doubtfulCustomerOk = doubtfulCustomerOk;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public List<InvoicePayment> getInvoicePaymentList() {
		return invoicePaymentList;
	}

	public void setInvoicePaymentList(List<InvoicePayment> invoicePaymentList) {
		this.invoicePaymentList = invoicePaymentList;
	}

	/**
	 * Add the given {@link InvoicePayment} item to the {@code invoicePaymentList}.
	 *
	 * <p>
	 * It sets {@code item.invoice = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addInvoicePaymentListItem(InvoicePayment item) {
		if (getInvoicePaymentList() == null) {
			setInvoicePaymentList(new ArrayList<>());
		}
		getInvoicePaymentList().add(item);
		item.setInvoice(this);
	}

	/**
	 * Remove the given {@link InvoicePayment} item from the {@code invoicePaymentList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeInvoicePaymentListItem(InvoicePayment item) {
		if (getInvoicePaymentList() == null) {
			return;
		}
		getInvoicePaymentList().remove(item);
	}

	/**
	 * Clear the {@code invoicePaymentList} collection.
	 *
	 * <p>
	 * If you have to query {@link InvoicePayment} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearInvoicePaymentList() {
		if (getInvoicePaymentList() != null) {
			getInvoicePaymentList().clear();
		}
	}

	public LocalDate getVentilatedDate() {
		return ventilatedDate;
	}

	public void setVentilatedDate(LocalDate ventilatedDate) {
		this.ventilatedDate = ventilatedDate;
	}

	public User getVentilatedByUser() {
		return ventilatedByUser;
	}

	public void setVentilatedByUser(User ventilatedByUser) {
		this.ventilatedByUser = ventilatedByUser;
	}

	public List<InvoiceLine> getInvoiceLineList() {
		return invoiceLineList;
	}

	public void setInvoiceLineList(List<InvoiceLine> invoiceLineList) {
		this.invoiceLineList = invoiceLineList;
	}

	/**
	 * Add the given {@link InvoiceLine} item to the {@code invoiceLineList}.
	 *
	 * <p>
	 * It sets {@code item.invoice = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addInvoiceLineListItem(InvoiceLine item) {
		if (getInvoiceLineList() == null) {
			setInvoiceLineList(new ArrayList<>());
		}
		getInvoiceLineList().add(item);
		item.setInvoice(this);
	}

	/**
	 * Remove the given {@link InvoiceLine} item from the {@code invoiceLineList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeInvoiceLineListItem(InvoiceLine item) {
		if (getInvoiceLineList() == null) {
			return;
		}
		getInvoiceLineList().remove(item);
	}

	/**
	 * Clear the {@code invoiceLineList} collection.
	 *
	 * <p>
	 * If you have to query {@link InvoiceLine} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearInvoiceLineList() {
		if (getInvoiceLineList() != null) {
			getInvoiceLineList().clear();
		}
	}

	public List<InvoiceLineTax> getInvoiceLineTaxList() {
		return invoiceLineTaxList;
	}

	public void setInvoiceLineTaxList(List<InvoiceLineTax> invoiceLineTaxList) {
		this.invoiceLineTaxList = invoiceLineTaxList;
	}

	/**
	 * Add the given {@link InvoiceLineTax} item to the {@code invoiceLineTaxList}.
	 *
	 * <p>
	 * It sets {@code item.invoice = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addInvoiceLineTaxListItem(InvoiceLineTax item) {
		if (getInvoiceLineTaxList() == null) {
			setInvoiceLineTaxList(new ArrayList<>());
		}
		getInvoiceLineTaxList().add(item);
		item.setInvoice(this);
	}

	/**
	 * Remove the given {@link InvoiceLineTax} item from the {@code invoiceLineTaxList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeInvoiceLineTaxListItem(InvoiceLineTax item) {
		if (getInvoiceLineTaxList() == null) {
			return;
		}
		getInvoiceLineTaxList().remove(item);
	}

	/**
	 * Clear the {@code invoiceLineTaxList} collection.
	 *
	 * <p>
	 * If you have to query {@link InvoiceLineTax} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearInvoiceLineTaxList() {
		if (getInvoiceLineTaxList() != null) {
			getInvoiceLineTaxList().clear();
		}
	}

	public Boolean getAlreadyPrintedOk() {
		return alreadyPrintedOk == null ? Boolean.FALSE : alreadyPrintedOk;
	}

	public void setAlreadyPrintedOk(Boolean alreadyPrintedOk) {
		this.alreadyPrintedOk = alreadyPrintedOk;
	}

	public Invoice getOriginalInvoice() {
		return originalInvoice;
	}

	public void setOriginalInvoice(Invoice originalInvoice) {
		this.originalInvoice = originalInvoice;
	}

	public List<Invoice> getRefundInvoiceList() {
		return refundInvoiceList;
	}

	public void setRefundInvoiceList(List<Invoice> refundInvoiceList) {
		this.refundInvoiceList = refundInvoiceList;
	}

	/**
	 * Add the given {@link Invoice} item to the {@code refundInvoiceList}.
	 *
	 * <p>
	 * It sets {@code item.originalInvoice = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addRefundInvoiceListItem(Invoice item) {
		if (getRefundInvoiceList() == null) {
			setRefundInvoiceList(new ArrayList<>());
		}
		getRefundInvoiceList().add(item);
		item.setOriginalInvoice(this);
	}

	/**
	 * Remove the given {@link Invoice} item from the {@code refundInvoiceList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeRefundInvoiceListItem(Invoice item) {
		if (getRefundInvoiceList() == null) {
			return;
		}
		getRefundInvoiceList().remove(item);
	}

	/**
	 * Clear the {@code refundInvoiceList} collection.
	 *
	 * <p>
	 * If you have to query {@link Invoice} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearRefundInvoiceList() {
		if (getRefundInvoiceList() != null) {
			getRefundInvoiceList().clear();
		}
	}

	public Integer getIrrecoverableStatusSelect() {
		return irrecoverableStatusSelect == null ? 0 : irrecoverableStatusSelect;
	}

	public void setIrrecoverableStatusSelect(Integer irrecoverableStatusSelect) {
		this.irrecoverableStatusSelect = irrecoverableStatusSelect;
	}

	public ManagementObject getManagementObject() {
		return managementObject;
	}

	public void setManagementObject(ManagementObject managementObject) {
		this.managementObject = managementObject;
	}

	public Invoice getStandardInvoice() {
		return standardInvoice;
	}

	public void setStandardInvoice(Invoice standardInvoice) {
		this.standardInvoice = standardInvoice;
	}

	public Set<Batch> getBatchSet() {
		return batchSet;
	}

	public void setBatchSet(Set<Batch> batchSet) {
		this.batchSet = batchSet;
	}

	/**
	 * Add the given {@link Batch} item to the {@code batchSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addBatchSetItem(Batch item) {
		if (getBatchSet() == null) {
			setBatchSet(new HashSet<>());
		}
		getBatchSet().add(item);
	}

	/**
	 * Remove the given {@link Batch} item from the {@code batchSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeBatchSetItem(Batch item) {
		if (getBatchSet() == null) {
			return;
		}
		getBatchSet().remove(item);
	}

	/**
	 * Clear the {@code batchSet} collection.
	 *
	 */
	public void clearBatchSet() {
		if (getBatchSet() != null) {
			getBatchSet().clear();
		}
	}

	public Boolean getDebitBlockingOk() {
		return debitBlockingOk == null ? Boolean.FALSE : debitBlockingOk;
	}

	public void setDebitBlockingOk(Boolean debitBlockingOk) {
		this.debitBlockingOk = debitBlockingOk;
	}

	public StopReason getDebitBlockingReason() {
		return debitBlockingReason;
	}

	public void setDebitBlockingReason(StopReason debitBlockingReason) {
		this.debitBlockingReason = debitBlockingReason;
	}

	public LocalDate getDebitBlockingToDate() {
		return debitBlockingToDate;
	}

	public void setDebitBlockingToDate(LocalDate debitBlockingToDate) {
		this.debitBlockingToDate = debitBlockingToDate;
	}

	public User getDebitBlockingByUser() {
		return debitBlockingByUser;
	}

	public void setDebitBlockingByUser(User debitBlockingByUser) {
		this.debitBlockingByUser = debitBlockingByUser;
	}

	public Boolean getDebtRecoveryBlockingOk() {
		return debtRecoveryBlockingOk == null ? Boolean.FALSE : debtRecoveryBlockingOk;
	}

	public void setDebtRecoveryBlockingOk(Boolean debtRecoveryBlockingOk) {
		this.debtRecoveryBlockingOk = debtRecoveryBlockingOk;
	}

	public StopReason getDebtRecoveryBlockingReason() {
		return debtRecoveryBlockingReason;
	}

	public void setDebtRecoveryBlockingReason(StopReason debtRecoveryBlockingReason) {
		this.debtRecoveryBlockingReason = debtRecoveryBlockingReason;
	}

	public LocalDate getDebtRecoveryBlockingToDate() {
		return debtRecoveryBlockingToDate;
	}

	public void setDebtRecoveryBlockingToDate(LocalDate debtRecoveryBlockingToDate) {
		this.debtRecoveryBlockingToDate = debtRecoveryBlockingToDate;
	}

	public User getDebtRecoveryBlockingByUser() {
		return debtRecoveryBlockingByUser;
	}

	public void setDebtRecoveryBlockingByUser(User debtRecoveryBlockingByUser) {
		this.debtRecoveryBlockingByUser = debtRecoveryBlockingByUser;
	}

	public String getDebitNumber() {
		return debitNumber;
	}

	public void setDebitNumber(String debitNumber) {
		this.debitNumber = debitNumber;
	}

	public DirectDebitManagement getDirectDebitManagement() {
		return directDebitManagement;
	}

	public void setDirectDebitManagement(DirectDebitManagement directDebitManagement) {
		this.directDebitManagement = directDebitManagement;
	}

	public MoveLine getRejectMoveLine() {
		return rejectMoveLine;
	}

	public void setRejectMoveLine(MoveLine rejectMoveLine) {
		this.rejectMoveLine = rejectMoveLine;
	}

	public Boolean getUsherPassageOk() {
		return usherPassageOk == null ? Boolean.FALSE : usherPassageOk;
	}

	public void setUsherPassageOk(Boolean usherPassageOk) {
		this.usherPassageOk = usherPassageOk;
	}

	public BigDecimal getAmountRejected() {
		return amountRejected == null ? BigDecimal.ZERO : amountRejected;
	}

	public void setAmountRejected(BigDecimal amountRejected) {
		this.amountRejected = amountRejected;
	}

	public LocalDate getRejectDate() {
		return rejectDate;
	}

	public void setRejectDate(LocalDate rejectDate) {
		this.rejectDate = rejectDate;
	}

	public InterbankCodeLine getInterbankCodeLine() {
		return interbankCodeLine;
	}

	public void setInterbankCodeLine(InterbankCodeLine interbankCodeLine) {
		this.interbankCodeLine = interbankCodeLine;
	}

	public BigDecimal getDirectDebitAmount() {
		return directDebitAmount == null ? BigDecimal.ZERO : directDebitAmount;
	}

	public void setDirectDebitAmount(BigDecimal directDebitAmount) {
		this.directDebitAmount = directDebitAmount;
	}

	public Move getPaymentMove() {
		return paymentMove;
	}

	public void setPaymentMove(Move paymentMove) {
		this.paymentMove = paymentMove;
	}

	public Integer getInvoicesCopySelect() {
		return invoicesCopySelect == null ? 0 : invoicesCopySelect;
	}

	public void setInvoicesCopySelect(Integer invoicesCopySelect) {
		this.invoicesCopySelect = invoicesCopySelect;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getSpecificNotes() {
		return specificNotes;
	}

	public void setSpecificNotes(String specificNotes) {
		this.specificNotes = specificNotes;
	}

	public MetaFile getPrintedPDF() {
		return printedPDF;
	}

	public void setPrintedPDF(MetaFile printedPDF) {
		this.printedPDF = printedPDF;
	}

	public BigDecimal getExTaxTotal() {
		return exTaxTotal == null ? BigDecimal.ZERO : exTaxTotal;
	}

	public void setExTaxTotal(BigDecimal exTaxTotal) {
		this.exTaxTotal = exTaxTotal;
	}

	public BigDecimal getTaxTotal() {
		return taxTotal == null ? BigDecimal.ZERO : taxTotal;
	}

	public void setTaxTotal(BigDecimal taxTotal) {
		this.taxTotal = taxTotal;
	}

	public BigDecimal getInTaxTotal() {
		return inTaxTotal == null ? BigDecimal.ZERO : inTaxTotal;
	}

	public void setInTaxTotal(BigDecimal inTaxTotal) {
		this.inTaxTotal = inTaxTotal;
	}

	public BigDecimal getAmountPaid() {
		return amountPaid == null ? BigDecimal.ZERO : amountPaid;
	}

	public void setAmountPaid(BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}

	public BigDecimal getAmountRemaining() {
		return amountRemaining == null ? BigDecimal.ZERO : amountRemaining;
	}

	public void setAmountRemaining(BigDecimal amountRemaining) {
		this.amountRemaining = amountRemaining;
	}

	public BigDecimal getCompanyExTaxTotal() {
		return companyExTaxTotal == null ? BigDecimal.ZERO : companyExTaxTotal;
	}

	public void setCompanyExTaxTotal(BigDecimal companyExTaxTotal) {
		this.companyExTaxTotal = companyExTaxTotal;
	}

	public BigDecimal getCompanyTaxTotal() {
		return companyTaxTotal == null ? BigDecimal.ZERO : companyTaxTotal;
	}

	public void setCompanyTaxTotal(BigDecimal companyTaxTotal) {
		this.companyTaxTotal = companyTaxTotal;
	}

	public BigDecimal getCompanyInTaxTotal() {
		return companyInTaxTotal == null ? BigDecimal.ZERO : companyInTaxTotal;
	}

	public void setCompanyInTaxTotal(BigDecimal companyInTaxTotal) {
		this.companyInTaxTotal = companyInTaxTotal;
	}

	public BigDecimal getCompanyInTaxTotalRemaining() {
		return companyInTaxTotalRemaining == null ? BigDecimal.ZERO : companyInTaxTotalRemaining;
	}

	public void setCompanyInTaxTotalRemaining(BigDecimal companyInTaxTotalRemaining) {
		this.companyInTaxTotalRemaining = companyInTaxTotalRemaining;
	}

	public Boolean getHasPendingPayments() {
		return hasPendingPayments == null ? Boolean.FALSE : hasPendingPayments;
	}

	public void setHasPendingPayments(Boolean hasPendingPayments) {
		this.hasPendingPayments = hasPendingPayments;
	}

	public Integer getStatusSelect() {
		return statusSelect == null ? 0 : statusSelect;
	}

	public void setStatusSelect(Integer statusSelect) {
		this.statusSelect = statusSelect;
	}

	public User getValidatedByUser() {
		return validatedByUser;
	}

	public void setValidatedByUser(User validatedByUser) {
		this.validatedByUser = validatedByUser;
	}

	public LocalDate getValidatedDate() {
		return validatedDate;
	}

	public void setValidatedDate(LocalDate validatedDate) {
		this.validatedDate = validatedDate;
	}

	public Boolean getInAti() {
		return inAti == null ? Boolean.FALSE : inAti;
	}

	public void setInAti(Boolean inAti) {
		this.inAti = inAti;
	}

	public String getSupplierInvoiceNb() {
		return supplierInvoiceNb;
	}

	public void setSupplierInvoiceNb(String supplierInvoiceNb) {
		this.supplierInvoiceNb = supplierInvoiceNb;
	}

	public BankDetails getCompanyBankDetails() {
		return companyBankDetails;
	}

	public void setCompanyBankDetails(BankDetails companyBankDetails) {
		this.companyBankDetails = companyBankDetails;
	}

	public Boolean getInvoiceAutomaticMail() {
		return invoiceAutomaticMail == null ? Boolean.FALSE : invoiceAutomaticMail;
	}

	public void setInvoiceAutomaticMail(Boolean invoiceAutomaticMail) {
		this.invoiceAutomaticMail = invoiceAutomaticMail;
	}

	public Template getInvoiceMessageTemplate() {
		return invoiceMessageTemplate;
	}

	public void setInvoiceMessageTemplate(Template invoiceMessageTemplate) {
		this.invoiceMessageTemplate = invoiceMessageTemplate;
	}

	public Set<Invoice> getAdvancePaymentInvoiceSet() {
		return advancePaymentInvoiceSet;
	}

	public void setAdvancePaymentInvoiceSet(Set<Invoice> advancePaymentInvoiceSet) {
		this.advancePaymentInvoiceSet = advancePaymentInvoiceSet;
	}

	/**
	 * Add the given {@link Invoice} item to the {@code advancePaymentInvoiceSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addAdvancePaymentInvoiceSetItem(Invoice item) {
		if (getAdvancePaymentInvoiceSet() == null) {
			setAdvancePaymentInvoiceSet(new HashSet<>());
		}
		getAdvancePaymentInvoiceSet().add(item);
	}

	/**
	 * Remove the given {@link Invoice} item from the {@code advancePaymentInvoiceSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeAdvancePaymentInvoiceSetItem(Invoice item) {
		if (getAdvancePaymentInvoiceSet() == null) {
			return;
		}
		getAdvancePaymentInvoiceSet().remove(item);
	}

	/**
	 * Clear the {@code advancePaymentInvoiceSet} collection.
	 *
	 */
	public void clearAdvancePaymentInvoiceSet() {
		if (getAdvancePaymentInvoiceSet() != null) {
			getAdvancePaymentInvoiceSet().clear();
		}
	}

	public TradingName getTradingName() {
		return tradingName;
	}

	public void setTradingName(TradingName tradingName) {
		this.tradingName = tradingName;
	}

	public PrintingSettings getPrintingSettings() {
		return printingSettings;
	}

	public void setPrintingSettings(PrintingSettings printingSettings) {
		this.printingSettings = printingSettings;
	}

	public LocalDate getSubscriptionFromDate() {
		return subscriptionFromDate;
	}

	public void setSubscriptionFromDate(LocalDate subscriptionFromDate) {
		this.subscriptionFromDate = subscriptionFromDate;
	}

	public LocalDate getSubscriptionToDate() {
		return subscriptionToDate;
	}

	public void setSubscriptionToDate(LocalDate subscriptionToDate) {
		this.subscriptionToDate = subscriptionToDate;
	}

	public SaleOrder getSaleOrder() {
		return saleOrder;
	}

	public void setSaleOrder(SaleOrder saleOrder) {
		this.saleOrder = saleOrder;
	}

	public PurchaseOrder getPurchaseOrder() {
		return purchaseOrder;
	}

	public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}

	public Boolean getInterco() {
		return interco == null ? Boolean.FALSE : interco;
	}

	public void setInterco(Boolean interco) {
		this.interco = interco;
	}

	public Boolean getCreatedByInterco() {
		return createdByInterco == null ? Boolean.FALSE : createdByInterco;
	}

	public void setCreatedByInterco(Boolean createdByInterco) {
		this.createdByInterco = createdByInterco;
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
		if (!(obj instanceof Invoice)) return false;

		final Invoice other = (Invoice) obj;
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
			.add("invoiceId", getInvoiceId())
			.add("operationTypeSelect", getOperationTypeSelect())
			.add("operationSubTypeSelect", getOperationSubTypeSelect())
			.add("partnerTaxNbr", getPartnerTaxNbr())
			.add("internalReference", getInternalReference())
			.add("externalReference", getExternalReference())
			.add("hideDiscount", getHideDiscount())
			.add("invoiceDate", getInvoiceDate())
			.add("dueDate", getDueDate())
			.add("originDate", getOriginDate())
			.add("schedulePaymentOk", getSchedulePaymentOk())
			.omitNullValues()
			.toString();
	}
}
