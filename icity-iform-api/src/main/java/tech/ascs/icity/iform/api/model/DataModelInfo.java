package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("数据模型摘要信息")
public class DataModelInfo extends NameEntity {

    @ApiModelProperty(value = "表名称", position = 2)
	private String tableName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@JsonIgnore
	public String getTabName() {
		return getTableName();
	}

	public void setTabName(String tabName) {
		setTableName(tabName);
	}

	@JsonIgnore
	public String getTabNameDesc() {
		return getName();
	}

	public void setTabNameDesc(String tabNameDesc) {
		setName(tabNameDesc);
	}
}
