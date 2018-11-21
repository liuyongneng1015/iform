package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiModel("数据模型数据信息")
public class DataModelInstance extends NameEntity {

    @ApiModelProperty(value = "表名称", position = 2)
	private String tableName;

	@ApiModelProperty(value = "控件的数据", position = 3)
	private List<List<ItemInstance>> items = new ArrayList<>();

	@ApiModelProperty(value = "数据条数", position = 4)
	private Integer size;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

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
}
