package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.ReferenceType;

import javax.persistence.*;
import java.util.List;

/**
 * 子表单行级控件模型
 */
@Entity
@Table(name = "ifm_row_item_model")
@DiscriminatorValue("rowItemModel")
public class RowItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="row_number") // 当前行数
	private Integer rowsNumber;

	@Column(name="column_number") // 当前列数
	private Integer columnNumber;

	@JoinColumn(name="parent_id") //子表模型
	@ManyToOne(cascade={CascadeType.ALL})
	private SubFormItemModelEntity parentItem;

	/** 组件子项（由组和字段构成） */
	@Column(name="parent_id")
	@OneToMany(cascade={CascadeType.ALL}) // {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE}
	private List<ItemModelEntity> items;

	public Integer getRowsNumber() {
		return rowsNumber;
	}

	public void setRowsNumber(Integer rowsNumber) {
		this.rowsNumber = rowsNumber;
	}

	public Integer getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(Integer columnNumber) {
		this.columnNumber = columnNumber;
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