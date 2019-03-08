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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Move;
import com.axelor.db.JpaRepository;

public class MoveRepository extends JpaRepository<Move> {

	public MoveRepository() {
		super(Move.class);
	}

	// STATUS SELECT
	public static final int STATUS_NEW = 1;
	public static final int STATUS_DAYBOOK = 2;
	public static final int STATUS_VALIDATED = 3;
	public static final int STATUS_CANCELED = 4;

	// TECHNICAL ORIGIN SELECT
	public static final int TECHNICAL_ORIGIN_ENTRY = 1;
	public static final int TECHNICAL_ORIGIN_AUTOMATIC = 2;
	public static final int TECHNICAL_ORIGIN_TEMPLATE = 3;
	public static final int TECHNICAL_ORIGIN_IMPORT = 4;
}

