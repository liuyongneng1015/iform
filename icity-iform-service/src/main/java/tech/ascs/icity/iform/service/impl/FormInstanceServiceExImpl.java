package tech.ascs.icity.iform.service.impl;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tech.ascs.icity.iflow.api.model.ProcessInstance;
import tech.ascs.icity.iflow.client.ProcessInstanceService;
import tech.ascs.icity.iflow.client.TaskService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormInstanceServiceEx;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;

@Service
public class FormInstanceServiceExImpl extends DefaultJPAService<FormModelEntity> implements FormInstanceServiceEx {
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private IFormSessionFactoryBuilder sessionFactoryBuilder;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Autowired
	private FormModelService formModelService;

	@Autowired
	private TaskService taskService;

	private JPAManager<FormModelEntity> formModelEntityJPAManager;

	private JPAManager<DictionaryEntity> dictionaryEntityJPAManager;

	private JPAManager<ItemModelEntity> itemModelManager;

	private JPAManager<DataModelEntity> dataModelManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public FormInstanceServiceExImpl() {
		super(FormModelEntity.class);
	}
	@Override
	protected void initManager() {
		super.initManager();
		formModelEntityJPAManager = getJPAManagerFactory().getJPAManager(FormModelEntity.class);
		dictionaryEntityJPAManager = getJPAManagerFactory().getJPAManager(DictionaryEntity.class);
		itemModelManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
		dataModelManager = getJPAManagerFactory().getJPAManager(DataModelEntity.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormInstance> listFormInstance(ListModelEntity listModel, Map<String, String> queryParameters) {
		Criteria criteria = generateCriteria(listModel, queryParameters);
		addSort(listModel, criteria);

		return wrapList(listModel, criteria.list());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<FormInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, String> queryParameters) {
		Criteria criteria = generateCriteria(listModel, queryParameters);
		addSort(listModel, criteria);

		criteria.setFirstResult((page - 1) * pagesize);
		criteria.setMaxResults(pagesize);
		List<FormInstance> list = wrapList(listModel, criteria.list());

		criteria.setFirstResult(0);
		criteria.setProjection(Projections.rowCount());
		Number count = (Number) criteria.uniqueResult();		

		Page<FormInstance> result = Page.get(page, pagesize);
		return result.data(count.intValue(), list);
	}

	@Override
	public Page<String> pageByTableName(String tableName, int page, int pagesize) {
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM if_").append(tableName);
		int count = jdbcTemplate.queryForObject(countSql.toString(), Integer.class);

		StringBuilder sql = new StringBuilder("SELECT * FROM if_").append(tableName);
		String pageSql = buildPageSql(sql.toString(), page, pagesize);
		List<String> list = jdbcTemplate.queryForList(pageSql,String.class);

		Page<String> result = Page.get(page, pagesize);
		return result.data(count, list);
	}

	private String buildPageSql(String sql, int page, int pagesize) {
		String database = "";
		try {
			database = jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName().toLowerCase();
		} catch (SQLException e) {
			throw new IFormException("获取数据库类型失败", e);
		}
		if ("MySQL".equalsIgnoreCase(database)) {
			return sql + " LIMIT " + (page - 1) * pagesize + "," + pagesize;
		} else if ("PostgreSQL".equalsIgnoreCase(database)) {
			return sql + " LIMIT " + pagesize + " OFFSET " + (page - 1) * pagesize;
		} else if ("Oracle".equalsIgnoreCase(database)) {
			return new StringBuffer("SELECT * FROM (SELECT t1.*,rownum sn1 FROM (").append(sql).append(") t1) t2 WHERE t2.sn1 BETWEEN ")
					.append((page - 1) * pagesize + 1).append(" AND ").append(page * pagesize).toString();
		} else {
			return sql;
		}
	}
	@Override
	public FormInstance newFormInstance(FormModelEntity formModel) {
		FormInstance formInstance = new FormInstance();
		formInstance.setFormId(formModel.getId());
		if (formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getId())) {
			formInstance.setProcessId(formModel.getProcess().getId());
			formInstance.setActivityId(formModel.getProcess().getStartActivity());
		}

		List<ItemInstance> items = new ArrayList<ItemInstance>();
		for (ItemModelEntity itemModel : formModel.getItems()) {
			ColumnModelEntity column = itemModel.getColumnModel();
			if(column == null){
				continue;
			}
			ItemInstance itemInstance = new ItemInstance();
			itemInstance.setId(itemModel.getId());
			if (column.getKey()) {
				itemInstance.setVisible(false);
				itemInstance.setReadonly(true);
			} else {
				for (ItemActivityInfo activityInfo : itemModel.getActivities()) {
					if (activityInfo.getActivityId().equals(formModel.getProcess().getStartActivity())) {
						itemInstance.setVisible(activityInfo.isVisible());
						itemInstance.setReadonly(activityInfo.isReadonly());
						break;
					}
				}
			}
			items.add(itemInstance);
			formInstance.addData(column.getColumnName(), itemInstance.getValue());
		}
		formInstance.setItems(items);

		return formInstance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FormInstance getFormInstance(FormModelEntity formModel, String instanceId) {
		List<ItemModelEntity> columnItem = formModelService.getAllColumnItems(formModel.getItems());
		/*Map<String, List<ItemModelEntity>> referenceMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity : columnItem){
			if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getSelectMode() == null) {
				List<ItemModelEntity> list = referenceMap.get(itemModelEntity.getColumnModel().getDataModel()+"_f"+itemModelEntity.getColumnModel().getColumnName());
				if(list == null){
					list = new ArrayList<>();
				}
				list.add(itemModelEntity);
				referenceMap.put(itemModelEntity.getColumnModel().getDataModel()+"_f"+itemModelEntity.getColumnModel().getColumnName(), list);
			}
		}*/
		DataModelEntity dataModel = formModel.getDataModels().get(0);
		Session session = getSession(dataModel);
		Map<String, Object> map = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
		/*for(String key : map.keySet()){
			map.put(dataModel.getTableName() + "_" + key, map.get(key));
		}
		for(DataModelEntity dataModelEntity : dataModel.getSlaverModels()){
			 session = getSession(dataModel);
			Map<String, Object> slaverMap = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
			for(String key : map.keySet()){
				slaverMap.put(dataModel.getTableName() + "_" + key, map.get(key));
			}
			map.putAll(slaverMap);
		}*/


		return wrapEntity(formModel, map, instanceId, true);
	}

	private Map<String, Object> createDataModel(DataModelEntity dataModel, String instanceId){
		Session session = getSession(dataModel);
		Map<String, Object> map = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
		for(String key : map.keySet()){
			map.put(dataModel.getTableName() + "_" + key, map.get(key));
		}
		return map;
	}

	@Override
	public String createFormInstance(FormModelEntity formModel, FormInstance formInstance) {
		DataModelEntity dataModel = formModel.getDataModels().get(0);

		Map<String, Object> data = new HashMap<String, Object>();
		for (ItemInstance itemInstance : formInstance.getItems()) {
			ItemModelEntity itemModel = getItemModel(formModel, itemInstance.getId());
			if (itemModel.getColumnModel().getColumnName().equalsIgnoreCase("id")) {
				continue;
			}
			Object value;
			if (itemModel.getType() == ItemType.DatePicker) {
				value = new Date((Long) itemInstance.getValue());
			} else {
				value = itemInstance.getValue();
			}
			data.put(itemModel.getColumnModel().getColumnName(), value);
		}
		
		Session session = getSession(dataModel);
		session.beginTransaction();

		String newId = (String) session.save(dataModel.getTableName(), data);

		// 启动流程
		if (formModel.getProcess() != null && formModel.getProcess().getKey() != null) {
			String processInstanceId = processInstanceService.startProcess(formModel.getProcess().getKey(), newId, data);
			updateProcessInfo(formModel, data, processInstanceId);
			session.update(dataModel.getTableName(), data);
		}

		session.getTransaction().commit();
		session.close();
		
		return newId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateFormInstance(FormModelEntity formModel, String instanceId, FormInstance formInstance) {
		DataModelEntity dataModel = formModel.getDataModels().get(0);
		Session session = getSession(dataModel);

		Map<String, Object> data = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);

		//主表数据
		setMasterFormItemInstances(formInstance.getItems(),data);
		//TODO 子表数据
		List<SubFormItemInstance> subFormItemInstanceList = formInstance.getSubFormData();
		for(SubFormItemInstance subFormItemInstance : subFormItemInstanceList){
			String key = subFormItemInstance.getTableName()+"_list";
			DataModelEntity dataModelEntity = dataModelManager.findUniqueByProperty("tableName", subFormItemInstance.getTableName());

			//新的数据
			Set<Map<String, Object>> newListMap = new HashSet<>();
			for(List<SubFormRowItemInstance> subFormRowItemInstanceList : subFormItemInstance.getItemInstances()){
				Map<String, Object> map = new HashMap<>();
				for(SubFormRowItemInstance instance : subFormRowItemInstanceList){
					for(ItemInstance itemModelService : instance.getItemInstances()){
						setItemInstance(itemModelService, map);
					}
				}
				newListMap.add(map);
			}
			List<String> idList = new ArrayList<>();
			for(Map<String, Object> newMap : newListMap){
				String id = String.valueOf(newMap.get("id"));
				if(id != null) {
					idList.add(id);
				}
			}

			//旧的数据
			Set<Map<String, Object>> oldListMap = (Set<Map<String, Object>>)data.get(key);
			Set<Map<String, Object>> saveListMap = getNewMapData(data, dataModelEntity,  oldListMap,  idList,  newListMap);

			data.put(key, saveListMap);
		}

		//TODO 关联表数据
		List<DataModelInstance> referenceDataList = formInstance.getReferenceData();
		for(DataModelInstance dataModelInstance : referenceDataList) {
			String key = dataModelInstance.getReferenceTable() + "_list";
			boolean flag = false;
			if (dataModelInstance.getReferenceType() == ReferenceType.ManyToOne || dataModelInstance.getReferenceType() == ReferenceType.OneToOne) {
				key = dataModelInstance.getReferenceValueColumn();
				flag = true;
			}
			Set<Map<String, Object>> oldListMap = new HashSet<Map<String, Object>>();
			//旧的数据
			Object oldData = data.get(key);
			if (flag) {
				oldListMap.add((Map<String, Object>) oldData);
			} else {
				oldListMap = (Set<Map<String, Object>>) oldData;
			}
			DataModelEntity dataModelEntity = dataModelManager.findUniqueByProperty("tableName", dataModelInstance.getReferenceTable());


			Set<Map<String, Object>> newListMap = new HashSet<>();
			for (List<ItemInstance> instances : dataModelInstance.getItems()) {
				Map<String, Object> map = new HashMap<>();
				for (ItemInstance itemModelService : instances) {
					setItemInstance(itemModelService, map);
				}
				newListMap.add(map);
			}
			List<String> idList = new ArrayList<>();
			for (Map<String, Object> newMap : newListMap) {
				String id = String.valueOf(newMap.get("id"));
				if (id != null) {
					idList.add(id);
				}
			}
			//新的数据
			Set<Map<String, Object>> saveListMap =getNewMapData( data, dataModelEntity,  oldListMap,  idList,  newListMap);
			if (flag && saveListMap.size() > 0) {
				data.put(key, new ArrayList<>(saveListMap).get(0));
			}else{
				data.put(key, saveListMap);
			}
		}


		// 流程操作
		if (formInstance.getActivityInstanceId() != null) {
			taskService.completeTask(formInstance.getActivityInstanceId(), data);
			updateProcessInfo(formModel, data, formInstance.getProcessInstanceId());
		}

		session.beginTransaction();
		session.update(dataModel.getTableName(), data);
		session.getTransaction().commit();
		session.close();
	}

	private Set<Map<String, Object>> getNewMapData(Map<String, Object> data, DataModelEntity dataModelEntity, Set<Map<String, Object>> oldListMap, List<String> idList, Set<Map<String, Object>> newListMap){
		Set<Map<String, Object>> saveListMap = new HashSet<>();
		//旧的数据
		for (Map<String, Object> map : oldListMap) {
			if (idList.contains(String.valueOf(map.get("id")))) {
				continue;
			}
			Session subFormSession = getSession(dataModelEntity);
			Map<String, Object> subFormData = (Map<String, Object>) subFormSession.load(dataModelEntity.getTableName(), String.valueOf(map.get("id")));
			//子表数据
			subFormData.remove("master_id");
			subFormData.put("master_id", null);
			subFormSession.beginTransaction();
			subFormSession.update(dataModelEntity.getTableName(), subFormData);
			subFormSession.getTransaction().commit();
			subFormSession.close();
		}

		for(Map<String, Object> newMap : newListMap) {
			String id = (String) newMap.get("id");
			Map<String, Object> subFormData = new HashMap<>();
			if (id != null) {
				Session subFormSession = getSession(dataModelEntity);
				subFormData = (Map<String, Object>) subFormSession.load(dataModelEntity.getTableName(), id);
				subFormSession.close();
			} else {
				Session subFormSession = getSession(dataModelEntity);
				Map<String, Object> subFormRowData = new HashMap<>();
				for (String keyString : newMap.keySet()) {
					if (!"id".equals(keyString)) {
						subFormRowData.put(keyString, newMap.get(keyString));
					}
				}
				subFormSession.beginTransaction();
				subFormData = (Map<String, Object>) subFormSession.merge(dataModelEntity.getTableName(), subFormRowData);
				subFormSession.getTransaction().commit();
				subFormSession.close();
			}
			//子表数据
			subFormData.put("master_id", data);
			saveListMap.add(subFormData);
		}
		return saveListMap;
	}

	private void setMasterFormItemInstances(List<ItemInstance> itemInstances, Map<String, Object> data){
		for (ItemInstance itemInstance : itemInstances) {
			setItemInstance(itemInstance, data);
		}
	}

	private void setItemInstance(ItemInstance itemInstance, Map<String, Object> data){
		ItemModelEntity itemModel = itemModelManager.get(itemInstance.getId());
		Object value;
		if (itemModel.getType() == ItemType.DatePicker) {
			value = new Date((Long) itemInstance.getValue());
		} else {
			value = itemInstance.getValue();
		}
		data.put(itemModel.getColumnModel().getColumnName(), value);
	}



	@Deprecated
	private void ss(FormModelEntity formModel, List<ItemInstance> itemInstances){
		for (ItemInstance itemInstance : itemInstances) {
			ItemModelEntity itemModel = getItemModel(formModel, itemInstance.getId());
			/*if (itemModel.getColumnModel().getColumnName().equalsIgnoreCase("id")) {
				if (!instanceId.equals(itemInstance.getValue())) {
					throw new IFormException("表单实例ID不一致");
				}
				continue;
			}*/
			Object value;
			if (itemModel.getType() == ItemType.DatePicker) {
				value = new Date((Long) itemInstance.getValue());
			} else {
				value = itemInstance.getValue();
			}
			//data.put(itemModel.getColumnModel().getColumnName(), value);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteFormInstance(FormModelEntity formModel, String instanceId) {
		DataModelEntity dataModel = formModel.getDataModels().get(0);
		Session session = getSession(dataModel);
		Map<String, Object> entity = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
		session.delete(dataModel.getTableName(), entity);
	}

	@Override
	public List<TableDataModel> findTableDataFormInstance(String formId, String id) {
		FormModelEntity formModelEntity = formModelEntityJPAManager.find(formId);
		List<DataModelEntity> dataModels = formModelEntity.getDataModels();
		//主表
		DataModelEntity masterDataModelEntity = null;
		//从表
		List<DataModelEntity> slaverDataModels = new ArrayList<DataModelEntity>();
		for(DataModelEntity dataModelEntity : dataModels){
			if(dataModelEntity.getModelType() != DataModelType.Slaver){
				masterDataModelEntity = dataModelEntity;
			}else{
				slaverDataModels.add(dataModelEntity);
			}
		}
		List<TableDataModel> tableDataModels = new ArrayList<>();
		//主表的数据
		StringBuffer stringBuffer = new StringBuffer("select * from if_");
		stringBuffer.append(masterDataModelEntity.getTableName());
		stringBuffer.append(" where id=" + id);
		Map<String, Object> masterMap = jdbcTemplate.queryForMap(stringBuffer.toString());
		tableDataModels.addAll(setColumData(masterMap, masterDataModelEntity.getColumns()));
		//子表数据
		for(DataModelEntity slaverDataModelEntity : slaverDataModels) {
			StringBuffer sb = new StringBuffer("select * from if_");
			sb.append(slaverDataModelEntity.getTableName());
			sb.append(" where parent_id=" + id);
			Map<String, Object> slaverDataMap = jdbcTemplate.queryForMap(sb.toString());
			List<ColumnModelEntity> columns = slaverDataModelEntity.getColumns();
			tableDataModels.addAll(setColumData(slaverDataMap, columns));
		}
		//TODO 如果值为下拉选择我还没处理

		//TODO  还没做关联表关系
		List<ColumnModelEntity> masterColumnModelEntities = masterDataModelEntity.getColumns();
		Map<String, List<ItemModelEntity>> tableItemMap = new HashMap<>();
		List<ItemModelEntity> referenceItmes = new ArrayList<>();
		List<ItemModelEntity> attribute = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : formModelEntity.getItems()){
			if(itemModelEntity.getType() == ItemType.ReferenceList){
				referenceItmes.add(itemModelEntity);
			}
			if(itemModelEntity.getType() == ItemType.ReferenceLabel){
				attribute.add(itemModelEntity);
			}
		}

		for(ItemModelEntity itemModelEntity : referenceItmes){
			if(((ReferenceItemModelEntity)itemModelEntity).getSelectMode() == SelectMode.Inverse){
				StringBuffer sb = new StringBuffer("select * from if_");
				sb.append(((ReferenceItemModelEntity) itemModelEntity).getReferenceTable());
				sb.append(" where "+((ReferenceItemModelEntity) itemModelEntity).getReferenceValueColumn()+"=" + id);
				List<Map<String, Object>> referenceDataMap = jdbcTemplate.queryForList(sb.toString());
				List<String> list = new ArrayList<String>();

				for(Map<String, Object> map : referenceDataMap){
					if(itemModelEntity.getType() == ItemType.DatePicker){
						list.add(DateFormatUtils.format((Date) map.get(itemModelEntity.getColumnModel().getColumnName()),((TimeItemModelEntity)itemModelEntity).getTimeFormat()));
					}else{
						list.add(String.valueOf(map.get(itemModelEntity.getColumnModel().getColumnName())));
					}
				}
				tableDataModels.add(setTableDataModel(itemModelEntity.getColumnModel(), org.apache.commons.lang3.StringUtils.join(list,",")));
			}//TODO MANYTOMANY
		}


		return tableDataModels;
	}

	private List<TableDataModel> setColumData(Map<String, Object> dataMap, List<ColumnModelEntity> columns){
		List<TableDataModel> list = new ArrayList<>();
		for(ColumnModelEntity columnModelEntity : columns){
			List<ItemModelEntity> itemModelEntity = itemModelManager.findByProperty("columnModel.id", columnModelEntity.getId());
			if(itemModelEntity == null || itemModelEntity.size() == 0){
				continue;
			}
			ItemModelEntity modelEntity = itemModelEntity.get(0);
			String value = null;
			if(modelEntity.getType() == ItemType.DatePicker){
				value = DateFormatUtils.format((Date) dataMap.get(columnModelEntity.getColumnName()),((TimeItemModelEntity)modelEntity).getTimeFormat());
			}else{
				value = String.valueOf(dataMap.get(columnModelEntity.getColumnName()));
			}
			list.add(setTableDataModel(columnModelEntity, value));
		}
		return list;
	}
	private TableDataModel setTableDataModel(ColumnModelEntity columnModelEntity, String value){
		TableDataModel tableDataModel = new TableDataModel();
		tableDataModel.setColumnId(columnModelEntity.getId());
		tableDataModel.setValue(value);
		return tableDataModel;
	}

	protected void updateProcessInfo(FormModelEntity formModel, Map<String, Object> entity, String processInstanceId) {
		ProcessInstance processInstance = processInstanceService.get(processInstanceId);
		entity.put("PROCESS_ID", formModel.getProcess().getId());
		entity.put("PROCESS_INSTANCE", processInstanceId);
		entity.put("ACTIVITY_ID", processInstance.getCurrentActivityId());
		entity.put("ACTIVITY_INSTANCE", processInstance.getCurrentActivityInstanceId());
	}

	protected Session getSession(DataModelEntity dataModel) {
		SessionFactory sessionFactory = sessionFactoryBuilder.getSessionFactory(dataModel);
		return sessionFactory.openSession();
	}

	@SuppressWarnings("deprecation")
	protected Criteria generateCriteria(ListModelEntity listModel, Map<String, String> queryParameters) {
		DataModelEntity dataModel = listModel.getMasterForm().getDataModels().get(0);
		Session session = getSession(dataModel);
		Criteria criteria = session.createCriteria(dataModel.getTableName());
		for (ListSearchItem searchItem : listModel.getSearchItems()) {
			String value = queryParameters.get(searchItem.getItemModel().getId());
			if (StringUtils.hasText(value)) {
				String propertyName = searchItem.getItemModel().getColumnModel().getColumnName();
				if (searchItem.getSearch().getSearchType() == SearchType.Like) {
					criteria.add(Restrictions.like(propertyName, "%" + value + "%"));
				} else {
					criteria.add(Restrictions.eq(propertyName, value));
				}
			}
		}

		return criteria;
	}

	protected void addSort(ListModelEntity listModel, Criteria criteria) {
		for (ListSortItem sortItem : listModel.getSortItems()) {
			String propertyName = sortItem.getItemModel().getColumnModel().getColumnName();
			if (sortItem.isAsc()) {
				criteria.addOrder(Order.asc(propertyName));
			} else {
				criteria.addOrder(Order.desc(propertyName));
			}
		}
	}

	protected List<FormInstance> wrapList(ListModelEntity listModel, List<Map<String, Object>> entities) {
		List<FormInstance> FormInstanceList = new ArrayList<FormInstance>();
		FormModelEntity formModel = listModel.getMasterForm();
		for (Map<String, Object> entity : entities) {
			FormInstanceList.add(wrapEntity(formModel, entity,String.valueOf(entity.get("id")),false));
		}
		return FormInstanceList;
	}

	protected FormInstance wrapEntity(FormModelEntity formModel, Map<String, Object> entity, String instanceId, boolean referenceFlag) {
		FormInstance formInstance = new FormInstance();
		formInstance.setFormId(formModel.getId());
		//数据id
		formInstance.setId(instanceId);
		if (formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getId())) {
			formInstance.setProcessId((String) entity.get("PROCESS_ID"));
			formInstance.setProcessInstanceId((String) entity.get("PROCESS_INSTANCE"));
			formInstance.setActivityId((String) entity.get("ACTIVITY_ID"));
			formInstance.setActivityInstanceId((String) entity.get("ACTIVITY_INSTANCE"));
		}
		return setFormInstanceModel(formInstance, formModel, entity, referenceFlag);
	}

	private FormInstance setFormInstanceModel(FormInstance formInstance, FormModelEntity fromFormModel, Map<String, Object> entity, boolean referenceFlag){
		List<ItemInstance> items = new ArrayList<>();
		List<ItemModelEntity> list = fromFormModel.getItems();
		List<DataModelInstance> referenceDataModelList = formInstance.getReferenceData();
		List<SubFormItemInstance> subFormItems = formInstance.getSubFormData();
		for (ItemModelEntity itemModel : list) {
			System.out.println(itemModel.getId()+"____begin");
			ColumnModelEntity column = itemModel.getColumnModel();
			if(column == null && !(itemModel instanceof  ReferenceItemModelEntity) && !(itemModel instanceof SubFormItemModelEntity)){
				continue;
			}
			Object value = new Object();
			if(column != null) {
				value = entity.get(column.getColumnName());
			}
			if(itemModel instanceof ReferenceItemModelEntity){
				if(!referenceFlag){
					continue;
				}
				//主表字段
				ReferenceItemModelEntity fromItem = (ReferenceItemModelEntity)itemModel;

				//关联表数据模型
				FormModelEntity toModelEntity = formModelEntityJPAManager.query().filterEqual("name",fromItem.getReferenceTable()).unique();
				if (toModelEntity == null) {
					continue;
				}

				//关联的item
				ItemModelEntity toItemModel = itemModelManager.query().filterEqual("name", fromItem.getReferenceValueColumn()).filterEqual("formModel.id", toModelEntity.getId()).unique();

				ColumnModelEntity columnModelEntity = fromItem.getColumnModel();
				if(fromItem.getReferenceType() != ReferenceType.ManyToMany && columnModelEntity == null){
					continue;
				}

				if(fromItem.getReferenceType() == ReferenceType.ManyToOne || fromItem.getReferenceType() == ReferenceType.OneToOne){
					String key = fromItem.getReferenceValueColumn();
					Map<String, Object> listMap = (Map<String, Object>)entity.get(key);
					if( listMap == null || listMap.size() == 0) {
						continue;
					}
					FormInstance f = getBaseItemInstance(toModelEntity, listMap);
					DataModelInstance dataModelInstance = new DataModelInstance();
					dataModelInstance.setId(toModelEntity.getDataModels().get(0).getId());
					dataModelInstance.setName(toModelEntity.getDataModels().get(0).getName());
					dataModelInstance.setReferenceTable(toModelEntity.getDataModels().get(0).getTableName());
					dataModelInstance.setReferenceType(fromItem.getReferenceType());
					dataModelInstance.setReferenceValueColumn(columnModelEntity.getColumnName());
					dataModelInstance.setSize(1);
					dataModelInstance.getItems().add(f.getItems());
					referenceDataModelList.add(dataModelInstance);
				}else if(fromItem.getReferenceType() == ReferenceType.ManyToMany || fromItem.getReferenceType() == ReferenceType.OneToMany){
					String key = toModelEntity.getDataModels().get(0).getTableName()+"_list";
					if(fromItem.getReferenceType() == ReferenceType.OneToMany){
						key = toItemModel.getColumnModel().getColumnName();
					}
					Set<Map<String, Object>> listMap = (Set<Map<String, Object>>)entity.get(key);
					if( listMap == null || listMap.size() == 0) {
						continue;
					}
					DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, entity, listMap);
					dataModelInstance.setReferenceType(fromItem.getReferenceType());
					dataModelInstance.setReferenceValueColumn(columnModelEntity.getColumnName());
					referenceDataModelList.add(dataModelInstance);
				}
			}else if(itemModel instanceof SubFormItemModelEntity) {
				if(!referenceFlag){
					continue;
				}
				//TODO 子表数据结构
				SubFormItemModelEntity itemModelEntity = (SubFormItemModelEntity)itemModel;
				String key =((SubFormItemModelEntity) itemModel).getTableName()+"_list";
				Set<Map<String, Object>> listMap = (Set<Map<String, Object>>)entity.get(key);
				if( listMap == null || listMap.size() == 0) {
					continue;
				}
				SubFormItemInstance subFormItemInstance = new SubFormItemInstance();
				List<List<SubFormRowItemInstance>> subFormItemInstances = new ArrayList<List<SubFormRowItemInstance>>();
				subFormItemInstance.setId(itemModelEntity.getId());
				subFormItemInstance.setItemInstances(subFormItemInstances);
				subFormItemInstance.setTableName(itemModelEntity.getTableName());
				for(Map<String, Object> map : listMap) {
					List<SubFormRowItemInstance> subFormRowItemInstanceList = new ArrayList<>();
					for (SubFormRowItemModelEntity subFormRowItemModelEntity : itemModelEntity.getItems()) {
						SubFormRowItemInstance subFormRowItemInstance = new SubFormRowItemInstance();
						subFormRowItemInstance.setRowNumber(subFormRowItemModelEntity.getRowNumber());
						subFormRowItemInstance.setId(subFormRowItemModelEntity.getId());
						List<ItemInstance> instances = new ArrayList<>();
						subFormRowItemInstance.setItemInstances(instances);
						for (ItemModelEntity item : subFormRowItemModelEntity.getItems()) {
							ColumnModelEntity columnModelEntity  = item.getColumnModel();
							ItemInstance itemInstance = setItemInstance(columnModelEntity.getKey(), item, map.get(item.getColumnModel().getColumnName()), formInstance.getActivityId());
							instances.add(itemInstance);
						}
						//子表主键id
						ColumnModelEntity subFormColumnModelEntity  = itemModel.getColumnModel();
						ItemInstance subFomrItemInstance = setItemInstance(subFormColumnModelEntity.getKey(), itemModel, map.get("id"), formInstance.getActivityId());
						instances.add(subFomrItemInstance);

						subFormRowItemInstance.setItemInstances(instances);
						subFormRowItemInstanceList.add(subFormRowItemInstance);
					}
					subFormItemInstances.add(subFormRowItemInstanceList);
				}
				subFormItems.add(subFormItemInstance);
			}else{
				ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
				items.add(itemInstance);
				formInstance.addData(itemModel.getColumnModel().getId(), itemInstance.getValue());
			}
			System.out.println(itemModel.getId()+"____end");
		}
		formInstance.getItems().addAll(items);
		return formInstance;
	}

