/* ==================================================================   
 * Created [2016-3-5] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.dm.record;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.boubei.tss.framework.persistence.IEntity;

/**
 * 数据用录入模块来维护，但也配以实体类。
 */
@MappedSuperclass
public abstract class ARecordTable implements IEntity {
    
    protected String creator;
    protected Date   createTime = new Date();
    protected String updator;  
    protected Date   updateTime;
    
    @Version
    private Integer version = 0;
    
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	public int hashCode() {
		return this.getPK().hashCode();
	}
	
	public boolean equals(Object other) {
		if(other == null) return false;
		
		return this.getClass().equals(other.getClass()) 
				&& this.hashCode() == other.hashCode();
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdator() {
		return updator;
	}

	public void setUpdator(String updator) {
		this.updator = updator;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}	
}

