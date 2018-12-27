package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.SystemCreateType;

import javax.persistence.*;

/**
 * 创建人控件表
 */
@Entity
@Table(name = "ifm_creator_item_model")
@DiscriminatorValue("creatorItemModel")
public class CreatorItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="create_type")//创建类型
	@Enumerated(EnumType.STRING)
	private SystemCreateType createType = SystemCreateType.Normal;

	public SystemCreateType getCreateType() {
		return createType;
	}

	public void setCreateType(SystemCreateType createType) {
		this.createType = createType;
	}
}