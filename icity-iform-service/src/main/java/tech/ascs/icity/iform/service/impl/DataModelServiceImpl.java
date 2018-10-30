package tech.ascs.icity.iform.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.ColumnModel;
import tech.ascs.icity.iform.api.model.ColumnModelInfo;
import tech.ascs.icity.iform.api.model.DataModel;
import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.iform.api.model.IndexModel;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.model.IndexModelEntity;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
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

		if (dataModel.getMasterModel() != null) {
			old.setMasterModel(get(dataModel.getMasterModel().getId()));
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
			BeanUtils.copyProperties(column, columnEntity, new String[] {"dataModel"});
			columnEntity.setDataModel(old);
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

		return save(old, cloumnIds, indexIds);
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
			//TODO 处理查看行是否被关联,则提示“该字段被XXX表单XXX控件关联”
			for(String id : deletedCloumnIds) {
				ColumnModelEntity entity1 = columnManager.get(id);
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
