package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.IdEntity;

import java.util.ArrayList;
import java.util.List;

public class SubFormDataItemInstance extends IdEntity {

	@ApiModelProperty(value = "子表单行（小子表）字段值", position = 1)
	private List<SubFormRowItemInstance> items = new ArrayList<>();

	@ApiModelProperty(value = "行数", position = 2)
	private Integer rowNumber;

	@ApiModelProperty(value = "字段模型ID（uuid）", position = 0)
	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}

	public List<SubFormRowItemInstance> getItems() {
		return items;
	}

	public void setItems(List<SubFormRowItemInstance> items) {
		this.items = items;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

}
