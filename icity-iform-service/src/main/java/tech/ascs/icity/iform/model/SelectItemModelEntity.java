package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.SelectReferenceType;

import javax.persistence.*;

/**
 * 选择表单控件模型
 */
@Entity
@Table(name = "ifm_select_item_model")
@DiscriminatorValue("selectItemModel")
public class SelectItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="select_reference_type")
	@Enumerated(EnumType.STRING)
	private SelectReferenceType selectReferenceType;

	@Column(name="multiple")// 是否多选
	private Boolean multiple;

	@Column(name="reference_dictionary_id")// 关联字典ID
	private String referenceDictionaryId;

	@Column(name="reference_table")// 关联表
	private String referenceTable;

	@Column(name="reference_value_column")// 关联值字段（比如“ID”）
	private String referenceValueColumn;

	@JoinColumn(name="list_model_id") // 关联显示列表模型
	@ManyToOne(cascade = CascadeType.ALL)
	private ListModelEntity listModel;

	public SelectReferenceType getSelectReferenceType() {
		return selectReferenceType;
	}

	public void setSelectReferenceType(SelectReferenceType selectReferenceType) {
		this.selectReferenceType = selectReferenceType;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public String getReferenceDictionaryId() {
		return referenceDictionaryId;
	}

	public void setReferenceDictionaryId(String referenceDictionaryId) {
		this.referenceDictionaryId = referenceDictionaryId;
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

	public ListModelEntity getListModel() {
		return listModel;
	}

	public void setListModel(ListModelEntity listModel) {
		this.listModel = listModel;
	}
}