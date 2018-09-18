package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

import tech.ascs.icity.model.IdEntity;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 索引信息
 */
@ApiModel("索引信息表")
public class IndexInfo extends  IdEntity{

//	private static final long serialVersionUID = 1262251249512788L;

//	private String id;
	@ApiModelProperty(value = "表名称",required=true)
	private String tabName;
	@ApiModelProperty(value = "索引名称",required=true)
	private String indexName;
	@ApiModelProperty(value = "索引列列表",required=true)
	private String indexColumns;
	@ApiModelProperty(value = "索引类型",required=true,example="Unique,Normal")
	private String type;
	@ApiModelProperty(value = "备注")
	private String remark;
	@ApiModelProperty(value = "操作人",required=true)
	private String createBy;
	@ApiModelProperty(value = "更新人")
	private String updateBy;
	
	@ApiModelProperty(value = "创建时间")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Timestamp createTime;
	@ApiModelProperty(value = "更新时间")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Timestamp updateTime;

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getIndexColumns() {
		return indexColumns;
	}

	public void setIndexColumns(String indexColumns) {
		this.indexColumns = indexColumns;
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

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	 public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("IndexInfo[");			
		 str.append("tabName=").append(tabName);		 
		 str.append(",indexName=").append(indexName);		 
		 str.append(",indexColumns=").append(indexColumns);		 
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