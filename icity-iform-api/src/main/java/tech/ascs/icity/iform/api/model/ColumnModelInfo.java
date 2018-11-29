package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("数据字段模型摘要信息")
public class ColumnModelInfo extends ColumnModel {
	@ApiModelProperty(value = "数据库表名", position = 2)
	private String tableName;


	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@JsonIgnore
	@Override
	public Integer getLength() {
		return super.getLength();
	}

	@JsonIgnore
	@Override
	public Integer getPrecision() {
		return super.getPrecision();
	}

	@JsonIgnore
	@Override
	public Integer getScale() {
		return super.getScale();
	}

	@JsonIgnore
	@Override
	public Boolean getNotNull() {
		return super.getNotNull();
	}

}
