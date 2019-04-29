package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 业务触发类型
 * 
 * @author Jackie
 *
 */
public enum BusinessTriggerType implements Serializable {

	Add_Before("Add_Before"),//新增前

	Add_After("Add_After"),//新增后

	Update_Before("Update_Before"),//更新前

	Update_After("Update_After"),//更新后

	Delete_Before("Delete_Before"),//删除前

	Delete_After("Delete_After");//删除后


	private String value;//

	private BusinessTriggerType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
