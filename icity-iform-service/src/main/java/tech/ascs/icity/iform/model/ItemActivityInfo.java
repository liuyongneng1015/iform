package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 表单控件与流程环节绑定
 */
@Entity
@Table(name = "ifm_item_activity")
public class ItemActivityInfo extends JPAEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	private String activityId;

	private String activityName;

	private boolean visible = true;

	private boolean readonly = false;

	public ItemModelEntity getItemModel() {
		return itemModel;
	}

	public void setItemModel(ItemModelEntity itemModel) {
		this.itemModel = itemModel;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
 
}