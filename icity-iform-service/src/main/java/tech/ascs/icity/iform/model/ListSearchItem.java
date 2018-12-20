package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 列表排序字段
 */
@Entity
@Table(name = "ifm_list_search_item")
public class ListSearchItem extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name="list_id")
	private ListModelEntity listModel;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
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