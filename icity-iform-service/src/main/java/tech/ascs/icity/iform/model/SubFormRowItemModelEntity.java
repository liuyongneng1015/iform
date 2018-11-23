package tech.ascs.icity.iform.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 子表单行级控件模型
 */
@Entity
@Table(name = "ifm_sub_form_row_item_model")
@DiscriminatorValue("subFormRowItemModel")
public class SubFormRowItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="row_number") // 当前行数
	private Integer rowNumber;

	@JoinColumn(name="parent_id") //子表模型
	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private SubFormItemModelEntity parentItem;

	/** 组件子项（由组和字段构成） */
	@JoinColumn(name="master_id")
	@OneToMany(cascade = {CascadeType.ALL}) // {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE}
	private List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public SubFormItemModelEntity getParentItem() {
		return parentItem;
	}

	public void setParentItem(SubFormItemModelEntity parentItem) {
		this.parentItem = parentItem;
	}

	public List<ItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<ItemModelEntity> items) {
		this.items = items;
	}
}