	private DataModelInstance setDataModelInstance(FormModelEntity toModelEntity, Map<String, Object> entity, Set<Map<String, Object>> listMap){

		List<Map<String, Object>> listData = new ArrayList<>(listMap);
		int pageIndex = 1;
		int pageSize = 10;
		List<Map<String, Object>> referenceListData = listData.subList((pageIndex-1) * pageSize, pageIndex * pageSize -1 < listData.size()  ? pageIndex * pageSize : listData.size());

		DataModelInstance dataModelInstance = new DataModelInstance();
		dataModelInstance.setId(toModelEntity.getDataModels().get(0).getId());
		dataModelInstance.setName(toModelEntity.getDataModels().get(0).getName());
		dataModelInstance.setReferenceTable(toModelEntity.getDataModels().get(0).getTableName());
		dataModelInstance.setSize(listMap.size());
		for(Map<String, Object> map : referenceListData) {
			FormInstance newFormInstance = getBaseItemInstance(toModelEntity, map);
			dataModelInstance.getItems().add(newFormInstance.getItems());
		}
		return dataModelInstance;
	}

	private FormInstance getBaseItemInstance(FormModelEntity formModelEntity, Map<String,Object> entity){
		FormInstance formInstance = new FormInstance();
		formInstance.setFormId(formModelEntity.getId());
		List<ItemInstance> items = new ArrayList<>();
		List<ItemModelEntity> list = formModelService.getAllColumnItems(formModelEntity.getItems());
		for (ItemModelEntity itemModel : list) {
			System.out.println(itemModel.getId()+"____begin");
			ColumnModelEntity column = itemModel.getColumnModel();
			if(column == null || itemModel instanceof ReferenceItemModelEntity){
				continue;
			}
			Object value = entity.get(column.getColumnName());
			ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
			items.add(itemInstance);
			formInstance.addData(itemModel.getColumnModel().getId(), itemInstance.getValue());
			System.out.println(itemModel.getId()+"____end");
		}
		formInstance.getItems().addAll(items);
		return formInstance;
	}


