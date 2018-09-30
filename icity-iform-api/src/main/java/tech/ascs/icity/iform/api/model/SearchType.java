package tech.ascs.icity.iform.api.model;

/**
 * 字段查询类型
 * 
 * @author Jackie
 *
 */
public enum SearchType {

	/** 精确查询 */
	Equal("="),

	/** 模糊查询 */
	Like("LIKE");

	private String value;

	private SearchType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}
