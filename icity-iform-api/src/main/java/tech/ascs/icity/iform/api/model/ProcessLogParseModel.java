package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 流程日志解析形式
 * 
 * @author Jackie
 *
 */
public enum ProcessLogParseModel implements Serializable {

	Timeline("Timeline"),//时间抽

	ListModel("ListModel");//列表

	private String value;//

	private ProcessLogParseModel(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
