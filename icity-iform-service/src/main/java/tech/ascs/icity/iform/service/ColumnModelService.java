package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.ReferenceModel;
import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.ColumnReferenceEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.model.IndexModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;

public interface ColumnModelService extends JPAService<ColumnModelEntity> {

    //创建column未持久化到数据库
    ColumnModelEntity saveColumnModelEntity(DataModelEntity dataModel, String columnName);

    //解除关系
    void deleteOldColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> deleteOldToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList);

    //保存关系持久化到数据库
    void saveColumnReferenceEntity(ColumnModelEntity fromColumnEntity, ColumnModelEntity toColumnEntity, ReferenceType referenceType, String referenceMiddleTableName);

    //删除行的关联关系
    void deleteColumnReferenceEntity(ColumnReferenceEntity columnReferenceEntity);

    //获取关联关系
     List<ReferenceModel> getReferenceModel(ColumnModelEntity entity);

    //删除数据库字段
    void deleteTableColumn(String tableName, String columnName);

    //删除数据库
    void deleteTable(String tableName);

    //删除数据库字段索引
    void deleteTableColumnIndex(String tableName, String columnName);

    //更新数据库字段索引
    void updateColumnModelEntityIndex(ColumnModelEntity columnModelEntity);

    //删除数据库索引
    void deleteTableIndex(String tableName, String indexName);

    //创建数据库索引
    void createTableIndex(String tableName, IndexModelEntity index);
}
