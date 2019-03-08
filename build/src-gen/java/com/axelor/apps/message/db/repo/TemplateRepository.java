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

import com.axelor.apps.message.db.Template;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class TemplateRepository extends JpaRepository<Template> {

	public TemplateRepository() {
		super(Template.class);
	}

	public Template findByName(String name) {
		return Query.of(Template.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	// MEDIA TYPE SELECT
	public static final int MEDIA_TYPE_MAIL = 1;
	public static final int MEDIA_TYPE_EMAIL = 2;

	public static final int MEDIA_TYPE_CHAT = 3;
}

