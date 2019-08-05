package tech.ascs.icity.iform.service;

import java.util.List;
import java.util.Map;

import tech.ascs.icity.admin.api.model.TreeSelectData;
import tech.ascs.icity.admin.api.model.User;
import tech.ascs.icity.admin.api.model.UserBase;
import tech.ascs.icity.iflow.api.model.ProcessInstance;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

public interface FormInstanceServiceEx {

	List<FormInstance> listInstance(ListModelEntity listModel, Map<String, Object> queryParameters);

	List<FormDataSaveInstance> formInstance(ListModelEntity listModel, FormModelEntity formModel, Map<String, Object> queryParameters);

	List<Map<String, Object>> findFormInstanceByColumnMap(FormModelEntity formModel, Map<String, Object> queryParameters);

	Page<FormDataSaveInstance> pageFormInstance(FormModelEntity formModel, int page, int pagesize, Map<String, Object> queryParameters);

	Page<FormDataSaveInstance> pageListInstance(ListModelEntity listModel, int page, int pagesize, Map<String, Object> queryParameters);

	Page<String> pageByTableName(String tableName, int page, int pagesize);

	FormInstance getFormInstance(FormModelEntity formModel, String instanceId);

	FormDataSaveInstance getQrCodeFormDataSaveInstance(ListModelEntity listModel, String instanceId);

	FormInstance newFormInstance(FormModelEntity formModel);

	String createFormInstance(FormModelEntity formModel, FormDataSaveInstance formInstance);

	void updateFormInstance(FormModelEntity formModel, String instanceId, FormDataSaveInstance formInstance);

	void deleteFormInstance(FormModelEntity formModel, String instanceId);

	//赋值itemmodel
	void updateValue(ItemModelEntity itemModel, ItemInstance itemInstance, Object value);

	List<UserBase> getUserInfoByIds(List<String> ids);

    FormDataSaveInstance getFormDataSaveInstance(FormModelEntity formModel, String id);

    List<String> setSelectItemDisplayValue(ItemInstance itemInstance, SelectItemModelEntity selectItemModelEntity, List<String> list);

	List<TreeSelectData> getTreeSelectData(TreeSelectDataSource dataSourceType, String[] ids, String dictionaryId);

	Map<String,String> columnNameAndItemIdMap(List<ItemModelEntity> items);

	//启动表单实例
	IdEntity startFormInstanceProcess(FormModelEntity formModel, String id);

	//设置表单流程
    void setFlowFormInstance(FormModelEntity formModelEntity, ProcessInstance processInstance, FormDataSaveInstance instance);

    //通过表单控件字段保存表单数据
	Map<String, Object> saveFormInstance(FormModelEntity formModel, Map<String, Object> parameters);

	//根据字段参数查询表单分页数据
	Page<FormDataSaveInstance> pageByColumnMap(FormModelEntity formModel, int page, int pagesize, Map<String, Object> parameters);

	//根据字段参数查询表单数据
	List<FormDataSaveInstance> findByColumnMap(FormModelEntity formModel, Map<String, Object> columnMap);

	//删除表单数据
    void deleteFormData(FormModelEntity formModelEntity);

	/**
	 * 删除校验, 删除某行数据的时候校验当前数据是否被引用
	 */
    void deleteVerify(ColumnReferenceEntity columnReferenceEntity, Map<String, Object> entity, List<ReferenceItemModelEntity> itemModelEntities);

	/**
	 * 执行业务触发器
	 */
	void sendWebService(FormModelEntity formModelEntity, BusinessTriggerType triggerType, Map<String, Object> data, String id);

}
