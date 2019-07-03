package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
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

	@ApiModelProperty(value = "唯一编码", position = 1)
	private String uniqueCode;

	@ApiModelProperty(value = "描述", position = 2)
	private String description;

	@ApiModelProperty(value = "是否支持多选", position = 3)
	private Boolean multiSelect = true;

	@ApiModelProperty(value = "主表单模型", position = 4)
	private FormModel masterForm;

	@ApiModelProperty(value = "附加表单模型列表", position = 5)
	private List<FormModel> slaverForms = new ArrayList();

	@ApiModelProperty(value = "排序字段列表", position = 6)
	private List<SortItem> sortItems = new ArrayList();

	@ApiModelProperty(value = "功能列表", position = 7)
	private List<FunctionModel> functions = new ArrayList();

	@ApiModelProperty(value = "查询字段列表", position = 8)
	private List<SearchItem> searchItems = new ArrayList();

	@ApiModelProperty(value = "显示字段列表", position = 9)
	private List<ItemModel> displayItems = new ArrayList();

	@ApiModelProperty(value = "快速筛选", position = 10)
	private List<QuickSearchItem> quickSearchItems = new ArrayList();

	@ApiModelProperty(value = "表单绑定的数据模型列表", position = 11)
	private List<DataModel> dataModels = new ArrayList();

	@ApiModelProperty(value = "应用id", position = 12)
	private String applicationId;

	@ApiModelProperty(value = "数据权限", position = 13)
	private DataPermissionsType dataPermissions;

	@ApiModelProperty(value ="APP列表展示模板", position = 14)
	private List<Map> appListTemplate;

	@ApiModelProperty(value="关联关系控件集合",position = 15)
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<ItemModel> relevanceItemModelList = new ArrayList();

	@ApiModelProperty(value="门户列表模板",position = 16)
	private List<Map> protalListTemplate;

	private String searchItemsSort;

	public String getUniqueCode() {
		return uniqueCode;
	}

	public void setUniqueCode(String uniqueCode) {
		this.uniqueCode = uniqueCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getMultiSelect() {
		return multiSelect;
	}

	public void setMultiSelect(Boolean multiSelect) {
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

	public List<DataModel> getDataModels() {
		return dataModels;
	}

	public void setDataModels(List<DataModel> dataModels) {
		this.dataModels = dataModels;
	}

	public DataPermissionsType getDataPermissions() {
		return dataPermissions;
	}

	public void setDataPermissions(DataPermissionsType dataPermissions) {
		this.dataPermissions = dataPermissions;
	}

	public List<Map> getAppListTemplate() {
		return appListTemplate;
	}

	public void setAppListTemplate(List<Map> appListTemplate) {
		this.appListTemplate = appListTemplate;
	}

	public List<ItemModel> getRelevanceItemModelList() {
		return relevanceItemModelList;
	}

	public void setRelevanceItemModelList(List<ItemModel> relevanceItemModelList) {
		this.relevanceItemModelList = relevanceItemModelList;
	}

	public List<Map> getProtalListTemplate() {
		return protalListTemplate;
	}

	public void setProtalListTemplate(List<Map> protalListTemplate) {
		this.protalListTemplate = protalListTemplate;
	}

	public String getSearchItemsSort() {
		return searchItemsSort;
	}

	public void setSearchItemsSort(String searchItemsSort) {
		this.searchItemsSort = searchItemsSort;
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
