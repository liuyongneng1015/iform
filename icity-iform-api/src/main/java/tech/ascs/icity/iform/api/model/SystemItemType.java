package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统控件类型
 * 
 * @author LiuYongneng
 *
 */
public enum SystemItemType {

	/** 未知类型 */
	NONE(""),

	/** 人员单选 */
	PersonnelRadio("PersonnelRadio"),
	/** 人员多选 */
	PersonnelMore("PersonnelMore"),

	/** 部门单选 */
	DepartmentRadio("DepartmentRadio"),
	/** 部门多选 */
	DepartmentMore("DepartmentMore"),

	/** 主键id */
	ID("ID"),
	/** 子表id */
	ChildId("ChildId"),

	/** 创建时间 */
	CreateDate("CreateDate"),

	/** 创建人 */
	Creator("Creator"),

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
	Row("Row"),
	/** 富文本控件 */
	Editor("Editor"),
	/** 标签页 */
	Tabs("Tabs"),
	/** 标签页子项 */
	TabPane("TabPane");

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

	public static List<ColumnType> getColumnType(SystemItemType systemItemType){
		List<ColumnType> list = new ArrayList<>();
		if(SystemItemType.CreateDate == systemItemType || SystemItemType.DatePicker == systemItemType){
			list.add(ColumnType.Date);
			list.add(ColumnType.Time);
			list.add(ColumnType.Timestamp);
		}else if(SystemItemType.InputNumber == systemItemType){
			list.add(ColumnType.Integer);
			list.add(ColumnType.Long);
			list.add(ColumnType.Float);
			list.add(ColumnType.Double);
		}else{
			list.add(ColumnType.String);
			list.add(ColumnType.Text);
			list.add(ColumnType.Boolean);
		}
		return list;

	}
}
