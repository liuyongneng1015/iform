package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("应用表单模型")
public class ApplicationModel extends NameEntity {


	@ApiModelProperty(value="表单模型",position = 3)
	private List<FormModel> formModels = new ArrayList<>();

	@ApiModelProperty(value="数据模型",position = 3)
	private List<DataModel> dataModels = new ArrayList<>();

	@ApiModelProperty(value="列表模型",position = 3)
	private List<ListModel> listModels = new ArrayList<>();

	public List<FormModel> getFormModels() {
		return formModels;
	}

	public void setFormModels(List<FormModel> formModels) {
		this.formModels = formModels;
	}

	public List<DataModel> getDataModels() {
		return dataModels;
	}

	public void setDataModels(List<DataModel> dataModels) {
		this.dataModels = dataModels;
	}

	public List<ListModel> getListModels() {
		return listModels;
	}

	public void setListModels(List<ListModel> listModels) {
		this.listModels = listModels;
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
