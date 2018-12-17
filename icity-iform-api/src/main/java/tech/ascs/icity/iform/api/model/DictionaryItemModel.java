package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.Codeable;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * 排序号
	 */
	@ApiModelProperty(value = "排序号", position = 4)
	private Integer orderNo;


	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述", position = 5)
	private DictionaryItemModel paraentItem ;

	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述", position = 5)
	private List<DictionaryItemModel> childrenItem = new ArrayList<>();
	
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

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public DictionaryItemModel getParaentItem() {
		return paraentItem;
	}

	public void setParaentItem(DictionaryItemModel paraentItem) {
		this.paraentItem = paraentItem;
	}

	public List<DictionaryItemModel> getChildrenItem() {
		return childrenItem;
	}

	public void setChildrenItem(List<DictionaryItemModel> childrenItem) {
		this.childrenItem = childrenItem;
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
