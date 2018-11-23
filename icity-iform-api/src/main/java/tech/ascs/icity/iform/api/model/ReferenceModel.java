package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("关联字段模型")
public class ReferenceModel extends NameEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "关联关系", position = 3)
	private ReferenceType referenceType = ReferenceType.ManyToOne;

	@ApiModelProperty(value = "关联表", position = 3)
	private String referenceTable;

	@ApiModelProperty(value = "关联值字段（比如“id”）", position = 3)
	private String referenceValueColumn = "id";

	public ReferenceType getReferenceType() {
		if(referenceType == null){
			referenceType = ReferenceType.ManyToOne;
		}
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
		if(!StringUtils.isEmpty(this.referenceValueColumn)){
			referenceValueColumn = "id";
		}
		return referenceValueColumn;
	}

	public void setReferenceValueColumn(String referenceValueColumn) {
		this.referenceValueColumn = referenceValueColumn;
	}
}
