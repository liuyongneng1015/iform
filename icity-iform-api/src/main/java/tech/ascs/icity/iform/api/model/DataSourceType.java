package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

/**
 * 数据来源类型
 * 
 * @author Jackie
 *
 */
public enum DataSourceType {

	/** 表单 */
	FormModel("FormModel"),

	/** 表单控件 */
	ItemModel("ItemModel"),

	/** 用户头像 */
	HeadPortrait("HeadPortrait"),

	/** 其他方式 */
	Other("Other");

	private String value;

	private DataSourceType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static DataSourceType getDataSourceType(String value) {
		for(DataSourceType sourceType : DataSourceType.values()){
			if(StringUtils.hasText(value) && sourceType.getValue().equals(value)){
				return sourceType;
			}
		}
		return null;
	}
}
