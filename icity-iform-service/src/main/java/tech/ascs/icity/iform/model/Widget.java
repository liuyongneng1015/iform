package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 表单控件属性
 */
@Entity
@Table(name = "iform_widget")
public class Widget extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 24346650423448344L;

//	private Integer id;
	private String fieldName;
	private String meaning;
	private String type;
	private String remark;
	private String createBy;
	private Timestamp createTime;
	private String updateBy;
	private Timestamp updateTime;

//	public Integer getId() {
//		return id;
//	}
//
//	public void setId(Integer id) {
//		this.id = id;
//	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
		 str.append("IformWidget[");			
		 str.append("id=").append(super.getId());		 
		 str.append(",fieldName=").append(fieldName);		 
		 str.append(",meaning=").append(meaning);		 
		 str.append(",type=").append(type);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",createBy=").append(createBy);		 
		 str.append(",createTime=").append(createTime);		 
		 str.append(",updateBy=").append(updateBy);		 
		 str.append(",updateTime=").append(updateTime);		 
		 str.append("]");			 
		 return str.toString();			 
	 }		 
}