package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import tech.ascs.icity.iform.api.model.ColumnType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 数据字段模型
 */
@Entity
@Table(name = "ifm_column_model")
public class ColumnModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="data_model_id")
	private DataModelEntity dataModel;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "fromColumn")
	private List<ColumnReferenceEntity> columnReferences = new ArrayList<ColumnReferenceEntity>();

	@Column(name="column_name")
	private String columnName;

	@Column(name="description")
	private String description;

	@JoinColumn(name="data_type")
	@Enumerated(EnumType.STRING)
    private ColumnType dataType;

	@Column(name="length")
	private Integer length = 0;

	@Column(name = "precision_")
    private Integer precision = 0;

	@Column(name = "scale_")
    private Integer scale = 0;

	@Column(name="not_null")
	private Boolean notNull = false;

	@Column(name = "is_key")
	private Boolean key = false;

	@Column(name="default_value")
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
