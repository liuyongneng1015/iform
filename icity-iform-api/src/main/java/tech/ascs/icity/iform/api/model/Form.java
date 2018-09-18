package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

import tech.ascs.icity.model.IdEntity;

/**
 * 自定义表单
 */
@ApiModel("自定义表单表")
public class Form extends IdEntity{

//	private String id;
	@ApiModelProperty(value = "实体表名称列表(多个用逗号隔开),即关联数据表")
	private String tabNameList;
	@ApiModelProperty(value = "表单类别")
	private String category;
	@ApiModelProperty(value = "表单状态")
	private String status;
	@ApiModelProperty(value = "描述(备注)")
	private String remark;
	@ApiModelProperty(value = "创建人员")
	private String createBy;
	@ApiModelProperty(value = "创建时间")
	private Timestamp createTime;
	@ApiModelProperty(value = "更新人员")
	private String updateBy;
	@ApiModelProperty(value = "更新时间")
	private Timestamp updateTime;
	@ApiModelProperty(value = "表单对应的json数据",required=true)
	private String jsonData;
	@ApiModelProperty(value = "表单名",required=true)
	private String name;
	
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		 str.append("Form[");			
		 str.append("id=").append(super.getId());		
		 str.append(",name=").append(name);	
		 str.append(",tabNameList=").append(tabNameList);		 
		 str.append(",category=").append(category);		 
		 str.append(",status=").append(status);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",createBy=").append(createBy);		 
		 str.append(",createTime=").append(createTime);		 
		 str.append(",updateBy=").append(updateBy);		 
		 str.append(",updateTime=").append(updateTime);		 
		 str.append(",jsonData= ").append(jsonData);	
		 str.append(" ]");			 
		 return str.toString();			 
	 }		 
}