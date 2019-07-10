package tech.ascs.icity.iform.service;

import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ReferenceItemModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;
import java.util.Map;

public interface FormModelService extends JPAService<FormModelEntity> {
     //新增或编辑表单模型
     FormModelEntity saveFormModel(FormModel formModel);

     //获取表单模型的控件
     List<ItemModelEntity> getAllColumnItems(List<ItemModelEntity> itemModelEntities);

     ItemModelEntity getItemModelEntity(ItemType itemType, SystemItemType systemItemType);

     void deleteFormModelEntityById(String id);

     List<ItemModelEntity> findAllItems(FormModelEntity entity);

     //保存检查校验
     FormModelEntity saveFormModelSubmitCheck(FormModelEntity entity);

     //流程绑定
     FormModelEntity saveFormModelProcessBind(FormModelEntity entity);

     //根据数据模型查询表单
     List<FormModelEntity> listByDataModel(DataModelEntity dataModelEntity);

     List<FormModelEntity> listByDataModelIds(List<String> dataModelIds);

     //获取关联数据标识控件
     List<ItemModelEntity> getReferenceItemModelList(ReferenceItemModelEntity itemModelEntity);

     void deleteItemOtherReferenceEntity(ItemModelEntity itemModelEntity);

     List<ItemModelEntity> getChildRenItemModelEntity(ItemModelEntity itemModelEntity);

     DataModel getDataModel(DataModelEntity dataModelEntity);

     ItemModelEntity findItemByTableAndColumName(String tableName, String columnName);

     List<ItemModel> findAllItemModels(List<ItemModel> itemModels);

     FormModelEntity findByTableName(String tableName);

     //查询流程建模表单
     List<FormModelEntity> findProcessApplicationFormModel(String key);

     //更新表单模型流程
    void saveFormModelProcess(FormModel formModel);


    //解析表单模型 parameters:模型控件默认参数
    AnalysisFormModel toAnalysisDTO(FormModelEntity entity, Map<String, Object> parameters);

    //表单实体转模型
     void entityToDTO(FormModelEntity entity, Object object, boolean isAnalysisForm, String parseArea, DefaultFunctionType functionType);

     //设置表单流程数据
     void setFlowParams(FormModelEntity entity, List<Activity> activities , boolean isFlowForm);

     //表单控件数据标识
      List<ItemModel> getItemModelList(List<String> idResultList);

     //设置表单实体装模型
     FormModel toDTODetail(FormModelEntity entity);

     //模型转实体
     FormModelEntity wrap(FormModel formModel);

     //校验表单名称
     void verifyFormModelName(FormModel formModel);

     //实体转model
     FormModel toDTO(FormModelEntity entity, boolean setFormProcessFlag);
}
