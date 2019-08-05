package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 数据模型
 */
@Entity
@Table(name = "ifm_data_model")
public class DataModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "prefix")
	private String prefix;

	@Column(name = "table_name")
	private String tableName;

	@Column(name = "description")
	private String description;

	@JoinColumn(name="model_type")
	@Enumerated(EnumType.STRING)
	private DataModelType modelType;

	@Column(name = "application_id")
	private String applicationId;//应用id

	@ManyToOne(cascade = {CascadeType.REFRESH })
	@JoinColumn(name = "master_model")
	private DataModelEntity masterModel;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "masterModel")
	private List<DataModelEntity> slaverModels = new ArrayList<DataModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataModel")
    private List<ColumnModelEntity> columns = new ArrayList<ColumnModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataModel")
    private List<IndexModelEntity> indexes = new ArrayList<IndexModelEntity>();

	@Column(name="synchronized_")
	private Boolean synchronize = false;

	@Transient//关联数据模型不存数据库
	private List<DataModelEntity> referencesDataModel = new ArrayList<DataModelEntity>();

	public String getPrefix() {
		if(prefix != null){
			prefix = prefix.toLowerCase();
		}
		return prefix;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getTableName() {
		if(tableName != null){
			tableName = tableName.toLowerCase();
		}
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

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
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

	public Boolean getSynchronize() {
		if(synchronize == null){
			return false;
		}
		return synchronize;
	}

	public void setSynchronize(Boolean synchronize) {
		this.synchronize = synchronize;
	}

	public List<DataModelEntity> getReferencesDataModel() {
		return referencesDataModel;
	}

	public void setReferencesDataModel(List<DataModelEntity> referencesDataModel) {
		this.referencesDataModel = referencesDataModel;
	}
}
