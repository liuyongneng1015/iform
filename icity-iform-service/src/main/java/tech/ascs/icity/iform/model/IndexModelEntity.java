package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import tech.ascs.icity.iform.api.model.IndexType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 索引信息
 */
@Entity
@Table(name = "ifm_index_info")
public class IndexModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.REFRESH })
	@JoinColumn(name="data_model")
	private DataModelEntity dataModel;

	@ManyToMany(cascade = {CascadeType.REFRESH })
	@JoinTable(
		name = "ifm_index_column",
		joinColumns = @JoinColumn( name="index_info"),
		inverseJoinColumns = @JoinColumn( name="column_model")
	)
	private List<ColumnModelEntity> columns = new ArrayList<ColumnModelEntity>();

	@Enumerated(EnumType.STRING)
	private IndexType indexType;

	private String description;

	public DataModelEntity getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModelEntity dataModel) {
		this.dataModel = dataModel;
	}

	public List<ColumnModelEntity> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModelEntity> columns) {
		this.columns = columns;
	}

	public IndexType getIndexType() {
		return indexType;
	}

	public void setIndexType(IndexType indexType) {
		this.indexType = indexType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}