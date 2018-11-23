package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiModel("数据模型数据信息")
public class DataModelInstance extends NameEntity {

	@ApiModelProperty(value = "控件的数据", position = 3)
	private List<List<ItemInstance>> items = new ArrayList<>();

	@ApiModelProperty(value = "数据条数", position = 4)
	private Integer size;

	@ApiModelProperty(value = "表名", position = 5)
	private ReferenceType referenceType;

	@ApiModelProperty(value = " 关联表单模型", position = 6)
	private String referenceTable;
	@ApiModelProperty(value = " 关联字段模型（比如“id”）", position = 7)
	private String referenceValueColumn;

	public List<List<ItemInstance>> getItems() {
		return items;
	}

	public void setItems(List<List<ItemInstance>> items) {
		this.items = items;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public String getReferenceTable() {
		return referenceTable;
	}

	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	public String getReferenceValueColumn() {
		return referenceValueColumn;
	}

	public void setReferenceValueColumn(String referenceValueColumn) {
		this.referenceValueColumn = referenceValueColumn;
	}
}
