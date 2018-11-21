package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.IdEntity;

import java.util.ArrayList;
import java.util.List;

public class SubFormItemInstance extends IdEntity {

	@ApiModelProperty(value = "子表单字段值", position = 1)
	private List<List<SubFormRowItemInstance>> itemInstances = new ArrayList<>();

	@ApiModelProperty(value = "子表单", position = 5)
	private String tableName;

	@ApiModelProperty(value = "字段模型ID（uuid）", position = 0)
	@Override
	public String getId() {
		return super.getId();
	}

	public List<List<SubFormRowItemInstance>> getItemInstances() {
		return itemInstances;
	}

	public void setItemInstances(List<List<SubFormRowItemInstance>> itemInstances) {
		this.itemInstances = itemInstances;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
