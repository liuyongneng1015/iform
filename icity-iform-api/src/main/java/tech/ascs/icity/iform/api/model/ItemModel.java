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
	private List<ActivityInfo> activities;

	//FileItemModel
	@ApiModelProperty(value = "文件类型", position = 7)
	private FileReferenceType fileReferenceType = FileReferenceType.Attachment;

	@ApiModelProperty(value = "文件大小限制M", position = 7)
	private Integer fileSizeLimit = 10;

	@ApiModelProperty(value = "隐藏条件", position = 8)
	private String hiddenCondition;

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
	@ApiModelProperty(value = "默认值名称(数据字典的默认值)", position = 15)
	private Object defaultValueName;

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

	@ApiModelProperty(value = "关联列表模型Id",position = 17)
	private String referenceListId;

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

	@ApiModelProperty(value="上级关联控件模型id",position = 22)
	private String parentItemId ;

	//subformrow/row
	@ApiModelProperty(value="当前行数",position = 23)
	private Integer rowNumber;
	@ApiModelProperty(value="组件子项（由组和字段构成） ",position = 26)
	private List<ItemModel> items;

	//TimeItemModel
	@ApiModelProperty(value="时间格式如yyyy-MM-dd",position = 27)//时间格式
	private String timeFormat;

	//TimeItemModel
	@ApiModelProperty(value="系统控件类型", position = 29)
	private SystemItemType systemItemType;

	@ApiModelProperty(value="创建类型：Create创建时，Update更新时，Normal普通", position = 29)
	private SystemCreateType createType = SystemCreateType.Normal;

	@ApiModelProperty(value="数据标识:控件集合",position = 30)
	private List<ItemModel> itemModelList = new ArrayList<ItemModel>();
	@ApiModelProperty(value="是否被选中:true选中，flse未选中",position = 31)
	private Boolean  selectFlag = false;

	@ApiModelProperty(value="新增控件权限",position = 31)
	private ItemPermissionModel  permissions;

	@ApiModelProperty(value="前缀业务标识",position = 32)
	private String prefix;

	@ApiModelProperty(value="后缀自增长数字位数",position = 33)
	private Integer suffix;

	@ApiModelProperty(value="数据字典值类型",position = 34)
	private DictionaryValueType dictionaryValueType;

	@ApiModelProperty(value = "字段名", position = 35)
	private String columnName;

	@ApiModelProperty(value = "控件表名", position = 36)
	private String itemTableName;

	@ApiModelProperty(value = "控件字段名", position = 37)
	private String itemColunmName;

	@ApiModelProperty(value = "控件标识", position = 38)
	private String uuid;

	@ApiModelProperty(value = "关联控件标识", position = 39)
	private String referenceUuid;

	@ApiModelProperty(value = "下拉数据来源", position = 40)
	private TreeSelectDataSource dataSource;


	@ApiModelProperty(value = "是否唯一", position = 41)
	private Boolean uniquene = false;

	@ApiModelProperty(value = "自动计算", position = 42)
	private String calculationFormula;

	@ApiModelProperty(value = "数字位数", position = 43)
	private Integer decimalDigits;

	@ApiModelProperty(value = "千分位分隔符", position = 44)
	private Boolean thousandSeparator;

	@ApiModelProperty(value = "后缀单位", position = 45)
	private String suffixUnit;

	@ApiModelProperty(value = "数据范围", position = 46)
	private String dataRange;

	@ApiModelProperty(value = "数据深度", position = 47)
	private Integer dataDepth;

	@ApiModelProperty(value = "数据范围名称", position = 48)
	private String dataRangeName;

	@ApiModelProperty(value = "经度", position = 49)
	private String longitude;

	@ApiModelProperty(value = "纬度", position = 50)
	private String latitude;

	@ApiModelProperty(value = "地图描述", position = 51)
	private String mapDesc;

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

	public Integer getFileSizeLimit() {
		return fileSizeLimit;
	}

	public void setFileSizeLimit(Integer fileSizeLimit) {
		this.fileSizeLimit = fileSizeLimit;
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

	public String getParentItemId() {
		return parentItemId;
	}

	public void setParentItemId(String parentItemId) {
		this.parentItemId = parentItemId;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
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

	public String getReferenceDictionaryItemName() {
		return referenceDictionaryItemName;
	}

	public void setReferenceDictionaryItemName(String referenceDictionaryItemName) {
		this.referenceDictionaryItemName = referenceDictionaryItemName;
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

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Object getDefaultValueName() {
		return defaultValueName;
	}

	public void setDefaultValueName(Object defaultValueName) {
		this.defaultValueName = defaultValueName;
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

	public ItemPermissionModel getPermissions() {
		return permissions;
	}

	public void setPermissions(ItemPermissionModel permissions) {
		this.permissions = permissions;
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

	public String getReferenceListId() {
		return referenceListId;
	}

	public void setReferenceListId(String referenceListId) {
		this.referenceListId = referenceListId;
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

	public SystemCreateType getCreateType() {
		return createType;
	}

	public void setCreateType(SystemCreateType createType) {
		this.createType = createType;
	}

	public List<ItemModel> getItemModelList() {
		return itemModelList;
	}

	public void setItemModelList(List<ItemModel> itemModelList) {
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

	public String getItemTableName() {
		return itemTableName;
	}

	public void setItemTableName(String itemTableName) {
		this.itemTableName = itemTableName;
	}

	public String getItemColunmName() {
		return itemColunmName;
	}

	public void setItemColunmName(String itemColunmName) {
		this.itemColunmName = itemColunmName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getReferenceUuid() {
		return referenceUuid;
	}

	public void setReferenceUuid(String referenceUuid) {
		this.referenceUuid = referenceUuid;
	}

	public TreeSelectDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(TreeSelectDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Boolean getUniquene() {
		return uniquene;
	}

	public void setUniquene(Boolean uniquene) {
		this.uniquene = uniquene;
	}

	public String getCalculationFormula() {
		return calculationFormula;
	}

	public void setCalculationFormula(String calculationFormula) {
		this.calculationFormula = calculationFormula;
	}

	public Integer getDecimalDigits() {
		return decimalDigits;
	}

	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public Boolean getThousandSeparator() {
		return thousandSeparator;
	}

	public void setThousandSeparator(Boolean thousandSeparator) {
		this.thousandSeparator = thousandSeparator;
	}

	public String getSuffixUnit() {
		return suffixUnit;
	}

	public void setSuffixUnit(String suffixUnit) {
		this.suffixUnit = suffixUnit;
	}

	public String getDataRange() {
		return dataRange;
	}

	public void setDataRange(String dataRange) {
		this.dataRange = dataRange;
	}

	public Integer getDataDepth() {
		return dataDepth;
	}

	public void setDataDepth(Integer dataDepth) {
		this.dataDepth = dataDepth;
	}

	public String getDataRangeName() {
		return dataRangeName;
	}

	public void setDataRangeName(String dataRangeName) {
		this.dataRangeName = dataRangeName;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getMapDesc() {
		return mapDesc;
	}

	public void setMapDesc(String mapDesc) {
		this.mapDesc = mapDesc;
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
