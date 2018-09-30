package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 字段数据
 */
@Entity
@Table(name = "iform_column_data")
public class ColumnData extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1539813323166968L;

//	private String id;
	private String tabInfoId;
	private String tabName;
	private String colName;
	private String colNameDesc;
	private String type;
	private Integer length;
	private Integer decimalLen;
	private Boolean notNull;
	private Boolean keyFlag;
	private String defaultValue;
	private String remark;
	private String foreignKey;
	private String foreignTab;
	private String createBy;
	private Date createTime;
	private String updateBy;
	private Date updateTime;

	
//	@ManyToOne
//	@JsonIgnore
//	private TabInfo tabInfo;
//	
//	public TabInfo getTabInfo() {
//		return tabInfo;
//	}

//	public void setTabInfo(TabInfo tabInfo) {
//		this.tabInfo = tabInfo;
//	}
	

	public String getTabName() {
		return tabName;
	}

	public String getTabInfoId() {
		return tabInfoId;
	}

	public void setTabInfoId(String tabInfoId) {
		this.tabInfoId = tabInfoId;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getColNameDesc() {
		return colNameDesc;
	}

	public void setColNameDesc(String colNameDesc) {
		this.colNameDesc = colNameDesc;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getDecimalLen() {
		return decimalLen;
	}

	public void setDecimalLen(Integer decimalLen) {
		this.decimalLen = decimalLen;
	}



	public Boolean getNotNull() {
		return notNull;
	}

	public void setNotNull(Boolean notNull) {
		this.notNull = notNull;
	}

	public Boolean getKeyFlag() {
		return keyFlag;
	}

	public void setKeyFlag(Boolean keyFlag) {
		this.keyFlag = keyFlag;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(String foreignKey) {
		this.foreignKey = foreignKey;
	}

	public String getForeignTab() {
		return foreignTab;
	}

	public void setForeignTab(String foreignTab) {
		this.foreignTab = foreignTab;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	 public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("ColumnData[");			
		 str.append("tabName=").append(tabName);
		 str.append("tabInfoId=").append(tabInfoId);
		 str.append(",colName=").append(colName);		 
		 str.append(",colNameDesc=").append(colNameDesc);		 
		 str.append(",type=").append(type);		 
		 str.append(",length=").append(length);		 
		 str.append(",decimalLen=").append(decimalLen);		 
		 str.append(",notNull=").append(notNull);		 
		 str.append(",keyFlag=").append(keyFlag);		 
		 str.append(",defaultValue=").append(defaultValue);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",foreignKey=").append(foreignKey);		 
		 str.append(",foreignTab=").append(foreignTab);		 
		 str.append(",createBy=").append(createBy);		 
		 str.append(",createTime=").append(createTime);		 
		 str.append(",updateBy=").append(updateBy);		 
		 str.append(",updateTime=").append(updateTime);		 
		 str.append("]");			 
		 return str.toString();			 
	 }		 
}