package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典
 */
@ApiModel("字典表")
public class SystemCodeModel extends NameEntity {
	


	/**
	 * 系统代码分类
	 */
	@ApiModelProperty(value = "系统代码分类", position = 4)
	private List<DictionaryModel> dictionaryModels = new ArrayList<>();

	/**
	 * 数据字典项
	 */
	@ApiModelProperty(value = "系统代码数据字典项", position = 5)
	private DictionaryItemModel dictionaryItemModel;

	public List<DictionaryModel> getDictionaryModels() {
		return dictionaryModels;
	}

	public void setDictionaryModels(List<DictionaryModel> dictionaryModels) {
		this.dictionaryModels = dictionaryModels;
	}

	public DictionaryItemModel getDictionaryItemModel() {
		return dictionaryItemModel;
	}

	public void setDictionaryItemModel(DictionaryItemModel dictionaryItemModel) {
		this.dictionaryItemModel = dictionaryItemModel;
	}

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}