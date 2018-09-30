package tech.ascs.icity.iform.model;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import tech.ascs.icity.iform.api.model.SearchType;

/**
 * 表单控件查询设置
 */
@Embeddable
public class ItemSearchInfo {

	@Enumerated(EnumType.STRING)
	private SearchType searchType;

	private boolean visible = true;

	private String defaultValue;

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

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
 
}