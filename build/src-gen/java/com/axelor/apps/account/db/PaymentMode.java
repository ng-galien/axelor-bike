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
package com.axelor.apps.account.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Cacheable
@Table(name = "ACCOUNT_PAYMENT_MODE", indexes = { @Index(columnList = "code") })
public class PaymentMode extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_PAYMENT_MODE_SEQ")
	@SequenceGenerator(name = "ACCOUNT_PAYMENT_MODE_SEQ", sequenceName = "ACCOUNT_PAYMENT_MODE_SEQ", allocationSize = 1)
	private Long id;

	@HashKey
	@Widget(title = "Label", translatable = true)
	@NotNull
	@Column(unique = true)
	private String name;

	@Widget(title = "Code")
	@NotNull
	private String code;

	@Widget(title = "Type", selection = "iaccount.payment.mode.type.select")
	private Integer typeSelect = 0;

	@Widget(title = "In / Out", selection = "iaccount.payment.mode.in.out.select")
	private Integer inOutSelect = 0;

	@Widget(title = "Accounting settings")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "paymentMode", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccountManagement> accountManagementList;

	private Boolean validatePaymentByDepositSlipPublication = Boolean.FALSE;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public PaymentMode() {
	}

	public PaymentMode(String name, String code) {
		this.name = name;
		this.code = code;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getTypeSelect() {
		return typeSelect == null ? 0 : typeSelect;
	}

	public void setTypeSelect(Integer typeSelect) {
		this.typeSelect = typeSelect;
	}

	public Integer getInOutSelect() {
		return inOutSelect == null ? 0 : inOutSelect;
	}

	public void setInOutSelect(Integer inOutSelect) {
		this.inOutSelect = inOutSelect;
	}

	public List<AccountManagement> getAccountManagementList() {
		return accountManagementList;
	}

	public void setAccountManagementList(List<AccountManagement> accountManagementList) {
		this.accountManagementList = accountManagementList;
	}

	/**
	 * Add the given {@link AccountManagement} item to the {@code accountManagementList}.
	 *
	 * <p>
	 * It sets {@code item.paymentMode = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addAccountManagementListItem(AccountManagement item) {
		if (getAccountManagementList() == null) {
			setAccountManagementList(new ArrayList<>());
		}
		getAccountManagementList().add(item);
		item.setPaymentMode(this);
	}

	/**
	 * Remove the given {@link AccountManagement} item from the {@code accountManagementList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeAccountManagementListItem(AccountManagement item) {
		if (getAccountManagementList() == null) {
			return;
		}
		getAccountManagementList().remove(item);
	}

	/**
	 * Clear the {@code accountManagementList} collection.
	 *
	 * <p>
	 * If you have to query {@link AccountManagement} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearAccountManagementList() {
		if (getAccountManagementList() != null) {
			getAccountManagementList().clear();
		}
	}

	public Boolean getValidatePaymentByDepositSlipPublication() {
		return validatePaymentByDepositSlipPublication == null ? Boolean.FALSE : validatePaymentByDepositSlipPublication;
	}

	public void setValidatePaymentByDepositSlipPublication(Boolean validatePaymentByDepositSlipPublication) {
		this.validatePaymentByDepositSlipPublication = validatePaymentByDepositSlipPublication;
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
		if (!(obj instanceof PaymentMode)) return false;

		final PaymentMode other = (PaymentMode) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getName(), other.getName())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(127276201, this.getName());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", getId())
			.add("name", getName())
			.add("code", getCode())
			.add("typeSelect", getTypeSelect())
			.add("inOutSelect", getInOutSelect())
			.add("validatePaymentByDepositSlipPublication", getValidatePaymentByDepositSlipPublication())
			.omitNullValues()
			.toString();
	}
}
