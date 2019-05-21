package tech.ascs.icity.iform.service.impl;

import java.lang.reflect.Field;
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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tech.ascs.icity.ICityException;
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

	private static final Random random = new Random();

	@Autowired
	private DictionaryDataService dictionaryService;

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

    @Autowired
    private ListModelService listModelService;

	@Autowired
	private DictionaryModelService dictionaryModelService;

	private JPAManager<FormModelEntity> formModelEntityJPAManager;

	private JPAManager<DictionaryDataEntity> dictionaryEntityJPAManager;

	private JPAManager<DictionaryDataItemEntity> dictionaryItemManager;

	private JPAManager<ItemModelEntity> itemModelManager;

	private JPAManager<DataModelEntity> dataModelManager;

	private JPAManager<FileUploadEntity> fileUploadManager;

	private JPAManager<GeographicalMapEntity> mapEntityJPAManager;

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

	@Autowired
	OKHttpLogService okHttpLogService;

	public	FormInstanceServiceExImpl() {
		super(FormModelEntity.class);
	}
	@Override
	protected void initManager() {
		super.initManager();
		formModelEntityJPAManager = getJPAManagerFactory().getJPAManager(FormModelEntity.class);
		dictionaryEntityJPAManager = getJPAManagerFactory().getJPAManager(DictionaryDataEntity.class);
		dictionaryItemManager = getJPAManagerFactory().getJPAManager(DictionaryDataItemEntity.class);
		itemModelManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
		dataModelManager = getJPAManagerFactory().getJPAManager(DataModelEntity.class);
		fileUploadManager = getJPAManagerFactory().getJPAManager(FileUploadEntity.class);
		mapEntityJPAManager = getJPAManagerFactory().getJPAManager(GeographicalMapEntity.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormInstance> listInstance(ListModelEntity listModel, Map<String, Object> queryParameters) {
		Session session = getSession(listModel.getMasterForm().getDataModels().get(0));
		List<FormInstance> list = new ArrayList<>();
		try {
			Criteria criteria = generateCriteria(session, listModel.getMasterForm(), null, queryParameters);
			addSort(listModel, criteria);
			list = wrapList(listModel, criteria.list());
		} catch (Exception e) {
			e.printStackTrace();
			new ICityException(e.getLocalizedMessage(), e);
		} finally {
			if (session!=null) {
				session.close();
				session = null;
			}
		}
		return list;
	}

	@Override
	public List<FormDataSaveInstance> formInstance(FormModelEntity formModel, Map<String, Object> queryParameters) {
		Session session = getSession(formModel.getDataModels().get(0));
		List<FormDataSaveInstance> list = new ArrayList<>();
		try {
			Criteria criteria = generateCriteria(session, formModel, null, queryParameters);
			list = wrapFormDataList(formModel, null, criteria.list());
		} catch (Exception e) {
			e.printStackTrace();
			new ICityException(e.getLocalizedMessage(), e);
		} finally {
			if (session!=null) {
				session.close();
				session = null;
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<FormDataSaveInstance> pageFormInstance(FormModelEntity formModel, int page, int pagesize, Map<String, Object> queryParameters) {
		Page<FormDataSaveInstance> result = Page.get(page, pagesize);
		Session session = getSession(formModel.getDataModels().get(0));
		try {
			Criteria criteria = generateCriteria(session, formModel, null, queryParameters);
			criteria.setFirstResult((page - 1) * pagesize);
			criteria.setMaxResults(pagesize);
			List<FormDataSaveInstance> list = wrapFormDataList(formModel, null, criteria.list());

			criteria.setFirstResult(0);
			criteria.setProjection(Projections.rowCount());
			Number count = (Number) criteria.uniqueResult();
			result.data(count.intValue(), list);
		} catch (Exception e) {
			e.printStackTrace();
			new ICityException(e.getLocalizedMessage(), e);
		} finally {
			if (session != null) {
				session.close();
				session = null;
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<FormDataSaveInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, Object> queryParameters) {
		Page<FormDataSaveInstance> result = Page.get(page, pagesize);
		Session session = getSession(listModel.getMasterForm().getDataModels().get(0));
		try {
			Criteria criteria = generateCriteria(session, listModel.getMasterForm(), listModel, queryParameters);
			addCreatorCriteria(criteria, listModel);
			addSort(listModel, criteria);

			criteria.setFirstResult((page - 1) * pagesize);
			criteria.setMaxResults(pagesize);

			List data = criteria.list();
			List<FormDataSaveInstance> list = wrapFormDataList(null, listModel, data);

			criteria.setFirstResult(0);
			criteria.setProjection(Projections.rowCount());
			Number count = (Number) criteria.uniqueResult();

			result.data(count.intValue(), list);
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ICityException){
				throw e;
			}
			throw new IFormException(e.getLocalizedMessage(), e);
		} finally {
			if (session!=null) {
				session.close();
				session = null;
			}
		}
		return result;
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

		setFormInstanceModel( formInstance,  formModel, new HashMap<>(), true);
		return formInstance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FormInstance getFormInstance(FormModelEntity formModel, String instanceId) {

		FormInstance formInstance = null;
		try {
			DataModelEntity dataModel = formModel.getDataModels().get(0);
			Map<String, Object> map =  getDataInfo(dataModel, instanceId);
			if(map == null || map.keySet() == null){
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
			}
			formInstance = wrapEntity(formModel, map, instanceId, true);
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ICityException){
				throw e;
			}
			throw new IFormException("没有查询到【" + formModel.getName() + "】表单，instanceId【"+instanceId+"】的数据");
		}
		return formInstance;
	}

	@Override
	public FormDataSaveInstance getQrCodeFormDataSaveInstance(ListModelEntity listModel, String instanceId) {
		FormDataSaveInstance formInstance = null;
		try {
			DataModelEntity dataModel = listModel.getMasterForm().getDataModels().get(0);
			Map<String, Object> map =  getDataInfo(dataModel, instanceId);
			if(map == null || map.keySet() == null){
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
			}
			formInstance = wrapQrCodeFormDataEntity(true, listModel, map, instanceId, true);
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ICityException){
				throw e;
			}
			throw new IFormException("没有查询到【" + listModel.getMasterForm().getName() + "】表单，instanceId【"+instanceId+"】的数据");
		}
		return formInstance;
	}

	private Map<String, Object> getDataInfo(DataModelEntity dataModel, String instanceId){
		Session session = null;
		Map<String, Object> map = new HashMap<>();
		try {
			session = getSession(dataModel);
			if(!StringUtils.hasText(instanceId)){
				return map;
			}
			map = (Map<String, Object>) session.get(dataModel.getTableName(), instanceId);
			if(map == null || map.keySet() == null || map.keySet().size() < 1){
				map = new HashMap<>();
				//throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if(session != null){
				session.close();
				session = null;
			}
		}
		return map;
	}

	private Map<String, Object> createDataModel(DataModelEntity dataModel, String instanceId){
		Session session = getSession(dataModel);
		Map<String, Object> map = null;
		try {
			map = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
			if(map == null || map.keySet() == null) {
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
			}

			for(String key : map.keySet()){
				map.put(dataModel.getTableName() + "_" + key, map.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session!=null) {
				session.close();
				session = null;
			}
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
			user = CurrentUserUtils.getCurrentUser();
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
			setItemInstance(itemModelEntity, itemMap, list, user);
		}

		return saveFormData(formModel.getDataModels().get(0), formInstance, user, formModelEntity, formModel);
	}

	private void setItemInstance(ItemModelEntity itemModelEntity, Map<String, ItemInstance> itemMap, List<ItemInstance> list, UserInfo user){
		if(itemModelEntity.getSystemItemType() == SystemItemType.CreateDate && ((TimeItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Create){
			if(itemMap.keySet().contains(itemModelEntity.getId())){
				list.remove(itemMap.get(itemModelEntity.getId()));
			}

			list.add(getItemInstance(itemModelEntity.getId(), getNowTime(((TimeItemModelEntity) itemModelEntity).getTimeFormat())));

		}else if(itemModelEntity.getSystemItemType() == SystemItemType.Creator && ((ReferenceItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Create){
			if(itemMap.keySet().contains(itemModelEntity.getId())){
				list.remove(itemMap.get(itemModelEntity.getId()));
			}
			list.add(getItemInstance(itemModelEntity.getId(), user != null ? user.getId() : null));
		}else if(itemModelEntity.getSystemItemType() == SystemItemType.SerialNumber){
			if(itemMap.keySet().contains(itemModelEntity.getId())){
				list.remove(itemMap.get(itemModelEntity.getId()));
			}
			String format = ((SerialNumberItemModelEntity)itemModelEntity).getTimeFormat();
			String prefix = ((SerialNumberItemModelEntity)itemModelEntity).getPrefix();
			StringBuffer str = new StringBuffer(!StringUtils.hasText(prefix) ? "" : prefix);
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

	private String saveFormData(DataModelEntity dataModel, FormDataSaveInstance formInstance, UserInfo user, FormModelEntity formModelEntity, FormModelEntity formModel){
		Session session = null;
		String newId = null;
		try {
			session = getSession(dataModel);
			session.beginTransaction();
			Map<String, Object> data = new HashMap<String, Object>();

			//主表数据
			setMasterFormItemInstances(formInstance, data, DisplayTimingType.Add);
			data.put("create_at", new Date());
			data.put("create_by",  user != null ? user.getId() : null);
			//流程参数
			data.put("PROCESS_ID", formInstance.getProcessId());
			data.put("PROCESS_INSTANCE", formInstance.getProcessInstanceId());
			data.put("ACTIVITY_ID", formInstance.getActivityId());
			data.put("ACTIVITY_INSTANCE", formInstance.getActivityInstanceId());

			//设置子表数据
			setSubFormReferenceData(session, user, formInstance, data, DisplayTimingType.Add);
			//关联表数据
			saveReferenceData( user,formInstance, data,  session,  formModelEntity.getDataModels().get(0).getTableName(), formModelService.findAllItems(formModelEntity), DisplayTimingType.Add);

			// before
			sendWebService(formModelEntity, BusinessTriggerType.Add_Before, data, formInstance.getId());

			newId = (String) session.save(dataModel.getTableName(), data);

			// 启动流程
			if (formModel.getProcess() != null && formModel.getProcess().getKey() != null) {
				//跳过第一个流程环节
				data.put("PASS_THROW_FIRST_USERTASK", true);
				System.out.println("传给工作流的数据=====>>>>>"+data);
				String processInstanceId = processInstanceService.startProcess(formModel.getProcess().getKey(), newId, data);
				updateProcessInfo(formModel, data, processInstanceId);
			}

			session.getTransaction().commit();
			data.put("id", newId);
			// after
			sendWebService( formModelEntity, BusinessTriggerType.Add_After, data, newId);

		} catch (Exception e) {
			throw e;
		} finally {
			if(session != null){
				session.close();
				session = null;
			}
		}
		return newId;
	}

	private void sendWebService(FormModelEntity formModelEntity, BusinessTriggerType triggerType,  Map<String, Object> data, String id){
		BusinessTriggerEntity triggerEntity = getBusinessTrigger(formModelEntity, triggerType);
		if (triggerEntity!=null) {
            if (triggerEntity.getParamCondition() == ParamCondition.FormCurrentData) {
                okHttpLogService.sendOKHttpRequest(triggerEntity, formModelEntity, data);
            } else {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("formId", formModelEntity.getId());
                if (StringUtils.hasText(id)) {
                    dataMap.put("id", id);
                }
                okHttpLogService.sendOKHttpRequest(triggerEntity, formModelEntity, dataMap);
            }
        }
	}

	private BusinessTriggerEntity getBusinessTrigger(FormModelEntity formModelEntity, BusinessTriggerType triggerType){
		if(formModelEntity.getTriggeres() != null){
			for(BusinessTriggerEntity triggerEntity : formModelEntity.getTriggeres()){
				if(triggerEntity.getType() == triggerType){
					return triggerEntity;
				}
			}
		}
		return null;
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
			user = CurrentUserUtils.getCurrentUser();
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
			getNewItemInstance(itemModelEntity, list, user, itemMap);
		}
		saveFormData(formModel.getDataModels().get(0), formInstance, user, formModelEntity, formModel, instanceId);
	}

	private void getNewItemInstance(ItemModelEntity itemModelEntity, List<ItemInstance> list, UserInfo user, Map<String, ItemInstance> itemMap){
		if(itemModelEntity.getSystemItemType() == SystemItemType.CreateDate && ((TimeItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Update){
			if(itemMap.keySet().contains(itemModelEntity.getId())){
				list.remove(itemMap.get(itemModelEntity.getId()));
			}
			list.add(getItemInstance(itemModelEntity.getId(), getNowTime(((TimeItemModelEntity) itemModelEntity).getTimeFormat())));
		}else if(itemModelEntity.getSystemItemType() == SystemItemType.Creator && ((ReferenceItemModelEntity)itemModelEntity).getCreateType() == SystemCreateType.Update){
			if(itemMap.keySet().contains(itemModelEntity.getId())){
				list.remove(itemMap.get(itemModelEntity.getId()));
			}
			list.add(getItemInstance(itemModelEntity.getId(), user != null ? user.getId() : null));
		}
	}

	private void saveFormData(DataModelEntity dataModel, FormDataSaveInstance formInstance, UserInfo user, FormModelEntity formModelEntity, FormModelEntity formModel, String instanceId){
		Session session = null;
		try {
			session = getSession(dataModel);
			//开启事务
			session.beginTransaction();

			Map<String, Object> data = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);

			//主表数据
			setMasterFormItemInstances(formInstance, data, DisplayTimingType.Update);
			data.put("update_at", new Date());
			data.put("update_by",  user != null ? user.getId() : null);

			setSubFormReferenceData(session, user, formInstance, data, DisplayTimingType.Update);
			//关联表数据
			saveReferenceData(user, formInstance, data,  session,  formModelEntity.getDataModels().get(0).getTableName(), formModelService.findAllItems(formModelEntity), DisplayTimingType.Update);

			// before
			sendWebService( formModelEntity, BusinessTriggerType.Update_Before, data, instanceId);

			// 流程操作
			if (formInstance.getActivityInstanceId() != null) {
				taskService.completeTask(formInstance.getActivityInstanceId(), data);
				updateProcessInfo(formModel, data, formInstance.getProcessInstanceId());
			}
			System.out.println("___"+data.get("event_nature"));
			session.update(dataModel.getTableName(), data);
			session.getTransaction().commit();

			// after
			sendWebService( formModelEntity, BusinessTriggerType.Update_After, data, instanceId);
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ICityException){
				throw e;
			}
			throw new IFormException("保存【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据失败");
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
		List<DataModelEntity> slaverModels = masterFormModelEntity.getDataModels().get(0).getSlaverModels();
		for(DataModelEntity slaverDataModelEntity : slaverModels){
			slaverModelsList.add(slaverDataModelEntity.getTableName());
		}


		List<NewDataList> newDataList = new ArrayList<>();
		//TODO 子表数据
		if(formInstance.getSubFormData() != null &&formInstance.getSubFormData().size() > 0) {
			for (SubFormItemInstance subFormItemInstance : formInstance.getSubFormData()) {
				setSubFormItemInstance(subFormItemInstance, slaverModelsList, session, user, data, newDataList,
						 displayTimingType, formInstance);
			}
		}
		for(NewDataList newDataList1 : newDataList){
			List<Map<String, Object>>  subFormData = new ArrayList<>();
			for (Map<String, Object> map : newDataList1.getDataListMap()) {
				Map<String, Object> subFormMap =(Map<String, Object>) session.load(newDataList1.getTableName(), String.valueOf(map.get("id")));
				subFormMap.put("master_id", data);
				subFormData.add(subFormMap);
			}
			data.put(newDataList1.getKey(), subFormData);
		}
		deleteSalverModelData(slaverModelsList,  session,  data);
	}

	private void setSubFormItemInstance(SubFormItemInstance subFormItemInstance,List<String> slaverModelsList, Session session, UserInfo user, Map<String, Object> data,List<NewDataList> newDataList,
					DisplayTimingType displayTimingType , FormDataSaveInstance formInstance){
		String key = subFormItemInstance.getTableName() + "_list";
		slaverModelsList.remove(subFormItemInstance.getTableName());
		DataModelEntity dataModelEntity = dataModelManager.findUniqueByProperty("tableName", subFormItemInstance.getTableName());
		if(dataModelEntity == null){
			return;
		}
		//子表session
		Session subFormSession = session;
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
							stringListMap.put(itemModel.getId()+"_"+itemModel.getName(), list);
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
					if(StringUtils.hasText(string) && (map.get("id") == null ||  !string.equals(map.get("id")))){
						String[] strings = str.split("_");
						throw new IFormException(strings[strings.length-1]+"必须唯一");
					}
				}
			}
			if(map == null || map.keySet() == null || map.keySet().size() < 1){
				continue;
			}

			if(displayTimingType == DisplayTimingType.Add){
				map.put("create_at", new Date());
				map.put("create_by", user != null ? user.getId() : null );
				map.put("update_at", new Date());
			}else{
				map.put("update_at", new Date());
				map.put("update_by", user != null ? user.getId() : null);
			}
			String id = map.get("id") == null ? null : String.valueOf(map.get("id"));
			if (StringUtils.hasText(id)) {
				idList.add(id);
			}
			//保存子表数据
			FormDataSaveInstance formDataSaveInstance = new FormDataSaveInstance();
			formDataSaveInstance.setFormId(formInstance.getFormId());
			formDataSaveInstance.setReferenceData(referenceData);
			if(referenceItemModelEntityList != null && referenceItemModelEntityList.size() > 0) {
				saveReferenceData(user, formDataSaveInstance, map, subFormSession, dataModelEntity.getTableName(), referenceItemModelEntityList, displayTimingType);
			}
			String newId = id;
			if(id != null && StringUtils.hasText(id)){
				subFormSession.merge(dataModelEntity.getTableName(), map);
			}else{
				newId = (String) subFormSession.save(dataModelEntity.getTableName(), map);
			}
			map.put("id", newId);
			newListMap.add(map);
		}
		NewDataList newDataList1 = new NewDataList();
		newDataList1.setKey(key);
		newDataList1.setTableName(dataModelEntity.getTableName());
		newDataList1.setDataListMap(newListMap);
		newDataList.add(newDataList1);
		//旧的数据
		List<Map<String, Object>> oldListMap = (List<Map<String, Object>>) data.get(key);
		data.put(key, new ArrayList<>());
		deleteSubFormNewMapData("master_id", session, dataModelEntity, oldListMap, idList);
	}

	//清楚子表旧数据
	private void deleteSalverModelData(List<String> slaverModelsList, Session session, Map<String, Object> data){
		for (String str : slaverModelsList) {
			data.put(str + "_list", new ArrayList<>());
			//旧的数据
			List<Map<String, Object>> oldListMap = (List<Map<String, Object>>) data.get(str + "_list");
			if (oldListMap != null && oldListMap.size() > 0) {
				for (Map<String, Object> map : oldListMap) {
					deleteData(session, str, String.valueOf(map.get("id")), "master_id");
				}
			}
		}
	}

	private void saveReferenceData(UserInfo user, FormDataSaveInstance formInstance,  Map<String, Object> data, Session session, String tableName, List<ItemModelEntity> itemModelEntityList, DisplayTimingType displayTimingType){
		//主表表单
		FormModelEntity masterFormModelEntity = formModelService.get(formInstance.getFormId());
		//关联表
		Map<String, ReferenceDataModel> referenceMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity : itemModelEntityList){
			setReferenceDataModel(itemModelEntity, referenceMap);
		}

		if(formInstance.getReferenceData() != null && formInstance.getReferenceData().size() > 0) {
			for (ReferenceDataInstance dataModelInstance : formInstance.getReferenceData()) {
				setReferenceDataInstance(dataModelInstance, masterFormModelEntity, referenceMap, data, user, formInstance, session, tableName,  displayTimingType);
			}
		}
		for(String str: referenceMap.keySet()){
			//旧的数据
			Object oldData = data.get(str);
			if(oldData != null) {
				ReferenceDataModel dataModel = referenceMap.get(str);
				if (dataModel.getFlag()) {
					data.put(str, null);
					//deleteData(session, dataModel.getTableName(), String.valueOf(((Map<String, Object>)oldData).get("id")), str);
				} else {
					List<Map<String, Object>> oldListMap = (List<Map<String, Object>>) oldData;
					data.put(str, new ArrayList<>());
					for(Map<String, Object> map : oldListMap){
						//deleteData(session, dataModel.getTableName(), String.valueOf(map.get("id")), str);
					}
				}
			}
		}
	}

	private void setReferenceDataInstance(ReferenceDataInstance dataModelInstance, FormModelEntity masterFormModelEntity, Map<String, ReferenceDataModel> referenceMap, Map<String, Object> data,
					UserInfo user, FormDataSaveInstance formInstance, Session session, String tableName, DisplayTimingType displayTimingType){
		if (dataModelInstance.getValue() == null || (dataModelInstance.getValue() instanceof String && StringUtils.isEmpty(dataModelInstance.getValue()))) {
			return;
		}

		ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity) itemModelManager.get(dataModelInstance.getId());

		if(referenceItemModelEntity.getSystemItemType() == SystemItemType.Creator){
			return;
		}

		FormModelEntity formModelEntity = formModelService.get(referenceItemModelEntity.getReferenceFormId());
		DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);

		//主表关联的key
		Map<String ,Boolean> referenceKeyMap = getReferenceMap(referenceItemModelEntity, formModelEntity);
		String referenceKey = new ArrayList<>(referenceKeyMap.keySet()).get(0);
		boolean flag = referenceKeyMap.get(referenceKey);

		//被关联表的key
		Map<String, Boolean> toReferenceKeyMap = new HashMap<>();
		String toReferenceKey = null;
		boolean toFlag = false;
		//方向
		if(referenceItemModelEntity.getSelectMode() == SelectMode.Inverse) {
			ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemModelManager.get(referenceItemModelEntity.getReferenceItemId());
			toReferenceKeyMap = getReferenceMap(referenceItemModelEntity1, masterFormModelEntity);
			if(toReferenceKeyMap == null){
				return;
			}
			toReferenceKey = new ArrayList<>(toReferenceKeyMap.keySet()).get(0);;
			toFlag = toReferenceKeyMap.get(toReferenceKey);
		}else{
			if (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne
					|| referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne) {
				toReferenceKey = referenceItemModelEntity.getColumnModel().getDataModel().getTableName()+"_"+referenceItemModelEntity.getColumnModel().getColumnName() + "_list";
				if(referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne){
					toFlag = true;
				}
			} else{
				toReferenceKey = masterFormModelEntity.getDataModels().get(0).getTableName() + "_list";
			}
			toReferenceKeyMap.put(toReferenceKey, toFlag);
		}

		referenceMap.remove(referenceKey);
		if (!StringUtils.hasText(referenceKey)) {
			return;
		}

		List<Map<String, Object>> oldListMap = new ArrayList<>();
		//旧的数据
		Object oldData = data.get(referenceKey);
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

		//新的数据
		List<Map<String, Object>> saveListMap = getNewMapData(user, toFlag, toReferenceKey, session, data, dataModelEntity, oldListMap, idList, newListMap, displayTimingType);

		if (referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne && referenceItemModelEntity.getColumnModel() != null && saveListMap != null && saveListMap.size() > 0) {
			String idValue = String.valueOf(saveListMap.get(0).get("id"));
			List<String> list = listByTableName(referenceItemModelEntity.getType(), tableName, referenceItemModelEntity.getColumnModel().getColumnName(), idValue);

			for (String str : list) {
				if (StringUtils.hasText(str) && (formInstance.getId() == null || !str.equals(formInstance.getId()))) {
					throw new IFormException("同一数据不能重复关联" + referenceItemModelEntity.getName());
				}
			}
		}

		if (saveListMap.size() > 0 && formInstance.getFormId().equals(referenceItemModelEntity.getReferenceFormId())) {
			for (Map<String, Object> map : saveListMap) {
				map.remove(toReferenceKey);
			}
		}
		if (flag) {
			if(saveListMap.size() > 0) {
				data.put(referenceKey, new ArrayList<>(saveListMap).get(0));
			}else{
				data.put(referenceKey, null);
			}
		} else {
			data.put(referenceKey, saveListMap);
		}
	}

	private void setReferenceDataModel(ItemModelEntity itemModelEntity, Map<String, ReferenceDataModel> referenceMap){
		if(itemModelEntity instanceof ReferenceItemModelEntity && itemModelEntity.getType() != ItemType.ReferenceLabel) {
			ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity)itemModelEntity;
			//创建者更新者不走关联
			if(referenceItemModelEntity.getSystemItemType() == SystemItemType.Creator){
				return;
			}
			ReferenceDataModel referenceDataModel = new ReferenceDataModel();
			FormModelEntity formModelEntity = formModelService.get(referenceItemModelEntity.getReferenceFormId());
			//主表关联的key
			Map<String ,Boolean> referenceKeyMap = getReferenceMap(referenceItemModelEntity, formModelEntity);
			if(referenceKeyMap == null){
				return;
			}
			String key = new ArrayList<>(referenceKeyMap.keySet()).get(0);
			boolean flag = referenceKeyMap.get(key);
			DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);

			referenceDataModel.setKey(key);
			referenceDataModel.setFlag(flag);
			referenceDataModel.setTableName(dataModelEntity.getTableName());
			referenceMap.put(key, referenceDataModel);
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

	private List<Map<String, Object>> getNewMapData(UserInfo user,boolean flag, String referenceKey, Session session, Map<String,Object> data, DataModelEntity dataModelEntity, List<Map<String, Object>> oldListMap, List<String> idList, List<Map<String, Object>> newListMap, DisplayTimingType displayTimingType){
		List<Map<String, Object>> saveListMap = new ArrayList<>();
		//旧的数据
		if(oldListMap != null && oldListMap.size() > 0) {
			for (Map<String, Object> map : oldListMap) {
				if (map == null || map.get("id") == null || idList.contains(String.valueOf(map.get("id")))) {
					continue;
				}
				Map<String, Object> objectMap = (Map<String, Object>)session.load(dataModelEntity.getTableName(), String.valueOf(map.get("id")));
				objectMap.put(referenceKey, null);
				session.update(dataModelEntity.getTableName(), objectMap);
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
					subFormData.put("update_by", user != null ? user.getId() : null);
				} else {
					Map<String, Object> dataMap = new HashMap<>();
					for (String keyString : newMap.keySet()) {
						if (!"id".equals(keyString)) {
							dataMap.put(keyString, newMap.get(keyString));
						}
					}
					dataMap.put("create_at", new Date());
					dataMap.put("create_by", user != null ? user.getId() : null);
					subFormData = (Map<String, Object>) session.merge(dataModelEntity.getTableName(), dataMap);
				}

				//子表数据
				if(flag) {
					subFormData.put(referenceKey, data);
				}else{
					List<Map<String, Object>> list = (List<Map<String, Object>>)subFormData.get(referenceKey);
					if(list != null){
						List<String> referenceIds = new ArrayList<>();
						if(data.get("id") == null){
							list.add(data);
						}else {
							for (Map<String, Object> map : list) {
								if (map.get("id") != null) {
									referenceIds.add((String) map.get("id"));
								}
							}
							if (!referenceIds.contains(data.get("id"))) {
								list.add(data);
							}
						}
					}else{
						list = new ArrayList<>();
						list.add(data);
						subFormData.put(referenceKey, list);
					}
				}
				saveListMap.add(subFormData);
			}
		}
		return saveListMap;
	}


	private void deleteData(Session session, String tableName, String id, String referenceKey){
		try {
			Map<String, Object> objectMap = (Map<String, Object>)session.load(tableName, id);
			session.delete(objectMap);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException("删除【" + tableName + "】表，id【"+id+"】的数据失败");
		}
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
                value = o == null || StringUtils.isEmpty(o) ? null : String.valueOf(o);
            }
		} else if (itemModel.getType() == ItemType.InputNumber && ((NumberItemModelEntity)itemModel).getDecimalDigits() != null
				&& ((NumberItemModelEntity)itemModel).getDecimalDigits() > 0 && itemInstance.getValue() != null) {
			BigDecimal bigDecimal = new BigDecimal(String.valueOf(itemInstance.getValue()));
			value = bigDecimal.divide(new BigDecimal(1.0), ((NumberItemModelEntity)itemModel).getDecimalDigits(), BigDecimal.ROUND_DOWN).doubleValue();
			System.out.println(value);
		} else if (itemModel.getType() == ItemType.Media || itemModel.getType() == ItemType.Attachment) {
            Object o = itemInstance.getValue();
			Map<String, FileUploadEntity> fileUploadEntityMap = new HashMap<>();
			//文件数
			Integer numberLimit = ((FileItemModelEntity)itemModel).getFileNumberLimit();
			//文件类型
			String fileFormat = ((FileItemModelEntity)itemModel).getFileFormat();
			if(data.get("id") != null && data.get("id") != "") {
				List<FileUploadEntity> fileUploadList = fileUploadManager.query().filterEqual("fromSourceDataId", data.get("id")).filterEqual("fromSource", itemModel.getId()).filterEqual("sourceType", DataSourceType.ItemModel).list();
				for (FileUploadEntity fileUploadEntity : fileUploadList) {
					fileUploadEntityMap.put(fileUploadEntity.getId(), fileUploadEntity);
				}
			}
			if(o != null && o instanceof List){
				if(numberLimit != null && numberLimit > 0 && ((List) o).size() > numberLimit){
					throw new IFormException(itemModel.getName() + "控件上传文件数量超过" + numberLimit);
				}
				List<Map<String, String>> fileList = (List<Map<String, String>>)o;
				List<FileUploadEntity> newList = new ArrayList<>();
				for(Map<String, String> fileUploadModelMap : fileList){
					if(fileUploadModelMap == null || fileUploadModelMap.values() == null || fileUploadModelMap.values().size() < 1){
						continue;
					}
					FileUploadEntity fileUploadEntity = saveFileUploadEntity( fileUploadModelMap, fileUploadEntityMap, itemModel);
					newList.add(fileUploadEntity);
				}
				value = String.join(",", newList.parallelStream().map(FileUploadEntity::getId).collect(Collectors.toList()));
			}else{
				Map<String, String> fileUploadModel = o == null || o == "" ? null : (Map<String, String>)o;
				if(fileUploadModel != null && fileUploadModel.values() != null && fileUploadModel.values().size() > 0){
					FileUploadEntity fileUploadEntity = saveFileUploadEntity( fileUploadModel, fileUploadEntityMap, itemModel);
					value = fileUploadEntity.getId();
				}
			}
			for(String key : fileUploadEntityMap.keySet()){
				fileUploadManager.deleteById(key);
			}
		}  else if (itemModel.getType() == ItemType.Location) {
			Object o = itemInstance.getValue();
			if(o != null && o instanceof List){
				List<Map<String, Object>> fileList = (List<Map<String, Object>>)o;
				List<GeographicalMapEntity> newList = new ArrayList<>();
				for(Map<String, Object> geographicalMap : fileList){
					if(geographicalMap == null || geographicalMap.values() == null || geographicalMap.values().size() < 1){
						continue;
					}
					GeographicalMapEntity fileUploadEntity = saveGeographicalMapEntity( geographicalMap, itemModel.getId());
					newList.add(fileUploadEntity);
				}
				value = String.join(",", newList.parallelStream().map(GeographicalMapEntity::getId).collect(Collectors.toList()));
			}else{
				Map<String, Object> fileUploadModel = o == null || o == "" ? null : (Map<String, Object>)o;
				if(fileUploadModel != null && fileUploadModel.values() != null && fileUploadModel.values().size() > 0){
					GeographicalMapEntity fileUploadEntity = saveGeographicalMapEntity( fileUploadModel, itemModel.getId());
					value = fileUploadEntity.getId();
				}
			}
		}else {
			value = itemInstance.getValue() == null || StringUtils.isEmpty(itemInstance.getValue()) ? null : itemInstance.getValue();
        }
		ColumnModelEntity columnModel = itemModel.getColumnModel();
		if (Objects.nonNull(columnModel)) {
			data.put(columnModel.getColumnName(), value);
		}
	}

	public static void main(String[] args) {
		String fileKey = "2019-03-26/jpg/6e538e3812bd4107908903c875ab7d1f.jpg";
		String format = fileKey.substring(fileKey.lastIndexOf(".")+1);
		System.out.println(format);
	}

	private FileUploadEntity saveFileUploadEntity(Map<String, String> fileUploadModelMap, Map<String, FileUploadEntity> fileUploadEntityMap, ItemModelEntity itemModel){
		String fileKey = fileUploadModelMap.get("fileKey");
		String format = fileKey.substring(fileKey.lastIndexOf(".")+1);
		String fileFormat = ((FileItemModelEntity)itemModel).getFileFormat();
		if(StringUtils.hasText(fileFormat) && !fileFormat.contains(format)){
			throw new IFormException("未找到【"+fileUploadModelMap.get("id")+"】对应的文件格式类型不对");
		}

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
			fileUploadModel.setFileKey(fileKey);
			fileUploadModel.setName(fileUploadModelMap.get("name"));
			fileUploadModel.setUrl(fileUploadModelMap.get("url"));
			fileUploadModel.setThumbnail(fileUploadModelMap.get("thumbnail"));
			fileUploadModel.setThumbnailUrl(fileUploadModelMap.get("thumbnailUrl"));
			BeanUtils.copyProperties(fileUploadModel, fileUploadEntity);
		}
		fileUploadEntity.setSourceType(DataSourceType.ItemModel);
		fileUploadEntity.setFromSource(itemModel.getId());
		fileUploadManager.save(fileUploadEntity);
		return fileUploadEntity;
	}

	private GeographicalMapEntity saveGeographicalMapEntity(Map<String, Object> geographicalMap, String itemId){
		GeographicalMapEntity geographicalMapEntity = null;
		if(geographicalMap.get("id") != null){
			geographicalMapEntity = mapEntityJPAManager.get((String)geographicalMap.get("id"));
			if(geographicalMapEntity == null){
				throw new IFormException("未找到【"+geographicalMap.get("id")+"】对应的文件");
			}
		}else {
			geographicalMapEntity = new GeographicalMapEntity();
		}
		geographicalMapEntity.setLandmark((String)geographicalMap.get("landmark"));
		geographicalMapEntity.setDetailAddress((String)geographicalMap.get("detailAddress"));
		geographicalMapEntity.setLat(new BigDecimal(String.valueOf(geographicalMap.get("lat"))).doubleValue());
		geographicalMapEntity.setLng(new BigDecimal(String.valueOf(geographicalMap.get("lng"))).doubleValue());
		geographicalMapEntity.setFromSource(itemId);
		mapEntityJPAManager.save(geographicalMapEntity);
		return geographicalMapEntity;
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
			//校验是否关联
			for(ColumnReferenceEntity columnReferenceEntity : referenceEntityList){
				deleteVerify(columnReferenceEntity, entity,  itemModelEntities);
            }
			// before
			sendWebService( formModel, BusinessTriggerType.Delete_Before, entity, instanceId);
            if(entity != null) {
                session.beginTransaction();
                session.delete(dataModel.getTableName(), entity);
                session.getTransaction().commit();
            }
			// after
			sendWebService( formModel, BusinessTriggerType.Delete_After, entity, instanceId);
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

	private void deleteVerify(ColumnReferenceEntity columnReferenceEntity, Map<String, Object> entity, List<ReferenceItemModelEntity> itemModelEntities){
		if(columnReferenceEntity.getReferenceType() == ReferenceType.ManyToMany || !columnReferenceEntity.getFromColumn().getColumnName().equals("id") ){
			return;
		}
		if(columnReferenceEntity.getReferenceType() == ReferenceType.OneToOne ||
				columnReferenceEntity.getReferenceType() == ReferenceType.OneToMany ){
			String key = columnReferenceEntity.getToColumn().getDataModel().getTableName()+"_"+columnReferenceEntity.getToColumn().getColumnName()+"_list";
			if(entity.get(key) != null && entity.get(key) != ""){
				if(entity.get(key) instanceof List && ((List) entity.get(key)).size() < 1){
					return;
				}
				String formName = null;
				String itemName = null;
				for(ReferenceItemModelEntity referenceItemModelEntity : itemModelEntities){
					if(referenceItemModelEntity.getType() == ItemType.ReferenceLabel || referenceItemModelEntity.getColumnModel() == null
							|| !referenceItemModelEntity.getColumnModel().getId().equals(columnReferenceEntity.getToColumn().getId())){
						continue;
					}

					FormModelEntity formModelEntity = referenceItemModelEntity.getFormModel();
					if(formModelEntity == null && referenceItemModelEntity.getSourceFormModelId() != null){
						formModelEntity = formModelService.get(referenceItemModelEntity.getSourceFormModelId());
					}
					String itemModelEntityList = formModelEntity == null || formModelEntity.getItemModelIds() == null ? "" : formModelEntity.getItemModelIds();
					String[] list = itemModelEntityList.split(",");

					formName = formModelEntity == null? "" : formModelEntity.getName();
					List<String> idList = new ArrayList<>();
					if(entity.get(key) instanceof List){
						for(Map<String, Object> objectMap : (List<Map<String,Object>>)entity.get(key)){
							String idValue = objectMap.get("master_id") == null ? (String)objectMap.get("id") : (String)((Map<String, Object>)objectMap.get("master_id")).get("id");
							if(idValue != null){
								idList.add(idValue);
							}
						}
					}else{
						Map<String, Object> objectMap = ((Map<String, Object>)entity.get(key));
						String idValue = objectMap.get("master_id") == null ? (String)objectMap.get("id") : (String)((Map<String, Object>)objectMap.get("master_id")).get("id");
						if(idValue != null){
							idList.add(idValue);
						}
					}
					itemName = getItemName( idList, formModelEntity,  list);
				}
				throw new IFormException("该数据被【" + formName + "】表单的【"+itemName+"】关联，无法删除");
			}
		}
	}

	private String getItemName(List<String> idList, FormModelEntity formModelEntity, String[] list){
		List<String> valueList = new ArrayList<>();
		for(String str : idList){
			if(!StringUtils.hasText(str)){
				continue;
			}
			FormInstance formInstance =  getFormInstance(formModelEntity, str);
			if(formInstance == null){
				continue;
			}
			Map<String, ItemInstance> itemInstanceHashMap = new HashMap<>();
			for(ItemInstance itemInstance : formInstance.getItems()){
				itemInstanceHashMap.put(itemInstance.getId(), itemInstance);
			}
			for(String idStr : list){
				List<String> values = new ArrayList<>();
				ItemInstance itemInstance = itemInstanceHashMap.get(idStr);
				if(itemInstance == null){
					continue;
				}
				if(itemInstance instanceof List){
					if(itemInstance.getType() == ItemType.Media || itemInstance.getType() == ItemType.Attachment){
						values.add(((FileUploadModel)itemInstance.getDisplayValue()).getName());
					}else{
						values.add((String)itemInstance.getDisplayValue());
					}
				}else{
					if(itemInstance.getType() == ItemType.Media || itemInstance.getType() == ItemType.Attachment){
						values.add(((FileUploadModel)itemInstance.getDisplayValue()).getName());
					}else{
						values.add((String)itemInstance.getDisplayValue());
					}
				}
				valueList.add(String.join(",", values));
			}
		}
		return String.join(",", valueList);
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

	public Criteria generateCriteria(Session session, FormModelEntity formModel, ListModelEntity listModel, Map<String, Object> queryParameters) {
		DataModelEntity dataModel = formModel.getDataModels().get(0);

		Criteria criteria = session.createCriteria(dataModel.getTableName());

		Map<String, ItemModelEntity> idAndItemMap = assemblyFormAllItems(formModel);

		for (String id:queryParameters.keySet()) {
			Object value = queryParameters.get(id);
			if (value==null || "".equals(value.toString())) {
				continue;
			}
			if ("fullTextSearch".equals(id) && listModel!=null) {
				fullTextSearchCriteria(criteria, value, listModel);
				continue;
			}
			ItemModelEntity itemModel = idAndItemMap.get(id);
			if (itemModel==null) {
				continue;
			}

			Object[] values = null;
			if (value instanceof String[]) {
				values = (String[])value;
			} else if (value instanceof String) {
				if (value != null && StringUtils.hasText(String.valueOf(value))) {
					values = new Object[] {value};
				}
			}
			if (values==null || values.length==0) {
				continue;
			}

			ColumnModelEntity columnModel = itemModel.getColumnModel();

			String propertyName = null;
			Boolean propertyIsReferenceCollection = false;
			if (itemModel instanceof ReferenceItemModelEntity) {
				ReferenceItemModelEntity referenceItemModel = (ReferenceItemModelEntity)itemModel;
				if (referenceItemModel.getSelectMode() == SelectMode.Single && (referenceItemModel.getReferenceType() == ReferenceType.ManyToOne
						|| referenceItemModel.getReferenceType() == ReferenceType.OneToOne)) {
					if(referenceItemModel.getColumnModel() == null){
						continue;
					}
					columnModel = referenceItemModel.getColumnModel();
					propertyName = columnModel.getColumnName()+".id";
				}else if (referenceItemModel.getSelectMode() == SelectMode.Inverse && (referenceItemModel.getReferenceType() == ReferenceType.ManyToOne
						|| referenceItemModel.getReferenceType() == ReferenceType.OneToOne)) {
					ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemModelManager.get(referenceItemModel.getReferenceItemId());
					if(referenceItemModelEntity1.getColumnModel() == null){
						continue;
					}
					propertyIsReferenceCollection = true;
					columnModel = referenceItemModelEntity1.getColumnModel();
					propertyName = columnModel.getDataModel().getTableName()+"_"+referenceItemModelEntity1.getColumnModel().getColumnName()+"_list";
				}else if(referenceItemModel.getSelectMode() == SelectMode.Multiple){
//					columnModel = new ColumnModelEntity();
					propertyIsReferenceCollection = true;
					FormModelEntity toModelEntity = formModelService.find(((ReferenceItemModelEntity) itemModel).getReferenceFormId());
					if (toModelEntity == null) {
						continue;
					}
					propertyName = toModelEntity.getDataModels().get(0).getTableName()+"_list";
				}
			} else if (itemModel.getColumnModel()!=null) {        // 普通控件
				propertyName = columnModel.getColumnName();
			}

			if (StringUtils.isEmpty(propertyName) || columnModel == null) {
				continue;
			}

			boolean equalsFlag = false;

			for (int i = 0; i < values.length; i++) {
				value = null;
				if (itemModel.getSystemItemType() == SystemItemType.CreateDate || itemModel.getType() == ItemType.DatePicker || itemModel.getType() == ItemType.TimePicker) {
					equalsFlag = true;
					if (values[i] != null) {
                        value = getTimeParams(itemModel.getType(), String.valueOf(values[i]));
					}
				} else if (itemModel.getType() == ItemType.InputNumber) {
					equalsFlag = true;
					Object number = getNumberParams(itemModel, columnModel, value);
					if (number!=null) {
						values[i] = number;
					}
				} else if (columnModel.getDataType() == ColumnType.Boolean) {
					equalsFlag = true;
					if (!(value instanceof Boolean)) {
						String strValue = String.valueOf(value);
						values[i] = "true".equals(strValue);
					}
				}
			}

			if (equalsFlag) {
				if(itemModel.getSystemItemType() == SystemItemType.CreateDate || itemModel.getType() == ItemType.DatePicker || itemModel.getType() == ItemType.TimePicker) {
					//Date dateParams = timestampNumberToDate(value);
                    Object[] objects = (Object[])value;
					if (Objects.nonNull(value)) {  // Timestamp
                        if(objects.length == 3) {
                        	if(((Date)objects[2]).before((Date)objects[0])){
                        		throw new IFormException(itemModel.getName() + "开始时间不能大于结束时间");
							}
                            criteria.add(Restrictions.ge(propertyName, objects[0]));
                            criteria.add(Restrictions.lt(propertyName, objects[2]));
                        }else if(objects[0] instanceof Date){
                            criteria.add(Restrictions.ge(propertyName, objects[0]));
                        }else if(objects[0] instanceof String){
                            criteria.add(Restrictions.lt(propertyName, objects[1]));
                        }
					}
				} else {
					Criterion[] conditions = new Criterion[values.length];
					for (int i=0; i<values.length; i++) {
						conditions[i] = Restrictions.eq(propertyName, values[i]);
					}
					criteria.add(Restrictions.or(conditions));
				}
			} else {
				if (propertyIsReferenceCollection) {
					criteria.createCriteria(propertyName).add(Restrictions.in("id", values));
				} else {
					Criterion[] conditions = new Criterion[values.length];
					for (int i = 0; i < values.length; i++) {
						conditions[i] = Restrictions.like(propertyName, "%" + values[i] + "%");
					}
					criteria.add(Restrictions.or(conditions));
				}
			}
		}
		return criteria;
	}

	private Object getNumberParams(ItemModelEntity itemModel, ColumnModelEntity columnModel, Object value) {
		String strValue = String.valueOf(value);
		try {
			if (columnModel.getDataType() == ColumnType.Integer) {
				return Integer.parseInt(strValue);
			} else if (columnModel.getDataType() == ColumnType.Long) {
				return Long.parseLong(strValue);
			} else if (columnModel.getDataType() == ColumnType.Float) {
				return Float.parseFloat(strValue);
			} else if (columnModel.getDataType() == ColumnType.Double) {
				return Double.parseDouble(strValue);
			}
		} catch (NumberFormatException e) {
			throw new ICityException(itemModel.getName() + "格式不正确");
		}
		return null;
	}

	private void fullTextSearchCriteria(Criteria criteria, Object value, ListModelEntity listModelEntity) {
		if (value!=null && value instanceof String) {
			String valueStr = value.toString();
			List<Criterion> conditions = new ArrayList();
			List<ListSearchItem> searchItems = listModelEntity.getSearchItems();
			searchItems = searchItems.stream().filter(item->(item.getFullTextSearch()!=null && item.getFullTextSearch()==true)).collect(Collectors.toList());
			for (ListSearchItem searchItem:searchItems) {
				if (searchItem==null) {
					continue;
				}
				ItemModelEntity itemModelEntity = searchItem.getItemModel();
				if (itemModelEntity==null) {
					continue;
				}
				ColumnModelEntity columnModel = itemModelEntity.getColumnModel();
				if (itemModelEntity.getSystemItemType()==SystemItemType.Input || itemModelEntity.getSystemItemType()==SystemItemType.MoreInput
						|| itemModelEntity.getSystemItemType()==SystemItemType.Editor) {  // 单行文本控件,多行文本控件,富文本控件
					if (columnModel!=null) {
						conditions.add(Restrictions.like(columnModel.getColumnName(), "%" + valueStr + "%"));
					}
				} else if (itemModelEntity instanceof SelectItemModelEntity && columnModel!=null && StringUtils.hasText(columnModel.getColumnName())) {
					fullTextSearchSelectItemCriteria(valueStr, conditions, columnModel.getColumnName(), (SelectItemModelEntity)itemModelEntity);
				} else if (itemModelEntity instanceof TreeSelectItemModelEntity && columnModel!=null && StringUtils.hasText(columnModel.getColumnName())) {
					fullTextSearchTreeSelectItemCriteria(valueStr, conditions, columnModel.getColumnName(), (TreeSelectItemModelEntity)itemModelEntity);
				} else if (itemModelEntity instanceof ReferenceItemModelEntity) {
					ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity)itemModelEntity;
					ReferenceItemModelEntity parentItem = referenceItemModelEntity.getParentItem();
					if (referenceItemModelEntity.getSystemItemType()==SystemItemType.Creator ||
							(parentItem!=null && parentItem.getSystemItemType()==SystemItemType.Creator)) {
						fullTextSearchPeopleReferenceItemCriteria(valueStr, conditions, referenceItemModelEntity);
					} else {
						fullTextSearchReferenceItemCriteria(valueStr, conditions, columnModel, referenceItemModelEntity);
					}
				}
			}
			criteria.add(Restrictions.or(conditions.toArray(new SimpleExpression[]{})));
		}
	}

	private void fullTextSearchSelectItemCriteria(String valueStr, List<Criterion> conditions, String columnFullname, SelectItemModelEntity selectItemModelEntity) {
		if (StringUtils.isEmpty(columnFullname)) {
			return;
		}
		List<ItemSelectOption> options = selectItemModelEntity.getOptions();
		String referenceDictionaryId = selectItemModelEntity.getReferenceDictionaryId();
		if (options!=null && options.size()>0) {
			Set<String> optionIds = options.stream().filter(item-> StringUtils.hasText(item.getLabel())&&item.getLabel().contains(valueStr)).map(item->item.getId()).collect(Collectors.toSet());
			if (optionIds!=null && optionIds.size()>0) {
				for (String optionId:optionIds) {
					conditions.add(Restrictions.like(columnFullname, "%" + optionId + "%"));
				}
			}
		} else if (StringUtils.hasText(referenceDictionaryId)) {
			List<DictionaryDataItemModel> list = dictionaryService.findDictionaryItems(referenceDictionaryId, valueStr);
			for (DictionaryDataItemModel item:list) {
				conditions.add(Restrictions.like(columnFullname, "%" + item.getId() + "%"));
			}
		}
	}

	private void fullTextSearchTreeSelectItemCriteria(String valueStr, List<Criterion> conditions, String columnFullname, TreeSelectItemModelEntity treeSelectItemModelEntity) {
		if (StringUtils.isEmpty(columnFullname)) {
			return;
		}
		TreeSelectDataSource dataSource = treeSelectItemModelEntity.getDataSource();
		if (dataSource!=null) {
			if (TreeSelectDataSource.SystemCode==dataSource) {
				String dictionaryId = treeSelectItemModelEntity.getReferenceDictionaryId();
				if (StringUtils.hasText(dictionaryId)) {
					List<DictionaryDataItemModel> list = dictionaryService.findDictionaryItems(dictionaryId, valueStr);
					for (DictionaryDataItemModel item:list) {
						conditions.add(Restrictions.like(columnFullname, "%" + item.getId() + "%"));
					}
				}
			} else if (TreeSelectDataSource.Department==dataSource || TreeSelectDataSource.Position==dataSource
					|| TreeSelectDataSource.Personnel==dataSource || TreeSelectDataSource.PositionIdentify==dataSource) {
				List<TreeSelectData> list = groupService.queryTreeSelectDataSourceList(dataSource.getValue(), valueStr, treeSelectItemModelEntity.getDataRange(), treeSelectItemModelEntity.getDataDepth());
				for (TreeSelectData item:list) {
					conditions.add(Restrictions.like(columnFullname, "%" + item.getId() + "%"));
				}
			}
		}
	}

	private void fullTextSearchReferenceItemCriteria(String valueStr, List<Criterion> conditions, ColumnModelEntity columnModel, ReferenceItemModelEntity referenceItemModelEntity) {
		if (referenceItemModelEntity.getSelectMode() == SelectMode.Single && (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne
				|| referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne)) {
			fullTextSearchSingleReferenceItemCriteria(valueStr, conditions, columnModel, referenceItemModelEntity);
		} else if (referenceItemModelEntity.getSelectMode() == SelectMode.Inverse && (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne
				|| referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne)) {
			fullTextSearchInverseReferenceItemCriteria(valueStr, conditions, referenceItemModelEntity);
		} else if(referenceItemModelEntity.getSelectMode() == SelectMode.Multiple){
			fullTextSearchMultipleReferenceItemCriteria(valueStr, conditions, referenceItemModelEntity);
		}
	}

	// 关联表单多选
	private void fullTextSearchMultipleReferenceItemCriteria(String valueStr, List<Criterion> conditions, ReferenceItemModelEntity referenceItemModelEntity) {
		String referenceItemId = referenceItemModelEntity.getReferenceItemId();
		if (StringUtils.isEmpty(referenceItemId)) {
			return;
		}
		ItemModelEntity realItemModelEntity = itemModelManager.find(referenceItemId);
		if (realItemModelEntity==null) {
			return;
		}
		ColumnModelEntity columnModel = realItemModelEntity.getColumnModel();
		if (columnModel == null) {
			return;
		}
		FormModelEntity toModelEntity = formModelService.find(referenceItemModelEntity.getReferenceFormId());
		if (toModelEntity == null) {
			return;
		}
		String columnFullname = toModelEntity.getDataModels().get(0).getTableName()+"_list"+"."+columnModel.getColumnName();
		fullTextSearchMultipleOrInverseReferenceItemCriteria(valueStr, conditions, columnFullname, realItemModelEntity);
	}

	// 反向关联属性
	private void fullTextSearchInverseReferenceItemCriteria(String valueStr, List<Criterion> conditions, ReferenceItemModelEntity referenceItemModelEntity) {
		String referenceItemId = referenceItemModelEntity.getReferenceItemId();
		if (StringUtils.isEmpty(referenceItemId)) {
			return;
		}
		ItemModelEntity realItemModelEntity = itemModelManager.find(referenceItemId);
		if (realItemModelEntity==null) {
			return;
		}
		ColumnModelEntity columnModel = realItemModelEntity.getColumnModel();
		if(columnModel == null) {
			return;
		}
		DataModelEntity dataModelEntity = columnModel.getDataModel();
		if (dataModelEntity==null) {
			return;
		}
		String columnFullname = dataModelEntity.getTableName()+"_"+columnModel.getColumnName()+"_list"+"."+columnModel.getColumnName();
		fullTextSearchMultipleOrInverseReferenceItemCriteria(valueStr, conditions, columnFullname, realItemModelEntity);
	}

	// 反向关联属性和关联表单多选
	private void fullTextSearchMultipleOrInverseReferenceItemCriteria(String valueStr, List<Criterion> conditions, String columnFullname, ItemModelEntity itemModelEntity) {
		if (itemModelEntity.getSystemItemType() == SystemItemType.Input || itemModelEntity.getSystemItemType() == SystemItemType.MoreInput
				|| itemModelEntity.getSystemItemType() == SystemItemType.Editor) {  // 单行文本控件,多行文本控件,富文本控件
			conditions.add(Restrictions.like(columnFullname, "%" + valueStr + "%"));
		} else if (itemModelEntity instanceof SelectItemModelEntity) {
			fullTextSearchSelectItemCriteria(valueStr, conditions, columnFullname, (SelectItemModelEntity)itemModelEntity);
		} else if (itemModelEntity instanceof TreeSelectItemModelEntity) {
			fullTextSearchTreeSelectItemCriteria(valueStr, conditions, columnFullname, (TreeSelectItemModelEntity)itemModelEntity);
		}
	}

	// 正向关联属性
	private void fullTextSearchSingleReferenceItemCriteria(String valueStr, List<Criterion> conditions, ColumnModelEntity columnModel, ReferenceItemModelEntity referenceItemModelEntity) {
		String referenceItemId = referenceItemModelEntity.getReferenceItemId();
		if (StringUtils.hasText(referenceItemId)) {
			ItemModelEntity realItemModelEntity = itemModelManager.find(referenceItemId);
			if (realItemModelEntity==null) {
				return;
			}
			ReferenceItemModelEntity parentReferenceItemModelEntity = referenceItemModelEntity.getParentItem();
			if (parentReferenceItemModelEntity==null) {
				return;
			}
			String parentReferenceColumnName = parentReferenceItemModelEntity.getColumnModel().getColumnName();
			String columnName = realItemModelEntity.getColumnModel().getColumnName();
			if (StringUtils.isEmpty(parentReferenceColumnName) || StringUtils.isEmpty(columnName)) {
				return;
			}
			if (realItemModelEntity.getSystemItemType() == SystemItemType.Input || realItemModelEntity.getSystemItemType() == SystemItemType.MoreInput
					|| realItemModelEntity.getSystemItemType() == SystemItemType.Editor) {  // 单行文本控件,多行文本控件,富文本控件
				if (columnModel != null) {
					conditions.add(Restrictions.like(parentReferenceColumnName+"."+columnName, "%" + valueStr + "%"));
				}
			} else if (realItemModelEntity instanceof SelectItemModelEntity && columnModel!=null && StringUtils.hasText(columnModel.getColumnName())) {
				fullTextSearchSelectItemCriteria(valueStr, conditions, parentReferenceColumnName+"."+columnName, (SelectItemModelEntity)realItemModelEntity);
			} else if (realItemModelEntity instanceof TreeSelectItemModelEntity && columnModel!=null && StringUtils.hasText(columnModel.getColumnName())) {
				fullTextSearchTreeSelectItemCriteria(valueStr, conditions, parentReferenceColumnName+"."+columnName, (TreeSelectItemModelEntity)realItemModelEntity);
			}
		}
	}

	private void fullTextSearchPeopleReferenceItemCriteria(String valueStr, List<Criterion> conditions, ReferenceItemModelEntity referenceItemModelEntity) {
		String referenceItemId = referenceItemModelEntity.getReferenceItemId();
		if (StringUtils.hasText(referenceItemId)) {
			ReferenceItemModelEntity parentItem = referenceItemModelEntity.getParentItem();
			if (parentItem==null) {
				return;
			}
			ItemModelEntity itemModelEntity = itemModelService.find(referenceItemId);
			ColumnModelEntity columnModelEntity = itemModelEntity.getColumnModel();
			if (columnModelEntity!=null) {
				String userInfoColumnName = columnModelEntity.getColumnName();
				List<String> userIds = fullTextSearchUserId(userInfoColumnName, valueStr);
				if (userIds==null || userIds.size()==0) {
					return;
				}
				String columnName = parentItem.getColumnModel().getColumnName();
				for (String userId:userIds) {
					conditions.add(Restrictions.like(columnName, "%" + userId + "%"));
				}
			}
		} else if (referenceItemModelEntity.getSystemItemType()==SystemItemType.Creator) {
			ColumnModelEntity columnModel = referenceItemModelEntity.getColumnModel();
            List<String> userIds = fullTextSearchUserId(columnModel.getColumnName(), valueStr);
			if (userIds==null || userIds.size()==0) {
				return;
			}
			for (String userId:userIds) {
				conditions.add(Restrictions.like(columnModel.getColumnName(), "%" + userId + "%"));
			}
		}
	}

    /**
     * columnName的取值范围是 username(账号), phone(电话), nickname(昵称), position(岗位), group(部门)
     * @param columnName
     * @param value
     * @return
     */
    private Set<String> userColumnNames = new HashSet<>(Arrays.asList("username", "phone", "nickname", "position", "group"));
	private List<String> fullTextSearchUserId(String columnName, String value) {
	    List<String> list = new ArrayList<>();
	    List<String> groupIds = new ArrayList<>();
	    if (userColumnNames.contains(columnName)==false) {
	        return list;
        }
	    value = "%" + value + "%";
	    if ("position".equals(columnName) || "group".equals(columnName)) {
	        String groupType = "position".equals(columnName)? "2":"1";
            List<Map<String, Object>> data = jdbcTemplate.queryForList("select id FORM sys_group WHERE type='"+groupType+"' AND group_name LIKE "+value);
            for (Map<String, Object> item:data) {
                groupIds.add(item.get("id").toString());
            }
            if (groupIds.size()>0) {
                columnName = "position".equals(columnName)? "position_id":"group_id";
                String idArrStr = String.join("','", groupIds);
                data = jdbcTemplate.queryForList("select id FROM sys_user where " + columnName + " in ('"+idArrStr+"')");
                for (Map<String, Object> item:data) {
                    list.add(item.get("id").toString());
                }
            }
        } else {
            List<Map<String, Object>> data = jdbcTemplate.queryForList("select id FORM sys_user WHERE columnName LIKE "+value);
            for (Map<String, Object> item:data) {
                list.add(item.get("id").toString());
            }
        }
	    return list;
    }

	private Object[] getTimeParams(ItemType itemType, String strValue) {
		boolean flag = strValue.startsWith(",");
		if(flag){
			strValue = strValue.substring(1);
		}
		String[] timeParams = strValue.split(",");
		int size = timeParams.length;
		Object[] o = new Object[size + 1];
		if(size == 1){
			Object object = null;
			if(itemType != ItemType.TimePicker) {
				if(flag) {
					object = DateUtils.addDays(new Date(Long.parseLong(timeParams[0])), 1);;
				}else{
					object = new Date(Long.parseLong(timeParams[0]));
				}
			}else{
				object = timeParams[0];
			}
			if(flag){
				o[0] = ",";
				o[1] = object;
			}else{
				o[0] = object;
				o[1] = ",";
			}
		}else if(size == 2){
			for(int t = 0; t < size + 1; t++) {
				int k = t;
				if(t > 1){
					k = t-1;
				}
				if(t == 1){
					o[t] = ",";
					continue;
				}
				if(itemType != ItemType.TimePicker) {
					if(k > 0) {
						o[t] = DateUtils.addDays(new Date(Long.parseLong(timeParams[k])), 1);;
					}else {
						o[t] = new Date(Long.parseLong(timeParams[k]));
					}
				}else{
					o[t] = timeParams[k];
				}
			}
		}
		return  o;
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

	public Map<String, ItemModelEntity> assemblyFormAllItems(FormModelEntity formModelEntity) {
		Map<String, ItemModelEntity> map = new HashMap<>();
		for (ItemModelEntity itemModel:formModelEntity.getItems()) {
			assemblyItemsInItem(itemModel, map);
		}
		return map;
	}

	/** 获取item结构里面的items和parentItem */
	public void assemblyItemsInItem(ItemModelEntity itemModelEntity, Map<String, ItemModelEntity> idAndItemMap) {
		if (itemModelEntity!=null) {
			String id = itemModelEntity.getId();
			if (idAndItemMap.get(id)==null) {
				idAndItemMap.put(id, itemModelEntity);
				Class clazz = itemModelEntity.getClass();  //得到类对象
				Field[] fs = clazz.getDeclaredFields();    //得到属性集合
				for (Field f : fs) {                       //遍历属性
					if (f.getName().equals("items")) {
						f.setAccessible(true);             //设置属性是可以访问的(私有的也可以)
						try {
							Object itemValues = f.get(itemModelEntity);
							if (itemValues != null && itemValues instanceof List) {
								List<ItemModelEntity> items = (List<ItemModelEntity>) itemValues;
								for (ItemModelEntity subItem : items) {
									assemblyItemsInItem(subItem, idAndItemMap);
								}
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					} else if (f.getName().equals("parentItem")) {
						f.setAccessible(true);             //设置属性是可以访问的(私有的也可以)
						try {
							Object itemValue = f.get(itemModelEntity);
							if (itemValue != null && itemValue instanceof ItemModelEntity) {
								assemblyItemsInItem((ItemModelEntity)itemValue, idAndItemMap);
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
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

	protected List<FormDataSaveInstance> wrapFormDataList(FormModelEntity formModel, ListModelEntity listModel, List<Map<String, Object>> entities) {
		List<FormDataSaveInstance> FormInstanceList = new ArrayList<FormDataSaveInstance>();
		for (Map<String, Object> entity : entities) {
			FormInstanceList.add(wrapFormDataEntity(false, formModel, listModel, entity,String.valueOf(entity.get("id")), true));
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

	protected FormDataSaveInstance wrapFormDataEntity(boolean isQrCodeFlag, FormModelEntity formModelEntity, ListModelEntity listModel, Map<String, Object> entity, String instanceId, boolean referenceFlag) {
		FormDataSaveInstance formInstance = new FormDataSaveInstance();
		FormModelEntity formModel = null;
		if (listModel != null) {
			formModel = listModel.getMasterForm();
		} else {
			formModel = formModelEntity;
		}
		formInstance.setFormId(formModel.getId());
		//数据id
		formInstance.setId(instanceId);
		if (formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getId())) {
			formInstance.setProcessId((String) entity.get("PROCESS_ID"));
			formInstance.setProcessInstanceId((String) entity.get("PROCESS_INSTANCE"));
			formInstance.setActivityId((String) entity.get("ACTIVITY_ID"));
			formInstance.setActivityInstanceId((String) entity.get("ACTIVITY_INSTANCE"));
		}
		return setFormDataInstanceModel(isQrCodeFlag, formInstance, formModel,  listModel, entity, referenceFlag);
	}

	protected FormDataSaveInstance wrapQrCodeFormDataEntity(boolean isQrCodeFlag, ListModelEntity listModel, Map<String, Object> entity, String instanceId, boolean referenceFlag) {
		return wrapFormDataEntity(isQrCodeFlag, listModel.getMasterForm(), listModel, entity,String.valueOf(entity.get("id")), true);
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

	private FormDataSaveInstance setFormDataInstanceModel(boolean isQrCodeFlag, FormDataSaveInstance formInstance, FormModelEntity formModel,  ListModelEntity listModelEntity,
														  Map<String, Object> entity, boolean referenceFlag){
		List<ItemInstance> items = new ArrayList<>();
		List<ItemModelEntity> list = formModel.getItems();
		List<ReferenceDataInstance> referenceDataModelList = formInstance.getReferenceData();
		List<SubFormItemInstance> subFormItems = formInstance.getSubFormData();
		for (ItemModelEntity itemModel : list) {
			setFormDataItemInstance(isQrCodeFlag, itemModel, referenceFlag, entity, referenceDataModelList,
					subFormItems, items, formInstance);
		}

		List<String> labelIdList = null;
		if(StringUtils.hasText(formModel.getItemModelIds())){
			labelIdList = new ArrayList<>();
			for(String str : formModel.getItemModelIds().split(",")) {
				labelIdList.add(str);
			}
		}

		//展示字段
		List<String> displayIds = new ArrayList<>();
		if (!isQrCodeFlag && listModelEntity != null) {
			displayIds = listModelEntity.getDisplayItems().parallelStream().map(ItemModelEntity::getId).collect(Collectors.toList());
		}else if(isQrCodeFlag && formModel.getQrCodeItemModelIds() != null){
			displayIds = Arrays.asList(formModel.getQrCodeItemModelIds().split(","));
		}
		List<String> copyDisplayIds = displayIds.stream().collect(Collectors.toList());
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
		// referenceDataModelList的数据对应的是关联表单的数据标识的item的数据
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

		if(labelIdList != null) {
			Map<String, ItemInstance> labelItemMap = new HashMap<>();
			for(ItemInstance itemInstance : newDisplayItems) {
				if (labelIdList.contains(itemInstance.getId())) {
					labelItemMap.put(itemInstance.getId(), itemInstance);
				}
			}
			formInstance.setLabel(getLabel(labelIdList, labelItemMap));
		}

		formInstance.getItems().addAll(newDisplayItems);


		//二维码只有一张图
		if(idVlaue != null && idVlaue != "") {
			List<FileUploadEntity> fileUploadEntityList = uploadService.getFileUploadEntity(DataSourceType.FormModel, formInstance.getFormId(), String.valueOf(idVlaue));
			if (fileUploadEntityList != null && fileUploadEntityList.size() > 0) {
				FileUploadModel fileUploadModel = new FileUploadModel();
				BeanUtils.copyProperties(fileUploadEntityList.get(0), fileUploadModel);
				formInstance.setFileUploadModel(fileUploadModel);
			}
		}
		return formInstance;
	}

	private String getLabel(List<String> labelIdList, Map<String , ItemInstance> labelItemMap){
		StringBuffer label = new StringBuffer();
		for (int i = 0; i < labelIdList.size(); i++) {
			String labelItemId = labelIdList.get(i);
			ItemInstance itemInstance = labelItemMap.get(labelItemId);
			if (itemInstance == null || itemInstance.getDisplayValue() == null || !StringUtils.hasText(String.valueOf(itemInstance.getDisplayValue())) || itemInstance.getSystemItemType() == SystemItemType.ID) {
				labelIdList.remove(labelItemId);
				i--;
				continue;
			}
			String value =  getDisplayValue(itemInstance);
			if (i == 0) {
				label.append(value);
			} else {
				label.append("," + value);
			}
		}
		return label.toString();
	}

	private String getDisplayValue(ItemInstance itemInstance){
		Object displayVlaue = itemInstance.getDisplayValue();
		String value = null;
		if(itemInstance.getType() == ItemType.Attachment || itemInstance.getType() == ItemType.Media){
			List<FileUploadModel> listModels = (List<FileUploadModel>)displayVlaue;
			StringBuffer sub = new StringBuffer();
			for(int j = 0; j < listModels.size(); j ++){
				if(j == 0){
					sub.append(listModels.get(j));
				}else{
					sub.append(","+listModels.get(j));
				}
			}
			value = sub.toString();
		}else {
			if(displayVlaue instanceof List){
				List<String> valueList = (List<String>)displayVlaue;
				StringBuffer sub = new StringBuffer();
				for(int j = 0; j < valueList.size(); j ++){
					if(j == 0){
						sub.append(valueList.get(j));
					}else{
						sub.append(","+valueList.get(j));
					}
				}
				value = sub.toString();
			}else{
				value = (String)displayVlaue;
			}
		}
		return value;
	}


	private void setItemInstance(ItemModelEntity itemModel, boolean referenceFlag, Map<String, Object> entity, List<DataModelInstance> referenceDataModelList,
								 List<SubFormItemInstance> subFormItems, List<ItemInstance> items, FormInstance formInstance){
		ColumnModelEntity column = itemModel.getColumnModel();
		if(column == null && !(itemModel instanceof  ReferenceItemModelEntity) && !(itemModel instanceof  RowItemModelEntity) && !(itemModel instanceof SubFormItemModelEntity) && !(itemModel instanceof TabsItemModelEntity)){
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
		}else if(itemModel instanceof TabsItemModelEntity){
			for(TabPaneItemModelEntity itemModelEntity : ((TabsItemModelEntity) itemModel).getItems()) {
				for(ItemModelEntity itemModelEntity1 : itemModelEntity.getItems()) {
					setItemInstance(itemModelEntity1, referenceFlag, entity, referenceDataModelList,
							subFormItems, items, formInstance);
				}
			}
		}else{
			ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
			items.add(itemInstance);
			formInstance.addData(itemModel.getColumnModel().getId(), itemInstance.getValue());
		}
	}


	private void setFormDataItemInstance(boolean isQrCodeFlag, ItemModelEntity itemModel, boolean referenceFlag, Map<String, Object> entity, List<ReferenceDataInstance> referenceDataModelList,
								 List<SubFormItemInstance> subFormItems, List<ItemInstance> items, FormDataSaveInstance formInstance){
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
			setFormDataReferenceItemInstance(isQrCodeFlag, itemModel,  entity,  referenceDataModelList, items, referenceFlag);
		}else if(itemModel instanceof SubFormItemModelEntity) {
			if(!referenceFlag){
				return;
			}
			setSubFormItemInstance( itemModel,  entity,  subFormItems, formInstance.getActivityId());
		}else if(itemModel instanceof RowItemModelEntity){
			for(ItemModelEntity itemModelEntity : ((RowItemModelEntity) itemModel).getItems()) {
				setFormDataItemInstance(isQrCodeFlag, itemModelEntity, referenceFlag, entity, referenceDataModelList,
						subFormItems,  items, formInstance);
			}
		}else if(itemModel instanceof TabsItemModelEntity){
			for(TabPaneItemModelEntity itemModelEntity : ((TabsItemModelEntity) itemModel).getItems()) {
				for(ItemModelEntity itemModelEntity1 : itemModelEntity.getItems()) {
					setFormDataItemInstance(isQrCodeFlag, itemModelEntity1, referenceFlag, entity, referenceDataModelList,
							subFormItems, items, formInstance);
				}
			}
		}else{
			ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
			items.add(itemInstance);
			formInstance.addData(itemModel.getColumnModel().getId(), itemInstance.getValue());
		}
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
		if(fromItem.getSelectMode() == SelectMode.Attribute || fromItem.getType() == ItemType.ReferenceLabel){
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
		if(listMap == null){
			return;
		}
		if(flag) {
			DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, fromItem, columnModelEntity, (Map<String, Object>)listMap);
			dataModelInstance.setReferenceType(fromItem.getReferenceType());
			dataModelInstance.setReferenceValueColumn(referenceColumnName);
			referenceDataModelList.add(dataModelInstance);
		}else{
			List<Map<String, Object>> mapList = (List<Map<String, Object>>)listMap;
			for (Map<String, Object> map1 : mapList) {
				DataModelInstance dataModelInstance = setDataModelInstance(toModelEntity, fromItem, columnModelEntity, map1);
				dataModelInstance.setReferenceType(fromItem.getReferenceType());
				dataModelInstance.setReferenceValueColumn(referenceColumnName);
				referenceDataModelList.add(dataModelInstance);
			}
		}

	}


	private void setFormDataReferenceItemInstance( boolean isQrCodeFlag, ItemModelEntity itemModel, Map<String, Object> entity, List<ReferenceDataInstance> referenceDataModelList, List<ItemInstance> items, boolean referenceFlag){
		//主表字段
		ReferenceItemModelEntity fromItem = (ReferenceItemModelEntity)itemModel;

		//设置关联属性
		if(fromItem.getSelectMode() == SelectMode.Attribute || fromItem.getType() == ItemType.ReferenceLabel){
			setFormDataReferenceAttribute(fromItem, entity, items);
			return;
		}

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

		if(fromItem.getReferenceList() == null || fromItem.getReferenceList().getMasterForm() == null){
			throw new IFormException("关联控件【"+fromItem.getName()+"】未找到对应的列表模型");
		}

		String itemModelIds = toModelEntity.getItemModelIds();
		List<String> stringList = StringUtils.hasText(itemModelIds) ? Arrays.asList(itemModelIds.split(",")) : new ArrayList<>();

		Map<String, Boolean> keyMap = getReferenceMap( fromItem,  toModelEntity);
		if(keyMap == null){
			return;
		}
		//关联字段
		String key =new ArrayList<>(keyMap.keySet()).get(0);
		boolean flag = keyMap.get(key);
		Object listMap = null;
		if(flag) {
			if(fromItem.getSystemItemType() == SystemItemType.Creator){
				//关联人员
				if(entity.get(key) != null) {
					listMap = new HashMap<>();
					((HashMap) listMap).put("id", entity.get(key));
				}
			}else {
				listMap = (Map<String, Object>) entity.get(key);
			}
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
		if(fromItem.getReferenceType() == ReferenceType.ManyToOne || fromItem.getReferenceType() == ReferenceType.OneToOne ){
			if(flag) {
				if(listMap  != null && ((Map<String, Object>)listMap).get("id") != null && StringUtils.hasText(String.valueOf(((Map<String, Object>)listMap).get("id")))) {
					ReferenceDataInstance referenceDataInstance = createDataModelInstance(isQrCodeFlag, fromItem, toModelEntity, String.valueOf(((Map<String, Object>) listMap).get("id")), stringList, false);
					dataModelInstance.setValue(referenceDataInstance.getValue());
					List<Object> displayList = new ArrayList<>();
					displayList.add(referenceDataInstance.getDisplayValue());
					dataModelInstance.setDisplayValue(displayList);
					referenceDataModelList.add(dataModelInstance);
				}
			}else{
				List<String> valueList = new ArrayList<>();
				List<String> displayValueList = new ArrayList<>();
				if(listMap != null && ((List<Map<String, Object>>) listMap).size() > 0) {
					for (Map<String, Object> map : (List<Map<String, Object>>) listMap) {
						if (map.get("id") != null && StringUtils.hasText(String.valueOf(map.get("id")))) {
							ReferenceDataInstance referenceDataInstance = createDataModelInstance(isQrCodeFlag, fromItem, toModelEntity, String.valueOf(map.get("id")), stringList, false);
							if (referenceDataInstance != null && referenceDataInstance.getValue() != null) {
								valueList.add(String.valueOf(referenceDataInstance.getValue()));
								displayValueList.add(String.valueOf(referenceDataInstance.getDisplayValue()));
							}
						}
					}
					dataModelInstance.setValue(valueList);
					dataModelInstance.setDisplayValue(displayValueList);
					referenceDataModelList.add(dataModelInstance);
				}
			}
		}else if(fromItem.getReferenceType() == ReferenceType.ManyToMany || fromItem.getReferenceType() == ReferenceType.OneToMany ){
			List<String> values = new ArrayList<>();
			List<Object> idValues = new ArrayList<>();

			for(Map<String, Object> map  : (List<Map<String, Object>>)listMap) {
				idValues.add(map.get("id"));
				if(stringList != null) {
					FormInstance getFormInstance = getFormInstance(toModelEntity, String.valueOf(map.get("id")));
					List<String> arrayList = new ArrayList<>();
					Map<String, String> stringMap = new HashMap<>();
					for (ItemInstance itemInstance : getFormInstance.getItems()) {
						String value = getValue(stringList, itemInstance);
						if (StringUtils.hasText(value)) {
							stringMap.put(itemInstance.getId(), value);
						}
					}

					for (String string : stringList) {
						if (stringMap.get(string) != null && StringUtils.hasText(stringMap.get(string))) {
							arrayList.add(stringMap.get(string));
						}
					}
					values.add(String.join(",", arrayList));
				}
			}
			dataModelInstance.setValue(idValues);
			dataModelInstance.setDisplayValue(values);
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
			key = referenceItemModelEntity1.getColumnModel().getDataModel().getTableName()+"_"+referenceItemModelEntity1.getColumnModel().getColumnName()+"_list";
			if(fromItem.getReferenceType() == ReferenceType.OneToOne) {
				flag = true;
			}
		}else if(fromItem.getSelectMode() == SelectMode.Multiple){
			key = toModelEntity.getDataModels().get(0).getTableName()+"_list";
		}
		map.put(key,flag);
		return map;
	}

	private ReferenceDataInstance createDataModelInstance(boolean isQrCodeFlag, ReferenceItemModelEntity fromItem, FormModelEntity toModelEntity, String id, List<String> stringList, boolean referenceFlag){
		ReferenceDataInstance dataModelInstance = new ReferenceDataInstance();
		dataModelInstance.setValue(id);
		DataModelEntity dataModel = toModelEntity.getDataModels().get(0);
		Map<String, Object> map = getDataInfo(dataModel, id);
		FormDataSaveInstance formDataSaveInstance = wrapFormDataEntity(isQrCodeFlag, null, fromItem.getReferenceList(), map, id, referenceFlag);

		List<String> valueList = new ArrayList<>();
		Map<String, ItemInstance> valueMap = new HashMap<>();
		for(ItemInstance itemInstance : formDataSaveInstance.getItems()){
			valueMap.put(itemInstance.getId(), itemInstance);
		}
		for(String string : stringList){
			if(valueMap.get(string) != null) {
				String value = getValue(stringList, valueMap.get(string));
				if(StringUtils.hasText(value)) {
					valueList.add(value);
				}
			}
		}
		dataModelInstance.setDisplayValue(String.join(",", valueList));

		return dataModelInstance;
	}

	private String getValue(List<String> stringList ,ItemInstance itemInstance){
		if(stringList == null || !stringList.contains(itemInstance.getId())){
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
				return itemInstance.getDisplayValue() == null || itemInstance.getDisplayValue() == "" ? null : String.valueOf(itemInstance.getDisplayValue());
			}
		}
		return null;
	}

	private void setReferenceAttribute(ReferenceItemModelEntity fromItem,FormModelEntity toModelEntity, ColumnModelEntity columnModelEntity,
									   Map<String, Object> entity, List<DataModelInstance> referenceDataModelList){
		if(fromItem.getParentItem() == null || fromItem.getParentItem().getColumnModel() == null){
			return;
		}
		String key = fromItem.getParentItem().getColumnModel().getDataModel().getTableName()+"_"+fromItem.getParentItem().getColumnModel().getColumnName()+"_list";
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

	private void setFormDataReferenceAttribute(ReferenceItemModelEntity fromItem, Map<String, Object> entity, List<ItemInstance> items){
		if(fromItem.getReferenceItemId() == null || fromItem.getParentItem() == null){
			return;
		}
		ItemModelEntity itemModelEntity = itemModelManager.find(fromItem.getReferenceItemId());
		if(itemModelEntity == null || itemModelEntity.getColumnModel() == null){
			return;
		}
		String columnName = itemModelEntity.getColumnModel().getColumnName();
		String key = fromItem.getParentItem().getColumnModel().getColumnName();

		if(entity.get(key) == null){
			return;
		}

		ItemInstance itemModelInstance = new ItemInstance();
		itemModelInstance.setId(fromItem.getId());
		itemModelInstance.setType(fromItem.getType());
		itemModelInstance.setSystemItemType(fromItem.getSystemItemType());
		itemModelInstance.setVisible(true);
		itemModelInstance.setReadonly(true);
		itemModelInstance.setColumnModelId(itemModelEntity.getColumnModel().getId());
		itemModelInstance.setColumnModelName(columnName);
		itemModelInstance.setProps(fromItem.getProps());
		itemModelInstance.setItemName(fromItem.getName());

		if(fromItem.getParentItem().getReferenceType() == ReferenceType.ManyToMany){
			List<Map<String, Object>> listMap = (List<Map<String, Object>>)entity.get(key);
			if( listMap == null || listMap.size() == 0) {
				return;
			}
			List<String> displayValues = new ArrayList<>();
			List<Object> values = new ArrayList<>();
			for(Map<String, Object> map : listMap){
				ItemInstance itemInstance = new ItemInstance();
				updateValue(fromItem, itemInstance, map.get(columnName));
				displayValues.add(String.valueOf(itemInstance.getDisplayValue()));
				values.add(itemInstance.getValue());
			}
			itemModelInstance.setValue(values);
			itemModelInstance.setDisplayValue(String.join(",",displayValues));
			items.add(itemModelInstance);
		}else{
            Map<String, Object> mapData = null;
            if(fromItem.getParentItem() != null && (fromItem.getParentItem().getSystemItemType() == SystemItemType.Creator ||
					(fromItem.getParentItem().getCreateForeignKey() != null && !fromItem.getParentItem().getCreateForeignKey())) ){
            	FormModelEntity referenceFormModel = fromItem.getParentItem().getReferenceList().getMasterForm();
            	if(referenceFormModel == null && StringUtils.hasText(fromItem.getParentItem().getReferenceFormId())){
					referenceFormModel = formModelEntityJPAManager.find(fromItem.getParentItem().getReferenceFormId());
				}
		        DataModelEntity dataModelEntity = referenceFormModel.getDataModels().get(0);
                mapData = getDataInfo(dataModelEntity, String.valueOf(entity.get(key)));
            }else{
                mapData = (Map<String, Object>)entity.get(key);
            }
			if( mapData == null || mapData.size() == 0) {
				return;
			}
			ItemInstance itemInstance = new ItemInstance();
			updateValue(fromItem, itemInstance, mapData.get(columnName));
			itemModelInstance.setValue(itemInstance.getValue());
			itemModelInstance.setDisplayValue(itemInstance.getDisplayValue());
			items.add(itemModelInstance);
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
        if (itemModelEntity.getColumnModel()==null) {
            return;
        }
		DataModelEntity subFormDataModel = itemModelEntity.getColumnModel().getDataModel();
		Session subFormSession = getSession(subFormDataModel);
		try {
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

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (subFormSession!=null) {
				subFormSession.close();
				subFormSession = null;
			}
		}
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
		itemInstance.setColumnModelName(itemModel.getColumnModel().getColumnName());
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
				setTreeselectItemValue( itemModel,  itemInstance,  value);
				break;
			case Media:
				setFileItemInstance(value, itemInstance);
				break;
			case Attachment:
				setFileItemInstance(value, itemInstance);
				break;
			case Location:
				setLocationItemInstance(value, itemInstance);
				break;
			case ReferenceLabel:
				ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity)itemModel;
				if (StringUtils.hasText(referenceItemModelEntity.getReferenceItemId())) {
					ItemModelEntity referenceItem = itemModelManager.find(referenceItemModelEntity.getReferenceItemId());
					if (referenceItem != null) {
						updateValue(referenceItem, itemInstance, value);
					}
				}
				break;
			case ReferenceList:
				ReferenceItemModelEntity referenceItemModel = (ReferenceItemModelEntity)itemModel;
				FormModelEntity toModelEntity = referenceItemModel.getReferenceList() == null ?  null : referenceItemModel.getReferenceList().getMasterForm();
				List<String> stringList = Arrays.asList(referenceItemModel.getReferenceList().getDisplayItemsSort().split(","));
				if (toModelEntity!=null) {
					ReferenceDataInstance referenceDataInstance = createDataModelInstance(false, referenceItemModel, toModelEntity, String.valueOf(((Map<String, Object>) value).get("id")), stringList, false);
					itemInstance.setValue(referenceDataInstance.getValue());
					List<Object> displayList = new ArrayList<>();
					displayList.add(referenceDataInstance.getDisplayValue());
					itemInstance.setDisplayValue(displayList);
				}
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

	private void setTreeselectItemValue(ItemModelEntity itemModel, ItemInstance itemInstance, Object value){
		String valueStrs = value == null || StringUtils.isEmpty(value) ?  null : String.valueOf(value);
		String[] strings = valueStrs == null ? new String[]{} : valueStrs.split(",");
		List<String> ids  = strings == null ? new ArrayList<>() : Arrays.asList(strings);
		TreeSelectItemModelEntity treeSelectItem = (TreeSelectItemModelEntity)itemModel;
		if(treeSelectItem.getMultiple() != null && treeSelectItem.getMultiple()){
			itemInstance.setValue(ids);
		}else {
			itemInstance.setValue(valueStrs);
		}

		if(valueStrs != null && valueStrs.length() > 0) {
			List<TreeSelectData> list = getTreeSelectData(treeSelectItem.getDataSource(), valueStrs.split(","));
			if(list != null && list.size() > 0) {
				List<String> values = list.parallelStream().map(TreeSelectData::getName).collect(Collectors.toList());
				if(treeSelectItem.getMultiple() != null && treeSelectItem.getMultiple()) {
					itemInstance.setDisplayValue(values);
				}else{
					itemInstance.setDisplayValue(values.get(0));
				}
			}

		}
	}

	@Override
	public List<TreeSelectData> getTreeSelectData(TreeSelectDataSource dataSourceType, String[] ids) {
		if (ids==null || ids.length==0 || dataSourceType==null) {
			return new ArrayList<>();
		}
		List<TreeSelectData> list = new ArrayList<>();
		// 部门，岗位，人员，岗位标识
		if (TreeSelectDataSource.Department==dataSourceType || TreeSelectDataSource.Personnel==dataSourceType ||
			TreeSelectDataSource.Position==dataSourceType || TreeSelectDataSource.PositionIdentify==dataSourceType) {
			list = groupService.getTreeSelectDataSourceByIds(dataSourceType.getValue(), ids);
		// 系统代码
		} else if (TreeSelectDataSource.SystemCode==dataSourceType){
			List<DictionaryDataItemEntity> dictionaryItems = dictionaryService.findByItemIds(ids);
			if (dictionaryItems!=null && dictionaryItems.size()>0) {
				for (DictionaryDataItemEntity dictionaryItem:dictionaryItems) {
					TreeSelectData treeSelectData = new TreeSelectData();
					treeSelectData.setType(TreeSelectDataSource.SystemCode.getValue());
					treeSelectData.setId(dictionaryItem.getId());
					treeSelectData.setName(dictionaryItem.getName());
					list.add(treeSelectData);
				}
			}
		}
		return list;
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
				stringBuffer.append("#0.");
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
		SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)itemModel;
		List<String> displayValuelist = setSelectItemDisplayValue(itemInstance, selectItemModelEntity, list);

		// displayValuelist为空，说明在字典表里面已经删掉该内容，因此value也要设为空
		if (displayValuelist == null || displayValuelist.size() == 0) {
			itemInstance.setDisplayValue(null);
		}else{
			if(selectItemModelEntity.getSystemItemType() == SystemItemType.RadioGroup){
				itemInstance.setDisplayValue(displayValuelist.get(0));
			}if(selectItemModelEntity.getSystemItemType() == SystemItemType.CheckboxGroup || (selectItemModelEntity.getMultiple() != null && selectItemModelEntity.getMultiple())){
				itemInstance.setDisplayValue(displayValuelist);
			}else{
				itemInstance.setDisplayValue(displayValuelist.get(0));
			}
		}
	}

	public List<String> setSelectItemDisplayValue(ItemInstance itemInstance, SelectItemModelEntity selectItemModelEntity, List<String> list){
		if(list == null || list.size() < 1){
			return null;
		}
		List<String> displayValuelist = new ArrayList<>();
		List<Object> displayObjectList = new ArrayList<>();
		if((selectItemModelEntity.getSelectReferenceType() == SelectReferenceType.Dictionary ||
				selectItemModelEntity.getReferenceDictionaryItemId() != null || checkParentSelectItemHasDictionaryItem(selectItemModelEntity)) && list != null && list.size() > 0){
			List<DictionaryDataItemEntity> dictionaryItemEntities = dictionaryItemManager.query().filterIn("id",list).list();
			if(dictionaryItemEntities != null) {
				Map<String, DictionaryDataItemEntity> map = new HashMap<>();
				for (DictionaryDataItemEntity dictionaryItemEntity : dictionaryItemEntities) {
					map.put(dictionaryItemEntity.getId(), dictionaryItemEntity);
				}
				for(String str : list){
					DictionaryDataItemEntity dictionaryItemEntity = map.get(str);
					if(dictionaryItemEntity != null) {
						SelectItemModelValue selectItemModelValue = new SelectItemModelValue();
						selectItemModelValue.setCode(dictionaryItemEntity.getCode());
						selectItemModelValue.setIcon(dictionaryItemEntity.getIcon());
						selectItemModelValue.setDescription(dictionaryItemEntity.getName());
						displayObjectList.add(selectItemModelValue);
						displayValuelist.add(dictionaryItemEntity.getName());
					}
				}
			}
		}else if(selectItemModelEntity.getOptions() != null && selectItemModelEntity.getOptions().size() > 0) {
			for (ItemSelectOption option : selectItemModelEntity.getOptions()) {
				if (list.contains(option.getId())) {
					SelectItemModelValue selectItemModelValue = new SelectItemModelValue();
					selectItemModelValue.setCode(option.getValue());
					selectItemModelValue.setDescription(option.getLabel());
					displayObjectList.add(selectItemModelValue);
					displayValuelist.add(option.getLabel());
				}
			}
		}else if(selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.Dictionary_Model){
			//字典模型数据
			displayValuelist.add(dictionaryModelService.getDictionaryModelDataName(selectItemModelEntity.getReferenceDictionaryId(), Integer.parseInt(itemInstance.getId())));
		}else if(list != null){
			displayValuelist.add(String.join(",", list));
		}

		//设置控件的显示对象
		if(itemInstance != null) {
			itemInstance.setDisplayObject(displayObjectList);
		}

		return displayValuelist;
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

	private void setLocationItemInstance(Object value, ItemInstance itemInstance){
		String valueStr = value == null || StringUtils.isEmpty(value) ?  null : String.valueOf(value);
		if(valueStr != null) {
			List<String> idlist = Arrays.asList(valueStr.split(","));
			GeographicalMapModel mapModel = new GeographicalMapModel();
			List<GeographicalMapEntity> entityList = mapEntityJPAManager.query().filterIn("id", idlist).list();
			for(GeographicalMapEntity entity : entityList){
				GeographicalMapModel geographicalMapModel = new GeographicalMapModel();
				BeanUtils.copyProperties(entity, geographicalMapModel);
				mapModel = geographicalMapModel;
			}
			itemInstance.setValue(mapModel);
			itemInstance.setDisplayValue(mapModel);
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

	@SuppressWarnings("unchecked")
	@Override
	public FormDataSaveInstance getFormDataSaveInstance(FormModelEntity formModel, String id) {
		FormDataSaveInstance formInstance = null;
		try {
			DataModelEntity dataModel = formModel.getDataModels().get(0);
			Map<String, Object> map =  getDataInfo(dataModel, id);
			if(map == null || map.keySet() == null ||  map.keySet().size() < 1){
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+id+"】的数据");
			}
			formInstance = wrapFormDataEntity(false, formModel, null, map, id,true);
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ICityException){
				throw e;
			}
			throw new IFormException("没有查询到【" + formModel.getName() + "】表单，instanceId【"+id+"】的数据");
		}
		return formInstance;
	}

	@Override
	public Map<String, String> columnNameAndItemIdMap(List<ItemModelEntity> items) {
		Map<String, String> columnNameAndItemIdMap = new HashMap();
		if (items==null || items.size()==0) {
			return columnNameAndItemIdMap;
		}
		Set<String> itemIds = items.stream().map(item->item.getId()).collect(Collectors.toSet());
		String idArrStr = String.join("','", itemIds);
		List<Map<String, Object>> list = jdbcTemplate.queryForList("select ifm.column_name, iim.id item_id from ifm_item_model iim LEFT JOIN ifm_column_model ifm ON iim.column_id=ifm.id where iim.id in ('"+idArrStr+"')");
		if (list==null || list.size()==0) {
			return columnNameAndItemIdMap;
		}
		for (Map<String, Object> map:list) {
			columnNameAndItemIdMap.put((String)map.get("column_name"), (String)map.get("item_id"));
		}
		return columnNameAndItemIdMap;
	}


}
