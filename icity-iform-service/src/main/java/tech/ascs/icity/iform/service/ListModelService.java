package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;

public interface ListModelService extends JPAService<ListModelEntity> {

    //查询列表模型
    List<ListModel> findListModelsByTableName(String tableName);

    //删除排序
    void deleteSort(String id);

    //删除排序
    void deleteSearch(String id);

    //删除排序
    void deleteFunction(String id);

    //查询所有列表模型
    List<ListModel> findListModels();

    //通过控件id集合查询列表模型
    List<ListModel> findListModelsByItemModelIds(List<String> itemModelIds);

    // 因为ListModelEntity在ReferenceItemModelEntity和SelectItemModelEntity用的是单向关联，因此删除列表的时候要判断是否被引用了
    void checkListModelCanDelete(String id);
}
