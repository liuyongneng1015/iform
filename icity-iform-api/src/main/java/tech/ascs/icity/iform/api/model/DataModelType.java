package tech.ascs.icity.iform.api.model;

/**
 * 数据模型类型
 * 
 * @author Jackie
 *
 */
public enum DataModelType {

	Single("单表"),

	Master("主表"),

	Slaver("从表");

	private String value;

	private DataModelType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}
