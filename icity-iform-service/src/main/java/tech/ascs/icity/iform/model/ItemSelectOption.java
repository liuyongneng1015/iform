package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.*;

import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 表单Select控件选项
 */
@Entity
@Table(name = "ifm_item_select_option")
public class ItemSelectOption  extends BaseEntity{

	private static final long serialVersionUID = 52721L;

	@ManyToOne(cascade = {CascadeType.REFRESH})
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	//描叙内容比如说男，女
	private String label;

	//值比如说0，1
	private String value;

	//是否默认
	private Boolean defaultFlag = false;

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

	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}
}