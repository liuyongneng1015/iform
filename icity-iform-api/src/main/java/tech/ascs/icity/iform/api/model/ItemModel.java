package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("表单控件模型ItemModel")
public class ItemModel extends NameEntity {

	@ApiModelProperty(value = "控件类型", position = 3)
	private ItemType type;

	@ApiModelProperty(value = "前端个性化属性（直接存json字符串，后端不做处理）", position = 4)
	private String props;

	@ApiModelProperty(value = "数据字段模型", position = 5)
	private ColumnModelInfo columnModel;

	@ApiModelProperty(value = "流程环节配置", position = 6)
	private List<ActivityInfo> activities = new ArrayList<ActivityInfo>();

	//FileItemModel
	@ApiModelProperty(value = "文件类型", position = 7)
	private FileReferenceType fileReferenceType;
	@ApiModelProperty(value = "文件路径", position = 8)
	private String filePath;

	@ApiModelProperty(value = "隐藏条件", position = 8)
	private String hiddenCondition;

	//File/selectItemModel
	@ApiModelProperty(value = "关联类型", position = 9)
	private ReferenceType referenceType;
	@ApiModelProperty(value = "多对多创建表的名称", position = 10)
	private String referenceTableName;
	@ApiModelProperty(value="选择关系",position = 11)
	private SelectReferenceType selectReferenceType;
	@ApiModelProperty(value="是否多选",position = 11)
	private Boolean multiple;
	@ApiModelProperty(value="单选、多选、反选",position = 12)
	private SelectMode selectMode;
	@ApiModelProperty(value = "关联字典分类ID", position = 13)
	private String referenceDictionaryId;
	@ApiModelProperty(value = "关联字典分类名称", position = 13)
	private String referenceDictionaryName;

	@ApiModelProperty(value = " 关联字典联动目标id", position = 13)
	private String referenceDictionaryItemId;
	@ApiModelProperty(value = " 关联字典联动目标", position = 13)
	private List<DictionaryItemModel> referenceDictionaryItemList = new ArrayList<>();

	@ApiModelProperty(value = " 关联表单(如表名、表单名)", position = 14)
	private String referenceTable;
	@ApiModelProperty(value = " 关联字段模型（比如字段、控件名）", position = 15)
	private String referenceValueColumn;
	@ApiModelProperty(value = "默认值(数据字典的默认值)", position = 15)
	private List<String> defaultValue = new ArrayList<>();

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
	@ApiModelProperty(value = "选项配置", position = 18)
	private List<Option> options = new ArrayList<Option>();

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
	@ApiModelProperty(value="时间格式如yyyy-MM-dd",position = 27)//时间格式
	private String timeFormat;

	//TimeItemModel
	@ApiModelProperty(value="系统控件类型", position = 29)
	private SystemItemType systemItemType;


	@ApiModelProperty(value="数据标识:控件id集合",position = 30)
	private List<String> itemModelList = new ArrayList<>();
	@ApiModelProperty(value="是否被选中:true选中，flse未选中",position = 31)
	private Boolean  selectFlag = false;

	@ApiModelProperty(value="控件权限",position = 31)
	private ItemPermissionModel  permission;

	@ApiModelProperty(value="前缀业务标识",position = 32)
	private String prefix;

	@ApiModelProperty(value="后缀自增长数字位数",position = 33)
	private Integer suffix;

	@ApiModelProperty(value="数据字典值类型",position = 34)
	private DictionaryValueType dictionaryValueType;


	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public ColumnModelInfo getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(ColumnModelInfo columnModel) {
		this.columnModel = columnModel;
	}

