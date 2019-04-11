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

	@ApiModelProperty(value = "控件模型表名", position = 4)
	private String tableName;

	@ApiModelProperty(value = "控件模型字段名", position = 4)
	private String columnName;

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

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
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
