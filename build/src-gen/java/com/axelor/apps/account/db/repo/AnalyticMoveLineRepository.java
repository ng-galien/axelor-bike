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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.db.JpaRepository;

public class AnalyticMoveLineRepository extends JpaRepository<AnalyticMoveLine> {

	public AnalyticMoveLineRepository() {
		super(AnalyticMoveLine.class);
	}

	// STATUS SELECT
	public static final int STATUS_FORECAST_ORDER = 1;
	public static final int STATUS_FORECAST_INVOICE = 2;
	public static final int STATUS_REAL_ACCOUNTING = 3;
}

