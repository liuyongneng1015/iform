package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 流程日志时间排序类型
 * 
 * @author Jackie
 *
 */
public enum ProcessLogSortType implements Serializable {

	DESC("DESC"),//降序

	ASC("ASC");//升序

	private String value;//

	private ProcessLogSortType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
