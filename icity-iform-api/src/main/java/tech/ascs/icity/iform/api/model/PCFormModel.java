package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("PC表单模型FormModel")
public class PCFormModel extends NameEntity {

	@ApiModelProperty(value = "子表单绑定的数据模型列表", position = 3)
	private List<FormModel> childrenFormModels = new ArrayList<FormModel>();

	@ApiModelProperty(value = "表单绑定的数据模型列表", position = 3)
	private FormModel formModel;

	public List<FormModel> getChildrenFormModels() {
		return childrenFormModels;
	}

	public void setChildrenFormModels(List<FormModel> childrenFormModels) {
		this.childrenFormModels = childrenFormModels;
	}

	public FormModel getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModel formModel) {
		this.formModel = formModel;
	}
}
