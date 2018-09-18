package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import tech.ascs.icity.model.IdEntity;

/**
 * 动态表信息
 */
@ApiModel("动态表信息")
public class TabInfo extends IdEntity{

//	private String id;

    @ApiModelProperty(value = "表名称", position = 2,required=true)
	private String tabName;
    
    @ApiModelProperty(value = "中文列名", position = 3)
	private String tabNameDesc;
    
    @ApiModelProperty(value = "表类型(单表，主表，从表)", position = 4,required=true)
	private String tableType;
    
    @ApiModelProperty(value = "从表对应的主表(当表类型为从表时需要填)", position = 5)
	private String masterTable;
    
//    @ApiModelProperty(value = "主表对应的从表(当表类型为主表时需要填)", position = 6)
//	private String subTable;
    
    @ApiModelProperty(value = "备注")
	private String remark;
    @ApiModelProperty(value = "同步时间")
	private Timestamp synTime;
    
    @ApiModelProperty(value = "同步标志",required=true)
	private Boolean synFlag;
    @ApiModelProperty(value = "创建人员",required=true)
	private String createBy;
    @ApiModelProperty(value = "创建时间")
	private Timestamp createTime;
    @ApiModelProperty(value = "更新人员")
	private String updateBy;
    @ApiModelProperty(value = "更新时间")
	private Timestamp updateTime;
	
	
//	@JsonIgnore
//    @ApiModelProperty(hidden=true)
	private List<ColumnData> columnDatas = new ArrayList<ColumnData>();
	
	public List<ColumnData> getColumnDatas() {
		return columnDatas;
	}

	public void setColumnDatas(List<ColumnData> columnDatas) {
		this.columnDatas = columnDatas;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public String getTabNameDesc() {
		return tabNameDesc;
	}

	public void setTabNameDesc(String tabNameDesc) {
		this.tabNameDesc = tabNameDesc;
	}

	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public String getMasterTable() {
		return masterTable;
	}

	public void setMasterTable(String masterTable) {
		this.masterTable = masterTable;
	}
	
//	public String getSubTable() {
//		return subTable;
//	}
//
//	public void setSubTable(String subTable) {
//		this.subTable = subTable;
//	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Timestamp getSynTime() {
		return synTime;
	}

	public void setSynTime(Timestamp synTime) {
		this.synTime = synTime;
	}

	public Boolean getSynFlag() {
		return synFlag;
	}

	public void setSynFlag(Boolean synFlag) {
		this.synFlag = synFlag;
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
		 str.append("TabInfo[");			
		 str.append("id=").append(super.getId());		 
		 str.append("tabName=").append(tabName);		 
		 str.append(",tabNameDesc=").append(tabNameDesc);		 
		 str.append(",tableType=").append(tableType);		 
		 str.append(",masterTable=").append(masterTable);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",synTime=").append(synTime);		 
		 str.append(",synFlag=").append(synFlag);		 
		 str.append(",createBy=").append(createBy);		 
		 str.append(",createTime=").append(createTime);		 
		 str.append(",updateBy=").append(updateBy);		 
		 str.append(",updateTime=").append(updateTime);		 
		 str.append("]");			 
		 return str.toString();			 
	 }	

}