package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.ascs.icity.model.IdEntity;

/**
 * @author renjie
 * @since 0.7.2
 **/
@EqualsAndHashCode(callSuper = true)
@ApiModel("数据库表模型")
@Data
public class DataTable extends IdEntity {

    @ApiModelProperty("数据库表名")
    private String tableName;
    @ApiModelProperty("数据库表描述")
    private String describe;

    @ApiModelProperty("表关系类型")
    private DataModelType dataModelType = DataModelType.Single;

}
