package tech.ascs.icity.iform.service.impl;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.ColumnModelService;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.iform.utils.CommonUtils;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.utils.BeanUtils;

public class DataModelServiceImpl extends DefaultJPAService<DataModelEntity> implements DataModelService {

	private JPAManager<ColumnModelEntity> columnManager;

	private JPAManager<IndexModelEntity> indexManager;

	private JPAManager<FormModelEntity> formManager;

	private JPAManager<ItemModelEntity> itemManager;

	private JPAManager<ColumnReferenceEntity> columnReferenceManager;

	@Autowired
	private IFormSessionFactoryBuilder sessionFactoryBuilder;

	@Autowired
	private ColumnModelService columnModelService;


	public DataModelServiceImpl() {
		super(DataModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		columnManager = getJPAManagerFactory().getJPAManager(ColumnModelEntity.class);
		indexManager = getJPAManagerFactory().getJPAManager(IndexModelEntity.class);
		formManager = getJPAManagerFactory().getJPAManager(FormModelEntity.class);
		itemManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
		columnReferenceManager = getJPAManagerFactory().getJPAManager(ColumnReferenceEntity.class);
	}

	@Override
	public DataModelEntity save(DataModel dataModel) {
		boolean flag = dataModel.isNew();
		DataModelEntity old = flag ? new DataModelEntity() : get(dataModel.getId());
		BeanUtils.copyProperties(dataModel, old, new String[] {"masterModel", "slaverModels", "columns", "indexes"});
		verifyTableName(old, dataModel.getTableName());

		//主从表
		setReferenceTable(dataModel, old);

		//所以有旧的字段
		List<String> oldCloumnIds = new ArrayList<String>();
		String idColumns = "";
		for (ColumnModelEntity oldColumn : old.getColumns()) {
			if(oldColumn.getColumnName().equals("id")){
				idColumns = oldColumn.getId();
			}
			oldCloumnIds.add(oldColumn.getId());
		}
		//所以带删除的旧的字段
		List<String> deleteCloumnIds = new ArrayList<String>();
		//所以有的字段(新的旧的)
		List<String> newCloumnIds = new ArrayList<String>();
		Map<String,Object> map = new HashMap<>();
		for (ColumnModel newColumn : dataModel.getColumns()) {
			if(map.get(newColumn.getColumnName()) != null){
				throw new IFormException("同步数据模型【" + dataModel.getName() + "】失败：字段" +newColumn.getColumnName()+"重复了");
			}
			map.put(newColumn.getColumnName(),newColumn.getColumnName());
			if(StringUtils.isNoneBlank(newColumn.getId())) {
				newCloumnIds.add(newColumn.getId());
			}
		}

		//待删除的行
		for(String oldClounm : oldCloumnIds) {
			if (!newCloumnIds.contains(oldClounm)){
				deleteCloumnIds.add(oldClounm);
			}
		}
		if(deleteCloumnIds.contains(idColumns)){
			deleteCloumnIds.remove(idColumns);
		}
		//检查属性是否被关联
		checkRelevance(deleteCloumnIds);
		//id行
		ColumnModelEntity idColumn = null;
		List<ColumnModelEntity> columnModelEntities = new ArrayList<ColumnModelEntity>();
		for (ColumnModel column : dataModel.getColumns()) {
			ColumnModelEntity columnModelEntity = setColumns(column);
			if(columnModelEntity.getColumnName().equals("id")){
				idColumn = columnModelEntity;
			}
			columnModelEntity.setDataModel(old);
			columnModelEntities.add(columnModelEntity);
		}
		if(idColumn == null){
			columnModelEntities.add(columnModelService.saveColumnModelEntity(old, "id"));
		}
		old.setColumns(columnModelEntities);

		List<IndexModelEntity> indexes = new ArrayList<IndexModelEntity>();
		List<String> indexIds = new ArrayList<String>(); // 用于存放需删除的索引列表
		setIndex(dataModel,  old,  indexes,  indexIds);

		old.setIndexes(indexes);

		if(dataModel.getModelType() == DataModelType.Slaver){
			old.setModelType(DataModelType.Slaver);
			columnModelService.saveColumnModelEntity(old, "master_id");
		}
		return save(old, deleteCloumnIds, indexIds);
	}

	//设置column
	private ColumnModelEntity setColumns(ColumnModel column){
		//是否为新的模型
		ColumnModelEntity columnEntity = new ColumnModelEntity();
		if(!column.isNew()){
			columnEntity = columnModelService.find(column.getId());
		}
		BeanUtils.copyProperties(column, columnEntity, new String[]{"dataModel","referenceTables"});
		if(column.isNew()){
			columnEntity.setId(null);
		}
		Map<String, ReferenceModel> referenceMap = new HashMap<String, ReferenceModel>();
		List<ReferenceModel> referenceModelList = column.getReferenceTables();
		//新关联行id
		List<String> newToColumnIds = new ArrayList<>();
		if(referenceModelList != null && referenceModelList.size() > 0) {
			for (ReferenceModel model : referenceModelList) {
				DataModelEntity dataModelEntity = findUniqueByProperty("tableName", model.getReferenceTable());
				if(dataModelEntity != null) {
					ColumnModelEntity columnModelEntity = columnModelService.saveColumnModelEntity(dataModelEntity, model.getReferenceValueColumn());
					newToColumnIds.add(columnModelEntity.getId());
					referenceMap.put(columnModelEntity.getId(), model);
				}
			}
		}

		//旧的关联实体
		List<ColumnReferenceEntity> oldReferenceEntityList = columnEntity.getColumnReferences();

		//旧关联行id
		List<String> oldToColumnIds = new ArrayList<>();
		for (ColumnReferenceEntity entity : oldReferenceEntityList) {
			oldToColumnIds.add(entity.getToColumn().getId());
		}
		//旧关联行id
		List<String> deleteOldToColumnIds = new ArrayList<String>();
		for(String oldToColumnId : oldToColumnIds) {
			if (!newToColumnIds.contains(oldToColumnId)){
				deleteOldToColumnIds.add(oldToColumnId);
			}else {
				newToColumnIds.remove(oldToColumnId);
			}
		}
		//删除旧的关联关系
		if(!deleteOldToColumnIds.isEmpty()) {
			columnModelService.deleteOldColumnReferenceEntity(columnEntity, deleteOldToColumnIds, oldReferenceEntityList);
		}
		columnEntity.setColumnReferences(oldReferenceEntityList);

		//创建新的关联关系
		addNewColumnReferenceEntity(columnEntity, newToColumnIds, oldReferenceEntityList, referenceMap);
		return columnEntity;
	}

	//设置索引
	private void setIndex(DataModel dataModel, DataModelEntity old, List<IndexModelEntity> indexes, List<String> indexIds){
		for (IndexModelEntity oldIndex : old.getIndexes()) {
			indexIds.add(oldIndex.getId());
		}
		for (IndexModel index : dataModel.getIndexes()) {
			IndexModelEntity indexEntity = index.isNew() ? new IndexModelEntity() : indexManager.get(index.getId());
			if (!indexEntity.isNew()) {
				indexIds.remove(indexEntity.getId());
			}
			BeanUtils.copyProperties(index, indexEntity, new String[] {"dataModel", "columns"});
			indexEntity.setDataModel(old);
			List<ColumnModelEntity> indexColumns = new ArrayList<ColumnModelEntity>();
			for (ColumnModelInfo column : index.getColumns()) {
				indexColumns.add(getColumn(column.getColumnName(), old.getColumns()));
			}
			indexEntity.setColumns(indexColumns);
			indexes.add(indexEntity);
		}
	}

	//设置主从表
	private void setReferenceTable(DataModel dataModel, DataModelEntity old){
		//主表
		if (dataModel.getMasterModel() != null) {
			old.setMasterModel(get(dataModel.getMasterModel().getId()));
		}
		//从表
		if (dataModel.getSlaverModels() != null && dataModel.getSlaverModels().size() > 0) {
			Object[] transactionsIds = dataModel.getSlaverModels().parallelStream().
					map(DataModelInfo::getId).toArray();
			old.setSlaverModels(query().filterIn("id", transactionsIds).list());
		}
	}

	private void verifyTableName(DataModelEntity old, String tableName){
		if(StringUtils.isNoneBlank(tableName)) {
			DataModelEntity dataModelEntity = findUniqueByProperty("tableName", tableName);
			if(dataModelEntity != null && !StringUtils.equals(dataModelEntity.getId(), old.getId())){
				throw new IFormException("表名重复了");
			}
		}
	}


	@Override
	public void deleteColumnReferenceEntity(ColumnModelEntity columnEntity){
		List<ColumnReferenceEntity> oldReferenceEntityList = columnEntity.getColumnReferences();
		//删除旧的关联
		List<String> deleteOldToColumnIds = new ArrayList<String>();
		for(ColumnReferenceEntity columnReferenceEntity : oldReferenceEntityList){
			deleteOldToColumnIds.add(columnReferenceEntity.getToColumn().getId());
		}
		columnModelService.deleteOldColumnReferenceEntity( columnEntity, deleteOldToColumnIds,  oldReferenceEntityList);
	}

	public void addColumnReferenceEntity(ColumnModelEntity columnEntity, Map<String, ReferenceType> referenceMap) {
		List<ColumnReferenceEntity> oldReferenceEntityList = columnEntity.getColumnReferences();
		//新增的关联
		List<String> addNewToColumnIds = new ArrayList<String>();
		for(ColumnReferenceEntity columnReferenceEntity : oldReferenceEntityList){
			addNewToColumnIds.add(columnReferenceEntity.getToColumn().getId());
		}
		//addNewColumnReferenceEntity(columnEntity, addNewToColumnIds,  oldReferenceEntityList, referenceMap);
	}

	//创建新的关联关系
	private void addNewColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> newToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList, Map<String, ReferenceModel> referenceMap){
		if(!newToColumnIds.isEmpty()){
			List<ColumnModelEntity> newToColumnModelEntityList = columnManager.query().filterIn("id",newToColumnIds).list();
			for(ColumnModelEntity addToEntity : newToColumnModelEntityList){
				columnModelService.saveColumnReferenceEntity(columnEntity, addToEntity, referenceMap.get(addToEntity.getId()).getReferenceType(), referenceMap.get(addToEntity.getId()).getReferenceMiddleTableName());
				columnManager.save(columnEntity);
				columnManager.save(addToEntity);
			}
		}
	}

