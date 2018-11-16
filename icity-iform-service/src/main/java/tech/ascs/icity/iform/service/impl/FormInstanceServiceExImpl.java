package tech.ascs.icity.iform.service.impl;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private TaskService taskService;

	private JPAManager<FormModelEntity> formModelEntityJPAManager;

	private JPAManager<DictionaryEntity> dictionaryEntityJPAManager;

	private JPAManager<ItemModelEntity> itemModelManager;

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
		}

		for(DataModelEntity dataModelEntity : dataModel.getChildrenModels()){

		}*/


		return wrapEntity(formModel, map);
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
		for (ItemInstance itemInstance : formInstance.getItems()) {
			ItemModelEntity itemModel = getItemModel(formModel, itemInstance.getId());
			if (itemModel.getColumnModel().getColumnName().equalsIgnoreCase("id")) {
				if (!instanceId.equals(itemInstance.getValue())) {
					throw new IFormException("表单实例ID不一致");
				}
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
			ItemModelEntity modelEntity = itemModelManager.findUniqueByProperty("columnModel.id", columnModelEntity.getId());
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
			FormInstanceList.add(wrapEntity(formModel, entity));
		}
		return FormInstanceList;
	}

	protected FormInstance wrapEntity(FormModelEntity formModel, Map<String, Object> entity) {
		FormInstance formInstance = new FormInstance();
		formInstance.setFormId(formModel.getId());
		formInstance.setId((String) entity.get("id"));

		if (formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getId())) {
			formInstance.setProcessId((String) entity.get("PROCESS_ID"));
			formInstance.setProcessInstanceId((String) entity.get("PROCESS_INSTANCE"));
			formInstance.setActivityId((String) entity.get("ACTIVITY_ID"));
			formInstance.setActivityInstanceId((String) entity.get("ACTIVITY_INSTANCE"));
		}

		List<ItemInstance> items = new ArrayList<ItemInstance>();
		for (ItemModelEntity itemModel : formModel.getItems()) {
			ColumnModelEntity column = itemModel.getColumnModel();
			Object value = entity.get(column.getColumnName());
			ItemInstance itemInstance = new ItemInstance();
			itemInstance.setId(itemModel.getId());
			updateValue(itemModel, itemInstance, value);
			if (column.getKey()) {
				itemInstance.setVisible(false);
				itemInstance.setReadonly(true);
			} else {
				updateActivityInfo(itemModel, itemInstance, formInstance.getActivityId());
			}
			items.add(itemInstance);
			formInstance.addData(column.getColumnName(), itemInstance.getValue());
		}
		formInstance.setItems(items);
		
		return formInstance;
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
