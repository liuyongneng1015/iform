package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.SystemCreateType;
import tech.ascs.icity.iform.api.model.TreeSelectDataSource;

import javax.persistence.*;

/**
 * 时间表单控件模型
 */
@Entity
@Table(name = "ifm_tree_select_item_model")
@DiscriminatorValue("treeSelectItemModel")
public class TreeSelectItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="multiple")// 是否多选
	private Boolean multiple;

	@Column(name="data_sources")//下拉数据来源
	@Enumerated(EnumType.STRING)
	private TreeSelectDataSource dataSource;

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public TreeSelectDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(TreeSelectDataSource dataSource) {
		this.dataSource = dataSource;
	}
}