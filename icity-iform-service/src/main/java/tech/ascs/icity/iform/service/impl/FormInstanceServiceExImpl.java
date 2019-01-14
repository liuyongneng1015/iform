package tech.ascs.icity.iform.service.impl;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import net.minidev.json.JSONObject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
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
import tech.ascs.icity.iform.service.UploadService;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.rbac.feign.model.UserInfo;

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

	private JPAManager<DictionaryItemEntity> dictionaryItemManager;

	private JPAManager<ItemModelEntity> itemModelManager;

	private JPAManager<DataModelEntity> dataModelManager;

	private JPAManager<FileUploadEntity> fileUploadManager;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	UploadService uploadService;

	public FormInstanceServiceExImpl() {
		super(FormModelEntity.class);
	}
	@Override
	protected void initManager() {
		super.initManager();
		formModelEntityJPAManager = getJPAManagerFactory().getJPAManager(FormModelEntity.class);
		dictionaryEntityJPAManager = getJPAManagerFactory().getJPAManager(DictionaryEntity.class);
		dictionaryItemManager = getJPAManagerFactory().getJPAManager(DictionaryItemEntity.class);
		itemModelManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
		dataModelManager = getJPAManagerFactory().getJPAManager(DataModelEntity.class);
		fileUploadManager = getJPAManagerFactory().getJPAManager(FileUploadEntity.class);
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
		//List<ItemModelEntity> columnItem = formModelService.getAllColumnItems(formModel.getItems());
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

		FormInstance formInstance = null;
		Session session = null;
		try {
			DataModelEntity dataModel = formModel.getDataModels().get(0);
			session = getSession(dataModel);
			Map<String, Object> map = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
			if(map == null || map.keySet() == null){
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
			}
			formInstance = wrapEntity(formModel, map, instanceId, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException("没有查询到【" + formModel.getName() + "】表单，instanceId【"+instanceId+"】的数据");
		}finally {
			if(session != null){
				session.close();
			}
		}
		return formInstance;
	}

	private Map<String, Object> createDataModel(DataModelEntity dataModel, String instanceId){
		Session session = getSession(dataModel);
		Map<String, Object> map = null;

		map = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
		if(map == null || map.keySet() == null) {
			throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
		}

		for(String key : map.keySet()){
			map.put(dataModel.getTableName() + "_" + key, map.get(key));
		}
		return map;
	}

	@Override
	public String createFormInstance(FormModelEntity formModel, FormInstance formInstance) {
		FormModelEntity formModelEntity = formModelService.get(formInstance.getFormId());
		List<ItemInstance> list = formInstance.getItems();
		Map<String, ItemInstance> itemMap = new HashMap<>();
		for(ItemInstance itemInstance : list){
			itemMap.put(itemInstance.getId(), itemInstance);
		}
		UserInfo user = null;
		try {
			user = tech.ascs.icity.rbac.util.Application.getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(ItemModelEntity itemModelEntity : formModelEntity.getItems()){
			if(itemModelEntity.getSystemItemType() == SystemItemType.CreateDate && ((TimeItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Create){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(), new Date()));
			}else if(itemModelEntity.getSystemItemType() == SystemItemType.Creator && ((CreatorItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Create){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(), user != null ? user.getId() : "-1"));
			}else if(itemModelEntity.getSystemItemType() == SystemItemType.SerialNumber){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(),System.currentTimeMillis()+"_"+new Random().nextInt(10000)));
			}
		}


		DataModelEntity dataModel = formModel.getDataModels().get(0);
		Session session = getSession(dataModel);
		session.beginTransaction();
		Map<String, Object> data = new HashMap<String, Object>();
		//主表数据
		setMasterFormItemInstances(formInstance.getItems(), data);
		//设置关联数据
		setReferenceData(session, dataModel.getTableName(), formInstance, data);

		String newId = (String) session.save(dataModel.getTableName(), data);

		// 启动流程
		if (formModel.getProcess() != null && formModel.getProcess().getKey() != null) {
			String processInstanceId = processInstanceService.startProcess(formModel.getProcess().getKey(), newId, data);
			updateProcessInfo(formModel, data, processInstanceId);
		}
		session.getTransaction().commit();
		session.close();

		return newId;
	}

	private ItemInstance getItemInstance(String id, Object value){
		ItemInstance itemInstance = new ItemInstance();
		itemInstance.setId(id);
		itemInstance.setReadonly(true);
		itemInstance.setVisible(true);
		itemInstance.setValue(value);
		itemInstance.setDisplayValue(value);
		return itemInstance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateFormInstance(FormModelEntity formModel, String instanceId, FormInstance formInstance) {

		FormModelEntity formModelEntity = formModelService.get(formInstance.getFormId());
		List<ItemInstance> list = formInstance.getItems();
		Map<String, ItemInstance> itemMap = new HashMap<>();
		for(ItemInstance itemInstance : list){
			itemMap.put(itemInstance.getId(), itemInstance);
		}
		UserInfo user = null;
		try {
			user = tech.ascs.icity.rbac.util.Application.getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(ItemModelEntity itemModelEntity : formModelEntity.getItems()){
			if(itemModelEntity.getSystemItemType() == SystemItemType.CreateDate && ((TimeItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Update){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(), new Date()));
			}else if(itemModelEntity.getSystemItemType() == SystemItemType.Creator && ((CreatorItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Update){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(),user != null ? user.getId() : "-1"));
			}
		}

		DataModelEntity dataModel = formModel.getDataModels().get(0);
		Session session = null;
		try {
			session = getSession(dataModel);
			//开启事务
			session.beginTransaction();

			Map<String, Object> data = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);

			//主表数据
			setMasterFormItemInstances(formInstance.getItems(),data);

			setReferenceData(session, dataModel.getTableName(), formInstance, data);

			// 流程操作
			if (formInstance.getActivityInstanceId() != null) {
				taskService.completeTask(formInstance.getActivityInstanceId(), data);
				updateProcessInfo(formModel, data, formInstance.getProcessInstanceId());
			}

			session.update(dataModel.getTableName(), data);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
		}finally {
			if(session != null){
				session.close();
			}
		}
	}

	//设置关联数据
	private void setReferenceData(Session session, String tableName, FormInstance formInstance, Map<String, Object> data){
		//TODO 子表数据
		for(SubFormItemInstance subFormItemInstance : formInstance.getSubFormData()){
			String key = subFormItemInstance.getTableName()+"_list";
			DataModelEntity dataModelEntity = dataModelManager.findUniqueByProperty("tableName", subFormItemInstance.getTableName());

			//新的数据
			List<Map<String, Object>> newListMap = new ArrayList<>();
			for(SubFormDataItemInstance subFormDataItemInstance : subFormItemInstance.getItemInstances()){
				Map<String, Object> map = new HashMap<>();
				for(SubFormRowItemInstance instance : subFormDataItemInstance.getItems()){
					for(ItemInstance itemModelService : instance.getItems()){
						setItemInstance(itemModelService, map);
					}
				}
				newListMap.add(map);
			}
			List<String> idList = new ArrayList<>();
			for(Map<String, Object> newMap : newListMap){
				String id = String.valueOf(newMap.get("id"));
				if(!StringUtils.isEmpty(id)) {
					idList.add(id);
				}
			}

			//旧的数据
			List<Map<String, Object>> oldListMap = (List<Map<String, Object>>)data.get(key);
			List<Map<String, Object>> saveListMap = getNewMapData("master_id",session, data, dataModelEntity,  oldListMap,  idList,  newListMap);

			data.put(key, saveListMap);
		}

		//TODO 关联表数据
		for(DataModelInstance dataModelInstance : formInstance.getReferenceData()) {
			String key = dataModelInstance.getReferenceTable() + "_list";
			boolean flag = false;
			if (dataModelInstance.getReferenceType() == ReferenceType.ManyToOne || dataModelInstance.getReferenceType() == ReferenceType.OneToOne) {
				key = dataModelInstance.getReferenceValueColumn();
				flag = true;
			}
			List<Map<String, Object>> oldListMap = new ArrayList<>();
			//旧的数据
			Object oldData = data.get(key);
			if (flag) {
				oldListMap.add((Map<String, Object>) oldData);
			} else {
				oldListMap = (List<Map<String, Object>>) oldData;
			}
			DataModelEntity dataModelEntity = dataModelManager.findUniqueByProperty("tableName", dataModelInstance.getReferenceTable());


			List<Map<String, Object>> newListMap = new ArrayList<>();
			for (DataModelRowInstance instances : dataModelInstance.getItems()) {
				Map<String, Object> map = new HashMap<>();
				for (ItemInstance itemModelService : instances.getItems()) {
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
			String keyStr = dataModelInstance.getReferenceValueColumn();
			if("id".equals(keyStr)){
				keyStr = tableName+"_list";
			}
			Object o = null;
			if (flag) {
				o = data;
			}else{
				List<Map<String, Object>> map = new ArrayList<>();
				map.add(data);
				o = map;
			}
			//新的数据
			List<Map<String, Object>> saveListMap = getNewMapData(keyStr, session, o, dataModelEntity,  oldListMap,  idList,  newListMap);
			if (flag && saveListMap.size() > 0) {
				data.put(key, new ArrayList<>(saveListMap).get(0));
			}else{
				data.put(key, saveListMap);
			}
		}
	}

	private List<Map<String, Object>> getNewMapData(String referenceKey, Session session, Object data, DataModelEntity dataModelEntity, List<Map<String, Object>> oldListMap, List<String> idList, List<Map<String, Object>> newListMap){
		List<Map<String, Object>> saveListMap = new ArrayList<>();
		//旧的数据
		if(oldListMap != null && oldListMap.size() > 0) {
			for (Map<String, Object> map : oldListMap) {
				if (idList.contains(String.valueOf(map.get("id")))) {
					continue;
				}
				Session subFormSession = session;//getSession(dataModelEntity);
				Map<String, Object> subFormData = (Map<String, Object>) subFormSession.load(dataModelEntity.getTableName(), String.valueOf(map.get("id")));
				//子表数据
				if(subFormData == null || subFormData.keySet() == null) {
					throw new IFormException("没有查询到【" + dataModelEntity.getTableName() + "】表，id【"+String.valueOf(map.get("id"))+"】的数据");
				}
				subFormData.remove(referenceKey);
				subFormData.put(referenceKey, null);
				//saveListMap.add(subFormData);
			}
		}
		if(newListMap != null && newListMap.size() > 0) {
			for (Map<String, Object> newMap : newListMap) {
				String id = (String) newMap.get("id");
				Map<String, Object> subFormData = new HashMap<>();
				Session subFormSession = session;//getSession(dataModelEntity);
				if (id != null) {
					subFormData = (Map<String, Object>) subFormSession.load(dataModelEntity.getTableName(), id);
					if(subFormData == null || subFormData.keySet() == null) {
						throw new IFormException("没有查询到【" + dataModelEntity.getTableName() + "】表，id【"+ id +"】的数据");
					}
				} else {
					Map<String, Object> dataMap = new HashMap<>();
					for (String keyString : newMap.keySet()) {
						if (!"id".equals(keyString)) {
							dataMap.put(keyString, newMap.get(keyString));
						}
					}
					subFormData = (Map<String, Object>) subFormSession.merge(dataModelEntity.getTableName(), dataMap);
				}
				//子表数据
				subFormData.put(referenceKey, data);
				saveListMap.add(subFormData);
			}
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
		Object value = itemInstance.getValue();
		if (itemModel.getType() == ItemType.DatePicker) {
			try {
				value = itemInstance.getValue() == null ? null : new Date(Long.parseLong(String.valueOf(itemInstance.getValue())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (itemModel.getType() == ItemType.Select || itemModel.getType() == ItemType.RadioGroup || itemModel.getType() == ItemType.CheckboxGroup) {
            Object o = itemInstance.getValue();
            if(o != null && o instanceof List){
                value = String.join(",", (List)o );
            }else{
                value = o == null || StringUtils.isEmpty(o) ? null : o;
            }
		} else if (itemModel.getType() == ItemType.Media || itemModel.getType() == ItemType.Attachment) {
            JSONObject allJson = new JSONObject();
            Object o = itemInstance.getValue();
			if(o != null && o instanceof List){
				List<FileUploadEntity> oldList = fileUploadManager.query().filterEqual("fromSource", itemModel.getId()).filterEqual("uploadType", FileUploadType.ItemModel).list();
				Map<String, FileUploadEntity> map = new HashMap<>();
				for(FileUploadEntity entity : oldList){
					map.put(entity.getId(), entity);
				}
                List<FileUploadModel> list = new ArrayList<>();

				List<Map<String, Object>> maplist = (List<Map<String, Object>>)o;
                for(Map<String, Object> mapStr : maplist){
                    FileUploadModel fileUploadModel = new FileUploadModel();
                    fileUploadModel.setUrl((String)mapStr.get("url"));
					fileUploadModel.setFileKey((String)mapStr.get("fileKey"));
                    fileUploadModel.setName((String)mapStr.get("name"));
                    fileUploadModel.setId((String)mapStr.get("id"));
                    list.add(fileUploadModel);
                }
				List<FileUploadEntity> newList = new ArrayList<>();
				for(FileUploadModel fileUploadModel : list){
					if(!fileUploadModel.isNew()){
						FileUploadEntity fileUploadEntity = map.remove(fileUploadModel.getId());
						if(fileUploadEntity != null) {
							newList.add(fileUploadEntity);
						}
					}else {
						FileUploadEntity fileUploadEntity = new FileUploadEntity();
						BeanUtils.copyProperties(fileUploadModel, fileUploadEntity);
						fileUploadEntity.setFromSource(itemModel.getId());
						newList.add(fileUploadManager.save(fileUploadEntity));
					}
				}
				for(String key : map.keySet()){
					fileUploadManager.deleteById(key);
				}
				value = String.join(",", newList.parallelStream().map(FileUploadEntity::getId).collect(Collectors.toList()));
			}else{
				value = o == null || StringUtils.isEmpty(o) ? null : o;
			}
		} else {
            value = itemInstance.getValue() == null || StringUtils.isEmpty(itemInstance.getValue()) ? null : itemInstance.getValue();
        }
		ColumnModelEntity columnModel = itemModel.getColumnModel();
		if (Objects.nonNull(columnModel)) {
			data.put(columnModel.getColumnName(), value);
		}
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
		Map<String, Object> entity = null;
		try {
			 entity = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
			if(entity == null || entity.keySet() == null) {
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+ instanceId +"】的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException("删除【" + formModel.getName() + "】表单，instanceId【"+ instanceId +"】的数据失败");
		}finally {
			try {
				if(entity != null) {
					session.beginTransaction();
					session.delete(dataModel.getTableName(), entity);
					session.getTransaction().commit();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new IFormException("删除【" + formModel.getName() + "】表单，instanceId【"+ instanceId +"】的数据失败");
			}finally {
				if(session != null){
					session.close();
				}
			}
		}
	}

	protected void updateProcessInfo(FormModelEntity formModel, Map<String, Object> entity, String processInstanceId) {
		ProcessInstance processInstance = processInstanceService.get(processInstanceId);
		entity.put("PROCESS_ID", formModel.getProcess().getId());
		entity.put("PROCESS_INSTANCE", processInstanceId);
		entity.put("ACTIVITY_ID", processInstance.getCurrentActivityId());
		entity.put("ACTIVITY_INSTANCE", processInstance.getCurrentActivityInstanceId());
	}

	protected Session getSession(DataModelEntity dataModel) {
        SessionFactory sessionFactory = null;
        try {
            sessionFactory = sessionFactoryBuilder.getSessionFactory(dataModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionFactory == null ? null : sessionFactory.openSession();
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
			ItemModelEntity itemModel = sortItem.getItemModel();
			if (Objects.nonNull(itemModel)) {
				ColumnModelEntity columnModel = itemModel.getColumnModel();
				if (Objects.nonNull(columnModel)) {
					String propertyName = columnModel.getColumnName();
					if (sortItem.isAsc()) {
						criteria.addOrder(Order.asc(propertyName));
					} else {
						criteria.addOrder(Order.desc(propertyName));
					}
				}
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
			setItemInstance(itemModel, referenceFlag, entity, referenceDataModelList,
					 subFormItems, items, formInstance);
		}
		formInstance.getItems().addAll(items);
		return formInstance;
	}

	private void setItemInstance(ItemModelEntity itemModel, boolean referenceFlag, Map<String, Object> entity, List<DataModelInstance> referenceDataModelList,
								 List<SubFormItemInstance> subFormItems, List<ItemInstance> items, FormInstance formInstance){
		System.out.println(itemModel.getId()+"____begin");
		ColumnModelEntity column = itemModel.getColumnModel();
		if(column == null && !(itemModel instanceof  ReferenceItemModelEntity) && !(itemModel instanceof  RowItemModelEntity) && !(itemModel instanceof SubFormItemModelEntity)){
			return;
		}
		Object value = new Object();
		if(column != null) {
			value = entity.get(column.getColumnName());
		}
		if(itemModel instanceof ReferenceItemModelEntity){
			if(!referenceFlag){
				return;
			}
			setReferenceItemInstance(itemModel,  entity,  referenceDataModelList);
		}else if(itemModel instanceof SubFormItemModelEntity) {
			if(!referenceFlag){
				return;
			}
			setSubFormItemInstance( itemModel,  entity,  subFormItems, formInstance);
		}else if(itemModel instanceof RowItemModelEntity){
			for(ItemModelEntity itemModelEntity : ((RowItemModelEntity) itemModel).getItems()) {
				setItemInstance(itemModelEntity, referenceFlag, entity, referenceDataModelList,
						 subFormItems,  items, formInstance);
			}
		}else{
			ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
			items.add(itemInstance);
			formInstance.addData(itemModel.getColumnModel().getId(), itemInstance.getValue());
		}
		System.out.println(itemModel.getId()+"____end");
	}

	private void setReferenceItemInstance(ItemModelEntity itemModel, Map<String, Object> entity, List<DataModelInstance> referenceDataModelList){
		//主表字段
		ReferenceItemModelEntity fromItem = (ReferenceItemModelEntity)itemModel;

		//关联表数据模型
		FormModelEntity toModelEntity = fromItem.getReferenceList().getMasterForm();
		if (toModelEntity == null) {
			return;
		}

		//关联的item
		ItemModelEntity toItemModel = itemModelManager.find(fromItem.getReferenceItemId());

		ColumnModelEntity columnModelEntity = fromItem.getColumnModel();
		if(fromItem.getReferenceType() != ReferenceType.ManyToMany && columnModelEntity == null){
			return;
		}
		ItemModelEntity item = itemModelManager.find(fromItem.getReferenceItemId());
		if(item == null || item.getColumnModel() == null){
			return;
		}
		//关联字段
		String referenceColumnName = item.getColumnModel().getColumnName();
		if(fromItem.getReferenceType() == ReferenceType.ManyToOne || fromItem.getReferenceType() == ReferenceType.OneToOne){
			Map<String, Object> listMap = (Map<String, Object>)entity.get(referenceColumnName);
			if( listMap == null || listMap.size() == 0) {
				return;
			}
			FormInstance f = getBaseItemInstance(toModelEntity, listMap);
			DataModelInstance dataModelInstance = new DataModelInstance();
			dataModelInstance.setId(toModelEntity.getDataModels().get(0).getId());
			dataModelInstance.setName(toModelEntity.getDataModels().get(0).getName());
			dataModelInstance.setReferenceTable(toModelEntity.getDataModels().get(0).getTableName());
			dataModelInstance.setReferenceType(fromItem.getReferenceType());
			dataModelInstance.setReferenceValueColumn(columnModelEntity.getColumnName());
			dataModelInstance.setSize(1);
			DataModelRowInstance dataModelRowInstance = new DataModelRowInstance();
			dataModelRowInstance.setRowNumber(1);
			dataModelRowInstance.setItems(f.getItems());
			dataModelInstance.getItems().add(dataModelRowInstance);
			referenceDataModelList.add(dataModelInstance);
		}else if(fromItem.getReferenceType() == ReferenceType.ManyToMany || fromItem.getReferenceType() == ReferenceType.OneToMany){
			String key = toModelEntity.getDataModels().get(0).getTableName()+"_list";
			if(fromItem.getReferenceType() == ReferenceType.OneToMany){
				key = toItemModel.getColumnModel().getColumnName();
			}
			List<Map<String, Object>> listMap = (List<Map<String, Object>>)entity.get(key);
			if( listMap == null || listMap.size() == 0) {
				return;
			}
			DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, entity, listMap);
			dataModelInstance.setReferenceType(fromItem.getReferenceType());
			dataModelInstance.setReferenceValueColumn(referenceColumnName);
			referenceDataModelList.add(dataModelInstance);
		}
	}


	private void setSubFormItemInstance(ItemModelEntity itemModel, Map<String, Object> entity, List<SubFormItemInstance> subFormItems, FormInstance formInstance){
		//TODO 子表数据结构
		SubFormItemModelEntity itemModelEntity = (SubFormItemModelEntity)itemModel;
		String key =((SubFormItemModelEntity) itemModel).getTableName()+"_list";
		List<Map<String, Object>> listMap = (List<Map<String, Object>>)entity.get(key);
		if( listMap == null || listMap.size() == 0) {
			return;
		}
		SubFormItemInstance subFormItemInstance = new SubFormItemInstance();
		List<SubFormDataItemInstance> subFormItemInstances = new ArrayList<SubFormDataItemInstance>();
		subFormItemInstance.setId(itemModelEntity.getId());
		subFormItemInstance.setItemInstances(subFormItemInstances);
		subFormItemInstance.setTableName(itemModelEntity.getTableName());
		int row = 1;
		for(Map<String, Object> map : listMap) {
			SubFormDataItemInstance subFormDataItemInstance = new SubFormDataItemInstance();
			List<SubFormRowItemInstance> subFormRowItemInstanceList = new ArrayList<>();
			for (SubFormRowItemModelEntity subFormRowItemModelEntity : itemModelEntity.getItems()) {
				SubFormRowItemInstance subFormRowItemInstance = new SubFormRowItemInstance();
				subFormRowItemInstance.setRowNumber(subFormRowItemModelEntity.getRowNumber());
				subFormRowItemInstance.setId(subFormRowItemModelEntity.getId());
				List<ItemInstance> instances = new ArrayList<>();
				subFormRowItemInstance.setItems(instances);
				for (ItemModelEntity item : subFormRowItemModelEntity.getItems()) {
					ColumnModelEntity columnModelEntity  = item.getColumnModel();
					if(columnModelEntity == null){
						continue;
					}
					ItemInstance itemInstance = setItemInstance(columnModelEntity.getKey(), item, map.get(columnModelEntity.getColumnName()), formInstance.getActivityId());
					instances.add(itemInstance);
				}
				//这一行没有数据
				if(instances.size() < 1){
					continue;
				}
				//子表主键id
				ColumnModelEntity subFormColumnModelEntity  = itemModel.getColumnModel();
				ItemInstance subFomrItemInstance = setItemInstance(subFormColumnModelEntity.getKey(), itemModel, map.get("id"), formInstance.getActivityId());
				instances.add(subFomrItemInstance);

				subFormRowItemInstance.setItems(instances);
				subFormRowItemInstanceList.add(subFormRowItemInstance);
			}
			subFormDataItemInstance.setRowNubmer(row ++);
			subFormDataItemInstance.setItems(subFormRowItemInstanceList);
			subFormItemInstances.add(subFormDataItemInstance);
		}
		subFormItems.add(subFormItemInstance);
	}

	private DataModelInstance setDataModelInstance(FormModelEntity toModelEntity, Map<String, Object> entity, List<Map<String, Object>> listMap){

		List<Map<String, Object>> listData = new ArrayList<>(listMap);
		int pageIndex = 1;
		int pageSize = 10;
		List<Map<String, Object>> referenceListData = listData.subList((pageIndex-1) * pageSize, pageIndex * pageSize -1 < listData.size()  ? pageIndex * pageSize : listData.size());

		DataModelInstance dataModelInstance = new DataModelInstance();
		dataModelInstance.setId(toModelEntity.getDataModels().get(0).getId());
		dataModelInstance.setName(toModelEntity.getDataModels().get(0).getName());
		dataModelInstance.setReferenceTable(toModelEntity.getDataModels().get(0).getTableName());
		dataModelInstance.setSize(listMap.size());
		int row = 1;
		for(Map<String, Object> map : referenceListData) {
			FormInstance newFormInstance = getBaseItemInstance(toModelEntity, map);
			DataModelRowInstance dataModelRowInstance = new DataModelRowInstance();
			dataModelRowInstance.setRowNumber(row++);
			dataModelRowInstance.setItems(newFormInstance.getItems());
			dataModelInstance.getItems().add(dataModelRowInstance);
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
		itemInstance.setType(itemModel.getType());
		itemInstance.setSystemItemType(itemModel.getSystemItemType());
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

	@Override
	public void updateValue(ItemModelEntity itemModel, ItemInstance itemInstance, Object value) {
		if (value == null) {
			return;
		}
		switch(itemModel.getType()) {
			case DatePicker:
				Date date = (Date) value;
				itemInstance.setValue(date);
				itemInstance.setDisplayValue(DateFormatUtils.format(date,((TimeItemModelEntity)itemModel).getTimeFormat() == null ? "yyyy-MM-dd HH:mm:ss" : ((TimeItemModelEntity)itemModel).getTimeFormat()));
				break;
			case Select:
				setSelectItemValue(itemModel, itemInstance, value);
				break;
			case RadioGroup:
				setSelectItemValue(itemModel, itemInstance, value);
				break;
			case CheckboxGroup:
				setSelectItemValue(itemModel, itemInstance, value);
				break;
			case Media:
				setFileItemInstance(value, itemInstance);
				break;
			case Attachment:
				setFileItemInstance(value, itemInstance);
				break;
			default:
                String valueStr = value == null || StringUtils.isEmpty(value) ?  null : String.valueOf(value);
                itemInstance.setValue(value);
				itemInstance.setDisplayValue(valueStr);
				break;
		}
	}

	private void setSelectItemValue(ItemModelEntity itemModel, ItemInstance itemInstance, Object value){
		String valueString = value == null || StringUtils.isEmpty(value) ?  null : String.valueOf(value);
		String[] values = valueString == null ?  null : valueString.split(",");
		List<String> list = new ArrayList<>();
		if(values != null){
			list = Arrays.asList(values);
		}
		itemInstance.setValue(list);
		List<String> displayValuelist = new ArrayList<>();
		SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)itemModel;
		if((selectItemModelEntity.getSelectReferenceType() == SelectReferenceType.Dictionary || (selectItemModelEntity.getReferenceDictionaryItemId() != null && selectItemModelEntity.getReferenceDictionaryId() != null))
				&& list != null && list.size() > 0){
			List<DictionaryItemEntity> dictionaryItemEntities = dictionaryItemManager.query().filterIn("id",list).list();
			if(dictionaryItemEntities != null) {
				for (DictionaryItemEntity dictionaryItemEntity : dictionaryItemEntities) {
					displayValuelist.add(dictionaryItemEntity.getName());
				}
			}
			itemInstance.setDisplayValue(displayValuelist);
		}else if(itemModel.getOptions() != null && itemModel.getOptions().size() > 0) {
			for (ItemSelectOption option : itemModel.getOptions()) {
				if (displayValuelist.contains(option.getId())) {
					displayValuelist.add(option.getLabel());
				}
			}
			itemInstance.setDisplayValue(displayValuelist);
		}else {
			displayValuelist.add(valueString);
			itemInstance.setDisplayValue(displayValuelist);
		}
	}

	private void setFileItemInstance(Object value, ItemInstance itemInstance){
		String valueStr = value == null || StringUtils.isEmpty(value) ?  null : String.valueOf(value);
		if(valueStr != null) {
			List<String> listv = Arrays.asList(valueStr.split(","));
			List<FileUploadModel> listModels = new ArrayList<>();
			List<FileUploadEntity> entityList = fileUploadManager.query().filterIn("id", listv).list();
			for(FileUploadEntity entity : entityList){
				FileUploadModel fileUploadModel = new FileUploadModel();
				BeanUtils.copyProperties(entity, fileUploadModel);
				fileUploadModel.setUrl(StringUtils.hasText(entity.getFileKey()) ? uploadService.getFileUrl(entity.getFileKey()) : entity.getUrl());
				listModels.add(fileUploadModel);
			}
			itemInstance.setValue(listModels);
			itemInstance.setDisplayValue(listModels);
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
