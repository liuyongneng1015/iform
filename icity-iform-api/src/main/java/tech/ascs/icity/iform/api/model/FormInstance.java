package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.IdEntity;

@ApiModel("表单实例FormInstance")
public class FormInstance extends IdEntity {

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

	@ApiModelProperty(value = "表单实例ID", hidden = true)
	@JsonIgnore
	private Map<String, Object> data = new HashMap<String, Object>();

	@ApiModelProperty(value = "表单实例ID", position = 0)
	@Override
	public String getId() {
		return super.getId();
	}

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
}
