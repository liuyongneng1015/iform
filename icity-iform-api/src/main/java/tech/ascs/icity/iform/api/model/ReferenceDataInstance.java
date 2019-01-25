package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.IdEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("关联数据实例DataInstance")
public class ReferenceDataInstance extends IdEntity {
	@ApiModelProperty(value = "控件的数据", position = 3)
	private List<DataInstance> items = new ArrayList<>();

	@ApiModelProperty(value = "数据条数", position = 4)
	private Integer size;

	@ApiModelProperty(value = "关联类型", position = 5)
	private ReferenceType referenceType;

	@ApiModelProperty(value = " 关联表单模型", position = 6)
	private String referenceTable;
	@ApiModelProperty(value = " 关联字段模型（比如“id”）", position = 7)
	private String referenceValueColumn;

	public List<DataInstance> getItems() {
		return items;
	}

	public void setItems(List<DataInstance> items) {
		this.items = items;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
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

	@ApiModelProperty(value = "表单实例ID", position = 0)
	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
