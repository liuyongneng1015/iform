package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.DictionaryValueType;
import tech.ascs.icity.iform.api.model.SelectReferenceType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

	@Column(name="reference_dictionary_id")// 数据字典分类
	private String referenceDictionaryId;

	@Column(name="reference_dictionary_item_id")// 关联字典数据范围
	private String referenceDictionaryItemId;

	@Column(name="reference_table")// 关联表
	private String referenceTable;

	@Column(name="reference_value_column")// 关联值字段（比如“ID”）
	private String referenceValueColumn;

	@Column(name="default_reference_value",columnDefinition = "text")// 默认关联值逗号隔开
	private String defaultReferenceValue;

	@JoinColumn(name="list_model_id") // 关联显示列表模型
	@ManyToOne(cascade = CascadeType.ALL)
	private ListModelEntity referenceList;


	//数据字典值类型
	@Column(name="dictionary_value_type")
	@Enumerated(EnumType.STRING)
	private DictionaryValueType dictionaryValueType;


	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="parent_select_id")// 关联字典联动目标
	private SelectItemModelEntity parentItem;

	/** 被关联字典联动目标 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parentItem")
	private List<SelectItemModelEntity> items = new ArrayList<SelectItemModelEntity>();

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

	public String getReferenceDictionaryItemId() {
		return referenceDictionaryItemId;
	}

	public void setReferenceDictionaryItemId(String referenceDictionaryItemId) {
		this.referenceDictionaryItemId = referenceDictionaryItemId;
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

	public String getDefaultReferenceValue() {
		return defaultReferenceValue;
	}

	public void setDefaultReferenceValue(String defaultReferenceValue) {
		this.defaultReferenceValue = defaultReferenceValue;
	}

	public ListModelEntity getReferenceList() {
		return referenceList;
	}

	public void setReferenceList(ListModelEntity referenceList) {
		this.referenceList = referenceList;
	}

	public DictionaryValueType getDictionaryValueType() {
		return dictionaryValueType;
	}

	public void setDictionaryValueType(DictionaryValueType dictionaryValueType) {
		this.dictionaryValueType = dictionaryValueType;
	}

	public SelectItemModelEntity getParentItem() {
		return parentItem;
	}

	public void setParentItem(SelectItemModelEntity parentItem) {
		this.parentItem = parentItem;
	}

	public List<SelectItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<SelectItemModelEntity> items) {
		this.items = items;
	}
}