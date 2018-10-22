package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 数据模型
 */
@Entity
@Table(name = "ifm_data_model")
public class DataModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String tableName;

	private String description;

	@Enumerated(EnumType.STRING)
	private DataModelType modelType;

	@ManyToOne
	@JoinColumn(name = "master_model")
	private DataModelEntity masterModel;

	@OneToMany(mappedBy = "masterModel")
	private List<DataModelEntity> slaverModels = new ArrayList<DataModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataModel")
    private List<ColumnModelEntity> columns = new ArrayList<ColumnModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataModel")
    private List<IndexModelEntity> indexes = new ArrayList<IndexModelEntity>();

	private Boolean synchronized_;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DataModelType getModelType() {
		return modelType;
	}

	public void setModelType(DataModelType modelType) {
		this.modelType = modelType;
	}

	public DataModelEntity getMasterModel() {
		return masterModel;
	}

	public void setMasterModel(DataModelEntity masterModel) {
		this.masterModel = masterModel;
	}

	public List<DataModelEntity> getSlaverModels() {
		return slaverModels;
	}

	public void setSlaverModels(List<DataModelEntity> slaverModels) {
		this.slaverModels = slaverModels;
	}

	public List<ColumnModelEntity> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModelEntity> columns) {
		this.columns = columns;
	}

	public List<IndexModelEntity> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<IndexModelEntity> indexes) {
		this.indexes = indexes;
	}

	public Boolean getSynchronized() {
		return synchronized_;
	}

	public void setSynchronized(Boolean synchronized_) {
		this.synchronized_ = synchronized_;
	}

}