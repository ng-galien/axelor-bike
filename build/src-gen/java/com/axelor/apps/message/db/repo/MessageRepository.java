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

import com.axelor.apps.message.db.Message;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class MessageRepository extends JpaRepository<Message> {

	public MessageRepository() {
		super(Message.class);
	}

	public Query<Message> findByRelatedTo(long relatedToSelectId, String relatedToSelect) {
		return Query.of(Message.class)
				.filter("self.relatedTo1SelectId = :relatedToSelectId	 AND self.relatedTo1Select = :relatedToSelect	OR self.relatedTo2SelectId = :relatedToSelectId	 AND self.relatedTo2Select = :relatedToSelect")
				.bind("relatedToSelectId", relatedToSelectId)
				.bind("relatedToSelect", relatedToSelect);
	}

	// TYPE SELECT
	public static final int TYPE_RECEIVED = 1;
	public static final int TYPE_SENT = 2;

	// STATUS SELECT
	public static final int STATUS_DRAFT = 1;
	public static final int STATUS_SENT = 2;

	public static final int STATUS_DELETED = 3;

	// MEDIA TYPE SELECT
	public static final int MEDIA_TYPE_MAIL = 1;
	public static final int MEDIA_TYPE_EMAIL = 2;

	public static final int MEDIA_TYPE_CHAT = 3;

	// RELATED TO SELECT
	public static final String RELATED_TO_PARTNER = "com.axelor.apps.base.db.Partner";
	public static final String RELATED_TO_LEAD = "com.axelor.apps.crm.db.Lead";
	public static final String RELATED_TO_OPPORTUNITY = "com.axelor.apps.crm.db.Opportunity";
	public static final String RELATED_TO_PRODUCT = "com.axelor.apps.base.db.Product";
	public static final String RELATED_TO_EVENT = "com.axelor.apps.crm.db.Event";
	public static final String RELATED_TO_SALESORDER = "com.axelor.apps.sale.db.SaleOrder";
	public static final String RELATED_TO_PROJECT = "com.axelor.apps.project.db.Project";
}

