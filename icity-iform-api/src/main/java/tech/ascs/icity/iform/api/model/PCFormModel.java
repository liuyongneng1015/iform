package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("PC表单模型FormModel")
public class PCFormModel extends NameEntity {

	@ApiModel("流程摘要信息")
	public static class PCProceeeModel extends NameEntity {
		@ApiModelProperty(value = "流程KEY", position = 0)
		private String key;
		@ApiModelProperty(value = "流程启动环节ID", position = 1)
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
	@ApiModelProperty(value = "表单绑定的流程模型", position = 1)
	private PCProceeeModel process;

	@ApiModelProperty(value = "描述", position = 2)
	private String description;

	@ApiModelProperty(value = "关联表数据模型列表", position = 3)
	private List<DataModel> dataModels = new ArrayList<DataModel>();


	public PCProceeeModel getProcess() {
		return process;
	}

	public void setProcess(PCProceeeModel process) {
		this.process = process;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<DataModel> getDataModels() {
		return dataModels;
	}

	public void setDataModels(List<DataModel> dataModels) {
		this.dataModels = dataModels;
	}
}
