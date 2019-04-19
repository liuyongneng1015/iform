package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.*;

import tech.ascs.icity.iform.api.model.*;
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

	// 请求方式，GET、HEAD、POST、PUT、DELETE、CONNECT、OPTIONS、TRACE
	private String method;

	private String icon;

	@Column(name="style", length=4096)
	private String style;

	@Enumerated(EnumType.STRING)
	private ParamCondition paramCondition;

	@Enumerated(EnumType.STRING)
	private ListFunctionType functionType;

	private String confirmForm;

	@Enumerated(EnumType.STRING)
	private ReturnOperation returnOperation;

	private String jumpNewUrl;

	private Boolean listActionBarVisible;

	private Boolean checkPageVisible;

	private Boolean addPageVisible;

	private Boolean updatePageVisible;

	@Column(name="order_no",columnDefinition = "int default 0")//排序号
	private Integer orderNo = 0;

	//显示时机 若为空标识所有时机都显示
	@JoinColumn(name="display_timing_type")
	@Enumerated(EnumType.STRING)
	private DisplayTimingType displayTiming;

	// 是否是系统的按钮
	private Boolean systemBtn;

	//设备类型
	@JoinColumn(name = "device_type")
	@Enumerated(EnumType.STRING)
	private DeviceType deviceType;

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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public ParamCondition getParamCondition() {
		return paramCondition;
	}

	public void setParamCondition(ParamCondition paramCondition) {
		this.paramCondition = paramCondition;
	}

	public ListFunctionType getFunctionType() {
		return functionType;
	}

	public void setFunctionType(ListFunctionType functionType) {
		this.functionType = functionType;
	}

	public String getConfirmForm() {
		return confirmForm;
	}

	public void setConfirmForm(String confirmForm) {
		this.confirmForm = confirmForm;
	}

	public ReturnOperation getReturnOperation() {
		return returnOperation;
	}

	public void setReturnOperation(ReturnOperation returnOperation) {
		this.returnOperation = returnOperation;
	}

	public String getJumpNewUrl() {
		return jumpNewUrl;
	}

	public void setJumpNewUrl(String jumpNewUrl) {
		this.jumpNewUrl = jumpNewUrl;
	}

	public Boolean getListActionBarVisible() {
		return listActionBarVisible;
	}

	public void setListActionBarVisible(Boolean listActionBarVisible) {
		this.listActionBarVisible = listActionBarVisible;
	}

	public Boolean getCheckPageVisible() {
		return checkPageVisible;
	}

	public void setCheckPageVisible(Boolean checkPageVisible) {
		this.checkPageVisible = checkPageVisible;
	}

	public Boolean getAddPageVisible() {
		return addPageVisible;
	}

	public void setAddPageVisible(Boolean addPageVisible) {
		this.addPageVisible = addPageVisible;
	}

	public Boolean getUpdatePageVisible() {
		return updatePageVisible;
	}

	public void setUpdatePageVisible(Boolean updatePageVisible) {
		this.updatePageVisible = updatePageVisible;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public DisplayTimingType getDisplayTiming() {
		return displayTiming;
	}

	public void setDisplayTiming(DisplayTimingType displayTiming) {
		this.displayTiming = displayTiming;
	}

	public Boolean getSystemBtn() {
		return systemBtn;
	}

	public void setSystemBtn(Boolean systemBtn) {
		this.systemBtn = systemBtn;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
}