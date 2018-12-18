package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 校验规则
 */
@Entity
@Table(name = "ifm_form_submit_checks")
public class FormSubmitCheckInfo extends JPAEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	//提示语
	private String cueWords;

	//排序号
	private Integer orderNo;

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
}