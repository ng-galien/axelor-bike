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

import com.axelor.apps.base.db.ICalendar;
import com.axelor.auth.db.User;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class ICalendarRepository extends JpaRepository<ICalendar> {

	public ICalendarRepository() {
		super(ICalendar.class);
	}

	public ICalendar findByName(String name) {
		return Query.of(ICalendar.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	public ICalendar findByUser(User user) {
		return Query.of(ICalendar.class)
				.filter("self.user = :user")
				.bind("user", user)
				.fetchOne();
	}

	// TYPE SELECT
			public static final int ICAL_SERVER = 1;
	public static final int CALENDAR_SERVER = 2;
	public static final int GCAL = 3;
	public static final int ZIMBRA = 4;
	public static final int KMS = 5;
	public static final int CGP = 6;
	public static final int CHANDLER = 7;

	// SYNCHRONISATION SELECT
	public static final String ICAL_ONLY = "ICalEvent";

	// SYNCHRONISATION SELECT
	public static final String CRM_SYNCHRO = "Event";
}

