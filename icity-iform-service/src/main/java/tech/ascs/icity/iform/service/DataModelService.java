package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.ColumnReferenceEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;
import java.util.Map;

public interface DataModelService extends JPAService<DataModelEntity> {

	DataModelEntity save(DataModel dataModel);

	void sync(DataModelEntity dataModel);

	List<DataModel> findDataModelByFormId(String formId);

	void deleteColumnReferenceEntity(ColumnModelEntity columnEntity);

	PCDataModel transitionToModel(String formId, DataModelEntity modelEntity);

	void deleteDataModel(DataModelEntity modelEntity);
}
