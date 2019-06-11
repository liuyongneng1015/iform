package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.IdEntity;

/**
 * @author renjie
 * @since 0.7.2
 **/
@ApiModel("数据库表模型")
public class DataTable extends IdEntity {

    @ApiModelProperty("数据库表名")
    private String tableName;
    @ApiModelProperty("数据库表描述")
    private String describe;

    @ApiModelProperty("表关系类型")
    private DataModelType dataModelType = DataModelType.Single;


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public DataModelType getDataModelType() {
        return dataModelType;
    }

    public void setDataModelType(DataModelType dataModelType) {
        this.dataModelType = dataModelType;
    }
}
