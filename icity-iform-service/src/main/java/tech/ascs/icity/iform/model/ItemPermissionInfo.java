package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.DisplayTimingType;
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

	@ManyToOne(cascade = {CascadeType.REFRESH })
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	//可见
	private Boolean visible = true;

	//可填
	private Boolean canFill;

	//必填
	private Boolean required;

	//显示时机 若为空标识所有时机都显示
	@JoinColumn(name="display_timing_type")
	@Enumerated(EnumType.STRING)
	private DisplayTimingType displayTiming;

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

	public DisplayTimingType getDisplayTiming() {
		return displayTiming;
	}

	public void setDisplayTiming(DisplayTimingType displayTiming) {
		this.displayTiming = displayTiming;
	}
}