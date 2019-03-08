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
package com.axelor.db;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.axelor.db.annotations.Widget;

@MappedSuperclass
public abstract class Model {

	@Widget(title = "Import ID", readonly = true, copyable = false)
	@Column(unique = true)
	private String importId;

	@Widget(title = "Imported from", readonly = true, copyable = false)
	private String importOrigin;

	public String getImportId() {
		return importId;
	}

	public void setImportId(String importId) {
		this.importId = importId;
	}

	public String getImportOrigin() {
		return importOrigin;
	}

	public void setImportOrigin(String importOrigin) {
		this.importOrigin = importOrigin;
	}

	@Version
	private Integer version;

	@Transient
	private transient boolean selected;

	@Widget(massUpdate = true)
	private Boolean archived;

	public abstract Long getId();

	public abstract void setId(Long id);

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}
}
