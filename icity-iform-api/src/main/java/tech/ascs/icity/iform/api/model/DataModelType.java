package tech.ascs.icity.iform.api.model;

/**
 * 数据模型类型
 * 
 * @author Jackie
 *
 */
public enum DataModelType {

	//单表
	Single("Single"),

	//主表
	Master("Master"),

	//从表
	Slaver("Slaver"),

	//关联表
	Relevance("Relevance");

	private String value;

	private DataModelType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

}
