package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.DataModel;
import tech.ascs.icity.iform.api.model.DataModelInfo;
import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;

public interface DataModelService extends JPAService<DataModelEntity> {

	DataModelEntity save(DataModel dataModel);

	void sync(DataModelEntity dataModel);

	List<DataModel> findDataModelByFormId(String formId);
}
