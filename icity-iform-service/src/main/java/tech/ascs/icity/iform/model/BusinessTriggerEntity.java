package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 业务触发实体
 */
@Entity
@Table(name = "ifm_business_trigger")
public class BusinessTriggerEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.REFRESH})
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	@Column(name="order_no",columnDefinition = "int default 0")//排序号
	private Integer orderNo = 0;

	@JoinColumn(name="type")
	@Enumerated(EnumType.STRING)
	private BusinessTriggerType type;

	@Column(name="url")//调用微服务地址
	private String url ;

	// 请求方式，GET、HEAD、POST、PUT、DELETE、CONNECT、OPTIONS、TRACE
	private String method;

	@JoinColumn(name="param_condition")
	@Enumerated(EnumType.STRING)
	private ParamCondition paramCondition;

	// 返回结果
	@JoinColumn(name="return_result")
	@Enumerated(EnumType.STRING)
	private ReturnResult returnResult;

	@Column(name="remark")//备注
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

	public FormModelEntity getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModelEntity formModel) {
		this.formModel = formModel;
	}
}