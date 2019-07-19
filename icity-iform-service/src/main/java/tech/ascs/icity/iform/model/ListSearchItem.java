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

	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="item_id")
	private ItemModelEntity itemModel;

	@Embedded
	private ItemSearchInfo search;

	private String parseArea;

	/** 联动数据解绑 */
	private Boolean linkageDataUnbind = false;
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

	public String getParseArea() {
		return parseArea;
	}

	public void setParseArea(String parseArea) {
		this.parseArea = parseArea;
	}

	public Boolean getLinkageDataUnbind() {
		return linkageDataUnbind;
	}

	public void setLinkageDataUnbind(Boolean linkageDataUnbind) {
		this.linkageDataUnbind = linkageDataUnbind;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
}