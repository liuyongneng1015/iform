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
	/** 视频或图片 */
	Media("Media"),
	/** 附件 */
	Attachment("Attachment"),

	/** 日期控件 */
	DatePicker("DatePicker"),

	/** 时间控件 */
	TimePicker("TimePicker"),

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
	/** 关联属性内嵌 */
	ReferenceInnerLabel("ReferenceInnerLabel"),
	/** 子表 */
	SubForm("SubForm"),
	/** 子表自定义（组合控件） */
	RowItem("RowItem"),
	/** 主表自定义（组合控件） */
	Row("Row"),

    /** 富文本控件 */
    Editor("Editor"),

	/** 标签页 */
	Tabs("Tabs"),

	/** 标签页子项 */
	TabPane("TabPane"),

	/** 树形下拉 */
	Treeselect("Treeselect"),

	/** 地图 */
	Location("Location"),

	/** 流程日志 */
	ProcessLog("ProcessLog"),

	/** 门户建模控件 */
	/** 新闻 */
	News("News"),
	/** 快捷菜单 */
	QuickMenu("QuickMenu"),
	/** 列表 */
	List("List"),
	/** 报表 */
	Report("Report"),
	/** 通知 */
	Notice("Notice");

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

	public boolean hasContext() {
		if (this == Label || this == SubForm || this == RowItem || this == Row || this == Tabs || this == TabPane || this == QuickMenu || this == List || this == Report || this == Notice || this == News){
			return false;
		}else {
			return true;
		}
	}

}
