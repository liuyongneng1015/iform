package tech.ascs.icity.iform.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.genericdao.search.Filter;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.hibernate.criterion.Order;
import org.hibernate.internal.CriteriaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.admin.api.model.*;
import tech.ascs.icity.admin.client.GroupService;
import tech.ascs.icity.admin.client.UserService;
import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iflow.api.model.Process;
import tech.ascs.icity.iflow.api.model.ProcessInstance;
import tech.ascs.icity.iflow.api.model.TaskInstance;
import tech.ascs.icity.iflow.api.model.WorkingTask;
import tech.ascs.icity.iflow.client.ProcessInstanceService;
import tech.ascs.icity.iflow.client.ProcessService;
import tech.ascs.icity.iflow.client.TaskService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.iform.utils.CommonUtils;
import tech.ascs.icity.iform.utils.CurrentUserUtils;
import tech.ascs.icity.iform.utils.InnerItemUtils;
import tech.ascs.icity.iform.utils.OkHttpUtils;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.NameEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.rbac.feign.model.UserInfo;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FormInstanceServiceExImpl extends DefaultJPAService<FormModelEntity> implements FormInstanceServiceEx {

	private static final Random random = new Random();

	private ObjectMapper objectMapper = new ObjectMapper();

	private final Logger logger = LoggerFactory.getLogger(FormInstanceServiceExImpl.class);

	private final static String phoneRegex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
	private final static String emailRegEx = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

	@Autowired
	private DictionaryDataService dictionaryDataService;

	@Autowired
	private DictionaryModelService dictionaryModelService;

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
	private ProcessService processService;

	@Autowired
	private ELProcessorService elProcessorService;

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

	public FormInstanceServiceExImpl() {
		super(FormModelEntity.class);
		InnerItemUtils.setReferenceDataHandler(this::createDataModelInstance);
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
			if (session != null) {
				session.close();
				session = null;
			}
		}
		return list;
	}

	@Override
	public List<FormDataSaveInstance> formInstance(ListModelEntity listModel, FormModelEntity formModel, Map<String, Object> queryParameters) {
		Session session = getSession(formModel.getDataModels().get(0));
		List<FormDataSaveInstance> list = new ArrayList<>();
		try {
			Criteria criteria = generateCriteria(session, formModel, listModel, queryParameters);
			list = wrapFormDataList(formModel, listModel, criteria.list());
		} catch (Exception e) {
			e.printStackTrace();
			new ICityException(e.getLocalizedMessage(), e);
		} finally {
			if (session != null) {
				session.close();
				session = null;
			}
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> flowFormInstance(ListModelEntity listModel,Map<String, Object> queryParameters) {
		Session session = null;
		try {
			FormModelEntity formModel = listModel.getMasterForm();
			String userId = (String)queryParameters.get("userId");
			Date beginDate = (Date)queryParameters.get("beginDate");
			Date endDate = (Date)queryParameters.get("endDate");
			Map<String, Object> parameters = new HashMap<>();
			session = getSession(formModel.getDataModels().get(0));
			boolean hasProcess = hasProcess(formModel);
			int processStatus = hasProcess ? getProcessStatusParameter(formModel, SystemItemType.ProcessStatus, parameters) : -1;
			int userStatus = hasProcess ? getProcessStatusParameter(formModel, SystemItemType.ProcessPrivateStatus, parameters) : -1;
			List<String> groupIds = hasProcess ? getGroupIds(userId) : null;

			Criteria criteria = generateColumnMapCriteria(session, formModel,  parameters);
			if (hasProcess) {
				addProcessCriteria(criteria, processStatus, userStatus, userId, groupIds, beginDate, endDate);
			}
			return criteria.list();
		} catch (Exception e) {
			e.printStackTrace();
			new ICityException(e.getLocalizedMessage(), e);
		} finally {
			if (session != null) {
				session.close();
				session = null;
			}
		}
		return null;
	}

	@Override
	public List<Map<String, Object>> findFormInstanceByColumnMap(FormModelEntity formModel, Map<String, Object> queryParameters) {
		Session session = getSession(formModel.getDataModels().get(0));
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			Criteria criteria = generateColumnMapCriteria(session, formModel, queryParameters);
			criteria.list();
		} catch (Exception e) {
			e.printStackTrace();
			new ICityException(e.getLocalizedMessage(), e);
		} finally {
			if (session != null) {
				session.close();
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
	public Page<FormDataSaveInstance> pageListInstance(ListModelEntity listModel, int page, int pagesize, Map<String, Object> queryParameters) {
		Page<FormDataSaveInstance> result = Page.get(page, pagesize);
		Session session = getSession(listModel.getMasterForm().getDataModels().get(0));
		try {
			boolean hasProcess = hasProcess(listModel.getMasterForm());
			int processStatus = hasProcess ? getProcessStatusParameter(listModel.getMasterForm(), SystemItemType.ProcessStatus, queryParameters) : -1;
			int userStatus = hasProcess ? getProcessStatusParameter(listModel.getMasterForm(), SystemItemType.ProcessPrivateStatus, queryParameters) : -1;
			Process process = hasProcess ? processService.get(listModel.getMasterForm().getProcess().getKey()) : null;
			String userId = hasProcess ? (queryParameters.get("userId") == null ? CurrentUserUtils.getCurrentUser().getId() : (String)queryParameters.get("userId")) : null;
			List<String> groupIds = hasProcess ? getGroupIds(userId) : null;

			Criteria criteria = generateCriteria(session, listModel.getMasterForm(), listModel, queryParameters);
			addCreatorCriteria(criteria, listModel);
			if (hasProcess) {
				addProcessCriteria(criteria, processStatus, userStatus, userId, groupIds, null, null);
			}
			addSort(listModel, criteria);

			criteria.setFirstResult((page - 1) * pagesize);
			criteria.setMaxResults(pagesize);

			List<Map<String, Object>> data = criteria.list();
			if (process != null) {
				data.forEach(entity -> {
					entity.put("process", process);
					entity.put("userId", userId);
					entity.put("groupIds", groupIds);
				});
			}
			List<FormDataSaveInstance> list = wrapFormDataList(listModel.getMasterForm(), listModel, data);

			criteria.setFirstResult(0);
			criteria.setProjection(Projections.rowCount());

			// 清除排序字段
			for (Iterator<CriteriaImpl.OrderEntry> i = ((CriteriaImpl) criteria).iterateOrderings(); i.hasNext(); ) {
				i.next();
				i.remove();
			}
			Number count = (Number) criteria.uniqueResult();

			result.data(count.intValue(), list);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ICityException) {
				throw e;
			}
			throw new IFormException(e.getLocalizedMessage(), e);
		} finally {
			if (session != null) {
				session.close();
				session = null;
			}
		}
		return result;
	}

	@Override
	public Page<String> pageByTableName(String tableName, int page, int pagesize) {
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
		int count = jdbcTemplate.queryForObject(countSql.toString(), Integer.class);

		StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
		String pageSql = buildPageSql(sql.toString(), page, pagesize);
		List<String> list = jdbcTemplate.queryForList(pageSql, String.class);

		Page<String> result = Page.get(page, pagesize);
		return result.data(count, list);
	}


	private List<String> listByTableName(ItemType itemType, String tableName, String key, Object value) {
		StringBuffer params = new StringBuffer("'");
		if (value instanceof List) {
			List<String> valueList = new ArrayList<>();
			if (itemType == ItemType.Media || itemType == ItemType.Attachment) {
				List<FileUploadModel> maplist = (List<FileUploadModel>) value;
				for (FileUploadModel fileUploadModel : maplist) {
					valueList.add(fileUploadModel.getId());
				}
			} else {
				valueList = (List) value;
			}
			params.append(String.join(",", valueList));
		} else {
			params.append(String.valueOf(value));
			if (itemType == ItemType.Media || itemType == ItemType.Attachment) {
				params.append(((FileUploadModel) value).getId());
			}
		}
		params.append("'");
		StringBuilder sql = new StringBuilder("SELECT id FROM ").append(tableName).append(" where ").append(key).append("=" + params.toString());
		if (itemType == ItemType.InputNumber) {
			sql = new StringBuilder("SELECT id FROM ").append(tableName).append(" where ").append(key).append("=" + value);
		}

		List<String> list = jdbcTemplate.queryForList(sql.toString(), String.class);
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

		setFormInstanceModel(formInstance, formModel, new HashMap<>(), true);
		return formInstance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FormInstance getFormInstance(FormModelEntity formModel, String instanceId) {

		FormInstance formInstance = null;
		try {
			DataModelEntity dataModel = formModel.getDataModels().get(0);
			Map<String, Object> map = getDataInfo(dataModel, instanceId);
			if (map == null || map.keySet() == null) {
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【" + instanceId + "】的数据");
			}
			formInstance = wrapEntity(formModel, map, instanceId, true);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ICityException) {
				throw e;
			}
			throw new IFormException("没有查询到【" + formModel.getName() + "】表单，instanceId【" + instanceId + "】的数据");
		}
		return formInstance;
	}

	@Override
	public FormDataSaveInstance getQrCodeFormDataSaveInstance(ListModelEntity listModel, String instanceId) {
		FormDataSaveInstance formInstance = null;
		Map<String, Object> map = null;
		try {
			DataModelEntity dataModel = listModel.getMasterForm().getDataModels().get(0);
			map = getDataInfo(dataModel, instanceId);
			if (map == null || map.keySet() == null) {
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【" + instanceId + "】的数据");
			}
			formInstance = wrapQrCodeFormDataEntity(true, listModel, map, instanceId, true);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ICityException) {
				throw e;
			}
			throw new IFormException("没有查询到【" + listModel.getMasterForm().getName() + "】表单，instanceId【" + instanceId + "】的数据");
		}
		if(map != null && listModel.getMasterForm() != null && listModel.getMasterForm().getProcess() != null && formInstance.getProcessInstanceId() != null){
			setFormInstanceProcessStatus(listModel.getMasterForm(), map, formInstance);
		}
		return formInstance;
	}

	private Map<String, Object> getDataInfo(DataModelEntity dataModel, String instanceId) {
		Session session = null;
		Map<String, Object> map = new HashMap<>();
		try {
			session = getSession(dataModel);
			if (!StringUtils.hasText(instanceId)) {
				return map;
			}
			map = (Map<String, Object>) session.get(dataModel.getTableName(), instanceId);
			if (map == null || map.keySet() == null || map.keySet().size() < 1) {
				map = new HashMap<>();
				//throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (session != null) {
				session.close();
				session = null;
			}
		}
		return map;
	}

	private Map<String, Object> createDataModel(DataModelEntity dataModel, String instanceId) {
		Session session = getSession(dataModel);
		Map<String, Object> map = null;
		try {
			map = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
			if (map == null || map.keySet() == null) {
				throw new IFormException("没有查询到【" + dataModel.getTableName() + "】表，id【" + instanceId + "】的数据");
			}

			for (String key : map.keySet()) {
				map.put(dataModel.getTableName() + "_" + key, map.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
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
		for (ItemInstance itemInstance : list) {
			itemMap.put(itemInstance.getId(), itemInstance);
		}

		// 表单提交校验
		List<String> checkResult = elProcessorService.checkSubmitProcessor(itemMap, formModelEntity.getSubmitChecks());
		if (!checkResult.isEmpty()) {
			throw new IFormException(403, checkResult.get(0));
		}

		UserInfo user = null;
		try {
			user = CurrentUserUtils.getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
		itemModelEntityList.addAll(formModelEntity.getItems());
		for (ItemModelEntity itemModelEntity : formModelEntity.getItems()) {
			if (itemModelEntity instanceof RowItemModelEntity) {
				itemModelEntityList.addAll(((RowItemModelEntity) itemModelEntity).getItems());
			}
		}
		for (ItemModelEntity itemModelEntity : itemModelEntityList) {
			setItemInstance(itemModelEntity, itemMap, list, user);
		}

		return saveFormData(formModel.getDataModels().get(0), formInstance, user, formModelEntity, formModel);
	}

	private void setItemInstance(ItemModelEntity itemModelEntity, Map<String, ItemInstance> itemMap, List<ItemInstance> list, UserInfo user) {
		if (itemModelEntity.getSystemItemType() == SystemItemType.CreateDate && ((TimeItemModelEntity) itemModelEntity).getCreateType() == SystemCreateType.Create) {
			if (itemMap.keySet().contains(itemModelEntity.getId())) {
				list.remove(itemMap.get(itemModelEntity.getId()));
			}

			list.add(getItemInstance(itemModelEntity.getId(), getNowTime(((TimeItemModelEntity) itemModelEntity).getTimeFormat())));

		} else if (itemModelEntity.getSystemItemType() == SystemItemType.Creator && ((ReferenceItemModelEntity) itemModelEntity).getCreateType() == SystemCreateType.Create) {
			if (itemMap.keySet().contains(itemModelEntity.getId())) {
				list.remove(itemMap.get(itemModelEntity.getId()));
			}
			list.add(getItemInstance(itemModelEntity.getId(), user != null ? user.getId() : null));
		} else if (itemModelEntity.getSystemItemType() == SystemItemType.SerialNumber) {
			if (itemMap.keySet().contains(itemModelEntity.getId())) {
				list.remove(itemMap.get(itemModelEntity.getId()));
			}
			String format = ((SerialNumberItemModelEntity) itemModelEntity).getTimeFormat();
			String prefix = ((SerialNumberItemModelEntity) itemModelEntity).getPrefix();
			StringBuffer str = new StringBuffer(!StringUtils.hasText(prefix) ? "" : prefix);
			str.append("_");
			if (format != null) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
				str.append(simpleDateFormat.format(new Date()));
				str.append("_");
			}
			str.append(getRandom(((SerialNumberItemModelEntity) itemModelEntity).getSuffix()));
			list.add(getItemInstance(itemModelEntity.getId(), str.toString()));
		}
	}

	private String saveFormData(DataModelEntity dataModel, FormDataSaveInstance formInstance, UserInfo user, FormModelEntity formModelEntity, FormModelEntity formModel) {
		List<Map<String, Object>> assignmentList = new ArrayList<>();
		String paramCondition = verifyDataRequired(assignmentList, formInstance, formModelEntity, DisplayTimingType.Add);
		Session session = null;
		String newId = null;
		Map<String, Object> data = new HashMap();
		try {
			session = getSession(dataModel);
			session.beginTransaction();

			//主表数据
			setMasterFormItemInstances(formInstance, data, DisplayTimingType.Add);
			data.put("create_at", new Date());
			data.put("create_by", user != null ? user.getId() : null);
			//流程参数
			data.put("PROCESS_ID", formInstance.getProcessId());
			data.put("ACTIVITY_ID", formInstance.getActivityId());
			data.put("ACTIVITY_INSTANCE", formInstance.getActivityInstanceId());
			if (StringUtils.hasText(formInstance.getProcessInstanceId())) {
				Map<String, Object> processInstance = new HashMap<>();
				//Map<String, Object> subFormMap =(Map<String, Object>) session.load(newDataList1.getTableName(), String.valueOf(map.get("id")));
				//subFormMap.put("master_id", data);
				processInstance.put("id", formInstance.getProcessInstanceId());
				data.put("processInstance", processInstance);
			}

			//设置子表数据
			setSubFormReferenceData(session, user, formInstance, data, DisplayTimingType.Add);
			DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
			String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName() : dataModelEntity.getPrefix() + dataModelEntity.getTableName();
			//关联表数据
			saveReferenceData(user, formInstance, data, session, tableName, formModelService.findAllItems(formModelEntity), DisplayTimingType.Add);

			// before
			sendWebService(formModelEntity, BusinessTriggerType.Add_Before, data, formInstance.getId());

			newId = (String) session.save(dataModel.getTableName(), data);
			data.put("id", newId);
			// 启动流程
			if (formModel.getProcess() != null && formModel.getProcess().getKey() != null) {
				startProces(assignmentList, paramCondition, formInstance, data, formModel, newId, user);
			}

			session.getTransaction().commit();
			// after
			sendWebService(formModelEntity, BusinessTriggerType.Add_After, data, newId);

		} catch (Exception e) {
			if (e instanceof ICityException) {
				throw e;
			}
			logger.error("saveFormData add error with data=[" + OkHttpUtils.mapToJson(data) + "]");
			e.printStackTrace();
			throw new IFormException("保存数据失败，"+e.getLocalizedMessage());
		} finally {
			if (session != null) {
				session.close();
				session = null;
			}
		}
		return newId;
	}

	// 启动流程，工作流要取出字典表ID对应的数据，不是字典表的ID
	private void startProces(List<Map<String, Object>> assignmentList, String paramCondition, FormDataSaveInstance formInstance, Map<String, Object> data, FormModelEntity formModel, String newId, UserInfo user) {
		Map<String, ListFunction> listFunctions = new HashMap<>();
		for (ListFunction listFunction : formModel.getFunctions()) {
			if (listFunction.getFunctionType() != null) {
				listFunctions.put(listFunction.getFunctionType().getValue(), listFunction);
			}
		}
		if (listFunctions == null || !listFunctions.keySet().contains(ListFunctionType.StartProcess.getValue())) {
			return;
		}

		Map<String, Object> flowData = formInstance.getFlowData();
		if (flowData == null) {
			flowData = new HashMap<>();
		}
		flowData.putAll(data);

		//启动流程带入表单数据
		flowData.put("formId", formModel.getId());
		flowData.put("id", newId);

		//跳过第一个流程环节
		flowData.put("PASS_THROW_FIRST_USERTASK", true);

		//上一个流程环节数据
		TaskInstance taskInstance = null;
		if (StringUtils.hasText(formInstance.getProcessInstanceId())) {
			ProcessInstance processInstance = processInstanceService.get(formInstance.getProcessInstanceId());
			taskInstance = processInstance.getCurrentTaskInstance();
		}
		setColumnValue(assignmentList, flowData, data, user, taskInstance);

		flowData = toProcesDictionaryData(flowData, formModel);

		System.out.println("新增传给工作流的数据=====>>>>>" + OkHttpUtils.mapToJson(flowData));
		String processInstanceId = processInstanceService.startProcess(formModel.getProcess().getKey(), newId, flowData);
		updateProcessInfo(assignmentList, formModel, data, processInstanceId);
	}

	// 把主表单选控件的字典表的数据封装好对应的ID，name，code值，传给工作流
	private Map<String, Object> toProcesDictionaryData(Map<String, Object> flowData, FormModelEntity formModel) {
		Map<String, Object> returnMap = new HashMap(flowData);
		Map<String, ItemModelEntity> columnNameAndItemModelMap = new HashMap<>();
		List<ItemModelEntity> list = formModelService.findAllItems(formModel);
		for (ItemModelEntity itemModel : list) {
			ColumnModelEntity columnModel = itemModel.getColumnModel();
			if (columnModel != null) {
				columnNameAndItemModelMap.put(columnModel.getColumnName(), itemModel);
			}
		}
		for (String key : flowData.keySet()) {
			Object value = flowData.get(key);
			if(value == null){
				returnMap.put(key, null);
				continue;
			}
			ItemModelEntity itemModel = columnNameAndItemModelMap.get(key);
			if (itemModel == null) {
				returnMap.put(key, getValueStr(value));
				continue;
			}
			if (itemModel instanceof SelectItemModelEntity) {
				SelectItemModelEntity selectItemModel = (SelectItemModelEntity) itemModel;
				List<String> listString = new ArrayList<>();
				if (value instanceof List) {
					listString = (List<String>) value;
				} else {
					listString.add((String) value);
				}
				List<String> labelStrlList = new ArrayList<>();
				List<String> idStrlList = new ArrayList<>();
				List<String> valueStrlList = new ArrayList<>();

				for (String valueStr : listString) {
					SelectReferenceType selectReferenceType = selectItemModel.getSelectReferenceType();
					if (SelectReferenceType.Fixed == selectReferenceType) {
						List<ItemSelectOption> options = selectItemModel.getOptions();
						if (options == null) {
							continue;
						}
						Optional<ItemSelectOption> optional = options.stream().filter(item -> item.getId().equals(valueStr)).findFirst();
						if (optional.isPresent()) {
							labelStrlList.add(optional.get().getLabel());
							idStrlList.add(optional.get().getId());
							valueStrlList.add(optional.get().getValue());
						}
					} else if (SelectReferenceType.Dictionary == selectReferenceType) {
						if (selectItemModel.getSelectDataSourceType() == SelectDataSourceType.DictionaryData) {
							DictionaryDataItemEntity dictionaryDataItemEntity = dictionaryDataService.getDictionaryItemById(valueStr);
							if (dictionaryDataItemEntity == null) {
								continue;
							}
							labelStrlList.add(dictionaryDataItemEntity.getCode());
							idStrlList.add(dictionaryDataItemEntity.getId());
							valueStrlList.add(dictionaryDataItemEntity.getName());
						} else if (selectItemModel.getSelectDataSourceType() == SelectDataSourceType.DictionaryModel) {
							String referenceDictionaryId = selectItemModel.getReferenceDictionaryId();
							DictionaryModelData dictionaryModelData = dictionaryModelService.getDictionaryModelDataById(referenceDictionaryId, valueStr);
							if (dictionaryModelData == null) {
								continue;
							}
							labelStrlList.add(dictionaryModelData.getCode());
							idStrlList.add(dictionaryModelData.getId());
							valueStrlList.add(dictionaryModelData.getName());
						}
					}
				}
				returnMap.put(key, String.join(",", labelStrlList));
				returnMap.put(key + "_id", String.join(",", idStrlList));
				returnMap.put(key + "_name", String.join(",", valueStrlList));
			} else if (itemModel instanceof FileItemModelEntity || itemModel instanceof LocationItemModelEntity) {
				List<String> valueString = new ArrayList<>();
				if (value instanceof List) {
					for (Map fileUploadModel : (List<Map>) value) {
						valueString.add((String) fileUploadModel.get("id"));
					}
				} else if (value instanceof Map) {
					valueString.add((String) ((Map) value).get("id"));
				}
				returnMap.put(key, String.join(",", valueString));
			} else if (itemModel instanceof SubFormItemModelEntity) {
				returnMap.put(key, getValueStr(value));
			}else{
				if (value instanceof List) {
					List<String> valueString = new ArrayList<>();
					for (String string : (List<String>) value) {
						valueString.add(string);
					}
					returnMap.put(key, String.join(",", valueString));
				} else if(value instanceof String){
					returnMap.put(key, (String) value);
				}else{
					returnMap.put(key, value);
				}
			}
		}
		return returnMap;
	}

	private String getValueStr(Object value){
		if(value == null){
			return null;
		}
		try {
			Object object = null;
			if(value instanceof Map){
				Map<String, Object> map = new HashMap<>();
				for(String keyStr : ((Map<String, Object>)value).keySet()){
					Object o = ((Map<String, Object>)value).get(keyStr);
					if(o instanceof Map || o instanceof List){
						continue;
					}
					map.put(keyStr, o);
				}
				object = map;
			}else if(value instanceof List){
				List<Map<String, Object>> mapList = new ArrayList<>();
				for(Map<String, Object> map : (List<Map<String, Object>>)value){
					Map<String, Object> mapdata = new HashMap<>();
					for(String keyStr : map.keySet()) {
						Object o = map.get(keyStr);
						if (o instanceof Map || o instanceof List) {
							continue;
						}
						mapdata.put(keyStr, o);
					}
					mapList.add(mapdata);
				}
				object = mapList;
			}else{
				return String.valueOf(value);
			}
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
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
		Object functionid = formInstance.getFlowData() == null ? null: formInstance.getFlowData().get("functionId");
		//是否退回
		boolean isBack = formInstance.getFlowData() != null && "prev".equals(formInstance.getFlowData().get("circalation"));
	    List<Map<String, Object>> assignmentList = new ArrayList<>();
		String paramCondition = verifyDataRequired( assignmentList, formInstance, formModelEntity, DisplayTimingType.Update);
		Session session = null;
		Map<String, Object> data = null;
		try {
			session = getSession(dataModel);
			//开启事务
			session.beginTransaction();

			data = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);

			//主表数据
			setMasterFormItemInstances(formInstance, data, DisplayTimingType.Update);
			data.put("update_at", new Date());
			data.put("update_by",  user != null ? user.getId() : null);

			setSubFormReferenceData(session, user, formInstance, data, DisplayTimingType.Update);
			DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
			String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
			//关联表数据
			saveReferenceData(user, formInstance, data,  session,  tableName, formModelService.findAllItems(formModelEntity), DisplayTimingType.Update);

			// before
			sendWebService( formModelEntity, BusinessTriggerType.Update_Before, data, instanceId);

			// 流程操作
			if (StringUtils.hasText(formInstance.getActivityInstanceId()) && functionid != null) {
				completedProcess(assignmentList, paramCondition, formInstance, data, formModel, user, isBack);
			}
			session.update(dataModel.getTableName(), data);
			session.getTransaction().commit();

			// after
			sendWebService( formModelEntity, BusinessTriggerType.Update_After, data, instanceId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("saveFormData error with data=["+OkHttpUtils.mapToJson(data)+"]");
			if(e instanceof ICityException){
				throw e;
			}
			throw new IFormException("保存【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据失败");
		} finally {
			if(session != null){
				session.close();
			}
		}
	}

	//判断数据是否必填
	private String verifyDataRequired(List<Map<String, Object>> assignmentList, FormDataSaveInstance formInstance, FormModelEntity formModelEntity, DisplayTimingType displayTimingType){
		//不能为空的数据
		Map<String, ItemModelEntity> notNullIdMap = new HashMap();

		//表单数据
		Map<String, ItemInstance> formItemInstanceMap = new HashMap();
		for(ItemInstance itemInstance : formInstance.getItems()){
			formItemInstanceMap.put(itemInstance.getId(), itemInstance);
		}

		List<String> idList = new ArrayList<>();
		//参数类型
		String paramCondition = null;
		//流程字段名称
		if(StringUtils.hasText(formInstance.getProcessInstanceId())) {
			if(formInstance.getProcessInstanceId() != null && formInstance.getFlowData() != null && formInstance.getFlowData().get("functionId") != null) {
				ProcessInstance processInstance = processInstanceService.get(formInstance.getProcessInstanceId());
				if(processInstance.getCurrentTaskInstance() != null && processInstance.getCurrentTaskInstance().getOperations() != null) {
					for (Map<String, Object> map : (List<Map<String,Object>>) processInstance.getCurrentTaskInstance().getOperations()) {
						if (!map.get("id").equals(formInstance.getFlowData().get("functionId"))) {
							continue;
						}
						Map<String, Object> funcPropsMap = (Map<String, Object>) map.get("funcProps");
						if (funcPropsMap != null) {
							if(funcPropsMap.get("paramCondition") != null) {
								if (funcPropsMap.get("paramCondition") instanceof List) {
									paramCondition = String.join(",", (List<String>) funcPropsMap.get("paramCondition"));
								} else {
									paramCondition = (String) funcPropsMap.get("paramCondition");
								}
							}
							if(funcPropsMap.get("itemValue") != null) {
								assignmentList.addAll((List<Map<String, Object>>) funcPropsMap.get("itemValue"));
							}
							for(Map<String, Object> permissionsMap : (List<Map<String, Object>>) funcPropsMap.get("permissions")) {
								if (permissionsMap.get("required") != null && (Boolean) permissionsMap.get("required")) {
									ItemModelEntity itemModelEntity = itemModelManager.find((String) permissionsMap.get("id"));
									notNullIdMap.put((String) permissionsMap.get("id"), itemModelEntity);
								}
								idList.add((String) permissionsMap.get("id"));
							}
						}
					}
				}
			}
		} else {
			for(ItemModelEntity itemModelEntity : formModelService.findAllItems(formModelEntity)) {
				for (ItemPermissionInfo itemPermissionInfo :itemModelEntity.getPermissions()) {
					if(itemPermissionInfo.getDisplayTiming() == displayTimingType && itemPermissionInfo.getRequired() != null && itemPermissionInfo.getRequired()){
						notNullIdMap.put(itemModelEntity.getId(), itemModelEntity);
					}
				}
			}
		}

		for(ItemInstance itemInstance : formInstance.getItems()){
			if(itemInstance.getValue() != null && String.valueOf(itemInstance.getValue()) != null && !String.valueOf(itemInstance.getValue()).equals("null")){
				notNullIdMap.remove(itemInstance.getId());
			}
		}

		// 去掉关联属性中已填内容的字段
		for(ReferenceDataInstance referenceDataInstance : formInstance.getReferenceData()) {
			if (StringUtils.hasText(referenceDataInstance.getId()) && referenceDataInstance.getValue()!=null) {
				notNullIdMap.remove(referenceDataInstance.getId());
			}
		}

		if(notNullIdMap.size() > 0) {
			// 测试提的bug，校验字段非空时，要按照表单建模的控件顺序来校验
			List<ItemModelEntity> items = new ArrayList<>();
			for (ItemModelEntity itemModelEntity:notNullIdMap.values()) {
				items.add(itemModelEntity);
			}
			items = items.stream().sorted((o1, o2) -> {
				if (o1.getOrderNo()!=null && o2.getOrderNo()!=null) {
					return o1.getOrderNo()-o2.getOrderNo();
				} else {
					return o1.getOrderNo() == null ? -1 : 1;
				}
			}).collect(Collectors.toList());

			for (ItemModelEntity itemModelEntity:items) {
				if (notNullIdMap.containsKey(itemModelEntity.getId())) {
					throw new IFormException(itemModelEntity.getName() +"的值不允许为空");
				}
			}
			throw new IFormException("存在空的字段");
		}

		Map<String, Object> flowData = formInstance.getFlowData();
		if(flowData.containsKey("functionId")){
			flowData.remove("functionId");
		}
		for(String id : idList) {
			ItemModelEntity itemModelEntity = itemModelManager.find(id);
			if(itemModelEntity == null){
				continue;
			}
			if(flowData.containsKey(id)){
				Object value = flowData.get(id);
				veryInputValue(itemModelEntity, value);
				flowData.put(itemModelEntity.getColumnModel().getColumnName(), value);
			}else{
				ItemInstance itemInstance = formItemInstanceMap.get(id);
				if(itemInstance != null) {
					veryInputValue(itemModelEntity, itemInstance.getValue());
					flowData.put(itemModelEntity.getColumnModel().getColumnName(), itemInstance.getValue());
				}
			}
		}
		formInstance.setFlowData(flowData);
		return paramCondition;
	}

	//校验文本框的值
	private void veryInputValue(ItemModelEntity itemModel, Object value){
		if(itemModel.getType() == ItemType.Input){
			if("phone".equals(itemModel.getTypeKey())){
				Pattern p = Pattern.compile(phoneRegex);
				Matcher m = p.matcher(String.valueOf(value));
				if(!m.matches()){
					throw  new IFormException(itemModel.getName()+"数据格式错误");
				}
			}else if("email".equals(itemModel.getTypeKey())){
				Pattern p = Pattern.compile(emailRegEx);
				Matcher m = p.matcher(String.valueOf(value));
				if(!m.matches()){
					throw  new IFormException(itemModel.getName()+"数据格式错误");
				}
			}
		}
	}

	//完成当前任务
	private void completedProcess(List<Map<String, Object>> assignmentList, String paramCondition, FormDataSaveInstance formInstance, Map<String, Object> data, FormModelEntity formModel, UserInfo user, boolean isBack){
		String comment = (String)data.get("comment_");
		Map<String, Object> flowData = formInstance.getFlowData();
		if(flowData == null){
			flowData = new HashMap<>();
		}
		String functionType = (String)flowData.get("functionType");
		FlowFunctionType flowFunctionType = FlowFunctionType.getTypeByValue(functionType);

		flowData.remove("circalation");
		if(paramCondition != null && paramCondition.contains(ParamCondition.FormCurrentData.getValue())) {
			flowData.putAll(data);
		}
		flowData.put("formId", formModel.getId());
		flowData.put("id", formInstance.getId());
		//上一个流程环节数据
		TaskInstance taskInstance = null;
		if(StringUtils.hasText(formInstance.getProcessInstanceId())) {
			ProcessInstance processInstance = processInstanceService.get(formInstance.getProcessInstanceId());
			taskInstance = processInstance.getCurrentTaskInstance();
		}
		setColumnValue(assignmentList, flowData, data, user, taskInstance);
		flowData = toProcesDictionaryData(flowData, formModel);
		System.out.println("更新时传给工作流的数据=====>>>>>"+OkHttpUtils.mapToJson(flowData));
		if(flowFunctionType == null || FlowFunctionType.InvokeService == flowFunctionType
				|| FlowFunctionType.JumpURL == flowFunctionType || FlowFunctionType.ChangeState == flowFunctionType){
			if(StringUtils.hasText(comment)){
				taskService.addComment(formInstance.getActivityInstanceId(), comment);
			}
		}else if(FlowFunctionType.Sign == flowFunctionType){
			if(StringUtils.hasText(comment)){
				taskService.addComment(formInstance.getActivityInstanceId(), comment);
			}
			taskService.signTask(formInstance.getActivityInstanceId());
		}else if(isBack) {
			//退回
			taskService.returnTask(formInstance.getActivityInstanceId(), null, comment);
		}else {
			taskService.completeTask(formInstance.getActivityInstanceId(), flowData);
		}
		updateProcessInfo(assignmentList, formModel, data, formInstance.getProcessInstanceId());
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
					ItemModelEntity itemModel = itemModelManager.find(itemModelService.getId());
					if(itemModel != null && itemModelService.getValue() != null && StringUtils.hasText(String.valueOf(itemModelService.getValue())) &&
							(itemModel.getType() == ItemType.SubForm || itemModel.getColumnModel() != null && itemModel.getColumnModel().getColumnName().equals("id"))){
						map = (Map<String, Object>)subFormSession.load(dataModelEntity.getTableName(), (String)itemModelService.getValue());
						break;
					}
				}
			}
			//唯一校验
			Map<String, String> uniqueneItem = new HashMap<String, String>();
			for (SubFormRowItemInstance instance : subFormDataItemInstance.getItems()) {
				for (ItemInstance itemModelService : instance.getItems()) {
					ItemModelEntity itemModel = itemModelManager.find(itemModelService.getId());
					if(itemModel == null){
						continue;
					}
					if((itemModel instanceof ReferenceItemModelEntity) && itemModel.getType() != ItemType.ReferenceLabel ){
						referenceItemModelEntityList.add(itemModel);
					}
					if(itemModel.getUniquene() != null && itemModel.getUniquene()){
						String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
						ColumnModelEntity column = itemModel.getColumnModel();
						String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
						List<String> list = listByTableName(itemModelService.getType(), tableName, columnName, itemModelService.getValue());
						String itemKey = itemModel.getId()+"_"+itemModel.getName();
						if(list != null && list.size() > 0) {
							stringListMap.put(itemKey, list);
						}
						String  uniqueneItemValue = uniqueneItem.get(itemKey);
						if(itemModelService.getValue() != null && StringUtils.hasText(String.valueOf(itemModelService.getValue()))){
							if( uniqueneItemValue != null &&  uniqueneItemValue.equals(String.valueOf(itemModelService.getValue()))) {
								throw new IFormException(itemModel.getName() + "必须唯一");
							}else{
								uniqueneItemValue = String.valueOf(itemModelService.getValue());
							}
							uniqueneItem.put(itemKey, uniqueneItemValue);
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
				String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
				saveReferenceData(user, formDataSaveInstance, map, subFormSession, tableName, referenceItemModelEntityList, displayTimingType);
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

		ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity) itemModelManager.find(dataModelInstance.getId());

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
			ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemModelManager.find(referenceItemModelEntity.getReferenceItemId());
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
			ItemModelEntity itemModel = itemModelManager.find(itemInstance.getId());
			if(itemModel != null && itemModel.getName().equals("id")){
				itemInstance.setValue(formInstance.getId());
				itemInstance.setDisplayValue(formInstance.getId());
				idItemInstance = itemInstance;
			}
		}
		for (ItemInstance itemInstance : formInstance.getItems()) {
            ItemModelEntity itemModel = itemModelManager.find(itemInstance.getId());
            if(itemModel == null){
            	continue;
			}
            if(itemModel instanceof ReferenceItemModelEntity || itemModel.getSystemItemType() == SystemItemType.ID
					|| itemModel instanceof SubFormItemModelEntity || itemModel instanceof TabsItemModelEntity || itemModel instanceof RowItemModelEntity
					|| itemModel instanceof TabPaneItemModelEntity || itemModel instanceof SubFormRowItemModelEntity){
            	continue;
			}
            //唯一校验
            if(itemModel.getUniquene() != null && itemModel.getUniquene() &&itemModel.getColumnModel() != null && itemModel.getColumnModel().getDataModel() != null){
            	DataModelEntity dataModelEntity = itemModel.getColumnModel().getDataModel();
				String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
				ColumnModelEntity column = itemModel.getColumnModel();
				String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
               List<String> list = listByTableName(itemModel.getType(), tableName, columnName, itemInstance.getValue());

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
		//非流程表单校验字段值
		if(!StringUtils.hasText(itemInstance.getProcessInstanceId())) {
			verifyValue(itemModel, itemInstance.getValue(), displayTimingType);
		}
		if(itemInstance.getValue() == null || StringUtils.isEmpty(itemInstance.getValue())){
			itemInstance.setValue(null);
		}
		if (itemModel.getType() == ItemType.DatePicker || itemModel.getSystemItemType() == SystemItemType.CreateDate) {
			try {
				value = itemInstance.getValue() == null || !StringUtils.hasText(String.valueOf(itemInstance.getValue())) ? null : new Date(Long.parseLong(String.valueOf(itemInstance.getValue())));
			} catch (Exception e) {
				throw new IFormException(itemModel.getName() + "控件值类型不匹配");
			}
		} else if (itemModel.getType() == ItemType.Select || itemModel.getType() == ItemType.RadioGroup
				|| itemModel.getType() == ItemType.CheckboxGroup || itemModel.getType() == ItemType.Treeselect ) {
            Object o = itemInstance.getValue();
            if(o != null && o instanceof List){
                value = String.join(",", (List)o );
            }else{
                value = o == null || StringUtils.isEmpty(o) ? null : String.valueOf(o);
            }
		} else if (itemModel.getType() == ItemType.InputNumber  && itemInstance.getValue() != null) {
			BigDecimal bigDecimal = new BigDecimal(String.valueOf(itemInstance.getValue()));
			if(((NumberItemModelEntity)itemModel).getDecimalDigits() != null  && ((NumberItemModelEntity)itemModel).getDecimalDigits() > 0 ) {
				value = bigDecimal.divide(new BigDecimal(1.0), ((NumberItemModelEntity) itemModel).getDecimalDigits(), BigDecimal.ROUND_DOWN).doubleValue();
			}else{
				value = bigDecimal.intValue();;
			}
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
				if(o instanceof Map) {
					Map<String, String> fileUploadModel = o == null || o == "" ? null : (Map<String, String>) o;
					if (fileUploadModel != null && fileUploadModel.values() != null && fileUploadModel.values().size() > 0) {
						FileUploadEntity fileUploadEntity = saveFileUploadEntity(fileUploadModel, fileUploadEntityMap, itemModel);
						value = fileUploadEntity.getId();
					}
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
			value = itemInstance.getValue();
        }
		ColumnModelEntity columnModel = itemModel.getColumnModel();
		if (Objects.nonNull(columnModel)) {
			if(columnModel.getDataType() == ColumnType.String || columnModel.getDataType() == ColumnType.Text) {
				data.put(columnModel.getColumnName(), value == null ? null : String.valueOf(value));
			}else {
				data.put(columnModel.getColumnName(), value);
			}
		}
	}

	private FileUploadEntity saveFileUploadEntity(Map<String, String> fileUploadModelMap, Map<String, FileUploadEntity> fileUploadEntityMap, ItemModelEntity itemModel){
		String fileKey = fileUploadModelMap.get("fileKey");
		String url = fileUploadModelMap.get("url");
		String format = StringUtils.hasText(fileKey) ? fileKey.substring(fileKey.lastIndexOf(".")+1) : url.substring(url.lastIndexOf(".")+1) ;
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
			fileUploadModel.setUrl(url);
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
			if(addPermission != null || updatePermission != null){
				veryInputValue(itemModel, value);
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

            //删除流程数据
			FormProcessInfo formProcessInfo = formModel.getProcess();
			if (formProcessInfo != null) {
				processInstanceService.deleteByBusinessKey(instanceId);
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

	//更新新流程表单数据
	protected void updateProcessInfo(List<Map<String, Object>> assignmentList, FormModelEntity formModel, Map<String, Object> entity, String processInstanceId) {
		Map<String, Object> pi = new HashMap<>();
		pi.put("id", processInstanceId);
		entity.put("processInstance", pi);
		ProcessInstance processInstance = processInstanceService.get(processInstanceId);
		entity.put("PROCESS_ID", formModel.getProcess().getId());
		TaskInstance taskInstance = processInstance.getCurrentTaskInstance();
		entity.put("ACTIVITY_ID", taskInstance == null ? null : taskInstance.getActivityId());
		entity.put("ACTIVITY_INSTANCE", taskInstance == null ? null : taskInstance.getId());

		if(assignmentList == null || assignmentList.size() < 1){
			return;
		}
	}

	//更新字段值
	private void setColumnValue(List<Map<String, Object>> assignmentList, Map<String, Object> flowData, Map<String, Object> formData, UserInfo user, TaskInstance taskInstance){
		if(assignmentList == null || assignmentList.size() < 1){
			return;
		}
		for(Map<String, Object> map : assignmentList){
			String id = (String)map.get("id");
			if(id == null || map.get("id") == null){
				continue;
			}
			ItemModelEntity itemModelEntity = itemModelManager.find(id);
			if(itemModelEntity == null || itemModelEntity.getSystemItemType() == SystemItemType.ID){
				continue;
			}
			ColumnModelEntity columnModelEntity = itemModelEntity.getColumnModel();
			if(columnModelEntity == null){
				continue;
			}
			if(map.get("value") == null){
				flowData.put(columnModelEntity.getColumnName(), null);
				formData.put(columnModelEntity.getColumnName(), null);
				continue;
			}
			//更新标致
			boolean updateFlag = true;
			Object objectValue = map.get("value");
			if(AssignmentWay.DefaultManual.getValue().equals(map.get("valueType"))){
				Object value = map.get("value");
				if(columnModelEntity.getDataType() == ColumnType.Integer){
					value = Integer.parseInt(String.valueOf(value));
				}else if(columnModelEntity.getDataType() == ColumnType.Double){
					value = Double.parseDouble(String.valueOf(value));
				}else if(columnModelEntity.getDataType() == ColumnType.Long){
					value = Long.parseLong(String.valueOf(value));
				}else if(columnModelEntity.getDataType() == ColumnType.Boolean){
					value = String.valueOf(value).equals("true");
				}else if(columnModelEntity.getDataType() == ColumnType.Float){
					value = Float.parseFloat(String.valueOf(value));
				}else if(columnModelEntity.getDataType() == ColumnType.Date
						|| columnModelEntity.getDataType() == ColumnType.Time
						|| columnModelEntity.getDataType() == ColumnType.Timestamp){
					value = CommonUtils.str2Date(String.valueOf(value),((TimeItemModelEntity)itemModelEntity).getTimeFormat());
				}
				objectValue = value;
			}else{
				if(AssignmentArea.UserID.getValue().equals(map.get("value")) || AssignmentArea.UserName.getValue().equals(map.get("value")) ){
					if(AssignmentArea.UserID.getValue().equals(map.get("value"))) {
						objectValue = user == null ? null : user.getId();
					}else if(AssignmentArea.UserName.getValue().equals(map.get("value"))) {
						objectValue = user == null ? null : user.getUsername();
					}
				}else if(AssignmentArea.SystemTime.getValue().equals(map.get("value"))){
					objectValue = new Date();
				}else{
					if(AssignmentArea.ActivitieID.getValue().equals(map.get("value"))){
						objectValue = taskInstance == null ? null : taskInstance.getActivityId();
					}else if(AssignmentArea.ActivitieName.getValue().equals(map.get("value"))){
						objectValue = taskInstance == null ? null : taskInstance.getActivityName();
					}else{
						updateFlag = false;
					}
				}
			}
			flowData.remove(id);
			flowData.put(columnModelEntity.getColumnName(), objectValue);
			if(updateFlag) {
				formData.put(columnModelEntity.getColumnName(), objectValue);
			}
		}

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
			if (userId != null) {
				List<ItemModelEntity> itemModelEntityList = formModelService.findAllItems(listModel.getMasterForm());
				List<String> columnList = new ArrayList<>();
				for(ItemModelEntity itemModelEntity : itemModelEntityList){
					if(itemModelEntity.getColumnModel() != null){
						columnList.add(itemModelEntity.getColumnModel().getColumnName());
					}
				}
				if(columnList.contains("create_by")) {
					criteria.add(Restrictions.eq("create_by", userId));
				}
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

			// 地图控件搜索
			if (itemModel instanceof LocationItemModelEntity && value!=null) {
				locationItemModelSearch(criteria, value.toString(), (LocationItemModelEntity)itemModel);
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

			boolean equalsFlag = false;
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
					equalsFlag  = true;
				}else if (referenceItemModel.getSelectMode() == SelectMode.Inverse && (referenceItemModel.getReferenceType() == ReferenceType.ManyToOne
						|| referenceItemModel.getReferenceType() == ReferenceType.OneToOne)) {
					ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemModelManager.find(referenceItemModel.getReferenceItemId());
					if(referenceItemModelEntity1.getColumnModel() == null) {
						continue;
					}
					propertyIsReferenceCollection = true;
					columnModel = referenceItemModelEntity1.getColumnModel();
					propertyName = columnModel.getDataModel().getTableName()+"_"+referenceItemModelEntity1.getColumnModel().getColumnName()+"_list";
				}else if(referenceItemModel.getSelectMode() == SelectMode.Multiple){
					columnModel = new ColumnModelEntity();
					propertyIsReferenceCollection = true;
					FormModelEntity toModelEntity = formModelService.find(((ReferenceItemModelEntity) itemModel).getReferenceFormId());
					if (toModelEntity == null) {
						continue;
					}
					propertyName = toModelEntity.getDataModels().get(0).getTableName()+"_list";
				}
			} else if (itemModel instanceof SelectItemModelEntity &&
					(((SelectItemModelEntity)itemModel).getMultiple() == null || ((SelectItemModelEntity)itemModel).getMultiple() == false)) {
				propertyName = columnModel.getColumnName();
				equalsFlag = true;
			} else if (itemModel.getColumnModel() != null) {        // 普通控件
				propertyName = columnModel.getColumnName();
			}

			if (StringUtils.isEmpty(propertyName)) {
				continue;
			}

			for (int i = 0; i < values.length; i++) {
				value = null;
				if (values[i] != null) {
					if (itemModel.getSystemItemType() == SystemItemType.CreateDate || itemModel.getType() == ItemType.DatePicker || itemModel.getType() == ItemType.TimePicker) {
						equalsFlag = true;
						value = getTimeParams(itemModel.getType(), String.valueOf(values[i]));
					} else if (columnModel != null && itemModel.getType() == ItemType.InputNumber) {
						equalsFlag = true;
						Object number = getNumberParams(itemModel, columnModel, values[i]);
						if (number != null) {
							values[i] = number;
						}
					} else if (columnModel != null && columnModel.getDataType() == ColumnType.Boolean) {
						equalsFlag = true;
						if (!(values[i] instanceof Boolean)) {
							String strValue = String.valueOf(values[i]);
							values[i] = "true".equals(strValue);
						}
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

	public Criteria generateColumnMapCriteria(Session session, FormModelEntity formModel, Map<String, Object> queryParameters) {
		DataModelEntity dataModel = formModel.getDataModels().get(0);

		List<String> refereceColumn = new ArrayList<>();
		Map<String, ColumnModelEntity> columnMap = new HashMap<>();

		for(ColumnModelEntity columnModelEntity : dataModel.getColumns()){
			columnMap.put(columnModelEntity.getColumnName(), columnModelEntity);
		    if(columnModelEntity.getColumnReferences() != null && columnModelEntity.getColumnReferences().size() > 0){
		        if("id".equals(columnModelEntity.getColumnName())){
		            continue;
                }
		        refereceColumn.add(columnModelEntity.getColumnName());
            }
        }

		Criteria criteria = session.createCriteria(dataModel.getTableName());

		for (String columnName:queryParameters.keySet()) {
			Object value = queryParameters.get(columnName);
			if (value == null || !columnMap.keySet().contains(columnName) || "".equals(value.toString())) {
				if(value != null && columnName.startsWith("not_equal_") && columnMap.keySet().contains(columnName.substring("not_equal_".length()))){
					criteria.add(Restrictions.ne(columnName.substring("not_equal_".length()), value));
				}
				continue;
			}
			if("id".equals(columnName)) {
				criteria.add(Restrictions.in(columnName, value));
			}else{
			    if(refereceColumn.contains(columnName)) {
                    criteria.createCriteria(columnName).add(Restrictions.in("id", value));
                }else {
			    	ColumnModelEntity columnModelEntity = columnMap.get(columnName);
					if (columnModelEntity.getDataType() == ColumnType.Date || columnModelEntity.getDataType() == ColumnType.Time || columnModelEntity.getDataType() == ColumnType.Timestamp) {
						String dateStr = String.valueOf(value);
						String[] s = dateStr.split(",");
						Date[] objects = new Date[s.length];
						for(int i = 0; i < s.length; i++){
							objects[i] = new Date(Long.parseLong(s[i]));
						}
						if(objects.length == 2) {
							criteria.add(Restrictions.ge(columnName, objects[0]));
							criteria.add(Restrictions.lt(columnName, objects[1]));
						}else if(objects.length == 1){
							criteria.add(Restrictions.ge(columnName, objects[0]));
							Date endDate = DateUtils.ceiling(objects[0], Calendar.DAY_OF_MONTH);
							criteria.add(Restrictions.lt(columnName, endDate));
						}
					}else {
						if(value instanceof List || value instanceof Object[]){
							criteria.add(Restrictions.in(columnName, value));
						}else {
							criteria.add(Restrictions.eq(columnName, value));
						}
					}
                }
			}
		}
		if(queryParameters.get("DESC") != null){
			for(String str : ((String)queryParameters.get("DESC")).split(",")){
				criteria.addOrder(Order.desc(str));
			}
		}
		if(queryParameters.get("ASC") != null){
			for(String str : ((String)queryParameters.get("ASC")).split(",")){
				criteria.addOrder(Order.asc(str));
			}
		}
		if(queryParameters.get("DESC") == null || queryParameters.get("ASC") == null) {
			//默认id
			criteria.addOrder(Order.desc("id"));
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

	// 地图控件的名称搜索
	private void locationItemModelSearch(Criteria criteria, String queryValue, LocationItemModelEntity locationItemModelEntity) {
		if (StringUtils.isEmpty(queryValue)) {
			return;
		}
		ColumnModelEntity columnModel = locationItemModelEntity.getColumnModel();
		if (columnModel==null || StringUtils.isEmpty(columnModel.getColumnName())) {
			return;
		}
		queryValue = "%" + queryValue + "%";
		List<GeographicalMapEntity> list = mapEntityJPAManager.query().filterEqual("fromSource", locationItemModelEntity.getId()).filterLike("detailAddress", queryValue).list();
		if (list!=null && list.size()!=0) {
			List<String> ids = new ArrayList();
			for (GeographicalMapEntity entity:list) {
				ids.add(entity.getId());
			}
			criteria.add(Restrictions.in(columnModel.getColumnName(), ids.toArray(new String[]{})));
		} else {
			// 直接搜索地图没有满足条件的数据，构造一个不存在的查询条件，让后续查不出数据
			// criteria.add(Restrictions.in(columnModel.getColumnName(), new ArrayList()));  // Restrictions.in("字段名",空集合); 空集合导致这句代码抛错
			criteria.add(Restrictions.eq(columnModel.getColumnName(), UUID.randomUUID().toString()));
		}

	}

	private void fullTextSearchCriteria(Criteria criteria, Object queryValue, ListModelEntity listModelEntity) {
		if (queryValue!=null && queryValue instanceof String) {
			String queryValueStr = queryValue.toString();
			List<Criterion> conditions = new ArrayList();
			List<ListSearchItem> searchItems = listModelEntity.getSearchItems();
			searchItems = searchItems.stream().filter(item->(item.getParseArea()!=null && item.getParseArea().contains("FuzzyQuery"))).collect(Collectors.toList());
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
						conditions.add(Restrictions.like(columnModel.getColumnName(), "%" + queryValueStr + "%"));
					}
				} else if (itemModelEntity instanceof SelectItemModelEntity && columnModel!=null && StringUtils.hasText(columnModel.getColumnName())) {
					fullTextSearchSelectItemCriteria(queryValueStr, conditions, columnModel.getColumnName(), (SelectItemModelEntity)itemModelEntity);
				} else if (itemModelEntity instanceof TreeSelectItemModelEntity && columnModel!=null && StringUtils.hasText(columnModel.getColumnName())) {
					fullTextSearchTreeSelectItemCriteria(queryValueStr, conditions, columnModel.getColumnName(), (TreeSelectItemModelEntity)itemModelEntity);
				} else if (itemModelEntity instanceof ReferenceItemModelEntity) {
					ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity)itemModelEntity;
					ReferenceItemModelEntity parentItem = referenceItemModelEntity.getParentItem();
					if (referenceItemModelEntity.getSystemItemType()==SystemItemType.Creator ||
							(parentItem!=null && parentItem.getSystemItemType()==SystemItemType.Creator)) {
						fullTextSearchPeopleReferenceItemCriteria(queryValueStr, conditions, referenceItemModelEntity);
					} else {
						fullTextSearchReferenceItemCriteria(queryValueStr, conditions, columnModel, referenceItemModelEntity);
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

		if (selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.Option){
			List<ItemSelectOption> options = selectItemModelEntity.getOptions();
			Set<String> optionIds = options.stream().filter(item-> StringUtils.hasText(item.getLabel())&&item.getLabel().contains(valueStr)).map(item->item.getId()).collect(Collectors.toSet());
			if (optionIds!=null && optionIds.size()>0) {
				for (String optionId:optionIds) {
					conditions.add(Restrictions.in(columnFullname, optionId));
				}
			}
		} else if (selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.DictionaryData) {
			String referenceDictionaryId = selectItemModelEntity.getReferenceDictionaryId();
			List<DictionaryDataItemModel> list = dictionaryDataService.findDictionaryItems(referenceDictionaryId, valueStr);
			for (DictionaryDataItemModel item:list) {
				conditions.add(Restrictions.in(columnFullname, item.getId()));
			}
		} else if (selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.DictionaryModel) {
			String referenceDictionaryId = selectItemModelEntity.getReferenceDictionaryId();
			List<DictionaryModelData> list = dictionaryModelDataToList(dictionaryModelService.findDictionaryModelDataByDictionaryId(referenceDictionaryId));
			Set<String> ids = list.stream().filter(item->item.getName()!=null && item.getName().contains(valueStr)).map(item->item.getId()).collect(Collectors.toSet());
			for (String id:ids) {
				conditions.add(Restrictions.in(columnFullname, id));
			}
		}
	}

	private List<DictionaryModelData> dictionaryModelDataToList(DictionaryModelData dictionaryModelData) {
		List<DictionaryModelData> list = new ArrayList<>();
		if (dictionaryModelData!=null) {
			list.addAll(dictionaryModelDataToList(Arrays.asList(dictionaryModelData)));
		}
		return list;
	}

	private List<DictionaryModelData> dictionaryModelDataToList(List<DictionaryModelData> list) {
		List<DictionaryModelData> returnList = new ArrayList<>();
		if (list==null || list.size()==0) {
			return returnList;
		}
		for (DictionaryModelData item:list) {
			returnList.add(item);
			if (item.getResources()!=null && item.getResources().size()>0) {
				returnList.addAll(dictionaryModelDataToList(item.getResources()));
			}
		}
		return returnList;
	}

	private void fullTextSearchTreeSelectItemCriteria(String valueStr, List<Criterion> conditions, String columnFullname, TreeSelectItemModelEntity treeSelectItemModelEntity) {
		if (StringUtils.isEmpty(columnFullname)) {
			return;
		}
		TreeSelectDataSource dataSource = treeSelectItemModelEntity.getDataSource();
		if (dataSource!=null) {
			if (TreeSelectDataSource.DictionaryData==dataSource) {
				String dictionaryId = treeSelectItemModelEntity.getReferenceDictionaryId();
				if (StringUtils.hasText(dictionaryId)) {
					List<DictionaryDataItemModel> list = dictionaryDataService.findDictionaryItems(dictionaryId, valueStr);
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
			} else if (TreeSelectDataSource.DictionaryModel==dataSource) {

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
			List<ItemModelEntity> itemModelEntityList = formModelService.findAllItems(listModel.getMasterForm());
			List<String> columnList = new ArrayList<>();
			for(ItemModelEntity itemModelEntity : itemModelEntityList){
				if(itemModelEntity.getColumnModel() != null){
					columnList.add(itemModelEntity.getColumnModel().getColumnName());
				}
			}
			if(columnList.contains("create_at")) {
				criteria.addOrder(Order.desc("create_at"));
			}
			criteria.addOrder(Order.desc("id"));
		}
	}

	protected List<FormDataSaveInstance> wrapFormDataList(FormModelEntity formModel, ListModelEntity listModel, List<Map<String, Object>> entities) {
		return entities.stream().map(entity -> {
			return wrapFormDataEntity(false, formModel, listModel, entity, String.valueOf(entity.get("id")), true);
		}).collect(Collectors.toList());
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
		if (formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getKey())) {
			formInstance.setProcessId((String) entity.get("PROCESS_ID"));
			formInstance.setActivityId((String) entity.get("ACTIVITY_ID"));
			formInstance.setActivityInstanceId((String) entity.get("ACTIVITY_INSTANCE"));
			Map<String, Object> processInstance = (Map<String, Object>) entity.get("processInstance");
			if (processInstance != null) {
				formInstance.setProcessInstanceId((String) processInstance.get("id"));
			}
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
		Map<String, Object> processInstance = null;
		if (formModel.getProcess() != null) {
			formInstance.setProcessId((String) entity.get("PROCESS_ID"));
			formInstance.setActivityId((String) entity.get("ACTIVITY_ID"));
			formInstance.setActivityInstanceId((String) entity.get("ACTIVITY_INSTANCE"));
			processInstance = (Map<String, Object>) entity.get("processInstance");
		}
		FormDataSaveInstance formDataSaveInstance = setFormDataInstanceModel(isQrCodeFlag, formInstance, formModel,  listModel, entity, referenceFlag);
		if (formModel.getProcess() != null){
			if (processInstance != null) {
				formDataSaveInstance.setProcessInstanceId((String) processInstance.get("id"));
				setFlowFormInstance(formModelEntity, wrapProcessInstance(formModel.getProcess().getKey(), entity), formDataSaveInstance);
			}
		}
		return formDataSaveInstance;
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

		//表单数据标识id集合
		List<String> labelIdList = null;
		if(StringUtils.hasText(formModel.getItemModelIds())){
			labelIdList = new ArrayList<>();
			for(String str : formModel.getItemModelIds().split(",")) {
				labelIdList.add(str);
			}
		}



		//所有显示的控件实例
        List<ItemInstance> newAllDisplayItems = new ArrayList<>();
		for(ItemInstance itemInstance : items){
            newAllDisplayItems.add(itemInstance);
		}

		//主键id值
		Object idVlaue = null;
		for(int i = 0; i < items.size(); i++ ){
			ItemInstance itemInstance = items.get(i);
			if(itemInstance.getSystemItemType() == SystemItemType.ID){
				idVlaue = itemInstance.getValue();
			}
		}

		//展示字段
		List<String>  displayIds = setDisplayItemIds(isQrCodeFlag, listModelEntity, formModel, items);

		// referenceDataModelList的数据对应的是关联表单的数据标识的item的数据
		for (ReferenceDataInstance referenceDataInstance : referenceDataModelList) {
			if(displayIds.contains(referenceDataInstance.getId())){
				ItemModelEntity itemModelEntity = itemModelManager.find(referenceDataInstance.getId());
				ItemInstance itemInstance = new ItemInstance();
				itemInstance.setSystemItemType(itemModelEntity == null ? SystemItemType.ReferenceList : itemModelEntity.getSystemItemType());
				itemInstance.setType(itemModelEntity == null ? ItemType.ReferenceList : itemModelEntity.getType());
				itemInstance.setId(referenceDataInstance.getId());
				itemInstance.setItemName(itemModelEntity.getName());
				itemInstance.setValue(referenceDataInstance.getValue());
				ColumnModelEntity columnModelEntity = itemModelEntity.getColumnModel();
				itemInstance.setColumnModelId(columnModelEntity == null ? null : columnModelEntity.getId());
				itemInstance.setColumnModelName(columnModelEntity == null ? null : columnModelEntity.getColumnName());
				itemInstance.setDisplayValue(referenceDataInstance.getDisplayValue());
				newAllDisplayItems.add(itemInstance);
			}
		}

		//显示的数据标识label
		if(labelIdList != null) {
			Map<String, ItemInstance> labelItemMap = new HashMap<>();
			for(ItemInstance itemInstance : newAllDisplayItems) {
				if (labelIdList.contains(itemInstance.getId())) {
					labelItemMap.put(itemInstance.getId(), itemInstance);
				}
			}
			formInstance.setLabel(getLabel(labelIdList, labelItemMap));
		}

		formInstance.getItems().addAll(newAllDisplayItems);


		//二维码只有一张图
		if(idVlaue != null && idVlaue != "") {
			List<FileUploadEntity> fileUploadEntityList = uploadService.getFileUploadEntity(DataSourceType.FormModel, formInstance.getFormId(), String.valueOf(idVlaue));
			if (fileUploadEntityList != null && fileUploadEntityList.size() > 0) {
				FileUploadModel fileUploadModel = new FileUploadModel();
				BeanUtils.copyProperties(fileUploadEntityList.get(0), fileUploadModel);
				formInstance.setFileUploadModel(fileUploadModel);
			}
		}

		//控件实例转data map
		Map<String, Object> map = new HashMap<>();
		setItemInstanceMap(map, formInstance.getItems());
		formInstance.addAllData(map);

		//子表数据
		for(SubFormItemInstance subFormItemInstance : formInstance.getSubFormData()) {
			formInstance.addData(subFormItemInstance.getTableName(), listValue(subFormItemInstance));
		}

		return formInstance;
	}

	private List<String> setDisplayItemIds(boolean isQrCodeFlag, ListModelEntity listModelEntity, FormModelEntity formModel, List<ItemInstance> items){
		//展示字段
		List<String> displayIds = new ArrayList<>();
		if (!isQrCodeFlag && listModelEntity != null) {
			displayIds = listModelEntity.getDisplayItems().parallelStream().map(ItemModelEntity::getId).collect(Collectors.toList());
		}else	if(isQrCodeFlag && formModel.getQrCodeItemModelIds() != null){
			displayIds = Arrays.asList(formModel.getQrCodeItemModelIds().split(","));
		}else if(formModel != null){
			List<ItemModelEntity> allCheckItem = new ArrayList<>();
			for(ItemModelEntity itemModelEntity : formModelService.findAllItems(formModel)){
				for(ItemPermissionInfo itemPermissionInfo : itemModelEntity.getPermissions()){
					if(itemPermissionInfo.getDisplayTiming() == DisplayTimingType.Check && itemPermissionInfo.getVisible() != null && itemPermissionInfo.getVisible()){
						allCheckItem.add(itemModelEntity);
					}
				}
			}
			displayIds = allCheckItem.parallelStream().map(ItemModelEntity::getId).collect(Collectors.toList());
		}else{
			displayIds = items.parallelStream().map(ItemInstance::getId).collect(Collectors.toList());
		}
		return displayIds;
	}

	private List<Map<String, Object>> listValue(SubFormItemInstance subFormItemInstance){
		List<Map<String, Object>> list = new ArrayList<>();
		for(SubFormDataItemInstance subFormDataItemInstance:subFormItemInstance.getItemInstances()){
			Map<String, Object> map = new HashMap<>();
			for(SubFormRowItemInstance subFormRowItemInstance : subFormDataItemInstance.getItems()){
				setItemInstanceMap(map, subFormRowItemInstance.getItems());
			}
			list.add(map);
		}
		return list;
	}

	private void setItemInstanceMap(Map<String, Object> map, List<ItemInstance> list){
		for(ItemInstance itemInstance : list){
			if(!StringUtils.hasText(itemInstance.getColumnModelName())){
				continue;
			}
			if(itemInstance.getType() == ItemType.Media || itemInstance.getType() == ItemType.Attachment){
				List<String> idList = new ArrayList<>();
				if(itemInstance.getValue() != null){
					if(itemInstance.getValue() instanceof List){
						for(FileUploadModel fileUploadModel : (List<FileUploadModel>)itemInstance.getValue()){
							idList.add(fileUploadModel.getId());
						}
					}else{
						idList.add(((FileUploadModel)itemInstance.getValue()).getId());
					}
				}
				map.put(itemInstance.getColumnModelName(), String.join(",",idList));
			}else if(itemInstance.getType() == ItemType.Location){
				map.put(itemInstance.getColumnModelName(),itemInstance.getValue() == null ? null : ((GeographicalMapModel)itemInstance.getValue()).getId());
			}else {
				map.put(itemInstance.getColumnModelName(), itemInstance.getValue());
			}
		}
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
				List<String> valueList = (List<String>) displayVlaue;
				StringBuffer sub = new StringBuffer();
				for (int j = 0; j < valueList.size(); j++) {
					if (j == 0) {
						sub.append(valueList.get(j));
					} else {
						sub.append("," + valueList.get(j));
					}
				}
				value = sub.toString();
			}else{
				value = (String) displayVlaue;
			}
		}
		return value;
	}


	private void setItemInstance(ItemModelEntity itemModel, boolean referenceFlag, Map<String, Object> entity, List<DataModelInstance> referenceDataModelList,
								 List<SubFormItemInstance> subFormItems, List<ItemInstance> items, FormInstance formInstance){
		ColumnModelEntity column = itemModel.getColumnModel();
		if(column == null && !(itemModel instanceof  ReferenceItemModelEntity) && !(itemModel instanceof  RowItemModelEntity) && !(itemModel instanceof SubFormItemModelEntity) && !(itemModel instanceof TabsItemModelEntity)
			&& !(itemModel instanceof ReferenceInnerItemModelEntity)){
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
			setSubFormItemInstance( itemModel,  entity,  subFormItems, items, formInstance.getActivityId());
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
		}else if (itemModel instanceof ReferenceInnerItemModelEntity) {

		} else{
			ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
			items.add(itemInstance);
			formInstance.addData(itemModel.getColumnModel().getId(), itemInstance.getValue());
		}
	}

    // 处理每个控件实例
	private void setFormDataItemInstance(boolean isQrCodeFlag, ItemModelEntity itemModel, boolean referenceFlag, Map<String, Object> entity, List<ReferenceDataInstance> referenceDataModelList,
								 List<SubFormItemInstance> subFormItems, List<ItemInstance> items, FormDataSaveInstance formInstance){
		ColumnModelEntity column = itemModel.getColumnModel();
		if(column == null && !(itemModel instanceof  ReferenceItemModelEntity) && !(itemModel instanceof  RowItemModelEntity)
				&& !(itemModel instanceof SubFormItemModelEntity) && !(itemModel instanceof TabsItemModelEntity)
                && !(itemModel instanceof ReferenceInnerItemModelEntity)){
			items.add(setItemInstance(itemModel));
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
			setSubFormItemInstance(itemModel,  entity,  subFormItems, items, formInstance.getActivityId());
		}else if(itemModel instanceof RowItemModelEntity){
			items.add(setItemInstance(itemModel));
			for(ItemModelEntity itemModelEntity : ((RowItemModelEntity) itemModel).getItems()) {
				setFormDataItemInstance(isQrCodeFlag, itemModelEntity, referenceFlag, entity, referenceDataModelList,
						subFormItems,  items, formInstance);
			}
		}else if(itemModel instanceof TabsItemModelEntity){
			items.add(setItemInstance(itemModel));
			for(TabPaneItemModelEntity itemModelEntity : ((TabsItemModelEntity) itemModel).getItems()) {
				items.add(setItemInstance(itemModelEntity));
				for(ItemModelEntity itemModelEntity1 : itemModelEntity.getItems()) {
					setFormDataItemInstance(isQrCodeFlag, itemModelEntity1, referenceFlag, entity, referenceDataModelList,
							subFormItems, items, formInstance);
				}
			}
		}else if (itemModel instanceof ReferenceInnerItemModelEntity){
            setReferenceInnerItemInstance((ReferenceInnerItemModelEntity) itemModel, entity, items);
        } else{
			ItemInstance itemInstance = setItemInstance(column.getKey(), itemModel, value, formInstance.getActivityId());
			items.add(itemInstance);
		}
	}

	private ItemInstance setItemInstance(ItemModelEntity itemModel){
		ItemInstance itemInstance = new ItemInstance();
		itemInstance.setId(itemModel.getId());
		itemInstance.setType(itemModel.getType());
		itemInstance.setSystemItemType(itemModel.getSystemItemType());
		itemInstance.setItemName(itemModel.getName());
		return itemInstance;
	}

	/**
	 * 处理关联属性内嵌, 并且添加ItemInstance到 itemInstances内
	 * @param model  关联控件实体模型
	 * @param rowData 当前行数据
	 * @param itemInstances 存储的itemInstances
	 */
	private void setReferenceInnerItemInstance(ReferenceInnerItemModelEntity model, Map<String, Object> rowData, List<ItemInstance> itemInstances) {
		ItemModelEntity innerItem =  itemModelService.findUniqueByProperty("uuid", model.getReferenceInnerItemUuid());
		InnerItemUtils.InnerItemHandler innerItemHandler = InnerItemUtils.InnerItemHandlerFactory.getHandler(innerItem);
		String displayValue = innerItemHandler.findDisplayValue(model, innerItem, (id) ->itemModelService.find(Objects.toString(id)), rowData);
		itemInstances.add(InnerItemUtils.buildItemInstance(model, displayValue));
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
			ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemModelManager.find(fromItem.getReferenceItemId());
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

	private ReferenceDataInstance createDataModelInstance(ReferenceItemModelEntity fromItem, FormModelEntity toModelEntity, String id, List<String> itemIds) {
		return createDataModelInstance(false, fromItem, toModelEntity, id, itemIds, false);
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

	private void setSubFormItemInstance(ItemModelEntity itemModel, Map<String, Object> entity, List<SubFormItemInstance> subFormItems, List<ItemInstance> items, String activityId){
		//TODO 子表数据结构
		SubFormItemModelEntity itemModelEntity = (SubFormItemModelEntity)itemModel;
		//子表
		ItemInstance subFormitemModelInstance = new ItemInstance();
		subFormitemModelInstance.setId(itemModel.getId());
		subFormitemModelInstance.setType(itemModel.getType());
		subFormitemModelInstance.setItemName(itemModel.getName());
		items.add(subFormitemModelInstance);
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
			// 根据更新时间排序，如何update_at存在，并且为Date类型类型或者是Date的子类对象，才排序
			Optional<Map<String, Object>> optional = listMap.stream().filter(item->item.get("update_at")!=null).findFirst();
			if (optional.isPresent()) {
				if (Date.class.isAssignableFrom(optional.get().get("update_at").getClass())) {
					Collections.sort(listMap, new Comparator<Map<String, Object>>() {
						public int compare(Map<String, Object> o1, Map<String, Object> o2) {
							if (o1 == null || o1.get("update_at") == null) {
								return -1;
							}
							if (o2 == null || o2.get("update_at") == null) {
								return 1;
							}
							Date date1 = (Date) o1.get("update_at");
							Date date2 = (Date) o2.get("update_at");
							return date1.compareTo(date2);
						}
					});
				}
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
					ItemInstance subFormRowItemModelIntance = new ItemInstance();
					subFormRowItemModelIntance.setId(subFormRowItemModelEntity.getId());
					subFormRowItemModelIntance.setType(subFormRowItemModelEntity.getType());
					subFormRowItemModelIntance.setItemName(subFormRowItemModelEntity.getName());
					items.add(subFormRowItemModelIntance);


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
			List<TreeSelectData> list = getTreeSelectData(treeSelectItem.getDataSource(), valueStrs.split(","), treeSelectItem.getReferenceDictionaryId());
			if(list != null && list.size() > 0) {
				List<String> values = list.parallelStream().map(TreeSelectData::getName).collect(Collectors.toList());
				if(treeSelectItem.getMultiple() != null && treeSelectItem.getMultiple()) {
					itemInstance.setValue(list.stream().map(item->item.getId()).collect(Collectors.toList()));
					itemInstance.setDisplayValue(values);
				} else {
					itemInstance.setValue(list.get(0).getId());
					itemInstance.setDisplayValue(values.get(0));
				}
			}

		}
	}

	@Override
	public List<TreeSelectData> getTreeSelectData(TreeSelectDataSource dataSourceType, String[] ids, String dictionaryId) {
		if (ids==null || ids.length==0 || dataSourceType==null) {
			return new ArrayList<>();
		}
		List<TreeSelectData> list = new ArrayList<>();
		// 部门，岗位，人员，岗位标识
		if (TreeSelectDataSource.Department==dataSourceType || TreeSelectDataSource.Personnel==dataSourceType ||
			TreeSelectDataSource.Position==dataSourceType || TreeSelectDataSource.PositionIdentify==dataSourceType) {
			list = groupService.getTreeSelectDataSourceByIds(dataSourceType.getValue(), ids);
		// 系统代码
		} else if (TreeSelectDataSource.DictionaryData==dataSourceType) {
			List<DictionaryDataItemEntity> dictionaryItems = dictionaryDataService.findByItemIds(ids);
			if (dictionaryItems!=null && dictionaryItems.size()>0) {
				for (DictionaryDataItemEntity dictionaryItem:dictionaryItems) {
					TreeSelectData treeSelectData = new TreeSelectData();
					treeSelectData.setType(TreeSelectDataSource.DictionaryData.getValue());
					treeSelectData.setId(dictionaryItem.getId());
					treeSelectData.setName(dictionaryItem.getName());
					list.add(treeSelectData);
				}
			}
		// 字典模型
		} else if (TreeSelectDataSource.DictionaryModel==dataSourceType) {
			List<DictionaryModelData> dictionaryModelItems = dictionaryModelService.getDictionaryModelDataByIds(dictionaryId, ids);
			for (DictionaryModelData item:dictionaryModelItems) {
				TreeSelectData treeSelectData = new TreeSelectData();
				treeSelectData.setType(TreeSelectDataSource.DictionaryModel.getValue());
				treeSelectData.setId(item.getId());
				treeSelectData.setName(item.getName());
				list.add(treeSelectData);
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
			return new ArrayList();
		}
		List<String> displayValuelist = new ArrayList<>();
		List<Object> displayObjectList = new ArrayList<>();
		if((selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.DictionaryData ||
				(selectItemModelEntity.getSelectDataSourceType() == null && selectItemModelEntity.getReferenceDictionaryId() != null)) && list != null && list.size() > 0){
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
		}else if(selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.DictionaryModel){
            List<DictionaryModelData> dictionaryModelDatas = dictionaryModelService.findDictionaryModelDataName(selectItemModelEntity.getReferenceDictionaryId(), list);
            if(dictionaryModelDatas != null){
                //字典模型数据
                displayValuelist.add(String.join(",", dictionaryModelDatas.parallelStream().map(DictionaryModelData::getName).collect(Collectors.toList())));
                for(DictionaryModelData dictionaryModelData : dictionaryModelDatas){
                    displayObjectList.add(dictionaryModelData);
                }
            }
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
			GeographicalMapModel mapModel = null;
			List<GeographicalMapEntity> entityList = mapEntityJPAManager.query().filterIn("id", idlist).list();
			if(entityList != null && entityList.size() > 0){
				mapModel = new GeographicalMapModel();
			}
			for(GeographicalMapEntity entity : entityList){
				GeographicalMapModel geographicalMapModel = new GeographicalMapModel();
				BeanUtils.copyProperties(entity, geographicalMapModel);
				mapModel = geographicalMapModel;
			}
			itemInstance.setValue(mapModel);
			String displayVlaue = mapModel == null ? null : mapModel.getDetailAddress() ;
			itemInstance.setDisplayValue(displayVlaue);
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

	public List<UserBase> getUserInfoByIds(List<String> ids) {
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
		Map<String, Object> map = null;
		try {
			DataModelEntity dataModel = formModel.getDataModels().get(0);
			map =  getDataInfo(dataModel, id);
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
        if(map != null && formModel.getProcess() != null && formInstance.getProcessInstanceId() != null){
            setFormInstanceProcessStatus(formModel, map, formInstance);
        }
		return formInstance;
	}

	//设置流程状态
	private void setFormInstanceProcessStatus(FormModelEntity formModelEntity, Map<String, Object> entity, FormDataSaveInstance formInstance){
		setFlowFormInstance(formModelEntity, wrapProcessInstance(formModelEntity.getProcess().getKey(), entity), formInstance);
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

	@Override
	public IdEntity startFormInstanceProcess(FormModelEntity formModelEntity, String instanceId) {
		// 启动流程
		if (formModelEntity.getProcess() == null || formModelEntity.getProcess().getKey() == null) {
			return null;
		}
		Session session = null;
		DataModelEntity dataModel = formModelEntity.getDataModels().get(0);
		UserInfo user = null;
		try {
			user = CurrentUserUtils.getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String processInstanceId = null;
		try {
			session = getSession(dataModel);
			//开启事务
			session.beginTransaction();

			Map<String, Object> data = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);


			data.put("update_at", new Date());
			data.put("update_by",  user != null ? user.getId() : null);


			// 流程操作
			Map<String, Object> flowData  = new HashMap<>();
			if(flowData == null){
				flowData = new HashMap<>();
			}
			flowData.putAll(data);

			//跳过第一个流程环节
			flowData.put("PASS_THROW_FIRST_USERTASK", true);
			flowData = toProcesDictionaryData(flowData, formModelEntity);
			System.out.println("传给工作流的数据=====>>>>>"+OkHttpUtils.mapToJson(flowData));
			setColumnValue(null, flowData, data, user, null);
			processInstanceId = processInstanceService.startProcess(formModelEntity.getProcess().getKey(), instanceId, flowData);
			updateProcessInfo(null, formModelEntity, data, processInstanceId);

			session.update(dataModel.getTableName(), data);
			session.getTransaction().commit();

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
		return new IdEntity(processInstanceId);
	}

	@Override
	public void setFlowFormInstance(FormModelEntity formModelEntity, ProcessInstance processInstance, FormDataSaveInstance instance) {
		if (processInstance == null) {
			return;
		}
		//表单控件查询权限
		Map<String, ItemPermissionInfo> itemPermissionMap = null;
		if (processInstance.getStatus() != ProcessInstance.Status.Ended) {
			if(processInstance.isMyTask()) {
				instance.setCanEdit(true);
			}else{
				instance.setCanEdit(false);
			}
		} else {
			itemPermissionMap = itemModelService.findItemPermissionByDisplayTimingType(formModelEntity, DisplayTimingType.Check);
			instance.setCanEdit(false);
		}
		instance.setMyTask(processInstance.isMyTask());
		instance.setFunctions(processInstance.getCurrentTaskInstance() == null ? null : processInstance.getCurrentTaskInstance().getOperations());
		WorkingTask taskInstance =  null;
		if(processInstance.getCurrentTaskInstance() instanceof WorkingTask) {
			taskInstance =  new WorkingTask();
			taskInstance.setSignable(processInstance.getCurrentTaskInstance().isSignable());
			taskInstance.setRejectable(processInstance.getCurrentTaskInstance().isRejectable());
			taskInstance.setComplatable(processInstance.getCurrentTaskInstance().isComplatable());
			taskInstance.setReturnable(processInstance.getCurrentTaskInstance().isReturnable());
			taskInstance.setJumpable(processInstance.getCurrentTaskInstance().isJumpable());
		}
		instance.setCurrentTaskInstance(taskInstance);

		//流程表单控件权限
		List<Map<String, Object>> flowFormDefinition = processInstance.getCurrentTaskInstance() == null ? null : (List<Map<String, Object>>)(processInstance.getCurrentTaskInstance().getFormDefinition());
		Map<String, Map<String, Object>> flowPermissionsMap = new HashMap<>();
		if(flowFormDefinition != null) {
			for(Map<String, Object> objectMap : flowFormDefinition){
				flowPermissionsMap.put((String)objectMap.get("id"), objectMap);
			}
		}
		for(ItemInstance itemInstance : instance.getItems()){
			itemInstance.setProcessInstanceId(processInstance.getId());
			Map<String, Object> instanceMap = flowPermissionsMap.get(itemInstance.getId());
			boolean visible = false;
			boolean canFill =  false;
			boolean required =  false;
			if(instanceMap != null) {
				visible = instanceMap.get("visible") == null ? false : (Boolean)instanceMap.get("visible");
				if(processInstance.isMyTask()) {
					canFill = instanceMap.get("canFill") == null ? false : (Boolean) instanceMap.get("canFill");
					required = instanceMap.get("required") == null ? false : (Boolean) instanceMap.get("required");
				}
			}else {
				if(!instance.getCanEdit()){
					if(itemPermissionMap != null && itemPermissionMap.get(itemInstance.getId()) != null){
						ItemPermissionInfo itemPermissionInfo = itemPermissionMap.get(itemInstance.getId());
						visible = itemPermissionInfo.getVisible() == null ? false : itemPermissionInfo.getVisible();
					}
				}
			}
			itemInstance.setVisible(visible);
			itemInstance.setCanFill(canFill);
			itemInstance.setRequired(required);
		}
		String formName = StringUtils.hasText(processInstance.getFormTitle()) ? processInstance.getFormTitle() : processInstance.getFormTitle();
		instance.setFormName(formName);
		setFormInstanceProcessStatus(instance, processInstance);
	}

    @Override
    public Map<String, Object> saveFormInstance(FormModelEntity formModelEntity, Map<String, Object> parameters) {
        Session session = null;
        Map<String, Object> data = null;
        DataModelEntity dataModel = formModelEntity.getDataModels().get(0);

		Map<String, ColumnReferenceEntity> columnReferencesMap = new HashMap<>();
		Map<String, ColumnModelEntity> columnMap = new HashMap<>();

		for(ColumnModelEntity columnModelEntity : dataModel.getColumns()) {
			if(columnModelEntity.getColumnName() == null || "id".equals(columnModelEntity.getColumnName())){
				continue;
			}
			columnMap.put(columnModelEntity.getColumnName(), columnModelEntity);
			if(columnModelEntity.getColumnReferences() != null && columnModelEntity.getColumnReferences().size() > 0){
				columnReferencesMap.put(columnModelEntity.getColumnName(), columnModelEntity.getColumnReferences().get(0));
			}
		}

        String instanceId = (String)parameters.get("id");
        try {
            UserInfo user = null;
            try {
                user = CurrentUserUtils.getCurrentUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
            session = getSession(dataModel);
            //开启事务
            session.beginTransaction();
            for(String key : parameters.keySet()){
            	Object vlaue = parameters.get(key);
            	if(vlaue == null){
            		continue;
				}
            	ColumnModelEntity columnModelEntity = columnMap.get(key);
            	if(!(vlaue instanceof Date) && columnMap.keySet().contains(key) && (columnModelEntity.getDataType() == ColumnType.Date
						|| columnModelEntity.getDataType() == ColumnType.Time || columnModelEntity.getDataType() == ColumnType.Timestamp)){
					parameters.put(key, new Date((Long)parameters.get(key)));
				}
            	if(columnReferencesMap.containsKey(key)){
					Map<String, Object> map = null;
					try {
						map = (Map<String, Object>)session.load(columnReferencesMap.get(key).getToColumn().getDataModel().getTableName(), (String)parameters.get(key));
					} catch (Exception e) {
						e.printStackTrace();
					}
					parameters.put(key, map);
				}
			}
            if(StringUtils.hasText(instanceId)) {
                data = (Map<String, Object>) session.load(dataModel.getTableName(), instanceId);
                data.putAll(parameters);
                //主表数据
                data.put("update_at", new Date());
                data.put("update_by",  user != null ? user.getId() : null);
                // before
                sendWebService( formModelEntity, BusinessTriggerType.Update_Before, data, instanceId);
                session.update(dataModel.getTableName(), data);
                session.getTransaction().commit();
                // after
                sendWebService( formModelEntity, BusinessTriggerType.Update_After, data, instanceId);
            }else{
                data = parameters;
                //主表数据
                data.put("create_at", new Date());
                data.put("create_by",  user != null ? user.getId() : null);
                sendWebService(formModelEntity, BusinessTriggerType.Add_Before, data, null);
                instanceId = (String) session.save(dataModel.getTableName(), data);
                session.getTransaction().commit();
                // after
                sendWebService( formModelEntity, BusinessTriggerType.Add_After, data, instanceId);
            }
			data.put("id", instanceId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("saveFormData error with data=["+OkHttpUtils.mapToJson(data)+"]");
            if(e instanceof ICityException){
                throw e;
            }
            throw new IFormException("保存【" + dataModel.getTableName() + "】表，id【"+instanceId+"】的数据失败");
        } finally {
            if(session != null){
                session.close();
            }
        }
        return data;
    }


	@Override
	public Page<FormDataSaveInstance> pageByColumnMap(FormModelEntity formModel, int page, int pagesize, Map<String, Object> parameters) {
		Page<FormDataSaveInstance> result = Page.get(page, pagesize);
		Session session = getSession(formModel.getDataModels().get(0));
		try {
			boolean hasProcess = hasProcess(formModel);
			int processStatus = hasProcess ? getProcessStatusParameter(formModel, SystemItemType.ProcessStatus, parameters) : -1;
			int userStatus = hasProcess ? getProcessStatusParameter(formModel, SystemItemType.ProcessPrivateStatus, parameters) : -1;
			Process process = hasProcess ? processService.get(formModel.getProcess().getKey()) : null;
			String userId = hasProcess ? CurrentUserUtils.getCurrentUser().getId() : null;
			List<String> groupIds = hasProcess ? getGroupIds(userId) : null;

			Criteria criteria = generateColumnMapCriteria(session, formModel,  parameters);
			if (hasProcess) {
				addProcessCriteria(criteria, processStatus, userStatus, userId, groupIds, null, null);
			}
			criteria.setFirstResult((page - 1) * pagesize);
			criteria.setMaxResults(pagesize);

			List<Map<String, Object>> data = criteria.list();
			if (process != null) {
				data.forEach(entity -> {
					entity.put("process", process);
					entity.put("userId", userId);
					entity.put("groupIds", groupIds);
				});
			}
			List<FormDataSaveInstance> list = wrapFormDataList(formModel, null, data);

			criteria.setFirstResult(0);
			criteria.setProjection(Projections.rowCount());

			// 清除排序字段
			for (Iterator<CriteriaImpl.OrderEntry> i = ((CriteriaImpl) criteria).iterateOrderings(); i.hasNext(); ) {
				i.next();
				i.remove();
			}
			Number count = (Number) criteria.uniqueResult();

			result.data(count.intValue(), list);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ICityException) {
				throw e;
			}
			throw new IFormException(e.getLocalizedMessage(), e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return result;
	}

    @Override
    public List<FormDataSaveInstance> findByColumnMap(FormModelEntity formModel, Map<String, Object> columnMap) {
        List<FormDataSaveInstance> result = null;
        Session session = getSession(formModel.getDataModels().get(0));
        try {
            Criteria criteria = generateColumnMapCriteria(session, formModel,  columnMap);
            List data = criteria.list();
            result = wrapFormDataList(formModel, null, data);

            criteria.setFirstResult(0);
            criteria.setProjection(Projections.rowCount());

            // 清除排序字段
            for (Iterator<CriteriaImpl.OrderEntry> i = ((CriteriaImpl) criteria).iterateOrderings(); i.hasNext(); ) {
                i.next();
                i.remove();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ICityException) {
                throw e;
            }
            throw new IFormException(e.getLocalizedMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }

	@Override
	public void deleteFormData(FormModelEntity formModelEntity) {
		DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
		if(dataModelEntity == null){
			return;
		}
		List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select tablename from pg_tables where schemaname='public'");
		List<String> tableList = new ArrayList<>();
		if(mapList != null && mapList.size() >0 ) {
			for (Map<String, Object> map : mapList) {
				tableList.add((String)map.get("tablename"));
			}
		}
		String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName() : dataModelEntity.getPrefix()+dataModelEntity.getTableName();
		if(tableList.contains(tableName)){
			List<Map<String, Object>> idList = jdbcTemplate.queryForList("select id from "+tableName);
			if(idList != null && idList.size() >0 ) {
				for (Map<String, Object> map : idList) {
					if(map.get("id") != null) {
						deleteFormInstance(formModelEntity, (String) map.get("id"));
					}
				}
			}
		}

	}

    //设置流程状态
	private void setFormInstanceProcessStatus(FormDataSaveInstance formInstance, ProcessInstance processInstance){
		ItemInstance processStatusItemInstance = null;
		for(ItemInstance instance : formInstance.getItems()){
			if(instance.getSystemItemType() == SystemItemType.ProcessStatus){
				processStatusItemInstance = instance;
			}
		}
		if(processStatusItemInstance != null){
			ItemModelEntity itemModelEntity = itemModelService.get(processStatusItemInstance.getId());
			List<Option> lists = (List<Option>) JSON.parseArray(((ProcessStatusItemModelEntity) itemModelEntity).getProcessStatus(),Option.class);
			Map<String, Object> objectMap = new HashMap<>();
			for(Option option : lists){
				objectMap.put(option.getId(), option.getLabel());
			}
			//TODO 个人流程状态未配
			String status = "0";
			if(processInstance.getStatus()==ProcessInstance.Status.Ended){
				status = "1";
			}
			processStatusItemInstance.setDisplayValue(objectMap.get(status));
		}
	}

	protected boolean hasProcess(FormModelEntity formModel) {
		return formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getKey());
	}

	protected ProcessInstance wrapProcessInstance(String processKey, Map<String, Object> entity) {
		Process process = (Process) entity.get("process");
		String userId = (String) entity.get("userId");
		List<String> groupIds = (List<String>) entity.get("groupIds");

		if(process == null && processKey != null) {
			process = processService.get(processKey);
			userId = CurrentUserUtils.getCurrentUser().getId();
			groupIds = getGroupIds(userId);
		}

		entity = (Map<String, Object>) entity.get("processInstance");

		ProcessInstance pi = new ProcessInstance();
		pi.setFormId(process == null ? null : process.getFormId());
		pi.setFormName(process == null ? null : process.getFormName());
		pi.setFormTitle(process == null ? null : process.getFormTitle());
		pi.setStartTime((Date) entity.get("startTime"));
		pi.setEndTime((Date) entity.get("endTime"));
		pi.setCurrentTask((String) entity.get("currentTask"));
		pi.setCurrentHandler((String) entity.get("currentHandler"));

		pi.setMyTask(false);
		if (entity.get("endTime") == null) { // 未完结流程
			List<Map<String, Object>> workingTasks = (List) entity.get("workingTasks");
			if (workingTasks != null && workingTasks.size() > 0) {
				Map<String, Object> currentTask = workingTasks.get(0);
				for (Map<String, Object> workingTask : workingTasks) {
					if (isMyTask(workingTask, userId, groupIds)) {
						pi.setMyTask(true);
						currentTask = workingTask;
						break;
					}
				}
				pi.setCurrentTaskInstance(wrapTaskInstance(currentTask, pi, process));
			}
		}
		return pi;
	}

	protected WorkingTask wrapTaskInstance(Map<String, Object> currentTask, ProcessInstance processInstance, Process process) {
		WorkingTask ti = new WorkingTask();
		ti.setId((String) currentTask.get("id"));
		ti.setActivityId((String) currentTask.get("taskDefKey"));
		ti.setCreateTime((Date) currentTask.get("createTime"));
		ti.setClaimTime((Date) currentTask.get("claimTime"));
		ti.setAssignee((String) currentTask.get("assignee"));

		String previousTaskId = (String) currentTask.get("prevTaskId");
		ti.setReturnable(previousTaskId != null && previousTaskId.indexOf(",") < 0);
		ti.setRejectable(ti.isReturnable());
		ti.setJumpable(previousTaskId != null);
		ti.setSignable(ti.getClaimTime() == null);

		Activity activity = findActivity(process, ti.getActivityId());
		if (activity != null) {
			ti.setActivityName(activity.getName());
			ti.setFormDefinition(activity.getFormDefinition());
			if (processInstance.isMyTask()) {
				ti.setOperations(activity.getOperations());
			}
		}

		return ti;
	}

	protected Activity findActivity(Process process, String taskDefKey) {
		if(process != null) {
			for (Activity activity : process.getActivities()) {
				if (activity.getId().equals(taskDefKey)) {
					return activity;
				}
			}
		}
		
		return null;
	}

	protected boolean isMyTask(Map<String, Object> workingTask, String userId, List<String> groupIds) {
		if (userId.equals(workingTask.get("assignee"))) {
			return true;
		} else {
			List<Map<String, Object>> candidates = (List) workingTask.get("candidates");
			for (Map<String, Object> candidate : candidates) {
				if (userId.equals(candidate.get("userId")) || groupIds.stream().anyMatch(groupId -> groupId.equals(candidate.get("groupId")))) {
					return true;
				}
			}
		}
		return false;
	}

	protected void addProcessCriteria(Criteria criteria, int processStatus, int userStatus, String userId, List<String> groupIds, Date beginDate, Date endDate) {
		criteria = criteria.createAlias("processInstance", "pi");
		if (userStatus == 0) { // 查询用户待办列表
			criteria.add(Property.forName("pi.id").in(workListCriteria(userId, groupIds, beginDate, endDate)));
		} else if (userStatus == 1) { // 查询用户经办列表
			criteria.add(Property.forName("pi.id").in(doneListCriteria(userId, groupIds, beginDate, endDate)));
		} else { // 查询当前用户所有相关流程实例
			criteria.add(Restrictions.or(
					Property.forName("pi.id").in(workListCriteria(userId, groupIds, beginDate, endDate)),
					Property.forName("pi.id").in(doneListCriteria(userId, groupIds, beginDate, endDate))
			));
			if (processStatus == 0) { // 未办结
				criteria.add(Restrictions.isNull("pi.endTime"));
			} else if (processStatus == 1) { // 已办结
				criteria.add(Restrictions.isNotNull("pi.endTime"));
			}
		}
	}

	protected int getProcessStatusParameter(FormModelEntity formModelEntity, SystemItemType type, Map<String, Object> queryParameters) {
		int status = -1;
		Optional<ItemModelEntity> optional = formModelService.findAllItems(formModelEntity).stream().filter(item-> (item.getSystemItemType() == type)).findFirst();
		if (optional.isPresent() && queryParameters.get(optional.get().getId()) != null) {
			status = Integer.parseInt((String.valueOf(queryParameters.get(optional.get().getId()))));
			queryParameters.remove(optional.get().getId());
		}
		
		return status;
	}

	protected void queryWorkList(Criteria criteria, String userId, List<String> groups, Date beginDate, Date endDate) {
		criteria.createAlias("processInstance", "pi")
				.add(Property.forName("pi.id").in(workListCriteria(userId, groups, beginDate, endDate)));
	}

	protected void queryDoneList(Criteria criteria, String userId, List<String> groups, Date beginDate, Date endDate) {
		criteria.createAlias("processInstance", "pi")
				.add(Property.forName("pi.id").in(doneListCriteria(userId, groups, beginDate, endDate)));
	}

	protected void queryProcessInstanceList(Criteria criteria, String userId, List<String> groups, int processStatus, Date beginDate, Date endDate) {
		criteria.createAlias("processInstance", "pi")
				.add(Restrictions.or(
						Property.forName("pi.id").in(workListCriteria(userId, groups, beginDate, endDate)),
						Property.forName("pi.id").in(doneListCriteria(userId, groups, beginDate, endDate))
				))
				.addOrder(Order.desc("pi.id"));
	}

	protected DetachedCriteria workListCriteria(String userId, List<String> groups, Date beginDate, Date endDate) {

		DetachedCriteria detachedCriteria = DetachedCriteria.forEntityName("WorkingTask", "wt").createCriteria("wt.candidates", "c")
				.add(Restrictions.or(
						Restrictions.eq("wt.assignee", userId),
						Restrictions.and(
								Restrictions.isNull("wt.assignee"),
								Restrictions.or(
										Restrictions.eq("c.userId", userId),
										Restrictions.in("c.groupId", groups)
								)
						)
				))
				.setProjection(Projections.distinct(Property.forName("wt.processInstance")));
		if(beginDate != null){
			detachedCriteria.add(Restrictions.ge("createTime", beginDate));
		}
		if(endDate != null){
			detachedCriteria.add(Restrictions.le("createTime", endDate));
		}
		return detachedCriteria;
	}

	protected DetachedCriteria doneListCriteria(String userId, List<String> groups, Date beginDate, Date endDate) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forEntityName("DoneTask", "dt")
				.add(Restrictions.eq("dt.assignee", userId))
				.setProjection(Projections.distinct(Property.forName("dt.processInstance")));
		if(beginDate != null){
			detachedCriteria.add(Restrictions.ge("endTime", beginDate));
		}
		if(endDate != null){
			detachedCriteria.add(Restrictions.le("endTime", endDate));
		}
		return detachedCriteria;
	}

	protected List<String> getGroupIds(String userId) {
		List<String> result = new ArrayList<String>();

		// 添加部门/岗位列表
		List<Group> groups = userService.getGroups(userId);
		for (Group group : groups) {
			String type = "2".equals(group.getType()) ? "Position" : "Department";
			result.add(type.charAt(0) + group.getId());
		}

		// 添加角色列表
		List<Role> roles = userService.getRoles(userId, "1");
		for (Role role : roles) {
			result.add("R" + role.getId());
		}

		return result;
	}
}
