package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
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
	private List<PCDataModel> dataModels = new ArrayList<PCDataModel>();

	@ApiModelProperty(value = "表单字段列表", position = 5)
	private List<ItemModel> items = new ArrayList<ItemModel>();

	@ApiModelProperty(value = "应用id", position = 6)
	private String applicationId;

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

	public List<PCDataModel> getDataModels() {
		return dataModels;
	}

	public void setDataModels(List<PCDataModel> dataModels) {
		this.dataModels = dataModels;
	}

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(List<ItemModel> items) {
		this.items = items;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
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
