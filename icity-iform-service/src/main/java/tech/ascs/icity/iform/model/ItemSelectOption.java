package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 表单Select控件选项
 */
@Entity
@Table(name = "ifm_item_select_option")
public class ItemSelectOption extends JPAEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	private String label;

	private String value;

	public ItemModelEntity getItemModel() {
		return itemModel;
	}

	public void setItemModel(ItemModelEntity itemModel) {
		this.itemModel = itemModel;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}