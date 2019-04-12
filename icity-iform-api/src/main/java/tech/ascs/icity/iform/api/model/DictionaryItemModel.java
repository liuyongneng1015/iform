package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.Codeable;
import tech.ascs.icity.model.NameEntity;

import java.util.List;

/**
 * 数据字典项
 */
@ApiModel("数据字典项")
public class DictionaryItemModel extends NameEntity implements Codeable {
	
	/**
	 * 所属数据字典id
	 */
    @ApiModelProperty(value = "分类id", position = 3)
	private String dictionaryId;

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
	 * 按钮icon
	 */
	@ApiModelProperty(value = "按钮icon", position = 4)
	private String icon;

	/**
	 * 排序号
	 */
	@ApiModelProperty(value = "排序号", position = 5)
	private Integer orderNo = 0;


	/**
	 * 父选项
	 */
	@ApiModelProperty(value = "父选项", position = 6)
	private String parentId;

	/**
	 * 子选项描述
	 */
	@ApiModelProperty(value = "子选项描述", position = 7)
	private List<DictionaryItemModel> resources;

	public String getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(String dictionaryId) {
		this.dictionaryId = dictionaryId;
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

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public List<DictionaryItemModel> getResources() {
		return resources;
	}

	public void setResources(List<DictionaryItemModel> resources) {
		this.resources = resources;
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
