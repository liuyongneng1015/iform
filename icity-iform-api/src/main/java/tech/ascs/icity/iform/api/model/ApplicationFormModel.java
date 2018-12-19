package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("应用表单模型")
public class ApplicationFormModel extends NameEntity {


	@ApiModelProperty(value="表单模型",position = 3)
	private List<FormModel> formModels = new ArrayList<>();

	public List<FormModel> getFormModels() {
		return formModels;
	}

	public void setFormModels(List<FormModel> formModels) {
		this.formModels = formModels;
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
