package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("表单模型权限")
public class ItemPermissionInfoModel extends NameEntity {

	@ApiModelProperty(value = "控件模型", position = 4)
	private ItemModel itemModel;

	@ApiModelProperty(value = "可见", position = 4)
	private Boolean visible = true;

	@ApiModelProperty(value = "可填", position = 5)
	private Boolean canFill = true;

	@ApiModelProperty(value = "必填", position = 6)
	private Boolean required = false;

	@ApiModelProperty(value ="显示时机(新增：add, 编辑：update)", position = 6)
	private DisplayTimingType displayTiming;

	public ItemModel getItemModel() {
		return itemModel;
	}

	public void setItemModel(ItemModel itemModel) {
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

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
