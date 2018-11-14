package tech.ascs.icity.iform.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 数据字典
 */
@Entity
@Table(name = "ifm_dictionary")
public class DictionaryEntity extends BaseEntity {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 描述
	 */
    @Column(name = "description")
	private String description;
	
	/**
	 * 数据字典项
	 */
	@OneToMany(mappedBy = "dictionary", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	private List<DictionaryItemEntity> dictionaryItems = new ArrayList<DictionaryItemEntity>();

	public List<DictionaryItemEntity> getDictionaryItems() {
		return dictionaryItems;
	}

	public void setDictionaryItems(List<DictionaryItemEntity> dictionaryItems) {
		this.dictionaryItems = dictionaryItems;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}