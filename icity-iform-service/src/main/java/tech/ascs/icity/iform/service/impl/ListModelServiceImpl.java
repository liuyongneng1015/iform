package tech.ascs.icity.iform.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DataModelInfo;
import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.ListModelService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.utils.BeanUtils;

public class ListModelServiceImpl extends DefaultJPAService<ListModelEntity> implements ListModelService {

	private JPAManager<ListSortItem> sortItemManager;

	private JPAManager<ListSearchItem> searchItemManager;

	private JPAManager<ListFunction> listFunctionManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public ListModelServiceImpl() {
		super(ListModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		sortItemManager = getJPAManagerFactory().getJPAManager(ListSortItem.class);
		searchItemManager = getJPAManagerFactory().getJPAManager(ListSearchItem.class);
		listFunctionManager = getJPAManagerFactory().getJPAManager(ListFunction.class);
	}

	@Override
	public ListModelEntity save(ListModelEntity entity) {
		validate(entity);
		if (!entity.isNew()) { // 先删除所有搜索字段及列表功能然后重建
			ListModelEntity old = get(entity.getId());

			List<String> sortItemIds = new ArrayList<String>();
			for (ListSortItem item : old.getSortItems()) {
				sortItemIds.add(item.getId());
			}

			List<String> searchItemIds = new ArrayList<String>();
			for (ListSearchItem item : old.getSearchItems()) {
				searchItemIds.add(item.getId());
			}

			List<String> functionIds = new ArrayList<String>();
			for (ListFunction item : old.getFunctions()) {
				functionIds.add(item.getId());
			}

			for (ListFunction function : entity.getFunctions()) {
				function.setId(null);
			}
			old.setName(entity.getName());
			old.setMultiSelect(entity.isMultiSelect());
			old.setMasterForm(entity.getMasterForm());
			old.setSlaverForms(entity.getSlaverForms());
			old.setSortItems(entity.getSortItems());
			old.setFunctions(entity.getFunctions());
			old.setSearchItems(entity.getSearchItems());
			old.setDisplayItems(entity.getDisplayItems());

			return doUpdate(old, sortItemIds, searchItemIds, functionIds);
		} else {
			return super.save(entity);
		}
	}

	@Override
	public List<ListModel> findListModelsByTableName(String tableName) {
		try {

			List<String> idlist = jdbcTemplate.query("select l.id from ifm_form_data_bind fd,ifm_list_model l,ifm_data_model d where fd.data_model=d.id and d.table_name ='"+tableName+"' and fd.form_model=l.master_form",
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getString("id");
						}});
			List<ListModelEntity> listModelEntities = query().filterIn("id",idlist).list();
			List<ListModel> list = new ArrayList<>();
			for(ListModelEntity listModelEntity : listModelEntities){
				list.add(BeanUtils.copy(listModelEntity, ListModel.class, new String[]{"displayItems","searchItems","functions","sortItems","slaverForms","masterForm"}));
			}
			return list;
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Transactional(readOnly = false)
	protected ListModelEntity doUpdate(ListModelEntity entity, List<String> deletedSortItemIds, List<String> searchItemIds, List<String> deletedFunctionIds) {
		if (deletedSortItemIds.size() > 0) {
			sortItemManager.deleteById(deletedSortItemIds.toArray(new String[] {}));
		}
		if (searchItemIds.size() > 0) {
			searchItemManager.deleteById(searchItemIds.toArray(new String[] {}));
		}
		if (deletedFunctionIds.size() > 0) {
			listFunctionManager.deleteById(deletedFunctionIds.toArray(new String[] {}));
		}
		return super.save(entity);
	}
	
	protected void validate(ListModelEntity entity) {
		// TODO 校验绑定数据模型、字段模型、流程及环节各相关ID存在
	}
}
