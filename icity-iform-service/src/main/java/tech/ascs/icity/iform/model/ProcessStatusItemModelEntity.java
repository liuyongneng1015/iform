package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.ProcessLogParseModel;
import tech.ascs.icity.iform.api.model.ProcessLogSortType;

import javax.persistence.*;

/**
 * 流程状态控件模型
 */
@Entity
@Table(name = "ifm_process_status_item_model")
@DiscriminatorValue("processStatusItemModel")
public class ProcessStatusItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 237837L;

	@Column(name="process_status", length = 512)//流程状态
	private String processStatus;


	public String getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
	}
}