package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("列表搜索字段信息")
public class SearchItem extends ItemModel {

	public static class Search {
		@ApiModelProperty(value = "查询类型", position = 0)
		private SearchType searchType;
		@ApiModelProperty(value = "是否可见", position = 1)
		private boolean visible;
		@ApiModelProperty(value = "查询条件默认值", position = 2)
		private List<String> defaultValue = new ArrayList<>();
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

		public List<String> getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(List<String> defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

	@ApiModelProperty(value = "查询定义，用于列表中的查询条件", position = 6)
	private Search search;

	public Search getSearch() {
		return search;
	}

	public void setSearch(Search search) {
		this.search = search;
	}
}
