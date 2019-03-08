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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class SequenceRepository extends JpaRepository<Sequence> {

	public SequenceRepository() {
		super(Sequence.class);
	}

	public Sequence findByCode(String code) {
		return Query.of(Sequence.class)
				.filter("self.code = :code")
				.bind("code", code)
				.fetchOne();
	}

	public Sequence findByName(String name) {
		return Query.of(Sequence.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	public Sequence find(String code, Company company) {
		return Query.of(Sequence.class)
				.filter("self.code = :code AND self.company = :company")
				.bind("code", code)
				.bind("company", company)
				.fetchOne();
	}

	//SEQUENCE SELECT
	public static final String PARTNER = "partner";
	public static final String PRODUCT = "product";

	//SEQUENCE SELECT
	public static final String PURCHASE_ORDER = "purchaseOrder";

	//SEQUENCE SELECT
			public static final String SALES_ORDER = "saleOrder";

	//SEQUENCE SELECT
			public static final String INVENTORY = "inventory";
			public static final String INTERNAL = "intStockMove";
	public static final String OUTGOING = "outStockMove";
	public static final String INCOMING = "inStockMove";
	public static final String PRODUCT_TRACKING_NUMBER = "productTrackingNumber";
	public static final String LOGISTICAL_FORM = "logisticalForm";

	//SEQUENCE SELECT
		public static final String ACCOUNT_CLEARANCE = "accountClearance";
		public static final String ACCOUNTING_REPORT = "accountingReport";
		public static final String ANALYTIC_REPORT = "analyticReport";
		public static final String CHEQUE_REJECT = "chequeReject";
		public static final String DEPOSIT_SLIP = "depositSlip";
		public static final String DEBIT = "debit";
		public static final String INVOICE = "invoice";
		public static final String IRRECOVERABLE = "irrecoverable";
		public static final String MOVE = "move";
		public static final String MOVE_LINE_EXPORT = "moveLineExport";
		public static final String PAYMENT_SCHEDULE = "paymentSchedule";
		public static final String PAYMENT_VOUCHER = "paymentVoucher";
		public static final String PAYMENT_VOUCHER_RECEIPT_NUMBER = "paymentVoucherReceiptNo";
		public static final String RECONCILE = "reconcile";
	public static final String RECONCILE_GROUP_DRAFT = "reconcileGroupDraft";
	public static final String RECONCILE_GROUP_FINAL = "reconcileGroupFinal";
		public static final String REIMBOURSEMENT = "reimbursement";
		public static final String SALES_INTERFACE = "saleInterface";
		public static final String REFUND_INTERFACE = "refundInterface";
		public static final String TREASURY_INTERFACE = "treasuryInterface";
		public static final String PURCHASE_INTERFACE = "purchaseInterface";
		public static final String SUB_ROGATION_RELEASE = "subrogationRelease";

	//SEQUENCE SELECT
			public static final String PRODUCTION_ORDER = "productionOrder";
			public static final String MANUF_ORDER = "manufOrder";
			public static final String RAW_MATERIAL_REQUIREMENT = "rawMaterialRequirement";
}