	public List<ActivityInfo> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityInfo> activities) {
		this.activities = activities;
	}

	public FileReferenceType getFileReferenceType() {
		return fileReferenceType;
	}

	public void setFileReferenceType(FileReferenceType fileReferenceType) {
		this.fileReferenceType = fileReferenceType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getHiddenCondition() {
		return hiddenCondition;
	}

	public void setHiddenCondition(String hiddenCondition) {
		this.hiddenCondition = hiddenCondition;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public String getReferenceTableName() {
		return referenceTableName;
	}

	public void setReferenceTableName(String referenceTableName) {
		this.referenceTableName = referenceTableName;
	}

	public ItemModel getParentItem() {
		return parentItem;
	}

	public void setParentItem(ItemModel parentItem) {
		this.parentItem = parentItem;
	}

	public SelectReferenceType getSelectReferenceType() {
		return selectReferenceType;
	}

	public void setSelectReferenceType(SelectReferenceType selectReferenceType) {
		this.selectReferenceType = selectReferenceType;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public SelectMode getSelectMode() {
		return selectMode;
	}

	public void setSelectMode(SelectMode selectMode) {
		this.selectMode = selectMode;
	}

	public String getReferenceDictionaryId() {
		return referenceDictionaryId;
	}

	public void setReferenceDictionaryId(String referenceDictionaryId) {
		this.referenceDictionaryId = referenceDictionaryId;
	}

	public String getReferenceDictionaryName() {
		return referenceDictionaryName;
	}

	public void setReferenceDictionaryName(String referenceDictionaryName) {
		this.referenceDictionaryName = referenceDictionaryName;
	}

	public List<DictionaryItemModel> getReferenceDictionaryItemList() {
		return referenceDictionaryItemList;
	}

	public void setReferenceDictionaryItemList(List<DictionaryItemModel> referenceDictionaryItemList) {
		this.referenceDictionaryItemList = referenceDictionaryItemList;
	}

	public String getReferenceDictionaryItemId() {
		return referenceDictionaryItemId;
	}

	public void setReferenceDictionaryItemId(String referenceDictionaryItemId) {
		this.referenceDictionaryItemId = referenceDictionaryItemId;
	}

	public String getReferenceTable() {
		return referenceTable;
	}

	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}

	public String getReferenceValueColumn() {
		return referenceValueColumn;
	}

	public void setReferenceValueColumn(String referenceValueColumn) {
		this.referenceValueColumn = referenceValueColumn;
	}

	public List<String> getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(List<String> defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getReferenceFormId() {
		return referenceFormId;
	}

	public void setReferenceFormId(String referenceFormId) {
		this.referenceFormId = referenceFormId;
	}

	public String getReferenceItemId() {
		return referenceItemId;
	}

	public String getReferenceFormName() {
		return referenceFormName;
	}

	public void setReferenceFormName(String referenceFormName) {
		this.referenceFormName = referenceFormName;
	}

	public String getReferenceItemName() {
		return referenceItemName;
	}

	public void setReferenceItemName(String referenceItemName) {
		this.referenceItemName = referenceItemName;
	}

	public void setReferenceItemId(String referenceItemId) {
		this.referenceItemId = referenceItemId;
	}

	public ItemPermissionModel getPermission() {
		return permission;
	}

	public void setPermission(ItemPermissionModel permission) {
		this.permission = permission;
	}

	public ControlType getControlType() {
		return controlType;
	}

	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}

	public ListModel getReferenceList() {
		return referenceList;
	}

	public void setReferenceList(ListModel referenceList) {
		this.referenceList = referenceList;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public Boolean getShowHead() {
		return showHead;
	}

	public void setShowHead(Boolean showHead) {
		this.showHead = showHead;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(List<ItemModel> items) {
		this.items = items;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public SystemItemType getSystemItemType() {
		return systemItemType;
	}

	public void setSystemItemType(SystemItemType systemItemType) {
		this.systemItemType = systemItemType;
	}

	public List<String> getItemModelList() {
		return itemModelList;
	}

	public void setItemModelList(List<String> itemModelList) {
		this.itemModelList = itemModelList;
	}

	public Boolean getSelectFlag() {
		return selectFlag;
	}

	public void setSelectFlag(Boolean selectFlag) {
		this.selectFlag = selectFlag;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Integer getSuffix() {
		return suffix;
	}

	public void setSuffix(Integer suffix) {
		this.suffix = suffix;
	}

	public DictionaryValueType getDictionaryValueType() {
		return dictionaryValueType;
	}

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
