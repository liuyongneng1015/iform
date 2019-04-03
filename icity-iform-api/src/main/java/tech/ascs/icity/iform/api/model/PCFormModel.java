package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	@ApiModelProperty(value = "控件权限", position = 5)
	private List<ItemPermissionModel> permissions = new ArrayList<ItemPermissionModel>();

	@ApiModelProperty(value = "表单功能按钮", position = 8)
	private List<FunctionModel> functions = new ArrayList<FunctionModel>();

	@ApiModelProperty(value = "应用id", position = 6)
	private String applicationId;

	@ApiModelProperty(value="数据标识:控件集合",position = 10)
	private List<ItemModel> itemModelList = new ArrayList<ItemModel>();

	@ApiModelProperty(value="二维码数据标识:控件集合",position = 10)
	private List<ItemModel> qrCodeItemModelList = new ArrayList<ItemModel>();

	@ApiModelProperty(value="关联表单模型集合",position = 11)
	private List<PCFormModel> referenceFormModel = new ArrayList<PCFormModel>();

	@ApiModelProperty(value="关联关系控件集合",position = 10)
	private List<ItemModel> relevanceItemModelList = new ArrayList<ItemModel>();

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

	public List<ItemPermissionModel> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<ItemPermissionModel> permissions) {
		this.permissions = permissions;
	}

	public List<FunctionModel> getFunctions() {
		return functions;
	}

	public void setFunctions(List<FunctionModel> functions) {
		this.functions = functions;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public List<ItemModel> getItemModelList() {
		return itemModelList;
	}

	public void setItemModelList(List<ItemModel> itemModelList) {
		this.itemModelList = itemModelList;
	}

	public List<ItemModel> getQrCodeItemModelList() {
		return qrCodeItemModelList;
	}

	public void setQrCodeItemModelList(List<ItemModel> qrCodeItemModelList) {
		this.qrCodeItemModelList = qrCodeItemModelList;
	}

	public List<PCFormModel> getReferenceFormModel() {
		return referenceFormModel;
	}

	public void setReferenceFormModel(List<PCFormModel> referenceFormModel) {
		this.referenceFormModel = referenceFormModel;
	}

	public List<ItemModel> getRelevanceItemModelList() {
		return relevanceItemModelList;
	}

	public void setRelevanceItemModelList(List<ItemModel> relevanceItemModelList) {
		this.relevanceItemModelList = relevanceItemModelList;
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
