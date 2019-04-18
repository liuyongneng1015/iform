package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import tech.ascs.icity.iform.api.model.FormType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 表单模型
 */
@Entity
@Table(name = "ifm_form_model")
public class FormModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String description;

	@ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name = "ifm_form_data_bind",
		joinColumns = @JoinColumn( name="form_model"),
		inverseJoinColumns = @JoinColumn( name="data_model")
	)
	private List<DataModelEntity> dataModels = new ArrayList<DataModelEntity>();

	@Column(name = "application_id")
	private String applicationId;//应用id

	@Embedded
	private FormProcessInfo process;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "formModel")
	private List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "formModel")
	private List<FormSubmitCheckInfo> submitChecks = new ArrayList<FormSubmitCheckInfo>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "formModel")
	private List<ListFunction> functions = new ArrayList<>();

	@Column(name="item_model_ids", length = 2048) // 数据标识：控件id集合
	private String itemModelIds;

	@Column(name="item_uuids", length = 2048) // 数据标识：控件对应的唯一标识
	private String itemUuids;

	@Column(name="qrcode_item_model_ids", length = 2048) // 二维码数据标识：控件id集合
	private String qrCodeItemModelIds;

	@Column(name="qrcode_item_uuids", length = 2048) // 二维码数据标识：控件对应的唯一标识
	private String qrCodeItemUuids;

	@JoinColumn(name="type")
	@Enumerated(EnumType.STRING)
	private FormType type = FormType.General;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<DataModelEntity> getDataModels() {
		return dataModels;
	}

	public void setDataModels(List<DataModelEntity> dataModels) {
		this.dataModels = dataModels;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public FormProcessInfo getProcess() {
		return process;
	}

	public void setProcess(FormProcessInfo process) {
		this.process = process;
	}

	public List<ItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<ItemModelEntity> items) {
		this.items = items;
	}

	public List<FormSubmitCheckInfo> getSubmitChecks() {
		return submitChecks;
	}

	public void setSubmitChecks(List<FormSubmitCheckInfo> submitChecks) {
		this.submitChecks = submitChecks;
	}

	public List<ListFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<ListFunction> functions) {
		this.functions = functions;
	}

	public String getItemModelIds() {
		return itemModelIds;
	}

	public void setItemModelIds(String itemModelIds) {
		this.itemModelIds = itemModelIds;
	}

	public String getItemUuids() {
		return itemUuids;
	}

	public void setItemUuids(String itemUuids) {
		this.itemUuids = itemUuids;
	}

	public String getQrCodeItemModelIds() {
		return qrCodeItemModelIds;
	}

	public void setQrCodeItemModelIds(String qrCodeItemModelIds) {
		this.qrCodeItemModelIds = qrCodeItemModelIds;
	}

	public String getQrCodeItemUuids() {
		return qrCodeItemUuids;
	}

	public void setQrCodeItemUuids(String qrCodeItemUuids) {
		this.qrCodeItemUuids = qrCodeItemUuids;
	}

	public FormType getType() {
		return type;
	}

	public void setType(FormType type) {
		this.type = type;
	}
}