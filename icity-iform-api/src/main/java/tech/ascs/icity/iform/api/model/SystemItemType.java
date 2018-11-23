package tech.ascs.icity.iform.api.model;

/**
 * 系统控件类型
 * 
 * @author LiuYongneng
 *
 */
public enum SystemItemType {

	/** 主键id */
	ID("ID"),
	/** 子表id */
	ChildId("ChildId"),
	/** 创建时间 */
	CreateDate("CreateDate"),
	/** 更新时间 */
	UpdataDate("UpdataDate"),
	/** 创建人 */
	CreateBy("CreateBy"),
	/** 更新人 */
	UpdataBy("UpdataBy"),
	/** 流水号 */
	SerialNumber("SerialNumber"),
	/** 标题 */
	Label("Label"),
	/** 描叙 */
	Description("Description"),
	/** 单行文本 */
	Input("Input"),
	/** 多行文本 */
	MoreInput("MoreInput"),
	/** 数值输入框 */
	InputNumber("InputNumber"),
	/** 图片 */
	Media("Media"),
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
	private SystemItemType(String value) {
		this.value= value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
