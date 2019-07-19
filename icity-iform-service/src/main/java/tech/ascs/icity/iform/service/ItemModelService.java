package tech.ascs.icity.iform.service;

import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iform.api.model.DisplayTimingType;
import tech.ascs.icity.iform.api.model.FormModel;
import tech.ascs.icity.iform.api.model.ItemModel;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;
import java.util.Map;

public interface ItemModelService extends JPAService<ItemModelEntity> {

    List<ReferenceItemModelEntity> findRefenceItemByFormModelId(String formModelId);

    ItemModelEntity saveItemModelEntity(FormModelEntity formModelEntity, String itemModelName);

    Map<String, ItemPermissionInfo> findItemPermissionByDisplayTimingType(FormModelEntity formModelEntity, DisplayTimingType displayTimingType);

    void copyItemModelEntityToItemModel(ItemModelEntity itemModelEntity, ItemModel itemModel);

    void copyItemModelToItemModelEntity(ItemModel itemModel, ItemModelEntity itemModelEntity);

    //控件实体装模型
    ItemModel toDTO(ItemModelEntity entity, boolean isAnalysisItem, String tableName);

    //设置控件权限
    void setFormItemActvitiy(List<ItemModel> itemModels, List<Activity> activities);

    //控件模型转实体
    ItemModelEntity wrap(String sourceFormModelId, ItemModel itemModel);

    //更新控件实体
    void setItemModelEntity(FormModel formModel, ItemModel itemModel, FormModelEntity entity, List<ItemModelEntity> items,
                            List<ItemModelEntity> itemModelEntityList, Map<String, List<ItemModelEntity>> formMap);

    /**
     * 返回
     * {
     *     "item": itemModelEntity,
     *     "level": int
     * }
     */
    // 联动数据解绑查询时，要找到最原始节点的item控件，以及它们之间相隔了多少层来获取对应层数的数据
    Map<String, Object> findLinkageOriginItemModelEntity(String id);
}
