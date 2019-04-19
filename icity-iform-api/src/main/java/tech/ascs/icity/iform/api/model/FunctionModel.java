package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("功能模型")
public class FunctionModel extends NameEntity implements Comparable<FunctionModel> {

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
	@ApiModelProperty(value ="显示时机", position = 7)
	private DisplayTimingType displayTiming;
    @ApiModelProperty(value ="按钮icon图标", position = 8)
    private String icon;
	@ApiModelProperty(value ="按钮样式", position = 9)
	private String style;
	@ApiModelProperty(value ="参数条件", position = 10)
	private ParamCondition paramCondition;
	@ApiModelProperty(value ="功能类型", position = 11)
	private ListFunctionType functionType;
	@ApiModelProperty(value ="确认框", position = 12)
	private String confirmForm;
	@ApiModelProperty(value ="返回操作", position = 13)
	private ReturnOperation returnOperation;
	@ApiModelProperty(value ="跳转新url", position = 14)
	private String jumpNewUrl;
	@ApiModelProperty(value ="列表操作栏", position = 15)
	private Boolean listActionBarVisible = false;
	@ApiModelProperty(value ="查看页", position = 16)
	private Boolean checkPageVisible = false;
	@ApiModelProperty(value ="新增页", position = 17)
	private Boolean addPageVisible = false;
	@ApiModelProperty(value ="编辑页", position = 18)
	private Boolean updatePageVisible = false;
	// 是否是系统的按钮
	private Boolean systemBtn = false;

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

	public DisplayTimingType getDisplayTiming() {
		return displayTiming;
	}

	public void setDisplayTiming(DisplayTimingType displayTiming) {
		this.displayTiming = displayTiming;
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

	public Boolean getSystemBtn() {
		return systemBtn;
	}

	public void setSystemBtn(Boolean systemBtn) {
		this.systemBtn = systemBtn;
	}

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}

	@Override
	public int compareTo(FunctionModel o) {
		if (this.getOrderNo() == null && o.getOrderNo() == null) {
			return 0;
		}
		if (this.getOrderNo() == null) {
			return 1;
		}
		if (o.getOrderNo() == null) {
			return -1;
		}
		return this.getOrderNo() - o.getOrderNo();
	}
}
