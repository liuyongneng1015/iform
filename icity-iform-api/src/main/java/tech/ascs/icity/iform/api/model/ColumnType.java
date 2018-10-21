package tech.ascs.icity.iform.api.model;

/**
 * 数据字段类型
 * 
 * @author Jackie
 *
 */
public enum ColumnType {

	Integer("整数（32位）"),

	Long("整数（64位）"),

	Float("浮点数（32位）"),

	Double("浮点数（64位）"),

	String("字符串"),

	Date("日期"),

	Time("时间"),

	Timestamp("时间戳"),

	Boolean("布尔类型");

	private String value;

	private ColumnType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
//
//	@Override
//	public String toString() {
//		return value;
//	}
}
