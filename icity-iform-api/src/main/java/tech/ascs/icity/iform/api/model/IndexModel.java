package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import tech.ascs.icity.model.NameEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * 索引信息
 */
@ApiModel("索引信息表")
public class IndexModel extends NameEntity {

	@ApiModelProperty(value = "数据模型", hidden = true)
	@JsonBackReference
	private DataModel dataModel;

	@ApiModelProperty(value = "索引字段列表", position = 2)
	private List<ColumnModelInfo> columns = new ArrayList<ColumnModelInfo>();

	@ApiModelProperty(value = "索引类型", position = 3)
	private IndexType indexType;

	@ApiModelProperty(value = "描述", position = 3)
	private String description;

	public DataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public List<ColumnModelInfo> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModelInfo> columns) {
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