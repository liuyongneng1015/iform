package tech.ascs.icity.iform.api.model;

/**
 * 系统创建类型
 * 
 * @author Jackie
 *
 */
public enum SystemCreateType {

	/** 创建时 */
	Create("create"),

	/** 更新时 */
	Update("Update"),

	/** 普通 */
	Normal("Normal");

	private String value;

	private SystemCreateType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
