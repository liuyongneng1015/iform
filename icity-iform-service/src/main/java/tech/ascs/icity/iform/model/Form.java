package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 自定义表单
 */
@Entity
@Table(name = "iform_form")
public class Form extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 38431584119262665L;

//	private String id;
	private String tabNameList;
	private String category;
	private String status;
	private String remark;
	private String createBy;
	private Timestamp createTime;
	private String updateBy;
	private Timestamp updateTime;
	private String jsonData;

//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

	public String getTabNameList() {
		return tabNameList;
	}

	public void setTabNameList(String tabNameList) {
		this.tabNameList = tabNameList;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
	
	 public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("IformForm[");			
		 str.append("id=").append(super.getId());		 
		 str.append(",tabNameList=").append(tabNameList);		 
		 str.append(",category=").append(category);		 
		 str.append(",status=").append(status);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",createBy=").append(createBy);		 
		 str.append(",createTime=").append(createTime);		 
		 str.append(",updateBy=").append(updateBy);		 
		 str.append(",updateTime=").append(updateTime);		 
		 str.append(",jsonData=").append(jsonData);	
		 str.append("]");			 
		 return str.toString();			 
	 }		 
}