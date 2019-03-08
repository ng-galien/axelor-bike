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

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class PaymentModeRepository extends JpaRepository<PaymentMode> {

	public PaymentModeRepository() {
		super(PaymentMode.class);
	}

	public PaymentMode findByCode(String code) {
		return Query.of(PaymentMode.class)
				.filter("self.code = :code")
				.bind("code", code)
				.fetchOne();
	}

	public PaymentMode findByName(String name) {
		return Query.of(PaymentMode.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	// TYPE
	public static final int TYPE_OTHER = 1;
	public static final int TYPE_DD = 2;
	public static final int TYPE_IPO = 3;
	public static final int TYPE_IPO_CHEQUE = 4;
	public static final int TYPE_CASH = 5;
	public static final int TYPE_BANK_CARD = 6;
	public static final int TYPE_CHEQUE = 7;
	public static final int TYPE_WEB = 8;
	public static final int TYPE_TRANSFER = 9;

	// Sales or purchase
	public static final int SALES = 1;
	public static final int PURCHASES = 2;

	// IN OUT SELECT
	public static final int IN = 1;
	public static final int OUT = 2;
}

