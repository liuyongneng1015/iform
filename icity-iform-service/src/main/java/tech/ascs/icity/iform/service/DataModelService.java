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

	PCDataModel transitionToModel(String formId, DataModelEntity modelEntity, List<String> displayColuns);

	void deleteDataModel(DataModelEntity modelEntity);

	// 单个删除或者批量删除时，校验模型是否被关联
	void checkDataModelIsReference(List<DataModelEntity> list);

	//更新索引
	void updateDataModelIndex(DataModelEntity modelEntity);

	//查询表单所有索引
	List<String> listDataIndexName(String tableName);

	//删除表单无需校验
	void deleteDataModelWithoutVerify(DataModelEntity modelEntity);

	List<DataModel> queryAllList();
}
