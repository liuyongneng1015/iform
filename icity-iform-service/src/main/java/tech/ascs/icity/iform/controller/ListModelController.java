package tech.ascs.icity.iform.controller;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.admin.api.model.Application;
import tech.ascs.icity.admin.api.model.TreeSelectData;
import tech.ascs.icity.admin.client.ApplicationService;
import tech.ascs.icity.admin.client.GroupService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.model.ListModel.SortItem;
import tech.ascs.icity.iform.api.model.SearchItem.Search;
import tech.ascs.icity.iform.api.model.export.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.utils.BeanCopiers;
import tech.ascs.icity.iform.utils.ExportListFunctionUtils;
import tech.ascs.icity.iform.utils.ResourcesUtils;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.NameEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

import javax.annotation.PostConstruct;

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
	private ExportDataService exportDataService;

	@Autowired
	GroupService groupService;

	@Value("${appListTemplate.file-path:classpath:config/appListTemplate.json}")
	private String appListTemplatePath;
	private List<Map> appListTemplateList;
	private ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void initData() throws IOException {
		String appListTemplateStr = ResourcesUtils.readFileToString(appListTemplatePath);
		appListTemplateList = (List<Map>)objectMapper.readValue(appListTemplateStr, List.class);
	}

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
		if (entity == null || entity.getMasterForm() == null) {
            return null;
		}
		try {
			return toDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public ListModel getAppListModelById(@PathVariable(name="id") String id) {
		ListModel listModel = get(id);
		if (listModel==null) {
			return listModel;
		}
		List<Map> appListTemplate = listModel.getAppListTemplate();
		if (appListTemplate!=null && appListTemplate.size()>0) {
			for (Map map:appListTemplate) {
				Object visible = map.get("visible");
				if (visible!=null && visible instanceof Boolean && (Boolean)visible &&
						map.get("template")!=null && map.get("template") instanceof List) {
					listModel.setAppListTemplate((List)map.get("template"));
					return listModel;
				}
			}
		}
		// appListTemplate字段要返回
		listModel.setAppListTemplate(new ArrayList<>());
		return listModel;
	}

	@Override
	public ListModel find(@RequestParam(name = "uniqueCode") String uniqueCode) {
		if (StringUtils.isEmpty(uniqueCode)) {
			throw new ICityException("uniqueCode不允许为空");
		}
		ListModelEntity entity = listModelService.query().filterEqual("uniqueCode", uniqueCode).first();
		if (entity!=null) {
			return toDTO(entity);
		} else {
			return null;
		}
	}

	// 新增列表的时候，自动创建新增、批量删除，为系统自带功能
	private DefaultFunctionType[] functionDefaultActions = {DefaultFunctionType.Add, DefaultFunctionType.BatchDelete, DefaultFunctionType.Export, DefaultFunctionType.TemplateDownload, DefaultFunctionType.Import};
	private String[] parseAreas = { ParseArea.PC.value()+","+ParseArea.APP.value(), null , ParseArea.PC.value(), ParseArea.PC.value(), ParseArea.PC.value()};
	private String[] functionDefaultIcons = new String[]{null, "icon-xuanzhong", null, null, null};
	private String[] functionDefaultMethods = new String[]{"POST", "DELETE", "GET", "GET", "POST"};
	private Boolean[] functionVisibles = {true, true, true, true, true};
	private List<Consumer<ListFunction>> functionOtherControl = Arrays.asList(null, null, ExportListFunctionUtils::assemblyDefaultExportListFunction, null, ExportListFunctionUtils::assemblyDefaultImportBaseFunction);

	@Override
	public IdEntity createListModel(@RequestBody ListModel ListModel) {
		if (StringUtils.hasText(ListModel.getId())) {
			throw new IFormException("列表模型ID不为空，请使用更新操作");
		}
		if (ListModel.getMasterForm()==null || StringUtils.isEmpty(ListModel.getMasterForm().getId())) {
			throw new IFormException("关联表单的ID不能为空");
		}
		if (StringUtils.hasText(ListModel.getUniqueCode())) {
			if (listModelService.query().filterEqual("uniqueCode", ListModel.getUniqueCode()).count()>0) {
				throw new ICityException("唯一编码与已存在的列表建模有重复");
			}
		}
		ListModelEntity entity = listModelService.toListModelEntity(ListModel);
		for (Map map:appListTemplateList) {
			map.put("id", UUID.randomUUID().toString().replaceAll("-",""));
		}

		try {
			entity.setAppListTemplate(objectMapper.writeValueAsString(appListTemplateList));
			ListModel.setDataPermissions(DataPermissionsType.AllPeople);
			// 创建默认的功能按钮
			List<ListFunction> functions = new ArrayList<>();
			for (int i = 0; i < functionDefaultActions.length; i++) {
				ListFunction function = new ListFunction();
				function.setAction(functionDefaultActions[i].getValue());
				function.setLabel(functionDefaultActions[i].getDesc());
				function.setMethod(functionDefaultMethods[i]);
                function.setParseArea(parseAreas[i]);
				function.setReturnResult(ReturnResult.NONE);
                function.setVisible(functionVisibles[i]);
                function.setIcon(functionDefaultIcons[i]);
                function.setSystemBtn(true);
				function.setOrderNo(i+1);
				function.setListModel(entity);
                if (functionOtherControl.get(i) != null) {
                    functionOtherControl.get(i).accept(function);
                }
				functions.add(function);
			}
			entity.setFunctions(functions);
            assemblyItemModelEntityImportFunction(entity);
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
		if (StringUtils.hasText(ListModel.getUniqueCode())) {
			if (listModelService.query().filterEqual("uniqueCode", ListModel.getUniqueCode()).filterNotEqual("id", ListModel.getId()).count()>0) {
				throw new ICityException("唯一编码与已存在的列表建模有重复");
			}
		}
		// 校验默认的功能按钮是否被删除
		checkDefaultFuncExists(ListModel);
		try {
			ListModelEntity entity = listModelService.toListModelEntity(ListModel);
			listModelService.save(entity);
		} catch (Exception e) {
		    throw new IFormException("保存列表模型列表失败：" + e.getMessage(), e);
		}
	}

	// 校验默认的功能按钮是否被删除
	private void checkDefaultFuncExists(ListModel listModel) {
		List<FunctionModel> functions = listModel.getFunctions();
		// 校验功能按钮的编码不允许为空和同名
		if (functions.stream().filter(item->item.getAction()!=null).map(item->item.getAction()).collect(Collectors.toSet()).size()<functions.size()) {
			throw new IFormException("功能按钮的编码不能为空和同名");
		}
		// 校验功能按钮的功能名不允许为空和同名
		if (functions.stream().filter(item->item.getLabel()!=null).map(item->item.getLabel()).collect(Collectors.toSet()).size()<functions.size()) {
			throw new IFormException("功能按钮的功能名能为空和同名");
		}
		for (FunctionModel function:functions) {
			if (function.getAction().contains("-")||function.getAction().contains("#")) {
				throw new IFormException("功能按钮的编码不能包含 - 和 # 字符");
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
														   @RequestParam(name="applicationId", required = false) String applicationId) {
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
		return listModelService.getFirstListModelByTableName(tableName);
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

	private List<Map> toAppListTemplate(String appListTemplate) {
		if (StringUtils.hasText(appListTemplate)) {
			try {
				List<Map> list = objectMapper.readValue(appListTemplate, List.class);
				if (list!=null && list.size()>0) {
					return list;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Map map:appListTemplateList) {
			map.put("id", UUID.randomUUID().toString().replaceAll("-",""));
		}
		return appListTemplateList;
	}

	private List<Map> toProtalListTemplate(String protalListTemplate) {
		return new ArrayList();
	}

	private ListModel toDTO(ListModelEntity listModelEntity) {
		ListModel listModel = new ListModel();
		BeanUtils.copyProperties(listModelEntity, listModel, new String[] {"masterForm", "slaverForms", "sortItems", "searchItems", "functions", "displayItems", "quickSearchItems", "protalListTemplate", "appListTemplate"});

		listModel.setAppListTemplate(toAppListTemplate(listModelEntity.getAppListTemplate()));
		listModel.setProtalListTemplate(toProtalListTemplate(listModelEntity.getProtalListTemplate()));


		if(listModelEntity.getMasterForm() != null){
			FormModelEntity formModelEntity = listModelEntity.getMasterForm();
			FormModel masterForm = new FormModel();
			BeanUtils.copyProperties(formModelEntity, masterForm, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});
			listModel.setMasterForm(masterForm);
		}

		Set<String> masterFormItemIds = formModelService.findAllItems(listModelEntity.getMasterForm()).stream().map(item->item.getId()).collect(Collectors.toSet());

		setDisplayItems(listModelEntity, listModel, masterFormItemIds);
		setFunctions(listModelEntity, listModel);
		setSortItems(listModelEntity, listModel, masterFormItemIds);
		setQuickSearchItems(listModelEntity, listModel, masterFormItemIds);
		setSearchItems(listModelEntity, listModel, masterFormItemIds);

		if(listModelEntity.getSlaverForms() != null){
			List<FormModel> list = new ArrayList<>();
			for(FormModelEntity formModelEntity : listModelEntity.getSlaverForms()) {
				FormModel slaverForm = new FormModel();
				BeanUtils.copyProperties(formModelEntity, slaverForm, new String[]{"items", "dataModels", "permissions", "submitChecks","functions", "triggeres"});
				list.add(slaverForm);
			}
			listModel.setSlaverForms(list);
		}

		return listModel;
	}

	private void setDisplayItems(ListModelEntity listModelEntity, ListModel listModel, Set<String> masterFormItemIds) {
		if(listModelEntity.getDisplayItems() != null){
			List<ItemModel> displaySortList = new ArrayList<>();
			List<ItemModel> displayItems = new ArrayList<>();
			Map<String, ItemModel> map = new HashMap<>();
			for(ItemModelEntity itemModelEntity : listModelEntity.getDisplayItems()) {
				if (masterFormItemIds.contains(itemModelEntity.getId())) {
					ItemModel itemModel = new ItemModel();
					itemModelService.copyItemModelEntityToItemModel(itemModelEntity, itemModel);
					displayItems.add(itemModel);
					map.put(itemModel.getId(), itemModel);
				}
			}
			// displayItem是有排序的，排序的ID全部拼接到displayItemsSort这个字段
			if (!StringUtils.isEmpty(listModelEntity.getDisplayItemsSort())) {
				List<String> ids = Arrays.asList(listModelEntity.getDisplayItemsSort().split(","));
				for (String id:ids) {
					ItemModel itemModel = map.get(id);
					if (itemModel!=null) {
						displaySortList.add(itemModel);
					}
				}
				listModel.setDisplayItems(displaySortList);
			} else { // 兼容旧数据，没有排序字段的话，直接赋值displayItems
				listModel.setDisplayItems(displayItems);
			}
		}
	}

	private void setFunctions(ListModelEntity listModelEntity, ListModel listModel) {
		if(listModelEntity.getFunctions() != null){
			List<FunctionModel> functions = new ArrayList();
			for(ListFunction listFunction : listModelEntity.getFunctions()) {
				FunctionModel function = new FunctionModel();
				BeanUtils.copyProperties(listFunction, function, new String[]{"listModel", "formModel", "parseArea", "exportFunction"});
				if (StringUtils.hasText(listFunction.getParseArea())) {
					function.setParseArea(Arrays.asList(listFunction.getParseArea().split(",")));
				}
				assemblyFunctionModel(listModelEntity, listFunction, function);
				functions.add(function);
			}
			Collections.sort(functions);
			listModel.setFunctions(functions);
		}
	}

	private void setSortItems(ListModelEntity listModelEntity, ListModel listModel, Set<String> masterFormItemIds) {
		if (listModelEntity.getSortItems().size() > 0) {
			List<SortItem> sortItems = new ArrayList();
			for (ListSortItem sortItemEntity: listModelEntity.getSortItems()) {
				if(sortItemEntity.getItemModel() != null) {
					if (masterFormItemIds.contains(sortItemEntity.getItemModel().getId())==false) {
						continue;
					}
					SortItem sortItem = new SortItem();
					BeanUtils.copyProperties(sortItemEntity, sortItem, new String[]{"listModel", "itemModel"});
					ItemModel itemModel = new ItemModel();
					itemModelService.copyItemModelEntityToItemModel(sortItemEntity.getItemModel(), itemModel);
					sortItem.setItemModel(itemModel);
					sortItems.add(sortItem);
				}
			}
			listModel.setSortItems(sortItems);
		}
	}

	private void setQuickSearchItems(ListModelEntity listModelEntity, ListModel listModel, Set<String> masterFormItemIds) {
		if (listModelEntity.getQuickSearchItems().size() > 0) {
			List<QuickSearchItem> quickSearches = new ArrayList<>();
			for (QuickSearchEntity quickSearchEntity:listModelEntity.getQuickSearchItems()) {
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
					itemModelService.copyItemModelEntityToItemModel(quickSearchEntity.getItemModel(), itemModel);
					quickSearch.setItemModel(itemModel);
				}
				quickSearches.add(quickSearch);
			}
			Collections.sort(quickSearches);
			listModel.setQuickSearchItems(quickSearches);
		}
	}

	private void setSearchItems(ListModelEntity listModelEntity, ListModel listModel, Set<String> masterFormItemIds) {
		if (listModelEntity.getSearchItems().size() > 0) {
			Map<String, SearchItem> map = new HashMap<>();
			List<SearchItem> searchItems = new ArrayList<>();
			for (ListSearchItem searchItemEntity : listModelEntity.getSearchItems()) {
				ItemModelEntity itemModelEntity = searchItemEntity.getItemModel();
				if (itemModelEntity != null) {
					if (masterFormItemIds.contains(itemModelEntity.getId())==false) {
						continue;
					}
					SearchItem searchItem = new SearchItem();
					BeanUtils.copyProperties(searchItemEntity, searchItem, "listModel", "itemModel", "search", "parseArea");
					if (StringUtils.hasText(searchItemEntity.getParseArea())) {
						searchItem.setParseArea(Arrays.asList(searchItemEntity.getParseArea().split(",")));
					}
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
					itemModelService.copyItemModelEntityToItemModel(itemModelEntity, searchItem);
					// 更新时间和创建时间控件的type字段要改成 日期控件 ItemType.DatePicker
					if (itemModelEntity.getSystemItemType() == SystemItemType.CreateDate ||
						itemModelEntity.getSystemItemType() == SystemItemType.DatePicker) {
						searchItem.setType(ItemType.DatePicker);
						searchItem.setProps(searchItemEntity.getProps());
					}
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
						searchReferenceList.setMultiSelect(searchReferenceListEntity.getMultiSelect());
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
						// 是否是联动解绑，是的话，把 DictionaryValueType 属性取值 改成  Fixed
						if (searchItemEntity.getLinkageDataUnbind()!=null && searchItemEntity.getLinkageDataUnbind()) {
							searchItem.setDictionaryValueType(DictionaryValueType.Fixed);
						}
						SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)itemModelEntity;
						// 联动的下拉选择框，若存在parentItem，返回parentItem信息
						if(selectItemModelEntity.getParentItem() != null){
							ItemModel parentItemModel = new ItemModel();
							itemModelService.copyItemModelEntityToItemModel(selectItemModelEntity.getParentItem(), parentItemModel);
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
								ItemModel childItemModel = new ItemModel();
								itemModelService.copyItemModelEntityToItemModel(childSelectItemModelEntity.getParentItem(), childItemModel);
								if (childSelectItemModelEntity.getColumnModel() != null) {
									ColumnModelInfo columnModel = new ColumnModelInfo();
									BeanUtils.copyProperties(childSelectItemModelEntity.getColumnModel(), columnModel, new String[]{"dataModel", "columnReferences"});
									if (childSelectItemModelEntity.getColumnModel().getDataModel() != null) {
										columnModel.setTableName(childSelectItemModelEntity.getColumnModel().getDataModel().getTableName());
									}
									childItemModel.setColumnModel(columnModel);
								}
								chiildrenItemModel.add(childItemModel);
							}
							searchItem.setItems(chiildrenItemModel);
						}
					}

					setSearch(searchItemEntity, searchItem, itemModelEntity);
					map.put(searchItem.getId(), searchItem);
					searchItems.add(searchItem);
				}
			}
			List<SearchItem> searchItemSortList = new ArrayList<>();

			// searchItem是有排序的，排序的ID全部拼接到searchItemsSort这个字段
			if (!StringUtils.isEmpty(listModelEntity.getSearchItemsSort())) {
				List<String> ids = Arrays.asList(listModelEntity.getSearchItemsSort().split(","));
				for (String id:ids) {
					SearchItem searchItem = map.get(id);
					if (searchItem!=null) {
						searchItemSortList.add(searchItem);
					}
				}
				listModel.setSearchItems(searchItemSortList);
			} else {
				listModel.setSearchItems(searchItems);
			}
			listModel.setSearchItemsSort(null);
		}
	}

	private void setSearch(ListSearchItem searchItemEntity, SearchItem searchItem, ItemModelEntity itemModelEntity) {
		if (searchItemEntity.getSearch() != null) {
			Search search = new Search();
			BeanUtils.copyProperties(searchItemEntity.getSearch(), search, new String[]{"search", "defaultValue"});
			String defaultValue = searchItemEntity.getSearch().getDefaultValue();
			if (StringUtils.hasText(defaultValue)) {
				ItemType itemType = searchItem.getType();
				// ItemType.InputNumber和ItemType.DatePicker返回的是数字，不是字符串数组格式
				if (searchItem.getSystemItemType() == SystemItemType.CreateDate ||
						searchItem.getSystemItemType() == SystemItemType.DatePicker) {
					String[] arr = defaultValue.split(",");
					if (arr.length==1) {
						try {
							search.setDefaultValue(Long.valueOf(arr[0]));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						List list = new ArrayList();
						for (String item:arr) {
							try {
								list.add(Long.valueOf(item));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						search.setDefaultValue(list);
					}
				} else if (ItemType.InputNumber == itemType && (searchItem.getDecimalDigits() == null || searchItem.getDecimalDigits() == 0)) {
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
					TreeSelectItemModelEntity treeSelectItem = (TreeSelectItemModelEntity) itemModelEntity;
					Boolean multiple = treeSelectItem.getMultiple();
					TreeSelectDataSource dataSource = treeSelectItem.getDataSource();
					Set<String> set = new HashSet(Arrays.asList("Department", "Position", "Personnel", "PositionIdentify", "Role"));
					if (dataSource != null && set.contains(dataSource.getValue()) && multiple != null && multiple) {
						List<TreeSelectData> list = formInstanceServiceEx.getTreeSelectData(dataSource, defaultValue.split(","), treeSelectItem.getReferenceDictionaryId());
						if (list != null) {
							search.setDefaultValue(list.stream().map(item->item.getId()).collect(Collectors.toList()));
							search.setDefaultValueName(list.stream().map(item -> item.getName()).collect(Collectors.toList()));
						} else {
							search.setDefaultValue(new ArrayList<>());
							search.setDefaultValueName(new ArrayList<>());
						}
					} else if (dataSource != null && set.contains(dataSource.getValue()) && multiple != null && multiple == false) {
						List<TreeSelectData> list = formInstanceServiceEx.getTreeSelectData(dataSource, new String[]{defaultValue}, treeSelectItem.getReferenceDictionaryId());
						if (list != null && list.size() > 0) {
							search.setDefaultValue(list.get(0).getId());
							search.setDefaultValueName(list.get(0).getName());
						}
					}
				} else {
					search.setDefaultValue(Arrays.asList(defaultValue.split(",")));
				}
			}
			searchItem.setSearch(search);
		}
	}

	private List<ItemModel> getItemModelList(List<String> idResultList){
		if(idResultList == null || idResultList.size() < 1){
			return null;
		}

		List<ItemModelEntity> itemModelEntities = itemModelService.query().filterIn("id", idResultList).list();
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

	private void assemblyItemModelEntityImportFunction(ListModelEntity entity) {
		FormModelEntity dataModelEntity = formModelService.find(entity.getMasterForm().getId());
		List<ItemModelEntity> entities = exportDataService.eachHasColumnItemModel(dataModelEntity.getItems());
        entities.forEach(item -> {
            item.setTemplateName(item.getName());
            item.setTemplateSelected(false);
            item.setMatchKey(false);
            item.setDataImported(false);
        });
		entities.stream().filter(item -> "id".equals(item.getName()))
                .findAny()
                .ifPresent(item -> item.setMatchKey(true));

		entity.setMasterForm(dataModelEntity);
	}


	private void assemblyFunctionModel(ListModelEntity listModelEntity, ListFunction function, FunctionModel functionModel) {
	    if (DefaultFunctionType.Export.getValue().equals(function.getAction()) && function.getExportFunction() != null) {
	        ExportListFunction functionEntiry = function.getExportFunction();
            ExportFunctionModel exportModel = new ExportFunctionModel();
            exportModel.setControl(functionEntiry.getControl());
            exportModel.setFormat(functionEntiry.getFormat());
            exportModel.setType(functionEntiry.getType());
            exportModel.setCustomExport(Optional.ofNullable(functionEntiry.getCustomExport()).filter(StringUtils::hasText).map(str -> str.split(",")).map(Arrays::asList).orElseGet(Collections::emptyList));
            functionModel.setExportFunction(exportModel);
        }else if (DefaultFunctionType.TemplateDownload.getValue().equals(function.getAction())) {
			List<ItemModelEntity> items = exportDataService.eachHasColumnItemModel(listModelEntity.getMasterForm().getItems());
			List<TemplateItemModel> models = items.stream()
					.map(item -> {
						TemplateItemModel model = new TemplateItemModel();
						model.setId(item.getId());
						model.setItemName(item.getName());
						model.setSelected(item.isTemplateSelected());
						model.setTemplateName(item.getTemplateName());
						return model;
					}).collect(Collectors.toList());
			functionModel.setTemplateItemModels(models);
		} else if (DefaultFunctionType.Import.getValue().equals(function.getAction()) && function.getImportFunction()!=null) {
			List<ItemModelEntity> items = exportDataService.eachHasColumnItemModel(listModelEntity.getMasterForm().getItems());
			ImportFunctionModel model = new ImportFunctionModel();
			BeanCopiers.noConvertCopy(function.getImportFunction(), model);
			List<ImportTemplateItemModel> itemModels = items.stream()
					.map(item -> {
						ImportTemplateItemModel  importModel = new ImportTemplateItemModel();
						importModel.setId(item.getId());
						importModel.setImported(item.isDataImported());
						importModel.setItemName(item.getName());
						importModel.setTemplateName(item.getTemplateName());
						importModel.setKey(item.isMatchKey());
						return importModel;
					}).collect(Collectors.toList());
			model.setTemplateItemModels(itemModels);
			functionModel.setImportFunction(model);
		}
    }
}
