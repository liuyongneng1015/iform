package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel("选择器控件模型")
public class Option implements Serializable {

	public Option() {
		super();
	}
	public Option(String id, String label, String value) {
		this.id = id;
		this.label = label;
		this.value = value;
	}
	@ApiModelProperty(value = "选择id", position = 1)
	private String id;

	@ApiModelProperty(value = "显示名称", position = 2)
	private String label;

	@ApiModelProperty(value = "值", position = 3)
	private String value;

	public String getId() {
		return id;
	}

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
}
