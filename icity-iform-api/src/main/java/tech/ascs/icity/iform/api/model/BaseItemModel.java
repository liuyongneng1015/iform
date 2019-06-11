package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.List;

@ApiModel("表单基础控件模型")
public class BaseItemModel extends NameEntity {

	@ApiModelProperty(value = "数据字段模型", position = 5)
	private ColumnModelInfo columnModel;

	@ApiModelProperty(value = "控件类型", position = 3)
	private ItemType type;

	@ApiModelProperty(value = "前端个性化属性（直接存json字符串，后端不做处理）", position = 4)
	private String props;

	@ApiModelProperty(value = "隐藏条件", position = 8)
	private String hiddenCondition;

	@ApiModelProperty(value="系统控件类型", position = 29)
	private SystemItemType systemItemType;

	@ApiModelProperty(value = "流程环节配置", position = 6)
	private List<ActivityInfo> activities;

	@ApiModelProperty(value = "选项配置", position = 18)
	private List<Option> options;

	@ApiModelProperty(value="新增控件权限",position = 31)
	private List<ItemPermissionModel>  permissions;

	public ColumnModelInfo getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(ColumnModelInfo columnModel) {
		this.columnModel = columnModel;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public String getHiddenCondition() {
		return hiddenCondition;
	}

	public void setHiddenCondition(String hiddenCondition) {
		this.hiddenCondition = hiddenCondition;
	}

	public SystemItemType getSystemItemType() {
		return systemItemType;
	}

	public void setSystemItemType(SystemItemType systemItemType) {
		this.systemItemType = systemItemType;
	}

	public List<ActivityInfo> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityInfo> activities) {
		this.activities = activities;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public List<ItemPermissionModel> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<ItemPermissionModel> permissions) {
		this.permissions = permissions;
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
