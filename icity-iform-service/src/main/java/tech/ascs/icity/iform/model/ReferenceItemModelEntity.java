package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.ControlType;
import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.api.model.SelectMode;
import tech.ascs.icity.iform.api.model.SystemCreateType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 关联表单控件模型
 */
@Entity
@Table(name = "ifm_reference_item_model")
@DiscriminatorValue("referenceItemModel")
public class ReferenceItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 542421L;

	@Column(name="reference_type")
	@Enumerated(EnumType.STRING)
	private ReferenceType referenceType;

	@JoinColumn(name="select_mode")//选择方式
	@Enumerated(EnumType.STRING)
	private SelectMode selectMode;

	@Column(name="reference_form_id")// 关联表单模型id
	private String referenceFormId;

	@Column(name="reference_item_id")// 关联控件模型id
	private String referenceItemId;

	@Column(name="control_type")//控件类型选择框还是列表
	@Enumerated(EnumType.STRING)
	private ControlType controlType;

	@JoinColumn(name="list_model_id") // 关联显示列表模型
	@ManyToOne(cascade = {CascadeType.MERGE})
	private ListModelEntity referenceList;

	@Column(name="item_model_ids", length = 2048) // 关联数据标识：控件id集合
	private String itemModelIds;

	@ManyToOne(cascade = {CascadeType.PERSIST})
	@JoinColumn(name="parent_reference_id")// 父类控件
	private ReferenceItemModelEntity parentItem;

	/** 子类控件 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parentItem")
	private List<ReferenceItemModelEntity> items = new ArrayList<ReferenceItemModelEntity>();

	@Column(name="item_uuids", length = 2048) // 关联数据标识：控件对应的唯一标识
	private String itemUuids;

	@Column(name="create_foreign_key") // 是否创建外键
	private Boolean createForeignKey = true;

	@Column(name="create_type")//创建类型
	@Enumerated(EnumType.STRING)
	private SystemCreateType createType = SystemCreateType.Normal;

	@Column(name="reference_table_name")//关联数据建模表名称
	private String referenceTableName;

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

	public String getReferenceFormId() {
		return referenceFormId;
	}

	public void setReferenceFormId(String referenceFormId) {
		this.referenceFormId = referenceFormId;
	}

	public String getReferenceItemId() {
		return referenceItemId;
	}

	public void setReferenceItemId(String referenceItemId) {
		this.referenceItemId = referenceItemId;
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

	public String getItemModelIds() {
		return itemModelIds;
	}

	public void setItemModelIds(String itemModelIds) {
		this.itemModelIds = itemModelIds;
	}

	public ReferenceItemModelEntity getParentItem() {
		return parentItem;
	}

	public void setParentItem(ReferenceItemModelEntity parentItem) {
		this.parentItem = parentItem;
	}

	public List<ReferenceItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<ReferenceItemModelEntity> items) {
		this.items = items;
	}

	public String getItemUuids() {
		return itemUuids;
	}

	public void setItemUuids(String itemUuids) {
		this.itemUuids = itemUuids;
	}

	public Boolean getCreateForeignKey() {
		return createForeignKey;
	}

	public void setCreateForeignKey(Boolean createForeignKey) {
		this.createForeignKey = createForeignKey;
	}

	public SystemCreateType getCreateType() {
		return createType;
	}

	public void setCreateType(SystemCreateType createType) {
		this.createType = createType;
	}

	public String getReferenceTableName() {
		return referenceTableName;
	}

	public void setReferenceTableName(String referenceTableName) {
		this.referenceTableName = referenceTableName;
	}
}