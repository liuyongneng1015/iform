package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.io.Serializable;

/**
 * 业务触发类型
 * 
 * @author Jackie
 *
 */
public class BusinessTriggerModel extends NameEntity {

	@ApiModelProperty(value = "排序号", position = 3)//
	private Integer orderNo = 0;

	@ApiModelProperty(value = "控件类型", position = 3)
	private BusinessTriggerType type;

	@ApiModelProperty(value = "调用微服务地址", position = 4)
	private String url ;

	@ApiModelProperty(value = "请求方式，GET、HEAD、POST、PUT、DELETE、CONNECT、OPTIONS、TRACE", position = 5)
	private String method;

	@ApiModelProperty(value = "参数类型", position = 6)
	private ParamCondition paramCondition;

	@ApiModelProperty(value = "返回结果", position = 7)
	private ReturnResult returnResult;

	@ApiModelProperty(value = "备注", position = 8)
	private String remark ;

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public BusinessTriggerType getType() {
		return type;
	}

	public void setType(BusinessTriggerType type) {
		this.type = type;
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

	public ParamCondition getParamCondition() {
		return paramCondition;
	}

	public void setParamCondition(ParamCondition paramCondition) {
		this.paramCondition = paramCondition;
	}

	public ReturnResult getReturnResult() {
		return returnResult;
	}

	public void setReturnResult(ReturnResult returnResult) {
		this.returnResult = returnResult;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
