package tech.ascs.icity.iform.service;

import java.util.List;
import java.util.Map;

import tech.ascs.icity.iform.api.model.FormInstance;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.jpa.service.JPAService;
import tech.ascs.icity.model.Page;

public interface FormInstanceService extends JPAService<FormModelEntity> {

	List<FormInstance> listFormInstance(ListModelEntity listModel, Map<String, String> queryParameters);

	Page<FormInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, String> queryParameters);

	FormInstance getFormInstance(FormModelEntity formModel, String instanceId);

	FormInstance newFormInstance(FormModelEntity formModel);

	String createFormInstance(FormModelEntity formModel, FormInstance formInstance);

	void updateFormInstance(FormModelEntity formModel, String instanceId, FormInstance formInstance);

	void deleteFormInstance(FormModelEntity formModel, String instanceId);
}
