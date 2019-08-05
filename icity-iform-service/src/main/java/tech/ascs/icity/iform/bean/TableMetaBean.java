package tech.ascs.icity.iform.bean;

import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.iform.model.DataModelEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author renjie
 * @since 1.0.0
 **/

public class TableMetaBean {

    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String REMARKS = "REMARKS";

    private String tableName;
    private String remarks;

    public static TableMetaBean from(ResultSet resultSet) throws SQLException {
        TableMetaBean tableMetaBean = new TableMetaBean();

        tableMetaBean.setTableName(resultSet.getString(TABLE_NAME));
        tableMetaBean.setRemarks(resultSet.getString(REMARKS));
        return tableMetaBean;
    }

    public DataModelEntity buildSimpleDataModel(String applicationId) {
        DataModelEntity dataModelEntity = new DataModelEntity();
        dataModelEntity.setPrefix("");
        dataModelEntity.setApplicationId(applicationId);
        dataModelEntity.setTableName(this.getTableName());
        dataModelEntity.setDescription(this.getRemarks());
        dataModelEntity.setModelType(DataModelType.Single);
        dataModelEntity.setSynchronize(true);
        return dataModelEntity;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
