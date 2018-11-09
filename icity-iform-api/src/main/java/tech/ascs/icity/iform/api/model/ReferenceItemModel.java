package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("关联表单控件模型ItemModel")
public class ReferenceItemModel extends BaseItemModel {

	@ApiModelProperty(value = "关联类型", position = 10)
	private ReferenceType referenceType;

	@ApiModelProperty(value="关联类型单选，多选，反选，属性",position = 11)
	private SelectMode selectMode;

	@ApiModelProperty(value = "关联表", position = 13)
	private String referenceTable;

	@ApiModelProperty(value = "控件类型选择框还是列表", position = 14)
	private ControlType controlType;


	@ApiModelProperty(value = "关联值字段（比如“ID”）", position = 15)
	private String referenceValueColumn;

	@ApiModelProperty(value = "关联显示列表模型",position = 16)
	private ListModel referenceList;

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public SelectMode getSelectMode() {
		return selectMode;
	}

	public void setSelectMode(SelectMode selectMode) {
		this.selectMode = selectMode;
	}

	public String getReferenceTable() {
		return referenceTable;
	}

	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	public ControlType getControlType() {
		return controlType;
	}

	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}

	public String getReferenceValueColumn() {
		return referenceValueColumn;
	}

	public void setReferenceValueColumn(String referenceValueColumn) {
		this.referenceValueColumn = referenceValueColumn;
	}

	public ListModel getReferenceList() {
		return referenceList;
	}

	public void setReferenceList(ListModel referenceList) {
		this.referenceList = referenceList;
	}

}