	//检查属性是否被关联
	@Transactional(readOnly = true)
	protected void checkRelevance(List<String> waitingDeletCloumnIds) {
		if (!waitingDeletCloumnIds.isEmpty()) {
			//TODO 处理查看行是否被关联,则提示“字段被XXX表单XXX控件关联”
			for(String id : waitingDeletCloumnIds) {
				ColumnModelEntity entity = columnManager.get(id);
				List<ItemModelEntity> itemModelEntity = itemManager.findByProperty("columnModel.id", id);
				if(itemModelEntity != null && itemModelEntity.size() > 0) {
					for (ItemModelEntity itemModel : itemModelEntity) {
						throw new IFormException(CommonUtils.exceptionCode, entity.getColumnName() + "字段被" + itemModel.getFormModel().getName() + "表单" + itemModel.getName() + "控件关联");
					}
				}
			}
		}
	}

	@Override
	public void sync(DataModelEntity dataModel) {
		try {
			sessionFactoryBuilder.getSessionFactory(dataModel, true);
			dataModel.setSynchronized(true);
			save(dataModel);
		} catch (Exception e) {
			throw new IFormException("同步数据模型【" + dataModel.getName() + "】失败：" + e.getMessage(), e);
		}
		
	}

	@Override
	public List<DataModel> findDataModelByFormId(String formId) {
		FormModelEntity formModelEntity = formManager.find(formId);
		if(formModelEntity == null) {
			throw new IFormException("未找到【" + formId + "】表单模型");
		}
		List<DataModel> dataModelList = new ArrayList<>();
		DataModelEntity dataModelEntity = formModelEntity.getDataModels() == null || formModelEntity.getDataModels().size() < 1 ? null : formModelEntity.getDataModels().get(0);
		if(dataModelEntity != null) {
			List<DataModelEntity> list = new ArrayList<>();
			if(dataModelEntity.getMasterModel() != null ) {
				list.add(dataModelEntity.getMasterModel());
			}
			list.add(dataModelEntity);
			if(dataModelEntity.getSlaverModels() != null && dataModelEntity.getSlaverModels().size() > 0) {
				list.addAll(dataModelEntity.getSlaverModels());
			}
			for (DataModelEntity modelEntity : list) {
				dataModelList.add(entityToModel(modelEntity));
			}
		}
		return dataModelList;
	}

