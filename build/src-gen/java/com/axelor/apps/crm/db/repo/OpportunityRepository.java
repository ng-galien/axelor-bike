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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class OpportunityRepository extends JpaRepository<Opportunity> {

	public OpportunityRepository() {
		super(Opportunity.class);
	}

	public Opportunity findByName(String name) {
		return Query.of(Opportunity.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	// Stage SELECT
		public static final int SALES_STAGE_NEW = 1;
		public static final int SALES_STAGE_QUALIFICATION = 2;
		public static final int SALES_STAGE_PROPOSITION = 3;
		public static final int SALES_STAGE_NEGOTIATION = 4;
		public static final int SALES_STAGE_CLOSED_WON = 5;
		public static final int SALES_STAGE_CLOSED_LOST = 6;
}

