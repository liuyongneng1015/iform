package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.BtnPermission;
import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.jpa.service.JPAService;
import tech.ascs.icity.model.Page;

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

    /**
     * 查询列表模型的简要信息
     * @param name
     * @param applicationId
     * @param formId
     * @param hasAcvititi 列表对应的表单是否绑定了工作流
     * @return
     */
    List<ListModel> findListModelSimpleInfo(String name, String applicationId, String formId, boolean hasAcvititi);

    /**
     * 查询列表模型的简要分页信息
     * @param name
     * @param applicationId
     * @return
     */
    Page<ListModel> findListModelSimplePageInfo(String name, String applicationId, int page, int pagesize);

    //通过列表的id集合查询列表模型
    List<ListModel> findListModelSimpleByIds(List<String> ids);

    //通过控件id集合查询列表模型
    List<ListModel> findListModelsByItemModelIds(List<String> itemModelIds);

    // 因为ListModelEntity在ReferenceItemModelEntity和SelectItemModelEntity用的是单向关联，因此删除列表的时候要判断是否被引用了
    // 把关联了该列表模型的控件的列表置空
    void setItemReferenceListModelNull(String id);

    // 获取列表的按钮权限
    List<BtnPermission> findListBtnPermission(ListModelEntity entity);

    // 获取表单的按钮功能
    List<BtnPermission> findFormBtnPermission(FormModelEntity entity);

    // 提交列表的按钮功能的权限给admin服务
    void submitListBtnPermission(ListModelEntity entity);

    // 提交表单的按钮功能的权限给admin服务
    void submitFormBtnPermission(FormModelEntity entity);

    // 调用这个方法删除的列表的按钮功能的权限
    void deleteListBtnPermission(String listId);

    // 调用这个方法删除的表单的按钮功能的权限
    void deleteFormBtnPermission(String formId, List<String> listIds);

    //通过控件id集合查询列表模型
    List<ListModelEntity> findListModelsByItemModelId(String itemModelId);

    //通过表名集合查询列表模型
    ListModel getByTableName(String tableName);
}
