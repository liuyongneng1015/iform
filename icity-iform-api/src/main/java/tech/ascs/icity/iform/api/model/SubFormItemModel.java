package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@ApiModel("子表单控件模型ItemModel")
public class SubFormItemModel extends BaseItemModel {

	@ApiModelProperty(value="头部标签",position = 11)
	private String legend;
	@ApiModelProperty(value=" 控件行数",position = 12)
	private Integer rowCount;
	@ApiModelProperty(value="是否显示表头",position = 13)
	private Boolean showHead;
	@ApiModelProperty(value="表名",position = 14)
	private String tableName;
	@ApiModelProperty(value="组件子项（由组和字段构成） ",position = 15)
	private List<SubFormRowItemModel> items = new ArrayList<SubFormRowItemModel>();

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public Boolean getShowHead() {
		return showHead;
	}

	public void setShowHead(Boolean showHead) {
		this.showHead = showHead;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<SubFormRowItemModel> getItems() {
		return items;
	}

	public void setItems(List<SubFormRowItemModel> items) {
		this.items = items;
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
