package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
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
	private List<DataModel> dataModels = new ArrayList<DataModel>();

	@ApiModelProperty(value = "表单绑定的流程模型", position = 4)
	private ProceeeModel process;

	@ApiModelProperty(value = "表单字段列表", position = 5)
	private List<ItemModel> items = new ArrayList<ItemModel>();

	@ApiModelProperty(value = "应用id", position = 6)
	private String applicationId;

    @ApiModelProperty(value = "控件权限", position = 7)
    private List<ItemPermissionModel> permissions = new ArrayList<ItemPermissionModel>();

    @ApiModelProperty(value = "表单提交校验", position = 8)
    private List<FormSubmitCheckModel> submitChecks = new ArrayList<FormSubmitCheckModel>();

	@ApiModelProperty(value = "表单功能按钮", position = 8)
	private List<FunctionModel> functions = new ArrayList<FunctionModel>();

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

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

    public List<ItemPermissionModel> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<ItemPermissionModel> permissions) {
        this.permissions = permissions;
    }

    public List<FormSubmitCheckModel> getSubmitChecks() {
        return submitChecks;
    }

    public void setSubmitChecks(List<FormSubmitCheckModel> submitChecks) {
        this.submitChecks = submitChecks;
    }

	public List<FunctionModel> getFunctions() {
		return functions;
	}

	public void setFunctions(List<FunctionModel> functions) {
		this.functions = functions;
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
