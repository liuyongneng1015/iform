package tech.ascs.icity.iform.api.model;

/**
 * 索引类型
 * 
 * @author Jackie
 *
 */
public enum IndexType {

	/** 精确查询 */
	Unique("惟一索引"),

	/** 模糊查询 */
	Normal("普通索引");

	private String value;

	private IndexType(String value) {
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
