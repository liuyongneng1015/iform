package tech.ascs.icity.iform.model;

import javax.persistence.*;

import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.model.Codeable;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典项
 */
@Entity
@Table(name = "ifm_dictionary_item")
public class DictionaryItemEntity extends BaseEntity implements Codeable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 所属数据字典
	 */
	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "parent_id")
	private DictionaryEntity dictionary;

	/**
	 * 编码比如0,1 对应name字段男女
	 */
	@Column(name = "code")
	private String code;
	
	/**
	 * 描述
	 */
	@Column(name = "description")
	private String description;

	/**
	 * 按钮icon
	 */
	@Column(name = "icon")
	private String icon;

	/**
	 * 排序号
	 */
	@Column(name = "order_no")
	private Integer orderNo = 0;

	/**
	 * 父类字典
	 */
	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name = "parent_item_id")
	private DictionaryItemEntity parentItem;

	@OneToMany(mappedBy = "parentItem",cascade = CascadeType.ALL )
	private List<DictionaryItemEntity> childrenItem = new ArrayList<>();
	
	public DictionaryEntity getDictionary() {
		return dictionary;
	}

	public void setDictionary(DictionaryEntity dictionary) {
		this.dictionary = dictionary;
	}

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public DictionaryItemEntity getParentItem() {
		return parentItem;
	}

	public void setParentItem(DictionaryItemEntity parentItem) {
		this.parentItem = parentItem;
	}

	public List<DictionaryItemEntity> getChildrenItem() {
		return childrenItem;
	}

	public void setChildrenItem(List<DictionaryItemEntity> childrenItem) {
		this.childrenItem = childrenItem;
	}
}
