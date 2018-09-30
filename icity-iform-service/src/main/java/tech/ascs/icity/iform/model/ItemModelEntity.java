package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import tech.ascs.icity.iform.api.model.ItemType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 表单控件模型
 */
@Entity
@Table(name = "ifm_item_model")
public class ItemModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	@OneToOne
	@JoinColumn(name="column_id")
	private ColumnData columnModel;

	@Enumerated(EnumType.STRING)
	private ItemType type;

	private String props;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "itemModel")
	private List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "itemModel")
	private List<ItemSelectOption> options = new ArrayList<ItemSelectOption>();

	public FormModelEntity getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModelEntity formModel) {
		this.formModel = formModel;
	}

	public ColumnData getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(ColumnData columnModel) {
		this.columnModel = columnModel;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public List<ItemActivityInfo> getActivities() {
		return activities;
	}

	public void setActivities(List<ItemActivityInfo> activities) {
		this.activities = activities;
	}

	public List<ItemSelectOption> getOptions() {
		return options;
	}

	public void setOptions(List<ItemSelectOption> options) {
		this.options = options;
	}
}