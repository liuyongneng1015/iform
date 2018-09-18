package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;
import java.util.List;

import tech.ascs.icity.model.IdEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
*@author CTC
*/
@ApiModel("列表建模数据表")
public class ListData extends IdEntity{

//	private String id;
    @ApiModelProperty(value = "表单id",required=true)
	private String formId;
    @ApiModelProperty(value = "表单名称",required=true)
	private String formName;
    @ApiModelProperty(value = "列表名称",required=true)
	private String name;
    @ApiModelProperty(value = "主数据表",required=true)
	private String master;
    @ApiModelProperty(value = "从数据表")
	private String slaver;
    @ApiModelProperty(value = "备注")
	private String remark;
    @ApiModelProperty(value = "排序字段")
	private String orderColumn;
    @ApiModelProperty(value = "排序类型(正序 true  倒序 false)")
	private Boolean orderType;
    @ApiModelProperty(value = "批量操作标志(true启用 false禁止)")
	private Boolean batchFlag;
    @ApiModelProperty(value = "功能列表")
	private List<Function> fn;   
    @ApiModelProperty(value = "搜索")
	private List<Search> search;
    @ApiModelProperty(value = "字段列表",required=true)
	private List<ColumnItem> colList;
	@JsonIgnore
    @ApiModelProperty(hidden=true)
	private String showColumn;
    @ApiModelProperty(value = "创建人员",required=true)
	private String createBy;
    @ApiModelProperty(value = "创建时间")
	private Timestamp createTime;
    @ApiModelProperty(value = "更新人员")
	private String updateBy;
    @ApiModelProperty(value = "更新时间")
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

	public List<ColumnItem> getColList() {
		return colList;
	}

	public void setColList(List<ColumnItem> colList) {
		this.colList = colList;
	}
	
	public List<Function> getFn() {
		return fn;
	}

	public void setFn(List<Function> fn) {
		this.fn = fn;
	}

	public List<Search> getSearch() {
		return search;
	}

	public void setSearch(List<Search> search) {
		this.search = search;
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