package tech.ascs.icity.iform.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.model.Codeable;

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
	@ManyToOne
    @JoinColumn(name = "parent_id")
	private DictionaryEntity dictionary;

	/**
	 * 编码
	 */
	@Column(name = "code")
	private String code;
	
	/**
	 * 描述
	 */
	@Column(name = "description")
	private String description;
	
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
	
}
