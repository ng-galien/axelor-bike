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

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class BankDetailsRepository extends JpaRepository<BankDetails> {

	public BankDetailsRepository() {
		super(BankDetails.class);
	}

	public BankDetails findByCode(String code) {
		return Query.of(BankDetails.class)
				.filter("self.code = :code")
				.bind("code", code)
				.fetchOne();
	}

	public BankDetails findDefaultByPartner(Partner partner) {
		return Query.of(BankDetails.class)
				.filter("self.partner = :partner AND self.isDefault = TRUE")
				.bind("partner", partner)
				.fetchOne();
	}

	public Query<BankDetails> findActivesByPartner(Partner partner, Boolean active) {
		return Query.of(BankDetails.class)
				.filter("self.partner = :partner AND self.active = :active")
				.bind("partner", partner)
				.bind("active", active);
	}

}

