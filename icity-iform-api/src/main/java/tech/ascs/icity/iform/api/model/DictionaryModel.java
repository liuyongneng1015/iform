package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.io.Serializable;

/**
 * 字典建模
 */
@ApiModel("字典建模")
public class DictionaryModel extends NameEntity implements Serializable {

	/**
	 * 数据表
	 */
	@ApiModelProperty(value = "数据表", position = 4)
	private String tableName;

	/**
	 * 数据表
	 */
	@ApiModelProperty(value = "数据表", position = 4)
	private String data;


	@ApiModelProperty(value = "应用id", position = 4)
	private String applicationId;

	/**
	 * 排序号
	 */
	@ApiModelProperty(value = "排序号", position = 4)
	private Integer orderNo = 0;


	public String getTableName() {
		if(tableName == null){
			tableName = data;
		}
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getData() {
		if(data == null){
			data = tableName;
		}
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
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
