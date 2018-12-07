package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.IdEntity;

public class ItemInstance extends IdEntity {

	@ApiModelProperty(value = "字段值（类型取决于字段模型中的“dataType”）", position = 1)
	private Object value;

	@ApiModelProperty(value = "显示值（通常只有Select才需要用）", position = 2)
	private String displayValue;

	@ApiModelProperty(value = "是否可见", position = 3)
	private boolean visible = true;

	@ApiModelProperty(value = "是否只读", position = 4)
	private boolean readonly = false;

	@ApiModelProperty(value = "对应数据模型字段id", position = 5)
	private String columnModelId;

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
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

	public String getColumnModelId() {
		return columnModelId;
	}

	public void setColumnModelId(String columnModelId) {
		this.columnModelId = columnModelId;
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
