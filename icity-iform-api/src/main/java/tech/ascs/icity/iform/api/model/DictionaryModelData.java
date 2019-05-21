package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 字典建模数据项
 */
@ApiModel("字典建模数据项")
public class DictionaryModelData implements Serializable {

	/**
	 * 所属字典建模id
	 */
	@ApiModelProperty(value = "字典建模id", position = 3)
	private String dictionaryId;

	/**
	 * 主键id
	 */
	@ApiModelProperty(value = "主键id", position = 3)
	private Integer id;

	/**
	 * 名称
	 */
	@ApiModelProperty(value = "名称", position = 3)
	private String name;

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
	private Integer parentId;

	/**
	 * 子选项描述
	 */
	@ApiModelProperty(value = "子选项描述", position = 7)
	private List<DictionaryModelData> resources;


	public String getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(String dictionaryId) {
		this.dictionaryId = dictionaryId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public List<DictionaryModelData> getResources() {
		return resources;
	}

	public void setResources(List<DictionaryModelData> resources) {
		this.resources = resources;
	}
}
