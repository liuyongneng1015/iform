package tech.ascs.icity.iform.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import tech.ascs.icity.admin.api.model.Application;
import tech.ascs.icity.admin.client.ApplicationService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.model.ListModel.SortItem;
import tech.ascs.icity.iform.api.model.SearchItem.Search;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.service.ListModelService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "列表模型服务", description = "包含列表模型的增删改查等功能")
@RestController
public class ListModelController implements tech.ascs.icity.iform.api.service.ListModelService {

	@Autowired
	private ListModelService listModelService;

	@Autowired
	private FormModelService formModelService ;

	@Autowired
	private ItemModelService itemModelService ;

	@Autowired
	private ApplicationService applicationService ;


	@Override
	public List<ListModel> list(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<ListModelEntity, ListModelEntity> query = listModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId", applicationId);
			}
			List<ListModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public Page<ListModel> page(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name="page", defaultValue="1") int page,
								@RequestParam(name="pagesize", defaultValue="10") int pagesize, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<ListModelEntity, ListModelEntity> query = listModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId", applicationId);
			}
			Page<ListModelEntity> entities = query.page(page, pagesize).page();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public ListModel get(@PathVariable(name="id") String id) {
		ListModelEntity entity = listModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "列表模型【" + id + "】不存在");
		}
		try {
			return toDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	// 新增列表的时候，自动创建新增、导出、导入、删除、二维码，为系统自带功能
	private List<String> functionDefaultActions = Arrays.asList(new String[]{"add", "export", "import", "batchDelete", "QR-Code"});
	private List<String> functionDefaultLabels  = Arrays.asList(new String[]{"新增", "导出", "导入", "删除", "二维码"});
	private List<String> functionDefaultMethods = Arrays.asList(new String[]{"POST", "GET", "POST", "DELETE", "GET"});
	@Override
	public IdEntity createListModel(@RequestBody ListModel ListModel) {
		if (StringUtils.hasText(ListModel.getId())) {
			throw new IFormException("列表模型ID不为空，请使用更新操作");
		}
		if (ListModel.getMasterForm()==null || StringUtils.isEmpty(ListModel.getMasterForm().getId())) {
			throw new IFormException("关联表单的ID不能为空");
		}
		try {
			ListModelEntity entity = wrap(ListModel);
			// 创建默认的功能按钮
			List<ListFunction> functions = new ArrayList<>();
			for (int i = 0; i < functionDefaultActions.size(); i++) {
				ListFunction function = new ListFunction();
				function.setAction(functionDefaultActions.get(i));
				function.setLabel(functionDefaultLabels.get(i));
				function.setMethod(functionDefaultMethods.get(i));
                function.setVisible(true);
				function.setOrderNo(i+1);
				function.setListModel(entity);
				functions.add(function);
			}
			entity.setFunctions(functions);
			entity = listModelService.save(entity);
			return new IdEntity(entity.getId());
		} catch (Exception e) {
			throw new IFormException("保存列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void updateListModel(@PathVariable(name = "id") String id, @RequestBody ListModel ListModel) {
		if (!StringUtils.hasText(ListModel.getId()) || !id.equals(ListModel.getId())) {
			throw new IFormException("列表模型ID不一致");
		}
		if (ListModel.getMasterForm()==null || StringUtils.isEmpty(ListModel.getMasterForm().getId())) {
			throw new IFormException("关联表单的ID不能为空");
		}
		// 校验默认的功能按钮是否被删除
		// checkDefaultFuncExists(ListModel);
		try {
			ListModelEntity entity = wrap(ListModel);
			listModelService.save(entity);
		} catch (Exception e) {
			throw new IFormException("保存列表模型列表失败：" + e.getMessage(), e);
		}
	}

	// 校验默认的功能按钮是否被删除
	private void checkDefaultFuncExists(ListModel listModel) {
		List<FunctionModel> functions = listModel.getFunctions();
		if (functions==null || functions.size()==0) {
			throw new IFormException("系统自带的功能按钮不允许删除");
		}
		// 校验功能按钮的编码不允许为空和同名
		if (functions.stream().filter(item->item.getAction()!=null).map(item->item.getAction()).collect(Collectors.toSet()).size()<functions.size()) {
			throw new IFormException("功能按钮的编码不能为空和同名");
		}
		// 校验功能按钮的功能名不允许为空和同名
		if (functions.stream().filter(item->item.getLabel()!=null).map(item->item.getLabel()).collect(Collectors.toSet()).size()<functions.size()) {
			throw new IFormException("校验功能按钮的功能名能为空和同名");
		}
		// 校验默认的功能按钮是否被删除
		for (int i = 0; i < functionDefaultActions.size(); i++) {
			String action = functionDefaultActions.get(i);
			String label = functionDefaultLabels.get(i);
			Optional<FunctionModel> optional = functions.stream().filter(item->!StringUtils.isEmpty(item.getId()) &&
																				action.equals(item.getAction()) &&
																				label.equals(item.getLabel())).findFirst();
			if (optional.isPresent()==false) {
				throw new IFormException("系统自带的功能按钮不允许删除和编辑");
			}
		}
	}

	@Override
	public void removeListModel(@PathVariable(name="id") String id) {
		listModelService.checkListModelCanDelete(id);
		listModelService.deleteById(id);
	}

	@Override
	public List<ListModel> findListModelsByTableName(String tableName) {
		return listModelService.findListModelsByTableName(tableName);
	}

	@Override
	public List<ApplicationModel> findListApplicationModel(@RequestParam(name="applicationId", required = true) String applicationId) {
		return list(applicationId, listModelService.findListModelSimpleInfo());
	}

	@Override
	public ListFormBtnPermission getListFormBtnPermissions(@PathVariable(name = "id") String id) {
		ListModelEntity entity = listModelService.find(id);
		ListFormBtnPermission listFormBtnPermission = new ListFormBtnPermission();
		if (entity!=null) {
			listFormBtnPermission.setListPermissions(listModelService.findListBtnPermission(entity));
			FormModelEntity formModel = entity.getMasterForm();
			if (formModel!=null) {
				listFormBtnPermission.setFormPermissions(listModelService.findFormBtnPermission(formModel));
			}
		}
		return listFormBtnPermission;
	}

	@Override
	public void batchDelete(@RequestBody List<String> ids) {
		if (ids!=null && ids.size()>0) {
			String[] idArr = ids.stream().filter(item->!StringUtils.isEmpty(item)).toArray(String[]::new);
			if (idArr!=null && idArr.length>0) {
				listModelService.deleteById(idArr);
			}
		}
	}

	@Override
	public List<ListModel> findListModelSimpleByIds(@RequestParam(name = "ids", required = false) String[] ids) {
		if (ids!=null && ids.length>0) {
			return listModelService.findListModelSimpleByIds(Arrays.asList(ids));
		}
		return new ArrayList<>();
	}

	private List<ApplicationModel> list(String applicationId, List<ListModel> entities){
		if(entities == null){
			return new ArrayList<>();
		}
		Map<String, List<ListModel>> map = new HashMap<>();
		for(ListModel entity : entities){
			if(!StringUtils.hasText(entity.getApplicationId())){
				continue;
			}
			List<ListModel> list = map.get(entity.getApplicationId());
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(entity);
			map.put(entity.getApplicationId(), list);
		}
		List<ApplicationModel> applicationFormModels = new ArrayList<>();
		if(map != null && map.size() > 0) {
			//TODO 查询应用
			Set<String> c = map.keySet();
			String[] applicationIds =  new String[c.size()];
			c.toArray(applicationIds);
			List<Application> applicationList = applicationService.queryAppsByIds(new ArrayList<>(c));
			if(applicationList != null) {
				for (Application application : applicationList) {
					if(application.getId().equals(applicationId)){
						applicationFormModels.add(createApplicationModel(application, map));
						break;
					}
				}
				for (Application application : applicationList) {
					if(application.getId().equals(applicationId)){
						continue;
					}
					applicationFormModels.add(createApplicationModel(application, map));
				}
			}
		}

		return applicationFormModels;
	}

	private ApplicationModel createApplicationModel(Application application, Map<String, List<ListModel>> map){
		ApplicationModel applicationFormModel = new ApplicationModel();
		applicationFormModel.setId(application.getId());
		applicationFormModel.setName(application.getApplicationName());
		applicationFormModel.setListModels(map.get(application.getId()));
		return applicationFormModel;
	}

	private ListModelEntity wrap(ListModel listModel)  {

		verfyListName(listModel);

		ListModelEntity entity =  new ListModelEntity() ;
		BeanUtils.copyProperties(listModel, entity, new String[] {"masterForm","slaverForms","sortItems", "searchItems","functions","displayItems", "quickSearchItems"});

		if(listModel.getMasterForm() != null && !listModel.getMasterForm().isNew()){
			FormModelEntity formModelEntity = new FormModelEntity();
			BeanUtils.copyProperties(listModel.getMasterForm(), formModelEntity, new String[] {"dataModels","process","items", "permissions","submitChecks","functions"});
			entity.setMasterForm(formModelEntity);
		}

		if(listModel.getSlaverForms() != null) {
			List<FormModelEntity> formModelEntities = new ArrayList<>();
			for (FormModel formModel : listModel.getSlaverForms()){
				FormModelEntity formModelEntity = new FormModelEntity();
				BeanUtils.copyProperties(formModel, formModelEntity, new String[]{"dataModels", "process", "items", "permissions", "submitChecks","functions"});
				formModelEntities.add(formModelEntity);
			}
			entity.setSlaverForms(formModelEntities);
		}

		if(listModel.getDisplayItems() != null) {
			List<ItemModelEntity> itemModelEntities = new ArrayList<>();
			for (ItemModel itemModel : listModel.getDisplayItems()){
				ItemModelEntity itemModelEntity = new ItemModelEntity();
				BeanUtils.copyProperties(itemModel, itemModelEntity, new String[]{"formModel", "columnModel", "activities", "options", "permissions","items","parentItem","referenceList"});
				itemModelEntities.add(itemModelEntity);
			}
			entity.setDisplayItems(itemModelEntities);
		}

		if (listModel.getSortItems() != null) {
			List<ListSortItem> sortItems = new ArrayList<ListSortItem>();
			for (SortItem sortItem : listModel.getSortItems()) {
				ListSortItem sortItemEntity = new ListSortItem();
				sortItemEntity.setListModel(entity);
				if(sortItem.getItemModel() != null){
					ItemModelEntity itemModelEntity = new ItemModelEntity();
					BeanUtils.copyProperties(sortItem.getItemModel(), itemModelEntity, new String[]{"formModel", "columnModel", "activities", "options", "permissions","items","parentItem","referenceList"});
					sortItemEntity.setItemModel(itemModelEntity);
				}
				sortItemEntity.setAsc(sortItem.isAsc());
				sortItems.add(sortItemEntity);
			}
			entity.setSortItems(sortItems);
		}

		if (listModel.getSearchItems() != null) {
			List<ListSearchItem> searchItems = new ArrayList<ListSearchItem>();
			for (SearchItem searchItem : listModel.getSearchItems()) {
				ListSearchItem searchItemEntity =  new ListSearchItem();
				if(searchItem.getId() != null) {
					ItemModelEntity itemModelEntity = new ItemModelEntity();
					itemModelEntity.setId(searchItem.getId());
					itemModelEntity.setName(searchItem.getName());
					searchItemEntity.setItemModel(itemModelEntity);
				}
				searchItemEntity.setListModel(entity);
				if (searchItem.getSearch() == null) {
					throw new IFormException("控件【" + searchItemEntity.getItemModel().getName() + "】未定义搜索属性");
				}
				ItemSearchInfo searchInfo = new ItemSearchInfo();
				BeanUtils.copyProperties(searchItem.getSearch(), searchInfo, new String[]{"defaultValue"});
				Object defalueValue = searchItem.getSearch().getDefaultValue();
				if(defalueValue != null && defalueValue instanceof List){
					searchInfo.setDefaultValue(String.join(",", (List)searchItem.getSearch().getDefaultValue()));
				} else if(defalueValue != null && defalueValue instanceof String){
					searchInfo.setDefaultValue(StringUtils.isEmpty(defalueValue) ? null : (String)defalueValue);
				}
				searchItemEntity.setSearch(searchInfo);
				searchItems.add(searchItemEntity);
			}
			entity.setSearchItems(searchItems);
		}
		if (listModel.getFunctions() != null) {
			List<ListFunction> functions = new ArrayList<>();
			int i = 0;
			for (FunctionModel function : listModel.getFunctions()) {
				ListFunction listFunction = new ListFunction() ;
				BeanUtils.copyProperties(function, listFunction, new String[]{"listModel"});
				listFunction.setListModel(entity);
				listFunction.setOrderNo(++i);
				functions.add(listFunction);
			}
			entity.setFunctions(functions);
		}

		if (listModel.getQuickSearchItems() !=null) {
		    List<QuickSearchEntity> quickSearches = new ArrayList<>();
            int i = 0;
		    for (QuickSearchItem searchItem : listModel.getQuickSearchItems()) {
                QuickSearchEntity quickSearchEntity = new QuickSearchEntity();
                if(searchItem.getId() != null) {
                    ItemModelEntity itemModelEntity = new ItemModelEntity();
                    itemModelEntity.setId(searchItem.getId());
                    itemModelEntity.setName(searchItem.getName());
                    quickSearchEntity.setItemModel(itemModelEntity);
                }
                BeanUtils.copyProperties(searchItem, quickSearchEntity, new String[]{"itemModel", "searchValues"});
                if (searchItem.getSearchValues()!=null && searchItem.getSearchValues().size()>0) {
                    quickSearchEntity.setSearchValues(String.join(",", searchItem.getSearchValues()));
                }
                quickSearchEntity.setOrderNo(++i);
                quickSearchEntity.setListModel(entity);
                quickSearches.add(quickSearchEntity);
            }
            entity.setQuickSearchItems(quickSearches);
		}
		return entity;
	}

	private void verfyListName(ListModel listModel) {
		if(StringUtils.isEmpty(listModel.getName()) || StringUtils.isEmpty(listModel.getApplicationId())){
			throw new IFormException("名称或关联应用为空");
		}
		List<ListModelEntity> list = listModelService.query().filterEqual("name", listModel.getName()).filterEqual("applicationId", listModel.getApplicationId()).list();
		if(list == null || list.size() < 1){
			return;
		}
		if(list.size() > 0 && !StringUtils.hasText(listModel.getId())){
			throw new IFormException("名称重复了");
		}

		List<String> idList = list.parallelStream().map(ListModelEntity::getId).collect(Collectors.toList());
		if(StringUtils.hasText(listModel.getId()) && !idList.contains(listModel.getId())){
			throw new IFormException("列表名重复了");
		}
	}

	private Page<ListModel> toDTO(Page<ListModelEntity> entities) throws InstantiationException, IllegalAccessException {
		Page<ListModel> listModels = Page.get(entities.getPage(), entities.getPagesize());
		listModels.data(entities.getTotalCount(), toDTO(entities.getResults()));
		return listModels;
	}

	private List<ListModel> toDTO(List<ListModelEntity> entities) throws InstantiationException, IllegalAccessException {
		List<ListModel> listModels = new ArrayList<ListModel>();
		for (ListModelEntity entity : entities) {
			listModels.add(toDTO(entity));
		}
		return listModels;
	}

	private ListModel toDTO(ListModelEntity entity) {
		ListModel listModel = new ListModel();
		BeanUtils.copyProperties(entity, listModel, new String[] {"masterForm", "slaverForms", "sortItems", "searchItems", "functions", "displayItems", "quickSearchItems"});

		if(entity.getMasterForm() != null){
			FormModel masterForm = new FormModel();
			BeanUtils.copyProperties(entity.getMasterForm(), masterForm, new String[] {"items","dataModels","permissions","submitChecks","functions"});
			listModel.setMasterForm(masterForm);
		}

		if(entity.getDisplayItems() != null){
			List<ItemModel> list  = new ArrayList<>();
			for(ItemModelEntity itemModelEntity : entity.getDisplayItems()){
				ItemModel itemModel = new ItemModel();
				BeanUtils.copyProperties(itemModelEntity, itemModel, new String[] {"formModel", "columnModel","activities","options","searchItems", "sortItems","permissions", "referenceList","items","parentItem"});
				list.add(itemModel);
			}
			listModel.setDisplayItems(list);
		}

		if(entity.getSlaverForms() != null){
			List<FormModel> list = new ArrayList<>();
			for(FormModelEntity formModelEntity : entity.getSlaverForms()) {
				FormModel slaverForm = new FormModel();
				BeanUtils.copyProperties(formModelEntity, slaverForm, new String[]{"items", "dataModels", "permissions", "submitChecks","functions"});
				list.add(slaverForm);
			}
			listModel.setSlaverForms(list);
		}

		if(entity.getFunctions() != null){
			List<FunctionModel> functions = new ArrayList<FunctionModel>();
			for(ListFunction listFunction : entity.getFunctions()) {
				FunctionModel function = new FunctionModel();
				BeanUtils.copyProperties(listFunction, function, new String[]{"listModel"});
				functions.add(function);
			}
            Collections.sort(functions);
			listModel.setFunctions(functions);
		}

		if (entity.getSortItems().size() > 0) {
			List<SortItem> sortItems = new ArrayList<SortItem>();
			for (ListSortItem sortItemEntity: entity.getSortItems()) {
				SortItem sortItem = new SortItem();
				BeanUtils.copyProperties(sortItemEntity, sortItem, new String[]{"listModel","itemModel"});
				if(sortItemEntity.getItemModel() != null) {
					ItemModel itemModel = new ItemModel();
					BeanUtils.copyProperties(sortItemEntity.getItemModel(), itemModel, new String[] {"formModel", "columnModel", "searchItems", "sortItems", "permissions", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
					sortItem.setItemModel(itemModel);
				}
				sortItems.add(sortItem);
			}
			listModel.setSortItems(sortItems);
		}

		if (entity.getSearchItems().size() > 0) {
			List<SearchItem> searchItems = new ArrayList<SearchItem>();
			for (ListSearchItem searchItemEntity : entity.getSearchItems()) {
				SearchItem searchItem = new SearchItem();
				if(searchItemEntity.getItemModel() != null){
					BeanUtils.copyProperties(searchItemEntity.getItemModel(), searchItem, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
				}
				if(searchItemEntity.getSearch() != null){
					Search search = new Search();
					BeanUtils.copyProperties(searchItemEntity.getSearch(), search, new String[] {"search","defaultValue"});
					String defaultValue = searchItemEntity.getSearch().getDefaultValue();
					if(StringUtils.hasText(defaultValue)) {
						ItemType itemType = searchItem.getType();
						// Input, InputNumber返回的defaultValue是字符串格式，不是数组格式
						if (ItemType.Input.equals(itemType) || ItemType.InputNumber.equals(itemType)) {
							search.setDefaultValue(defaultValue);
						} else {
							search.setDefaultValue(Arrays.asList(defaultValue.split(",")));
						}
					}
					searchItem.setSearch(search);
				}
				searchItems.add(searchItem);
			}
			listModel.setSearchItems(searchItems);
		}

		if (entity.getQuickSearchItems().size() > 0) {
		    List<QuickSearchItem> quickSearches = new ArrayList<>();
		    for (QuickSearchEntity quickSearchEntity : entity.getQuickSearchItems()) {
		        QuickSearchItem quickSearch = new QuickSearchItem();
                BeanUtils.copyProperties(quickSearchEntity, quickSearch, new String[]{"listModel", "itemModel", "searchValues"});
                if (!StringUtils.isEmpty(quickSearchEntity.getSearchValues())) {
                    quickSearch.setSearchValues(Arrays.asList(quickSearchEntity.getSearchValues().split(",")));
                }
		        if (quickSearchEntity.getItemModel() != null) {
                    ItemModel itemModel = new ItemModel();
                    BeanUtils.copyProperties(quickSearchEntity.getItemModel(), itemModel, new String[] {"permissions", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
                    quickSearch.setItemModel(itemModel);
                }
                quickSearches.add(quickSearch);
            }
            Collections.sort(quickSearches);
            listModel.setQuickSearchItems(quickSearches);
        }
		return listModel;
	}


}
