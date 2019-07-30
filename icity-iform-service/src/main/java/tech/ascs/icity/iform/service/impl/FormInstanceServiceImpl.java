package tech.ascs.icity.iform.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import tech.ascs.icity.iflow.api.model.ProcessInstance;
import tech.ascs.icity.iflow.client.ProcessInstanceService;
import tech.ascs.icity.iflow.client.TaskService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormInstanceService;
import tech.ascs.icity.iform.service.FormInstanceServiceEx;
import tech.ascs.icity.iform.utils.OkHttpUtils;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;

public class FormInstanceServiceImpl extends DefaultJPAService<FormModelEntity> implements FormInstanceService {

	public class FormInstanceRowMapper implements RowMapper<FormInstance> {

		private FormModelEntity formModel;

		public FormInstanceRowMapper(FormModelEntity formModel) {
			this.formModel = formModel;
		}

		@Override
		public FormInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
			FormInstance formInstance = new FormInstance();
			formInstance.setFormId(formModel.getId());
			formInstance.setId(rs.getString("ID"));
			if (formModel.getProcess() != null && StringUtils.hasText(formModel.getProcess().getId())) {
				formInstance.setProcessId(rs.getString("PROCESS_ID"));
				formInstance.setProcessInstanceId(rs.getString("PROCESS_INSTANCE"));
				formInstance.setActivityId(rs.getString("ACTIVITY_ID"));
				formInstance.setActivityInstanceId(rs.getString("ACTIVITY_INSTANCE"));
			}

			List<ItemInstance> items = new ArrayList<ItemInstance>();
			for (ItemModelEntity itemModel : getAllColumnItems(formModel.getItems())) {
				ColumnModelEntity column = itemModel.getColumnModel();
				if(column == null){
					continue;
				}
				Object value = rs.getObject(column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName());
				ItemInstance itemInstance = new ItemInstance();
				itemInstance.setId(itemModel.getId());
				itemInstance.setProps(itemModel.getProps());
				itemInstance.setColumnModelId(column.getId());
				itemInstance.setColumnModelName(column.getColumnName());
				updateValue(itemModel, itemInstance, value);
				if (column.getKey()) {
					itemInstance.setVisible(false);
				} else {
					updateActivityInfo(itemModel, itemInstance, formInstance.getActivityId());
				}
				items.add(itemInstance);
				formInstance.addData(column.getColumnName(), itemInstance.getValue());
			}
			formInstance.setItems(items);

			return formInstance;
		}

