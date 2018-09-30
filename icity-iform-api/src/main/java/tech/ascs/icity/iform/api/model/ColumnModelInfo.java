package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("数据字段模型摘要信息")
public class ColumnModelInfo extends NameEntity {

    @ApiModelProperty(value = "数据库表名", position = 2)
	private String tableName;

    @ApiModelProperty(value = "数据库字段名", position = 3)
    private String columnName;

    @ApiModelProperty(value = "数据库字段类型", position = 4)
    private String dataType;

    @ApiModelProperty(value = " 字段默认值", position = 5)
    private String defaultValue;

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

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@JsonIgnore
	public String getTabName() {
		return getTableName();
	}

	public void setTabName(String tabName) {
		setTableName(tabName);
	}

	@JsonIgnore
	public String getColName() {
		return getColumnName();
	}

	public void setColName(String colName) {
		setColumnName(colName);
	}

	@JsonIgnore
	public String getColNameDesc() {
		return getName();
	}

	public void setColNameDesc(String colNameDesc) {
		setName(colNameDesc);
	}

	@JsonIgnore
	public String getType() {
		return getDataType();
	}

	public void setType(String type) {
		setDataType(type);
	}
}
