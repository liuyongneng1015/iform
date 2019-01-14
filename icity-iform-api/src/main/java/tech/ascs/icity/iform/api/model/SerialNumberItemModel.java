package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("表单流水号控件模型")
public class SerialNumberItemModel extends ItemModel {

	@ApiModelProperty(value = "数据字段模型", position = 5)
	private ColumnModelInfo columnModel;

	@ApiModelProperty(value = "控件类型", position = 3)
	private ItemType type;

	@ApiModelProperty(value = "前端个性化属性（直接存json字符串，后端不做处理）", position = 4)
	private String props;

	@ApiModelProperty(value = "隐藏条件", position = 8)
	private String hiddenCondition;

	@ApiModelProperty(value="系统控件类型", position = 29)
	private SystemItemType systemItemType;

	@ApiModelProperty(value = "流程环节配置", position = 6)
	private List<ActivityInfo> activities;

	@ApiModelProperty(value = "选项配置", position = 18)
	private List<Option> options;

	@ApiModelProperty(value="时间类型",position = 31)
	private String  timeFormat;

	@ApiModelProperty(value="前缀业务标识",position = 32)
	private String prefix;

	@ApiModelProperty(value="后缀自增长数字位数",position = 33)
	private Integer suffix;





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

	@ApiModelProperty(value="上级关联控件模型",position = 22)
	private ItemModel parentItem ;

	//subformrow/row
	@ApiModelProperty(value="当前行数",position = 23)
	private Integer rowNumber;
	@ApiModelProperty(value="组件子项（由组和字段构成） ",position = 26)
	private List<ItemModel> items = new ArrayList<ItemModel>();

	//TimeItemModel
	//TimeItemModel

	@ApiModelProperty(value="创建类型：Create创建时，Update更新时，Normal普通", position = 29)
	private SystemCreateType createType = SystemCreateType.Normal;

	@ApiModelProperty(value="数据标识:控件id集合",position = 30)
	private List<String> itemModelList = new ArrayList<>();
	@ApiModelProperty(value="是否被选中:true选中，flse未选中",position = 31)
	private Boolean  selectFlag = false;

	@ApiModelProperty(value="数据字典值类型",position = 34)
	private DictionaryValueType dictionaryValueType;

	@Override
	public ColumnModelInfo getColumnModel() {
		return columnModel;
	}

	@Override
	public void setColumnModel(ColumnModelInfo columnModel) {
		this.columnModel = columnModel;
	}

	@Override
	public ItemType getType() {
		return type;
	}

	@Override
	public void setType(ItemType type) {
		this.type = type;
	}

	@Override
	public String getProps() {
		return props;
	}

	@Override
	public void setProps(String props) {
		this.props = props;
	}

	@Override
	public String getHiddenCondition() {
		return hiddenCondition;
	}

	@Override
	public void setHiddenCondition(String hiddenCondition) {
		this.hiddenCondition = hiddenCondition;
	}

	@Override
	public SystemItemType getSystemItemType() {
		return systemItemType;
	}

	@Override
	public void setSystemItemType(SystemItemType systemItemType) {
		this.systemItemType = systemItemType;
	}

	@Override
	public List<ActivityInfo> getActivities() {
		return activities;
	}

	@Override
	public void setActivities(List<ActivityInfo> activities) {
		this.activities = activities;
	}

	@Override
	public List<Option> getOptions() {
		return options;
	}

