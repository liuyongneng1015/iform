package tech.ascs.icity.iform.controller;

import java.lang.reflect.Field;
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
import tech.ascs.icity.admin.api.model.TreeSelectData;
import tech.ascs.icity.admin.client.ApplicationService;
import tech.ascs.icity.admin.client.GroupService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.model.ListModel.SortItem;
import tech.ascs.icity.iform.api.model.SearchItem.Search;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormInstanceServiceEx;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.service.ListModelService;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.NameEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "列表模型服务", description = "包含列表模型的增删改查等功能")
@RestController
public class ListModelController implements tech.ascs.icity.iform.api.service.ListModelService {

	@Autowired
	private ListModelService listModelService;

	@Autowired
	private FormModelService formModelService;

	@Autowired
	private ItemModelService itemModelService;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private FormInstanceServiceEx formInstanceServiceEx;

	@Autowired
	GroupService groupService;

	@Override
	public List<ListModel> list(@RequestParam(name = "name", defaultValue = "") String name,
								@RequestParam(name = "applicationId", required = false) String applicationId) {
		return listModelService.findListModelSimpleInfo(name, applicationId, null, null);
	}

	@Override
	public Page<ListModel> page(@RequestParam(name = "name", defaultValue="") String name,
								@RequestParam(name = "page", defaultValue="1") int page,
								@RequestParam(name = "pagesize", defaultValue="10") int pagesize,
								@RequestParam(name = "applicationId", required = false) String applicationId) {
		return listModelService.findListModelSimplePageInfo(name, applicationId, page, pagesize);
	}

