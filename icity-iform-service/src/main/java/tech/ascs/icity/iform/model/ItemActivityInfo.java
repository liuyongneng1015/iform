package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.*;

import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 表单控件与流程环节绑定
 */
@Entity
@Table(name = "ifm_item_activity")
public class ItemActivityInfo extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	@Column(name="activity_id")
	private String activityId;

	@Column(name="activity_name")
	private String activityName;

	@Column(name="visible")//是否可见
	private boolean visible = true;

	@Column(name="readonly")//是否只读
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