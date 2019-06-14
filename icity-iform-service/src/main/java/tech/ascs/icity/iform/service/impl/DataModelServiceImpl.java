package tech.ascs.icity.iform.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

	@Autowired
	private FormModelService formModelService;

	@Autowired
	JdbcTemplate jdbcTemplate;

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
		Map<String, ColumnModelEntity> oldCloumnMap = new HashMap<>();
		String idColumns = "";
		for (ColumnModelEntity oldColumn : old.getColumns()) {
			if(oldColumn.getColumnName().equals("id")){
				idColumns = oldColumn.getId();
			}
			oldCloumnMap.put(oldColumn.getId(), oldColumn);
		}
		//所以带删除的旧的字段
		List<ColumnModelEntity> deleteCloumns = new ArrayList<ColumnModelEntity>();
		//所以有的字段(新的旧的)
		List<String> newCloumnIds = new ArrayList<String>();
		Map<String,Object> map = new HashMap<>();
		for (ColumnModel newColumn : dataModel.getColumns()) {
			if(StringUtils.isBlank(newColumn.getColumnName().trim())){
				throw new IFormException("字段名不能为空");
			}
			if(map.get(newColumn.getColumnName()) != null){
				throw new IFormException("同步数据模型【" + dataModel.getName() + "】失败：字段" +newColumn.getColumnName()+"重复了");
			}
			map.put(newColumn.getColumnName(),newColumn.getColumnName());
			if(StringUtils.isNoneBlank(newColumn.getId())) {
				newCloumnIds.add(newColumn.getId());
			}
		}

		//待删除的行
		for(String oldClounm : oldCloumnMap.keySet()) {
			if (!newCloumnIds.contains(oldClounm) && !idColumns.equals(oldClounm)){
				deleteCloumns.add(oldCloumnMap.get(oldClounm));
			}
		}

		//检查属性是否被关联
		checkRelevance(deleteCloumns);
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

		List<String> columnList = columnModelEntities.parallelStream().map(ColumnModelEntity::getColumnName).collect(Collectors.toList());
		if(!columnList.contains("create_at")) {
			columnModelEntities.add(columnModelService.saveColumnModelEntity(old, "create_at"));
		}
		if(!columnList.contains("update_at")) {
			columnModelEntities.add(columnModelService.saveColumnModelEntity(old, "update_at"));
		}
		if(!columnList.contains("create_by")) {
			columnModelEntities.add(columnModelService.saveColumnModelEntity(old, "create_by"));
		}
		if(!columnList.contains("update_by")) {
			columnModelEntities.add(columnModelService.saveColumnModelEntity(old, "update_by"));
		}

		old.setColumns(columnModelEntities);

		List<IndexModelEntity> indexes = new ArrayList<IndexModelEntity>();
		List<IndexModelEntity> deleteIndexes = setDeleteIndexs(dataModel,  old,  indexes);// 用于存放需删除的索引列表

		old.setIndexes(indexes);

		if(dataModel.getModelType() == DataModelType.Slaver){
			old.setModelType(DataModelType.Slaver);
			columnModelService.saveColumnModelEntity(old, "master_id");
		}
		return save(old, deleteCloumns, deleteIndexes);
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
	private List<IndexModelEntity> setDeleteIndexs(DataModel dataModel, DataModelEntity old, List<IndexModelEntity> indexes){
		Map<String, IndexModelEntity> indexModelEntityMap = new HashMap<>();
		for (IndexModelEntity oldIndex : old.getIndexes()) {
			indexModelEntityMap.put(oldIndex.getId(), oldIndex);
		}
		String tableName = old.getPrefix() == null ? old.getTableName(): old.getPrefix()+old.getTableName();
		Map<String, Object> map = new HashMap<>();
		List<String> list = listDataIndexName(tableName);
		for (IndexModel index : dataModel.getIndexes()) {
			 if(map.get(index.getName()) != null){
			 	throw new IFormException("索引名"+index.getName()+"重复了");
			 }
			 map.put(index.getName(), System.currentTimeMillis());
			IndexModelEntity indexEntity = index.isNew() ? new IndexModelEntity() : indexModelEntityMap.remove(index.getId());
			if(indexEntity == null){
				indexEntity = new IndexModelEntity();
			}
			if(!index.isNew() && !index.getName().equals(indexEntity.getName()) && list.contains(indexEntity.getName())){
				String odltableName = old.getPrefix() == null ? old.getTableName(): old.getPrefix()+old.getTableName();
				columnModelService.deleteTableIndex(odltableName, indexEntity.getName());
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

		return new ArrayList<>(indexModelEntityMap.values());
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
				throw new IFormException("数据模型表名重复了");
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
	protected void checkRelevance(List<ColumnModelEntity> waitingDeletCloumns) {
		if (!waitingDeletCloumns.isEmpty() &&  waitingDeletCloumns.size() > 0 ) {
			String tableName = waitingDeletCloumns.get(0).getDataModel().getTableName();
			//TODO 处理查看行是否被关联,则提示“字段被XXX表单XXX控件关联”
			for(ColumnModelEntity entity : waitingDeletCloumns) {
				verifyColumReferenceItem(tableName, entity);
			}
		}
	}

	private void verifyColumReferenceItem(String tableName, ColumnModelEntity entity){
		List<ItemModelEntity> itemModelEntityList = itemManager.findByProperty("columnModel.id", entity.getId());
		if(itemModelEntityList != null && itemModelEntityList.size() > 0) {
			for (ItemModelEntity itemModel : itemModelEntityList) {
				FormModelEntity formModelEntity  = itemModel.getFormModel();
				if(formModelEntity == null && itemModel.getSourceFormModelId() != null){
					formModelEntity  = formModelService.get(itemModel.getSourceFormModelId());
				}
				throw new IFormException(CommonUtils.exceptionCode, tableName + "数据表的" + entity.getColumnName() + "字段被" + (formModelEntity == null ? "" : formModelEntity.getName()) + "表单" + itemModel.getName() + "控件关联");
			}
		}
	}

	@Override
	public void sync(DataModelEntity dataModel) {
		try {
			sessionFactoryBuilder.getSessionFactory(dataModel, true);
			dataModel.setSynchronized(true);
			for(DataModelEntity slaverDataModelEntity : dataModel.getSlaverModels()){
				slaverDataModelEntity.setSynchronized(true);
			}
			save(dataModel);
			updateDataModelIndex(dataModel);
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
				dataModelList.add(entityToModel(formModelEntity, modelEntity));
			}
		}
		return dataModelList;
	}

	private DataModel entityToModel(FormModelEntity formModelEntity, DataModelEntity modelEntity){
		DataModel dataModel = new DataModel();
		BeanUtils.copyProperties(modelEntity, dataModel, new String[] {"columns","slaverModels","masterModel","parentsModel","childrenModels","indexes"});
		if(modelEntity.getMasterModel() != null){
			DataModelInfo masterModel = new DataModelInfo();
			BeanUtils.copyProperties(modelEntity.getMasterModel(), masterModel, new String[]{"masterModel","slaverModels","columns", "indexes","referencesDataModel"});
			dataModel.setMasterModel(masterModel);
		}
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		List<ColumnModel> columnModels = new ArrayList<>();
		Map<String, ItemModelEntity> itemModelEntityMap = new HashMap<>();
		if(formModelEntity != null) {
			List<ItemModelEntity> itemModelEntityList = formModelService.findAllItems(formModelEntity);
			for (ItemModelEntity itemModelEntity1 : itemModelEntityList) {
				if (itemModelEntity1.getColumnModel() != null) {
					itemModelEntityMap.put(itemModelEntity1.getColumnModel().getId(), itemModelEntity1);
				}
			}
		}
		for(ColumnModelEntity columnModelEntity : columnModelEntities){
			ColumnModel columnModel = new ColumnModel();
			BeanUtils.copyProperties(columnModelEntity, columnModel, new String[] {"dataModel","columnReferences"});
			columnModel.setReferenceTables(columnModelService.getReferenceModel(columnModelEntity));
			columnModel.setReferenceItem(itemModelEntityMap.get(columnModelEntity.getId()) != null ? true : false) ;
			if(StringUtils.equals("master_id",columnModel.getColumnName())){
				columnModel.setReferenceItem(true);
			}
			columnModels.add(columnModel);
		}
		dataModel.setColumns(columnModels);
		return dataModel;
	}

	@Override
	public AnalysisDataModel transitionToModel(String formId, DataModelEntity modelEntity, List<String> displayColuns){
		AnalysisDataModel dataModel = new AnalysisDataModel();
		BeanUtils.copyProperties(modelEntity, dataModel, new String[] {"columns","slaverModels","masterModel","parentsModel","childrenModels","indexes"});
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		List<ColumnModelInfo> columnModels = new ArrayList<>();
		FormModelEntity formModelEntity = formManager.get(formId);
		List<String> list = formModelService.getAllColumnItems(formModelEntity.getItems()).parallelStream().map(ItemModelEntity::getId).collect(Collectors.toList());
		Map<String, ColumnModelEntity> columnModelEntityMap = new HashMap<>();
		for(ColumnModelEntity columnModelEntity : columnModelEntities) {
			//主表全显示
			if (displayColuns != null && displayColuns.size() > 0 && !displayColuns.contains(columnModelEntity.getColumnName())) {
				continue;
			}
			columnModelEntityMap.put(columnModelEntity.getColumnName(), columnModelEntity);
		}
		for(String columnName : displayColuns){
			ColumnModelEntity columnModelEntity = columnModelEntityMap.get(columnName);
			List<ItemModelEntity> itemModelEntities = itemManager.findByProperty("columnModel.id", columnModelEntity.getId());
			ColumnModelInfo columnModel = new ColumnModelInfo();
			BeanUtils.copyProperties(columnModelEntity, columnModel, new String[] {"dataModel","columnReferences"});
			columnModel.setReferenceTables(columnModelService.getReferenceModel(columnModelEntity));
			columnModel.setReferenceItem(itemModelEntities == null || itemModelEntities.size() < 1 ? false : true) ;
			columnModel.setTableName(modelEntity.getTableName());
			for(ItemModelEntity item : itemModelEntities) {
				if (list.contains(item.getId())) {
					columnModel.setItemId(item.getId());
					columnModel.setItemName(item.getName());
				}
			}
			columnModels.add(columnModel);
		}
		dataModel.setColumns(columnModels);
		return dataModel;
	}

	@Override
	public void deleteDataModel(DataModelEntity modelEntity) {
		veryDataModelColumnModel(modelEntity);
		if(modelEntity.getSlaverModels() != null){
			for(DataModelEntity dataModelEntity  : modelEntity.getSlaverModels()){
				deleteDatModel(dataModelEntity);
			}
		}
		deleteDatModel(modelEntity);
	}

	private void deleteDatModel(DataModelEntity modelEntity){
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		deleteColumnModel(columnModelEntities);
		String tableName = modelEntity.getPrefix() == null ? modelEntity.getTableName() : modelEntity.getPrefix()+modelEntity.getTableName();
		delete(modelEntity);
		try {
			String deleteTableSql =" DROP TABLE IF exists "+tableName;
			jdbcTemplate.execute(deleteTableSql);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	private void veryDataModelColumnModel(DataModelEntity modelEntity){
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		for(ColumnModelEntity columnModelEntity : columnModelEntities){
			verifyColumReferenceItem(modelEntity.getTableName(), columnModelEntity);
		}
		if(modelEntity.getSlaverModels() != null){
			for(DataModelEntity dataModelEntity  : modelEntity.getSlaverModels()){
				for(ColumnModelEntity columnModelEntity : dataModelEntity.getColumns()){
					verifyColumReferenceItem("子表"+dataModelEntity.getTableName(), columnModelEntity);
				}
			}
		}
	}

	private void deleteColumnModel(List<ColumnModelEntity> columnModelEntities){
		for(int i = 0; i < columnModelEntities.size(); i++){
			ColumnModelEntity columnModelEntity = columnModelEntities.get(i);
			List<ColumnReferenceEntity> list = columnModelEntity.getColumnReferences();
			if(list != null && list.size() > 0){
				deleteColumnReferenceEntity(columnModelEntity);
			}
			DataModelEntity dataModelEntity = columnModelEntity.getDataModel();
			String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
			ColumnModelEntity column = columnModelEntity;
			String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
			columnModelService.deleteTableColumn(tableName, columnName);
		}
	}

	@Override
	public void checkDataModelIsReference(List<DataModelEntity> list) {
		for (DataModelEntity modelEntity:list) {
			List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
			for (ColumnModelEntity columnModelEntity : columnModelEntities) {
				List<ItemModelEntity> itemModelEntity = itemManager.findByProperty("columnModel.id", columnModelEntity.getId());
				if (itemModelEntity != null && itemModelEntity.size() > 0) {
					for (ItemModelEntity itemModel : itemModelEntity) {
						throw new IFormException(CommonUtils.exceptionCode, modelEntity.getTableName()+"数据表的"+columnModelEntity.getColumnName() + "字段被  " + itemModel.getFormModel().getName() + "  的" + itemModel.getName() + "控件关联");
					}
				}
			}
		}
	}

	@Override
	public void updateDataModelIndex(DataModelEntity modelEntity) {
		String tableName = modelEntity.getPrefix() == null ? modelEntity.getTableName(): modelEntity.getPrefix()+modelEntity.getTableName();
		List<String> list = listDataIndexName(tableName);
		for(IndexModelEntity indexModelEntity : modelEntity.getIndexes()){
			if (list.contains(indexModelEntity.getName())) {
				columnModelService.deleteTableIndex(tableName, indexModelEntity.getName());
			}
			columnModelService.createTableIndex(tableName, indexModelEntity);
		}
	}

	@Override
	public List<String> listDataIndexName(String tableName){
		String indexSql = "SELECT * FROM pg_indexes WHERE tablename ='"+tableName+"'";
		List<Map<String, Object>> indexList = listIndexBySql(indexSql);
		Set<String> list = new HashSet<>();
		for(Map<String, Object> map : indexList){
			list.add((String)map.get("indexname"));
		}
		return new ArrayList<>(list);
	}

	@Override
	public void deleteDataModelWithoutVerify(DataModelEntity modelEntity) {
		if(modelEntity.getSlaverModels() != null){
			for(DataModelEntity dataModelEntity  : modelEntity.getSlaverModels()){
				updateColumnItemModel(dataModelEntity);
				deleteDatModel(dataModelEntity);
			}
		}
		updateColumnItemModel(modelEntity);
		modelEntity.setMasterModel(null);
		deleteDatModel(modelEntity);
	}

	@Override
	public List<DataModel> queryAllList() {
		Map<String, DataModel> idAndDataModelMap = new HashMap();
		Map<String, String> masterModelIdMap = new HashMap();
		List<DataModel> list = jdbcTemplate.query("SELECT id, name, description, model_type, table_name, master_model, synchronized_, application_id FROM ifm_data_model",
			(rs, rowNum) -> {
				DataModel dataModel = new DataModel();
				dataModel.setId(rs.getString("id"));
				dataModel.setName(rs.getString("name"));
				dataModel.setTableName(rs.getString("table_name"));
				dataModel.setDescription(rs.getString("description"));
				dataModel.setSynchronized(rs.getBoolean("synchronized_"));
				dataModel.setApplicationId(rs.getString("application_id"));
				String modelType = rs.getString("model_type");
				if (org.springframework.util.StringUtils.hasText(modelType)) {
					dataModel.setModelType(DataModelType.valueOf(modelType));
				}
				idAndDataModelMap.put(dataModel.getId(), dataModel);
				String masterModelId = rs.getString("master_model");
				if (org.springframework.util.StringUtils.hasText(masterModelId)) {
					masterModelIdMap.put(dataModel.getId(), masterModelId);
				}
				return dataModel;
			}
		);
		for (DataModel item:list) {
			String masterModelId = masterModelIdMap.get(item.getId());
			if (org.springframework.util.StringUtils.hasText(masterModelId)) {
				DataModel masterModel = idAndDataModelMap.get(masterModelId);
				if (masterModel!=null){
					DataModelInfo masterModelInfo = new DataModelInfo();
					masterModelInfo.setId(masterModel.getId());
					masterModelInfo.setName(masterModel.getName());
					masterModelInfo.setTableName(masterModel.getTableName());
					masterModelInfo.setApplicationId(masterModel.getApplicationId());
					item.setMasterModel(masterModelInfo);
				}
			}
		}
		return list;
	}

	private void updateColumnItemModel(DataModelEntity modelEntity){
		for(ColumnModelEntity columnModelEntity : modelEntity.getColumns() ) {
			List<ItemModelEntity> itemModelEntityList = itemManager.query().filterIn("columnModel.id", columnModelEntity.getId()).list();
			for (ItemModelEntity itemModelEntity : itemModelEntityList) {
				itemModelEntity.setColumnModel(null);
				itemManager.save(itemModelEntity);
			}
		}
	}

	private List<Map<String, Object>> listIndexBySql(String sql) {
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = jdbcTemplate.query(sql, new DataModelIndexRowMapper());
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return list;
	}

	class DataModelIndexRowMapper implements RowMapper {

		@Override
		//实现mapRow方法
		public Map<String, Object> mapRow(ResultSet rs, int num) throws SQLException {
			//对类进行封装
			Map<String, Object> map = new HashMap<>();
			map.put("indexname",rs.getString("indexname"));
			return map;
		}
	}


	@Transactional(readOnly = false)
	protected DataModelEntity save(DataModelEntity entity, List<ColumnModelEntity> deletedCloumns, List<IndexModelEntity> deletedIndexes) {
		String tableName = entity.getPrefix() == null ? entity.getTableName(): entity.getPrefix()+entity.getTableName();

		//数据库表所有索引
		List<String> indexList = listDataIndexName(tableName);

		//删除索引
		if (!deletedIndexes.isEmpty()) {
			for(int j = 0;j < deletedIndexes.size() ; j++) {
				IndexModelEntity indexModelEntity = deletedIndexes.get(j);
				if(indexModelEntity.getColumns() != null && indexModelEntity.getColumns().size() > 0){
					indexModelEntity.setColumns(null);
				}
				indexModelEntity.setDataModel(null);
				if(indexList.contains(indexModelEntity.getName())) {
					String odltableName = entity.getPrefix() == null ? entity.getTableName(): entity.getPrefix()+entity.getTableName();
					columnModelService.deleteTableIndex(odltableName, indexModelEntity.getName());
				}
				indexManager.save(indexModelEntity);
				indexManager.delete(indexModelEntity);
			}
		}

		if (!deletedCloumns.isEmpty()) {
			for(int j = 0;j < deletedCloumns.size() ; j++){
				ColumnModelEntity columnModelEntity = deletedCloumns.get(j);
				List<ItemModelEntity> itemModelEntity = itemManager.findByProperty("columnModel.id", columnModelEntity.getId());
				if(itemModelEntity != null){
					for(ItemModelEntity itemModel : itemModelEntity){
						itemModel.setColumnModel(null);
						itemManager.save(itemModel);
					}
				}

				List<ColumnReferenceEntity> columnReferences = columnModelEntity.getColumnReferences();
				List<String> ids = new ArrayList<>();
				for(ColumnReferenceEntity columnReferenceEntity : columnReferences){
					ids.add(columnReferenceEntity.getToColumn().getId());
				}
				columnModelService.deleteOldColumnReferenceEntity(columnModelEntity, ids, columnReferences);
				columnModelService.updateColumnModelEntityIndex(columnModelEntity);
				columnModelService.delete(columnModelEntity);
				deletedCloumns.remove(columnModelEntity);
				j--;
			}
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
