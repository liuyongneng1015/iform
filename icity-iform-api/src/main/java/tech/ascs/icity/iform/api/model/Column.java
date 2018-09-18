package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("列字段元数据")
public class Column {
	@ApiModelProperty(value = "字段名",required=true)
	private String colName;
	@ApiModelProperty(value = "字段值",required=true)
	private Object value;
	@ApiModelProperty(value = "数据类型")
	private String dataType;
	@ApiModelProperty(value = "数据格式")
	private String formatter;
	
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getFormatter() {
		return formatter;
	}
	public void setFormatter(String formatter) {
		this.formatter = formatter;
	}
	
	
}
