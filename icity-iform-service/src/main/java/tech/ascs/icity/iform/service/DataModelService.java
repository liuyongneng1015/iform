package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.DataModel;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

public interface DataModelService extends JPAService<DataModelEntity> {

	DataModelEntity save(DataModel dataModel);

	void sync(DataModelEntity dataModel);
}
