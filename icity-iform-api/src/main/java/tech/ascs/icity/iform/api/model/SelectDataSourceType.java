package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

/**
 * 选择框数据来源类型
 * 
 * @author Jackie
 *
 */
public enum SelectDataSourceType {

	/** 自定义选项 */
	Option("Option"),

	/** 字典系统代码 */
	DictionaryData("DictionaryData"),

	/** 字典模型 */
	DictionaryModel("DictionaryModel"),

	/** 其他方式 */
	Other("Other");

	private String value;

	private SelectDataSourceType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static SelectDataSourceType getDataSourceType(String value) {
		for(SelectDataSourceType sourceType : SelectDataSourceType.values()){
			if(StringUtils.hasText(value) && sourceType.getValue().equals(value)){
				return sourceType;
			}
		}
		return null;
	}
}