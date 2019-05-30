package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

/**
 * 默认功能类型
 * 
 * @author Jackie
 *
 */
public enum DefaultFunctionType {

	Add("add","新增"),

	Edit("edit", "编辑"),

	Import("import","导入"),

	Export("export","导出"),

	Delete("delete","删除"),

	QrCode("erweima","二维码"),

	TempStore("tempStore", "暂存"),

	Manage("manage", "办理"),

	Download("download", "下载"),

	BatchDelete("batchDelete", "批量删除");

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

	public static DefaultFunctionType getByValue(String value){
		if(!StringUtils.hasText(value)){
			return null;
		}
		for(DefaultFunctionType functionType : DefaultFunctionType.values()){
			if(functionType.getValue().equals(value)){
				return functionType;
			}
		}
		return null;
	}
}
