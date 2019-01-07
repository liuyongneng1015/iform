package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.ItemType;
import tech.ascs.icity.iform.api.model.SystemCreateType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间表单控件模型
 */
@Entity
@Table(name = "ifm_time_item_model")
@DiscriminatorValue("timeItemModel")
public class TimeItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@JoinColumn(name="time_format")//时间格式如（“yyyy-MM-dd”）
	private String timeFormat = "yyyy-MM-dd HH:mm:ss";

	@Column(name="create_type")//创建类型
	@Enumerated(EnumType.STRING)
	private SystemCreateType createType = SystemCreateType.Normal;

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public SystemCreateType getCreateType() {
		return createType;
	}

	public void setCreateType(SystemCreateType createType) {
		this.createType = createType;
	}
}