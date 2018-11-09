package tech.ascs.icity.iform.service;

import java.util.List;
import java.util.Map;

import tech.ascs.icity.iform.api.model.FormInstance;
import tech.ascs.icity.iform.api.model.TableDataModel;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.model.Page;

public interface FormInstanceServiceEx {

	List<FormInstance> listFormInstance(ListModelEntity listModel, Map<String, String> queryParameters);

	Page<FormInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, String> queryParameters);

	Page<String> pageByTableName(String tableName, int page, int pagesize);

	FormInstance getFormInstance(FormModelEntity formModel, String instanceId);

	FormInstance newFormInstance(FormModelEntity formModel);

	String createFormInstance(FormModelEntity formModel, FormInstance formInstance);

	void updateFormInstance(FormModelEntity formModel, String instanceId, FormInstance formInstance);

	void deleteFormInstance(FormModelEntity formModel, String instanceId);

	List<TableDataModel> findTableDataFormInstance(String formId, String id);
}
