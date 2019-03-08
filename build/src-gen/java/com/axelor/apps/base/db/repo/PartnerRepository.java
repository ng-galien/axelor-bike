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

import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class PartnerRepository extends JpaRepository<Partner> {

	public PartnerRepository() {
		super(Partner.class);
	}

	public Partner findByName(String name) {
		return Query.of(Partner.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	public Partner findByUser(User user) {
		return Query.of(Partner.class)
				.filter("self.user = :user")
				.bind("user", user)
				.fetchOne();
	}

	public static final int PARTNER_TYPE_COMPANY = 1;
	public static final int PARTNER_TYPE_INDIVIDUAL = 2;

	public static final int PARTNER_TITLE_M = 1;
	public static final int PARTNER_TITLE_MS = 2;
	public static final int PARTNER_TITLE_DR = 3;
	public static final int PARTNER_TITLE_PROF = 4;
}

