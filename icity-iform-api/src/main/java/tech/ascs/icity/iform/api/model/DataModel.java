package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("数据模型")
public class DataModel extends NameEntity {

    @ApiModelProperty(value = "数据库表名", position = 2)
	private String tableName;

	@ApiModelProperty(value = "描述", position = 3)
	private String description;

	@ApiModelProperty(value = "表类型（单表、主表、从表）", position = 4)
	private DataModelType modelType;
    
    @ApiModelProperty(value = "从表对应的主表(当表类型为从表时需要填)", position = 5)
	private DataModelInfo masterModel;

    @ApiModelProperty(value = "数据字段模型列表", position = 6)
    @JsonManagedReference
    private List<ColumnModel> columns = new ArrayList<ColumnModel>();

    @ApiModelProperty(value = "索引列表", position = 7)
    @JsonManagedReference
    private List<IndexModel> indexes = new ArrayList<IndexModel>();

    @ApiModelProperty(value = "是否已同步", position = 8)
    private boolean synchronized_;

    @ApiModelProperty(value = "中文名", position = 1)
	@Override
	public String getName() {
		return super.getName();
	}

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

	public DataModelInfo getMasterModel() {
		return masterModel;
	}

	public void setMasterModel(DataModelInfo masterModel) {
		this.masterModel = masterModel;
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModel> columns) {
		this.columns = columns;
	}

	public List<IndexModel> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<IndexModel> indexes) {
		this.indexes = indexes;
	}

    @ApiModelProperty(value = "是否已同步", position = 8)
	public boolean isSynchronized() {
		return synchronized_;
	}

	public void setSynchronized(boolean synchronized_) {
		this.synchronized_ = synchronized_;
	}

}
