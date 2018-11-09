package tech.ascs.icity.iform.service.impl;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.DataModelService;
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

	public DataModelServiceImpl() {
		super(DataModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		columnManager = getJPAManagerFactory().getJPAManager(ColumnModelEntity.class);
		indexManager = getJPAManagerFactory().getJPAManager(IndexModelEntity.class);
	}

	@Override
	public DataModelEntity save(DataModel dataModel) {
		DataModelEntity old = dataModel.isNew() ? new DataModelEntity() : get(dataModel.getId());
		BeanUtils.copyProperties(dataModel, old, new String[] {"masterModel", "slaverModels", "columns", "indexes"});

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

		List<ColumnModelEntity> columns = new ArrayList<ColumnModelEntity>();
		//所以有旧的字段
		List<String> oldCloumnIds = new ArrayList<String>();
		for (ColumnModelEntity oldColumn : old.getColumns()) {
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
		//检查属性是否被关联
		checkRelevance(deleteCloumnIds);

		for (ColumnModel column : dataModel.getColumns()) {
			//是否为新的模型
			ColumnModelEntity columnEntity = column.isNew() ? new ColumnModelEntity() : columnManager.get(column.getId());

			Map<String, ReferenceType> referenceMap = new HashMap<String, ReferenceType>();
			List<ReferenceModel> referenceModelList = column.getReferenceModelList();
			//新关联行id
			List<String> newToColumnIds = new ArrayList<>();
			if(referenceModelList != null && referenceModelList.size() > 0) {
				for (ReferenceModel model : referenceModelList) {
					newToColumnIds.add(model.getToColumn().getId());
					referenceMap.put(model.getToColumn().getId(), model.getReferenceType());
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
				deleteOldColumnReferenceEntity(columnEntity, deleteOldToColumnIds, oldReferenceEntityList);
			}
			BeanUtils.copyProperties(column, columnEntity, new String[] {"dataModel"});
			columnEntity.setDataModel(old);
			columnEntity.setColumnReferences(oldReferenceEntityList);
			columnManager.save(columnEntity);

			//创建新的关联关系
			addNewColumnReferenceEntity(columnEntity, newToColumnIds, oldReferenceEntityList, referenceMap);
			columns.add(columnEntity);
		}

		old.setColumns(columns);

		List<IndexModelEntity> indexes = new ArrayList<IndexModelEntity>();
		List<String> indexIds = new ArrayList<String>(); // 用于存放需删除的索引列表
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
				indexColumns.add(getColumn(column.getColumnName(), columns));
			}
			indexEntity.setColumns(indexColumns);
			indexes.add(indexEntity);
		}

		old.setIndexes(indexes);

		return save(old, deleteCloumnIds, indexIds);
	}



	//删除旧的关联关系
	private void deleteOldColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> deleteOldToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList){
		//删除旧的关联
		List<ColumnModelEntity> toColumnModelEntityList = columnManager.query().filterIn("id",deleteOldToColumnIds).list();
		//删除正向关联的关系
		Set<DataModelEntity> dataModelEntities = columnEntity.getDataModel().getChildrenModels();
		Iterator<DataModelEntity> it = dataModelEntities.iterator();
		//Iterator<ColumnReferenceEntity> oldReference = oldReferenceEntityList.iterator();
		for(int i = 0 ; i < oldReferenceEntityList.size(); i++ ) {
			ColumnReferenceEntity referenceEntity = oldReferenceEntityList.get(i) ;
			if (deleteOldToColumnIds.contains(referenceEntity.getToColumn().getId())) {
				//删除数据模型的关系
				while (it.hasNext()) {
					DataModelEntity dataModelEntity = it.next();
					if (dataModelEntity.getTableName().equals(referenceEntity.getToColumn().getDataModel().getTableName())) {
						it.remove();
						referenceEntity.setFromColumn(null);
						referenceEntity.setToColumn(null);
						referenceEntity.setReferenceType(null);
					}
				}
				oldReferenceEntityList.remove(referenceEntity);
				i--;
			}
		}
		//删除反向关联的关系
		for(ColumnModelEntity toEntity : toColumnModelEntityList){
			List<ColumnReferenceEntity> toReferenceEntities = new ArrayList<>(toEntity.getColumnReferences());
			//Iterator<ColumnReferenceEntity> toReference = toReferenceEntities.iterator();
			for(int i = 0; i < toReferenceEntities.size(); i++){
				ColumnReferenceEntity reference = toReferenceEntities.get(i);
				if(StringUtils.equals(reference.getToColumn().getId(), columnEntity.getId())){
					Set<DataModelEntity> referenceDataModelEntities = reference.getFromColumn().getDataModel().getChildrenModels();
					//删除关联关系
					Iterator<DataModelEntity> iterator = referenceDataModelEntities.iterator();
					while(iterator.hasNext()) {
						DataModelEntity dataModelEntity = iterator.next() ;
						if (dataModelEntity.getTableName().equals(columnEntity.getDataModel().getTableName())) {
							iterator.remove();
						}
					}
					reference.setFromColumn(null);
					reference.setToColumn(null);
					reference.setReferenceType(null);
					toReferenceEntities.remove(reference);
					i--;
				}
			}
		}
	}

	//创建新的关联关系
	private void addNewColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> newToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList, Map<String, ReferenceType> referenceMap){
		if(!newToColumnIds.isEmpty()){
			List<ColumnModelEntity> newToColumnModelEntityList = columnManager.query().filterIn("id",newToColumnIds).list();
			DataModelEntity dataModelEntity = columnEntity.getDataModel();
			Set<DataModelEntity> childrenModelEntities = dataModelEntity.getChildrenModels();
			for(ColumnModelEntity addToEntity : newToColumnModelEntityList){

				Set<DataModelEntity> addToChildrenModelEntities = addToEntity.getDataModel().getChildrenModels();
				addToChildrenModelEntities.add(dataModelEntity);

				childrenModelEntities.add(addToEntity.getDataModel());
				//正向关联
				ColumnReferenceEntity addFromReferenceEntity = new ColumnReferenceEntity();
				addFromReferenceEntity.setFromColumn(columnEntity);
				addFromReferenceEntity.setToColumn(addToEntity);
				addFromReferenceEntity.setReferenceType(referenceMap.get(addToEntity.getId()));
				oldReferenceEntityList.add(addFromReferenceEntity);

				//反向关联
				ColumnReferenceEntity addToReferenceEntity = new ColumnReferenceEntity();
				addToReferenceEntity.setFromColumn(addToEntity);
				addToReferenceEntity.setToColumn(columnEntity);
				addToReferenceEntity.setReferenceType(ReferenceModel.getToReferenceType(addFromReferenceEntity.getReferenceType()));
				addToEntity.getColumnReferences().add(addToReferenceEntity);

			}
		}
	}

	//检查属性是否被关联
	@Transactional(readOnly = true)
	protected void checkRelevance(List<String> waitingDeletCloumnIds) {
		if (!waitingDeletCloumnIds.isEmpty()) {
			//TODO 处理查看行是否被关联,则提示“该字段被XXX表单XXX控件关联”
			for(String id : waitingDeletCloumnIds) {
				ColumnModelEntity entity = columnManager.get(id);
				List<ColumnReferenceEntity> columnReferenceEntityList = new ArrayList<>(entity.getColumnReferences());
				if(!columnReferenceEntityList.isEmpty()){
					ColumnModelEntity entity1 =columnReferenceEntityList.get(0).getToColumn();
					throw new IFormException(CommonUtils.exceptionCode, entity.getColumnName()+"被"+entity1.getDataModel().getTableName()+"表单" + entity1.getColumnName() + "控件关联");
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
		List<DataModel> dataModelList = new ArrayList<>();
		DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
		List<DataModelEntity> list = new ArrayList<>();
		list.add(dataModelEntity);
		list.addAll(dataModelEntity.getSlaverModels());
		list.addAll(dataModelEntity.getChildrenModels());
		for(DataModelEntity modelEntity : list){
			try {
				dataModelList.add(BeanUtils.copy(modelEntity, DataModel.class, new String[] {"slaverModels","masterModel","parentsModel","childrenModels","indexes"}));
			} catch (Exception e) {
				throw new IFormException("同步数据模型【" + dataModelEntity.getName() + "】转换失败：" + e.getMessage(), e);
			}
		}
		return dataModelList;
	}

	@Transactional(readOnly = false)
	protected DataModelEntity save(DataModelEntity entity, List<String> deletedCloumnIds, List<String> deletedIndexIds) {
		if (!deletedCloumnIds.isEmpty()) {
			List<ColumnModelEntity> dataModelEntities = columnManager.query().filterIn("id", deletedCloumnIds).list();
			for(int i = 0 ; i < dataModelEntities.size() ; i++ ){
				if( dataModelEntities.get(i).getItemModel() != null) {
					itemManager.delete(dataModelEntities.get(i).getItemModel());
				}
			}
			columnManager.deleteById(deletedCloumnIds.toArray(new String[] {}));

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
