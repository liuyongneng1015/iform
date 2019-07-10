package tech.ascs.icity.iform.service;

import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iform.api.model.DisplayTimingType;
import tech.ascs.icity.iform.api.model.FormModel;
import tech.ascs.icity.iform.api.model.ItemModel;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ItemPermissionInfo;
import tech.ascs.icity.iform.model.ReferenceItemModelEntity;
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
}