		private  List<ItemModelEntity> getAllColumnItems(List<ItemModelEntity> itemModelEntities){
			List<ItemModelEntity> itemModels = new ArrayList<>();
			for(ItemModelEntity itemModelEntity : itemModelEntities){
				if(itemModelEntity.getColumnModel() != null){
					itemModels.add(itemModelEntity);
					continue;
				}
				if(itemModelEntity instanceof SubFormItemModelEntity){
					List<SubFormRowItemModelEntity> subRowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
					for(SubFormRowItemModelEntity rowItemModelEntity : subRowItems){
						for(ItemModelEntity itemModel : rowItemModelEntity.getItems()) {
							if (itemModel.getColumnModel() != null) {
								itemModels.add(itemModel);
								continue;
							}
						}
					}
				}else if(itemModelEntity instanceof RowItemModelEntity){
					for(ItemModelEntity itemModel : ((RowItemModelEntity) itemModelEntity).getItems()) {
						if (itemModel.getColumnModel() != null) {
							itemModels.add(itemModel);
							continue;
						}
					}
				}else if(itemModelEntity instanceof TabsItemModelEntity){
					for(TabPaneItemModelEntity itemModel : ((TabsItemModelEntity) itemModelEntity).getItems()) {
						if (itemModel.getColumnModel() != null) {
							itemModels.add(itemModel);
							continue;
						}
					}
				}
			}
			return itemModels;
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

		protected void updateValue(ItemModelEntity itemModel, ItemInstance itemInstance, Object value) {
			formInstanceServiceEx.updateValue(itemModel, itemInstance, value);
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Autowired
	private TaskService taskService;

	@Autowired
	public FormInstanceServiceEx formInstanceServiceEx;


	public FormInstanceServiceImpl() {
		super(FormModelEntity.class);
	}

	private JPAManager<ItemModelEntity> itemModelEntityJPAManager;

	private JPAManager<DataModelEntity> dataModelEntityJPAManager;

	@Override
	protected void initManager() {
		super.initManager();
		itemModelEntityJPAManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
		dataModelEntityJPAManager = getJPAManagerFactory().getJPAManager(DataModelEntity.class);
	}

	@Override
	public List<FormInstance> listFormInstance(ListModelEntity listModel, Map<String, String> queryParameters) {
		String sql = buildListSql(listModel, queryParameters);
		return jdbcTemplate.query(sql, new FormInstanceRowMapper(listModel.getMasterForm()));
	}

	@Override
	public Page<FormInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, String> queryParameters) {
		String where = buildWhereSql(listModel, queryParameters);
		DataModelEntity dataModelEntity = listModel.getMasterForm().getDataModels().get(0);
		String tableName = dataModelEntity.getTableName();
		String prefix = dataModelEntity.getPrefix() == null ? "" : dataModelEntity.getPrefix();
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM "+prefix).append(tableName).append(where);
		int count = jdbcTemplate.queryForObject(countSql.toString(), Integer.class);

		StringBuilder sql = new StringBuilder("SELECT * FROM "+prefix).append(tableName)
				.append(where).append(buildOrderBySql(listModel, queryParameters));

		String pageSql = buildPageSql(sql.toString(), page, pagesize);
		List<FormInstance> list = jdbcTemplate.query(pageSql, new FormInstanceRowMapper(listModel.getMasterForm()));
		//TODO select如何处理
		Map<String, Object> map = selectValue(tableName, list.get(0).getData());
		for(FormInstance instance : list) {
			changeSelectItemValue(tableName, instance.getData(), (Map<String, Object>)map.get("valueMap"));
		}

		Page<FormInstance> result = Page.get(page, pagesize);
		return result.data(count, list);
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
			ItemInstance itemInstance = new ItemInstance();
			itemInstance.setId(itemModel.getId());
			if (column.getKey()) {
				itemInstance.setVisible(false);
			} else {
				for (ItemActivityInfo activityInfo : itemModel.getActivities()) {
					if (activityInfo.getActivityId().equals(formModel.getProcess().getStartActivity())) {
						itemInstance.setVisible(activityInfo.isVisible());
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

	@Override
	public FormInstance getFormInstance(FormModelEntity formModel, String instanceId) {
		DataModelEntity dataModelEntity = formModel.getDataModels().get(0);
		String tableName = dataModelEntity.getTableName();
		String prefix = dataModelEntity.getPrefix() == null ? "" : dataModelEntity.getPrefix();
		StringBuilder sql = new StringBuilder("SELECT * FROM "+prefix).append(tableName).append(" WHERE id='").append(instanceId).append("'");
		List<FormInstance> list = jdbcTemplate.query(sql.toString(), new FormInstanceRowMapper(formModel));
		if (list.size() == 0) {
			throw new IFormException(404, "表单实例【" + instanceId + "】不存在");
		}
		return list.get(0);
	}

	@Override
	public String createFormInstance(FormModelEntity formModel, FormInstance formInstance) {
		String newId = UUID.randomUUID().toString().replace("-", "");
		List<Object> params = new ArrayList<Object>();
		StringBuilder fields = new StringBuilder("id");
		StringBuilder values = new StringBuilder("?");
		params.add(newId);

		Map<String, Object> data = new HashMap<String, Object>();
		for (ItemInstance itemInstance : formInstance.getItems()) {
			ItemModelEntity itemModel = getItemModel(formModel, itemInstance.getId());
			if (itemModel.getColumnModel().getColumnName().equalsIgnoreCase("id")) {
				continue;
			}
			ColumnModelEntity column = itemModel.getColumnModel();
			String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
			fields.append(", ").append(columnName);
			values.append(", ?");
			Object value;
			if (itemModel.getSystemItemType() == SystemItemType.CreateDate) {
				value = new Date((Long) itemInstance.getValue());
			} else {
				value = itemInstance.getValue();
			}
			params.add(value);
			data.put(itemModel.getColumnModel().getColumnName(), value);
		}
		DataModelEntity dataModelEntity = formModel.getDataModels().get(0);
		String tableName = dataModelEntity.getTableName();
		String prefix = dataModelEntity.getPrefix() == null ? "" : dataModelEntity.getPrefix();
		StringBuilder sql = new StringBuilder("INSERT INTO "+prefix).append(tableName)
				.append(" (").append(fields).append(") VALUES (").append(values).append(")");
		doUpdate(sql.toString(), params.toArray(new Object[] {}));

		// 启动流程
		if (formModel.getProcess() != null && formModel.getProcess().getKey() != null) {
			System.out.println("传给工作流的数据=====>>>>>"+ OkHttpUtils.mapToJson(data));
			String processInstanceId = processInstanceService.startProcess(formModel.getProcess().getKey(), newId, data);
			updateProcessInfo(formModel, newId, processInstanceId);
		}

		return newId;
	}

	@Override
	public void updateFormInstance(FormModelEntity formModel, String instanceId, FormInstance formInstance) {
		List<Object> params = new ArrayList<Object>();
		StringBuilder fields = new StringBuilder();
		Map<String, Object> data = new HashMap<String, Object>();
		for (ItemInstance itemInstance : formInstance.getItems()) {
			ItemModelEntity itemModel = getItemModel(formModel, itemInstance.getId());
			if (itemModel.getColumnModel().getColumnName().equalsIgnoreCase("id") && !instanceId.equals(itemInstance.getValue())) {
				throw new IFormException("表单实例ID不一致");
			}
			if (fields.length() > 0) {
				fields.append(",");
			}
			ColumnModelEntity column = itemModel.getColumnModel();
			String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
			fields.append(columnName).append("=?");

			Object value;
			if (itemModel.getSystemItemType() == SystemItemType.CreateDate) {
				value = new Date((Long) itemInstance.getValue());
			} else {
				value = itemInstance.getValue();
			}
			params.add(value);
			data.put(itemModel.getColumnModel().getColumnName(), value);
		}

		DataModelEntity dataModelEntity = formModel.getDataModels().get(0);
		String tableName = dataModelEntity.getTableName();
		String prefix = dataModelEntity.getPrefix() == null ? "" : dataModelEntity.getPrefix();

		StringBuilder sql = new StringBuilder("UPDATE "+prefix).append(tableName)
				.append(" SET ").append(fields).append(" WHERE id=?");
		params.add(instanceId);
		doUpdate(sql.toString(), params.toArray(new Object[] {}));

		// 流程操作
		if (formInstance.getActivityInstanceId() != null) {
			taskService.completeTask(formInstance.getActivityInstanceId(), data);
			updateProcessInfo(formModel, instanceId, formInstance.getProcessInstanceId());
		}
	}

	@Override
	public void deleteFormInstance(FormModelEntity formModel, String instanceId) {
		DataModelEntity dataModelEntity = formModel.getDataModels().get(0);
		String tableName = dataModelEntity.getTableName();
		String prefix = dataModelEntity.getPrefix() == null ? "" : dataModelEntity.getPrefix();
		StringBuilder sql = new StringBuilder("DELETE FROM "+prefix).append(tableName).append(" WHERE id=?");
		doUpdate(sql.toString(), instanceId);
	}

	@Override
	public Page<Map<String,Object>> pageSubFormInstance(String prefix, String tableName, int page, int pagesize) {
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM "+prefix).append(tableName);
		int count = jdbcTemplate.queryForObject(countSql.toString(), Integer.class);

		DataModelEntity dataModelEntity = dataModelEntityJPAManager.findUniqueByProperty("tableName", tableName);

		StringBuilder sql = new StringBuilder("SELECT * FROM "+prefix).append(tableName);
		String pageSql = buildPageSql(sql.toString(), page, pagesize);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(pageSql);

		//可选择的值
		Map<String, Object> mapSelect = selectValue(tableName, list.get(0));
		Map<String, Object> selectMap = (Map<String, Object>) mapSelect.get("choiceMap");
		for(Map<String, Object> map : list) {
			changeSelectItemValue(tableName, map, (Map<String, Object>) mapSelect.get("valueMap"));
		}

		List<Map<String, Object>> outPut = new ArrayList<>();
		//子表
		if(dataModelEntity.getModelType() == DataModelType.Slaver){
			SubFormItemModelEntity subFormItemModelEntity = (SubFormItemModelEntity) itemModelEntityJPAManager.findUniqueByProperty("tableName", tableName);
			List<SubFormRowItemModelEntity> rowItems = subFormItemModelEntity.getItems();
			for(Map<String, Object> map : list) {
				for(SubFormRowItemModelEntity entity : rowItems){
					Map<String, Object> mapStr =new HashMap<>();
					List<ItemModelEntity> itemModelEntities = entity.getItems();
					for(ItemModelEntity itemModelEntity: itemModelEntities){
						mapStr.put(itemModelEntity.getId(), map.get(itemModelEntity.getColumnModel().getColumnName()));
					}
					outPut.add(mapStr);
				}
			}
		}

		Page<Map<String,Object>> result = Page.get(page, pagesize);
		return result.data(count, outPut);

	}

	private void changeSelectItemValue(String tableName, Map<String, Object> datamap , Map<String, Object> valueMap){
		//改变选择宽的值
		for (String key : datamap.keySet()) {
			if(valueMap.containsKey(key)){
				datamap.put(key, getSelectValue(tableName, key,String.valueOf(datamap.get(key))));
			}
		}
	}

	private Map<String, Object> selectValue(String tableName, Map<String,Object> mapkey){
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> choiceMap = new HashMap<>();
		Map<String, Object> valueMap = new HashMap<>();
		for(String key : mapkey.keySet()) {
			List<Map<String, Object>> mapDataList = jdbcTemplate.queryForList(" select i.id, i.type from ifm_data_model d,ifm_column_model c,ifm_item_model i where c.id=i.column_id  and c.data_model_id=d.id and d.table_name ='" + tableName + "' and c.column_name='" + key + "' ");
			if (mapDataList == null || mapDataList.size() < 1) {
				continue;
			}
			Map<String, Object> item_type = mapDataList.get(0);
			if(ItemType.Select.getValue().equals(item_type.get("type"))) {
				ItemModelEntity itemModelEntity = itemModelEntityJPAManager.find((String)item_type.get("id"));
				if(((SelectItemModelEntity)itemModelEntity).getSelectReferenceType() == SelectReferenceType.Table){
					//TODO 根据类型查找对应的值
					List<String> tableValues = jdbcTemplate.queryForList(" select " + ((SelectItemModelEntity) itemModelEntity).getReferenceValueColumn() + " from " + ((SelectItemModelEntity) itemModelEntity).getReferenceTable(),String.class);
					//selectMap.put(itemModelEntity.getId(), tableValues);
					//valueMap.put(key, tableValues);
				}else if(((SelectItemModelEntity)itemModelEntity).getSelectReferenceType() == SelectReferenceType.Dictionary){
					//TODO 根据类型查找对应的值
					List<DictionaryDataItemModel> tableValues = jdbcTemplate.queryForList(" select *  from ifm_dictionary_item where parent_id = "+((SelectItemModelEntity) itemModelEntity).getReferenceDictionaryId(), DictionaryDataItemModel.class);
					choiceMap.put(itemModelEntity.getId(), tableValues);
					valueMap.put(key, tableValues);
				}else if(((SelectItemModelEntity)itemModelEntity).getSelectReferenceType() == SelectReferenceType.Fixed){
					//TODO 根据类型查找对应的值
					List<Option> tableValues = jdbcTemplate.queryForList(" select *  from ifm_item_select_option where item_id = "+itemModelEntity.getId(), Option.class);
					choiceMap.put(itemModelEntity.getId(), tableValues);
					valueMap.put(key, tableValues);
				}
			}
		}
		map.put("choiceMap", choiceMap);
		map.put("valueMap", valueMap);
		return map;
	}

	private String getSelectValue(String tableName, String key, String value){
		List<Map<String, Object>> mapDataList = jdbcTemplate.queryForList(" select i.id, i.type from ifm_data_model d,ifm_column_model c,ifm_item_model i where c.id=i.column_id  and c.data_model_id=d.id and d.table_name ='" + tableName + "' and c.column_name='" + key + "' ");
		if (mapDataList == null || mapDataList.size() < 1) {
			return  value;
		}
		Map<String, Object> item_type = mapDataList.get(0);
		if(ItemType.Select.getValue().equals(item_type.get("type"))) {
			ItemModelEntity itemModelEntity = itemModelEntityJPAManager.find((String)item_type.get("id"));
			if(((SelectItemModelEntity)itemModelEntity).getSelectReferenceType() == SelectReferenceType.Table){
				//TODO 根据类型查找对应的值
				//List<String> tableValues = jdbcTemplate.queryForList(" select " + ((SelectItemModelEntity) itemModelEntity).getReferenceValueColumn() + " from " + ((SelectItemModelEntity) itemModelEntity).getReferenceTable(),String.class);
			}else if(((SelectItemModelEntity)itemModelEntity).getSelectReferenceType() == SelectReferenceType.Dictionary){
				//TODO 根据类型查找对应的值
				List<DictionaryDataItemModel> tableValues = jdbcTemplate.queryForList(" select *  from ifm_dictionary_item where parent_id = "+((SelectItemModelEntity) itemModelEntity).getReferenceDictionaryId(), DictionaryDataItemModel.class);
				for(DictionaryDataItemModel model : tableValues){
					if(model.getCode().equals(value)){
						return model.getName();
					}
				}
			}else if(((SelectItemModelEntity)itemModelEntity).getSelectReferenceType() == SelectReferenceType.Fixed){
				//TODO 根据类型查找对应的值
				List<Option> tableValues = jdbcTemplate.queryForList(" select * from ifm_item_select_option where item_id = "+itemModelEntity.getId(), Option.class);
				for(Option model : tableValues){
					if(model.getLabel().equals(value)){
						return model.getValue();
					}
				}
			}
		}
		return value;
	}

	protected void updateProcessInfo(FormModelEntity formModel, String formInstanceId, String processInstanceId) {
		ProcessInstance processInstance = processInstanceService.get(processInstanceId);
		if(processInstance == null || processInstance.getCurrentTaskInstance() == null){
		    return;
        }
		DataModelEntity dataModelEntity = formModel.getDataModels().get(0);
		String tableName = dataModelEntity.getTableName();
		String prefix = dataModelEntity.getPrefix() == null ? "" : dataModelEntity.getPrefix();
		StringBuilder updateSql = new StringBuilder("UPDATE "+prefix).append(tableName)
				.append(" SET PROCESS_ID=?,PROCESS_INSTANCE=?,ACTIVITY_ID=?,ACTIVITY_INSTANCE=? WHERE id=?");
		doUpdate(updateSql.toString(), formModel.getProcess().getId(), processInstanceId, processInstance.getCurrentTaskInstance().getActivityId(), processInstance.getCurrentTaskInstance().getId(), formInstanceId);
	}

	private String buildListSql(ListModelEntity listModel, Map<String, String> queryParameters) {
		DataModelEntity dataModelEntity = listModel.getMasterForm().getDataModels().get(0);
		String tableName = dataModelEntity.getTableName();
		String prefix = dataModelEntity.getPrefix() == null ? "" : dataModelEntity.getPrefix();
		StringBuilder sql = new StringBuilder("SELECT * FROM "+prefix).append(tableName)
				.append(buildWhereSql(listModel, queryParameters))
				.append(buildOrderBySql(listModel, queryParameters));

		return sql.toString();
	}

	@Override
	public String buildPageSql(String sql, int page, int pagesize) {
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

	private String buildWhereSql(ListModelEntity listModel, Map<String, String> queryParameters) {
		StringBuilder sql = new StringBuilder();
		for (ListSearchItem searchItem : listModel.getSearchItems()) {
			String value = queryParameters.get(searchItem.getItemModel().getId());
			if (StringUtils.hasText(value)) {
				if (sql.length() > 0) {
					sql.append(" AND ");
				}
				ColumnModelEntity column = searchItem.getItemModel().getColumnModel();
				String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
				sql.append(columnName);
				if (searchItem.getSearch().getSearchType() == SearchType.Like) {
					sql.append(" LIKE '%").append(value).append("%'");
				} else {
					sql.append("=");
					if (searchItem.getItemModel().getType() == ItemType.InputNumber) {
						sql.append(value);
					} else {
						sql.append("'").append(value).append("'");
					}
				}
			}
		}
		return sql.length() > 0 ? " WHERE " + sql : sql.toString();
	}

	private String buildOrderBySql(ListModelEntity listModel, Map<String, String> queryParameters) {
		StringBuilder sql = new StringBuilder();
		for (ListSortItem sortItem : listModel.getSortItems()) {
			if (sql.length() > 0) {
				sql.append(",");
			}
			ColumnModelEntity column = sortItem.getItemModel().getColumnModel();
			String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
			sql.append(columnName).append(sortItem.isAsc() ? "" : " DESC");
		}
		return sql.length() > 0 ? " ORDER BY " + sql : sql.toString();
	}

	private ItemModelEntity getItemModel(FormModelEntity formModel, String itemModelId) {
		for (ItemModelEntity itemModel : formModel.getItems()) {
			if (itemModel.getId().equals(itemModelId)) {
				return itemModel;
			}
		}
		throw new IFormException(404, "表单控件模型不存在");
	}

	@Transactional(readOnly = false)
	public void doUpdate(String sql, @Nullable Object... args) {
		jdbcTemplate.update(sql, args);
	}
}
