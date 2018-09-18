package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 表单控件数据记录
 */
@Entity
@Table(name = "iform_form_widget")
public class FormWidget extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 255978473092377L;
//	private String id;
	private String formId;
	private String tabName;
	private String colName;
	private Boolean visible;
	private Boolean disabledFlag;
	private Boolean readonlyFlag;
	private Boolean required;
	private String widgetId;
	private String name;
	private String type;
	private String value;
	private String defaultPara;
	private String defaultValue;
	private String label;
	private String placeholder;
	private String tipText;
	private String size;
	private String maxlength;
	private Boolean passWord;
	private String cols;
	private String rows;
	private Boolean multiple;
	private String labelsValues;
	private String defaultName;
	private String url;
	private String childItem;
	private String remark;
	private String createBy;
	private Timestamp createTime;
	private String updateBy;
	private Timestamp updateTime;

//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
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

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getDisabledFlag() {
		return disabledFlag;
	}

	public void setDisabledFlag(Boolean disabledFlag) {
		this.disabledFlag = disabledFlag;
	}

	public Boolean getReadonlyFlag() {
		return readonlyFlag;
	}

	public void setReadonlyFlag(Boolean readonlyFlag) {
		this.readonlyFlag = readonlyFlag;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getMaxlength() {
		return maxlength;
	}

	public void setMaxlength(String maxlength) {
		this.maxlength = maxlength;
	}

	public Boolean getPassWord() {
		return passWord;
	}

	public void setPassWord(Boolean passWord) {
		this.passWord = passWord;
	}

	public String getCols() {
		return cols;
	}

	public void setCols(String cols) {
		this.cols = cols;
	}

	public String getRows() {
		return rows;
	}

	public void setRows(String rows) {
		this.rows = rows;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public String getLabelsValues() {
		return labelsValues;
	}

	public void setLabelsValues(String labelsValues) {
		this.labelsValues = labelsValues;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getChildItem() {
		return childItem;
	}

	public void setChildItem(String childItem) {
		this.childItem = childItem;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	 public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("IformFormWidget[");			
		 str.append("id=").append(super.getId());		 
		 str.append(",formId=").append(formId);		 
		 str.append(",tabName=").append(tabName);		 
		 str.append(",colName=").append(colName);		 
		 str.append(",visible=").append(visible);		 
		 str.append(",disabledFlag=").append(disabledFlag);		 
		 str.append(",readonlyFlag=").append(readonlyFlag);		 
		 str.append(",required=").append(required);		 
		 str.append(",widgetId=").append(widgetId);		 
		 str.append(",name=").append(name);		 
		 str.append(",type=").append(type);		 
		 str.append(",value=").append(value);		 
		 str.append(",defaultPara=").append(defaultPara);		 
		 str.append(",defaultValue=").append(defaultValue);		 
		 str.append(",label=").append(label);		 
		 str.append(",placeholder=").append(placeholder);		 
		 str.append(",tipText=").append(tipText);		 
		 str.append(",size=").append(size);		 
		 str.append(",maxlength=").append(maxlength);		 
		 str.append(",passWord=").append(passWord);		 
		 str.append(",cols=").append(cols);		 
		 str.append(",rows=").append(rows);		 
		 str.append(",multiple=").append(multiple);		 
		 str.append(",labelsValues=").append(labelsValues);		 
		 str.append(",defaultName=").append(defaultName);		 
		 str.append(",url=").append(url);		 
		 str.append(",childItem=").append(childItem);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",createBy=").append(createBy);		 
		 str.append(",createTime=").append(createTime);		 
		 str.append(",updateBy=").append(updateBy);		 
		 str.append(",updateTime=").append(updateTime);		 
		 str.append("]");			 
		 return str.toString();			 
	 }	 
}