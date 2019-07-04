package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("表单模型提交校验")
public class FormSubmitCheckModel extends NameEntity {

	@ApiModelProperty(value = "表单模型", position = 3)
	private FormModel formModel;


	@ApiModelProperty(value = "提示语", position = 4)
	private String cueWords;

	//校验规则
	@ApiModelProperty(value = "校验表达式", position = 5)
	private String cueExpression;

	@ApiModelProperty(value = "排序号", position = 6)
	private Integer orderNo = 0;


	public FormModel getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModel formModel) {
		this.formModel = formModel;
	}

	public String getCueWords() {
		return cueWords;
	}

	public void setCueWords(String cueWords) {
		this.cueWords = cueWords;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public String getCueExpression() {
		return cueExpression;
	}

	public void setCueExpression(String cueExpression) {
		this.cueExpression = cueExpression;
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