	@Override
	public ListModel get(@PathVariable(name="id") String id) {
		ListModelEntity entity = listModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "列表模型【" + id + "】不存在");
		}
		if (entity.getMasterForm()==null) {
			return null;
		}
		try {
			return toDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	// 新增列表的时候，自动创建新增、导入、批量删除，为系统自带功能
	private DefaultFunctionType[] functionDefaultActions = {DefaultFunctionType.Add, DefaultFunctionType.BatchDelete, DefaultFunctionType.Export};
	private String[] functionDefaultLabels = new String[]{"新增", "批量删除", "导入"};
	private String[] functionDefaultIcons = new String[]{null, "icon-xuanzhong", null};
	private String[] functionDefaultMethods = new String[]{"POST", "DELETE", "GET"};
	private Boolean[] functionVisibles = {true, false, false};

	@Override
	public IdEntity createListModel(@RequestBody ListModel ListModel) {
		if (StringUtils.hasText(ListModel.getId())) {
			throw new IFormException("列表模型ID不为空，请使用更新操作");
		}
		if (ListModel.getMasterForm()==null || StringUtils.isEmpty(ListModel.getMasterForm().getId())) {
			throw new IFormException("关联表单的ID不能为空");
		}
		try {
			ListModel.setDataPermissions(DataPermissionsType.AllPeople);
			ListModelEntity entity = wrap(ListModel);
			// 创建默认的功能按钮
			List<ListFunction> functions = new ArrayList<>();
			for (int i = 0; i < functionDefaultActions.length; i++) {
				ListFunction function = new ListFunction();
				function.setAction(functionDefaultActions[i].getValue());
				function.setLabel(functionDefaultActions[i].getDesc());
				function.setMethod(functionDefaultMethods[i]);
                function.setVisible(functionVisibles[i]);
                function.setIcon(functionDefaultIcons[i]);
                function.setSystemBtn(true);
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
		checkDefaultFuncExists(ListModel);
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
			throw new IFormException("系统自带的功能按钮允许不启用，但不允许删除");
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
		for (int i = 0; i < functionDefaultActions.length; i++) {
			String action = functionDefaultActions[i].getValue();
			String label = functionDefaultActions[i].getDesc();
			Optional<FunctionModel> optional = functions.stream().filter(item->!StringUtils.isEmpty(item.getId()) &&
																				action.equals(item.getAction()) &&
																				label.equals(item.getLabel())).findFirst();
			if (optional.isPresent()==false) {
				throw new IFormException("系统自带的功能按钮 "+functionDefaultLabels[i]+" 不允许删除，改名，或者修改功能编码");
			}
		}
	}

	@Override
	public void removeListModel(@PathVariable(name="id") String id) {
		ListModelEntity listModelEntity = listModelService.find(id);
		if (listModelEntity!=null) {
			listModelService.setItemReferenceListModelNull(id);
			listModelService.deleteById(id);
			listModelService.deleteListBtnPermission(id);
		}
	}

	@Override
	public List<ListModel> findListModelsByTableName(String tableName) {
		return listModelService.findListModelsByTableName(tableName);
	}

	@Override
	public List<ApplicationModel> findListApplicationModel(@RequestParam(name = "formId", required = false) String formId,
														   @RequestParam(name = "functionType", required = false) FunctionType functionType,
														   @RequestParam(name="applicationId", required = true) String applicationId) {
		return list(applicationId, listModelService.findListModelSimpleInfo(null, null, formId, FunctionType.activitiList==functionType));
	}

	@Override
	public AppListForm findAppReferenceListForm(@RequestParam(name="applicationId", required = true) String applicationId) {
		AppListForm appListForm = new AppListForm();
		if (!StringUtils.isEmpty(applicationId)) {
			List<FormModelEntity> formModelEntities = formModelService.query().filterEqual("applicationId", applicationId).list();
			List<NameEntity> forms = new ArrayList<>();
			for (FormModelEntity entity:formModelEntities) {
				forms.add(new NameEntity(entity.getId(), entity.getName()));
			}
			appListForm.setForms(forms);
			List<ListModelEntity> listModelEntities = listModelService.query().filterEqual("applicationId", applicationId).list();
			List<NameEntity> lists = new ArrayList<>();
			for (ListModelEntity entity:listModelEntities) {
				lists.add(new NameEntity(entity.getId(), entity.getName()));
			}
			appListForm.setLists(lists);
		}
		return appListForm;
	}

	@Override
	public ListFormBtnPermission getListFormBtnPermissions(@PathVariable(name = "id") String id) {
		ListModelEntity entity = listModelService.find(id);
		ListFormBtnPermission listFormBtnPermission = new ListFormBtnPermission();
		if (entity!=null) {
			listFormBtnPermission.setListId(id);
			listFormBtnPermission.setListPermissions(listModelService.findListBtnPermission(entity));
			FormModelEntity formModel = entity.getMasterForm();
			if (formModel!=null) {
                listFormBtnPermission.setFormId(formModel.getId());
				listFormBtnPermission.setFormPermissions(listModelService.findFormBtnPermission(formModel));
			}
		}
		return listFormBtnPermission;
	}

	@Override
	public void removeListModels(@RequestBody List<String> ids) {
		for (String id:ids) {
			removeListModel(id);
		}
	}

	@Override
	public List<ListModel> findListModelSimpleByIds(@RequestParam(name = "ids", required = false) String[] ids) {
		if (ids!=null && ids.length>0) {
			return listModelService.findListModelSimpleByIds(Arrays.asList(ids));
		}
		return new ArrayList<>();
	}

	@Override
	public ListModel getByTableName(@PathVariable(name="tableName") String tableName) {
		return listModelService.getByTableName(tableName);
	}

	private List<ApplicationModel> list(String applicationId, List<ListModel> entities){
		if(entities == null){
			return new ArrayList<>();
		}
		Map<String, List<ListModel>> map = new HashMap<>();
		List<ListModel> listModelEntityList = entities.parallelStream().sorted(Comparator.comparing(ListModel::getId).reversed()).collect(Collectors.toList());
		for(ListModel entity : listModelEntityList){
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
			List<Application> applicationList = applicationService.queryAppsByIds(new ArrayList<>(map.keySet()));
			if(applicationList != null) {
				for (Application application:applicationList) {
					if(application.getId().equals(applicationId)){
						applicationFormModels.add(createApplicationModel(application, map));
						break;
					}
				}
				for (Application application:applicationList) {
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
		BeanUtils.copyProperties(listModel, entity, new String[] {"dataModels", "masterForm","slaverForms","sortItems", "searchItems","functions","displayItems", "quickSearchItems", "relevanceItemModelList"});

		if(listModel.getMasterForm() != null && !listModel.getMasterForm().isNew()) {
			FormModelEntity formModelEntity = new FormModelEntity();
			BeanUtils.copyProperties(listModel.getMasterForm(), formModelEntity, new String[] {"dataModels","items", "permissions","submitChecks","functions"});
			entity.setMasterForm(formModelEntity);
		}

		if(listModel.getSlaverForms() != null) {
			List<FormModelEntity> formModelEntities = new ArrayList<>();
			for (FormModel formModel : listModel.getSlaverForms()){
				FormModelEntity formModelEntity = new FormModelEntity();
				BeanUtils.copyProperties(formModel, formModelEntity, new String[]{"dataModels", "items", "permissions", "submitChecks","functions"});
				formModelEntities.add(formModelEntity);
			}
			entity.setSlaverForms(formModelEntities);
		}

		if(listModel.getDisplayItems() != null) {
			List<ItemModelEntity> itemModelEntities = new ArrayList<>();
			for (ItemModel itemModel : listModel.getDisplayItems()) {
				if (itemModel==null || StringUtils.isEmpty(itemModel.getId())) {
					throw new IFormException("列表字段勾选的item的ID不能为空");
				}
				ItemModelEntity itemModelEntity = new ItemModelEntity();
				BeanUtils.copyProperties(itemModel, itemModelEntity, new String[]{"formModel", "columnModel", "activities", "options", "permissions","items","parentItem","referenceList"});
				itemModelEntities.add(itemModelEntity);
			}
			entity.setDisplayItems(itemModelEntities);
		}

		if (listModel.getSortItems() != null) {
			List<ListSortItem> sortItems = new ArrayList<ListSortItem>();
			for (SortItem sortItem : listModel.getSortItems()) {
				if(sortItem.getItemModel() == null || StringUtils.isEmpty(sortItem.getItemModel().getId())) {
					throw new IFormException("默认排序勾选的item的ID不能为空");
				}
				ListSortItem sortItemEntity = new ListSortItem();
				sortItemEntity.setListModel(entity);
				ItemModelEntity itemModelEntity = new ItemModelEntity();
				BeanUtils.copyProperties(sortItem.getItemModel(), itemModelEntity, new String[]{"formModel", "columnModel", "activities", "options", "permissions","items","parentItem","referenceList"});
				sortItemEntity.setItemModel(itemModelEntity);
				sortItemEntity.setAsc(sortItem.isAsc());
				sortItems.add(sortItemEntity);
			}
			entity.setSortItems(sortItems);
		}

		if (listModel.getSearchItems() != null) {
			List<ListSearchItem> searchItems = new ArrayList();
			for (int i = 0; i < listModel.getSearchItems().size(); i++) {
				SearchItem searchItem =  listModel.getSearchItems().get(i);
				if (searchItem==null || StringUtils.isEmpty(searchItem.getId())) {
					throw new IFormException("查询条件勾选的item的ID不能为空");
				}
				ItemModelEntity itemModelEntity = new ItemModelEntity();
				itemModelEntity.setId(searchItem.getId());
				itemModelEntity.setName(searchItem.getName());
				ListSearchItem searchItemEntity =  new ListSearchItem();
				searchItemEntity.setItemModel(itemModelEntity);
				searchItemEntity.setListModel(entity);
				if (searchItem.getSearch() == null) {
					throw new IFormException("控件【" + searchItemEntity.getItemModel().getName() + "】未定义搜索属性");
				}
				ItemSearchInfo searchInfo = new ItemSearchInfo();
				BeanUtils.copyProperties(searchItem.getSearch(), searchInfo, new String[]{"defaultValue", "defaultValueName"});
				Object defalueValue = searchItem.getSearch().getDefaultValue();
				if(defalueValue != null && defalueValue instanceof List){
					searchInfo.setDefaultValue(String.join(",", (List)searchItem.getSearch().getDefaultValue()));
				} else if(defalueValue != null) {
					searchInfo.setDefaultValue(StringUtils.isEmpty(defalueValue) ? null : String.valueOf(defalueValue));
				}
				searchItemEntity.setOrderNo(i);
				searchItemEntity.setSearch(searchInfo);
				searchItems.add(searchItemEntity);
			}
			entity.setSearchItems(searchItems);
		}
		if (listModel.getFunctions() != null) {
			List<ListFunction> functions = new ArrayList<>();
			int i = 0;
			for (FunctionModel function : listModel.getFunctions()) {
				if (function==null || StringUtils.isEmpty(function.getLabel()) || StringUtils.isEmpty(function.getAction())) {
					throw new IFormException("功能按钮存在功能名或者功能编码为空");
				}
				ListFunction listFunction = new ListFunction() ;
				BeanUtils.copyProperties(function, listFunction, new String[]{"listModel"});
				listFunction.setListModel(entity);
				listFunction.setOrderNo(++i);
				functions.add(listFunction);
			}
			entity.setFunctions(functions);
		}

		if (listModel.getQuickSearchItems() !=null) {
			// 检测快速筛选的个数
			Long count = listModel.getQuickSearchItems().stream().filter(item->item.getDefaultActive()!=null && item.getDefaultActive()==true).count();
			if (count>1) {
				throw new IFormException("快速筛选的默认勾选个数不能超过1个");
			}
		    List<QuickSearchEntity> quickSearches = new ArrayList<>();
            int i = 0;
            int allQuickSearchCount = 0;
		    for (QuickSearchItem searchItem : listModel.getQuickSearchItems()) {
		    	if (searchItem==null || StringUtils.isEmpty(searchItem.getName())) {
					throw new IFormException("快速筛选有导航名为空");
				}
				ItemModel itemModel = searchItem.getItemModel();
		    	if (itemModel==null || StringUtils.isEmpty(itemModel.getId())) {
					allQuickSearchCount++;
				}
				if ((itemModel!=null && !StringUtils.isEmpty(itemModel.getId()) && (searchItem.getSearchValues()==null || searchItem.getSearchValues().size()==0))) {
					throw new IFormException("快速筛选勾选了筛选控件后必须勾选筛选值");
				}
                QuickSearchEntity quickSearchEntity = new QuickSearchEntity();
		    	if (itemModel!=null && !StringUtils.isEmpty(itemModel.getId())) {
					ItemModelEntity itemModelEntity = new ItemModelEntity();
					itemModelEntity.setId(itemModel.getId());
					quickSearchEntity.setItemModel(itemModelEntity);
				}
                BeanUtils.copyProperties(searchItem, quickSearchEntity, new String[]{"itemModel", "searchValues"});
				quickSearchEntity.setSearchValues(String.join(",", searchItem.getSearchValues()));
                quickSearchEntity.setOrderNo(++i);
                quickSearchEntity.setListModel(entity);
				quickSearchEntity.setDefaultActive(searchItem.getDefaultActive());
                quickSearches.add(quickSearchEntity);
            }
            if (allQuickSearchCount>1) {
		    	throw new IFormException("快速搜索只允许有一个不绑定筛选控件和筛选值，该刷选用于无条件的刷选");
			}
            entity.setQuickSearchItems(quickSearches);
		}
		return entity;
	}

	private void verfyListName(ListModel listModel) {
		if(StringUtils.isEmpty(listModel.getName()) || StringUtils.isEmpty(listModel.getApplicationId())){
			throw new IFormException("名称或关联应用为空");
		}
		List<ListModelEntity> list = listModelService.query().filterEqual("name", listModel.getName())
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
			FormModelEntity formModelEntity = entity.getMasterForm();
			FormModel masterForm = new FormModel();
			BeanUtils.copyProperties(formModelEntity, masterForm, new String[] {"items","dataModels","permissions","submitChecks","functions"});
			listModel.setMasterForm(masterForm);

			if(formModelEntity.getDataModels() != null && formModelEntity.getDataModels().size() > 0){
				List<DataModel> dataModelList = new ArrayList<>();
				List<DataModelEntity> dataModelEntities = new ArrayList<>();
				for (DataModelEntity dataModelEntity : formModelEntity.getDataModels()) {
					dataModelEntities.add(dataModelEntity);
					if(dataModelEntity.getSlaverModels() != null && dataModelEntity.getSlaverModels().size() > 0) {
						dataModelEntities.addAll(dataModelEntity.getSlaverModels());
					}
				}
				for (DataModelEntity dataModelEntity : dataModelEntities) {
					dataModelList.add(formModelService.getDataModel(dataModelEntity));
				}
				listModel.setDataModels(dataModelList);
			}
		}

//		Set<String> masterFormItemIds = new HashSet<>();
//		if (entity.getMasterForm()!=null && entity.getMasterForm().getItems()!=null && entity.getMasterForm().getItems().size()>0) {
//			masterFormItemIds = entity.getMasterForm().getItems().stream().map(item->item.getId()).collect(Collectors.toSet());
//		}
		Set<String> masterFormItemIds = getMasterFormItems(entity.getMasterForm()).stream().map(item->item.getId()).collect(Collectors.toSet());

		if(entity.getDisplayItems() != null){
			List<ItemModel> list = new ArrayList<>();
			for(ItemModelEntity itemModelEntity : entity.getDisplayItems()) {
				if (masterFormItemIds.contains(itemModelEntity.getId())) {
					ItemModel itemModel = new ItemModel();
					BeanUtils.copyProperties(itemModelEntity, itemModel, new String[]{"defaultValue", "formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "referenceList", "items", "parentItem"});
					list.add(itemModel);
				}
			}
			// displayItem是有排序的，排序的ID全部拼接到displayItemsSort这个字段
			if (!StringUtils.isEmpty(entity.getDisplayItemsSort())) {
				List<String> ids = Arrays.asList(entity.getDisplayItemsSort().split(","));
				List<ItemModel> displaySortList = new ArrayList<>();
				for (String id:ids) {
					Optional<ItemModel> optional = list.stream().filter(item->id.equals(item.getId())).findFirst();
					if (optional.isPresent()) {
						displaySortList.add(optional.get());
					}
				}
				listModel.setDisplayItems(displaySortList);
			} else {
				listModel.setDisplayItems(list);
			}
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
			List<FunctionModel> functions = new ArrayList();
			for(ListFunction listFunction : entity.getFunctions()) {
				FunctionModel function = new FunctionModel();
				BeanUtils.copyProperties(listFunction, function, new String[]{"listModel", "formModel"});
				functions.add(function);
			}
            Collections.sort(functions);
			listModel.setFunctions(functions);
		}

		if (entity.getSortItems().size() > 0) {
			List<SortItem> sortItems = new ArrayList();
			for (ListSortItem sortItemEntity: entity.getSortItems()) {
				if(sortItemEntity.getItemModel() != null) {
					if (masterFormItemIds.contains(sortItemEntity.getItemModel().getId())==false) {
						continue;
					}
					SortItem sortItem = new SortItem();
					BeanUtils.copyProperties(sortItemEntity, sortItem, new String[]{"listModel", "itemModel"});
					ItemModel itemModel = new ItemModel();
					BeanUtils.copyProperties(sortItemEntity.getItemModel(), itemModel, new String[]{"defaultValue", "formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "referenceList", "items", "parentItem"});
					sortItem.setItemModel(itemModel);
					sortItems.add(sortItem);
				}
			}
			listModel.setSortItems(sortItems);
		}

		if (entity.getSearchItems().size() > 0) {
			List<SearchItem> searchItems = new ArrayList();
			for (ListSearchItem searchItemEntity : entity.getSearchItems()) {
				ItemModelEntity itemModelEntity = searchItemEntity.getItemModel();
				if (itemModelEntity != null) {
					if (masterFormItemIds.contains(itemModelEntity.getId())==false) {
						continue;
					}
					SearchItem searchItem = new SearchItem();
					if(itemModelEntity instanceof ReferenceItemModelEntity) {
						ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity)itemModelEntity;
						if (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToMany) {
							searchItem.setMultiple(true);
						} else if (referenceItemModelEntity.getReferenceType() == ReferenceType.OneToOne) {
							searchItem.setMultiple(false);
						} else if (referenceItemModelEntity.getReferenceType() == ReferenceType.ManyToOne ||
								   referenceItemModelEntity.getReferenceType() == ReferenceType.OneToMany) {
							if (itemModelEntity.getType() == ItemType.ReferenceLabel) {
								searchItem.setMultiple(true);
							} else {
								searchItem.setMultiple(false);
							}
						}
					}
					searchItem.setOrderNo(searchItemEntity.getOrderNo());
					BeanUtils.copyProperties(itemModelEntity, searchItem, new String[]{"defaultValue", "formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
					List<ItemSelectOption> options = itemModelEntity.getOptions();
					// 自定义的下拉框，在列表建模的渲染页面，要返回options属性
					if (options!=null && options.size()>0) {
						try {
							searchItem.setOptions(BeanUtils.copyList(options, Option.class, "itemModel"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getReferenceList() != null) {
						ListModel searchReferenceList = new ListModel();
						ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity) itemModelEntity;
						ListModelEntity searchReferenceListEntity = referenceItemModelEntity.getReferenceList();
						searchReferenceList.setId(searchReferenceListEntity.getId());
						searchReferenceList.setName(searchReferenceListEntity.getName());
						searchReferenceList.setMultiSelect(searchReferenceListEntity.isMultiSelect());
						searchReferenceList.setDescription(searchReferenceListEntity.getDescription());
						searchReferenceList.setApplicationId(searchReferenceListEntity.getApplicationId());
						searchItem.setReferenceList(searchReferenceList);
						searchItem.setReferenceListId(searchReferenceListEntity.getId());
						if(referenceItemModelEntity.getItemModelIds() != null) {
							List<String> resultList = new ArrayList<>(Arrays.asList(referenceItemModelEntity.getItemModelIds().split(",")));
							searchItem.setItemModelList(getItemModelList(resultList));
						}
					}

					if (itemModelEntity instanceof SelectItemModelEntity) {
						SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)itemModelEntity;
						// 联动的下拉选择框，若存在parentItem，返回parentItem信息
						if(selectItemModelEntity.getParentItem() != null){
							ItemModel parentItemModel = new ItemModel();
							BeanUtils.copyProperties(selectItemModelEntity.getParentItem(), parentItemModel, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
							if(selectItemModelEntity.getParentItem().getColumnModel() != null){
								ColumnModelInfo columnModel = new ColumnModelInfo();
								BeanUtils.copyProperties(selectItemModelEntity.getParentItem().getColumnModel(), columnModel, new String[] {"dataModel","columnReferences"});
								if(selectItemModelEntity.getParentItem().getColumnModel().getDataModel() != null){
									columnModel.setTableName(selectItemModelEntity.getParentItem().getColumnModel().getDataModel().getTableName());
								}
								parentItemModel.setColumnName(columnModel.getColumnName());
								parentItemModel.setTableName(columnModel.getTableName());
							}
							searchItem.setParentItem(parentItemModel);
							searchItem.setParentItemId(parentItemModel.getId());
						}

						// 在列表建模渲染页面，如果查询条件是连动的下拉选择框，要返回联动的items的信息
						if(selectItemModelEntity.getItems() != null && selectItemModelEntity.getItems().size() > 0) {
							List<ItemModel> chiildrenItemModel = new ArrayList<>();
							for(SelectItemModelEntity childSelectItemModelEntity : selectItemModelEntity.getItems()) {
								ItemModel chiildItemModel = new ItemModel();
								BeanUtils.copyProperties(childSelectItemModelEntity, chiildItemModel, new String[]{"formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "items", "parentItem", "referenceList"});
								if (childSelectItemModelEntity.getColumnModel() != null) {
									ColumnModelInfo columnModel = new ColumnModelInfo();
									BeanUtils.copyProperties(childSelectItemModelEntity.getColumnModel(), columnModel, new String[]{"dataModel", "columnReferences"});
									if (childSelectItemModelEntity.getColumnModel().getDataModel() != null) {
										columnModel.setTableName(childSelectItemModelEntity.getColumnModel().getDataModel().getTableName());
									}
									chiildItemModel.setColumnModel(columnModel);
								}
								chiildrenItemModel.add(chiildItemModel);
							}

							// 设置联动控件
							ItemModel selectItemModel = new ItemModel();
							selectItemModel.setId(selectItemModelEntity.getId());
//							selectItemModel.setType(selectItemModelEntity.getType());
//							selectItemModel.setName(selectItemModelEntity.getName());
//							selectItemModel.setProps(selectItemModelEntity.getProps());
//							selectItemModel.setSystemItemType(selectItemModelEntity.getSystemItemType());
							selectItemModel.setReferenceDictionaryId(selectItemModelEntity.getReferenceDictionaryId());
							selectItemModel.setReferenceDictionaryItemId(selectItemModelEntity.getReferenceDictionaryItemId());
							listModel.getRelevanceItemModelList().add(selectItemModel);
							setChildrenItems(selectItemModel, selectItemModelEntity);
						}
					}

					if (searchItemEntity.getSearch() != null) {
						Search search = new Search();
						BeanUtils.copyProperties(searchItemEntity.getSearch(), search, new String[] {"search", "defaultValue"});
						String defaultValue = searchItemEntity.getSearch().getDefaultValue();
						if(StringUtils.hasText(defaultValue)) {
							ItemType itemType = searchItem.getType();
							// ItemType.InputNumber和ItemType.DatePicker返回的是数字，不是字符串数组格式
							if (searchItem.getSystemItemType() == SystemItemType.CreateDate ||
									(ItemType.InputNumber == itemType && (searchItem.getDecimalDigits() == null ||searchItem.getDecimalDigits() == 0 ))) {
								try {
									search.setDefaultValue(Long.valueOf(defaultValue));
								} catch (Exception e) {
									e.printStackTrace();
									search.setDefaultValue(null);
								}
							// ItemType.Input，ItemType.RadioGroup和ItemType.Editor返回的defaultValue是字符串格式，不是字符串数组格式
							} else if (ItemType.Input.equals(itemType) || ItemType.RadioGroup.equals(itemType) || ItemType.Editor.equals(itemType)) {
								search.setDefaultValue(defaultValue);
							} else if (itemModelEntity instanceof TreeSelectItemModelEntity) {
								TreeSelectItemModelEntity treeSelectItem = (TreeSelectItemModelEntity)itemModelEntity;
								Boolean multiple = treeSelectItem.getMultiple();
								TreeSelectDataSource dataSource = treeSelectItem.getDataSource();
								Set<String> set = new HashSet(Arrays.asList("Department", "Position", "Personnel", "PositionIdentify"));
								if (dataSource!=null && set.contains(dataSource.getValue()) && multiple!=null && multiple) {
									search.setDefaultValue(defaultValue.split(","));
									List<TreeSelectData> list = formInstanceServiceEx.getTreeSelectData(dataSource, defaultValue.split(","));
									if (list!=null) {
										search.setDefaultValueName(list.stream().map(item->item.getName()).collect(Collectors.toList()));
									}
								} else if (dataSource!=null && set.contains(dataSource.getValue()) && multiple!=null && multiple==false) {
									List<TreeSelectData> list = formInstanceServiceEx.getTreeSelectData(dataSource, new String[]{defaultValue});
									search.setDefaultValue(defaultValue);
									if (list!=null && list.size()>0) {
										search.setDefaultValueName(list.get(0).getName());
									}
								}
							} else {
								search.setDefaultValue(Arrays.asList(defaultValue.split(",")));
							}
						}
						searchItem.setSearch(search);
					}
					searchItems.add(searchItem);
				}
			}
			Collections.sort(searchItems);
			listModel.setSearchItems(searchItems);
		}

		if (entity.getQuickSearchItems().size() > 0) {
		    List<QuickSearchItem> quickSearches = new ArrayList<>();
		    for (QuickSearchEntity quickSearchEntity:entity.getQuickSearchItems()) {
				if (quickSearchEntity.getItemModel() != null && masterFormItemIds.contains(quickSearchEntity.getItemModel().getId())==false) {
					continue;
				}
				QuickSearchItem quickSearch = new QuickSearchItem();
				BeanUtils.copyProperties(quickSearchEntity, quickSearch, new String[]{"listModel", "itemModel", "searchValues"});
				if (!StringUtils.isEmpty(quickSearchEntity.getSearchValues())) {
					quickSearch.setSearchValues(Arrays.asList(quickSearchEntity.getSearchValues().split(",")));
				}
				if (quickSearchEntity.getItemModel() != null) {
					ItemModel itemModel = new ItemModel();
					BeanUtils.copyProperties(quickSearchEntity.getItemModel(), itemModel, new String[]{"formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "items", "parentItem", "referenceList"});
					quickSearch.setItemModel(itemModel);
				}
				quickSearches.add(quickSearch);
            }
            Collections.sort(quickSearches);
            listModel.setQuickSearchItems(quickSearches);
        }
		return listModel;
	}

	/**
	 * @param parentItemModel
	 * @param parentItemModelEntity
	 */
	private void setChildrenItems(ItemModel parentItemModel, ItemModelEntity parentItemModelEntity) {
		if (parentItemModelEntity instanceof SelectItemModelEntity) {
			List<ItemModel> list = new ArrayList<>();
			SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)parentItemModelEntity;
			for (SelectItemModelEntity selectItemSonEntity:selectItemModelEntity.getItems()) {
				ItemModel selectItemSonModel = new ItemModel();
				selectItemSonModel.setId(selectItemSonEntity.getId());
//				selectItemSonModel.setType(selectItemSonEntity.getType());
//				selectItemSonModel.setName(selectItemSonEntity.getName());
//				selectItemSonModel.setProps(selectItemSonEntity.getProps());
				selectItemSonModel.setParentItemId(parentItemModelEntity.getId());
//				selectItemSonModel.setSystemItemType(selectItemSonEntity.getSystemItemType());
//				selectItemSonModel.setReferenceDictionaryId(selectItemSonEntity.getReferenceDictionaryId());
//				selectItemSonModel.setReferenceDictionaryItemId(selectItemSonEntity.getReferenceDictionaryItemId());
				list.add(selectItemSonModel);
				setChildrenItems(selectItemSonModel, selectItemSonEntity);
			}
			if (list.size()>0) {
				parentItemModel.setItems(list);
			}
		}
	}

	private List<ItemModel> getItemModelList(List<String> idResultList){
		if(idResultList == null || idResultList.size() < 1){
			return null;
		}

		Set<ItemModelEntity> itemModelEntities = new HashSet<>();
		for(String itemId : idResultList) {
			ItemModelEntity itemModelEntity = itemModelService.find(itemId);
			if (itemModelEntity!=null) {
				itemModelEntities.add(itemModelService.find(itemId));
			}
		}

		List<ItemModel> list = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			ItemModel itemModel = new ItemModel();
			itemModel.setId(itemModelEntity.getId());
			itemModel.setName(itemModelEntity.getName());
			if(itemModelEntity.getColumnModel() != null) {
				itemModel.setTableName(itemModelEntity.getColumnModel().getDataModel().getTableName());
				itemModel.setColumnName(itemModelEntity.getColumnModel().getColumnName());
			}
			list.add(itemModel);
		}
		return list;
	}

	/**
	 *
	 * 获取主表单包含的普通控件item集合
	 * @param masterForm
	 * @return
	 */
	public Set<ItemModelEntity> getMasterFormItems(FormModelEntity masterForm) {
		Set<ItemModelEntity> items = new HashSet<>();
		if (masterForm!=null && masterForm.getItems()!=null) {
			for (ItemModelEntity item:masterForm.getItems()) {
				if (item.getType()!=null) {
					items.add(item);
					items.addAll(getItemsInItem(item));
				}
			}
		}
		return items;
	}

	/**
	 * 获取标签页里面嵌套保存的子Item
	 * @param tabsItemModelEntity
	 * @return
	 */
	public List<ItemModelEntity> getTabsInsideItems(TabsItemModelEntity tabsItemModelEntity) {
		List<ItemModelEntity> items = new ArrayList<>();
		if (tabsItemModelEntity.getItems()!=null && tabsItemModelEntity.getItems().size()>0) {
			List<TabPaneItemModelEntity> tabPaneItems = tabsItemModelEntity.getItems();
			for (TabPaneItemModelEntity tabPaneItem:tabPaneItems) {
				List<ItemModelEntity> tabPaneReferenceItems = tabPaneItem.getItems();
				if (tabPaneReferenceItems!=null && tabPaneReferenceItems.size()>0) {
					items.addAll(tabPaneReferenceItems);
				}
			}
		}
		return items;
	}

	public List<ItemModelEntity> getItemsInItem(ItemModelEntity itemModelEntity) {
		List<ItemModelEntity> list = new ArrayList<>();
		if (itemModelEntity!=null) {
			list.add(itemModelEntity);
			Class clazz = itemModelEntity.getClass();  //得到类对象
			Field[] fs = clazz.getDeclaredFields();    //得到属性集合
			for (Field f:fs) {                         //遍历属性
				if (f.getName().equals("items")) {
					f.setAccessible(true);             //设置属性是可以访问的(私有的也可以)
					try {
						Object itemValues = f.get(itemModelEntity);
						if (itemValues!=null && itemValues instanceof List) {
							List<ItemModelEntity> items = (List<ItemModelEntity>)itemValues;
							for (ItemModelEntity subItem:items) {
								list.addAll(getItemsInItem(subItem));
							}
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return list;
	}
}
