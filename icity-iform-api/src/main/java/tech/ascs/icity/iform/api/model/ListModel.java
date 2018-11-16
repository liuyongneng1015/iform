package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("列表模型ListModel")
public class ListModel extends NameEntity {

	@ApiModel("表单模型摘要信息")
	public static class FormModelInfo extends FormModel {
		@JsonIgnore
		@Override
		public List<DataModel> getDataModels() {
			return super.getDataModels();
		}
		@JsonIgnore
		@Override
		public ProceeeModel getProcess() {
			return super.getProcess();
		}
		@JsonIgnore
		@Override
		public List<ItemModel> getItems() {
			return super.getItems();
		}
	}

	@ApiModel("列表排序字段摘要信息")
	public static class SortItem extends NameEntity {
		@ApiModelProperty(value = "是否正序", position = 2)
		private boolean asc;
		public boolean isAsc() {
			return asc;
		}
		public void setAsc(boolean asc) {
			this.asc = asc;
		}
	}

	public static class Function {
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
	}

	@ApiModel("列表显示字段摘要信息")
	public static class DisplayItem extends NameEntity {
	}

	@ApiModelProperty(value = "描述", position = 2)
	private String description;

	@ApiModelProperty(value = "是否支持多选", position = 3)
	private boolean multiSelect;

	@ApiModelProperty(value = "主表单模型", position = 4)
	private FormModelInfo masterForm;

	@ApiModelProperty(value = "附加表单模型列表", position = 5)
	private List<FormModelInfo> slaverForms = new ArrayList<FormModelInfo>();

	@ApiModelProperty(value = "排序字段列表", position = 6)
	private List<SortItem> sortItems = new ArrayList<SortItem>();

	@ApiModelProperty(value = "功能列表", position = 7)
	private List<Function> functions = new ArrayList<Function>();

	@ApiModelProperty(value = "查询字段列表", position = 8)
	private List<SearchItem> searchItems = new ArrayList<SearchItem>();

	@ApiModelProperty(value = "显示字段列表", position = 9)
	private List<DisplayItem> displayItems = new ArrayList<DisplayItem>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isMultiSelect() {
		return multiSelect;
	}

	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public FormModelInfo getMasterForm() {
		return masterForm;
	}

	public void setMasterForm(FormModelInfo masterForm) {
		this.masterForm = masterForm;
	}

	public List<FormModelInfo> getSlaverForms() {
		return slaverForms;
	}

	public void setSlaverForms(List<FormModelInfo> slaverForms) {
		this.slaverForms = slaverForms;
	}

	public List<SortItem> getSortItems() {
		return sortItems;
	}

	public void setSortItems(List<SortItem> sortItems) {
		this.sortItems = sortItems;
	}

	public List<Function> getFunctions() {
		return functions;
	}

	public void setFunctions(List<Function> functions) {
		this.functions = functions;
	}

	public List<SearchItem> getSearchItems() {
		return searchItems;
	}

	public void setSearchItems(List<SearchItem> searchItems) {
		this.searchItems = searchItems;
	}

	public List<DisplayItem> getDisplayItems() {
		return displayItems;
	}

	public void setDisplayItems(List<DisplayItem> displayItems) {
		this.displayItems = displayItems;
	}
}
