package tech.ascs.icity.iform.api.model;

/**
 * 默认功能类型
 * 
 * @author Jackie
 *
 */
public enum DefaultFunctionType {

	Add("Add","新增"),

	Import("Import","导入"),

	Export("Export","导出"),

	Delete("Delete","删除"),

	QrCode("QrCode","二维码");

	private String value;
	private String desc;

	private DefaultFunctionType(String value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
