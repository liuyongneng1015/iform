package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.ReferenceType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 子表单控件模型
 */
@Entity
@Table(name = "ifm_sub_form_item_model")
@DiscriminatorValue("subFormItemModel")
public class SubFormItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@JoinColumn(name="legend") // 头部标签
	private String legend;
	@JoinColumn(name="rowCount") // 控件行数
	private Integer rowCount;
	@JoinColumn(name="showHead") // 是否显示表头
	private Boolean showHead;

	@JoinColumn(name="table_name") // 子表名
	private String tableName;

	@Column(name="reference_type")
	@Enumerated(EnumType.STRING)
	private ReferenceType referenceType = ReferenceType.OneToMany;//主表对子表一对多

	/** 组件子项（由组和字段构成） */
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parentItem")
	private List<SubFormRowItemModelEntity> items = new ArrayList<SubFormRowItemModelEntity>();


	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public Boolean getShowHead() {
		return showHead;
	}

	public void setShowHead(Boolean showHead) {
		this.showHead = showHead;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public List<SubFormRowItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<SubFormRowItemModelEntity> items) {
		this.items = items;
	}
}