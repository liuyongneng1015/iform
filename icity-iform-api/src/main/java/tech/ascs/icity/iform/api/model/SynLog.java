package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

import tech.ascs.icity.model.IdEntity;


/**
 * 同步操作记录
 */
@ApiModel("同步操作记录表")
public class SynLog  extends  IdEntity{
//	private static final long serialVersionUID = 169396081482762939L;

//	private String id;
	@ApiModelProperty(value = "动态表id",required=true)
	private String tabInfoId; 
	@ApiModelProperty(value = "表名称",required=true)
	private String tabName;
	@ApiModelProperty(value = "sql类型",required=true)
	private String sqlType;
	@ApiModelProperty(value = "备注")
	private String remark;
	@ApiModelProperty(value = "同步操作人",required=true)
	private String synBy;
	@ApiModelProperty(value = "同步操作时间")
	private Date synTime;
	
	public String getTabInfoId() {
		return tabInfoId;
	}

	public void setTabInfoId(String tabInfoId) {
		this.tabInfoId = tabInfoId;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSynBy() {
		return synBy;
	}

	public void setSynBy(String synBy) {
		this.synBy = synBy;
	}
	

	 public Date getSynTime() {
		return synTime;
	}

	public void setSynTime(Date synTime) {
		this.synTime = synTime;
	}

	public String toString(){	
		 StringBuffer str=new StringBuffer();	
		 str.append("SynLog[");			
		 str.append("tabName=").append(tabName);		 
		 str.append(",sqlType=").append(sqlType);		 
		 str.append(",remark=").append(remark);		 
		 str.append(",synBy=").append(synBy);		 
		 str.append(",synTime=").append(synTime);		 
		 str.append("]");			 
		 return str.toString();			 
	 }		 
}