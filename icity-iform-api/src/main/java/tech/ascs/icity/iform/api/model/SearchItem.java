package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("列表搜索字段信息")
public class SearchItem extends ItemModel implements Comparable<SearchItem> {

	public static class Search {
		@ApiModelProperty(value = "查询类型", position = 0)
		private SearchType searchType;
		@ApiModelProperty(value = "是否可见", position = 1)
		private boolean visible;
		@ApiModelProperty(value = "查询条件默认值", position = 2)
		private Object defaultValue;
		@ApiModelProperty(value = "查询条件默认值", position = 2)
		private Object defaultValueName;
		public SearchType getSearchType() {
			return searchType;
		}
		public void setSearchType(SearchType searchType) {
			this.searchType = searchType;
		}
		public boolean isVisible() {
			return visible;
		}
		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
		}

		public Object getDefaultValueName() {
			return defaultValueName;
		}

		public void setDefaultValueName(Object defaultValueName) {
			this.defaultValueName = defaultValueName;
		}
	}

	@ApiModelProperty(value = "全文搜索", position = 6)
	private Boolean fullTextSearch = false;
	@ApiModelProperty(value = "app端使用的查询条件", position = 7)
	private Boolean appUse = false;
	@ApiModelProperty(value = "pc端使用的查询条件", position = 8)
	private Boolean pcUse = false;
	private String referenceListId;
	@ApiModelProperty(value = "查询定义，用于列表中的查询条件", position = 9)
	private Search search;

	@ApiModelProperty("搜索框排序号")//排序号
	private Integer orderNo = 0;

	public String getReferenceListId() {
		return referenceListId;
	}

	public void setReferenceListId(String referenceListId) {
		this.referenceListId = referenceListId;
	}

	public Boolean getFullTextSearch() {
		return fullTextSearch;
	}

	public void setFullTextSearch(Boolean fullTextSearch) {
		this.fullTextSearch = fullTextSearch;
	}

	public Boolean getAppUse() {
		return appUse;
	}

	public void setAppUse(Boolean appUse) {
		this.appUse = appUse;
	}

	public Boolean getPcUse() {
		return pcUse;
	}

	public void setPcUse(Boolean pcUse) {
		this.pcUse = pcUse;
	}

	public Search getSearch() {
		return search;
	}

	public void setSearch(Search search) {
		this.search = search;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

    @Override
    public int compareTo(SearchItem target) {
	    return this.getOrderNo().compareTo(target.getOrderNo());
    }
}
