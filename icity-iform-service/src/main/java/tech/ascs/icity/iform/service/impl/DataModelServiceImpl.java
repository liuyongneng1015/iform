package tech.ascs.icity.iform.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.ColumnReferenceEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.model.IndexModelEntity;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.iform.utils.CommonUtils;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.utils.BeanUtils;

public class DataModelServiceImpl extends DefaultJPAService<DataModelEntity> implements DataModelService {

	private JPAManager<ColumnModelEntity> columnManager;

	private JPAManager<IndexModelEntity> indexManager;

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
		if (!dataModel.getSlaverModels().isEmpty()) {
			//TODO 获取从表id集合
			Object[] transactionsIds = dataModel.getSlaverModels().parallelStream().
					map(DataModelInfo::getId).toArray();
			old.setSlaverModels(query().filterIn("id", transactionsIds).list());
		}

		List<ColumnModelEntity> columns = new ArrayList<ColumnModelEntity>();
		List<String> cloumnIds = new ArrayList<String>(); // 用于存放需删除的字段列表
		for (ColumnModelEntity oldColumn : old.getColumns()) {
			cloumnIds.add(oldColumn.getId());
		}
		for (ColumnModel column : dataModel.getColumns()) {
			ColumnModelEntity columnEntity = column.isNew() ? new ColumnModelEntity() : columnManager.get(column.getId());
			if (!columnEntity.isNew()) {
				cloumnIds.remove(columnEntity.getId());
			}
			Map<String, ReferenceType> referenceMap = new HashMap<String, ReferenceType>();
			List<ReferenceModel> referenceModelList = column.getReferenceModelList();
			//新关联行id
			List<String> newToColumnIds = new ArrayList<>();
			if(!referenceModelList.isEmpty()) {
				for (ReferenceModel model : referenceModelList) {
					newToColumnIds.add(model.getToColumnModel().getId());
					referenceMap.put(model.getToColumnModel().getId(), model.getReferenceType());
				}
			}
			//旧的关联实体
			List<ColumnReferenceEntity> oldReferenceEntityList = columnEntity.getColumnReferences();
			if(oldReferenceEntityList.isEmpty()){
				oldReferenceEntityList = new ArrayList<>();
			}
			//旧关联行id
			List<String> oldToColumnIds = new ArrayList<>();
			for(ColumnReferenceEntity entity: oldReferenceEntityList){
				oldToColumnIds.add(entity.getToColumn().getId());
			}
			//旧关联行id
			List<String> oldToColumnIdList = new ArrayList<>(oldToColumnIds);

			//删除旧的关联关系
			deleteOlbColumnReferenceEntity(columnEntity, oldToColumnIds, newToColumnIds, oldReferenceEntityList);


			//待增新的
			newToColumnIds.removeAll(oldToColumnIdList);
			//创建新的关联关系
			addNewColumnReferenceEntity(columnEntity, newToColumnIds, oldReferenceEntityList, referenceMap);

			BeanUtils.copyProperties(column, columnEntity, new String[] {"dataModel"});
			columnEntity.setDataModel(old);
			columns.add(columnEntity);
		}
		//检查属性是否被关联
		checkRelevance(cloumnIds);

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

		return save(old, cloumnIds, indexIds);
	}


	//删除旧的关联关系
	private void deleteOlbColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> oldToColumnIds, List<String> newToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList){
		//删除旧的关联
		oldToColumnIds.removeAll(newToColumnIds);
		List<ColumnModelEntity> toColumnModelEntityList = columnManager.query().filterIn("id",oldToColumnIds).list();
		//删除正向关联的关系
		for(ColumnReferenceEntity referenceEntity: oldReferenceEntityList){
			if(oldToColumnIds.contains(referenceEntity.getToColumn().getId())){
				oldReferenceEntityList.remove(referenceEntity);
			}
		}
		//删除方向关联的关系
		for(ColumnModelEntity toEntity : toColumnModelEntityList){
			List<ColumnReferenceEntity> toReferenceEntities = toEntity.getColumnReferences();
			for(ColumnReferenceEntity reference : toReferenceEntities){
				if(StringUtils.equals(reference.getToColumn().getId(), columnEntity.getId())){
					toReferenceEntities.remove(reference);
				}
			}
		}
	}

	//创建新的关联关系
	private void addNewColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> newToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList, Map<String, ReferenceType> referenceMap){
		if(!newToColumnIds.isEmpty()){
			List<ColumnModelEntity> newToColumnModelEntityList = columnManager.query().filterIn("id",newToColumnIds).list();
			for(ColumnModelEntity addToEntity : newToColumnModelEntityList){
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
	protected void checkRelevance(List<String> deletedCloumnIds) {
		if (!deletedCloumnIds.isEmpty()) {
			//TODO 处理查看行是否被关联,则提示“该字段被XXX表单XXX控件关联”
			for(String id : deletedCloumnIds) {
				ColumnModelEntity entity = columnManager.get(id);
				List<ColumnReferenceEntity> columnReferenceEntityList = entity.getColumnReferences();
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

	@Transactional(readOnly = false)
	protected DataModelEntity save(DataModelEntity entity, List<String> deletedCloumnIds, List<String> deletedIndexIds) {
		if (!deletedCloumnIds.isEmpty()) {
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
