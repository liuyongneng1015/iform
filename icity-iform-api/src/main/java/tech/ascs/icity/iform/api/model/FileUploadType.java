package tech.ascs.icity.iform.api.model;

/**
 * 文件上传类型
 * 
 * @author Jackie
 *
 */
public enum FileUploadType {

	/** 表单控件 */
	ItemModel("ItemModel"),

	/** 其他方式 */
	Other("Other");

	private String value;

	private FileUploadType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
