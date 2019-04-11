package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("表单模型权限集合")
public class ItemPermissionModel extends NameEntity {

	@ApiModelProperty(value = "控件模型唯一标识", position = 4)
	private String uuid;

	@ApiModelProperty(value = "前端用的控件类型key", position = 53)
	private String typeKey;

	@ApiModelProperty(value = "新增时控件权限", position = 7)
	private ItemPermissionInfoModel addPermissions ;

	@ApiModelProperty(value = "编辑时控件权限", position = 7)
	private ItemPermissionInfoModel updatePermissions;

	@ApiModelProperty(value = "查看时控件权限", position = 7)
	private ItemPermissionInfoModel checkPermissions;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTypeKey() {
		return typeKey;
	}

	public void setTypeKey(String typeKey) {
		this.typeKey = typeKey;
	}

	public ItemPermissionInfoModel getAddPermissions() {
		return addPermissions;
	}

	public void setAddPermissions(ItemPermissionInfoModel addPermissions) {
		this.addPermissions = addPermissions;
	}

	public ItemPermissionInfoModel getUpdatePermissions() {
		return updatePermissions;
	}

	public void setUpdatePermissions(ItemPermissionInfoModel updatePermissions) {
		this.updatePermissions = updatePermissions;
	}

	public ItemPermissionInfoModel getCheckPermissions() {
		return checkPermissions;
	}

	public void setCheckPermissions(ItemPermissionInfoModel checkPermissions) {
		this.checkPermissions = checkPermissions;
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
