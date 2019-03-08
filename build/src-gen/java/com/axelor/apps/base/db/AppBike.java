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
package com.axelor.apps.base.db;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.axelor.db.EntityHelper;
import com.axelor.db.annotations.Widget;

@Entity
@Table(name = "BASE_APP_BIKE")
public class AppBike extends App {

	@Widget(title = "Debug")
	private Boolean debug = Boolean.TRUE;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public AppBike() {
	}

	public Boolean getDebug() {
		return debug == null ? Boolean.FALSE : debug;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	public String getAttrs() {
		return attrs;
	}

	public void setAttrs(String attrs) {
		this.attrs = attrs;
	}

	@Override
	public boolean equals(Object obj) {
		return EntityHelper.equals(this, obj);
	}

	@Override
	public int hashCode() {
		return EntityHelper.hashCode(this);
	}

	@Override
	public String toString() {
		return EntityHelper.toString(this);
	}
}
