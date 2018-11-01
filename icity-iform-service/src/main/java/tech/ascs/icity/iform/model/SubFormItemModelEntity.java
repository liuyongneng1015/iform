package tech.ascs.icity.iform.model;

import javax.persistence.*;
import java.util.List;

/**
 * 子表单控件模型
 */
@Entity
@Table(name = "ifm_sub_form_item_model")
@DiscriminatorValue("subFormItemModel")
public class SubFormItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@JoinColumn(name="legend") // 头部标签
	private String legend;
	@JoinColumn(name="rowCount") // 控件行数
	private Integer rowCount;
	@JoinColumn(name="showHead") // 是否显示表头
	private Boolean showHead;

	/** 组件子项（由组和字段构成） */
	@Column(name="parent_id")
	@OneToMany(cascade={CascadeType.ALL}) // {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE}
	private List<ItemModelEntity> items;


	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public Boolean getShowHead() {
		return showHead;
	}

	public void setShowHead(Boolean showHead) {
		this.showHead = showHead;
	}

	public List<ItemModelEntity> getItems() {
		return items;
	}

	public void setItems(List<ItemModelEntity> items) {
		this.items = items;
	}
}