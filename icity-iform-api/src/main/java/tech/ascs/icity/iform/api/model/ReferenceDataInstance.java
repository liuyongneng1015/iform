package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.IdEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("关联数据实例DataInstance")
public class ReferenceDataInstance extends IdEntity {
	@ApiModelProperty(value = "控件的数据", position = 3)
	private List<String> value = new ArrayList<>();

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	@ApiModelProperty(value = "表单实例ID", position = 0)
	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
