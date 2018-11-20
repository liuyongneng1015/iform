package tech.ascs.icity.iform.api.model;

import java.io.Serializable;

/**
 * 数据字段类型
 * 
 * @author Jackie
 *
 */
public enum ColumnType implements Serializable {

	Integer("Integer"),//整数（32位）

	Long("Long"),//整数（64位）

	Float("Float"),//浮点数（32位）

	Double("Double"),//浮点数（64位）

	String("String"),//字符串

	Date("Date"),//日期

	Time("Time"),//时间

	Timestamp("Timestamp"),//时间戳

	Boolean("Boolean");//布尔类型

	private String value;//

	private ColumnType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
