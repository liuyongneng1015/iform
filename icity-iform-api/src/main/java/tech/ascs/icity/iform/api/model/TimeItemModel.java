package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("时间控件模型ItemModel")
public class TimeItemModel extends BaseItemModel {

	@ApiModelProperty(value="time_format")//时间格式
	private String timeFormat;

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}
}
