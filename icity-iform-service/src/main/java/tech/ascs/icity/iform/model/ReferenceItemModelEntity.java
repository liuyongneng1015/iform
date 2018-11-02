package tech.ascs.icity.iform.model;

import javax.persistence.*;

/**
 * 选择表单控件模型
 */
@Entity
@Table(name = "ifm_reference_item_model")
@DiscriminatorValue("referenceItemModel")
public class ReferenceItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;
	public static enum ReferenceTableType {
		OneToOne,
		OneToMany,
		ManyToOne,
		ManyToMany
	}

	@Column(name="reference_table_type")
	@Enumerated(EnumType.STRING)
	private ReferenceTableType referenceTableType;

	@Column(name="reference_dictionary_id")// 关联字典ID
	private String referenceDictionaryId;

	@Column(name="reference_table")// 关联表
	private String referenceTable;

	@Column(name="reference_value_column")// 关联值字段（比如“ID”）
	private String referenceValueColumn;

	@Column(name="reference_list_model")// 关联显示列表模型
	private String referenceListModel;

	public ReferenceTableType getReferenceTableType() {
		return referenceTableType;
	}

	public void setReferenceTableType(ReferenceTableType referenceTableType) {
		this.referenceTableType = referenceTableType;
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

	public String getReferenceListModel() {
		return referenceListModel;
	}

	public void setReferenceListModel(String referenceListModel) {
		this.referenceListModel = referenceListModel;
	}
}