	@Override
	public void setOptions(List<Option> options) {
		this.options = options;
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
	@JsonIgnore
	public FileReferenceType getFileReferenceType() {
		return fileReferenceType;
	}

	@Override
	public void setFileReferenceType(FileReferenceType fileReferenceType) {
		this.fileReferenceType = fileReferenceType;
	}

	@Override
	@JsonIgnore
	public Integer getFileSizeLimit() {
		return fileSizeLimit;
	}

	@Override
	public void setFileSizeLimit(Integer fileSizeLimit) {
		this.fileSizeLimit = fileSizeLimit;
	}

	@Override
	@JsonIgnore
	public ReferenceType getReferenceType() {
		return referenceType;
	}

	@Override
	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	@JsonIgnore
	public String getReferenceTableName() {
		return referenceTableName;
	}

	@Override
	public void setReferenceTableName(String referenceTableName) {
		this.referenceTableName = referenceTableName;
	}

	@Override
	@JsonIgnore
	public SelectReferenceType getSelectReferenceType() {
		return selectReferenceType;
	}

	@Override
	public void setSelectReferenceType(SelectReferenceType selectReferenceType) {
		this.selectReferenceType = selectReferenceType;
	}

	@Override
	@JsonIgnore
	public Boolean getMultiple() {
		return multiple;
	}

	@Override
	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	@Override
	@JsonIgnore
	public SelectMode getSelectMode() {
		return selectMode;
	}

	@Override
	public void setSelectMode(SelectMode selectMode) {
		this.selectMode = selectMode;
	}

	@Override
	@JsonIgnore
	public String getReferenceDictionaryId() {
		return referenceDictionaryId;
	}

	@Override
	public void setReferenceDictionaryId(String referenceDictionaryId) {
		this.referenceDictionaryId = referenceDictionaryId;
	}

	@Override
	@JsonIgnore
	public String getReferenceDictionaryName() {
		return referenceDictionaryName;
	}

	@Override
	public void setReferenceDictionaryName(String referenceDictionaryName) {
		this.referenceDictionaryName = referenceDictionaryName;
	}

	@Override
	@JsonIgnore
	public String getReferenceDictionaryItemId() {
		return referenceDictionaryItemId;
	}

	@Override
	public void setReferenceDictionaryItemId(String referenceDictionaryItemId) {
		this.referenceDictionaryItemId = referenceDictionaryItemId;
	}

	@Override
	@JsonIgnore
	public String getReferenceDictionaryItemName() {
		return referenceDictionaryItemName;
	}

	@Override
	public void setReferenceDictionaryItemName(String referenceDictionaryItemName) {
		this.referenceDictionaryItemName = referenceDictionaryItemName;
	}

	@Override
	@JsonIgnore
	public List<DictionaryItemModel> getReferenceDictionaryItemList() {
		return referenceDictionaryItemList;
	}

	@Override
	public void setReferenceDictionaryItemList(List<DictionaryItemModel> referenceDictionaryItemList) {
		this.referenceDictionaryItemList = referenceDictionaryItemList;
	}

	@Override
	@JsonIgnore
	public String getReferenceTable() {
		return referenceTable;
	}

	@Override
	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	@Override
	@JsonIgnore
	public String getReferenceValueColumn() {
		return referenceValueColumn;
	}

	@Override
	public void setReferenceValueColumn(String referenceValueColumn) {
		this.referenceValueColumn = referenceValueColumn;
	}

	@Override
	@JsonIgnore
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	@JsonIgnore
	public String getReferenceFormId() {
		return referenceFormId;
	}

	@Override
	public void setReferenceFormId(String referenceFormId) {
		this.referenceFormId = referenceFormId;
	}

	@Override
	@JsonIgnore
	public String getReferenceFormName() {
		return referenceFormName;
	}

	@Override
	public void setReferenceFormName(String referenceFormName) {
		this.referenceFormName = referenceFormName;
	}

	@Override
	@JsonIgnore
	public String getReferenceItemId() {
		return referenceItemId;
	}

	@Override
	public void setReferenceItemId(String referenceItemId) {
		this.referenceItemId = referenceItemId;
	}

	@Override
	@JsonIgnore
	public String getReferenceItemName() {
		return referenceItemName;
	}

	@Override
	public void setReferenceItemName(String referenceItemName) {
		this.referenceItemName = referenceItemName;
	}

	@Override
	@JsonIgnore
	public ControlType getControlType() {
		return controlType;
	}

	@Override
	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}

	@Override
	@JsonIgnore
	public ListModel getReferenceList() {
		return referenceList;
	}

	@Override
	public void setReferenceList(ListModel referenceList) {
		this.referenceList = referenceList;
	}

	@Override
	@JsonIgnore
	public String getLegend() {
		return legend;
	}

	@Override
	public void setLegend(String legend) {
		this.legend = legend;
	}

	@Override
	@JsonIgnore
	public Integer getRowCount() {
		return rowCount;
	}

	@Override
	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	@Override
	@JsonIgnore
	public Boolean getShowHead() {
		return showHead;
	}

	@Override
	public void setShowHead(Boolean showHead) {
		this.showHead = showHead;
	}

	@Override
	@JsonIgnore
	public String getTableName() {
		return tableName;
	}

	@Override
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	@JsonIgnore
	public ItemModel getParentItem() {
		return parentItem;
	}

	@Override
	public void setParentItem(ItemModel parentItem) {
		this.parentItem = parentItem;
	}

	@Override
	@JsonIgnore
	public Integer getRowNumber() {
		return rowNumber;
	}

	@Override
	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	@Override
	@JsonIgnore
	public List<ItemModel> getItems() {
		return items;
	}

	@Override
	public void setItems(List<ItemModel> items) {
		this.items = items;
	}

	@Override
	@JsonIgnore
	public SystemCreateType getCreateType() {
		return createType;
	}

	@Override
	public void setCreateType(SystemCreateType createType) {
		this.createType = createType;
	}

	@Override
	@JsonIgnore
	public List<String> getItemModelList() {
		return itemModelList;
	}

	@Override
	public void setItemModelList(List<String> itemModelList) {
		this.itemModelList = itemModelList;
	}

	@Override
	@JsonIgnore
	public Boolean getSelectFlag() {
		return selectFlag;
	}

	@Override
	public void setSelectFlag(Boolean selectFlag) {
		this.selectFlag = selectFlag;
	}

	@Override
	@JsonIgnore
	public DictionaryValueType getDictionaryValueType() {
		return dictionaryValueType;
	}

	@Override
	public void setDictionaryValueType(DictionaryValueType dictionaryValueType) {
		this.dictionaryValueType = dictionaryValueType;
	}

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
