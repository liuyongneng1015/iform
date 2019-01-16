package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("列表模型ListModel")
public class ListModel extends NameEntity {


	@ApiModel("列表排序字段摘要信息")
	public static class SortItem extends NameEntity {
		@ApiModelProperty(value = "关联控件", position = 2)
		private ItemModel itemModel;
		@ApiModelProperty(value = "是否正序", position = 3)
		private boolean asc;

		public ItemModel getItemModel() {
			return itemModel;
		}

		public void setItemModel(ItemModel itemModel) {
			this.itemModel = itemModel;
		}

		public boolean isAsc() {
			return asc;
		}
		public void setAsc(boolean asc) {
			this.asc = asc;
		}
	}

	@ApiModelProperty(value = "描述", position = 2)
	private String description;

	@ApiModelProperty(value = "是否支持多选", position = 3)
	private boolean multiSelect = true;

	@ApiModelProperty(value = "主表单模型", position = 4)
	private FormModel masterForm;

	@ApiModelProperty(value = "附加表单模型列表", position = 5)
	private List<FormModel> slaverForms = new ArrayList<FormModel>();

	@ApiModelProperty(value = "排序字段列表", position = 6)
	private List<SortItem> sortItems = new ArrayList<SortItem>();

	@ApiModelProperty(value = "功能列表", position = 7)
	private List<FunctionModel> functions = new ArrayList<FunctionModel>();

	@ApiModelProperty(value = "查询字段列表", position = 8)
	private List<SearchItem> searchItems = new ArrayList<SearchItem>();

	@ApiModelProperty(value = "显示字段列表", position = 9)
	private List<ItemModel> displayItems = new ArrayList<ItemModel>();

	@ApiModelProperty(value = "快速筛选", position = 10)
	private List<QuickSearchItem> quickSearchItems = new ArrayList<QuickSearchItem>();

	@ApiModelProperty(value = "应用id", position = 11)
	private String applicationId;

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

	public FormModel getMasterForm() {
		return masterForm;
	}

	public void setMasterForm(FormModel masterForm) {
		this.masterForm = masterForm;
	}

	public List<FormModel> getSlaverForms() {
		return slaverForms;
	}

	public void setSlaverForms(List<FormModel> slaverForms) {
		this.slaverForms = slaverForms;
	}

	public List<SortItem> getSortItems() {
		return sortItems;
	}

	public void setSortItems(List<SortItem> sortItems) {
		this.sortItems = sortItems;
	}

	public List<FunctionModel> getFunctions() {
		return functions;
	}

	public void setFunctions(List<FunctionModel> functions) {
		this.functions = functions;
	}

	public List<SearchItem> getSearchItems() {
		return searchItems;
	}

	public void setSearchItems(List<SearchItem> searchItems) {
		this.searchItems = searchItems;
	}

	public List<ItemModel> getDisplayItems() {
		return displayItems;
	}

	public void setDisplayItems(List<ItemModel> displayItems) {
		this.displayItems = displayItems;
	}

	public List<QuickSearchItem> getQuickSearchItems() {
		return quickSearchItems;
	}

	public void setQuickSearchItems(List<QuickSearchItem> quickSearchItems) {
		this.quickSearchItems = quickSearchItems;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
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
