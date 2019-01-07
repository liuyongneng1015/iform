package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel("子表单行级控件模型")
public class SubFormRowItemModel extends ItemModel {


	//FileItemModel
	@ApiModelProperty(value = "文件类型", position = 7)
	private FileReferenceType fileReferenceType = FileReferenceType.Attachment;

	@ApiModelProperty(value = "文件大小限制M", position = 7)
	private Integer fileSizeLimit = 10;

	//File/selectItemModel
	@ApiModelProperty(value = "关联类型", position = 9)
	private ReferenceType referenceType;
	@ApiModelProperty(value = "多对多创建表的名称", position = 10)
	private String referenceTableName;
	@ApiModelProperty(value="选择关系",position = 11)
	private SelectReferenceType selectReferenceType;

	@ApiModelProperty(value="是否多选、是否允许多传",position = 11)
	private Boolean multiple;

	@ApiModelProperty(value="单选、多选、反选",position = 12)
	private SelectMode selectMode;
	@ApiModelProperty(value = "关联字典分类ID", position = 13)
	private String referenceDictionaryId;
	@ApiModelProperty(value = "关联字典分类名称", position = 13)
	private String referenceDictionaryName;

	@ApiModelProperty(value = " 关联字典联动目标id", position = 13)
	private String referenceDictionaryItemId;
	@ApiModelProperty(value = "关联字典取值范围", position = 13)
	private String referenceDictionaryItemName;

	@ApiModelProperty(value = " 关联字典默认选项", position = 13)
	private List<DictionaryItemModel> referenceDictionaryItemList;

	@ApiModelProperty(value = " 关联表单(如表名、表单名)", position = 14)
	private String referenceTable;
	@ApiModelProperty(value = " 关联字段模型（比如字段、控件名）", position = 15)
	private String referenceValueColumn;
	@ApiModelProperty(value = "默认值(数据字典的默认值)", position = 15)
	private Object defaultValue;

	@ApiModelProperty(value = "关联表单模型id", position = 15)
	private String referenceFormId;
	@ApiModelProperty(value = "关联表单模型名称", position = 15)
	private String referenceFormName;

	@ApiModelProperty(value = " 关联控件模型id", position = 15)
	private String referenceItemId;
	@ApiModelProperty(value = " 关联控件模型名称", position = 15)
	private String referenceItemName;

	@ApiModelProperty(value="控件类型选择框还是列表", position = 16)
	private ControlType controlType;
	@ApiModelProperty(value = "关联显示列表模型",position = 17)
	private ListModel referenceList;

	//SubFormItemModel
	@ApiModelProperty(value="头部标签",position = 19)
	private String legend;
	@ApiModelProperty(value=" 控件行数",position = 20)
	private Integer rowCount;
	@ApiModelProperty(value="是否显示表头",position = 21)
	private Boolean showHead;
	@ApiModelProperty(value="表名",position = 22)
	private String tableName;

	//TimeItemModel
	@ApiModelProperty(value="时间格式如yyyy-MM-dd",position = 27)//时间格式
	private String timeFormat;

	@ApiModelProperty(value="创建类型：Create创建时，Update更新时，Normal普通", position = 29)
	private SystemCreateType createType = SystemCreateType.Normal;

	@ApiModelProperty(value="数据标识:控件id集合",position = 30)
	private List<String> itemModelList = new ArrayList<>();
	@ApiModelProperty(value="是否被选中:true选中，flse未选中",position = 31)
	private Boolean  selectFlag = false;

	@ApiModelProperty(value="前缀业务标识",position = 32)
	private String prefix;

	@ApiModelProperty(value="后缀自增长数字位数",position = 33)
	private Integer suffix;

	@ApiModelProperty(value="数据字典值类型",position = 34)
	private DictionaryValueType dictionaryValueType;

	@Override
	public FileReferenceType getFileReferenceType() {
		return fileReferenceType;
	}

	@Override
	public void setFileReferenceType(FileReferenceType fileReferenceType) {
		this.fileReferenceType = fileReferenceType;
	}

	@Override
	public Integer getFileSizeLimit() {
		return fileSizeLimit;
	}

	@Override
	public void setFileSizeLimit(Integer fileSizeLimit) {
		this.fileSizeLimit = fileSizeLimit;
	}

	@Override
	public ReferenceType getReferenceType() {
		return referenceType;
	}

	@Override
	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	public String getReferenceTableName() {
		return referenceTableName;
	}

	@Override
	public void setReferenceTableName(String referenceTableName) {
		this.referenceTableName = referenceTableName;
	}

	@Override
	public SelectReferenceType getSelectReferenceType() {
		return selectReferenceType;
	}

