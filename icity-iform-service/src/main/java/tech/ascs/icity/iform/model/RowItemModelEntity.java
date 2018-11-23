package tech.ascs.icity.iform.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 表单组合控件模型
 */
@Entity
@Table(name = "ifm_row_item_model")
@DiscriminatorValue("rowItemModel")
public class RowItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="row_number") // 当前行数
	private Integer rowNumber;

	/** 组件子项（由组和字段构成） */
	@JoinColumn(name="parent_id")
	@OneToMany(cascade ={CascadeType.ALL}) // {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE}
	private List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public List<ItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<ItemModelEntity> items) {
		this.items = items;
	}
}