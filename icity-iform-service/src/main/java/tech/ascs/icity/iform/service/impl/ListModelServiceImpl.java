package tech.ascs.icity.iform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import tech.ascs.icity.iform.api.model.export.ExportFunctionModel;
import tech.ascs.icity.iform.api.model.export.ImportFunctionModel;
import tech.ascs.icity.iform.function.ThreeConsumer;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.utils.BeanCopiers;
import tech.ascs.icity.iform.utils.ExportListFunctionUtils;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListModelServiceImpl extends DefaultJPAService<ListModelEntity> implements ListModelService {

	private JPAManager<ListSortItem> sortItemManager;

	private JPAManager<ListSearchItem> searchItemManager;

	private JPAManager<ListFunction> listFunctionManager;

	private JPAManager<ReferenceItemModelEntity> referenceItemModelEntityManager;

	private JPAManager<ColumnModelEntity> columnModelManager;

	private JPAManager<QuickSearchEntity> quickSearchEntityManager;

	private JPAManager<FormModelEntity> formModelEntityJPAManager;

	private JPAManager<DataModelEntity> dataModelEntityJPAManager;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private FormModelService formModelService;

	@Autowired
	private ItemModelService itemModelService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private FormInstanceService formInstanceService;

	@Autowired
	private ExportDataService exportDataService;

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
		columnModelManager = getJPAManagerFactory().getJPAManager(ColumnModelEntity.class);
		referenceItemModelEntityManager = getJPAManagerFactory().getJPAManager(ReferenceItemModelEntity.class);
		formModelEntityJPAManager = getJPAManagerFactory().getJPAManager(FormModelEntity.class);
		dataModelEntityJPAManager = getJPAManagerFactory().getJPAManager(DataModelEntity.class);
	}

	@Transactional(rollbackFor = Exception.class)
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
				List<ListSearchItem> searchItems = new ArrayList();
				for (int i = 0; i < entity.getSearchItems().size(); i++) {
					ListSearchItem searchItem = entity.getSearchItems().get(i);
					ListSearchItem searchItemEntity =  new ListSearchItem();
					BeanUtils.copyProperties(searchItem, searchItemEntity, "listModel", "itemModel", "search");
					if(searchItem.getItemModel() != null) {
						ItemModelEntity itemModelEntity = searchItemEntity.getItemModel();
						if (itemModelEntity!=null && itemModelEntity.getSystemItemType()!=null && "ID".equals(itemModelEntity.getSystemItemType().getValue())) {
							continue;
						}
						ItemModelEntity searchItemModelEntity = itemModelService.find(searchItem.getItemModel().getId());
						if (searchItem.getParseArea()!=null && searchItem.getParseArea().contains("FuzzyQuery")) {
							ItemType itemType = searchItemModelEntity.getType();
							if (ItemType.InputNumber== itemType || ItemType.DatePicker==itemType || ItemType.TimePicker==itemType) {
								throw new ICityException("数字控件和时间控件不能加到全文索引");
							}
						}
						searchItemEntity.setItemModel(searchItemModelEntity);
					}
					searchItemEntity.setListModel(old);
					if (searchItem.getSearch() == null) {
						throw new IFormException("控件【" + searchItemEntity.getItemModel().getName() + "】未定义搜索属性");
					}
					ItemSearchInfo searchInfo = new ItemSearchInfo();
					BeanUtils.copyProperties(searchItem.getSearch(), searchInfo);
					searchItemEntity.setSearch(searchInfo);
					searchItems.add(searchItemEntity);
				}
				old.setSearchItems(searchItems);
			}
			if (entity.getFunctions() != null) {
				List<ListFunction> functions = new ArrayList<>();
				for (ListFunction function : entity.getFunctions()) {
					ListFunction listFunction = function.isNew() ? new ListFunction() : oldFunctionMap.get(function.getId());
					BeanUtils.copyProperties(function, listFunction, new String[]{"listModel", "formModel"});
					setFunction(function, listFunction);
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
					if(quickSearch.getItemModel() != null && !StringUtils.isEmpty(quickSearch.getItemModel().getId())) {
						quickSearchEntity.setItemModel(itemModelService.find(quickSearch.getItemModel().getId()));
					}
					quickSearchEntity.setListModel(old);
					quickSearches.add(quickSearchEntity);
				}
				old.setQuickSearchItems(quickSearches);
			}
			ListModelEntity returnEntity = doUpdate(old, oldSortMap.keySet(), oldSearchItemMap.keySet(), oldFunctionMap.keySet(), oldQuickSearchMap.keySet());
			// 给admin服务提交按钮权限
			submitListBtnPermission(returnEntity);
			return returnEntity;
		} else {
            setFormModel(entity);
			ListModelEntity returnEntity = super.save(entity);
			return returnEntity;
		}
	}

	//设置列表功能
	private void setFunction(ListFunction functionParams, ListFunction newListFunction){
		newListFunction.setUrl(functionParams.getUrl());
		// 请求方式，GET、HEAD、POST、PUT、DELETE、CONNECT、OPTIONS、TRACE
		newListFunction.setMethod(functionParams.getMethod());
		newListFunction.setIcon(functionParams.getIcon());
		newListFunction.setStyle(functionParams.getStyle());
		newListFunction.setParamCondition(functionParams.getParamCondition());
		newListFunction.setFunctionType(functionParams.getFunctionType());
		newListFunction.setHasConfirmForm(functionParams.getHasConfirmForm());
		newListFunction.setConfirmForm(functionParams.getConfirmForm());
		newListFunction.setReturnOperation(functionParams.getReturnOperation());
		newListFunction.setJumpNewUrl(functionParams.getJumpNewUrl());
		//显示时机 若为空标识所有时机都显示
		newListFunction.setDisplayTiming(functionParams.getDisplayTiming());
		// 返回结果
		newListFunction.setReturnResult(functionParams.getReturnResult());
		// 解析区域
		newListFunction.setParseArea(functionParams.getParseArea());
		// 是否是系统的按钮
		newListFunction.setSystemBtn(functionParams.getSystemBtn());
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
	public List<ListModel> findListModelsByTableName(String tableName) {
		try {
			List<String> idlist = jdbcTemplate.query("select l.id from ifm_list_model l,ifm_form_data_bind fd,ifm_data_model d where fd.form_model=l.master_form and fd.data_model=d.id and d.table_name ='"+tableName+"'",
					(rs, rowNum) -> rs.getString("id"));
			List<ListModelEntity> listModelEntities = query().filterIn("id",idlist).list();
			List<ListModel> list = new ArrayList<>();
			for(ListModelEntity listModelEntity : listModelEntities){
				ListModel listModel = new ListModel();
				BeanUtils.copyProperties(listModelEntity, listModel, new String[]{"displayItems","searchItems","functions","sortItems","slaverForms","masterForm", "protalListTemplate", "appListTemplate"});
				if(listModelEntity.getMasterForm() != null){
					FormModel masterForm = new FormModel();
					masterForm.setId(listModelEntity.getMasterForm().getId());
					listModel.setMasterForm(masterForm);
				}
				list.add(listModel);
			}
			return list;
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<ListModel> findListModelSimpleInfo(String name, String applicationId, String formId, Boolean hasAcvititi) {
		String sql = " SELECT l.id, l.name, l.application_id, f.id form_id, f.name form_name FROM ifm_list_model l LEFT JOIN ifm_form_model f ON l.master_form=f.id WHERE l.master_form IS NOT NULL ";
		if (hasAcvititi!=null) {
			if (true == hasAcvititi) {
				sql += " AND f.process_key IS NOT NULL ";
			} else {
				sql += " AND f.process_key IS NULL ";
			}
		}
		if (!StringUtils.isEmpty(applicationId)) {
			sql += " AND l.application_id='"+applicationId+"' ";
		}
		if (!StringUtils.isEmpty(name)) {
			sql += " AND l.name LIKE '%"+name+"%' ";
		}
		if (!StringUtils.isEmpty(formId)) {
			sql += " AND l.master_form = '"+formId+"' ";
		}
		sql += " ORDER BY l.id DESC ";
		return assemblySqlListModel(sql);
	}

	@Override
	public Page<ListModel> findListModelSimplePageInfo(String name, String applicationId, int page, int pagesize) {
		String dataSql = " SELECT l.id, l.name, l.application_id, f.id form_id, f.name form_name ";
		String countSql = " SELECT COUNT(1) ";
		String querySql = " FROM ifm_list_model l LEFT JOIN ifm_form_model f ON l.master_form=f.id WHERE f.id IS NOT NULL ";
		if (!StringUtils.isEmpty(applicationId)) {
			querySql += " AND l.application_id='"+applicationId+"' ";
		}
		if (!StringUtils.isEmpty(name)) {
			querySql += " AND l.name LIKE '%"+name+"%' ";
		}
		List<ListModel> data = assemblySqlListModel(dataSql+formInstanceService.buildPageSql(querySql+" ORDER BY l.id DESC ", page, pagesize));
		int count = jdbcTemplate.queryForObject(countSql+querySql, Integer.class);
		Page<ListModel> result = Page.get(page, pagesize);
		return result.data(count, data);
	}

	private List<ListModel> assemblySqlListModel(String sql){
		List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
		List<ListModel> returnList = new ArrayList<>();
		for (Map<String,Object> map:list) {
			ListModel listModel = new ListModel();
			listModel.setId((String) map.get("id"));
			listModel.setName((String) map.get("name"));
			listModel.setApplicationId((String) map.get("application_id"));
			if (map.get("form_id")!=null) {
				FormModel formModel = new FormModel();
				formModel.setId((String) map.get("form_id"));
				formModel.setName((String) map.get("form_name"));
				listModel.setMasterForm(formModel);
			}
			returnList.add(listModel);
		}
		return returnList;
	}

	@Override
	public List<ListModel> findListModelSimpleByIds(List<String> ids) {
		List<ListModel> list = new ArrayList();
		if (ids==null || ids.size()==0) {
			return list;
		}
		ids = ids.stream().filter(item->!StringUtils.isEmpty(item)).collect(Collectors.toList());
		if (ids==null || ids.size()==0) {
			return list;
		}
		String idArrStr = String.join("','", ids);
		List<Map<String, Object>> data = jdbcTemplate.queryForList("select id, name, master_form from ifm_list_model where master_form IS NOT NULL AND id in ('"+idArrStr+"')");
		if (data.size()==0) {
			return list;
		}
		List<String> formIds = new ArrayList();
		for (Map<String, Object> map:data) {
			formIds.add(map.get("master_form").toString());
		}
		List<FormModelEntity> forms = formModelService.query().filterIn("id", formIds).list();
		Map<String,FormModel> formModelMap = new HashMap();
		for (FormModelEntity formModelEntity:forms) {
			String id = formModelEntity.getId();
			FormModel formModel = new FormModel();
			formModel.setId(id);
			formModel.setName(formModelEntity.getName());
			FormProcessInfo processEntity = formModelEntity.getProcess();
			if (processEntity!=null) {
				FormModel.ProceeeModel process = new FormModel.ProceeeModel();
				process.setId(processEntity.getId());
				process.setKey(processEntity.getKey());
				process.setName(processEntity.getName());
				formModel.setProcess(process);
			}
			formModelMap.put(id, formModel);
		}

		for (Map<String, Object> map:data) {
			ListModel item = new ListModel();
			item.setId(map.get("id")==null?"":map.get("id").toString());
			item.setName(map.get("name")==null?"":map.get("name").toString());
			item.setMasterForm(formModelMap.get(map.get("master_form").toString()));
			list.add(item);
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
			if(idlist == null || idlist.size() < 1) {
				return list;
			}
			List<ListModelEntity> listModelEntities = query().filterIn("id",idlist).list();
			for(ListModelEntity listModelEntity : listModelEntities){
				list.add(entityToModel(listModelEntity));
			}
			return list;
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	private ListModel entityToModel(ListModelEntity listModelEntity){
		ListModel listModel = new ListModel();
		BeanUtils.copyProperties(listModelEntity, listModel, new String[]{"displayItems","searchItems","functions","sortItems","slaverForms","masterForm", "protalListTemplate", "appListTemplate"});
		return listModel;
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
	public void setItemReferenceListModelNull(String id) {
		List<ReferenceItemModelEntity> list = referenceItemModelEntityManager.query().filterEqual("referenceList.id", id).list();
		for (ReferenceItemModelEntity item:list) {
			item.setReferenceList(null);
//			FormModelEntity formModelEntity = item.getFormModel();
//			if (formModelEntity!=null) {
//				throw new IFormException("该列表被"+formModelEntity.getName()+"表单绑定了，请先解绑对应关系");
//			}
		}
		if (list.size()>0) {
			referenceItemModelEntityManager.save(list.toArray(new ReferenceItemModelEntity[]{}));
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
		List<BtnPermission> btnPermissions = new ArrayList<>();
		if (listModel!=null) {
			List<ListFunction> listFunctions = listModel.getFunctions();
			if (listFunctions!=null && listFunctions.size()>0) {
				for (ListFunction function:listFunctions) {
					if (function.getAction()!=null && function.getLabel()!=null && function.isVisible()) {
						BtnPermission permission = new BtnPermission();
						permission.setId(function.getId());
						permission.setCode(function.getAction());
						permission.setName(function.getLabel() + "(列表)");
						btnPermissions.add(permission);
					}
				}
			}
		} else if (formModel!=null) {
			List<ListFunction> listFunctions = formModel.getFunctions();
			// 当表单的关联了流程，不把按钮功能传给admin服务
			if (listFunctions!=null && listFunctions.size()>0 && formModel.getProcess()==null) {
				for (ListFunction function:listFunctions) {
					if (function.getAction()!=null && function.getLabel()!=null && function.isVisible()) {
						BtnPermission permission = new BtnPermission();
						permission.setId(function.getId());
						permission.setCode(function.getAction());
						permission.setName(function.getLabel() + "(表单)");
						btnPermissions.add(permission);
					}
				}
			}
		}
		return btnPermissions;
	}

	@Override
	public void submitListBtnPermission(ListModelEntity entity) {
		if (StringUtils.hasText(entity.getApplicationId())) {
			List<BtnPermission> listBtnPermission = assemblyBtnPermissions(entity, null);
			if (listBtnPermission != null) {
				ListFormBtnPermission listFormBtnPermission = new ListFormBtnPermission();
				listFormBtnPermission.setListId(entity.getId());
				listFormBtnPermission.setListPermissions(listBtnPermission);
				tech.ascs.icity.admin.api.model.ListFormBtnPermission adminListFormBtnPermission = new tech.ascs.icity.admin.api.model.ListFormBtnPermission();
				BeanUtils.copyProperties(listFormBtnPermission, adminListFormBtnPermission);
				resourceService.editListFormPermissions(adminListFormBtnPermission);
			}
		}
	}

	@Override
	public void submitFormBtnPermission(FormModelEntity entity) {
		if (entity!=null && StringUtils.hasText(entity.getId())) {
			List<BtnPermission> formBtnPermissions = assemblyBtnPermissions(null, entity);
			if (formBtnPermissions != null) {
				ListFormBtnPermission listFormBtnPermission = new ListFormBtnPermission();
				listFormBtnPermission.setFormId(entity.getId());
				listFormBtnPermission.setFormPermissions(formBtnPermissions);
				tech.ascs.icity.admin.api.model.ListFormBtnPermission adminListFormBtnPermission = new tech.ascs.icity.admin.api.model.ListFormBtnPermission();
				BeanUtils.copyProperties(listFormBtnPermission, adminListFormBtnPermission);
				List<String> listIds = query().filterEqual("masterForm.id", entity.getId()).list().stream().map(item->item.getId()).collect(Collectors.toList());
				if (listIds!=null && listIds.size()>0) {
					adminListFormBtnPermission.setListIds(listIds);
					resourceService.editListFormPermissions(adminListFormBtnPermission);
				}
			}
		}
	}

	@Override
	public void deleteListBtnPermission(String listId) {
		if (!StringUtils.isEmpty(listId)) {
			ListFormIds listFormIds = new ListFormIds();
			listFormIds.setListIds(Arrays.asList(listId));
			resourceService.deleteListFormPermissions(listFormIds);
		}
	}

	@Override
	public void deleteFormBtnPermission(String formId, List<String> listIds) {
		if (!StringUtils.isEmpty(formId) && listIds!=null && listIds.size()>0) {
			ListFormIds listFormIds = new ListFormIds();
			listFormIds.setListIds(Arrays.asList(formId));
			listFormIds.setListIds(listIds);
			resourceService.deleteListFormPermissions(listFormIds);
		}
	}

	@Override
	public List<ListModelEntity> findListModelsByItemModelId(String itemModelId) {
		try {
			List<String> idlist = jdbcTemplate.query("select t.list_id from ifm_list_display_item t where t.item_id in ('"+itemModelId+"')",
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getString("list_id");
						}});
			List<ListModelEntity> list = new ArrayList<>();
			if(idlist == null || idlist.size() < 1) {
				return list;
			}
			List<ListModelEntity> listModelEntities = query().filterIn("id",idlist).list();
			return listModelEntities;
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public ListModel getFirstListModelByTableName(String tableName) {
		if(!StringUtils.hasText(tableName)){
			throw new IFormException("请求参数【"+tableName+"】不能为空");
		}
		List<ListModel> listModels = findListModelsByTableName(tableName);
		return listModels  == null || listModels.size() < 1 ? null : listModels.get(0);
	}

    @Override
    public ListModelEntity toListModelEntity(ListModel listModel) {
        verfyListName(listModel);

        ListModelEntity listModelEntity =  new ListModelEntity() ;
        BeanUtils.copyProperties(listModel, listModelEntity, new String[] {"dataModels", "masterForm","slaverForms","sortItems", "searchItems","functions","displayItems", "quickSearchItems", "relevanceItemModelList", "protalListTemplate", "appListTemplate"});

        try {
            List<Map> protalListTemplate = listModel.getProtalListTemplate();
            List<Map> appListTemplate = listModel.getAppListTemplate();
            if (appListTemplate != null && appListTemplate.size()>0) {
                listModelEntity.setAppListTemplate(objectMapper.writeValueAsString(appListTemplate));
            }
            if (protalListTemplate != null && protalListTemplate.size()>0) {
                listModelEntity.setProtalListTemplate(objectMapper.writeValueAsString(protalListTemplate));
            }
        } catch (IOException e) {
            throw new ICityException(e.getLocalizedMessage(), e);
        }

        if(listModel.getMasterForm() != null && !listModel.getMasterForm().isNew()) {
            FormModelEntity formModelEntity = new FormModelEntity();
            BeanUtils.copyProperties(listModel.getMasterForm(), formModelEntity, new String[] {"dataModels","items", "permissions","submitChecks","functions", "triggeres", "protalListTemplate", "appListTemplate"});
            listModelEntity.setMasterForm(formModelEntity);
        }

        if(listModel.getSlaverForms() != null) {
            List<FormModelEntity> formModelEntities = new ArrayList<>();
            for (FormModel formModel : listModel.getSlaverForms()){
                FormModelEntity formModelEntity = new FormModelEntity();
                BeanUtils.copyProperties(formModel, formModelEntity, new String[]{"dataModels", "items", "permissions", "submitChecks","functions", "triggeres"});
                formModelEntities.add(formModelEntity);
            }
            listModelEntity.setSlaverForms(formModelEntities);
        }

        List<String> displayItemsSortIds = new ArrayList<>();
        if(listModel.getDisplayItems() != null) {
            List<ItemModelEntity> itemModelEntities = new ArrayList<>();
            for (ItemModel itemModel : listModel.getDisplayItems()) {
                if (itemModel==null || StringUtils.isEmpty(itemModel.getId())) {
                    throw new IFormException("列表字段勾选的item的ID不能为空");
                }
                ItemModelEntity itemModelEntity = new ItemModelEntity();
                itemModelService.copyItemModelToItemModelEntity(itemModel, itemModelEntity);
                if (itemModel.getTriggerIds()!=null && itemModel.getTriggerIds().size()>0) {
                    itemModelEntity.setTriggerIds(String.join(",", itemModel.getTriggerIds()));
                }
                itemModelEntities.add(itemModelEntity);
                displayItemsSortIds.add(itemModel.getId());
            }
            listModelEntity.setDisplayItems(itemModelEntities);
        }
        listModelEntity.setDisplayItemsSort(String.join(",", displayItemsSortIds));

        if (listModel.getSortItems() != null) {
            List<ListSortItem> sortItems = new ArrayList<ListSortItem>();
            for (ListModel.SortItem sortItem : listModel.getSortItems()) {
                if(sortItem.getItemModel() == null || StringUtils.isEmpty(sortItem.getItemModel().getId())) {
                    throw new IFormException("默认排序勾选的item的ID不能为空");
                }
                ListSortItem sortItemEntity = new ListSortItem();
                sortItemEntity.setListModel(listModelEntity);
                ItemModelEntity itemModelEntity = new ItemModelEntity();
                itemModelService.copyItemModelToItemModelEntity(sortItem.getItemModel(), itemModelEntity);
                if (sortItem.getItemModel().getTriggerIds()!=null && sortItem.getItemModel().getTriggerIds().size()>0) {
                    itemModelEntity.setTriggerIds(String.join(",", sortItem.getItemModel().getTriggerIds()));
                }
                sortItemEntity.setItemModel(itemModelEntity);
                sortItemEntity.setAsc(sortItem.isAsc());
                sortItems.add(sortItemEntity);
            }
            listModelEntity.setSortItems(sortItems);
        }

        List<String> searchItemsSortIds = new ArrayList<>();
        if (listModel.getSearchItems() != null) {
            List<ListSearchItem> searchItems = new ArrayList();
            for (int i = 0; i < listModel.getSearchItems().size(); i++) {
                SearchItem searchItem =  listModel.getSearchItems().get(i);
                if (searchItem==null || StringUtils.isEmpty(searchItem.getId())) {
                    throw new IFormException("查询条件勾选的item的ID不能为空");
                }
                if (searchItem.getSearch() == null) {
                    throw new IFormException("控件【" + searchItem.getName() + "】未定义搜索属性");
                }
                ItemModelEntity itemModelEntity = new ItemModelEntity();
                itemModelEntity.setId(searchItem.getId());
                ListSearchItem searchItemEntity =  new ListSearchItem();
                BeanUtils.copyProperties(searchItem, searchItemEntity, "listModel", "itemModel", "search", "id", "name", "parseArea");
                if (searchItem.getParseArea()!=null && searchItem.getParseArea().size()>0) {
                    searchItemEntity.setParseArea(String.join(",", searchItem.getParseArea()));
                }
                searchItemEntity.setListModel(listModelEntity);
                searchItemEntity.setItemModel(itemModelEntity);

                ItemSearchInfo searchInfo = new ItemSearchInfo();
                BeanUtils.copyProperties(searchItem.getSearch(), searchInfo, new String[]{"defaultValue", "defaultValueName"});
                Object defalueValue = searchItem.getSearch().getDefaultValue();
                if(defalueValue != null && defalueValue instanceof List){
					List list = (List)searchItem.getSearch().getDefaultValue();
					List newList = new ArrayList();
					for (Object item:list) {
						if (!StringUtils.isEmpty(item)) {
							newList.add(item+"");
						}
					}
					searchInfo.setDefaultValue(String.join(",", newList));
                } else if(defalueValue != null) {
                    searchInfo.setDefaultValue(StringUtils.isEmpty(defalueValue) ? null : String.valueOf(defalueValue));
                }
                searchItemEntity.setOrderNo(i);
                searchItemEntity.setSearch(searchInfo);
                searchItems.add(searchItemEntity);
                searchItemsSortIds.add(searchItem.getId());
            }
            listModelEntity.setSearchItems(searchItems);
        }
        listModelEntity.setSearchItemsSort(String.join(",", searchItemsSortIds));

        if (listModel.getFunctions() != null) {
            List<ListFunction> functions = new ArrayList<>();
            int i = 0;
            for (FunctionModel function : listModel.getFunctions()) {
                if (function==null || StringUtils.isEmpty(function.getLabel()) || StringUtils.isEmpty(function.getAction())) {
                    throw new IFormException("功能按钮存在功能名或者功能编码为空");
                }
                ListFunction listFunction = new ListFunction() ;
                BeanUtils.copyProperties(function, listFunction, new String[]{"listModel", "parseArea", "exportFunction"});

                if (function.getParseArea()!=null && function.getParseArea().size()>0) {
                    listFunction.setParseArea(String.join(",", function.getParseArea()));
                }
                listFunction.setListModel(listModelEntity);
                listFunction.setOrderNo(++i);
                functions.add(listFunction);
            }
			assemblyFunction(listModelEntity, functions, listModel.getFunctions());
			listModelEntity.setFunctions(functions);
		}

        if (listModel.getQuickSearchItems() !=null) {
            // 检测快速筛选的个数
            Long count = listModel.getQuickSearchItems().stream().filter(item->item.getDefaultActive()!=null && item.getDefaultActive()==true).count();
            if (count>1) {
                throw new IFormException("快速筛选的默认勾选个数不能超过1个");
            }
            List<QuickSearchEntity> quickSearches = new ArrayList<>();
            int i = 0;
            for (QuickSearchItem searchItem : listModel.getQuickSearchItems()) {
                if (searchItem==null || StringUtils.isEmpty(searchItem.getName())) {
                    throw new IFormException("快速筛选有导航名为空");
                }
                ItemModel itemModel = searchItem.getItemModel();
                QuickSearchEntity quickSearchEntity = new QuickSearchEntity();
                if (itemModel!=null && !StringUtils.isEmpty(itemModel.getId())) {
                    ItemModelEntity itemModelEntity = new ItemModelEntity();
                    itemModelEntity.setId(itemModel.getId());
                    quickSearchEntity.setItemModel(itemModelEntity);
                }
                BeanUtils.copyProperties(searchItem, quickSearchEntity, new String[]{"itemModel", "searchValues"});
                quickSearchEntity.setSearchValues(String.join(",", searchItem.getSearchValues()));
                quickSearchEntity.setOrderNo(++i);
                quickSearchEntity.setListModel(listModelEntity);
                quickSearchEntity.setDefaultActive(searchItem.getDefaultActive());
                quickSearches.add(quickSearchEntity);
            }
            listModelEntity.setQuickSearchItems(quickSearches);
        }
        return listModelEntity;
    }

	private void assemblyFunction(ListModelEntity listModelEntity, List<ListFunction> listFunctions, List<FunctionModel> functionModels) {
		FormModelEntity formModelEntity = formModelService.find(listModelEntity.getMasterForm().getId());
		Map<String, ItemModelEntity> itemModelEntities = exportDataService.eachHasColumnItemModel(formModelEntity.getItems()).stream().collect(Collectors.toMap(ItemModelEntity::getId, i ->i));
		Map<String, ListFunction> listFunctionMap = toMap(listFunctions, ListFunction::getAction);
		Map<String, FunctionModel> functionModelMap = toMap(functionModels, FunctionModel::getAction);

		BiConsumer<String, ThreeConsumer<Map<String, ItemModelEntity>, ListFunction, FunctionModel>> assemblyConsumer =
				( action, assemblyFunction ) -> {
					if (listFunctionMap.containsKey(action) && functionModelMap.containsKey(action)) {
						assemblyFunction.accept(itemModelEntities, listFunctionMap.get(action), functionModelMap.get(action));
					}else if (!listFunctionMap.containsKey(action)) {
						// 当目前功能按钮中不包含对应action的功能
						ListFunction listFunction = ExportListFunctionUtils.generateListFunction(ExportListFunctionUtils.FunctionsType.valueOfName(action));
						if (functionModelMap.containsKey(action)) {
							// 如果前端有传
							assemblyFunction.accept(itemModelEntities, listFunction, functionModelMap.get(action));
						}
						listFunctions.add(listFunction);
					}
				};

		// 如果不包含这两个功能, 则需要初始化功能数据
		if (!listFunctionMap.containsKey(DefaultFunctionType.TemplateDownload.getValue()) && !listFunctionMap.containsKey(DefaultFunctionType.Import.getValue())) {
			assemblyItemModelEntityImportFunction(itemModelEntities.values());
		}

		assemblyConsumer.accept(DefaultFunctionType.Export.getValue(), this::assemblyExportFunction);
		assemblyConsumer.accept(DefaultFunctionType.TemplateDownload.getValue(), this::assemblyTemplateDownloadFunction);
		assemblyConsumer.accept(DefaultFunctionType.Import.getValue(), this::assemblyImportFunction);

		listModelEntity.setMasterForm(formModelEntity);

	}

	private void assemblyItemModelEntityImportFunction(Collection<ItemModelEntity> entities) {
		entities.forEach(item -> {
			item.setTemplateSelected(false);
			item.setTemplateName(item.getName());
			item.setDataImported(false);
			item.setMatchKey(false);
		});
		entities.stream().filter(item -> "id".equals(item.getName()))
				.findAny()
				.ifPresent(item -> item.setMatchKey(true));
	}

	private void assemblyExportFunction(Map<String, ItemModelEntity> itemModelEntities, ListFunction listFunction, FunctionModel model) {
		ExportFunctionModel exportModel = Optional.ofNullable(model.getExportFunction()).orElseThrow(() -> new ICityException("导出功能按钮没有传入相关设置数据"));
		ExportListFunction exportFunction = new ExportListFunction();
		exportFunction.setControl(exportModel.getControl());
		exportFunction.setFormat(exportModel.getFormat());
		exportFunction.setType(exportModel.getType());
		exportFunction.setCustomExport(String.join(",", exportModel.getCustomExport()));
		listFunction.setExportFunction(exportFunction);
	}

	private void assemblyTemplateDownloadFunction(Map<String, ItemModelEntity> itemModelEntities, ListFunction listFunction, FunctionModel model) {
		model.getTemplateItemModels()
				.forEach(tModel -> {
					ItemModelEntity itemEntity = itemModelEntities.get(tModel.getId());
					itemEntity.setTemplateName(tModel.getTemplateName());
					itemEntity.setTemplateSelected(tModel.isSelected());
				});
	}

	private void assemblyImportFunction(Map<String, ItemModelEntity> itemModelEntities, ListFunction listFunction, FunctionModel model) {
		ImportFunctionModel importModel = Optional.ofNullable(model.getImportFunction()).orElseThrow(() -> new ICityException("导入功能按钮为传入相关设置"));
		ImportBaseFunctionEntity importEntity = Optional.ofNullable(listFunction.getImportFunction()).orElseGet(ImportBaseFunctionEntity::new);
		BeanCopiers.noConvertCopy(importModel, importEntity);
		listFunction.setImportFunction(importEntity);
		model.getImportFunction().getTemplateItemModels()
				.forEach(iModel -> {
					ItemModelEntity entity = itemModelEntities.get(iModel.getId());
					entity.setMatchKey(iModel.isKey());
					entity.setDataImported(iModel.isImported());
				});
	}

	private <K, V> Map<K, V> toMap(List<V> list, Function<V, K> keyMapper) {
		return list.stream().collect(Collectors.toMap(keyMapper, v -> v));
	}

    @Override
    public ListModel toListModel(ListModelEntity listModelEntity) {
        return null;
    }

    private void verfyListName(ListModel listModel) {
        if(StringUtils.isEmpty(listModel.getName()) || StringUtils.isEmpty(listModel.getApplicationId())){
            throw new IFormException("名称或关联应用为空");
        }
        List<ListModelEntity> list = query().filterEqual("name", listModel.getName())
                .filterEqual("applicationId", listModel.getApplicationId())
                .filterNotNull("masterForm")
                .list();
        if(list == null || list.size() < 1){
            return;
        }
        if(list.size() > 0 && !StringUtils.hasText(listModel.getId())){
            throw new IFormException("名称重复了");
        }

        List<String> idList = list.parallelStream().map(item->item.getId()).collect(Collectors.toList());
        if(StringUtils.hasText(listModel.getId()) && !idList.contains(listModel.getId())){
            throw new IFormException("列表名重复了");
        }
    }

}
