package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
*@author CTC
*/

@Entity
@Table(name = "iform_list_data")
public class ListData extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 4515019636098713L;

//	private String id;
	private String formId;
	private String formName;
	private String name;
	private String master;
	private String slaver;
	private String remark;
	private String orderColumn;
	private Boolean orderType;
	private Boolean batchFlag;
	private String fn;
	private String search;
	private String colList;
	private String showColumn;
	private String createBy;
	private Timestamp createTime;
	private String updateBy;
	private Timestamp updateTime;


	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getSlaver() {
		return slaver;
	}

	public void setSlaver(String slaver) {
		this.slaver = slaver;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getOrderColumn() {
		return orderColumn;
	}

	public void setOrderColumn(String orderColumn) {
		this.orderColumn = orderColumn;
	}

	public Boolean getOrderType() {
		return orderType;
	}

	public void setOrderType(Boolean orderType) {
		this.orderType = orderType;
	}

	public Boolean getBatchFlag() {
		return batchFlag;
	}

	public void setBatchFlag(Boolean batchFlag) {
		this.batchFlag = batchFlag;
	}

	public String getFn() {
		return fn;
	}

	public void setFn(String fn) {
		this.fn = fn;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getColList() {
		return colList;
	}

	public void setColList(String colList) {
		this.colList = colList;
	}

	public String getShowColumn() {
		return showColumn;
	}

	public void setShowColumn(String showColumn) {
		this.showColumn = showColumn;
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
	
}