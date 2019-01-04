package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 控件权限
 */
@Entity
@Table(name = "ifm_item_permissions")
public class ItemPermissionInfo extends JPAEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	//可见
	private Boolean visible = true;

	//可填
	private Boolean canFill = true;

	//必填
	private Boolean required = false;

	//显示时机(新增：add, 编辑：update)
	private String displayTiming;


	public FormModelEntity getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModelEntity formModel) {
		this.formModel = formModel;
	}

	public ItemModelEntity getItemModel() {
		return itemModel;
	}

	public void setItemModel(ItemModelEntity itemModel) {
		this.itemModel = itemModel;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getCanFill() {
		return canFill;
	}

	public void setCanFill(Boolean canFill) {
		this.canFill = canFill;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getDisplayTiming() {
		return displayTiming;
	}

	public void setDisplayTiming(String displayTiming) {
		this.displayTiming = displayTiming;
	}
}