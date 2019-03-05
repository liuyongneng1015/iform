package tech.ascs.icity.iform.service.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tech.ascs.icity.admin.api.model.TreeSelectData;
import tech.ascs.icity.admin.api.model.User;
import tech.ascs.icity.admin.client.GroupService;
import tech.ascs.icity.admin.client.UserService;
import tech.ascs.icity.iflow.api.model.ProcessInstance;
import tech.ascs.icity.iflow.client.ProcessInstanceService;
import tech.ascs.icity.iflow.client.TaskService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.iform.utils.CurrentUserUtils;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.rbac.feign.model.UserInfo;

@Service
public class FormInstanceServiceExImpl extends DefaultJPAService<FormModelEntity> implements FormInstanceServiceEx {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final Random random = new Random();

	@Autowired
	private IFormSessionFactoryBuilder sessionFactoryBuilder;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Autowired
	private FormModelService formModelService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private UserService userService;

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

	@Autowired
	ColumnModelService columnModelService;

    @Autowired
    GroupService groupService;

    @Autowired
    ItemModelService itemModelService;

	public
	FormInstanceServiceExImpl() {
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
	public List<FormInstance> listFormInstance(ListModelEntity listModel, Map<String, Object> queryParameters) {
		Criteria criteria = generateCriteria(listModel, queryParameters);
		addSort(listModel, criteria);

		return wrapList(listModel, criteria.list());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<FormDataSaveInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, Object> queryParameters) {
		Criteria criteria = generateCriteria(listModel, queryParameters);
		addCreatorCriteria(criteria, listModel);
		addSort(listModel, criteria);

		criteria.setFirstResult((page - 1) * pagesize);
		criteria.setMaxResults(pagesize);
		List<Map<String, Object>> entities = null;
		Page<FormDataSaveInstance> result = Page.get(page, pagesize);
		try {
			entities = criteria.list();
		} catch (HibernateException e) {
			e.printStackTrace();
			return result;
		}

		List<FormDataSaveInstance> list = wrapFormDataList(listModel, entities);

		criteria.setFirstResult(0);
		criteria.setProjection(Projections.rowCount());
		Number count = (Number) criteria.uniqueResult();

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


    private List<String> listByTableName(ItemType itemType, String tableName, String key, Object value) {
		StringBuffer params = new StringBuffer("'");
		if(value instanceof List){
			List<String> valueList = new ArrayList<>();
			if(itemType == ItemType.Media || itemType == ItemType.Attachment){
				List<FileUploadModel> maplist = (List<FileUploadModel>)value;
				for(FileUploadModel fileUploadModel : maplist){
					valueList.add(fileUploadModel.getId());
				}
			}else {
				valueList = (List) value;
			}
			params.append(String.join(",", valueList));
		}else{
			params.append(String.valueOf(value));
			if(itemType == ItemType.Media || itemType == ItemType.Attachment){
				params.append(((FileUploadModel)value).getId());
			}
		}
		params.append("'");
		StringBuilder sql = new StringBuilder("SELECT id FROM if_").append(tableName).append(" where ").append(key).append("="+params.toString());
		if(itemType == ItemType.InputNumber){
			sql = new StringBuilder("SELECT id FROM if_").append(tableName).append(" where ").append(key).append("="+value);
		}

        List<String> list = jdbcTemplate.queryForList(sql.toString(),String.class);
        return list;
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
		try {
			DataModelEntity dataModel = formModel.getDataModels().get(0);
			Map<String, Object> map =  getDataInfo(dataModel, instanceId);
			formInstance = wrapEntity(formModel, map, instanceId, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException("没有查询到【" + formModel.getName() + "】表单，instanceId【"+instanceId+"】的数据");
		}
		return formInstance;
	}

	private Map<String, Object> getDataInfo(DataModelEntity dataModel, String instanceId){
		Session session = null;
		Map<String, Object> map = new HashMap<>();
		try {
			session = getSession(dataModel);
			map = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
			if(map == null || map.keySet() == null){
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
			}
		} catch (Exception e) {
			throw e;
		}finally {
			if(session != null){
				session.close();
			}
		}
		return map;
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
	public String createFormInstance(FormModelEntity formModel, FormDataSaveInstance formInstance) {
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

		List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
		itemModelEntityList.addAll(formModelEntity.getItems());
		for(ItemModelEntity itemModelEntity : formModelEntity.getItems()){
			if(itemModelEntity instanceof RowItemModelEntity){
				itemModelEntityList.addAll(((RowItemModelEntity)itemModelEntity).getItems());
			}
		}
		for(ItemModelEntity itemModelEntity : itemModelEntityList){
			if(itemModelEntity.getSystemItemType() == SystemItemType.CreateDate && ((TimeItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Create){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}

				list.add(getItemInstance(itemModelEntity.getId(), getNowTime(((TimeItemModelEntity) itemModelEntity).getTimeFormat())));

			}else if(itemModelEntity.getSystemItemType() == SystemItemType.Creator && ((CreatorItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Create){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(), user != null ? user.getId() : "-1"));
			}else if(itemModelEntity.getSystemItemType() == SystemItemType.SerialNumber){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				String format = ((SerialNumberItemModelEntity)itemModelEntity).getTimeFormat();
				StringBuffer str = new StringBuffer(((SerialNumberItemModelEntity)itemModelEntity).getPrefix());
				str.append("_");
				if(format != null){
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
					str.append(simpleDateFormat.format(new Date()));
					str.append("_");
				}
				str.append(getRandom(((SerialNumberItemModelEntity)itemModelEntity).getSuffix()));
				list.add(getItemInstance(itemModelEntity.getId(), str.toString()));
			}
		}


		DataModelEntity dataModel = formModel.getDataModels().get(0);
		Session session = getSession(dataModel);
		session.beginTransaction();
		Map<String, Object> data = new HashMap<String, Object>();

		//主表数据
		setMasterFormItemInstances(formInstance, data, DisplayTimingType.Add);
		data.put("create_at", new Date());
		data.put("create_by",  user != null ? user.getId() : "-1");

		//设置子表数据
		setSubFormReferenceData(session, user, formInstance, data, DisplayTimingType.Add);
		//关联表数据
		saveReferenceData( user,formInstance, data,  session,  formModelEntity.getDataModels().get(0).getTableName(), formModelService.findAllItems(formModelEntity), DisplayTimingType.Add);

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

	/**
	 * 生成指定位数的随机数
	 * @param length
	 * @return
	 */
	public static String getRandom(Integer length){
		if(length == null){
			return "";
		}
		String val = "";
		for (int i = 0; i < length; i++) {
			val += String.valueOf(random.nextInt(10));
		}
		return val;
	}




	private long getNowTime(String format){
		String timeFormat = format == null ? "yyyy-MM-dd HH:mm:ss" : format;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat);
		String dateStr = simpleDateFormat.format(new Date());
		Date date = null;
		try {
			date = simpleDateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IFormException("时间转换异常");
		}
		return  date.getTime();
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
	public void updateFormInstance(FormModelEntity formModel, String instanceId, FormDataSaveInstance formInstance) {

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
		List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
		itemModelEntityList.addAll(formModelEntity.getItems());
		for(ItemModelEntity itemModelEntity : formModelEntity.getItems()){
			if(itemModelEntity instanceof RowItemModelEntity){
				itemModelEntityList.addAll(((RowItemModelEntity)itemModelEntity).getItems());
			}
		}
		for(ItemModelEntity itemModelEntity : itemModelEntityList){
			if(itemModelEntity.getSystemItemType() == SystemItemType.CreateDate && ((TimeItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Update){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(), getNowTime(((TimeItemModelEntity) itemModelEntity).getTimeFormat())));
			}else if(itemModelEntity.getSystemItemType() == SystemItemType.Creator && ((CreatorItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Update){
				if(itemMap.keySet().contains(itemModelEntity.getId())){
					list.remove(itemMap.get(itemModelEntity.getId()));
				}
				list.add(getItemInstance(itemModelEntity.getId(), user != null ? user.getId() : "-1"));
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
			setMasterFormItemInstances(formInstance, data, DisplayTimingType.Update);
			data.put("update_at", new Date());
			data.put("update_by",  user != null ? user.getId() : "-1");

			setSubFormReferenceData(session, user, formInstance, data, DisplayTimingType.Update);
			//关联表数据
			saveReferenceData(user, formInstance, data,  session,  formModelEntity.getDataModels().get(0).getTableName(), formModelService.findAllItems(formModelEntity), DisplayTimingType.Update);

			// 流程操作
			if (formInstance.getActivityInstanceId() != null) {
				taskService.completeTask(formInstance.getActivityInstanceId(), data);
				updateProcessInfo(formModel, data, formInstance.getProcessInstanceId());
			}

			session.update(dataModel.getTableName(), data);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof IFormException){
				throw e;
			}
			throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
		}finally {
			if(session != null){
				session.close();
			}
		}
	}

	//设置子表关联数据
	private void setSubFormReferenceData(Session session, UserInfo user, FormDataSaveInstance formInstance, Map<String, Object> data, DisplayTimingType displayTimingType){
		//主表
		FormModelEntity masterFormModelEntity = formModelService.get(formInstance.getFormId());

		//子表
		List<String> slaverModelsList = new ArrayList<>();
		for(DataModelEntity dataModelEntity : masterFormModelEntity.getDataModels().get(0).getSlaverModels()){
			slaverModelsList.add(dataModelEntity.getTableName());
		}


		//TODO 子表数据
		if(formInstance.getSubFormData() != null &&formInstance.getSubFormData().size() > 0) {
			for (SubFormItemInstance subFormItemInstance : formInstance.getSubFormData()) {
				String key = subFormItemInstance.getTableName() + "_list";
				slaverModelsList.remove(subFormItemInstance.getTableName());
				DataModelEntity dataModelEntity = dataModelManager.findUniqueByProperty("tableName", subFormItemInstance.getTableName());
				if(dataModelEntity == null){
					continue;
				}
				//子表session
				Session subFormSession = getSession(dataModelEntity);
				subFormSession.beginTransaction();
				//新的数据
				List<Map<String, Object>> newListMap = new ArrayList<>();
				List<ReferenceDataInstance> referenceData = new ArrayList<>();
				List<String> idList = new ArrayList<>();
				List<ItemModelEntity> referenceItemModelEntityList = new ArrayList<>();
				for (SubFormDataItemInstance subFormDataItemInstance : subFormItemInstance.getItemInstances()) {
					Map<String, Object> map = new HashMap<>();
					Map<String, List<String>> stringListMap = new HashMap<>();
					for (SubFormRowItemInstance instance : subFormDataItemInstance.getItems()) {
						for (ItemInstance itemModelService : instance.getItems()) {
							ItemModelEntity itemModel = itemModelManager.get(itemModelService.getId());
							if(itemModel != null && itemModelService.getValue() != null && StringUtils.hasText(String.valueOf(itemModelService.getValue())) &&
									(itemModel.getType() == ItemType.SubForm || itemModel.getColumnModel() != null && itemModel.getColumnModel().getColumnName().equals("id"))){
								map = (Map<String, Object>)subFormSession.load(dataModelEntity.getTableName(), (String)itemModelService.getValue());
								break;
							}
						}
					}
					for (SubFormRowItemInstance instance : subFormDataItemInstance.getItems()) {
						for (ItemInstance itemModelService : instance.getItems()) {
							ItemModelEntity itemModel = itemModelManager.get(itemModelService.getId());
							if((itemModel instanceof ReferenceItemModelEntity) && itemModel.getType() != ItemType.ReferenceLabel ){
								referenceItemModelEntityList.add(itemModel);
							}
							if(itemModel.getUniquene() != null && itemModel.getUniquene()){
								List<String> list = listByTableName(itemModelService.getType(), dataModelEntity.getTableName(), "f" + itemModel.getColumnModel().getColumnName(), itemModelService.getValue());
								if(list != null && list.size() > 0) {
									stringListMap.put(itemModelService.getId()+"_"+itemModelService.getItemName(), list);
								}
							}
							if(itemModel instanceof ReferenceItemModelEntity && itemModel.getType() != ItemType.ReferenceLabel){
								ReferenceDataInstance referenceDataInstance = new ReferenceDataInstance();
								referenceDataInstance.setId(itemModel.getId());
								referenceDataInstance.setValue(itemModelService.getValue());
								referenceDataInstance.setDisplayValue(itemModelService.getDisplayValue());
								referenceData.add(referenceDataInstance);
							}else {
								setItemInstance(itemModel, itemModelService, map, displayTimingType);
							}
						}
					}
					for(String str : stringListMap.keySet()){
						for(String string : stringListMap.get(str)){
							if(StringUtils.hasText(str) && (map.get("id") == null ||  !str.equals(map.get("id")))){
								String[] strings = string.split("_");
								throw new IFormException(strings[strings.length-1]+"必须唯一");
							}
						}
					}
					if(displayTimingType == DisplayTimingType.Add){
						map.put("create_at", new Date());
						map.put("create_by", user != null ? user.getId() : "-1" );
					}else{
						map.put("update_at", new Date());
						map.put("update_by", user != null ? user.getId() : "-1");
					}
					String id = map.get("id") == null ? null : String.valueOf(map.get("id"));
					if (StringUtils.hasText(id)) {
						idList.add(id);
					}
					//保存子表数据
					FormDataSaveInstance formDataSaveInstance = new FormDataSaveInstance();
					formDataSaveInstance.setFormId(formInstance.getFormId());
					formDataSaveInstance.setReferenceData(referenceData);
					saveReferenceData(user, formDataSaveInstance, map, subFormSession, dataModelEntity.getTableName(), referenceItemModelEntityList, displayTimingType);
					String newId = id;
					if(id != null && StringUtils.hasText(id)){
						subFormSession.merge(dataModelEntity.getTableName(), map);
					}else{
						newId = (String) subFormSession.save(dataModelEntity.getTableName(), map);
					}
					subFormSession.getTransaction().commit();
					map.put("id", newId);
					newListMap.add(map);
				}
				subFormSession.close();

				//旧的数据
				List<Map<String, Object>> oldListMap = (List<Map<String, Object>>) data.get(key);
				deleteSubFormNewMapData("master_id", session, dataModelEntity, oldListMap, idList);
				List<Map<String, Object>>  subFormData = new ArrayList<>();
				for (Map<String, Object> map : newListMap) {
					Map<String, Object> newMap = (Map<String, Object>) session.load(dataModelEntity.getTableName(), String.valueOf(map.get("id")));
					newMap.put("master_id", data);
					subFormData.add(newMap);
				}
				data.put(key, subFormData);
			}
		}
		for (String str : slaverModelsList) {
			//旧的数据
			List<Map<String, Object>> oldListMap = (List<Map<String, Object>>) data.get(str + "_list");
			if (oldListMap != null && oldListMap.size() > 0) {
				for (Map<String, Object> map : oldListMap) {
					deleteData(session, str, String.valueOf(map.get("id")), "master_id");
				}
			}
			data.put(str + "_list", null);
		}
	}

	private void saveReferenceData(UserInfo user, FormDataSaveInstance formInstance,  Map<String, Object> data, Session session, String tableName, List<ItemModelEntity> itemModelEntityList, DisplayTimingType displayTimingType){
		//TODO 关联表数据
		//关联表
		Map<String, ReferenceDataModel> referenceMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity : itemModelEntityList){
			if(itemModelEntity instanceof ReferenceItemModelEntity && itemModelEntity.getType() != ItemType.ReferenceLabel) {
				ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity)itemModelEntity;
				ReferenceDataModel referenceDataModel = new ReferenceDataModel();
				String key = "";
				boolean flag = false;
				FormModelEntity formModelEntity = formModelService.get(referenceItemModelEntity.getReferenceFormId());
				DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
				if (referenceItemModelEntity.getSelectMode() == SelectMode.Single && (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne
						|| referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne)) {
					if (referenceItemModelEntity.getColumnModel() == null) {
						continue;
					}
					key = referenceItemModelEntity.getColumnModel().getColumnName();
					flag = true;
				} else if (referenceItemModelEntity.getSelectMode() == SelectMode.Inverse && (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne
						|| referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne)) {
					ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity) itemModelManager.get(referenceItemModelEntity.getReferenceItemId());
					if (referenceItemModelEntity1.getColumnModel() == null) {
						continue;
					}
					key = referenceItemModelEntity1.getColumnModel().getColumnName() + "_list";
					if (referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne) {
						flag = true;
					}
				} else if (referenceItemModelEntity.getSelectMode() == SelectMode.Multiple) {
					key = dataModelEntity.getTableName() + "_list";
				}
				referenceDataModel.setKey(key);
				referenceDataModel.setFlag(flag);
				referenceDataModel.setTableName(dataModelEntity.getTableName());
				referenceMap.put(key, referenceDataModel);
			}
		}

		if(formInstance.getReferenceData() != null && formInstance.getReferenceData().size() > 0) {
			for (ReferenceDataInstance dataModelInstance : formInstance.getReferenceData()) {
				if (dataModelInstance.getValue() == null || (dataModelInstance.getValue() instanceof String && StringUtils.isEmpty(dataModelInstance.getValue()))) {
					continue;
				}

				ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity) itemModelManager.get(dataModelInstance.getId());
				FormModelEntity formModelEntity = formModelService.get(referenceItemModelEntity.getReferenceFormId());
				DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);

				String key = "";
				boolean flag = false;
				if (referenceItemModelEntity.getSelectMode() == SelectMode.Single && (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne
						|| referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne)) {
					if (referenceItemModelEntity.getColumnModel() == null) {
						continue;
					}
					key = referenceItemModelEntity.getColumnModel().getColumnName();
					flag = true;
				} else if (referenceItemModelEntity.getSelectMode() == SelectMode.Inverse && (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne
						|| referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne)) {
					ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity) itemModelManager.get(referenceItemModelEntity.getReferenceItemId());
					if (referenceItemModelEntity1.getColumnModel() == null) {
						continue;
					}
					key = referenceItemModelEntity1.getColumnModel().getColumnName() + "_list";
					if (referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne) {
						flag = true;
					}
				} else if (referenceItemModelEntity.getSelectMode() == SelectMode.Multiple) {
					if(referenceItemModelEntity.getSelectMode() != SelectMode.Inverse) {
						key = dataModelEntity.getTableName() + "_list";
					}
				}
				referenceMap.remove(key);
				if (!StringUtils.hasText(key)) {
					continue;
				}


				List<Map<String, Object>> oldListMap = new ArrayList<>();
				//旧的数据
				Object oldData = data.get(key);
				if (flag) {
					oldListMap.add((Map<String, Object>) oldData);
				} else {
					oldListMap = (List<Map<String, Object>>) oldData;
				}

				List<Map<String, Object>> newListMap = new ArrayList<>();
				if (dataModelInstance.getValue() != null && dataModelInstance.getValue() instanceof List) {
					for (String instances : (List<String>) dataModelInstance.getValue()) {
						Map<String, Object> map = new HashMap<>();
						map.put("id", instances);
						newListMap.add(map);
					}
				} else {
					Map<String, Object> map = new HashMap<>();
					map.put("id", dataModelInstance.getValue());
					newListMap.add(map);
				}
				List<String> idList = new ArrayList<>();
				for (Map<String, Object> newMap : newListMap) {
					String id = newMap.get("id") == null ? null : String.valueOf(newMap.get("id"));
					if (StringUtils.hasText(id)) {
						idList.add(id);
					}
				}

				Object o = null;
				if (flag) {
					o = data;
				} else {
					List<Map<String, Object>> map = new ArrayList<>();
					map.add(data);
					o = map;
				}
				//新的数据
				List<Map<String, Object>> saveListMap = getNewMapData(user, key, session, o, dataModelEntity, oldListMap, idList, newListMap, displayTimingType);

				if (referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne && referenceItemModelEntity.getColumnModel() != null && saveListMap != null && saveListMap.size() > 0) {
					String idValue = String.valueOf(saveListMap.get(0).get("id"));
					List<String> list = listByTableName(referenceItemModelEntity.getType(), tableName, referenceItemModelEntity.getColumnModel().getColumnName(), idValue);

					for (String str : list) {
						if (StringUtils.hasText(formInstance.getId()) && (str == null || !str.equals(formInstance.getId()))) {
							throw new IFormException("同一数据不能重复关联" + referenceItemModelEntity.getName());
						}
					}
				}

				if (saveListMap.size() > 0 && formInstance.getFormId().equals(referenceItemModelEntity.getReferenceFormId())) {
					for (Map<String, Object> map : saveListMap) {
						map.remove(key);
					}
				}
				if (flag && saveListMap.size() > 0) {
					data.put(key, new ArrayList<>(saveListMap).get(0));
				} else {
					data.put(key, saveListMap);
				}
			}
		}
		for(String str: referenceMap.keySet()){
			//旧的数据
			Object oldData = data.get(str);
			if(oldData != null) {
				ReferenceDataModel dataModel = referenceMap.get(str);
				if (dataModel.getFlag()) {
					deleteData(session, dataModel.getTableName(), String.valueOf(((Map<String, Object>)oldData).get("id")), str);
				} else {
					List<Map<String, Object>> oldListMap = (List<Map<String, Object>>) oldData;
					for(Map<String, Object> map : oldListMap){
						deleteData(session, dataModel.getTableName(), String.valueOf(((Map<String, Object>)map).get("id")), str);
					}
				}
			}
			data.put(str, null);
		}
	}



	private void deleteSubFormNewMapData(String referenceKey, Session session, DataModelEntity dataModelEntity, List<Map<String, Object>> oldListMap, List<String> idList){
		//旧的数据
		if(oldListMap != null && oldListMap.size() > 0) {
			for (Map<String, Object> map : oldListMap) {
				if (map == null || map.get("id") == null || idList.contains(String.valueOf(map.get("id")))) {
					continue;
				}
				deleteData( session, dataModelEntity.getTableName(),String.valueOf(map.get("id")),  referenceKey);
			}
		}
	}

	private List<Map<String, Object>> getNewMapData(UserInfo user, String referenceKey, Session session, Object data, DataModelEntity dataModelEntity, List<Map<String, Object>> oldListMap, List<String> idList, List<Map<String, Object>> newListMap, DisplayTimingType displayTimingType){
		List<Map<String, Object>> saveListMap = new ArrayList<>();
		//旧的数据
		if(oldListMap != null && oldListMap.size() > 0) {
			for (Map<String, Object> map : oldListMap) {
				if (map == null || map.get("id") == null || idList.contains(String.valueOf(map.get("id")))) {
					continue;
				}
				deleteData( session, dataModelEntity.getTableName(),String.valueOf(map.get("id")),  referenceKey);
			}
		}
		if(newListMap != null && newListMap.size() > 0) {
			for (Map<String, Object> newMap : newListMap) {
				String id = newMap.get("id") == null ? null : String.valueOf(newMap.get("id"));
				Map<String, Object> subFormData = new HashMap<>();
				if (id != null) {
					subFormData = (Map<String, Object>) session.load(dataModelEntity.getTableName(), id);
					if(subFormData == null || subFormData.keySet() == null) {
						throw new IFormException("没有查询到【" + dataModelEntity.getTableName() + "】表，id【"+ id +"】的数据");
					}
					if("master_id".equals(referenceKey)) {
						for (String keyString : newMap.keySet()) {
							if (!"id".equals(keyString)) {
								subFormData.put(keyString, newMap.get(keyString));
							}
						}
					}
					subFormData.put("update_at", new Date());
					subFormData.put("update_by", user != null ? user.getId() : "-1");
				} else {
					Map<String, Object> dataMap = new HashMap<>();
					for (String keyString : newMap.keySet()) {
						if (!"id".equals(keyString)) {
							dataMap.put(keyString, newMap.get(keyString));
						}
					}
					dataMap.put("create_at", new Date());
					dataMap.put("create_by", user != null ? user.getId() : "-1");
					subFormData = (Map<String, Object>) session.merge(dataModelEntity.getTableName(), dataMap);
				}

				//子表数据
				subFormData.put(referenceKey, data);
				saveListMap.add(subFormData);
			}
		}
		return saveListMap;
	}


	private void deleteData(Session session, String tableName, String id, String referenceKey){
		Session subFormSession = session;//getSession(dataModelEntity);
		Map<String, Object> subFormData = (Map<String, Object>) subFormSession.load(tableName, id);
		//子表数据
		if(subFormData == null || subFormData.keySet() == null) {
			throw new IFormException("没有查询到【" + tableName + "】表，id【"+id+"】的数据");
		}
		subFormData.remove(referenceKey);
		subFormData.put(referenceKey, null);
	}

	private void setMasterFormItemInstances( FormDataSaveInstance formInstance, Map<String, Object> data, DisplayTimingType displayTimingType){
		ItemInstance idItemInstance = null;
		data.put("id", formInstance.getId());
		for (ItemInstance itemInstance : formInstance.getItems()) {
			ItemModelEntity itemModel = itemModelManager.get(itemInstance.getId());
			if(itemModel.getName().equals("id")){
				idItemInstance = itemInstance;
			}
		}
		for (ItemInstance itemInstance : formInstance.getItems()) {
            ItemModelEntity itemModel = itemModelManager.get(itemInstance.getId());
            if(itemModel instanceof ReferenceItemModelEntity){
            	continue;
			}
            //唯一校验
            if(itemModel.getUniquene() != null && itemModel.getUniquene() &&itemModel.getColumnModel() != null && itemModel.getColumnModel().getDataModel() != null){
               List<String> list = listByTableName(itemModel.getType(), itemModel.getColumnModel().getDataModel().getTableName(), "f"+itemModel.getColumnModel().getColumnName(), itemInstance.getValue());

               for(String str : list){
				   if(StringUtils.hasText(str) && (idItemInstance == null ||  !str.equals(idItemInstance.getValue()))){
                        throw new IFormException(itemModel.getName()+"必须唯一");
                   }
               }
            }
            setItemInstance(itemModel, itemInstance, data, displayTimingType);
		}
	}

	private void setItemInstance(ItemModelEntity itemModel, ItemInstance itemInstance, Map<String, Object> data ,DisplayTimingType displayTimingType){
		Object value = null;
		verifyValue(itemModel, itemInstance.getValue(), displayTimingType);
		if (itemModel.getType() == ItemType.DatePicker || itemModel.getSystemItemType() == SystemItemType.CreateDate) {
			try {
				value = itemInstance.getValue() == null ? null : new Date(Long.parseLong(String.valueOf(itemInstance.getValue())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (itemModel.getType() == ItemType.Select || itemModel.getType() == ItemType.RadioGroup
				|| itemModel.getType() == ItemType.CheckboxGroup || itemModel.getType() == ItemType.Treeselect ) {
            Object o = itemInstance.getValue();
            if(o != null && o instanceof List){
                value = String.join(",", (List)o );
            }else{
                value = o == null || StringUtils.isEmpty(o) ? null : o;
            }
		} else if (itemModel.getType() == ItemType.InputNumber && ((NumberItemModelEntity)itemModel).getDecimalDigits() != null
				&& ((NumberItemModelEntity)itemModel).getDecimalDigits() > 0 && itemInstance.getValue() != null) {
			value = new BigDecimal(String.valueOf(itemInstance.getValue())).divide(new BigDecimal(1.0), ((NumberItemModelEntity)itemModel).getDecimalDigits(), BigDecimal.ROUND_DOWN).doubleValue();
		} else if (itemModel.getType() == ItemType.Media || itemModel.getType() == ItemType.Attachment) {
            Object o = itemInstance.getValue();
			Map<String, FileUploadEntity> fileUploadEntityMap = new HashMap<>();
			if(data.get("id") != null && data.get("id") != "") {
				List<FileUploadEntity> fileUploadList = fileUploadManager.query().filterEqual("fromSourceDataId", data.get("id")).filterEqual("fromSource", itemModel.getId()).filterEqual("uploadType", FileUploadType.ItemModel).list();
				for (FileUploadEntity fileUploadEntity : fileUploadList) {
					fileUploadEntityMap.put(fileUploadEntity.getId(), fileUploadEntity);
				}
			}
			if(o != null && o instanceof List){
				List<Map<String, String>> fileList = (List<Map<String, String>>)o;
				List<FileUploadEntity> newList = new ArrayList<>();
				for(Map<String, String> fileUploadModelMap : fileList){
					if(fileUploadModelMap == null || fileUploadModelMap.values() == null || fileUploadModelMap.values().size() < 1){
						continue;
					}
					FileUploadEntity fileUploadEntity = getFileUploadEntity( fileUploadModelMap, fileUploadEntityMap, itemModel.getId());
					newList.add(fileUploadEntity);
				}
				value = String.join(",", newList.parallelStream().map(FileUploadEntity::getId).collect(Collectors.toList()));
			}else{
				Map<String, String> fileUploadModel = o == null || o == "" ? null : (Map<String, String>)o;
				if(fileUploadModel != null && fileUploadModel.values() != null && fileUploadModel.values().size() > 0){
					FileUploadEntity fileUploadEntity = getFileUploadEntity( fileUploadModel, fileUploadEntityMap, itemModel.getId());
					value = fileUploadEntity.getId();
				}
			}
			for(String key : fileUploadEntityMap.keySet()){
				fileUploadManager.deleteById(key);
			}
		} else {
			value = itemInstance.getValue() == null || StringUtils.isEmpty(itemInstance.getValue()) ? null : itemInstance.getValue();
        }
		ColumnModelEntity columnModel = itemModel.getColumnModel();
		if (Objects.nonNull(columnModel)) {
			data.put(columnModel.getColumnName(), value);
		}
	}

	private FileUploadEntity getFileUploadEntity(Map<String, String> fileUploadModelMap, Map<String, FileUploadEntity> fileUploadEntityMap, String itemId){
		FileUploadEntity fileUploadEntity = null;
		if(fileUploadModelMap.get("id") != null){
			fileUploadEntity = fileUploadEntityMap.remove(fileUploadModelMap.get("id"));
			if(fileUploadEntity == null) {
				fileUploadEntity = fileUploadManager.get(fileUploadModelMap.get("id"));
				if(fileUploadEntity == null){
					throw new IFormException("未找到【"+fileUploadModelMap.get("id")+"】对应的文件");
				}
			}
		}else {
			fileUploadEntity = new FileUploadEntity();
			FileUploadModel fileUploadModel = new FileUploadModel();
			fileUploadModel.setFileKey(fileUploadModelMap.get("fileKey"));
			fileUploadModel.setName(fileUploadModelMap.get("name"));
			fileUploadModel.setUrl(fileUploadModelMap.get("url"));
			fileUploadModel.setThumbnail(fileUploadModelMap.get("thumbnail"));
			fileUploadModel.setThumbnailUrl(fileUploadModelMap.get("thumbnailUrl"));
			BeanUtils.copyProperties(fileUploadModel, fileUploadEntity);
		}
		fileUploadEntity.setUploadType(FileUploadType.ItemModel);
		fileUploadEntity.setFromSource(itemId);
		fileUploadManager.save(fileUploadEntity);
		return fileUploadEntity;
	}

	//校验字段值
	private  void verifyValue(ItemModelEntity itemModel, Object value, DisplayTimingType displayTimingType){
		if(itemModel.getPermissions() != null){
			ItemPermissionInfo addPermission = null;
			ItemPermissionInfo updatePermission = null;
			for(ItemPermissionInfo itemPermissionInfo : itemModel.getPermissions()) {
				if (displayTimingType == DisplayTimingType.Add && displayTimingType == itemPermissionInfo.getDisplayTiming()) {
					addPermission = itemPermissionInfo;
				}
				if (displayTimingType == DisplayTimingType.Update && displayTimingType == itemPermissionInfo.getDisplayTiming()) {
					updatePermission = itemPermissionInfo;
				}
			}
			if(addPermission != null && addPermission.getRequired() != null && addPermission.getRequired() && (value == null || !StringUtils.hasText(String.valueOf(value)))){
				throw  new IFormException(itemModel.getName()+"为必填");
			}
			if(updatePermission != null && updatePermission.getRequired() != null &&  updatePermission.getRequired() && (value == null || !StringUtils.hasText(String.valueOf(value)))){
				throw  new IFormException(itemModel.getName()+"为必填");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteFormInstance(FormModelEntity formModel, String instanceId) {
        List<ReferenceItemModelEntity> itemModelEntities = itemModelService.findRefenceItemByFormModelId(formModel.getId());

		DataModelEntity dataModel = formModel.getDataModels().get(0);
		ColumnModelEntity idColumnModel = columnModelService.saveColumnModelEntity(dataModel, "id");

		Session session = getSession(dataModel);
		Map<String, Object> entity = null;
		try {
			 entity = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
			if(entity == null || entity.keySet() == null) {
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+ instanceId +"】的数据");
			}
			List<ColumnReferenceEntity> referenceEntityList = idColumnModel.getColumnReferences();
			for(ColumnReferenceEntity columnReferenceEntity : referenceEntityList){
			    if(columnReferenceEntity.getReferenceType() != ReferenceType.ManyToMany ){
			        if(columnReferenceEntity.getReferenceType() == ReferenceType.OneToOne ||
                            columnReferenceEntity.getReferenceType() == ReferenceType.OneToMany ){
			            String key = columnReferenceEntity.getToColumn().getColumnName()+"_list";
			            if(entity.get(key) != null && entity.get(key) != ""){
			                String formName = null;
			                String itemName = null;
			                for(ReferenceItemModelEntity referenceItemModelEntity : itemModelEntities){
			                    FormModelEntity formModelEntity = referenceItemModelEntity.getFormModel();
			                    if(formModelEntity == null && referenceItemModelEntity.getSourceFormModelId() != null){
                                    formModelEntity = formModelService.get(referenceItemModelEntity.getSourceFormModelId());
                                }
                                formName = formModelEntity == null? "" : formModelEntity.getName();
                                itemName = referenceItemModelEntity.getName();
                                break;
                            }
                            throw new IFormException("该数据被【" + formName + "】表单的【"+itemName+"】（数据标识）关联，无法删除");
                        }
                    }
                }
            }

		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof IFormException){
			    throw e;
            }
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
                if(e instanceof IFormException){
                    throw e;
                }
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

	public void addCreatorCriteria(Criteria criteria, ListModelEntity listModel) {
		if (listModel.getDataPermissions()!=null && DataPermissionsType.MySelf.equals(listModel.getDataPermissions())) {
			String userId = CurrentUserUtils.getCurrentUserId();
			if (userId!=null) {
				criteria.add(Restrictions.eq("create_by", userId));
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected Criteria generateCriteria(ListModelEntity listModel, Map<String, Object> queryParameters) {
		FormModelEntity formModelEntity = listModel.getMasterForm();
		DataModelEntity dataModel = formModelEntity.getDataModels().get(0);
		Session session = getSession(dataModel);
		Criteria criteria = session.createCriteria(dataModel.getTableName());
		Map<String, ListSearchItem> searchItemMap = getSearchItemMaps(listModel.getSearchItems());
        List<ItemModelEntity> items = getFormAllItems(formModelEntity);
		// 要查询listModel.getSearchItems()和listModel.getQuickSearchItems()的取值
		for (ItemModelEntity itemModel:items) {
			// queryParameters的value可能是数组
			Object value = queryParameters.get(itemModel.getId());
			if (value==null) {
				continue;
			}
			Object[] values = null;
			if (value instanceof String[]) {
				List<String> list = Arrays.asList((String[])value).stream().filter(item->item!=null && StringUtils.hasText(String.valueOf(item))).collect(Collectors.toList());
				values = list.toArray(new String[]{});
			} else if (value instanceof String) {
				if (value != null && StringUtils.hasText(String.valueOf(value))) {
					values = new Object[] {value};
				}
			}
			if (values==null || values.length==0) {
				continue;
			}
			ColumnModelEntity columnModel = itemModel.getColumnModel();
			String propertyName = columnModel.getColumnName();
			boolean equalsFlag = false;

			for (int i = 0; i < values.length; i++) {
				value = values[i];
				if (itemModel.getSystemItemType() == SystemItemType.CreateDate) {
					equalsFlag = true;
					if (!(value instanceof Date)) {
						String strValue = String.valueOf(value);
						values[i] = new Date(Long.parseLong(strValue));
					}
				} else if (itemModel.getType() == ItemType.InputNumber) {
					equalsFlag = true;
					String strValue = String.valueOf(value);
					if (columnModel.getDataType() == ColumnType.Integer) {
						values[i] = Integer.parseInt(strValue);
					} else if (columnModel.getDataType() == ColumnType.Long) {
						values[i] = Long.parseLong(strValue);
					} else if (columnModel.getDataType() == ColumnType.Float) {
						values[i] = Float.parseFloat(strValue);
					} else if (columnModel.getDataType() == ColumnType.Double) {
						values[i] = Double.parseDouble(strValue);
					}
				} else if (columnModel.getDataType() == ColumnType.Boolean) {
					equalsFlag = true;
					if (!(value instanceof Boolean)) {
						String strValue = String.valueOf(value);
						values[i] = "true".equals(strValue);
					}
				}
			}

			ListSearchItem searchItem = searchItemMap.get(itemModel.getId());
			if (equalsFlag) {
				if(itemModel.getSystemItemType() == SystemItemType.CreateDate) {
					Date dateParams = timestampNumberToDate(value);
					if (Objects.nonNull(value)) {  // Timestamp
						criteria.add(Restrictions.ge(propertyName, dateParams));
						criteria.add(Restrictions.lt(propertyName, DateUtils.addDays(dateParams, 1)));
					}
				} else {
					Criterion[] conditions = Arrays.asList(values).stream().map(item->Restrictions.eq(propertyName, item)).toArray(Criterion[]::new);
					criteria.add(Restrictions.or(conditions));
//					criteria.add(Restrictions.eq(propertyName, value));  criteria.add(Restrictions.like(propertyName, "%" + value + "%"));
				}
			// searchItem 为空时，表示是快速搜索的搜索条件
			} else if (searchItem!=null && searchItem.getSearch()!=null && searchItem.getSearch().getSearchType() == SearchType.Like && itemModel.getType() != ItemType.InputNumber) {
				Criterion[] conditions = Arrays.asList(values).stream().map(item->Restrictions.like(propertyName, "%" + item + "%")).toArray(Criterion[]::new);
				criteria.add(Restrictions.or(conditions));
			} else {
				Criterion[] conditions = Arrays.asList(values).stream().map(item->Restrictions.eq(propertyName, item)).toArray(Criterion[]::new);
				criteria.add(Restrictions.or(conditions));
			}

		}
		return criteria;
	}

	/**
	 * 时间戳数字字符串转时间
	 * @param object
	 * @return
	 */
	public Date timestampNumberToDate(Object object) {
		if (Objects.nonNull(object) && !StringUtils.isEmpty(object.toString())) {
			String valueStr = object.toString();
			try {
				return new Date(Long.valueOf(valueStr));
			} catch (Exception e) {
				throw new IFormException("时间参数不对，时间查询条件应该是时间戳数字类型");
			}
		} else {
			return null;
		}
	}

	public List<ItemModelEntity> getFormAllItems(FormModelEntity formModelEntity) {
		List<ItemModelEntity> list = new ArrayList<>();
		for (ItemModelEntity itemModel:formModelEntity.getItems()) {
			if (itemModel instanceof RowItemModelEntity) {
				RowItemModelEntity rowItemModelEntity = (RowItemModelEntity)itemModel;
				if (rowItemModelEntity.getItems()!=null && rowItemModelEntity.getItems().size()>0) {
					list.addAll(rowItemModelEntity.getItems());
				}
			} else if (itemModel instanceof ReferenceItemModelEntity) {
                ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity)itemModel;
				if (referenceItemModelEntity.getItems()!=null && referenceItemModelEntity.getItems().size()>0) {
					list.addAll(referenceItemModelEntity.getItems());
				}
				list.add(referenceItemModelEntity);
				ReferenceItemModelEntity parentReferenceItemModelEntity = referenceItemModelEntity.getParentItem();
				if (parentReferenceItemModelEntity!=null) {
                	list.add(parentReferenceItemModelEntity);
					ReferenceItemModelEntity grandfather = parentReferenceItemModelEntity.getParentItem();
					if (grandfather!=null) {
						list.add(grandfather);
					}
				}
			} else if (itemModel instanceof SelectItemModelEntity) {
                SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)itemModel;
                if (selectItemModelEntity.getItems()!=null && selectItemModelEntity.getItems().size()>0) {
					list.addAll(selectItemModelEntity.getItems());
					for (SelectItemModelEntity sonSelectItemModelEntity:selectItemModelEntity.getItems()) {
						if (sonSelectItemModelEntity!=null && sonSelectItemModelEntity.getItems()!=null && sonSelectItemModelEntity.getItems().size()>0) {
							list.addAll(sonSelectItemModelEntity.getItems());
						}
					}
				}
				list.add(selectItemModelEntity);
            } else {
				list.add(itemModel);
			}

		}
		return list;
	}

	// 封装控件ID和ListSearchItem的关系
	public Map<String, ListSearchItem> getSearchItemMaps(List<ListSearchItem> listSearchItems) {
		Map<String, ListSearchItem> map = new HashMap<>();
		for (ListSearchItem searchItem:listSearchItems) {
			ItemModelEntity itemModelEntity = searchItem.getItemModel();
			if (itemModelEntity!=null) {
				map.put(itemModelEntity.getId(), searchItem);
			}
		}
		return map;
	}

	protected void addSort(ListModelEntity listModel, Criteria criteria) {
		if(listModel.getSortItems() != null && listModel.getSortItems().size() > 0) {
			for (ListSortItem sortItem : listModel.getSortItems()) {
				ItemModelEntity itemModel = sortItem.getItemModel();
				if (listModel.getMasterForm() == null || listModel.getMasterForm().getDataModels() == null
						|| listModel.getMasterForm().getDataModels().size() < 1) {
					continue;
				}
				List<String> columns = new ArrayList<>();
				for (ColumnModelEntity columnModelEntity : listModel.getMasterForm().getDataModels().get(0).getColumns()) {
					columns.add(columnModelEntity.getDataModel().getTableName() + "_" + columnModelEntity.getColumnName());
				}
				if (Objects.nonNull(itemModel)) {
					ColumnModelEntity columnModel = itemModel.getColumnModel();
					if (columnModel == null || !columns.contains(columnModel.getDataModel().getTableName() + "_" + columnModel.getColumnName())) {
						continue;
					}
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
		}else{
			criteria.addOrder(Order.desc("create_at"));
			criteria.addOrder(Order.desc("id"));
		}
	}

	protected List<FormDataSaveInstance> wrapFormDataList(ListModelEntity listModel, List<Map<String, Object>> entities) {
		List<FormDataSaveInstance> FormInstanceList = new ArrayList<FormDataSaveInstance>();
		for (Map<String, Object> entity : entities) {
			FormInstanceList.add(wrapFormDataEntity(listModel, entity,String.valueOf(entity.get("id")), true));
		}
		return FormInstanceList;
	}

	protected List<FormInstance> wrapList(ListModelEntity listModel, List<Map<String, Object>> entities) {
		List<FormInstance> FormInstanceList = new ArrayList<FormInstance>();
		FormModelEntity formModel = listModel.getMasterForm();
		for (Map<String, Object> entity : entities) {
			FormInstanceList.add(wrapEntity(formModel, entity,String.valueOf(entity.get("id")),true));
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

	protected FormDataSaveInstance wrapFormDataEntity(ListModelEntity listModel, Map<String, Object> entity, String instanceId, boolean referenceFlag) {
		FormDataSaveInstance formInstance = new FormDataSaveInstance();
		FormModelEntity formModel = listModel.getMasterForm();
		formInstance.setFormId(formModel.getId());
		//数据id
		formInstance.setId(instanceId);
		if (formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getId())) {
			formInstance.setProcessId((String) entity.get("PROCESS_ID"));
			formInstance.setProcessInstanceId((String) entity.get("PROCESS_INSTANCE"));
			formInstance.setActivityId((String) entity.get("ACTIVITY_ID"));
			formInstance.setActivityInstanceId((String) entity.get("ACTIVITY_INSTANCE"));
		}
		return setFormDataInstanceModel(formInstance, formModel,  listModel, entity, referenceFlag);
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

	private FormDataSaveInstance setFormDataInstanceModel(FormDataSaveInstance formInstance, FormModelEntity formModel,  ListModelEntity listModelEntity,
														  Map<String, Object> entity, boolean referenceFlag){
		List<ItemInstance> items = new ArrayList<>();
		List<ItemModelEntity> list = listModelEntity.getMasterForm().getItems();
		List<ReferenceDataInstance> referenceDataModelList = formInstance.getReferenceData();
		List<SubFormItemInstance> subFormItems = formInstance.getSubFormData();
		for (ItemModelEntity itemModel : list) {
			setFormDataItemInstance(formModel, itemModel, referenceFlag, entity, referenceDataModelList,
					subFormItems, items, formInstance);
		}

		//展示字段
		List<String> displayIds = listModelEntity.getDisplayItems().parallelStream().map(ItemModelEntity::getId).collect(Collectors.toList());
		List<String> copyDisplayIds = new ArrayList<>();
		for(String string : displayIds){
			copyDisplayIds.add(string);
		}
		//所以的字段
		List<String> itemIds = items.parallelStream().map(ItemInstance::getId).collect(Collectors.toList());
		List<String> copyItemIds = new ArrayList<>();
		for(String string : itemIds){
			copyItemIds.add(string);
		}
		//不展示的字段
		itemIds.removeAll(displayIds);


        List<ItemInstance> newDisplayItems = new ArrayList<>();

		for(ItemInstance itemInstance : items){
            newDisplayItems.add(itemInstance);
		}

		copyDisplayIds.removeAll(copyItemIds);

		Object idVlaue = null;
		for(int i = 0; i < items.size(); i++ ){
			ItemInstance itemInstance = items.get(i);
			if(itemInstance.getSystemItemType() == SystemItemType.ID){
				idVlaue = itemInstance.getValue();
			}
		}
		for (ReferenceDataInstance referenceDataInstance : referenceDataModelList) {
			if(copyDisplayIds.contains(referenceDataInstance.getId())){
				ItemModelEntity itemModelEntity = itemModelManager.get(referenceDataInstance.getId());
				ItemInstance itemInstance = new ItemInstance();
				itemInstance.setSystemItemType(itemModelEntity == null ? SystemItemType.ReferenceList : itemModelEntity.getSystemItemType());
				itemInstance.setType(itemModelEntity == null ? ItemType.ReferenceList : itemModelEntity.getType());
				itemInstance.setId(referenceDataInstance.getId());
				itemInstance.setValue(referenceDataInstance.getValue());
				itemInstance.setDisplayValue(referenceDataInstance.getDisplayValue());
				newDisplayItems.add(itemInstance);
			}
		}

		formInstance.getItems().addAll(newDisplayItems);


		//二维码只有一张图
		if(idVlaue != null && idVlaue != "") {
			List<FileUploadEntity> fileUploadEntityList = uploadService.getFileUploadEntity(FileUploadType.FormModel, formInstance.getFormId(), String.valueOf(idVlaue));
			if (fileUploadEntityList != null && fileUploadEntityList.size() > 0) {
				FileUploadModel fileUploadModel = new FileUploadModel();
				BeanUtils.copyProperties(fileUploadEntityList.get(0), fileUploadModel);
				formInstance.setFileUploadModel(fileUploadModel);
			}
		}
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
			setSubFormItemInstance( itemModel,  entity,  subFormItems, formInstance.getActivityId());
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


	private void setFormDataItemInstance(FormModelEntity formModel, ItemModelEntity itemModel, boolean referenceFlag, Map<String, Object> entity, List<ReferenceDataInstance> referenceDataModelList,
								 List<SubFormItemInstance> subFormItems, List<ItemInstance> items, FormDataSaveInstance formInstance){
		System.out.println(itemModel.getId()+"____begin");
		ColumnModelEntity column = itemModel.getColumnModel();
		if(column == null && !(itemModel instanceof  ReferenceItemModelEntity) && !(itemModel instanceof  RowItemModelEntity)
				&& !(itemModel instanceof SubFormItemModelEntity) && !(itemModel instanceof TabsItemModelEntity)){
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
			setFormDataReferenceItemInstance(itemModel,  entity,  referenceDataModelList, referenceFlag);
		}else if(itemModel instanceof SubFormItemModelEntity) {
			if(!referenceFlag){
				return;
			}
			setSubFormItemInstance( itemModel,  entity,  subFormItems, formInstance.getActivityId());
		}else if(itemModel instanceof RowItemModelEntity){
			for(ItemModelEntity itemModelEntity : ((RowItemModelEntity) itemModel).getItems()) {
				setFormDataItemInstance(formModel, itemModelEntity, referenceFlag, entity, referenceDataModelList,
						subFormItems,  items, formInstance);
			}
		}else if(itemModel instanceof TabsItemModelEntity){
			for(TabPaneItemModelEntity itemModelEntity : ((TabsItemModelEntity) itemModel).getItems()) {
				for(ItemModelEntity itemModelEntity1 : itemModelEntity.getItems()) {
					setFormDataItemInstance(formModel, itemModelEntity1, referenceFlag, entity, referenceDataModelList,
							subFormItems, items, formInstance);
				}
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
		if (StringUtils.isEmpty(((ReferenceItemModelEntity) itemModel).getReferenceFormId())) {
			return;
		}
		FormModelEntity toModelEntity = formModelService.find(((ReferenceItemModelEntity) itemModel).getReferenceFormId());
		if (toModelEntity == null) {
			return;
		}
		if(!StringUtils.hasText(fromItem.getReferenceItemId()) && !StringUtils.hasText(fromItem.getReferenceFormId())){
			throw new IFormException("关联控件【"+fromItem.getName()+"】未找到关联属性");
		}

		ColumnModelEntity columnModelEntity = fromItem.getColumnModel();
		if(fromItem.getReferenceType() != ReferenceType.ManyToMany && columnModelEntity == null){
			return;
		}

		//设置关联属性
		if(fromItem.getSelectMode() == SelectMode.Attribute){
			setReferenceAttribute(fromItem, toModelEntity,  columnModelEntity, entity, referenceDataModelList);
			return;
		}

		Map<String, Boolean> map = getReferenceMap( fromItem,  toModelEntity);
		//关联字段
		String key = new ArrayList<>(map.keySet()).get(0);
		boolean flag = map.get(key);
		String referenceColumnName = fromItem.getColumnModel() == null ? null : fromItem.getColumnModel().getColumnName();
		Object listMap = null;
		if(flag){
			listMap = (Map<String, Object>)entity.get(key);
		}else{
			listMap = (List<Map<String, Object>>)entity.get(key);
		}
		if(flag) {
			DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, fromItem, columnModelEntity, (Map<String, Object>)listMap);
			dataModelInstance.setReferenceType(fromItem.getReferenceType());
			dataModelInstance.setReferenceValueColumn(referenceColumnName);
			referenceDataModelList.add(dataModelInstance);
		}else{
			List<Map<String, Object>> mapList = (List<Map<String, Object>>)listMap;
			for(Map<String, Object> map1 : mapList){
				DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, fromItem, columnModelEntity, map1);
				dataModelInstance.setReferenceType(fromItem.getReferenceType());
				dataModelInstance.setReferenceValueColumn(referenceColumnName);
				referenceDataModelList.add(dataModelInstance);
			}
		}

	}


	private void setFormDataReferenceItemInstance( ItemModelEntity itemModel, Map<String, Object> entity, List<ReferenceDataInstance> referenceDataModelList, boolean referenceFlag){
		//主表字段
		ReferenceItemModelEntity fromItem = (ReferenceItemModelEntity)itemModel;

		//关联表数据模型
		if (StringUtils.isEmpty(((ReferenceItemModelEntity) itemModel).getReferenceFormId())) {
			return;
		}
		FormModelEntity toModelEntity = formModelService.find(((ReferenceItemModelEntity) itemModel).getReferenceFormId());
		if (toModelEntity == null) {
			return;
		}
		if(!StringUtils.hasText(fromItem.getReferenceItemId()) && !StringUtils.hasText(fromItem.getReferenceFormId())){
			throw new IFormException("关联控件【"+fromItem.getName()+"】未找到关联属性");
		}

		ColumnModelEntity columnModelEntity = fromItem.getColumnModel();
		if(fromItem.getReferenceType() != ReferenceType.ManyToMany && columnModelEntity == null && fromItem.getSelectMode() != SelectMode.Inverse){
			return;
		}

		//设置关联属性
		if(fromItem.getSelectMode() == SelectMode.Attribute || fromItem.getType() == ItemType.ReferenceLabel){
			setFormDataReferenceAttribute(fromItem, toModelEntity,  columnModelEntity, entity, referenceDataModelList);
			return;
		}

		if(fromItem.getReferenceList() == null || fromItem.getReferenceList().getMasterForm() == null){
			throw new IFormException("关联控件【"+fromItem.getName()+"】未找到对应的列表模型");
		}

		String itemModelIds = toModelEntity.getItemModelIds();
		List<String> stringList = StringUtils.hasText(itemModelIds) ? Arrays.asList(itemModelIds.split(",")) : new ArrayList<>();
		if(stringList == null || stringList.size() < 1){
			return;
		}

		Map<String, Boolean> keyMap = getReferenceMap( fromItem,  toModelEntity);
		if(keyMap == null){
			return;
		}
		//关联字段
		String key =new ArrayList<>(keyMap.keySet()).get(0);
		boolean flag = keyMap.get(key);
		if(fromItem.getReferenceType() == ReferenceType.ManyToOne || fromItem.getReferenceType() == ReferenceType.OneToOne){
			Object listMap = null;
			if(flag) {
				listMap = (Map<String, Object>) entity.get(key);
			}else{
				listMap= (List<Map<String, Object>>) entity.get(key);
			}
			if( listMap == null) {
				return;
			}
			ReferenceDataInstance dataModelInstance = new ReferenceDataInstance();
			String formId = ((ReferenceItemModelEntity) itemModel).getReferenceFormId();
			if(StringUtils.hasText(formId)) {
				FormModelEntity formModelEntity = formModelService.find(formId);
				dataModelInstance.setReferenceTable(formModelEntity == null ? null :formModelEntity.getDataModels().get(0).getTableName());
			}
			dataModelInstance.setId(itemModel.getId());

			if(flag) {
				if(listMap  != null && ((Map<String, Object>)listMap).get("id") != null && StringUtils.hasText(String.valueOf(((Map<String, Object>)listMap).get("id")))) {
					ReferenceDataInstance referenceDataInstance = createDataModelInstance(fromItem, toModelEntity, String.valueOf(((Map<String, Object>) listMap).get("id")), stringList, false);
					dataModelInstance.setValue(referenceDataInstance.getValue());
					dataModelInstance.setDisplayValue(referenceDataInstance.getDisplayValue());
					referenceDataModelList.add(dataModelInstance);
				}
			}else{
				List<String> valueList = new ArrayList<>();
				List<String> displayValueList = new ArrayList<>();
				if(listMap != null && ((List<Map<String, Object>>) listMap).size() > 0) {
					for (Map<String, Object> map : (List<Map<String, Object>>) listMap) {
						if (map.get("id") != null && StringUtils.hasText(String.valueOf(map.get("id")))) {
							ReferenceDataInstance referenceDataInstance = createDataModelInstance(fromItem, toModelEntity, String.valueOf(map.get("id")), stringList, false);
							if (referenceDataInstance != null && referenceDataInstance.getValue() != null) {
								valueList.add(String.valueOf(referenceDataInstance.getValue()));
								displayValueList.add(String.valueOf(referenceDataInstance.getDisplayValue()));
							}
						}
					}
					dataModelInstance.setValue(String.join(",",valueList));
					dataModelInstance.setDisplayValue(String.join(",", displayValueList));
					referenceDataModelList.add(dataModelInstance);
				}
			}
		}else if(fromItem.getReferenceType() == ReferenceType.ManyToMany || fromItem.getReferenceType() == ReferenceType.OneToMany){
			if(fromItem.getReferenceType() == ReferenceType.OneToMany){
				key = getRefenrenceItem(fromItem).getColumnModel().getColumnName();
			}
			List<Map<String, Object>> listMap = (List<Map<String, Object>>)entity.get(key);
			if( listMap == null || listMap.size() == 0) {
				return;
			}
			ReferenceDataInstance dataModelInstance =  new ReferenceDataInstance();
			String formId = ((ReferenceItemModelEntity) itemModel).getReferenceFormId();
			if(StringUtils.hasText(formId)) {
				FormModelEntity formModelEntity = formModelService.find(formId);
				dataModelInstance.setReferenceTable(formModelEntity == null ? null :formModelEntity.getDataModels().get(0).getTableName());
			}
			dataModelInstance.setId(itemModel.getId());
			List<String> values = new ArrayList<>();
			List<Object> idValues = new ArrayList<>();

			for(Map<String, Object> map  : listMap) {
				idValues.add(map.get("id"));
				FormInstance getFormInstance = getFormInstance(toModelEntity, String.valueOf(map.get("id")));
				List<String> arrayList = new ArrayList<>();
				Map<String, String> stringMap = new HashMap<>();
				for(ItemInstance itemInstance : getFormInstance.getItems()){
					String value = getValue(stringList, itemInstance);
					if(StringUtils.hasText(value)){
						stringMap.put(itemInstance.getId(), value);
					}
				}
				for(String string : stringList){
					if(StringUtils.hasText(stringMap.get(string))){
						arrayList.add(stringMap.get(string));
					}
				}
				values.add(String.join(",", arrayList));
			}
			dataModelInstance.setValue(idValues);
			dataModelInstance.setDisplayValue(String.join(",", values));
			referenceDataModelList.add(dataModelInstance);
		}
	}

	private Map<String, Boolean> getReferenceMap(ReferenceItemModelEntity fromItem, FormModelEntity toModelEntity){
		Map<String, Boolean> map = new HashMap<>();
		//关联字段
		String key = "";
		boolean flag = false;
		if (fromItem.getSelectMode() == SelectMode.Single && (fromItem.getReferenceType() == ReferenceType.ManyToOne
				|| fromItem.getReferenceType() == ReferenceType.OneToOne)) {
			if(fromItem.getColumnModel() == null){
				return null;
			}
			key = fromItem.getColumnModel().getColumnName();
			flag = true;
		}else if (fromItem.getSelectMode() == SelectMode.Inverse && (fromItem.getReferenceType() == ReferenceType.ManyToOne
				|| fromItem.getReferenceType() == ReferenceType.OneToOne)) {
			ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemModelManager.get(fromItem.getReferenceItemId());
			if(referenceItemModelEntity1.getColumnModel() == null){
				return null;
			}
			key = referenceItemModelEntity1.getColumnModel().getColumnName()+"_list";
			if(fromItem.getReferenceType() == ReferenceType.OneToOne) {
				flag = true;
			}
		}else if(fromItem.getSelectMode() == SelectMode.Multiple){
			key = toModelEntity.getDataModels().get(0).getTableName()+"_list";
		}
		map.put(key,flag);
		return map;
	}

	private ReferenceDataInstance createDataModelInstance(ReferenceItemModelEntity fromItem, FormModelEntity toModelEntity, String id, List<String> stringList, boolean referenceFlag){
		ReferenceDataInstance dataModelInstance = new ReferenceDataInstance();
		dataModelInstance.setValue(id);
		Map<String, Object> map = getDataInfo(toModelEntity.getDataModels().get(0), id);
		FormDataSaveInstance formDataSaveInstance = wrapFormDataEntity(fromItem.getReferenceList(), map, id, referenceFlag);

		List<String> valueList = new ArrayList<>();
		Map<String, ItemInstance> valueMap = new HashMap<>();
		for(ItemInstance itemInstance : formDataSaveInstance.getItems()){
			valueMap.put(itemInstance.getId(), itemInstance);
		}
		for(String string : stringList){
			if(valueMap.get(string) != null) {
				valueList.add(getValue(stringList, valueMap.get(string)));
			}
		}
		dataModelInstance.setDisplayValue(String.join(",", valueList));

		return dataModelInstance;
	}

	private String getValue(List<String> stringList ,ItemInstance itemInstance){
		if(!stringList.contains(itemInstance.getId())){
			return null;
		}
		if(itemInstance.getDisplayValue() != null && itemInstance.getDisplayValue() != ""){
			if(itemInstance.getDisplayValue() instanceof  List){
				List<String> stringList1 = new ArrayList<>();
				if(itemInstance.getType() == ItemType.Media || itemInstance.getType() == ItemType.Attachment){
					List<FileItemModel> fileItemModels = (List<FileItemModel>)itemInstance.getDisplayValue();
					stringList1 = fileItemModels.parallelStream().map(FileItemModel::getName).collect(Collectors.toList());
				}else {
					stringList1 = (List<String>)itemInstance.getDisplayValue();
				}
				return String.join(",", stringList1);
			}else{
				if(itemInstance.getType() == ItemType.Media || itemInstance.getType() == ItemType.Attachment){
					FileItemModel fileItemModel = (FileItemModel)itemInstance.getDisplayValue();
					return fileItemModel.getName();
				}
				return String.valueOf(itemInstance.getDisplayValue());
			}
		}
		return null;
	}

	private void setReferenceAttribute(ReferenceItemModelEntity fromItem,FormModelEntity toModelEntity, ColumnModelEntity columnModelEntity,
									   Map<String, Object> entity, List<DataModelInstance> referenceDataModelList){
		if(fromItem.getParentItem() == null || fromItem.getParentItem().getColumnModel() == null){
			return;
		}
		String key = fromItem.getParentItem().getColumnModel().getColumnName()+"_list";
		if(fromItem.getReferenceType() == ReferenceType.ManyToOne){
			List<Map<String, Object>> listMap = (List<Map<String, Object>>)entity.get(key);
			if( listMap == null || listMap.size() == 0) {
				return;
			}
			DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, entity, listMap);
			dataModelInstance.setReferenceType(fromItem.getReferenceType());
			referenceDataModelList.add(dataModelInstance);
		}else{
			Map<String, Object> mapData = (Map<String, Object>)entity.get(key);
			if( mapData == null || mapData.size() == 0) {
				return;
			}
			DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, fromItem, columnModelEntity, mapData);
			referenceDataModelList.add(dataModelInstance);
		}
	}

	private void setFormDataReferenceAttribute(ReferenceItemModelEntity fromItem,FormModelEntity toModelEntity, ColumnModelEntity columnModelEntity,
									   Map<String, Object> entity, List<ReferenceDataInstance> referenceDataModelList){
		if(fromItem.getReferenceItemId() == null){
			return;
		}
		ItemModelEntity itemModelEntity = itemModelManager.find(fromItem.getReferenceItemId());
		if(itemModelEntity == null || itemModelEntity.getColumnModel() == null){
			return;
		}
		String columnName = itemModelEntity.getColumnModel().getColumnName();
		if(fromItem.getReferenceType() == ReferenceType.ManyToOne){
			String key = columnName;
			List<Map<String, Object>> listMap = (List<Map<String, Object>>)entity.get(key);
			if( listMap == null || listMap.size() == 0) {
				return;
			}
			ReferenceDataInstance dataModelInstance = new ReferenceDataInstance();
			String formId = fromItem.getReferenceFormId();
			if(StringUtils.hasText(formId)) {
				FormModelEntity formModelEntity = formModelService.find(formId);
				dataModelInstance.setReferenceTable(formModelEntity == null ? null :formModelEntity.getDataModels().get(0).getTableName());
			}
			dataModelInstance.setId(fromItem.getId());
			List<String> displayValues = new ArrayList<>();
			List<Object> values = new ArrayList<>();

			for(Map<String, Object> map : listMap){
				ItemInstance itemInstance = new ItemInstance();
				updateValue(fromItem, itemInstance, map.get(columnName));
				ReferenceDataInstance referenceDataInstance = new ReferenceDataInstance();
				setDisplayVlaue(itemInstance,  itemModelEntity,  referenceDataInstance);
				displayValues.add(String.valueOf(referenceDataInstance.getDisplayValue()));
				values.add(itemInstance.getValue());
			}
			dataModelInstance.setValue(values);
			dataModelInstance.setDisplayValue(String.join(",",displayValues));
			referenceDataModelList.add(dataModelInstance);
		}else if(fromItem.getReferenceType() == ReferenceType.OneToOne){
			String key = columnName;
			Map<String, Object> mapData = (Map<String, Object>)entity.get(key);
			if( mapData == null || mapData.size() == 0) {
				return;
			}
			ItemInstance itemInstance = new ItemInstance();
			updateValue(fromItem, itemInstance, mapData.get(columnName));
			ReferenceDataInstance dataModelInstance = new ReferenceDataInstance();
			String formId = fromItem.getReferenceFormId();
			if(StringUtils.hasText(formId)) {
				FormModelEntity formModelEntity = formModelService.find(formId);
				dataModelInstance.setReferenceTable(formModelEntity == null ? null :formModelEntity.getDataModels().get(0).getTableName());
			}
			dataModelInstance.setId(fromItem.getId());
			dataModelInstance.setValue(itemInstance.getValue());
			if(itemInstance.getDisplayValue() != null){
				setDisplayVlaue( itemInstance,  itemModelEntity,  dataModelInstance);
			}
			referenceDataModelList.add(dataModelInstance);
		}
	}

	private void setDisplayVlaue(ItemInstance itemInstance, ItemModelEntity itemModelEntity, ReferenceDataInstance dataModelInstance){
		if(itemInstance.getDisplayValue() instanceof List){
			List<String> stringList = new ArrayList<>();
			if(itemModelEntity.getType() == ItemType.RadioGroup ||itemModelEntity.getType() == ItemType.CheckboxGroup || itemModelEntity.getType() == ItemType.Select ) {
				for (Object o : (List) itemInstance.getDisplayValue()) {
					stringList.add(String.valueOf(o));
				}
			}else if(itemModelEntity.getType() == ItemType.Media || itemModelEntity.getType() == ItemType.Attachment){
				for (FileUploadModel o : (List<FileUploadModel>) itemInstance.getDisplayValue()) {
					stringList.add(o.getName());
				}
			}
			dataModelInstance.setDisplayValue(String.join(",",stringList));
		}else{
			dataModelInstance.setDisplayValue(itemInstance.getDisplayValue());
		}
	}

	private ItemModelEntity getRefenrenceItem(ReferenceItemModelEntity fromItem){
		//关联的item
		ItemModelEntity toItemModel = null;
		if(StringUtils.hasText(fromItem.getReferenceItemId())) {
			toItemModel = itemModelManager.find(fromItem.getReferenceItemId());
		}else{
			FormModelEntity toFormModelEntity = formModelService.get(fromItem.getReferenceFormId());
			for(ItemModelEntity itemModelEntity : toFormModelEntity.getItems()){
				if(itemModelEntity.getSystemItemType() == SystemItemType.ID){
					toItemModel = itemModelEntity;
					break;
				}
			}
		}
		return toItemModel;
	}

private DataModelInstance setDataModelInstance(FormModelEntity toModelEntity, ReferenceItemModelEntity fromItem, ColumnModelEntity columnModelEntity, Map<String, Object> listMap){
	FormInstance f = getBaseItemInstance(toModelEntity, listMap);
	DataModelInstance dataModelInstance = new DataModelInstance();
	dataModelInstance.setId(toModelEntity.getDataModels().get(0).getId());
	dataModelInstance.setName(toModelEntity.getDataModels().get(0).getName());
	dataModelInstance.setReferenceTable(toModelEntity.getDataModels().get(0).getTableName());
	dataModelInstance.setReferenceType(fromItem.getReferenceType());
	dataModelInstance.setReferenceValueColumn(columnModelEntity == null ? null : columnModelEntity.getColumnName());
	dataModelInstance.setSize(1);
	DataModelRowInstance dataModelRowInstance = new DataModelRowInstance();
	dataModelRowInstance.setRowNumber(1);
	dataModelRowInstance.setItems(f.getItems());
	dataModelInstance.getItems().add(dataModelRowInstance);
	return dataModelInstance;
}

	private void setSubFormItemInstance(ItemModelEntity itemModel, Map<String, Object> entity, List<SubFormItemInstance> subFormItems, String activityId){
		//TODO 子表数据结构
		SubFormItemModelEntity itemModelEntity = (SubFormItemModelEntity)itemModel;
		//子表
		DataModelEntity subFormDataModel = itemModelEntity.getColumnModel().getDataModel();
		Session subFormSession  = getSession(subFormDataModel);
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
		List<DataModelInstance> referenceDataModelList = new ArrayList<>();
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
					if(columnModelEntity == null && !(item instanceof ReferenceItemModelEntity)){
						continue;
					}
					ItemInstance itemInstance = new ItemInstance();
					itemInstance.setId(item.getId());
					itemInstance.setType(item.getType());
					itemInstance.setSystemItemType(item.getSystemItemType());
					if(item instanceof ReferenceItemModelEntity) {
						if(item.getType() == ItemType.ReferenceLabel){
							itemInstance.setValue(((ReferenceItemModelEntity) item).getReferenceItemId());
						}else if(map.get("id") != null && map.get("id") != "" && columnModelEntity != null) {
							Map<String, Object> newMap = (Map<String, Object>) subFormSession.load(subFormDataModel.getTableName(), String.valueOf(map.get("id")));

                            FormModelEntity toModelEntity = formModelService.find(((ReferenceItemModelEntity) item).getReferenceFormId());
                            if (toModelEntity == null) {
                                continue;
                            }
                            Map<String, Boolean> keyMap = getReferenceMap((ReferenceItemModelEntity) item, toModelEntity);
                            if (keyMap == null) {
                                continue;
                            }
                            String referenceKey =  new ArrayList<>(keyMap.keySet()).get(0);
                            Boolean flag = keyMap.get(referenceKey);

							List<Object> idList = new ArrayList<>();
							if(newMap.get(referenceKey) != null) {
                                if (flag) {
                                    Map<String, Object> referenceMap = (Map<String, Object>) newMap.get(referenceKey);
                                    idList.add(referenceMap.get("id"));
                                } else {
                                    for (Map<String, Object> referenceMap : (List<Map<String, Object>>) newMap.get(referenceKey)) {
                                        idList.add(referenceMap.get("id"));
                                    }
                                }
                            }
							if (keyMap.get(referenceKey) && idList.size() > 0) {
								itemInstance.setValue(idList.get(0));
							} else {
								itemInstance.setValue(idList);
							}
						}
					}else{
						 itemInstance = setItemInstance(columnModelEntity.getKey(), item, map.get(columnModelEntity.getColumnName()), activityId);
					}
					instances.add(itemInstance);
				}
				//这一行没有数据
				if(instances.size() < 1){
					continue;
				}
				//子表主键id
				ColumnModelEntity subFormColumnModelEntity  = itemModel.getColumnModel();
				ItemInstance subFomrItemInstance = setItemInstance(subFormColumnModelEntity.getKey(), itemModel, map.get("id"), activityId);
				instances.add(subFomrItemInstance);

				subFormRowItemInstance.setItems(instances);
				subFormRowItemInstanceList.add(subFormRowItemInstance);
			}
			subFormDataItemInstance.setRowNumber(row ++);
			subFormDataItemInstance.setItems(subFormRowItemInstanceList);
			subFormItemInstances.add(subFormDataItemInstance);
		}
		subFormItems.add(subFormItemInstance);
	}

	private DataModelInstance setDataModelInstance(FormModelEntity toModelEntity, Map<String, Object> entity, List<Map<String, Object>> listMap){

		List<Map<String, Object>> listData = new ArrayList<>(listMap);
		//int pageIndex = 1;
		//int pageSize = 10;
		List<Map<String, Object>> referenceListData = listData;//listData.subList((pageIndex-1) * pageSize, pageIndex * pageSize -1 < listData.size()  ? pageIndex * pageSize : listData.size());

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
		if(entity != null && list != null) {
			for (ItemModelEntity itemModel : list) {
				System.out.println(itemModel.getId() + "____begin");
				ColumnModelEntity column = itemModel.getColumnModel();
				if (column == null) {
					continue;
				}
				Object value = entity.get(column.getColumnName());
				ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
				items.add(itemInstance);
				formInstance.addData(itemModel.getColumnModel().getId(), itemInstance.getValue());
				System.out.println(itemModel.getId() + "____end");
			}
		}
		formInstance.getItems().addAll(items);
		return formInstance;
	}


	private ItemInstance setItemInstance(Boolean visiblekey , ItemModelEntity itemModel, Object value, String activityId) {
		ItemInstance itemInstance = new ItemInstance();
		itemInstance.setId(itemModel.getId());
		itemInstance.setColumnModelId(itemModel.getColumnModel().getId());
		itemInstance.setType(itemModel.getType());
		itemInstance.setSystemItemType(itemModel.getSystemItemType());
//		itemInstance.setProps(itemModel.getProps());
		itemInstance.setItemName(itemModel.getName());
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
			case Select:
				setSelectItemValue(itemModel, itemInstance, value);
				break;
			case RadioGroup:
				setSelectItemValue(itemModel, itemInstance, value);
				break;
			case CheckboxGroup:
				setSelectItemValue(itemModel, itemInstance, value);
				break;
			case Treeselect:

				String valueStrs = value == null || StringUtils.isEmpty(value) ?  null : String.valueOf(value);
				String[] strings = valueStrs == null ? new String[]{} : valueStrs.split(",");
				List<String> ids  = Arrays.asList(strings);
				if(((TreeSelectItemModelEntity)itemModel).getMultiple() != null && ((TreeSelectItemModelEntity)itemModel).getMultiple()){
					itemInstance.setValue(ids);
				}else {
					itemInstance.setValue(valueStrs);
				}

                if(valueStrs.length() > 0) {
                    List<TreeSelectData> list = groupService.getTreeSelectDataSourceByIds(((TreeSelectItemModelEntity) itemModel).getDataSource().getValue(), valueStrs.split(","));
                    if(list != null && list.size() > 0) {
						List<String> values = list.parallelStream().map(TreeSelectData::getName).collect(Collectors.toList());
						itemInstance.setDisplayValue(values);
//                        if(((TreeSelectItemModelEntity)itemModel).getMultiple() != null && ((TreeSelectItemModelEntity)itemModel).getMultiple()){
//                        	List<String> values = list.parallelStream().map(TreeSelectData::getName).collect(Collectors.toList());
//                            itemInstance.setDisplayValue(String.join(",", values));
//                        }else{
//                            itemInstance.setDisplayValue(list.get(0).getName());
//                        }
                    }

                }


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
				if(itemModel.getSystemItemType() == SystemItemType.Creator && value != null && StringUtils.hasText((String)value)){
					User user = userService.getUserInfo(String.valueOf(value));
					itemInstance.setDisplayValue(user == null ? null : user.getUsername());
				}else if(itemModel.getType() == ItemType.InputNumber){
					try {
						NumberItemModelEntity numberItemModelEntity = (NumberItemModelEntity)itemModel;
						DecimalFormat df = new DecimalFormat(getNumberFormat(numberItemModelEntity));
						itemInstance.setDisplayValue(df.format(value) + (numberItemModelEntity.getSuffixUnit() == null ? "" : numberItemModelEntity.getSuffixUnit()));
					} catch (Exception e) {
						e.printStackTrace();
						itemInstance.setDisplayValue(valueStr);
					}
				}else if(itemModel.getSystemItemType() == SystemItemType.CreateDate || itemModel.getType() == ItemType.DatePicker){
					Date date = (Date) value;
					itemInstance.setValue(date);
					itemInstance.setDisplayValue(DateFormatUtils.format(date,((TimeItemModelEntity)itemModel).getTimeFormat() == null ? "yyyy-MM-dd HH:mm:ss" : ((TimeItemModelEntity)itemModel).getTimeFormat()));
				}else {
					itemInstance.setDisplayValue(valueStr);
				}

				break;
		}
	}

	private String getNumberFormat(NumberItemModelEntity numberItemModelEntity){
		StringBuffer stringBuffer = new StringBuffer();
		if(numberItemModelEntity.getThousandSeparator() != null && numberItemModelEntity.getThousandSeparator()) {
			if(numberItemModelEntity.getDecimalDigits() != null && numberItemModelEntity.getDecimalDigits() > 0){
				stringBuffer.append(",###,##0.	");
				for(int i = 0 ; i < numberItemModelEntity.getDecimalDigits(); i++){
					stringBuffer.append("0");
				}
			}else {
				stringBuffer.append(",###,##0");
			}
		}else {
			if (numberItemModelEntity.getDecimalDigits() != null && numberItemModelEntity.getDecimalDigits() > 0) {
				stringBuffer.append("#.	");
				for (int i = 0; i < numberItemModelEntity.getDecimalDigits(); i++) {
					stringBuffer.append("0");
				}
			} else {
				stringBuffer.append("#0");
			}
		}
		return stringBuffer.toString();
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
//		if((	selectItemModelEntity.getSelectReferenceType() == SelectReferenceType.Dictionary ||
//				(selectItemModelEntity.getReferenceDictionaryId() != null && selectItemModelEntity.getReferenceDictionaryItemId() != null) ||
//				(selectItemModelEntity.getReferenceDictionaryId() != null && checkParentSelectItemHasDictionaryItem(selectItemModelEntity))
//		) && list != null && list.size() > 0) {
//			List<DictionaryItemEntity> dictionaryItemEntities = dictionaryItemManager.query().filterIn("id", list).list();
//			if (dictionaryItemEntities != null) {
//				for (DictionaryItemEntity dictionaryItemEntity : dictionaryItemEntities) {
//					displayValuelist.add(dictionaryItemEntity.getName());
//				}
//			}
//		}
		if((selectItemModelEntity.getSelectReferenceType() == SelectReferenceType.Dictionary ||
			selectItemModelEntity.getReferenceDictionaryItemId() != null ||
			checkParentSelectItemHasDictionaryItem(selectItemModelEntity)
			) && list != null && list.size() > 0){
			List<DictionaryItemEntity> dictionaryItemEntities = dictionaryItemManager.query().filterIn("id",list).list();
			if(dictionaryItemEntities != null) {
				for (DictionaryItemEntity dictionaryItemEntity : dictionaryItemEntities) {
					displayValuelist.add(dictionaryItemEntity.getName());
				}
			}
			itemInstance.setDisplayValue(displayValuelist);
			// displayValuelist为空，说明在字典表里面已经删掉该内容，因此value也要设为空
			if (displayValuelist==null || displayValuelist.size()==0) {
				itemInstance.setValue(new ArrayList<>());
			}
		}else if(itemModel.getOptions() != null && itemModel.getOptions().size() > 0) {
			for (ItemSelectOption option : itemModel.getOptions()) {
				if (list.contains(option.getId())) {
					displayValuelist.add(option.getLabel());
				}
			}
			itemInstance.setDisplayValue(displayValuelist);
		}else {
			displayValuelist.add(valueString);
			itemInstance.setDisplayValue(displayValuelist);
		}
	}

	/**
	 * 当SelectItemModel是联动的时候，遍历递归查询获取上级存在getReferenceDictionaryId和referenceDictionaryItemId的item，直到找到为止，否则返回nul
	 * @param selectItemModelEntity
	 * @return
	 */
	public Boolean checkParentSelectItemHasDictionaryItem(SelectItemModelEntity selectItemModelEntity) {
		if (selectItemModelEntity==null) {
			return false;
		} else {
			if (selectItemModelEntity.getParentItem()!=null) {
				SelectItemModelEntity parentItem = selectItemModelEntity.getParentItem();
				if (parentItem.getReferenceDictionaryId()!=null && parentItem.getReferenceDictionaryItemId()!=null) {
					return true;
				} else {
					return checkParentSelectItemHasDictionaryItem(parentItem);
				}
			} else {
				return false;
			}
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

	public List<User> getUserInfoByIds(List<String> ids) {
		if (ids!=null && ids.size()>0) {
			return userService.queryUserInfoByIds(ids);
		} else {
			return new ArrayList<>();
		}
	}
}
