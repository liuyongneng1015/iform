package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.*;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 列表功能定义
 */
@Entity
@Table(name = "ifm_list_function")
public class ListFunction extends JPAEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name="list_id")
	private ListModelEntity listModel;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	//功能名
	private String label;

	//功能编码
	private String action;

	private boolean batch = false;

	//是否启用
	private boolean visible = true;

	private String url;


	private String method;

	@Column(name="order_no",columnDefinition = "int default 0")//排序号
	private Integer orderNo = 0;

	//显示时机 若为空标识所有时机都显示
	private String displayTiming;

	public ListModelEntity getListModel() {
		return listModel;
	}

	public void setListModel(ListModelEntity listModel) {
		this.listModel = listModel;
	}

	public FormModelEntity getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModelEntity formModel) {
		this.formModel = formModel;
	}

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
}