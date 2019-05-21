package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;


/**
 * 字典建模实体
 */
@Entity
@Table(name = "ifm_dictionary_model")
public class DictionaryModelEntity extends BaseEntity {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 数据表
	 */
    @Column(name = "table_name")
	private String tableName;

	@Column(name = "application_id")
	private String applicationId;//应用id

	/**
	 * 排序号
	 */
	@Column(name = "order_no")
	private Integer orderNo;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
}
