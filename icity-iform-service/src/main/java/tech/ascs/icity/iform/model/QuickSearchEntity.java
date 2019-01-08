package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 快速筛选
 */
@Entity
@Table(name = "ifm_list_quick_search_item")
public class QuickSearchEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch=FetchType.LAZY)
    @JoinColumn(name="list_id")
    private ListModelEntity listModel;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch=FetchType.LAZY)
    @JoinColumn(name="item_id")
    private ItemModelEntity itemModel;

    @Column(name = "is_use")
    private Boolean use = false;
    private String searchValues;
    private Boolean countVisible = false;
    @Column(name="order_no")
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

    public Boolean getUse() {
        return use;
    }

    public void setUse(Boolean use) {
        this.use = use;
    }

    public String getSearchValues() {
        return searchValues;
    }

    public void setSearchValues(String searchValues) {
        this.searchValues = searchValues;
    }

    public Boolean getCountVisible() {
        return countVisible;
    }

    public void setCountVisible(Boolean countVisible) {
        this.countVisible = countVisible;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }
}
