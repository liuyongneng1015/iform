package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.Set;

@ApiModel("数据字段模型")
public class ColumnModel extends NameEntity {

	@ApiModelProperty(value = "数据模型", hidden = true)
	@JsonBackReference
	private DataModel dataModel;

    @ApiModelProperty(value = "字段名", position = 3)
    private String columnName;

	@ApiModelProperty(value = "描述", position = 3)
	private String description;

	@ApiModelProperty(value = "字段类型", position = 4)
    private ColumnType dataType;

    @ApiModelProperty(value = "字段长度", position = 5)
	private Integer length;

    @ApiModelProperty(value = "精度", position = 6)
    private Integer precision;

    @ApiModelProperty(value = "小数位", position = 7)
    private Integer scale;

    @ApiModelProperty(value = "不允许为空", position = 8)
	private Boolean notNull;

    @ApiModelProperty(value = "是否主键", position = 9)
	private Boolean key;

	@ApiModelProperty(value = " 字段默认值", position = 9)
    private String defaultValue;

	public DataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ColumnType getDataType() {
		return dataType;
	}

	public void setDataType(ColumnType dataType) {
		this.dataType = dataType;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Boolean getNotNull() {
		return notNull;
	}

	public void setNotNull(Boolean notNull) {
		this.notNull = notNull;
	}

    public Boolean getKey() {
		return key;
	}

	public void setKey(Boolean key) {
		this.key = key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
