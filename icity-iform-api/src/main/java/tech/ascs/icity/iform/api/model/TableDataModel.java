package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("表单数据模型")
public class TableDataModel extends NameEntity {

	@ApiModelProperty(value = "字段值", position = 3)
	private String value;

	@ApiModelProperty(value = "控件id", position = 4)
	private String itemId;

	@ApiModelProperty(value = "字段id", position = 5)
	private String columnId;

	@ApiModelProperty(value = "表名", position = 6)
	private String tableName ;

    @ApiModelProperty(value = "表单id", position = 6)
    private String formId ;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }
}
