package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 表信息
 */
@Entity
@Table(name = "iform_tab_info")
public class TabInfo extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 25393987869344367L;

//	private String id;
	@Column(nullable=false,length=40)
	private String tabName;
	private String tabNameDesc;
	private String tableType;
	private String masterTable;
//	private String subTable;
	private String remark;
	private Timestamp synTime;
	private Boolean synFlag;
	private String createBy;
	private Timestamp createTime;
	private String updateBy;
	private Timestamp updateTime;
	
	
//	@OneToMany(mappedBy = "tabInfo", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinColumn(name="tabInfoId")
	@JsonIgnore
	private List<ColumnData> columnDatas = new ArrayList<ColumnData>();
//	 private Set<ColumnData> columnDatas; 
	
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
		 str.append(",tabName=").append(tabName);		 
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