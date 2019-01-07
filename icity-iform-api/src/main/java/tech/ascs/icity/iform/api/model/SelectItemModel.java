package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("表单下拉控件模型")
public class SelectItemModel extends ItemModel {

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

	//SubFormItemModel
	@ApiModelProperty(value="头部标签",position = 19)
	private String legend;
	@ApiModelProperty(value=" 控件行数",position = 20)
	private Integer rowCount;
	@ApiModelProperty(value="是否显示表头",position = 21)
	private Boolean showHead;
	@ApiModelProperty(value="表名",position = 22)
	private String tableName;


	//subformrow/row
	@ApiModelProperty(value="当前行数",position = 23)
	private Integer rowNumber;

	//TimeItemModel
	@ApiModelProperty(value="时间格式如yyyy-MM-dd",position = 27)//时间格式
	private String timeFormat;

	//TimeItemModel

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
	public Integer getRowNumber() {
		return rowNumber;
	}

	@Override
	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	@Override
	@JsonIgnore
	public String getTimeFormat() {
		return timeFormat;
	}

	@Override
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
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
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	@JsonIgnore
	public Integer getSuffix() {
		return suffix;
	}

	@Override
	public void setSuffix(Integer suffix) {
		this.suffix = suffix;
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
