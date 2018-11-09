package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.Codeable;
import tech.ascs.icity.model.NameEntity;

/**
 * 数据字典
 */
@ApiModel("字典表")
public class DictionaryModel extends NameEntity implements Codeable {
	
	/**
	 * 编码
	 */
    @ApiModelProperty(value = "编码", position = 2)
	private String code;
	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述", position = 3)
	private String description;

	/**
	 * 数据字典项
	 */
    @JsonManagedReference
	@ApiModelProperty(hidden = true)
	private List<DictionaryItemModel> dictionaryItems = new ArrayList<DictionaryItemModel>();

	public List<DictionaryItemModel> getDictionaryItems() {
		return dictionaryItems;
	}

	public void setDictionaryItems(List<DictionaryItemModel> dictionaryItems) {
		this.dictionaryItems = dictionaryItems;
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
