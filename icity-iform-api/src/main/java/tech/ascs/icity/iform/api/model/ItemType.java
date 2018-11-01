package tech.ascs.icity.iform.api.model;

/**
 * 表单控件类型
 * 
 * @author Jackie
 *
 */
public enum ItemType {

	/** 标签 */
	Label,
	/** 单行文本 / 多行文本 */
	Input,
	/** 数值输入框 */
	InputNumber,
	/** 图片 */
	Image,
	/** 附件 */
	Attachment,
	/** 日期控件 */
	DatePicker,
	/** 下拉选择 */
	Select,
	/** 单选 */
	RadioGroup,
	/** 多选 */
	CheckboxGroup,
	/** 关联表单列表 */
	ReferenceList,
	/** 关联属性 */
	ReferenceLabel,
	/** 子表 */
	SubForm,
	/** 自定义（组合控件） */
	Row
}
