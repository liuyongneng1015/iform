package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("控件绑定流程模型")
public class ItemActivityInfoModel extends NameEntity {

	@ApiModelProperty(value = "控件模型", position = 4)
	private ItemModel itemModel;

	@ApiModelProperty(value = "环节id", position = 4)
	private String activityId;

	@ApiModelProperty(value = "环节名称", position = 4)
	private String activityName;

	@ApiModelProperty(value = "可见", position = 4)
	private boolean visible = true;

	@ApiModelProperty(value = "是否只读(false标识可以编辑，true不可编辑）", position = 4)
	private boolean readonly = false;

	public ItemModel getItemModel() {
		return itemModel;
	}

	public void setItemModel(ItemModel itemModel) {
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

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
