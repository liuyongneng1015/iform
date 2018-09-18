package tech.ascs.icity.iform.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

@Entity
@Table(name = "iform_bind_flow")
public class BindFlow extends BaseEntity{

//	private String id;
	private String formId;
	private String formName;
	private String flowId;
	private String flowName;
	private String step;
	private String stepName;
	private String stepMap;
	

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
	public String getStepMap() {
		return stepMap;
	}
	public void setStepMap(String stepMap) {
		this.stepMap = stepMap;
	}
}
