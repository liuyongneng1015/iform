package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;


/**
 * 同步操作记录
 */
@Entity
@Table(name = "iform_syn_log")
public class SynLog extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 169396081482762939L;

//	private String id;
	private String tabInfoId; 
	private String tabName;
	private String sqlType;
	private String remark;
	private String synBy;
	private Date synTime;
	
	public String getTabInfoId() {
		return tabInfoId;
	}

	public void setTabInfoId(String tabInfoId) {
		this.tabInfoId = tabInfoId;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSynBy() {
		return synBy;
	}

	public void setSynBy(String synBy) {
		this.synBy = synBy;
	}

	public Date getSynTime() {
		return synTime;
	}

	public void setSynTime(Date synTime) {
		this.synTime = synTime;
	}

	 public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("SynLog[");			
		 str.append("tabName=").append(tabName);		 
		 str.append(",sqlType=").append(sqlType);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",synBy=").append(synBy);		 
		 str.append(",synTime=").append(synTime);		 
		 str.append("]");			 
		 return str.toString();			 
	 }		 
}