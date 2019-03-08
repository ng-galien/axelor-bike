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

import com.axelor.apps.base.db.Year;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class YearRepository extends JpaRepository<Year> {

	public YearRepository() {
		super(Year.class);
	}

	public Year findByCode(String code) {
		return Query.of(Year.class)
				.filter("self.code = :code")
				.bind("code", code)
				.fetchOne();
	}

	public Year findByName(String name) {
		return Query.of(Year.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	// STATUS SELECT
	public static final int STATUS_OPENED = 1;
	public static final int STATUS_CLOSED = 2;
	public static final int STATUS_ADJUSTING = 3;

	// TYPE SELECT
	public static final int TYPE_CIVIL = 0;
	public static final int TYPE_FISCAL = 1;
	public static final int TYPE_PAYROLL = 2;
}

