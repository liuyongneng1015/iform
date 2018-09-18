package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
*@author CTC
*Create on 2018-08-29 18:11:34
*/
@Entity
@Table(name = "iform_list_col_data")
public class ListColData extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1593964673524949L;

//	private String id;
	private String listDataId;
	private String tabName;
	private String colName;
	private String colNameDesc;
	private String colAsName;
	private Boolean listFlag;
	private Boolean hiddenFlag;
	private Boolean detailFlag;
	private Boolean queryFlag;
	private Integer width;
	private String type;
	private String defaultPara;
	private String defaultValue;
	private String labelsValues;
	private String multiple;
	private String label;
	private String placeholder;
	private String tipText;
	private String remark;
	private String updateBy;
	private Timestamp updateTime;
//	private String name;
	private Integer showOrder;
	private Integer orderNo;
	private String orderType;
	private Boolean quickFlag;

//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

	public String getListDataId() {
		return listDataId;
	}

	public void setListDataId(String listDataId) {
		this.listDataId = listDataId;
	}

	public String getTabName() {
		return tabName;
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

	public String getColAsName() {
		return colAsName;
	}

	public void setColAsName(String colAsName) {
		this.colAsName = colAsName;
	}

	public Boolean getListFlag() {
		return listFlag;
	}

	public void setListFlag(Boolean listFlag) {
		this.listFlag = listFlag;
	}

	public Boolean getHiddenFlag() {
		return hiddenFlag;
	}

	public void setHiddenFlag(Boolean hiddenFlag) {
		this.hiddenFlag = hiddenFlag;
	}

	public Boolean getDetailFlag() {
		return detailFlag;
	}

	public void setDetailFlag(Boolean detailFlag) {
		this.detailFlag = detailFlag;
	}

	public Boolean getQueryFlag() {
		return queryFlag;
	}

	public void setQueryFlag(Boolean queryFlag) {
		this.queryFlag = queryFlag;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultPara() {
		return defaultPara;
	}

	public void setDefaultPara(String defaultPara) {
		this.defaultPara = defaultPara;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getLabelsValues() {
		return labelsValues;
	}

	public void setLabelsValues(String labelsValues) {
		this.labelsValues = labelsValues;
	}

	public String getMultiple() {
		return multiple;
	}

	public void setMultiple(String multiple) {
		this.multiple = multiple;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getTipText() {
		return tipText;
	}

	public void setTipText(String tipText) {
		this.tipText = tipText;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}

	public Integer getShowOrder() {
		return showOrder;
	}

	public void setShowOrder(Integer showOrder) {
		this.showOrder = showOrder;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public Boolean getQuickFlag() {
		return quickFlag;
	}

	public void setQuickFlag(Boolean quickFlag) {
		this.quickFlag = quickFlag;
	}

	 public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("IformListColData[");			
		 str.append("id=").append(super.getId());		 
		 str.append(",listDataId=").append(listDataId);		 
		 str.append(",tabName=").append(tabName);		 
		 str.append(",colName=").append(colName);		 
		 str.append(",colNameDesc=").append(colNameDesc);		 
		 str.append(",colAsName=").append(colAsName);		 
		 str.append(",listFlag=").append(listFlag);		 
		 str.append(",hiddenFlag=").append(hiddenFlag);		 
		 str.append(",detailFlag=").append(detailFlag);		 
		 str.append(",queryFlag=").append(queryFlag);		 
		 str.append(",width=").append(width);		 
		 str.append(",type=").append(type);		 
		 str.append(",defaultPara=").append(defaultPara);		 
		 str.append(",defaultValue=").append(defaultValue);		 
		 str.append(",labelsValues=").append(labelsValues);		 
		 str.append(",multiple=").append(multiple);		 
		 str.append(",label=").append(label);		 
		 str.append(",placeholder=").append(placeholder);		 
		 str.append(",tipText=").append(tipText);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",updateBy=").append(updateBy);		 
		 str.append(",updateTime=").append(updateTime);		 
		 str.append(",name=").append(name);		 
		 str.append(",showOrder=").append(showOrder);		 
		 str.append(",orderNo=").append(orderNo);		 
		 str.append(",orderType=").append(orderType);		 
		 str.append(",quickFlag=").append(quickFlag);		 
		 str.append("]");			 
		 return str.toString();			 
	 }		 
}