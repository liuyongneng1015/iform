package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("数据模型行数据信息")
public class DataModelRowInstance extends NameEntity {

	@ApiModelProperty(value = "控件的数据", position = 3)
	private List<ItemInstance> items = new ArrayList<>();

	@ApiModelProperty(value = "数据条数", position = 4)
	private Integer rowNumber;

	public List<ItemInstance> getItems() {
		return items;
	}

	public void setItems(List<ItemInstance> items) {
		this.items = items;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
