package tech.ascs.icity.iform.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.iform.model.DataModelEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author renjie
 * @since 1.0.0
 **/
@Builder
@AllArgsConstructor
@Data
public class TableMetaBean {

    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String REMARKS = "REMARKS";

    private String tableName;
    private String remarks;

    public static TableMetaBean from(ResultSet resultSet) throws SQLException {
        return TableMetaBean.builder()
                .tableName(resultSet.getString(TABLE_NAME))
                .remarks(resultSet.getString(REMARKS))
                .build();
    }

    public DataModelEntity buildSimpleDataModel(String applicationId) {
        DataModelEntity dataModelEntity = new DataModelEntity();
        dataModelEntity.setPrefix("");
        dataModelEntity.setApplicationId(applicationId);
        dataModelEntity.setTableName(this.getTableName());
        dataModelEntity.setDescription(this.getRemarks());
        dataModelEntity.setModelType(DataModelType.Single);
        dataModelEntity.setSynchronized(true);
        return dataModelEntity;
    }

}
