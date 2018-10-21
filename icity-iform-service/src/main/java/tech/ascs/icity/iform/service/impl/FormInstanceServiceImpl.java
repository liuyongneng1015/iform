package tech.ascs.icity.iform.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import tech.ascs.icity.iform.api.model.FormInstance;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.api.model.ItemType;
import tech.ascs.icity.iform.api.model.SearchType;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemActivityInfo;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ItemSelectOption;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.iform.model.ListSearchItem;
import tech.ascs.icity.iform.model.ListSortItem;
import tech.ascs.icity.iform.service.FormInstanceService;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;

public class FormInstanceServiceImpl extends DefaultJPAService<FormModelEntity> implements FormInstanceService {

	public static class FormInstanceRowMapper implements RowMapper<FormInstance> {
		
		private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
			for (ItemModelEntity itemModel : formModel.getItems()) {
				ColumnModelEntity column = itemModel.getColumnModel();
				Object value = rs.getObject("f" + column.getColumnName());
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
					itemInstance.setDisplayValue(dateFormat.format(date));
					break;
				case Select:
					itemInstance.setValue(String.valueOf(value));
					for (ItemSelectOption option : itemModel.getOptions()) {
						if (option.getValue().equals(String.valueOf(value))) {
							itemInstance.setDisplayValue(option.getLabel());
							break;
						}
					}
					break;
//				case InputNumber:
//					break;
				default:
					itemInstance.setValue(value);
					itemInstance.setDisplayValue(String.valueOf(value));
					break;
			}
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Autowired
	private TaskService taskService;

	public FormInstanceServiceImpl() {
		super(FormModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
//		itemManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
	}

	@Override
	public List<FormInstance> listFormInstance(ListModelEntity listModel, Map<String, String> queryParameters) {
		String sql = buildListSql(listModel, queryParameters);
		return jdbcTemplate.query(sql, new FormInstanceRowMapper(listModel.getMasterForm()));
	}

	@Override
	public Page<FormInstance> pageFormInstance(ListModelEntity listModel, int page, int pagesize, Map<String, String> queryParameters) {
		String where = buildWhereSql(listModel, queryParameters);
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM if_").append(listModel.getMasterForm().getDataModels().get(0).getTableName()).append(where);
		int count = jdbcTemplate.queryForObject(countSql.toString(), Integer.class);
		
		StringBuilder sql = new StringBuilder("SELECT * FROM if_").append(listModel.getMasterForm().getDataModels().get(0).getTableName())
				.append(where).append(buildOrderBySql(listModel, queryParameters));

		String pageSql = buildPageSql(sql.toString(), page, pagesize);
		List<FormInstance> list = jdbcTemplate.query(pageSql, new FormInstanceRowMapper(listModel.getMasterForm()));
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

	@Override
	public FormInstance getFormInstance(FormModelEntity formModel, String instanceId) {
		StringBuilder sql = new StringBuilder("SELECT * FROM if_").append(formModel.getDataModels().get(0).getTableName()).append(" WHERE id='").append(instanceId).append("'");
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
			fields.append(", f").append(itemModel.getColumnModel().getColumnName());
			values.append(", ?");
			Object value;
			if (itemModel.getType() == ItemType.DatePicker) {
				value = new Date((Long) itemInstance.getValue());
			} else {
				value = itemInstance.getValue();
			}
			params.add(value);
			data.put(itemModel.getColumnModel().getColumnName(), value);
		}
		
		StringBuilder sql = new StringBuilder("INSERT INTO if_").append(formModel.getDataModels().get(0).getTableName())
				.append(" (").append(fields).append(") VALUES (").append(values).append(")");
		doUpdate(sql.toString(), params.toArray(new Object[] {}));

		// 启动流程
		if (formModel.getProcess() != null && formModel.getProcess().getKey() != null) {
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
			fields.append("f").append(itemModel.getColumnModel().getColumnName()).append("=?");

			Object value;
			if (itemModel.getType() == ItemType.DatePicker) {
				value = new Date((Long) itemInstance.getValue());
			} else {
				value = itemInstance.getValue();
			}
			params.add(value);
			data.put(itemModel.getColumnModel().getColumnName(), value);
		}
		StringBuilder sql = new StringBuilder("UPDATE if_").append(formModel.getDataModels().get(0).getTableName())
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
		StringBuilder sql = new StringBuilder("DELETE FROM if_").append(formModel.getDataModels().get(0).getTableName()).append(" WHERE id=?");
		doUpdate(sql.toString(), instanceId);
	}

	protected void updateProcessInfo(FormModelEntity formModel, String formInstanceId, String processInstanceId) {
		ProcessInstance processInstance = processInstanceService.get(processInstanceId);
		StringBuilder updateSql = new StringBuilder("UPDATE if_").append(formModel.getDataModels().get(0).getTableName())
				.append(" SET PROCESS_ID=?,PROCESS_INSTANCE=?,ACTIVITY_ID=?,ACTIVITY_INSTANCE=? WHERE id=?");
		doUpdate(updateSql.toString(), formModel.getProcess().getId(), processInstanceId, processInstance.getCurrentActivityId(), processInstance.getCurrentActivityInstanceId(), formInstanceId);
	}

	private String buildListSql(ListModelEntity listModel, Map<String, String> queryParameters) {
		StringBuilder sql = new StringBuilder("SELECT * FROM if_").append(listModel.getMasterForm().getDataModels().get(0).getTableName())
				.append(buildWhereSql(listModel, queryParameters))
				.append(buildOrderBySql(listModel, queryParameters));

		return sql.toString();
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

	private String buildWhereSql(ListModelEntity listModel, Map<String, String> queryParameters) {
		StringBuilder sql = new StringBuilder();
		for (ListSearchItem searchItem : listModel.getSearchItems()) {
			String value = queryParameters.get(searchItem.getItemModel().getId());
			if (StringUtils.hasText(value)) {
				if (sql.length() > 0) {
					sql.append(" AND ");
				}
				sql.append("f").append(searchItem.getItemModel().getColumnModel().getColumnName());
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
			sql.append("f").append(sortItem.getItemModel().getColumnModel().getColumnName()).append(sortItem.isAsc() ? "" : " DESC");
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
	private void doUpdate(String sql, @Nullable Object... args) {
		jdbcTemplate.update(sql, args);
	}
}
