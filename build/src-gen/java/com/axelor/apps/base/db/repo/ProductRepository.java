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

import com.axelor.apps.base.db.Product;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class ProductRepository extends JpaRepository<Product> {

	public ProductRepository() {
		super(Product.class);
	}

	public Product findByCode(String code) {
		return Query.of(Product.class)
				.filter("self.code = :code")
				.bind("code", code)
				.fetchOne();
	}

	public Product findByName(String name) {
		return Query.of(Product.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	// PRODUCT TYPE SELECT
	public static final String PRODUCT_TYPE_SERVICE = "service";
	public static final String PRODUCT_TYPE_STORABLE = "storable";
	public static final String PRODUCT_TYPE_PACK = "pack";

	// SALE SUPPLY SELECT
	public static final int SALE_SUPPLY_FROM_STOCK = 1;
	public static final int SALE_SUPPLY_PURCHASE = 2;
	public static final int SALE_SUPPLY_PRODUCE = 3;

	public static final String PROCUREMENT_METHOD_BUY = "buy";
	public static final String PROCUREMENT_METHOD_PRODUCE = "produce";
	public static final String PROCUREMENT_METHOD_BUYANDPRODUCE = "buyAndProduce";

	public static final int COST_TYPE_STANDARD = 1;
	public static final int COST_TYPE_LAST_PURCHASE_PRICE = 2;
	public static final int COST_TYPE_AVERAGE_PRICE = 3;
	public static final int COST_TYPE_LAST_PRODUCTION_PRICE = 4;

	// PRODUCT SUB-TYPE SELECT
	public static final int PRODUCT_SUB_TYPE_FINISHED_PRODUCT = 1;
	public static final int PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT = 2;
	public static final int PRODUCT_SUB_TYPE_COMPONENT = 3;

	// PRICE METHOD SELECT
	public static final int PRICE_METHOD_FORECAST = 1;
	public static final int PRICE_METHOD_REAL = 2;

	// COMPONENTS VALUATION
	public static final int COMPONENTS_VALUATION_METHOD_AVERAGE = 1;

	public static final int COMPONENTS_VALUATION_METHOD_COST = 2;
}

