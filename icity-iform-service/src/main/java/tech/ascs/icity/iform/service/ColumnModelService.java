package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.ColumnReferenceEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;

public interface ColumnModelService extends JPAService<ColumnModelEntity> {

    //创建column未持久化到数据库
    ColumnModelEntity saveColumnModelEntity(DataModelEntity dataModel, String columnName);

    //解除关系
    void deleteOldColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> deleteOldToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList);

    //保存关系持久化到数据库
    void saveColumnReferenceEntity(ColumnModelEntity fromColumnEntity, ColumnModelEntity toColumnEntity, ReferenceType referenceType);

    //保存关系持久化到数据库
    void deleteColumn(ColumnModelEntity columnEntity);
}
