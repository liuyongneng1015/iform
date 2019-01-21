package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("列表搜索字段信息")
public class SearchItem extends ItemModel {

	public static class Search {
		@ApiModelProperty(value = "查询类型", position = 0)
		private SearchType searchType;
		@ApiModelProperty(value = "是否可见", position = 1)
		private boolean visible;
		@ApiModelProperty(value = "查询条件默认值", position = 2)
		private Object defaultValue;
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
	}

	@ApiModelProperty(value = "查询定义，用于列表中的查询条件", position = 6)
	private Search search;

	@ApiModelProperty("搜索框排序号")//排序号
	private Integer orderNo = 0;

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
}
