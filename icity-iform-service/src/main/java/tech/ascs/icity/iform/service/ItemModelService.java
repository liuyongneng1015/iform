package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.DisplayTimingType;
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

}
