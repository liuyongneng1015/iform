package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 赋值区域
 * 
 * @author Jackie
 *
 */
public enum AssignmentArea implements Serializable {

	UserID("userID"),//当前用户id

	UserName("userName"),//当前用户名称

	SystemTime("systemTime"),//系统时间（操作点）

	ActivitieID("activitieID"),//当前环节名称id

	ActivitieName("activitieName");//当前环节名称


	private String value;//

	private AssignmentArea(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
