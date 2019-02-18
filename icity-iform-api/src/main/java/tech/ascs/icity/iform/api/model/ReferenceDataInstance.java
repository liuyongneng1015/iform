package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.IdEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("关联数据实例DataInstance")
public class ReferenceDataInstance extends IdEntity {
	@ApiModelProperty(value = "控件的数据", position = 3)
	private Object value;

	@ApiModelProperty(value = "显示数据", position = 4)
	private String displayValue;

	@ApiModelProperty(value = " 关联表单模型", position = 5)
	private String referenceTable;

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

	public String getReferenceTable() {
		return referenceTable;
	}

	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	@ApiModelProperty(value = "表单实例ID", position = 0)
	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
