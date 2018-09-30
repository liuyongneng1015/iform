package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("表单模型FormModel")
public class FormModel extends NameEntity {

	@ApiModel("流程摘要信息")
	public static class ProceeeModel extends NameEntity {
		@ApiModelProperty(value = "流程KEY", position = 2)
		private String key;
		@ApiModelProperty(value = "流程启动环节ID", position = 3)
		private String startActivity;
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getStartActivity() {
			return startActivity;
		}
		public void setStartActivity(String startActivity) {
			this.startActivity = startActivity;
		}
		
	}

	@ApiModelProperty(value = "描述", position = 2)
	private String description;

	@ApiModelProperty(value = "表单绑定的数据模型列表", position = 3)
	private List<DataModelInfo> dataModels = new ArrayList<DataModelInfo>();

	@ApiModelProperty(value = "表单绑定的流程模型", position = 4)
	private ProceeeModel process;

	@ApiModelProperty(value = "表单字段列表", position = 5)
	private List<ItemModel> items = new ArrayList<ItemModel>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<DataModelInfo> getDataModels() {
		return dataModels;
	}

	public void setDataModels(List<DataModelInfo> dataModels) {
		this.dataModels = dataModels;
	}

	public ProceeeModel getProcess() {
		return process;
	}

	public void setProcess(ProceeeModel process) {
		this.process = process;
	}

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(List<ItemModel> items) {
		this.items = items;
	}
}
