package tech.ascs.icity.iform.model;

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

	@Column(name="data_range")// 数据范围
	private String dataRange;

	@Column(name="data_range_name")// 数据范围名称
	private String dataRangeName;

	@Column(name="default_value")// 默认值
	private String defaultValue;

	@Column(name="data_depth")// 数据深度
	private Integer dataDepth;

	@Column(name="reference_dictionary_id")// 数据字典分类
	private String referenceDictionaryId;

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

	public String getDataRange() {
		return dataRange;
	}

	public void setDataRange(String dataRange) {
		this.dataRange = dataRange;
	}

	public Integer getDataDepth() {
		return dataDepth;
	}

	public void setDataDepth(Integer dataDepth) {
		this.dataDepth = dataDepth;
	}

	public String getDataRangeName() {
		return dataRangeName;
	}

	public void setDataRangeName(String dataRangeName) {
		this.dataRangeName = dataRangeName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getReferenceDictionaryId() {
		return referenceDictionaryId;
	}

	public void setReferenceDictionaryId(String referenceDictionaryId) {
		this.referenceDictionaryId = referenceDictionaryId;
	}
}