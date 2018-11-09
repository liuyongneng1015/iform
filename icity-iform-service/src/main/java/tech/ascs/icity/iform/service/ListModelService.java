package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;

public interface ListModelService extends JPAService<ListModelEntity> {

    //查询列表模型
    List<ListModel> findListModelsByTableName(String tableName);

}
