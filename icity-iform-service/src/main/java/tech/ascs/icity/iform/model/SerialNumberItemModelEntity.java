package tech.ascs.icity.iform.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * 流水号表单控件模型
 */
@Entity
@Table(name = "ifm_serial_number_item_model")
@DiscriminatorValue("serialNumberItemModel")
public class SerialNumberItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@JoinColumn(name="time_format")//时间格式如（“yyyy-MM-dd”）
	private String timeFormat;

	@JoinColumn(name="prefix")//前缀业务标识
	private String prefix;

	@JoinColumn(name="suffix")//后缀自增长数字位数
	private Integer suffix;

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
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
}