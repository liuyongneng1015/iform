package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("数据字段模型")
public class ColumnModel extends NameEntity {

	@ApiModelProperty(value = "数据模型", hidden = true)
	private DataModel dataModel;

    @ApiModelProperty(value = "字段名", position = 2)
    private String columnName;

	@ApiModelProperty(value = "描述", position = 3)
	private String description;

	@ApiModelProperty(value = "字段类型", position = 4)
    private ColumnType dataType = ColumnType.String;

    @ApiModelProperty(value = "字段长度", position = 5)
	private Integer length = 255;

    @ApiModelProperty(value = "精度", position = 6)
    private Integer precision;

    @ApiModelProperty(value = "小数位", position = 7)
    private Integer scale;

    @ApiModelProperty(value = "不允许为空", position = 8)
	private Boolean notNull = false;

    @ApiModelProperty(value = "是否主键", position = 9)
	private Boolean key;

	@ApiModelProperty(value = " 字段默认值", position = 10)
    private String defaultValue;

	@ApiModelProperty(value = "是否关联:true关联，false未关联", position = 12)
	private Boolean referenceItem;

	@ApiModelProperty(value = "关联表", position = 11)
	private List<ReferenceModel> referenceTables = new ArrayList<ReferenceModel>();

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
		if(dataType == null){
			dataType = ColumnType.String;
		}
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

	public List<ReferenceModel> getReferenceTables() {
		return referenceTables;
	}

	public void setReferenceTables(List<ReferenceModel> referenceTables) {
		this.referenceTables = referenceTables;
	}

	public Boolean getReferenceItem() {
		return referenceItem;
	}

	public void setReferenceItem(Boolean referenceItem) {
		this.referenceItem = referenceItem;
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
