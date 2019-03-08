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
package com.axelor.apps.message.db;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.TemplateContext;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "MESSAGE_TEMPLATE", indexes = { @Index(columnList = "name"), @Index(columnList = "meta_model"), @Index(columnList = "template_context"), @Index(columnList = "birt_template"), @Index(columnList = "language") })
public class Template extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MESSAGE_TEMPLATE_SEQ")
	@SequenceGenerator(name = "MESSAGE_TEMPLATE_SEQ", sequenceName = "MESSAGE_TEMPLATE_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NameColumn
	@NotNull
	private String name;

	@Widget(title = "Content", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String content;

	@Widget(title = "Subject", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String subject;

	@Widget(title = "From", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String fromAdress;

	@Widget(title = "Reply to", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String replyToRecipients;

	@Widget(title = "To", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String toRecipients;

	@Widget(title = "Cc", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String ccRecipients;

	@Widget(title = "Bcc", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String bccRecipients;

	@Widget(title = "Target receptor")
	private String target;

	@Widget(title = "Media Type", selection = "template.media.type.select")
	@NotNull
	private Integer mediaTypeSelect = 2;

	@Widget(title = "Address Block", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "text")
	private String addressBlock;

	@Widget(title = "Model")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "Default")
	private Boolean isDefault = Boolean.FALSE;

	@Widget(title = "System")
	private Boolean isSystem = Boolean.FALSE;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private TemplateContext templateContext;

	@Widget(title = "Birt Template")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private BirtTemplate birtTemplate;

	@Widget(title = "Language")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Language language;

	@Widget(title = "Attributes")
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "json")
	private String attrs;

	public Template() {
	}

	public Template(String name) {
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFromAdress() {
		return fromAdress;
	}

	public void setFromAdress(String fromAdress) {
		this.fromAdress = fromAdress;
	}

	public String getReplyToRecipients() {
		return replyToRecipients;
	}

	public void setReplyToRecipients(String replyToRecipients) {
		this.replyToRecipients = replyToRecipients;
	}

	public String getToRecipients() {
		return toRecipients;
	}

	public void setToRecipients(String toRecipients) {
		this.toRecipients = toRecipients;
	}

	public String getCcRecipients() {
		return ccRecipients;
	}

	public void setCcRecipients(String ccRecipients) {
		this.ccRecipients = ccRecipients;
	}

	public String getBccRecipients() {
		return bccRecipients;
	}

	public void setBccRecipients(String bccRecipients) {
		this.bccRecipients = bccRecipients;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Integer getMediaTypeSelect() {
		return mediaTypeSelect == null ? 0 : mediaTypeSelect;
	}

	public void setMediaTypeSelect(Integer mediaTypeSelect) {
		this.mediaTypeSelect = mediaTypeSelect;
	}

	public String getAddressBlock() {
		return addressBlock;
	}

	public void setAddressBlock(String addressBlock) {
		this.addressBlock = addressBlock;
	}

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public Boolean getIsDefault() {
		return isDefault == null ? Boolean.FALSE : isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public Boolean getIsSystem() {
		return isSystem == null ? Boolean.FALSE : isSystem;
	}

	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}

	public TemplateContext getTemplateContext() {
		return templateContext;
	}

	public void setTemplateContext(TemplateContext templateContext) {
		this.templateContext = templateContext;
	}

	public BirtTemplate getBirtTemplate() {
		return birtTemplate;
	}

	public void setBirtTemplate(BirtTemplate birtTemplate) {
		this.birtTemplate = birtTemplate;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
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
		if (!(obj instanceof Template)) return false;

		final Template other = (Template) obj;
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
			.add("target", getTarget())
			.add("mediaTypeSelect", getMediaTypeSelect())
			.add("isDefault", getIsDefault())
			.add("isSystem", getIsSystem())
			.omitNullValues()
			.toString();
	}
}
