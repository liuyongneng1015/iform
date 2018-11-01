package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import tech.ascs.icity.iform.api.model.ColumnType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 数据字段模型
 */
@Entity
@Table(name = "ifm_column_model")
public class ColumnModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="data_model_id")
	private DataModelEntity dataModel;

	@OneToMany(mappedBy = "fromColumn")
	private List<ColumnReferenceEntity> columnReferences = new ArrayList<ColumnReferenceEntity>();

	private String columnName;

	private String description;

	@Enumerated(EnumType.STRING)
    private ColumnType dataType;

	private Integer length = 0;

	@Column(name = "precision_")
    private Integer precision = 0;

	@Column(name = "scale_")
    private Integer scale = 0;

	private Boolean notNull = false;

	@Column(name = "is_key")
	private Boolean key = false;

	private String defaultValue;

	public DataModelEntity getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModelEntity dataModel) {
		this.dataModel = dataModel;
	}

    public List<ColumnReferenceEntity> getColumnReferences() {
		return columnReferences;
	}

	public void setColumnReferences(List<ColumnReferenceEntity> columnReferences) {
		this.columnReferences = columnReferences;
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