	private DataModel entityToModel(DataModelEntity modelEntity){
		DataModel dataModel = new DataModel();
		BeanUtils.copyProperties(modelEntity, dataModel, new String[] {"columns","slaverModels","masterModel","parentsModel","childrenModels","indexes"});
		if(modelEntity.getMasterModel() != null){
			DataModelInfo masterModel = new DataModelInfo();
			BeanUtils.copyProperties(modelEntity.getMasterModel(), masterModel, new String[]{"masterModel","slaverModels","columns", "indexes","referencesDataModel"});
			dataModel.setMasterModel(masterModel);
		}
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		List<ColumnModel> columnModels = new ArrayList<>();
		for(ColumnModelEntity columnModelEntity : columnModelEntities){
			List<ItemModelEntity> itemModelEntities = itemManager.findByProperty("columnModel.id", columnModelEntity.getId());
			ColumnModel columnModel = new ColumnModel();
			BeanUtils.copyProperties(columnModelEntity, columnModel, new String[] {"dataModel","columnReferences"});
			columnModel.setReferenceTables(columnModelService.getReferenceModel(columnModelEntity));
			columnModel.setReferenceItem(itemModelEntities == null || itemModelEntities.size() < 1 ? false : true) ;
			if(StringUtils.equals("master_id",columnModel.getColumnName())){
				columnModel.setReferenceItem(true);
			}
			columnModels.add(columnModel);
		}
		dataModel.setColumns(columnModels);
		return dataModel;
	}

