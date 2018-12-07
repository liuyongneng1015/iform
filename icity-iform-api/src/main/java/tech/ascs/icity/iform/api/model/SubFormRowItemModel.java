package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel("子表单行级控件模型ItemModel")
public class SubFormRowItemModel extends BaseItemModel {

	@ApiModelProperty(value=" 控件行数",position = 12)
	private Integer rowNumber;

	@ApiModelProperty(value="组件子项（由组和字段构成） ",position = 14)
	private List<ItemModel> items;

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(List<ItemModel> items) {
		this.items = items;
	}

}
