package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.ControlType;
import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.api.model.SelectMode;

import javax.persistence.*;

/**
 * 关联表单控件模型
 */
@Entity
@Table(name = "ifm_reference_item_model")
@DiscriminatorValue("referenceItemModel")
public class ReferenceItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="reference_type")
	@Enumerated(EnumType.STRING)
	private ReferenceType referenceType;

	@JoinColumn(name="select_mode")//选择方式
	@Enumerated(EnumType.STRING)
	private SelectMode selectMode;

	@Column(name="reference_table")// 关联表
	private String referenceTable;

	@Column(name="reference_value_column")// 关联值字段（比如“ID”）
	private String referenceValueColumn;

	@Column(name="control_type")//控件类型选择框还是列表
	@Enumerated(EnumType.STRING)
	private ControlType controlType;

	@JoinColumn(name="list_model_id") // 关联显示列表模型
	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private ListModelEntity referenceList;

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public SelectMode getSelectMode() {
		return selectMode;
	}

	public void setSelectMode(SelectMode selectMode) {
		this.selectMode = selectMode;
	}

	public String getReferenceTable() {
		return referenceTable;
	}

	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	public String getReferenceValueColumn() {
		return referenceValueColumn;
	}

	public void setReferenceValueColumn(String referenceValueColumn) {
		this.referenceValueColumn = referenceValueColumn;
	}

	public ControlType getControlType() {
		return controlType;
	}

	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}

	public ListModelEntity getReferenceList() {
		return referenceList;
	}

	public void setReferenceList(ListModelEntity referenceList) {
		this.referenceList = referenceList;
	}
}