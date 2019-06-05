package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 流程日志显示字段
 * 
 * @author Jackie
 *
 */
public enum ProcessLogDisplayField implements Serializable {

	Operator("Operator"),//操作者

	Time("Time"),//时间

	Content("Content"),//内容

	Title("Title");//标题

	private String value;//

	private ProcessLogDisplayField(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
