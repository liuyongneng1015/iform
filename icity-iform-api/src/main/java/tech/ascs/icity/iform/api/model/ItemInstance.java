package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.IdEntity;

public class ItemInstance extends IdEntity {

	@ApiModelProperty(value = "控件类型", position = 1)
	private ItemType type;

	@ApiModelProperty(value = "系统控件类型", position = 1)
	private SystemItemType systemItemType;

	@ApiModelProperty(value = "字段值（类型取决于字段模型中的“dataType”）", position = 1)
	private Object value;

	@ApiModelProperty(value = "显示值（通常只有Select才需要用）", position = 2)
	private Object displayValue;

	@ApiModelProperty(value = "显示值对象（通常只有Select才需要用）", position = 2)
	private Object displayObject;

	@ApiModelProperty(value = "是否可见", position = 3)
	private Boolean visible;

	@ApiModelProperty(value = "可填", position = 4)
	private Boolean canFill;

	@ApiModelProperty(value = "必填", position = 4)
	private Boolean required;

	@ApiModelProperty(value = "对应数据模型字段id", position = 5)
	private String columnModelId;

	@ApiModelProperty(value = "对应数据模型字段名称", position = 5)
	private String columnModelName;

	@ApiModelProperty(value = "前端格式", position = 6)
	private String props;

	@ApiModelProperty(value = "控件名称", position = 6)
	private String itemName;

	@ApiModelProperty(value = "流程实例ID", position = 4)
	private String processInstanceId;

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public SystemItemType getSystemItemType() {
		return systemItemType;
	}

	public void setSystemItemType(SystemItemType systemItemType) {
		this.systemItemType = systemItemType;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(Object displayValue) {
		this.displayValue = displayValue;
	}

	public Object getDisplayObject() {
		return displayObject;
	}

	public void setDisplayObject(Object displayObject) {
		this.displayObject = displayObject;
	}

	public Boolean isVisible() {
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

	public String getColumnModelId() {
		return columnModelId;
	}

	public void setColumnModelId(String columnModelId) {
		this.columnModelId = columnModelId;
	}

	public String getColumnModelName() {
		return columnModelName;
	}

	public void setColumnModelName(String columnModelName) {
		this.columnModelName = columnModelName;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@ApiModelProperty(value = "字段模型ID（uuid）", position = 0)
	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
