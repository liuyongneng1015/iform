package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.IdEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel("保存表单实例FormDataSaveInstance")
public class FormDataSaveInstance extends IdEntity {

	@ApiModelProperty(value = "表单模型ID", position = 1)
	private String formId;

	@ApiModelProperty(value = "流程ID", position = 2)
	private String processId;

	@ApiModelProperty(value = "环节ID", position = 3)
	private String activityId;

	@ApiModelProperty(value = "流程实例ID", position = 4)
	private String processInstanceId;

	@ApiModelProperty(value = "环节实例ID", position = 5)
	private String activityInstanceId;

	@ApiModelProperty(value = "表单控件实例列表", position = 6)
	private List<ItemInstance> items = new ArrayList<ItemInstance>();

	@ApiModelProperty(value = "关联表单数据", position = 7)
	private List<ReferenceDataInstance> referenceData = new ArrayList<ReferenceDataInstance>();

	@ApiModelProperty(value = "子表单数据", position = 8)
	private List<SubFormItemInstance> subFormData = new ArrayList<SubFormItemInstance>();

	@ApiModelProperty(value = "表单实例ID", position = 9)
	private Map<String, Object> data = new HashMap<String, Object>();

    @ApiModelProperty(value = "流程数据", position = 9)
    private Map<String, Object> flowData = new HashMap<String, Object>();

	@ApiModelProperty(value = "二维码图片", position = 10)
	private FileUploadModel fileUploadModel;

	@ApiModelProperty(value = "是否能编辑", position = 10)
	private Boolean canEdit = true;

	@ApiModelProperty(value = "数据标识集合", position = 10)
	private String label;

	/** 当前环节操作 */
	@ApiModelProperty(value = "当前环节操作", position = 14)
	protected Object functions;

	/** 当前环节表单配置 */
	@ApiModelProperty(value = "当前环节表单配置", position = 14)
	protected Object permissions;

	/** 当前用户是否当前流程环节处理人 */
	@ApiModelProperty(value = "当前用户是否当前流程环节处理人", position = 13)
	private Boolean myTask;

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getActivityInstanceId() {
		return activityInstanceId;
	}

	public void setActivityInstanceId(String activityInstanceId) {
		this.activityInstanceId = activityInstanceId;
	}

	public List<ItemInstance> getItems() {
		return items;
	}

	public void setItems(List<ItemInstance> items) {
		this.items = items;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public void addData(String key, Object value) {
		data.put(key, value);
	}

	public void addAllData(Map<String, Object> map) {
		data.putAll(map);
	}

	public Object getData(String key) {
		return data.get(key);
	}

    public Map<String, Object> getFlowData() {
        return flowData;
    }

    public void setFlowData(Map<String, Object> flowData) {
        this.flowData = flowData;
    }

    public List<ReferenceDataInstance> getReferenceData() {
		return referenceData;
	}

	public void setReferenceData(List<ReferenceDataInstance> referenceData) {
		this.referenceData = referenceData;
	}

	public List<SubFormItemInstance> getSubFormData() {
		return subFormData;
	}

	public void setSubFormData(List<SubFormItemInstance> subFormData) {
		this.subFormData = subFormData;
	}

	public FileUploadModel getFileUploadModel() {
		return fileUploadModel;
	}

	public void setFileUploadModel(FileUploadModel fileUploadModel) {
		this.fileUploadModel = fileUploadModel;
	}

	public Boolean getCanEdit() {
		return canEdit;
	}

	public void setCanEdit(Boolean canEdit) {
		this.canEdit = canEdit;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Object getFunctions() {
		return functions;
	}

	public void setFunctions(Object functions) {
		this.functions = functions;
	}

	public Object getPermissions() {
		return permissions;
	}

	public void setPermissions(Object permissions) {
		this.permissions = permissions;
	}

	public Boolean getMyTask() {
		return myTask;
	}

	public void setMyTask(Boolean myTask) {
		this.myTask = myTask;
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
