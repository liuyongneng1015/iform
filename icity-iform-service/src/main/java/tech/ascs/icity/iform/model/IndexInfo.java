package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 索引信息
 */
@Entity
@Table(name = "iform_index_info")
public class IndexInfo extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1262251249512788L;

//	private String id;
	@Column(nullable=false,length=40)
	private String tabName;
	@Column(nullable=false,length=40)
	private String indexName;
	private String indexColumns;
	private String type;
	private String remark;
	private String createBy;
	private Timestamp createTime;
	private String updateBy;
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