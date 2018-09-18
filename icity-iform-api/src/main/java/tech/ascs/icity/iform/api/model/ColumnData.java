package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

import tech.ascs.icity.model.IdEntity;

/**
 * 字段数据
 */
@ApiModel("字段数据表")
public class ColumnData extends IdEntity{

//	private String id;
	@ApiModelProperty(value = "表名称",required=true)
	private String tabName;
	@ApiModelProperty(value = "tabInfo表的id",required=true)
	private String tabInfoId;
	@ApiModelProperty(value = "列名称",required=true)
	private String colName;
	@ApiModelProperty(value = "表名称描述")
	private String colNameDesc;
	@ApiModelProperty(value = "字段类型",required=true)
	private String type;
	@ApiModelProperty(value = "长度")
	private Integer length;
	@ApiModelProperty(value = "小数长数")
	private Integer decimalLen;
	@ApiModelProperty(value = "不允许为空",required=true)
	private Boolean notNull;
	@ApiModelProperty(value = "主键标志",required=true)
	private Boolean keyFlag;
	@ApiModelProperty(value = "默认值")
	private String defaultValue;
	@ApiModelProperty(value = "备注")
	private String remark;
	@ApiModelProperty(value = "外键关联id")
	private String foreignKey;
	@ApiModelProperty(value = "外键关联表")
	private String foreignTab;
	@ApiModelProperty(value = "创建人",required=true)
	private String createBy;
	@ApiModelProperty(value = "创建时间")
	private Timestamp createTime;
	@ApiModelProperty(value = "更新人")
	private String updateBy;
	@ApiModelProperty(value = "更新时间")
	private Timestamp updateTime;

	
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

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}
	
	public String getTabInfoId() {
		return tabInfoId;
	}

	public void setTabInfoId(String tabInfoId) {
		this.tabInfoId = tabInfoId;
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

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	
	 public Boolean getNotNull() {
		return notNull;
	}

	public void setNotNull(Boolean notNull) {
		this.notNull = notNull;
	}

	public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("ColumnData[");			
		 str.append("tabName=").append(tabName);		 
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