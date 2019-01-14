package tech.ascs.icity.iform.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.admin.api.model.ListFormIds;
import tech.ascs.icity.admin.client.ResourceService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.service.ListModelService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.utils.BeanUtils;

public class ListModelServiceImpl extends DefaultJPAService<ListModelEntity> implements ListModelService {

	private JPAManager<ListSortItem> sortItemManager;

	private JPAManager<ListSearchItem> searchItemManager;

	private JPAManager<ListFunction> listFunctionManager;

	private JPAManager<ReferenceItemModelEntity> referenceItemModelEntityManager;

	private JPAManager<SelectItemModelEntity> selectItemModelEntityManager;

	private JPAManager<QuickSearchEntity> quickSearchEntityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private FormModelService formModelService;

	@Autowired
	private ItemModelService itemModelService;
	@Autowired
	private ResourceService resourceService;

	public ListModelServiceImpl() {
		super(ListModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		sortItemManager = getJPAManagerFactory().getJPAManager(ListSortItem.class);
		searchItemManager = getJPAManagerFactory().getJPAManager(ListSearchItem.class);
		listFunctionManager = getJPAManagerFactory().getJPAManager(ListFunction.class);
		quickSearchEntityManager = getJPAManagerFactory().getJPAManager(QuickSearchEntity.class);
		selectItemModelEntityManager = getJPAManagerFactory().getJPAManager(SelectItemModelEntity.class);
		referenceItemModelEntityManager = getJPAManagerFactory().getJPAManager(ReferenceItemModelEntity.class);
	}

	/**
	 * 划分新增时和编辑时两套逻辑
	 * 新增时逻辑：通过主表单ID和附属表单ID在数据库查出表单，对entity赋值，然后直接保存
	 * 编辑时逻辑：把之前的列表字段列表，查询条件列表，快速导航列表，功能设置列表，排序字段列表，批量操作删除，然后绑定新的列表
	 * @param entity
	 * @return
	 */
	@Override
	public ListModelEntity save(ListModelEntity entity) {
		validate(entity);
		if (!entity.isNew()) { // 先删除所有搜索字段及列表功能然后重建
			//ListModelEntity实体的字段 id,name,description,multiSelect,masterForm,applicationId,slaverForms,sortItems,functions,searchItems,displayItems,quickSearchItems
			ListModelEntity old = get(entity.getId()) ;
			BeanUtils.copyProperties(entity, old, new String[] {"masterForm", "slaverForms", "sortItems", "searchItems", "functions", "displayItems", "quickSearchItems"});

			setFormModel(entity);

			List<ItemModelEntity> oldItemModelEntities = old.getDisplayItems();
			Map<String, ItemModelEntity> oldItemMap = new HashMap<>();
			for(ItemModelEntity itemModel : oldItemModelEntities){
				oldItemMap.put(itemModel.getId(), itemModel);
			}
			List<ItemModelEntity> itemModels = new ArrayList<ItemModelEntity>();
			if(entity.getDisplayItems() != null){
				for (ItemModelEntity itemModel : entity.getDisplayItems()) {
					if(!itemModel.isNew()){
						ItemModelEntity itemModelEntity = itemModelService.find(itemModel.getId());
						itemModels.add(itemModelEntity);
					}
				}
			}
			old.getDisplayItems().clear();
			old.setDisplayItems(itemModels);

			List<ListSortItem> oldSortItems = old.getSortItems();
			Map<String, ListSortItem> oldSortMap = new HashMap<>();
			for(ListSortItem sortItem : oldSortItems){
				oldSortMap.put(sortItem.getId(), sortItem);
			}

			List<ListFunction> oldFunctions = old.getFunctions();
			Map<String, ListFunction> oldFunctionMap = new HashMap<>();
			for(ListFunction function : oldFunctions) {
				oldFunctionMap.put(function.getId(), function);
			}

			List<ListSearchItem> oldSearchItems = old.getSearchItems();
			Map<String, ListSearchItem> oldSearchItemMap = new HashMap<>();
			for(ListSearchItem searchItem : oldSearchItems){
				oldSearchItemMap.put(searchItem.getId(), searchItem);
			}

			List<QuickSearchEntity> oldQuickSearches = old.getQuickSearchItems();
			Map<String, QuickSearchEntity> oldQuickSearchMap = new HashMap<>();
			for (QuickSearchEntity quickSearch : oldQuickSearches) {
				oldQuickSearchMap.put(quickSearch.getId(), quickSearch);
			}

			if (entity.getSortItems() != null) {
				List<ListSortItem> sortItems = new ArrayList<ListSortItem>();
				for (ListSortItem sortItem : entity.getSortItems()) {
					ListSortItem sortItemEntity =  sortItem.isNew() ? new ListSortItem() : oldSortMap.remove(sortItem.getId());
					sortItemEntity.setListModel(old);
					sortItemEntity.setItemModel(sortItem.getItemModel() == null || sortItem.getItemModel().isNew() ? null : itemModelService.find(sortItem.getItemModel().getId()));
					sortItemEntity.setAsc(sortItem.isAsc());
					// 排序字段过滤掉ID组件
					ItemModelEntity itemModelEntity = sortItemEntity.getItemModel();
					if (itemModelEntity!=null) {
						SystemItemType systemItemType = itemModelEntity.getSystemItemType();
						if (systemItemType!=null && "ID".equals(systemItemType.getValue())) {
							continue;
						}
					}
					sortItems.add(sortItemEntity);
				}
				old.setSortItems(sortItems);
			}

			if (entity.getSearchItems() != null) {
				List<ListSearchItem> searchItems = new ArrayList<ListSearchItem>();
				for (ListSearchItem searchItem : entity.getSearchItems()) {
					ListSearchItem searchItemEntity =  new ListSearchItem();
					if(searchItem.getItemModel() != null) {
						// 排序字段过滤掉ID组件
						ItemModelEntity itemModelEntity = searchItemEntity.getItemModel();
						if (itemModelEntity!=null) {
							SystemItemType systemItemType = itemModelEntity.getSystemItemType();
							if (systemItemType!=null && "ID".equals(systemItemType.getValue())) {
								continue;
							}
						}
						searchItemEntity.setItemModel(itemModelService.find(searchItem.getItemModel().getId()));
					}
					searchItemEntity.setListModel(old);
					if (searchItem.getSearch() == null) {
						throw new IFormException("控件【" + searchItemEntity.getItemModel().getName() + "】未定义搜索属性");
					}
					ItemSearchInfo searchInfo = new ItemSearchInfo();
					ItemSearchInfo dbSearch = searchItem.getSearch();
					BeanUtils.copyProperties(dbSearch, searchInfo);
					searchItemEntity.setSearch(searchInfo);
					searchItems.add(searchItemEntity);
				}
				old.setSearchItems(searchItems);
			}
			if (entity.getFunctions() != null) {
				List<ListFunction> functions = new ArrayList<>();
				for (ListFunction function : entity.getFunctions()) {
					ListFunction listFunction = function.isNew() ? new ListFunction() : oldFunctionMap.get(function.getId());
					BeanUtils.copyProperties(function, listFunction, new String[]{"listModel", "listModel"});
					listFunction.setListModel(old);
					functions.add(listFunction);
				}
				old.setFunctions(functions);
			}

			if (entity.getQuickSearchItems() != null) {
				List<QuickSearchEntity> quickSearches = new ArrayList<>();
				for (QuickSearchEntity quickSearch : entity.getQuickSearchItems()) {
					QuickSearchEntity quickSearchEntity = quickSearch.isNew() ? new QuickSearchEntity() : oldQuickSearchMap.get(quickSearch.getId());
					BeanUtils.copyProperties(quickSearch, quickSearchEntity, "itemModel", "listModel");
					if(quickSearch.getItemModel() != null) {
						quickSearchEntity.setItemModel(itemModelService.find(quickSearch.getItemModel().getId()));
					}
					quickSearchEntity.setListModel(old);
					quickSearches.add(quickSearchEntity);
				}
				old.setQuickSearchItems(quickSearches);
			}
			return doUpdate(old, oldSortMap.keySet(), oldSearchItemMap.keySet(), oldFunctionMap.keySet(), oldQuickSearchMap.keySet());
		} else {
            setFormModel(entity);
            return super.save(entity);
		}
	}

	private  void setFormModel(ListModelEntity entity){
        if(entity.getMasterForm() != null && !entity.getMasterForm().isNew()){
			FormModelEntity formModelEntity = formModelService.find(entity.getMasterForm().getId());
			if (formModelEntity == null) {
				throw new ICityException("id为"+entity.getMasterForm().getId()+"主表单已被删除");
			}
			entity.setMasterForm(formModelEntity);
        }

        if(entity.getSlaverForms() != null){
            List<FormModelEntity> list = new ArrayList<>();
            for(FormModelEntity formModelEntity : entity.getSlaverForms()) {
                if(!formModelEntity.isNew()) {
					FormModelEntity formModel = formModelService.find(entity.getMasterForm().getId());
					if (formModel==null) {
						throw new ICityException("id为"+entity.getMasterForm().getId()+"子表单已被删除");
					}
                    list.add(formModel);
                }
            }
            entity.getSlaverForms().clear();
            entity.setSlaverForms(list);
        }
    }

	@Override
	public void deleteSort(String id) {
		sortItemManager.deleteById(id);
	}

	@Override
	public void deleteSearch(String id) {
		searchItemManager.deleteById(id);
	}

	@Override
	public void deleteFunction(String id) {
		listFunctionManager.deleteById(id);
	}

	@Override
	public List<ListModel> findListModels() {
		List<ListModelEntity> listModelEntities = query().list();
		List<ListModel> list = new ArrayList<>();
		for(ListModelEntity listModelEntity : listModelEntities){
			ListModel listModel  = new ListModel();
			BeanUtils.copyProperties(listModelEntity, listModel, new String[]{"displayItems","searchItems","functions","sortItems","slaverForms","masterForm"});
			list.add(listModel);
		}
		return list;
	}

	@Override
	public List<ListModel> findListModelsByTableName(String tableName) {
		try {

			List<String> idlist = jdbcTemplate.query("select l.id from ifm_form_data_bind fd,ifm_list_model l,ifm_data_model d where fd.data_model=d.id and d.table_name ='"+tableName+"' and fd.form_model=l.master_form",
					(rs, rowNum) -> rs.getString("id"));
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

	@Override
	public List<ListModel> findListModelSimpleInfo() {
		return jdbcTemplate.query("select id, name, application_id from ifm_list_model",
			(rs, rowNum) -> {
				ListModel item = new ListModel();
				item.setId(rs.getString("id"));
				item.setName(rs.getString("name"));
				item.setApplicationId(rs.getString("application_id"));
				return item;
			}
		);
	}

	@Override
	public List<ListModel> findListModelSimpleByIds(List<String> ids) {
		List<ListModel> list = new ArrayList();
		if (ids!=null && ids.size()>0) {
			ids = ids.stream().filter(item->!StringUtils.isEmpty(item)).collect(Collectors.toList());
			if (ids!=null && ids.size()>0) {
				String idArrStr = String.join("','", ids);
				list = jdbcTemplate.query("select id,name from ifm_list_model where id in ('"+idArrStr+"')",
					(rs, rowNum) -> {
						ListModel item = new ListModel();
						item.setId(rs.getString("id"));
						item.setName(rs.getString("name"));
						return item;
					}
				);
			}
		}
		return list;
	}

	@Override
	public List<ListModel> findListModelsByItemModelIds(List<String> itemModelIds) {
		try {
			String itemIds = org.apache.commons.lang3.StringUtils.join(itemModelIds, "','");
			List<String> idlist = jdbcTemplate.query("select t.list_id from ifm_list_display_item t where t.item_id in ('"+itemIds+"')",
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getString("list_id");
						}});
			List<ListModel> list = new ArrayList<>();
			if(idlist == null || idlist.size() < 1){
				return list;
			}
			List<ListModelEntity> listModelEntities = query().filterIn("id",idlist).list();
			for(ListModelEntity listModelEntity : listModelEntities){
				list.add(BeanUtils.copy(listModelEntity, ListModel.class, new String[]{"displayItems","searchItems","functions","sortItems","slaverForms","masterForm"}));
			}
			return list;
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Transactional(readOnly = false)
	protected ListModelEntity doUpdate(ListModelEntity entity, Set<String> deletedSortItemIds, Set<String> searchItemIds, Set<String> deletedFunctionIds,
									   Set<String> deleteQuickSearchItemIds) {
		if (deletedSortItemIds.size() > 0) {
			sortItemManager.deleteById(deletedSortItemIds.toArray(new String[] {}));
		}
		if (searchItemIds.size() > 0) {
			searchItemManager.deleteById(searchItemIds.toArray(new String[] {}));
		}
		if (deletedFunctionIds.size() > 0) {
			listFunctionManager.deleteById(deletedFunctionIds.toArray(new String[] {}));
		}
		if (deleteQuickSearchItemIds.size() > 0) {
			quickSearchEntityManager.deleteById(deleteQuickSearchItemIds.toArray(new String[]{}));
		}
		return super.save(entity);
	}
	
	protected void validate(ListModelEntity entity) {
		// TODO 校验绑定数据模型、字段模型、流程及环节各相关ID存在
	}

	@Override
	public void checkListModelCanDelete(String id) {
		List<ReferenceItemModelEntity> list = referenceItemModelEntityManager.query().filterEqual("referenceList.id", id).list();
		for (ReferenceItemModelEntity item:list) {
			FormModelEntity formModelEntity = item.getFormModel();
			if (formModelEntity!=null) {
				throw new IFormException("该列表被"+formModelEntity.getName()+"表单绑定了，请先解绑对应关系");
			}
		}
	}

	@Override
	public List<BtnPermission> findListBtnPermission(ListModelEntity entity) {
		return assemblyBtnPermissions(entity, null);
	}

	@Override
	public List<BtnPermission> findFormBtnPermission(FormModelEntity entity) {
		return assemblyBtnPermissions(null, entity);
	}

	public List<BtnPermission> assemblyBtnPermissions(ListModelEntity listModel, FormModelEntity formModel) {
		List<ListFunction> listFunctions = null;
		String suffix = null;
		List<BtnPermission> btnPermissions = new ArrayList<>();
		if (listModel!=null) {
			listFunctions = listModel.getFunctions();
			suffix = "(列表)";
		} else if (formModel!=null) {
			listFunctions = formModel.getFunctions();
			suffix = "(表单)";
		} else {
			return null;
		}
		if (listFunctions.size()>0) {
			List<BtnPermission> permissions = new ArrayList<>();
			for (ListFunction function:listFunctions) {
				if (function.getAction()!=null && function.getLabel()!=null) {
					BtnPermission permission = new BtnPermission();
					permission.setId(function.getId());
					permission.setCode(function.getAction());
					permission.setName(function.getLabel() + suffix);
					permissions.add(permission);
				}
			}

		}
		return btnPermissions;
	}

	@Override
	public void submitListBtnPermission(ListModelEntity entity) {
		List<BtnPermission> btnPermission = assemblyBtnPermissions(entity, null);
		if (btnPermission !=null) {
			ListFormBtnPermission listFormBtnPermission = new ListFormBtnPermission();
			listFormBtnPermission.setListPermissions(btnPermission);
			tech.ascs.icity.admin.api.model.ListFormBtnPermission adminListFormBtnPermission = new tech.ascs.icity.admin.api.model.ListFormBtnPermission();
			BeanUtils.copyProperties(listFormBtnPermission, adminListFormBtnPermission);
			resourceService.editListFormPermissions(adminListFormBtnPermission);
		}
	}

	@Override
	public void submitFormBtnPermission(FormModelEntity entity) {
		List<BtnPermission> btnPermission = assemblyBtnPermissions(null, entity);
		if (btnPermission !=null) {
			ListFormBtnPermission listFormBtnPermission = new ListFormBtnPermission();
			listFormBtnPermission.setFormPermissions(btnPermission);
			tech.ascs.icity.admin.api.model.ListFormBtnPermission adminListFormBtnPermission = new tech.ascs.icity.admin.api.model.ListFormBtnPermission();
			BeanUtils.copyProperties(listFormBtnPermission, adminListFormBtnPermission);
			resourceService.editListFormPermissions(adminListFormBtnPermission);
		}
	}

	@Override
	public void deleteListBtnPermission(String listId) {
		// resourceService.deleteListFormPermissions(String listId,String formId);
		if (!StringUtils.isEmpty(listId)) {
			ListFormIds listFormIds = new ListFormIds();
			listFormIds.setListIds(Arrays.asList(listId));
			resourceService.deleteListFormPermissions(listFormIds);
		}
	}

	@Override
	public void deleteFormBtnPermission(String formId) {
		// resourceService.deleteListFormPermissions(String listId,String formId);
		if (!StringUtils.isEmpty(formId)) {
			ListFormIds listFormIds = new ListFormIds();
			listFormIds.setListIds(Arrays.asList(formId));
			resourceService.deleteListFormPermissions(listFormIds);
		}
	}
}
