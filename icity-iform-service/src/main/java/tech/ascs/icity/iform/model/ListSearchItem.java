package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.*;

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

	@Column(name="full_text_search")
	private Boolean fullTextSearch = false;
	@Column(name="app_use")
	private Boolean appUse = false;
	@Column(name="pc_use")
	private Boolean pcUse = false;

	@Column(name="order_no",columnDefinition = "int default 0")//排序号
	private Integer orderNo = 0;

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

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
}