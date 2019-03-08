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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.db.JpaRepository;

public class InvoiceRepository extends JpaRepository<Invoice> {

	public InvoiceRepository() {
		super(Invoice.class);
	}

	static final int NONE = 0;

	// OPERATION TYPE SELECT
	public static final int OPERATION_TYPE_SUPPLIER_PURCHASE = 1;
	public static final int OPERATION_TYPE_SUPPLIER_REFUND = 2;
	public static final int OPERATION_TYPE_CLIENT_SALE = 3;
	public static final int OPERATION_TYPE_CLIENT_REFUND = 4;

	// IRRECOVERABLE STATUS SELECT
	public static final int IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE = 0;
	public static final int IRRECOVERABLE_STATUS_TO_PASS_IN_IRRECOUVRABLE = 1;
	public static final int IRRECOVERABLE_STATUS_PASSED_IN_IRRECOUVRABLE = 2;

	// STATUS SELECT
	public static final int STATUS_DRAFT = 1;
	public static final int STATUS_VALIDATED = 2;
	public static final int STATUS_VENTILATED = 3;
	public static final int STATUS_CANCELED = 4;

	// OPERATION TYPE SUB SELECT
	public static final int OPERATION_SUB_TYPE_DEFAULT = 1;
	public static final int OPERATION_SUB_TYPE_ADVANCE = 2;
	public static final int OPERATION_SUB_TYPE_BALANCE = 3;

	// OPERATION TYPE SUB SELECT
	public static final int OPERATION_SUB_TYPE_SUBSCRIPTION = 6;
}

