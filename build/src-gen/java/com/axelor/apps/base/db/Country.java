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

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Cacheable
@Table(name = "BASE_COUNTRY", indexes = { @Index(columnList = "name"), @Index(columnList = "economic_area") })
public class Country extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BASE_COUNTRY_SEQ")
	@SequenceGenerator(name = "BASE_COUNTRY_SEQ", sequenceName = "BASE_COUNTRY_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Alpha-3 code (ISO)")
	@Size(min = 3, max = 3)
	private String alpha3Code;

	@Widget(title = "Alpha-2 code (ISO)")
	@Size(min = 2, max = 2)
	private String alpha2Code;

	@Widget(title = "Numeric code (ISO)")
	@Size(min = 3, max = 3)
	private String numericCode;

	@Widget(title = "Country name")
	@NotNull
	private String name;

	@Widget(title = "COG")
	private String cog;

	@Widget(title = "Calling code")
	private String phonePrefix;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private EconomicArea economicArea;

	@Widget(title = "Is ISPM15 required")
	private Boolean isIspmRequired = Boolean.FALSE;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public Country() {
	}

	public Country(String name) {
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getAlpha3Code() {
		return alpha3Code;
	}

	public void setAlpha3Code(String alpha3Code) {
		this.alpha3Code = alpha3Code;
	}

	public String getAlpha2Code() {
		return alpha2Code;
	}

	public void setAlpha2Code(String alpha2Code) {
		this.alpha2Code = alpha2Code;
	}

	public String getNumericCode() {
		return numericCode;
	}

	public void setNumericCode(String numericCode) {
		this.numericCode = numericCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCog() {
		return cog;
	}

	public void setCog(String cog) {
		this.cog = cog;
	}

	public String getPhonePrefix() {
		return phonePrefix;
	}

	public void setPhonePrefix(String phonePrefix) {
		this.phonePrefix = phonePrefix;
	}

	public EconomicArea getEconomicArea() {
		return economicArea;
	}

	public void setEconomicArea(EconomicArea economicArea) {
		this.economicArea = economicArea;
	}

	public Boolean getIsIspmRequired() {
		return isIspmRequired == null ? Boolean.FALSE : isIspmRequired;
	}

	public void setIsIspmRequired(Boolean isIspmRequired) {
		this.isIspmRequired = isIspmRequired;
	}

	public String getAttrs() {
		return attrs;
	}

	public void setAttrs(String attrs) {
		this.attrs = attrs;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof Country)) return false;

		final Country other = (Country) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", getId())
			.add("alpha3Code", getAlpha3Code())
			.add("alpha2Code", getAlpha2Code())
			.add("numericCode", getNumericCode())
			.add("name", getName())
			.add("cog", getCog())
			.add("phonePrefix", getPhonePrefix())
			.add("isIspmRequired", getIsIspmRequired())
			.omitNullValues()
			.toString();
	}
}
