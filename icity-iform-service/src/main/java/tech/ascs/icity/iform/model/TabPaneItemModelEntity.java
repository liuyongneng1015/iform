package tech.ascs.icity.iform.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 标签页子项
 */
@Entity
@Table(name = "ifm_tab_pane_item_model")
@DiscriminatorValue("tabPaneItemModel")
public class TabPaneItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;
	@JoinColumn(name="parent_tabs_item_id") //标签页
	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private TabsItemModelEntity parentItem;

	/** 标签页子项关联控件 */
	@JoinColumn(name="parent_tab_pane_id")
	@OneToMany(cascade = {CascadeType.ALL}) // {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE}
	private List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();

	public TabsItemModelEntity getParentItem() {
		return parentItem;
	}

	public void setParentItem(TabsItemModelEntity parentItem) {
		this.parentItem = parentItem;
	}

	public List<ItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<ItemModelEntity> items) {
		this.items = items;
	}
}