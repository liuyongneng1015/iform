package tech.ascs.icity.iform.service.impl;

import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.iform.api.model.ColumnType;
import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.ColumnModelService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.List;
import java.util.stream.Collectors;

public class ColumnModelServiceImpl extends DefaultJPAService<ColumnModelEntity> implements ColumnModelService {

	private JPAManager<ColumnReferenceEntity> columnReferenceManager;

	private JPAManager<ColumnModelEntity> columnModelManager;

	public ColumnModelServiceImpl() {
		super(ColumnModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		columnReferenceManager = getJPAManagerFactory().getJPAManager(ColumnReferenceEntity.class);
		columnModelManager = getJPAManagerFactory().getJPAManager(ColumnModelEntity.class);
	}

	@Override
	public ColumnModelEntity saveColumnModelEntity(DataModelEntity dataModel, String columnName) {
		List<ColumnModelEntity> list = dataModel.getColumns();
		if (list != null) {
			for(ColumnModelEntity columnModelEntity : list) {
				if (columnModelEntity.getColumnName().equals(columnName)) {
					return columnModelEntity;
				}
			}
		}
		return saveColumns(dataModel, columnName);
	}

	private ColumnModelEntity saveColumns(DataModelEntity dataModel, String columnName){
		ColumnModelEntity columnModelEntity = new ColumnModelEntity();
		columnModelEntity.setDataModel(dataModel);
		columnModelEntity.setColumnName(columnName);
		if("id".equals(columnName)) {
			columnModelEntity.setDescription("主键");
			columnModelEntity.setName("主键id");
		}else{
			columnModelEntity.setDescription("关联字段");
			columnModelEntity.setName("关联字段id");
		}
		columnModelEntity.setDataType(ColumnType.String);
		columnModelEntity.setLength(32);
		columnModelEntity.setPrecision(32);
		columnModelEntity.setScale(null);
		columnModelEntity.setNotNull(true);
		columnModelEntity.setKey(true);
		columnModelEntity.setDefaultValue("-1");
		dataModel.getColumns().add(columnModelEntity);
		return columnModelEntity;
	}



	//删除旧的关联关系
	@Override
	public void deleteOldColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> deleteOldToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList){
		//删除旧的关联
		List<ColumnModelEntity> toColumnModelEntityList = columnModelManager.query().filterIn("id",deleteOldToColumnIds).list();
		//删除正向关联的关系
		for(int i = 0 ; i < oldReferenceEntityList.size(); i++ ) {
			ColumnReferenceEntity referenceEntity = oldReferenceEntityList.get(i) ;
			if (deleteOldToColumnIds.contains(referenceEntity.getToColumn().getId())) {
				oldReferenceEntityList.remove(referenceEntity);
				i--;
				columnReferenceManager.delete(referenceEntity);
			}
		}
		//删除反向关联的关系
		for(ColumnModelEntity toEntity : toColumnModelEntityList){
			List<ColumnReferenceEntity> toReferenceEntities = toEntity.getColumnReferences();
			for(int i = 0; i < toReferenceEntities.size(); i++){
				ColumnReferenceEntity reference = toReferenceEntities.get(i);
				if(StringUtils.equals(reference.getToColumn().getId(), columnEntity.getId())){
					toReferenceEntities.remove(reference);
					i--;
					columnReferenceManager.delete(reference);
				}
			}
		}
	}

	@Override
	public void saveColumnReferenceEntity(ColumnModelEntity fromColumnEntity, ColumnModelEntity toColumnEntity, ReferenceType referenceType) {
		//关联关系
		List<ColumnReferenceEntity> columnReferenceEntityList = toColumnEntity.getColumnReferences();
		List<String> referenceColumnName = columnReferenceEntityList.parallelStream().map(ColumnReferenceEntity::getFromColumn).map(ColumnModelEntity::getColumnName).collect(Collectors.toList());
		if(!referenceColumnName.contains(fromColumnEntity.getColumnName())){
			//正向关联
			ColumnReferenceEntity columnReferenceEntity = new ColumnReferenceEntity();
			columnReferenceEntity.setFromColumn(fromColumnEntity);
			columnReferenceEntity.setToColumn(toColumnEntity);
			columnReferenceEntity.setReferenceType(referenceType);
			fromColumnEntity.getColumnReferences().add(columnReferenceEntity);

			//反向关联
			ColumnReferenceEntity reverseColumnReferenceEntity = new ColumnReferenceEntity();
			reverseColumnReferenceEntity.setFromColumn(toColumnEntity);
			reverseColumnReferenceEntity.setToColumn(fromColumnEntity);
			reverseColumnReferenceEntity.setReferenceType(ReferenceType.getReverseReferenceType(referenceType));
			columnReferenceEntityList.add(reverseColumnReferenceEntity);
		}
	}

	@Override
	public void deleteColumn(ColumnModelEntity columnEntity) {
		delete(columnEntity);
	}
}
