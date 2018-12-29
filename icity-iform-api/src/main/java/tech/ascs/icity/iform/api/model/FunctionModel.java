package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("功能模型")
public class FunctionModel extends NameEntity {

	@ApiModelProperty(value = "显示名称，如“新增”、“删除”等等", position = 0)
	private String label;
	@ApiModelProperty(value = "操作，由后端提供可供选择的操作列表，现在支持操作有：add、edit、delete", position = 1)
	private String action;
	@ApiModelProperty(value = "是否批量操作", position = 2)
	private boolean batch;
	@ApiModelProperty(value = "是否显示", position = 3)
	private boolean visible;
	@ApiModelProperty(value = "功能URL", position = 4)
	private String url;
	@ApiModelProperty(value = "URL请求方式，GET、POST、PUT、DELETE等等", position = 5)
	private String method;
	@ApiModelProperty(value ="排序号", position = 6)
	private Integer orderNo = 0;
	@ApiModelProperty(value ="显示时机", position = 6)
	private String displayTiming;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isBatch() {
		return batch;
	}

	public void setBatch(boolean batch) {
		this.batch = batch;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public String getDisplayTiming() {
		return displayTiming;
	}

	public void setDisplayTiming(String displayTiming) {
		this.displayTiming = displayTiming;
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
