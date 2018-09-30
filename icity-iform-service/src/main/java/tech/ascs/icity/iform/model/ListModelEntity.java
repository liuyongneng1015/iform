package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 列表模型
 */
@Entity
@Table(name = "ifm_list_model")
public class ListModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String description;

	private boolean multiSelect = false;

	@ManyToOne
	@JoinColumn(name="master_form")
	private FormModelEntity masterForm;

	@ManyToMany
	@JoinTable(
		name = "ifm_list_form",
		joinColumns = @JoinColumn( name="list_model"),
		inverseJoinColumns = @JoinColumn( name="form_model")
	)
	private List<FormModelEntity> slaverForms = new ArrayList<FormModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel")
	private List<ListSortItem> sortItems = new ArrayList<ListSortItem>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel")
	private List<ListFunction> functions = new ArrayList<ListFunction>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel")
	private List<ListSearchItem> searchItems = new ArrayList<ListSearchItem>();

	@ManyToMany
	@JoinTable(
		name = "ifm_list_display_item",
		joinColumns = @JoinColumn(name="list_id"),
		inverseJoinColumns = @JoinColumn(name="item_id")
	)
	private List<ItemModelEntity> displayItems = new ArrayList<ItemModelEntity>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isMultiSelect() {
		return multiSelect;
	}

	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public FormModelEntity getMasterForm() {
		return masterForm;
	}

	public void setMasterForm(FormModelEntity masterForm) {
		this.masterForm = masterForm;
	}

	public List<FormModelEntity> getSlaverForms() {
		return slaverForms;
	}

	public void setSlaverForms(List<FormModelEntity> slaverForms) {
		this.slaverForms = slaverForms;
	}

	public List<ListSortItem> getSortItems() {
		return sortItems;
	}

	public void setSortItems(List<ListSortItem> sortItems) {
		this.sortItems = sortItems;
	}

	public List<ListFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<ListFunction> functions) {
		this.functions = functions;
	}

	public List<ListSearchItem> getSearchItems() {
		return searchItems;
	}

	public void setSearchItems(List<ListSearchItem> searchItems) {
		this.searchItems = searchItems;
	}

	public List<ItemModelEntity> getDisplayItems() {
		return displayItems;
	}

	public void setDisplayItems(List<ItemModelEntity> displayItems) {
		this.displayItems = displayItems;
	}
}