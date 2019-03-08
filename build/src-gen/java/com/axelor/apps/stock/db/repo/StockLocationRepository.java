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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class StockLocationRepository extends JpaRepository<StockLocation> {

	public StockLocationRepository() {
		super(StockLocation.class);
	}

	public StockLocation findByName(String name) {
		return Query.of(StockLocation.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	public StockLocation findByCompany(Company company) {
		return Query.of(StockLocation.class)
				.filter("self.company = :company")
				.bind("company", company)
				.fetchOne();
	}

	public StockLocation findByPartner(Partner partner) {
		return Query.of(StockLocation.class)
				.filter("self.partner = :partner")
				.bind("partner", partner)
				.fetchOne();
	}

	// TYPE SELECT
	public static final int TYPE_INTERNAL = 1;
	public static final int TYPE_EXTERNAL = 2;
	public static final int TYPE_VIRTUAL = 3;
}

