package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 赋值方式
 * 
 * @author Jackie
 *
 */
public enum AssignmentWay implements Serializable {

	SystemType("systemType"),//系统属性

	DefaultManual("default");//手写


	private String value;//

	private AssignmentWay(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
