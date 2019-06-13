package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.ProcessLogParseModel;
import tech.ascs.icity.iform.api.model.ProcessLogSortType;

import javax.persistence.*;

/**
 * 流程日志控件模型
 */
@Entity
@Table(name = "ifm_process_log_item_model")
@DiscriminatorValue("processLogItemModel")
public class ProcessLogItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 237837L;

	@Column(name="sort_type")//排序类型DESC:降序 ASC:升序
	@Enumerated(EnumType.STRING)
	private ProcessLogSortType sortType;

	@Column(name="display_field")//显示字典
	private String displayFields;

	@Column(name="parse_model")//解析类型
	@Enumerated(EnumType.STRING)
	private ProcessLogParseModel parseModel;

	public ProcessLogSortType getSortType() {
		return sortType;
	}

	public void setSortType(ProcessLogSortType sortType) {
		this.sortType = sortType;
	}

	public String getDisplayFields() {
		return displayFields;
	}

	public void setDisplayFields(String displayFields) {
		this.displayFields = displayFields;
	}

	public ProcessLogParseModel getParseModel() {
		return parseModel;
	}

	public void setParseModel(ProcessLogParseModel parseModel) {
		this.parseModel = parseModel;
	}
}