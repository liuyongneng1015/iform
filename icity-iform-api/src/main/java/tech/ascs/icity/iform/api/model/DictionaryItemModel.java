package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.Codeable;
import tech.ascs.icity.model.NameEntity;

/**
 * 数据字典项
 */
@ApiModel("数据字典项")
public class DictionaryItemModel extends NameEntity implements Codeable {
	
	/**
	 * 所属数据字典
	 */
	@JsonBackReference
    @ApiModelProperty(hidden = true)
	private DictionaryModel dictionary;

	/**
	 * 编码
	 */
    @ApiModelProperty(value = "编码", position = 3)
	private String code;

	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述", position = 4)
	private String description;
	
	public DictionaryModel getDictionary() {
		return dictionary;
	}

	public void setDictionary(DictionaryModel dictionary) {
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
