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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class StockMoveRepository extends JpaRepository<StockMove> {

	public StockMoveRepository() {
		super(StockMove.class);
	}

	public StockMove findByName(String name) {
		return Query.of(StockMove.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	public Query<StockMove> findAllBySaleOrderAndStatus(String originTypeSelect, Long originId, Integer statusSelect) {
		return Query.of(StockMove.class)
				.filter("self.originTypeSelect = :originTypeSelect AND self.originId = :originId AND self.statusSelect = :statusSelect")
				.bind("originTypeSelect", originTypeSelect)
				.bind("originId", originId)
				.bind("statusSelect", statusSelect);
	}

	// STATUS SELECT
	public static final int STATUS_DRAFT = 1;
	public static final int STATUS_PLANNED = 2;
	public static final int STATUS_REALIZED = 3;
	public static final int STATUS_CANCELED = 4;

	// AVAILABLE STATUS SELECT
	public static final int STATUS_AVAILABLE = 1;
	public static final int STATUS_PARTIALLY_AVAILABLE = 2;
	public static final int STATUS_UNAVAILABLE = 3;

	// USER TYPE
	public static final String USER_TYPE_SENDER = "Sender";
	public static final String USER_TYPE_SALESPERSON = "Salesperson";

	// TYPE SELECT
	public static final int TYPE_INTERNAL = 1;
	public static final int TYPE_OUTGOING = 2;
	public static final int TYPE_INCOMING = 3;

	// CONFORMITY SELECT
	public static final int CONFORMITY_NONE = 1;
	public static final int CONFORMITY_COMPLIANT = 2;
	public static final int CONFORMITY_NON_COMPLIANT = 3;

	public static final String ORIGIN_INVENTORY = "com.axelor.apps.stock.db.Inventory";

	public static final String ORIGIN_SALE_ORDER = "com.axelor.apps.sale.db.SaleOrder";
	public static final String ORIGIN_PURCHASE_ORDER = "com.axelor.apps.purchase.db.PurchaseOrder";

	public static final String ORIGIN_MANUF_ORDER = "com.axelor.apps.production.db.ManufOrder";
	public static final String ORIGIN_OPERATION_ORDER = "com.axelor.apps.production.db.OperationOrder";
}

