package tech.ascs.icity.iform.api.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.Codeable;
import tech.ascs.icity.model.NameEntity;

/**
 * 字典数据
 */
@ApiModel("字典数据")
public class DictionaryDataModel extends NameEntity implements Codeable {

	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述", position = 4)
	private String description;

	/**
	 * 编码
	 */
	@ApiModelProperty(value = "编码", position = 2)
	private String code;

	/**
	 * 按钮icon
	 */
	@ApiModelProperty(value = "按钮icon", position = 4)
	private String icon;

	/**
	 * 排序号
	 */
	@ApiModelProperty(value = "排序号", position = 4)
	private Integer orderNo;

	/**
	 * 数据字典项
	 */
	@ApiModelProperty(value = "数据字典项", position = 5)
	private List<DictionaryDataItemModel> resources;

	public List<DictionaryDataItemModel> getResources() {
		return resources;
	}

	public void setResources(List<DictionaryDataItemModel> resources) {
		this.resources = resources;
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

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
