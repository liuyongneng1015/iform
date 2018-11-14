package tech.ascs.icity.iform.api.model;

/**
 * 表单控件类型
 * 
 * @author Jackie
 *
 */
public enum ItemType {

	/** 标签 */
	Label("Label"),
	/** 单行文本 / 多行文本 */
	Input("Input"),
	/** 数值输入框 */
	InputNumber("InputNumber"),
	/** 图片 */
	Image("Image"),
	/** 附件 */
	Attachment("Attachment"),
	/** 日期控件 */
	DatePicker("DatePicker"),
	/** 下拉选择 */
	Select("Select"),
	/** 单选 */
	RadioGroup("RadioGroup"),
	/** 多选 */
	CheckboxGroup("CheckboxGroup"),
	/** 关联表单列表 */
	ReferenceList("ReferenceList"),
	/** 关联属性 */
	ReferenceLabel("ReferenceLabel"),
	/** 子表 */
	SubForm("SubForm"),
	/** 子表自定义（组合控件） */
	RowItem("RowItem"),
	/** 主表自定义（组合控件） */
	Row("Row");
	private String value;
	private ItemType(String value) {
		this.value= value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
