package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("表单联动控件模型ItemModel")
public class LinkedItemModel extends NameEntity {

	@ApiModelProperty(value = "关联字典分类ID", position = 13)
	private String referenceDictionaryId;

	@ApiModelProperty(value = " 关联字典联动目标id", position = 13)
	private String referenceDictionaryItemId;

	@ApiModelProperty(value="上级关联控件模型id",position = 22)
	private String parentItemId ;

	@ApiModelProperty(value="组件子项（由组和字段构成） ",position = 26)
	private List<LinkedItemModel> items;

	public String getReferenceDictionaryId() {
		return referenceDictionaryId;
	}

	public void setReferenceDictionaryId(String referenceDictionaryId) {
		this.referenceDictionaryId = referenceDictionaryId;
	}

	public String getReferenceDictionaryItemId() {
		return referenceDictionaryItemId;
	}

	public void setReferenceDictionaryItemId(String referenceDictionaryItemId) {
		this.referenceDictionaryItemId = referenceDictionaryItemId;
	}

	public String getParentItemId() {
		return parentItemId;
	}

	public void setParentItemId(String parentItemId) {
		this.parentItemId = parentItemId;
	}

	public List<LinkedItemModel> getItems() {
		return items;
	}

	public void setItems(List<LinkedItemModel> items) {
		this.items = items;
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
