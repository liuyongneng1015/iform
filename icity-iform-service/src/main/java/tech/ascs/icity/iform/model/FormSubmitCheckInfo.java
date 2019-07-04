package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 校验规则
 */
@Entity
@Table(name = "ifm_form_submit_checks")
public class FormSubmitCheckInfo extends BaseEntity implements Serializable, Comparable<FormSubmitCheckInfo> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	//校验失败提示
	private String cueWords;

	//校验规则
	private String cueExpression;

	//排序号
	private Integer orderNo = 0;

	public FormModelEntity getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModelEntity formModel) {
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
	public int compareTo(FormSubmitCheckInfo o) {
		return this.getOrderNo() - o.getOrderNo();
	}
}