package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 表单模型
 */
@Entity
@Table(name = "ifm_form_model")
public class FormModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String description;

	@ManyToMany
	@JoinTable(
		name = "ifm_form_data_bind",
		joinColumns = @JoinColumn( name="form_model"),
		inverseJoinColumns = @JoinColumn( name="data_model")
	)
	private List<DataModelEntity> dataModels = new ArrayList<DataModelEntity>();

	@Embedded
	private FormProcessInfo process;
 
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "formModel")
	private List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();

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
}