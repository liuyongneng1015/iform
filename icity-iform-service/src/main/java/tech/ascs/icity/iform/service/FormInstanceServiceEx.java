package tech.ascs.icity.iform.service;

import java.util.List;
import java.util.Map;

import tech.ascs.icity.admin.api.model.User;
import tech.ascs.icity.iform.api.model.FormDataSaveInstance;
import tech.ascs.icity.iform.api.model.FormInstance;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.model.Page;

public interface FormInstanceServiceEx {

	List<FormInstance> listFormInstance(ListModelEntity listModel, Map<String, Object> queryParameters);

	Page<FormDataSaveInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, Object> queryParameters);

	Page<String> pageByTableName(String tableName, int page, int pagesize);

	FormInstance getFormInstance(FormModelEntity formModel, String instanceId);

	FormDataSaveInstance getQrCodeFormDataSaveInstance(ListModelEntity listModel, String instanceId);

	FormInstance newFormInstance(FormModelEntity formModel);

	String createFormInstance(FormModelEntity formModel, FormDataSaveInstance formInstance);

	void updateFormInstance(FormModelEntity formModel, String instanceId, FormDataSaveInstance formInstance);

	void deleteFormInstance(FormModelEntity formModel, String instanceId);

	//赋值itemmodel
	void updateValue(ItemModelEntity itemModel, ItemInstance itemInstance, Object value);

	List<User> getUserInfoByIds(List<String> ids);
}
