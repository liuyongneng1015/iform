package tech.ascs.icity.iform.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 字典数据
 */
@Entity
@Table(name = "ifm_dictionary")
public class DictionaryDataEntity extends BaseEntity {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 描述
	 */
    @Column(name = "description")
	private String description;

	/**
	 * 编码
	 */
	@Column(name = "code")
	private String code;

	/**
	 * 按钮icon
	 */
	@Column(name = "icon")
	private String icon;

	/**
	 * 排序号
	 */
	@Column(name = "order_no")
	private Integer orderNo;
	
	/**
	 * 数据字典项
	 */
	@OneToMany(mappedBy = "dictionary", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	private List<DictionaryDataItemEntity> dictionaryItems = new ArrayList<DictionaryDataItemEntity>();

	public List<DictionaryDataItemEntity> getDictionaryItems() {
		return dictionaryItems;
	}

	public void setDictionaryItems(List<DictionaryDataItemEntity> dictionaryItems) {
		this.dictionaryItems = dictionaryItems;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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
}
