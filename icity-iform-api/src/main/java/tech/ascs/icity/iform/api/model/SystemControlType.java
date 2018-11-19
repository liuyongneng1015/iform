package tech.ascs.icity.iform.api.model;

/**
 * 系统控件类型
 * 
 * @author LiuYongneng
 *
 */
public enum SystemControlType {

	/** 创建时间 */
	CreateDate("CreateDate"),
	/** 更新时间 */
	UpdataDate("UpdataDate"),
	/** 创建人 */
	CreateBy("CreateBy"),
	/** 更新人 */
	UpdataBy("UpdataBy"),
	/** 流水号 */
	SerialNumber("SerialNumber");
	private String value;
	private SystemControlType(String value) {
		this.value= value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
