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
package com.axelor.apps.message.db.repo;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class EmailAccountRepository extends JpaRepository<EmailAccount> {

	public EmailAccountRepository() {
		super(EmailAccount.class);
	}

	public EmailAccount findByName(String name) {
		return Query.of(EmailAccount.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	// SERVER TYPE SELECT
	public static final int SERVER_TYPE_SMTP = 1;
	public static final int SERVER_TYPE_POP = 2;
	public static final int SERVER_TYPE_IMAP = 3;

	// SECURITY TYPE SELECT
	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_SSL = 1;
	public static final int SECURITY_STARTTLS = 2;
}

