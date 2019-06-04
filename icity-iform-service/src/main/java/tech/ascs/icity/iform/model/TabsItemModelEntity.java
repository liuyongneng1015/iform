package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.SystemCreateType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 标签页控件
 */
@Entity
@Table(name = "ifm_tabs_item_model")
@DiscriminatorValue("tabsItemModel")
public class TabsItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 178373L;
	/** 标签页子项 */
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parentItem")
	private List<TabPaneItemModelEntity> items = new ArrayList<TabPaneItemModelEntity>();

	public List<TabPaneItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<TabPaneItemModelEntity> items) {
		this.items = items;
	}
}