	private ItemInstance setItemInstance(Boolean visiblekey , ItemModelEntity itemModel, Object value, String activityId){
		ItemInstance itemInstance = new ItemInstance();
		itemInstance.setId(itemModel.getId());
		itemInstance.setColumnModelId(itemModel.getColumnModel().getId());
		updateValue(itemModel, itemInstance, value);
		if (visiblekey) {
			itemInstance.setVisible(false);
			itemInstance.setReadonly(true);
		} else {
			updateActivityInfo(itemModel, itemInstance, activityId);
		}
		return itemInstance;
	}

	protected void updateActivityInfo(ItemModelEntity itemModel, ItemInstance itemInstance, String activityId) {
		if (StringUtils.hasText(activityId)) {
			for (ItemActivityInfo activityInfo : itemModel.getActivities()) {
				if (activityInfo.getActivityId().equals(activityId)) {
					itemInstance.setVisible(activityInfo.isVisible());
					itemInstance.setReadonly(activityInfo.isReadonly());
					break;
				}
			}
		}
	}

	protected void updateValue(ItemModelEntity itemModel, ItemInstance itemInstance, Object value) {
		if (value == null) {
			return;
		}
		switch(itemModel.getType()) {
			case DatePicker:
				Date date = (Date) value;
				itemInstance.setValue(date);
				itemInstance.setDisplayValue(DateFormatUtils.format(date,((TimeItemModelEntity)itemModel).getTimeFormat()));
				break;
			case Select:
				itemInstance.setValue(String.valueOf(value));
				if(((SelectItemModelEntity)itemModel).getSelectReferenceType() == SelectReferenceType.Dictionary){
					DictionaryEntity dictionary = dictionaryEntityJPAManager.find(((SelectItemModelEntity) itemModel).getReferenceDictionaryId());
					if(dictionary != null) {
						List<DictionaryItemEntity> dictionaryItemEntities = dictionary.getDictionaryItems();
						for(DictionaryItemEntity dictionaryItemEntity : dictionaryItemEntities){
							if (dictionaryItemEntity.getCode().equals(String.valueOf(value))) {
								itemInstance.setDisplayValue(dictionaryItemEntity.getDescription());
								break;
							}
						}
					}
				}else if(((SelectItemModelEntity)itemModel).getSelectReferenceType() == SelectReferenceType.Fixed) {
					for (ItemSelectOption option : itemModel.getOptions()) {
						if (option.getValue().equals(String.valueOf(value))) {
							itemInstance.setDisplayValue(option.getLabel());
							break;
						}
					}
				}
				itemInstance.setValue(value);
				itemInstance.setDisplayValue(String.valueOf(value));
				break;
//			case InputNumber:
//				break;
			default:
				itemInstance.setValue(value);
				itemInstance.setDisplayValue(String.valueOf(value));
				break;
		}
	}

	protected ItemModelEntity getItemModel(FormModelEntity formModel, String itemModelId) {
		for (ItemModelEntity itemModel : formModel.getItems()) {
			if (itemModel.getId().equals(itemModelId)) {
				return itemModel;
			}
		}
		throw new IFormException(404, "表单控件模型不存在");
	}
}
