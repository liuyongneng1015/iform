package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;

public class StepMap {
	
	private String uuid;
	@ApiModelProperty(value = "字段名称",required=true)
	private String colName;
	@ApiModelProperty(value = "是否可见",required=true)
	private String visible;
	@ApiModelProperty(value = "是否可编辑",required=true)
	private String editable;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public String getVisible() {
		return visible;
	}
	public void setVisible(String visible) {
		this.visible = visible;
	}
	public String getEditable() {
		return editable;
	}
	public void setEditable(String editable) {
		this.editable = editable;
	}

}