	@Override
	public PCDataModel transitionToModel(String formId, DataModelEntity modelEntity){
		PCDataModel dataModel = new PCDataModel();
		BeanUtils.copyProperties(modelEntity, dataModel, new String[] {"columns","slaverModels","masterModel","parentsModel","childrenModels","indexes"});
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		List<ColumnModelInfo> columnModels = new ArrayList<>();
		for(ColumnModelEntity columnModelEntity : columnModelEntities){
			List<ItemModelEntity> itemModelEntities = itemManager.findByProperty("columnModel.id", columnModelEntity.getId());
			ColumnModelInfo columnModel = new ColumnModelInfo();
			BeanUtils.copyProperties(columnModelEntity, columnModel, new String[] {"dataModel","columnReferences"});
			columnModel.setReferenceTables(columnModelService.getReferenceModel(columnModelEntity));
			columnModel.setReferenceItem(itemModelEntities == null || itemModelEntities.size() < 1 ? false : true) ;
			columnModel.setTableName(modelEntity.getTableName());
			for(ItemModelEntity item : itemModelEntities) {
				if(item.getFormModel().getId().equals(formId)) {
					columnModel.setItemId(item.getId());
				}
			}
			columnModels.add(columnModel);
		}
		dataModel.setColumns(columnModels);
		return dataModel;
	}

	@Override
	public void deleteDataModel(DataModelEntity modelEntity) {
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		for(ColumnModelEntity columnModelEntity : columnModelEntities){
			List<ItemModelEntity> itemModelEntity = itemManager.findByProperty("columnModel.id", columnModelEntity.getId());
			if(itemModelEntity != null && itemModelEntity.size() > 0){
				for (ItemModelEntity itemModel : itemModelEntity) {
					throw new IFormException(CommonUtils.exceptionCode, columnModelEntity.getColumnName() + "字段被" + columnModelEntity.getDataModel().getTableName() + "表单" + itemModel.getName() + "控件关联");
				}
			}
		}
		for(ColumnModelEntity columnModelEntity : columnModelEntities){
			List<ColumnReferenceEntity> list = columnModelEntity.getColumnReferences();
			if(list == null || list.size() < 1){
				continue;
			}
			deleteColumnReferenceEntity(columnModelEntity);
		}
		delete(modelEntity);
	}

	@Transactional(readOnly = false)
	protected DataModelEntity save(DataModelEntity entity, List<String> deletedCloumnIds, List<String> deletedIndexIds) {
		if (!deletedCloumnIds.isEmpty()) {
			List<ColumnModelEntity> columnModelEntities = columnManager.query().filterIn("id", deletedCloumnIds).list();
			for(int j = 0;j < columnModelEntities.size() ; j++){
				ColumnModelEntity columnModelEntity = columnModelEntities.get(j);
				List<ItemModelEntity> itemModelEntity = itemManager.findByProperty("columnModel.id", columnModelEntity.getId());
				if(itemModelEntity != null){
					for(ItemModelEntity itemModel : itemModelEntity){
						itemModel.setColumnModel(null);
						itemManager.save(itemModel);
					}
				}

				List<ColumnReferenceEntity> columnReferences = columnModelEntities.get(j).getColumnReferences();
				List<String> ids = new ArrayList<>();
				for(ColumnReferenceEntity columnReferenceEntity : columnReferences){
					ids.add(columnReferenceEntity.getToColumn().getId());
				}
				columnModelService.deleteOldColumnReferenceEntity(columnModelEntities.get(j), ids, columnReferences);
				columnModelService.delete(columnModelEntities.get(j));
				columnModelEntities.remove(columnModelEntities.get(j));
				j--;
			}
		}
		if (deletedIndexIds.size() > 0) {
			indexManager.deleteById(deletedIndexIds.toArray(new String[] {}));
		}
		if (entity.getModelType() == DataModelType.Slaver) {
			entity = save(entity);
			entity.getMasterModel().setSynchronized(false);
			save(entity.getMasterModel());
			return entity;
		} else {
			entity.setSynchronized(false);
			return save(entity);
		}
	}

	private ColumnModelEntity getColumn(String columnName, List<ColumnModelEntity> columns) {
		for (ColumnModelEntity column : columns) {
			if (columnName.equals(column.getColumnName())) {
				return column;
			}
		}
		throw new IFormException("字段【" + columnName + "】不存在");
	}

	protected void validate(DataModelEntity entity) {
		if (entity.getModelType() == DataModelType.Slaver) {
			if (entity.getMasterModel() == null) {
				throw new IFormException("必须指定主数据模型");
			}
		} else {
			if (entity.getMasterModel() != null) {
				entity.setModelType(DataModelType.Slaver);
			}
		}
	}
}
