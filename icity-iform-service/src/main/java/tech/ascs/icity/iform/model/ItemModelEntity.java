package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.DiscriminatorOptions;
import tech.ascs.icity.iform.api.model.ItemType;
import tech.ascs.icity.iform.api.model.SystemItemType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 表单控件模型
 */
@Entity
@Table(name = "ifm_item_model")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force=true)
@DiscriminatorValue(value = "baseItemModel")
public class ItemModelEntity extends  BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="column_id")
	private ColumnModelEntity columnModel;

	@JoinColumn(name="type")
	@Enumerated(EnumType.STRING)
	private ItemType type;

	@JoinColumn(name="props")
	private String props;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "itemModel")
	private List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "itemModel")
	private List<ItemSelectOption> options = new ArrayList<ItemSelectOption>();

	@JoinColumn(name="system_item_type")//系统控件类型
	@Enumerated(EnumType.STRING)
	private SystemItemType systemItemType;

	public FormModelEntity getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModelEntity formModel) {
		this.formModel = formModel;
	}

	public ColumnModelEntity getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(ColumnModelEntity columnModel) {
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

	public SystemItemType getSystemItemType() {
		return systemItemType;
	}

	public void setSystemItemType(SystemItemType systemItemType) {
		this.systemItemType = systemItemType;
	}
}