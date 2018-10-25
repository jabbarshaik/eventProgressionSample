package com.werner.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@MappedSuperclass
@DynamicInsert(value = true)
@DynamicUpdate(value = true)
public class BaseAuditType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4910368668720329744L;

	@Column(name = "CREATE_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATE_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Version
	@Column(name = "UPDATE_VERSION")
	private short updateVersion;

	@PrePersist
	public void prePersist() {
		createDate = Objects.nonNull(getCreateDate()) ? getCreateDate() : new Timestamp(System.currentTimeMillis());
		updateDate = Objects.nonNull(getUpdateDate()) ? getUpdateDate() : new Timestamp(System.currentTimeMillis());
		createdBy = StringUtils.isEmpty(getCreatedBy()) ? "IML" : getCreatedBy();
		updatedBy = StringUtils.isEmpty(getUpdatedBy()) ? "IML" : getUpdatedBy();
		updateVersion = 1;
	}

	@PreUpdate
	public void preUpdate() {

		updateDate = new Timestamp(System.currentTimeMillis());
		if (StringUtils.isEmpty(getUpdatedBy())) {
			updatedBy = "IML";
		} else {
			updatedBy = getUpdatedBy();
		}
	}

	public String getCreatedBy() {

		return createdBy;
	}

	public void setCreatedBy(String createdBy) {

		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {

		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {

		this.updatedBy = updatedBy;
	}

	public Date getCreateDate() {

		return createDate;
	}

	public void setCreateDate(Date createDate) {

		this.createDate = createDate;
	}

	public Date getUpdateDate() {

		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {

		this.updateDate = updateDate;
	}

	public short getUpdateVersion() {

		return updateVersion;
	}

	public void setUpdateVersion(short updateVersion) {

		this.updateVersion = updateVersion;
	}
	
	public String toUpper(String value){
			return StringUtils.upperCase(value);
	}

}
