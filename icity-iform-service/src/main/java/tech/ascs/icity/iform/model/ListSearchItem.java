package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 列表排序字段
 */
@Entity
@Table(name = "ifm_list_search_item")
public class ListSearchItem extends JPAEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="list_id")
	private ListModelEntity listModel;

	@ManyToOne
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	@Embedded
	private ItemSearchInfo search;

	public ListModelEntity getListModel() {
		return listModel;
	}

	public void setListModel(ListModelEntity listModel) {
		this.listModel = listModel;
	}

	public ItemModelEntity getItemModel() {
		return itemModel;
	}

	public void setItemModel(ItemModelEntity itemModel) {
		this.itemModel = itemModel;
	}

	public ItemSearchInfo getSearch() {
		return search;
	}

	public void setSearch(ItemSearchInfo search) {
		this.search = search;
	}
}