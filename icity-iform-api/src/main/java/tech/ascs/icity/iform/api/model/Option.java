package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel("选择器控件模型")
public class Option implements Serializable {

	public Option() {
		super();
	}
	public Option(String id, String label, String value, Boolean defaultFlag) {
		this.id = id;
		this.label = label;
		this.value = value;
		this.defaultFlag = defaultFlag;
	}
	@ApiModelProperty(value = "选择id", position = 1)
	private String id;

	@ApiModelProperty(value = "显示名称", position = 2)
	private String label;

	@ApiModelProperty(value = "值", position = 3)
	private String value;

	@ApiModelProperty(value = "是否默认标识", position = 4)
	private Boolean defaultFlag = false;

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getDefaultFlag() {
		if(defaultFlag == null){
			defaultFlag = false;
		}
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public String getId() {
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
