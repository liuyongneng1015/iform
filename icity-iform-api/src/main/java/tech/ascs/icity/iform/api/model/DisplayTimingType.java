package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 显示时机
 * 
 * @author Jackie
 *
 */
public enum DisplayTimingType implements Serializable {

	Add("Add"),//新增时

	Update("Update"),//更新时

	Check("Check"),//查看时

	ListShow("ListShow"),//列表栏

	Other("Other");//其他

	private String value;

	private DisplayTimingType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
