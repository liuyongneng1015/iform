package tech.ascs.icity.iform.api.model;

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

	@ApiModelProperty(value = "关联表单数据", hidden = true)
	private List<ReferenceDataInstance> referenceData = new ArrayList<ReferenceDataInstance>();

	@ApiModelProperty(value = "子表单数据", hidden = true)
	private List<SubFormItemInstance> subFormData = new ArrayList<SubFormItemInstance>();

	@ApiModelProperty(value = "表单实例ID", hidden = true)
	private Map<String, Object> data = new HashMap<String, Object>();

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

	public Object getData(String key) {
		return data.get(key);
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