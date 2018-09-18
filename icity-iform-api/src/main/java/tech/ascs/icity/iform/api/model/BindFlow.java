package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import tech.ascs.icity.model.IdEntity;



@ApiModel("绑定流程操作接口")
public class BindFlow  extends IdEntity{

//	private String id;
	@ApiModelProperty(value = "表单id",required=true)
	private String formId;
	@ApiModelProperty(value = "表单名称",required=true)
	private String formName;
	@ApiModelProperty(value = "流程id",required=true)
	private String flowId;
	@ApiModelProperty(value = "流程名称",required=true)
	private String flowName;
	@ApiModelProperty(value = "流程环节",required=true)
	private String step;
	@ApiModelProperty(value = "流程环节名称")
	private String stepName;
	@ApiModelProperty(value = "流程环节显示字段配置列表",required=true)
	private List<StepMap> stepMap;
	
//	public String getId() {
//		return id;
//	}
//	public void setId(String id) {
//		this.id = id;
//	}
	public String getFormId() {
		return formId;
	}
	public void setFormId(String formId) {
		this.formId = formId;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getFlowId() {
		return flowId;
	}
	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}
	public String getFlowName() {
		return flowName;
	}
	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}
	public String getStep() {
		return step;
	}
	public void setStep(String step) {
		this.step = step;
	}
	public String getStepName() {
		return stepName;
	}
	public void setStepName(String stepName) {
		this.stepName = stepName;
	}
	public List<StepMap> getStepMap() {
		return stepMap;
	}
	public void setStepMap(List<StepMap> stepMap) {
		this.stepMap = stepMap;
	}

}
