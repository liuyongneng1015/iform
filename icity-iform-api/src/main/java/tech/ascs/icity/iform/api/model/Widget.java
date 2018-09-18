package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

import tech.ascs.icity.model.IdEntity;

/**
 * 表单控件
 */
@ApiModel("表单控件表")
public class Widget extends IdEntity{

//	private Integer id;
    @ApiModelProperty(value = "属性名称", position = 2)
	private String fieldName;
    @ApiModelProperty(value = "属性含义", position = 3)
	private String meaning;
    @ApiModelProperty(value = "控件类型", position = 4)
	private String type;
    @ApiModelProperty(value = "备注", position = 5)
	private String remark;
	@ApiModelProperty(value = "创建人")
	private String createBy;
	@ApiModelProperty(value = "创建时间")
	private Timestamp createTime;
	@ApiModelProperty(value = "更新人")
	private String updateBy;
	@ApiModelProperty(value = "更新时间")
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