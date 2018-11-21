package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.IdEntity;

import java.util.List;

public class SubFormRowItemInstance extends IdEntity {

	@ApiModelProperty(value = "行字段值", position = 1)
	private List<ItemInstance> itemInstances;

	@ApiModelProperty(value = "行数", position = 5)
	private Integer rowNumber;

	@ApiModelProperty(value = "字段模型ID（uuid）", position = 0)
	@Override
	public String getId() {
		return super.getId();
	}

	public List<ItemInstance> getItemInstances() {
		return itemInstances;
	}

	public void setItemInstances(List<ItemInstance> itemInstances) {
		this.itemInstances = itemInstances;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}
}
