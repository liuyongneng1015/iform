package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 字典建模数据项
 */
@ApiModel("字典建模数据项")
public class DictionaryModelData extends NameEntity implements Serializable {

	/**
	 * 所属字典建模id
	 */
	@ApiModelProperty(value = "字典建模id", position = 3)
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
	 * 大小
	 */
	@ApiModelProperty(value = "大小", position = 6)
	private Integer size = 0;

	/**
	 * 更新日期
	 */
	@ApiModelProperty(value = "更新日期", position = 6)
	private Date updateDate;

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

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

    public List<DictionaryModelData> getResources() {
		return resources;
	}

	public void setResources(List<DictionaryModelData> resources) {
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