	@Override
	public void setSelectReferenceType(SelectReferenceType selectReferenceType) {
		this.selectReferenceType = selectReferenceType;
	}

	@Override
	public Boolean getMultiple() {
		return multiple;
	}

	@Override
	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	@Override
	public SelectMode getSelectMode() {
		return selectMode;
	}

	@Override
	public void setSelectMode(SelectMode selectMode) {
		this.selectMode = selectMode;
	}

	@Override
	public String getReferenceDictionaryId() {
		return referenceDictionaryId;
	}

	@Override
	public void setReferenceDictionaryId(String referenceDictionaryId) {
		this.referenceDictionaryId = referenceDictionaryId;
	}

	@Override
	public String getReferenceDictionaryName() {
		return referenceDictionaryName;
	}

	@Override
	public void setReferenceDictionaryName(String referenceDictionaryName) {
		this.referenceDictionaryName = referenceDictionaryName;
	}

	@Override
	public String getReferenceDictionaryItemId() {
		return referenceDictionaryItemId;
	}

	@Override
	public void setReferenceDictionaryItemId(String referenceDictionaryItemId) {
		this.referenceDictionaryItemId = referenceDictionaryItemId;
	}

	@Override
	public String getReferenceDictionaryItemName() {
		return referenceDictionaryItemName;
	}

	@Override
	public void setReferenceDictionaryItemName(String referenceDictionaryItemName) {
		this.referenceDictionaryItemName = referenceDictionaryItemName;
	}

	@Override
	public List<DictionaryItemModel> getReferenceDictionaryItemList() {
		return referenceDictionaryItemList;
	}

	@Override
	public void setReferenceDictionaryItemList(List<DictionaryItemModel> referenceDictionaryItemList) {
		this.referenceDictionaryItemList = referenceDictionaryItemList;
	}

	@Override
	public String getReferenceTable() {
		return referenceTable;
	}

	@Override
	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	@Override
	public String getReferenceValueColumn() {
		return referenceValueColumn;
	}

	@Override
	public void setReferenceValueColumn(String referenceValueColumn) {
		this.referenceValueColumn = referenceValueColumn;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getReferenceFormId() {
		return referenceFormId;
	}

	@Override
	public void setReferenceFormId(String referenceFormId) {
		this.referenceFormId = referenceFormId;
	}

	@Override
	public String getReferenceFormName() {
		return referenceFormName;
	}

	@Override
	public void setReferenceFormName(String referenceFormName) {
		this.referenceFormName = referenceFormName;
	}

	@Override
	public String getReferenceItemId() {
		return referenceItemId;
	}

	@Override
	public void setReferenceItemId(String referenceItemId) {
		this.referenceItemId = referenceItemId;
	}

	@Override
	public String getReferenceItemName() {
		return referenceItemName;
	}

	@Override
	public void setReferenceItemName(String referenceItemName) {
		this.referenceItemName = referenceItemName;
	}

	@Override
	public ControlType getControlType() {
		return controlType;
	}

	@Override
	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}

	@Override
	public ListModel getReferenceList() {
		return referenceList;
	}

	@Override
	public void setReferenceList(ListModel referenceList) {
		this.referenceList = referenceList;
	}

	@Override
	public String getLegend() {
		return legend;
	}

	@Override
	public void setLegend(String legend) {
		this.legend = legend;
	}

	@Override
	public Integer getRowCount() {
		return rowCount;
	}

	@Override
	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	@Override
	public Boolean getShowHead() {
		return showHead;
	}

	@Override
	public void setShowHead(Boolean showHead) {
		this.showHead = showHead;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public String getTimeFormat() {
		return timeFormat;
	}

	@Override
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	@Override
	public SystemCreateType getCreateType() {
		return createType;
	}

	@Override
	public void setCreateType(SystemCreateType createType) {
		this.createType = createType;
	}

	@Override
	public List<String> getItemModelList() {
		return itemModelList;
	}

	@Override
	public void setItemModelList(List<String> itemModelList) {
		this.itemModelList = itemModelList;
	}

	@Override
	public Boolean getSelectFlag() {
		return selectFlag;
	}

	@Override
	public void setSelectFlag(Boolean selectFlag) {
		this.selectFlag = selectFlag;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Integer getSuffix() {
		return suffix;
	}

	@Override
	public void setSuffix(Integer suffix) {
		this.suffix = suffix;
	}

	@Override
	public DictionaryValueType getDictionaryValueType() {
		return dictionaryValueType;
	}

	@Override
	public void setDictionaryValueType(DictionaryValueType dictionaryValueType) {
		this.dictionaryValueType = dictionaryValueType;
	}
}
