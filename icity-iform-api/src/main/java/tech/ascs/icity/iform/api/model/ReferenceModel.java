package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("关联字段模型")
public class ReferenceModel extends NameEntity {

	private static final long serialVersionUID = 1L;

	public static ReferenceType getToReferenceType(ReferenceType referenceType) {
		if(ReferenceType.OneToMany == referenceType){
			return ReferenceType.ManyToOne;
		}else if(ReferenceType.ManyToOne == referenceType){
			return ReferenceType.OneToMany;
		}else{
			return referenceType;
		}
	}

	@ApiModelProperty(value = "关联关系", position = 3)
	private ReferenceType referenceType;

	@ApiModelProperty(value = "目标", position = 4)
	private ColumnModel toColumn = new ColumnModel();

	public ColumnModel getToColumn() {
		return toColumn;
	}

	public void setToColumn(ColumnModel toColumn) {
		this.toColumn = toColumn;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}
}
