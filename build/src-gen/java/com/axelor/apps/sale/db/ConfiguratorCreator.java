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
package com.axelor.apps.sale.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaJsonField;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "SALE_CONFIGURATOR_CREATOR", indexes = { @Index(columnList = "name"), @Index(columnList = "configurator_bom") })
public class ConfiguratorCreator extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SALE_CONFIGURATOR_CREATOR_SEQ")
	@SequenceGenerator(name = "SALE_CONFIGURATOR_CREATOR_SEQ", sequenceName = "SALE_CONFIGURATOR_CREATOR_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NotNull
	private String name;

	@Widget(title = "Attributes")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MetaJsonField> attributes;

	@Widget(title = "Indicators")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MetaJsonField> indicators;

	@Widget(title = "Configurator formula list")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "productCreator", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConfiguratorProductFormula> configuratorProductFormulaList;

	@Widget(title = "Configurator formula list")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "soLineCreator", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConfiguratorSOLineFormula> configuratorSOLineFormulaList;

	@Widget(title = "Authorized users")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<User> authorizedUserSet;

	@Widget(title = "Authorized groups")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Group> authorizedGroupSet;

	@Widget(title = "Generate product")
	private Boolean generateProduct = Boolean.TRUE;

	@Widget(title = "Is Active")
	private Boolean isActive = Boolean.FALSE;

	@Widget(title = "Configurator BoM")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ConfiguratorBOM configuratorBom;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public ConfiguratorCreator() {
	}

	public ConfiguratorCreator(String name) {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<MetaJsonField> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<MetaJsonField> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Add the given {@link MetaJsonField} item to the {@code attributes}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addAttribute(MetaJsonField item) {
		if (getAttributes() == null) {
			setAttributes(new ArrayList<>());
		}
		getAttributes().add(item);
	}

	/**
	 * Remove the given {@link MetaJsonField} item from the {@code attributes}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeAttribute(MetaJsonField item) {
		if (getAttributes() == null) {
			return;
		}
		getAttributes().remove(item);
	}

	/**
	 * Clear the {@code attributes} collection.
	 *
	 * <p>
	 * If you have to query {@link MetaJsonField} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearAttributes() {
		if (getAttributes() != null) {
			getAttributes().clear();
		}
	}

	public List<MetaJsonField> getIndicators() {
		return indicators;
	}

	public void setIndicators(List<MetaJsonField> indicators) {
		this.indicators = indicators;
	}

	/**
	 * Add the given {@link MetaJsonField} item to the {@code indicators}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addIndicator(MetaJsonField item) {
		if (getIndicators() == null) {
			setIndicators(new ArrayList<>());
		}
		getIndicators().add(item);
	}

	/**
	 * Remove the given {@link MetaJsonField} item from the {@code indicators}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeIndicator(MetaJsonField item) {
		if (getIndicators() == null) {
			return;
		}
		getIndicators().remove(item);
	}

	/**
	 * Clear the {@code indicators} collection.
	 *
	 * <p>
	 * If you have to query {@link MetaJsonField} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearIndicators() {
		if (getIndicators() != null) {
			getIndicators().clear();
		}
	}

	public List<ConfiguratorProductFormula> getConfiguratorProductFormulaList() {
		return configuratorProductFormulaList;
	}

	public void setConfiguratorProductFormulaList(List<ConfiguratorProductFormula> configuratorProductFormulaList) {
		this.configuratorProductFormulaList = configuratorProductFormulaList;
	}

	/**
	 * Add the given {@link ConfiguratorProductFormula} item to the {@code configuratorProductFormulaList}.
	 *
	 * <p>
	 * It sets {@code item.productCreator = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addConfiguratorProductFormulaListItem(ConfiguratorProductFormula item) {
		if (getConfiguratorProductFormulaList() == null) {
			setConfiguratorProductFormulaList(new ArrayList<>());
		}
		getConfiguratorProductFormulaList().add(item);
		item.setProductCreator(this);
	}

	/**
	 * Remove the given {@link ConfiguratorProductFormula} item from the {@code configuratorProductFormulaList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeConfiguratorProductFormulaListItem(ConfiguratorProductFormula item) {
		if (getConfiguratorProductFormulaList() == null) {
			return;
		}
		getConfiguratorProductFormulaList().remove(item);
	}

	/**
	 * Clear the {@code configuratorProductFormulaList} collection.
	 *
	 * <p>
	 * If you have to query {@link ConfiguratorProductFormula} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearConfiguratorProductFormulaList() {
		if (getConfiguratorProductFormulaList() != null) {
			getConfiguratorProductFormulaList().clear();
		}
	}

	public List<ConfiguratorSOLineFormula> getConfiguratorSOLineFormulaList() {
		return configuratorSOLineFormulaList;
	}

	public void setConfiguratorSOLineFormulaList(List<ConfiguratorSOLineFormula> configuratorSOLineFormulaList) {
		this.configuratorSOLineFormulaList = configuratorSOLineFormulaList;
	}

	/**
	 * Add the given {@link ConfiguratorSOLineFormula} item to the {@code configuratorSOLineFormulaList}.
	 *
	 * <p>
	 * It sets {@code item.soLineCreator = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addConfiguratorSOLineFormulaListItem(ConfiguratorSOLineFormula item) {
		if (getConfiguratorSOLineFormulaList() == null) {
			setConfiguratorSOLineFormulaList(new ArrayList<>());
		}
		getConfiguratorSOLineFormulaList().add(item);
		item.setSoLineCreator(this);
	}

	/**
	 * Remove the given {@link ConfiguratorSOLineFormula} item from the {@code configuratorSOLineFormulaList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeConfiguratorSOLineFormulaListItem(ConfiguratorSOLineFormula item) {
		if (getConfiguratorSOLineFormulaList() == null) {
			return;
		}
		getConfiguratorSOLineFormulaList().remove(item);
	}

	/**
	 * Clear the {@code configuratorSOLineFormulaList} collection.
	 *
	 * <p>
	 * If you have to query {@link ConfiguratorSOLineFormula} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearConfiguratorSOLineFormulaList() {
		if (getConfiguratorSOLineFormulaList() != null) {
			getConfiguratorSOLineFormulaList().clear();
		}
	}

	public Set<User> getAuthorizedUserSet() {
		return authorizedUserSet;
	}

	public void setAuthorizedUserSet(Set<User> authorizedUserSet) {
		this.authorizedUserSet = authorizedUserSet;
	}

	/**
	 * Add the given {@link User} item to the {@code authorizedUserSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addAuthorizedUserSetItem(User item) {
		if (getAuthorizedUserSet() == null) {
			setAuthorizedUserSet(new HashSet<>());
		}
		getAuthorizedUserSet().add(item);
	}

	/**
	 * Remove the given {@link User} item from the {@code authorizedUserSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeAuthorizedUserSetItem(User item) {
		if (getAuthorizedUserSet() == null) {
			return;
		}
		getAuthorizedUserSet().remove(item);
	}

	/**
	 * Clear the {@code authorizedUserSet} collection.
	 *
	 */
	public void clearAuthorizedUserSet() {
		if (getAuthorizedUserSet() != null) {
			getAuthorizedUserSet().clear();
		}
	}

	public Set<Group> getAuthorizedGroupSet() {
		return authorizedGroupSet;
	}

	public void setAuthorizedGroupSet(Set<Group> authorizedGroupSet) {
		this.authorizedGroupSet = authorizedGroupSet;
	}

	/**
	 * Add the given {@link Group} item to the {@code authorizedGroupSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addAuthorizedGroupSetItem(Group item) {
		if (getAuthorizedGroupSet() == null) {
			setAuthorizedGroupSet(new HashSet<>());
		}
		getAuthorizedGroupSet().add(item);
	}

	/**
	 * Remove the given {@link Group} item from the {@code authorizedGroupSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeAuthorizedGroupSetItem(Group item) {
		if (getAuthorizedGroupSet() == null) {
			return;
		}
		getAuthorizedGroupSet().remove(item);
	}

	/**
	 * Clear the {@code authorizedGroupSet} collection.
	 *
	 */
	public void clearAuthorizedGroupSet() {
		if (getAuthorizedGroupSet() != null) {
			getAuthorizedGroupSet().clear();
		}
	}

	public Boolean getGenerateProduct() {
		return generateProduct == null ? Boolean.FALSE : generateProduct;
	}

	public void setGenerateProduct(Boolean generateProduct) {
		this.generateProduct = generateProduct;
	}

	public Boolean getIsActive() {
		return isActive == null ? Boolean.FALSE : isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public ConfiguratorBOM getConfiguratorBom() {
		return configuratorBom;
	}

	public void setConfiguratorBom(ConfiguratorBOM configuratorBom) {
		this.configuratorBom = configuratorBom;
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
		if (!(obj instanceof ConfiguratorCreator)) return false;

		final ConfiguratorCreator other = (ConfiguratorCreator) obj;
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
			.add("name", getName())
			.add("generateProduct", getGenerateProduct())
			.add("isActive", getIsActive())
			.omitNullValues()
			.toString();
	}
}
