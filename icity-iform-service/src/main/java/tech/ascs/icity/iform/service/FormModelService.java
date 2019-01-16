package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.FormModel;
import tech.ascs.icity.iform.api.model.ItemType;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;

public interface FormModelService extends JPAService<FormModelEntity> {
     //新增或编辑表单模型
     FormModelEntity saveFormModel(FormModel formModel);

     //获取表单模型的控件
     List<ItemModelEntity> getAllColumnItems(List<ItemModelEntity> itemModelEntities);

     ItemModelEntity getItemModelEntity(ItemType itemType);

     void deleteFormModelEntity(FormModelEntity formModel);

     List<ItemModelEntity> findAllItems(FormModelEntity entity);

     //保存检查校验
     FormModelEntity saveFormModelSubmitCheck(FormModelEntity entity);

}
