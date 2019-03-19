package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

/**
 * 文件上传类型
 * 
 * @author Jackie
 *
 */
public enum FileUploadType {

	/** 表单 */
	FormModel("FormModel"),

	/** 表单控件 */
	ItemModel("ItemModel"),

	/** 用户头像 */
	HeadPortrait("HeadPortrait"),

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

	public static FileUploadType getFileUploadType(String value) {
		for(FileUploadType fileUploadType : FileUploadType.values()){
			if(StringUtils.hasText(value) && fileUploadType.getValue().equals(value)){
				return fileUploadType;
			}
		}
		return null;
	}
}
