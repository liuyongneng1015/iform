package tech.ascs.icity.iform.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.googlecode.genericdao.search.Sort;
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
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.utils.CommonUtils;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "表单模型服务", description = "包含表单模型的增删改查等功能")
@RestController
public class FormModelController implements tech.ascs.icity.iform.api.service.FormModelService {

    private static Map<String, Object> concurrentmap = new ConcurrentHashMap<String, Object>();

	@Autowired
	private FormModelService formModelService;

	@Autowired
	private FormInstanceService formInstanceService;

	@Autowired
	private ListModelService listModelService;

	@Autowired
	private DataModelService dataModelService;

	@Autowired
	private ItemModelService itemModelService;

	@Autowired
	private ColumnModelService columnModelService;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	GroupService groupService;

	@Override
	public List<FormModel> list(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query().sort(Sort.desc("id"));
			if (StringUtils.hasText(name)) {
				query.filterLike("name", CommonUtils.convertParamOfFuzzySearch(name));
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}
			List<FormModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public Page<FormModel> page(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name="page", defaultValue="1") int page,
								@RequestParam(name="pagesize", defaultValue="10") int pagesize, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", CommonUtils.convertParamOfFuzzySearch(name));
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}
			Page<FormModelEntity> entities = query.sort(Sort.desc("id")).page(page, pagesize).page();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public FormModel get(@PathVariable(name="id") String id) {
		FormModelEntity entity = formModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "表单模型【" + id + "】不存在");
		}
		try {
			return toDTODetail(entity);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public IdEntity saveFormDataModel(@RequestBody FormModel formModel) {
        String key = formModel.getId()+"_"+formModel.getName();
        FormModelEntity oldEntity = null;
        try {
            if(concurrentmap.get(key) != null){
                throw  new IFormException("请不要重复提交");
            }
            concurrentmap.put(key, System.currentTimeMillis());
            //校验表名
            if(formModel != null && formModel.getDataModels() != null && formModel.getDataModels().size() > 0) {
                DataModel dataModel = formModel.getDataModels().get(0);
                DataModelEntity dataModelEntity = new DataModelEntity();
                dataModelEntity.setId(dataModel.isNew()? null : dataModel.getId());
                dataModelEntity.setTableName(dataModel.getTableName());
                veryTableName(dataModelEntity);
            }
            verifyFormModelName(formModel);
            oldEntity = formModelService.saveFormModel(formModel);
        } catch (Exception e) {
            if(e instanceof IFormException){
                throw e;
            }
            throw new IFormException(e.getMessage());
        }finally {
            if(concurrentmap.containsKey(key)){
                concurrentmap.remove(key);
            }
        }
        return new IdEntity(oldEntity.getId());
	}



	@Override
	public IdEntity createFormModel(@RequestBody FormModel formModel) {
		if (StringUtils.hasText(formModel.getId())) {
			throw new IFormException("表单模型ID不为空，请使用更新操作");
		}
        String key = formModel.getId()+"_"+formModel.getName();
        try {
            if(concurrentmap.get(key) != null){
                throw  new IFormException("请不要重复提交");
            }
            concurrentmap.put(key, System.currentTimeMillis());
			FormModelEntity entity = wrap(formModel);
			entity = formModelService.save(entity);
			return new IdEntity(entity.getId());
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}finally {
            if(concurrentmap.containsKey(key)){
                concurrentmap.remove(key);
            }
        }
	}

	@Override
	public void updateFormModel(@PathVariable(name = "id") String id, @RequestBody FormModel formModel) {
		if (!StringUtils.hasText(formModel.getId()) || !id.equals(formModel.getId())) {
			throw new IFormException("表单模型ID不一致");
		}
        String key = formModel.getId()+"_"+formModel.getName();
        try {
            if(concurrentmap.get(key) != null){
                throw  new IFormException("请不要重复提交");
            }
            concurrentmap.put(key, System.currentTimeMillis());
			FormModelEntity entity = wrap(formModel);
			formModelService.save(entity);
			listModelService.submitFormBtnPermission(entity);
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}finally {
            if(concurrentmap.containsKey(key)){
                concurrentmap.remove(key);
            }
        }
	}

	@Override
	public void removeFormModel(@PathVariable(name="id", required = true) String id) {
        String key = id;
        try {
            if (concurrentmap.get(key) != null) {
                throw new IFormException("请不要重复提交");
            }
            concurrentmap.put(key, System.currentTimeMillis());
			FormModelEntity formModelEntity = formModelService.get(id);
			List<ItemModelEntity> lists = formModelService.getAllColumnItems(formModelEntity.getItems());
			List<ItemModelEntity> list = lists.parallelStream().sorted((d2, d1) -> d2.getOrderNo().compareTo(d1.getOrderNo())).collect(Collectors.toList());
			for(int i = 0 ; i < list.size() ; i ++){
				ItemModelEntity itemModelEntity1 = list.get(i);
				if(itemModelEntity1 instanceof SubFormItemModelEntity){
					columnModelService.deleteTable(itemModelEntity1.getColumnModel().getDataModel().getTableName());
				}else {
					columnModelService.deleteTableColumn(itemModelEntity1.getColumnModel().getDataModel().getTableName(), itemModelEntity1.getColumnModel().getColumnName());
				}
			}
			formModelService.delete(formModelEntity);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(concurrentmap.containsKey(key)){
                concurrentmap.remove(key);
            }
        }
	}

	@Override
	public PCFormModel getPCFormModelById(@PathVariable(name="id") String id) {
		FormModelEntity entity = formModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "表单模型【" + id + "】不存在");
		}
		try {
			return toPCDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public FormModel getByItemModelId(@RequestParam(name="itemModelId") String itemModelId) {
		ItemModelEntity itemModelEntity = itemModelService.get(itemModelId);
		if (itemModelEntity == null) {
			throw new IFormException(404, "控件【" + itemModelId + "】不存在");
		}
		String referenceFormId = ((ReferenceItemModelEntity) itemModelEntity).getReferenceFormId();
		if(!(itemModelEntity instanceof  ReferenceItemModelEntity) || !StringUtils.hasText(referenceFormId)){
			throw new IFormException(404, "【" + itemModelId + "】控件不是关联类型");
		}
		List<String> stringList = Arrays.asList(((ReferenceItemModelEntity) itemModelEntity).getItemModelIds().split(","));
		FormModelEntity formModelEntity = formModelService.find( referenceFormId);
		if (formModelEntity == null) {
			throw new IFormException(404, "【" + referenceFormId + "】表单模型不存在");
		}
		formModelEntity.setItems(formModelService.getAllColumnItems(formModelEntity.getItems()));
		try {
			 FormModel formModel = toDTO(formModelEntity);
			 for(ItemModel itemModel : formModel.getItems()){
			 	if(stringList.contains(itemModel.getId())){
			 		itemModel.setSelectFlag(true);
				}else{
					itemModel.setSelectFlag(false);
				}
			 }
			 return formModel;
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void saveFormModelSubmitCheck(@PathVariable(name="id", required = true) String id,@RequestBody FormModel formModel) {
		if (!StringUtils.hasText(formModel.getId()) || !id.equals(formModel.getId())) {
			throw new IFormException("表单模型ID不一致");
		}
		try {
			FormModelEntity entity = wrapSubmitCheck(formModel);
			formModelService.saveFormModelSubmitCheck(entity);
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<ApplicationModel> findApplicationFormModel(@RequestParam(name="applicationId", required = true) String applicationId,
														   @RequestParam(name="columnId", required = false) String columnId,
														   @RequestParam(name="formModelId", required = false) String formModelId) {
		List<FormModelEntity> formModels = null;
		if(StringUtils.hasText(columnId)){
			formModels = findFormModelsByColumnId(columnId);
		}else if(StringUtils.hasText(formModelId)){
			formModels = findFormModelsByFormModelId(formModelId);
			if(formModels == null || formModels.size() < 1){
				return new ArrayList<>();
			}
		}

		if(formModels == null || formModels.size() < 1) {
			 formModels = formModelService.findAll();
		}
		List<FormModel> formModelList = new ArrayList<>();
		Map<String, List<FormModel>> map = new HashMap<>();
		List<FormModelEntity> formModelEntityList = formModels.parallelStream().sorted(Comparator.comparing(FormModelEntity::getId).reversed()).collect(Collectors.toList());
		for(FormModelEntity entity : formModelEntityList){

			FormModel formModel = new FormModel();
			BeanUtils.copyProperties(entity, formModel, new String[] {"items","dataModels","permissions","submitChecks","functions"});
			if(entity.getDataModels() != null){
				List<DataModel> dateModels = new ArrayList<>();
				for(DataModelEntity dataModelEntity : entity.getDataModels()){
					DataModel dataModel = new DataModel();
					BeanUtils.copyProperties(dataModelEntity, dataModel, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
					dateModels.add(dataModel);
				}
				formModel.setDataModels(dateModels);
			}

			formModelList.add(formModel);

			if(!StringUtils.hasText(entity.getApplicationId())){
				continue;
			}
			List<FormModel> list = map.get(entity.getApplicationId());
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(formModel);
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
				for (int i = 0 ; i <  applicationList.size(); i++) {
					Application application  = applicationList.get(i);
					if(application.getId().equals(applicationId)){
						applicationFormModels.add(createApplicationModel(application, map));
						break;
					}
				}
				for (int i = 0 ; i <  applicationList.size(); i++) {
					Application application  = applicationList.get(i);
					if(application.getId().equals(applicationId)){
						continue;
					}
					applicationFormModels.add(createApplicationModel(application, map));
				}
			}
		}

		return applicationFormModels;
	}

	private List<FormModelEntity> findFormModelsByColumnId(String columnId){
		ColumnModelEntity columnModelEntity = columnModelService.get(columnId);
		if(columnModelEntity == null){
			throw new IFormException("未找到对应【" + columnId+"】的字段");
		}
		List<ColumnReferenceEntity> referenceEntityList = columnModelEntity.getColumnReferences();
		Set<FormModelEntity>  formModelEntitySet = new HashSet<>();
		for(ColumnReferenceEntity columnReferenceEntity : referenceEntityList) {
			List<FormModelEntity> formModelList = formModelService.listByDataModel(columnReferenceEntity.getToColumn().getDataModel());
			if(formModelList != null){
				for(FormModelEntity formModelEntity : formModelList) {
					formModelEntitySet.add(formModelEntity);
				}
			}
		}
		return new ArrayList<>(formModelEntitySet);
	}

	private List<FormModelEntity> findFormModelsByFormModelId(String formModelId){
		List<ReferenceItemModelEntity> itemModelEntities = itemModelService.findRefenceItemByFormModelId(formModelId);
		if(itemModelEntities == null){
			throw new IFormException("未找到对应【" + formModelId+"】的关联控件");
		}
		Set<FormModelEntity>  formModelEntitySet = new HashSet<>();

		List<String> dataModelIds = new ArrayList<>();
		for(ReferenceItemModelEntity referenceItemModelEntity : itemModelEntities) {
			if(referenceItemModelEntity.getColumnModel() != null && referenceItemModelEntity.getColumnModel().getDataModel() != null) {
				dataModelIds.add(referenceItemModelEntity.getColumnModel().getDataModel().getId());
			}
			if(referenceItemModelEntity.getFormModel() != null){
				formModelEntitySet.add(referenceItemModelEntity.getFormModel());
			}else if(StringUtils.hasText(referenceItemModelEntity.getSourceFormModelId())){
				FormModelEntity formModelEntity = formModelService.get(referenceItemModelEntity.getSourceFormModelId());
				if(formModelEntity != null){
					formModelEntitySet.add(formModelEntity);
				}
			}
		}
		List<FormModelEntity> list = formModelService.listByDataModelIds(dataModelIds);
		formModelEntitySet.addAll(list);
		return new ArrayList<>(formModelEntitySet);
	}

	private ApplicationModel createApplicationModel(Application application, Map<String, List<FormModel>> map){
		ApplicationModel applicationFormModel = new ApplicationModel();
		applicationFormModel.setId(application.getId());
		applicationFormModel.setName(application.getApplicationName());
		applicationFormModel.setFormModels(map.get(application.getId()));
		return applicationFormModel;
	}

	@Override
	public List<ItemModel> findItemsByFormId(@RequestParam(name="id", required = true) String id, @RequestParam(name="itemId", required = false) String itemId) {
		FormModelEntity formModelEntity = formModelService.get(id);
		if(formModelEntity == null){
			throw new IFormException("未找到【"+id+"】对应的表单");
		}
		List<ItemModel> itemModelList = new ArrayList<>();
		List<ItemModelEntity> list =  formModelService.getAllColumnItems(formModelEntity.getItems());
		if(list != null) {
			for (ItemModelEntity itemModelEntity : list){
				if(itemId != null && itemModelEntity.getId().equals(itemId)) {
					if(itemModelEntity.getColumnModel() != null && (itemModelEntity.getColumnModel().getColumnName().equals("id")
							|| itemModelEntity.getColumnModel().getColumnName().equals("master_id"))){
						continue;
					}
					itemModelList.add(convertItemModelByEntity(itemModelEntity));
					break;
				}
			}
			for (ItemModelEntity itemModelEntity : list){
				if(itemId != null && itemModelEntity.getId().equals(itemId)) {
					continue;
				}
				if(itemModelEntity.getColumnModel() != null && (itemModelEntity.getColumnModel().getColumnName().equals("id")
						|| itemModelEntity.getColumnModel().getColumnName().equals("master_id"))){
					continue;
				}
				itemModelList.add(convertItemModelByEntity(itemModelEntity));
			}
		}
		return itemModelList;
	}

	@Override
	public List<ItemModel> findReferenceItemsByFormId(@RequestParam(name="formModelId", required = true) String formModelId,
													  @RequestParam(name="referenceFormModelId", required = true) String referenceFormModelId) {
		List<ReferenceItemModelEntity> itemModelEntities = itemModelService.findRefenceItemByFormModelId(formModelId);
		List<ItemModel> list = new ArrayList<>();
		FormModelEntity formModelEntity = formModelService.get(referenceFormModelId);
		if(formModelEntity == null){
			throw new IFormException("未找到【"+formModelId+"】对应的表单建模");
		}
		for(ReferenceItemModelEntity entity : itemModelEntities){
			if(entity.getFormModel() != null && !entity.getFormModel().getId().equals(referenceFormModelId)){
				continue;
			}else if(StringUtils.hasText(entity.getSourceFormModelId()) && !referenceFormModelId.equals(entity.getSourceFormModelId())){
				continue;
			}else if(entity.getColumnModel() != null && !formModelEntity.getDataModels().get(0).getId().equals(entity.getColumnModel().getDataModel().getId())){
				continue;
			}
			ItemModel itemModel = new ItemModel();
			BeanUtils.copyProperties(entity, itemModel, new String[]{"formModel", "columnModel", "activities", "options", "permissions", "items", "parentItem", "referenceList"});
			list.add(itemModel);
		}
		return list;
	}

	private ItemModel convertItemModelByEntity(ItemModelEntity itemModelEntity){
		ItemModel itemModel = new ItemModel();
		BeanUtils.copyProperties(itemModelEntity, itemModel, new String[]{"formModel", "columnModel", "activities", "options", "permissions","items","parentItem","referenceList"});
		if(itemModelEntity.getColumnModel() != null){
			ColumnModelInfo columnModel = new ColumnModelInfo();
			BeanUtils.copyProperties(itemModelEntity.getColumnModel(), columnModel, new String[]{"dataModel", "columnReferences"});
			columnModel.setItemId(itemModel.getId());
			columnModel.setItemName(itemModel.getName());
			if(itemModelEntity.getColumnModel().getDataModel() != null){
				DataModel dataModel = new DataModel();
				BeanUtils.copyProperties(itemModelEntity.getColumnModel().getDataModel(), dataModel, new String[]{"masterModel","slaverModels","columns","indexes","referencesDataModel"});
				columnModel.setDataModel(dataModel);
			}
			itemModel.setColumnModel(columnModel);
		}
		return itemModel;
	}

	private void verifyFormModelName(FormModel formModel){
		if(formModel == null || StringUtils.isEmpty(formModel.getName())){
			return;
		}
		if(StringUtils.isEmpty(formModel.getApplicationId())){
			throw new IFormException("表单未关联应用");
		}
		List<FormModelEntity> list  = formModelService.query().filterEqual("name", formModel.getName()).filterEqual("applicationId", formModel.getApplicationId()).list();
		if(list == null || list.size() < 1){
			return;
		}
		if(list.size() > 0 && formModel.isNew()){
			throw new IFormException("表单名称重复了");
		}
		List<String> idList = list.parallelStream().map(FormModelEntity::getId).collect(Collectors.toList());
		if(!formModel.isNew() && !idList.contains(formModel.getId())) {
			throw new IFormException("表单名称重复了");
		}
	}

	//校验表单建模
	private void veryFormModel(FormModel formModel){
		if(!formModel.isNew()){
			FormModelEntity formModelEntity = formModelService.get(formModel.getId());
			if(formModelEntity == null){
				throw new IFormException("未找到【"+formModel.getId()+"】对应的表单模型");
			}
			if(formModelEntity.getItems() != null){
				List<String> oldItemIds = formModelEntity.getItems().parallelStream().map(ItemModelEntity::getId).collect(Collectors.toList());
				List<String> newItemIds = new ArrayList<>();
				for(ItemModel itemModel : formModel.getItems()){
					if(!itemModel.isNew()) {
						newItemIds.add(itemModel.getId());
					}
				}
				oldItemIds.removeAll(newItemIds);
				if(oldItemIds == null || oldItemIds.size() < 1){
					return;
				}
			}
		}
	}

	private FormModelEntity wrap(FormModel formModel) {
		veryFormModel(formModel);
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks","functions"});

		verifyFormModelName(formModel);

		List<DataModel> dataModels = formModel.getDataModels();
		if(dataModels == null || dataModels.isEmpty()){
			throw new IFormException("请先关联数据模型");
		}
		//TODO 获取主数据模型
		DataModel masterDataModel = null;
		for(DataModel dataModel : dataModels){
			if(dataModel.getMasterModel() == null){
				masterDataModel = dataModel;
				break;
			}
		}
		Map<String, Object> dataMap = new HashMap<>();
		for(DataModel dataModel : formModel.getDataModels()){
			if(dataMap.get(dataModel.getTableName()) != null){
				throw new IFormException("存在相同数据建模");
			}
			dataMap.put(dataModel.getTableName(), System.currentTimeMillis());
			if(!StringUtils.hasText(dataModel.getTableName())){
				throw new IFormException("表名不允许为空");
			}
			if(!isLetterDigit(dataModel.getTableName())){
				throw new IFormException("表名只允许包含字母、数字和下划线连接符");
			}
			List<String> columnModelEntities = dataModel.getColumns().parallelStream().map(ColumnModel::getColumnName).collect(Collectors.toList());
			Map<String, Object> map = new HashMap<String, Object>();
			for(String string : columnModelEntities){
				if(map.containsKey(string)){
					throw new IFormException("字段重复了");
				}
				map.put(string, string);
			}
		}

		if(masterDataModel == null || masterDataModel.getId() == null){
			throw new IFormException("未找到列表对应的数据建模");
		}

		//主表的数据建模
		DataModelEntity masterDataModelEntity = dataModelService.find(masterDataModel.getId());

		//旧的子数据建模
		Map<String, DataModelEntity> oldMasterDataModelMap = new HashMap<>();
		for(DataModelEntity dataModelEntity : masterDataModelEntity.getSlaverModels()){
			oldMasterDataModelMap.put(dataModelEntity.getId(), dataModelEntity);
		}


		List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();
		Map<String, ItemModelEntity> map = new HashMap<>();
		List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
		//为了设置关联
		Map<String, List<ItemModelEntity>> formMap = new HashMap<>();
		for (ItemModel itemModel : formModel.getItems()) {
			ItemModelEntity itemModelEntity = wrap(formModel.getId(), itemModel, map);
			itemModelEntity.setFormModel(entity);
			items.add(itemModelEntity);
			itemModelEntityList.add(itemModelEntity);
			if(itemModelEntity instanceof TabsItemModelEntity){
			    for(TabPaneItemModelEntity tabPaneItemModelEntity : ((TabsItemModelEntity)itemModelEntity).getItems()) {
			        for(ItemModelEntity itemModelEntity1 : tabPaneItemModelEntity.getItems()) {
			            if(itemModelEntity1 instanceof  SubFormItemModelEntity) {
                            formMap.put(((SubFormItemModelEntity) itemModelEntity1).getTableName(), formModelService.getChildRenItemModelEntity(itemModelEntity1));
                        }else{
                            itemModelEntityList.addAll(formModelService.getChildRenItemModelEntity(itemModelEntity1));
                        }
                    }
                }
			}else if(!(itemModelEntity instanceof SubFormItemModelEntity) ){
                itemModelEntityList.addAll(formModelService.getChildRenItemModelEntity(itemModelEntity));
            } else{
                formMap.put(((SubFormItemModelEntity) itemModelEntity).getTableName(), formModelService.getChildRenItemModelEntity(itemModelEntity));
            }
		}
		formMap.put(masterDataModel.getTableName(), itemModelEntityList);

		Map<String, DataModel> dataModelMap = new HashMap<>();
		for(DataModel dataModel : dataModels){
			dataModelMap.put(dataModel.getTableName(), dataModel);
		}

		for(String key : formMap.keySet()){
			setReference(dataModelMap.get(key), formMap.get(key));
		}


		//是否需要关联字段
		boolean needMasterId = false;
		if(masterDataModelEntity.getMasterModel() != null) {
			needMasterId = true;
		}
		//设置数据模型行
		setDataModelEntityColumns(masterDataModel, masterDataModelEntity, needMasterId);

		BeanUtils.copyProperties(masterDataModel, masterDataModelEntity, new String[]{"masterModel", "slaverModels", "columns", "indexes" });

		//创建获取主键未持久化到数据库
		ColumnModelEntity masterIdColumnEntity = columnModelService.saveColumnModelEntity(masterDataModelEntity, "id");
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "create_at");
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "update_at");
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "create_by");
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "update_by");

		//创建获取关联字段未持久化到数据库
		setMasterIdColumnEntity(masterDataModelEntity);

		if(masterDataModelEntity.getModelType() == DataModelType.Single && dataModels.size() > 1) {
			masterDataModelEntity.setModelType(DataModelType.Master);
		}
		masterDataModelEntity.setSynchronized(false);

		List<DataModelEntity> slaverDataModelEntities = new ArrayList<>();
		for(DataModel dataModel : dataModels) {
			if (dataModel.getMasterModel() == null) {
				continue;
			}
			//创建关联字段
			DataModelEntity slaverDataModelEntity = dataModel.isNew() ? new DataModelEntity() :  oldMasterDataModelMap.remove(dataModel.getId());

			slaverDataModelEntity.setModelType(DataModelType.Slaver);
			slaverDataModelEntity.setSynchronized(false);
			slaverDataModelEntity.setMasterModel(masterDataModelEntity);
			BeanUtils.copyProperties(dataModel, slaverDataModelEntity, new String[]{"masterModel","slaverModels","columns","indexes"});

			//设置数据模型行
			setDataModelEntityColumns(dataModel, slaverDataModelEntity, true);

			//获取主键未持久化到数据库
			columnModelService.saveColumnModelEntity(slaverDataModelEntity, "id");
			columnModelService.saveColumnModelEntity(slaverDataModelEntity, "create_at");
			columnModelService.saveColumnModelEntity(slaverDataModelEntity, "update_at");
			columnModelService.saveColumnModelEntity(slaverDataModelEntity, "create_by");
			columnModelService.saveColumnModelEntity(slaverDataModelEntity, "update_by");
			//子表不需要关联
			setMasterIdColunm(slaverDataModelEntity, masterIdColumnEntity);

			slaverDataModelEntities.add(slaverDataModelEntity);
		}
		masterDataModelEntity.setSlaverModels(slaverDataModelEntities);

		//设置数据模型结构了
		List<DataModelEntity> dataModelEntities = new ArrayList<>();
		dataModelEntities.add(masterDataModelEntity);

		entity.setDataModels(dataModelEntities);


		entity.setItems(items);

		//设置控件权限
		if(formModel.getPermissions() != null && formModel.getPermissions().size() > 0) {
			List<ItemPermissionModel> itemPermissionModels = formModel.getPermissions();
			for(int i = 0 ;i < itemPermissionModels.size() ; i++) {
				ItemPermissionModel itemPermissionModel = itemPermissionModels.get(i);
				setItemPermissions(itemPermissionModel, map);
			}
		}

		if(formModel.getFunctions() != null && formModel.getFunctions().size() > 0){
			wrapFormFunctions(entity, formModel);
		}

		if(formModel.getSubmitChecks() != null && formModel.getSubmitChecks().size() > 0){
			wrapFormModelSubmitCheck(entity, formModel);
		}

		for(String key : oldMasterDataModelMap.keySet()){
			dataModelService.deleteById(key);
		}

		//数据标识对应的字段
		if(formModel.getItemModelList() != null && formModel.getItemModelList().size() > 0) {
			List<String> list = new ArrayList<>();
			for(ItemModel itemModel1 : formModel.getItemModelList()) {
				list.add(itemModel1.getTableName()+"_"+itemModel1.getColumnName());
			}
			entity.setItemTableColunmName(String.join(",", list));
		}else{
			entity.setItemTableColunmName(null);
		}

		//二维码数据标识对应的字段
		if(formModel.getQrCodeItemModelList() != null && formModel.getQrCodeItemModelList().size() > 0) {
			List<String> list = new ArrayList<>();
			for(ItemModel itemModel1 : formModel.getQrCodeItemModelList()) {
				list.add(itemModel1.getTableName()+"_"+itemModel1.getColumnName());
			}
			entity.setQrCodeItemTableColunmName(String.join(",", list));
		}else{
			entity.setQrCodeItemTableColunmName(null);
		}
		//保存数据模型
		dataModelService.save(masterDataModelEntity);

		return entity;
	}

	/**
	 * 判断字符串中是否字母、数字和连接符
	 * @param str
	 * 待校验字符串
	 * @warn 能校验是否为中文标点符号
	 */
    public static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z\\-\\_]+$";
        return str.matches(regex);
    }

	//设置关联关系
	private void setReference(DataModel dataModel, List<ItemModelEntity> itemModelEntityList){
		for(ItemModelEntity itemModelEntity : itemModelEntityList){
			if(itemModelEntity instanceof ReferenceItemModelEntity ){
				if(itemModelEntity.getType() == ItemType.ReferenceLabel || ((ReferenceItemModelEntity) itemModelEntity).getSelectMode() == SelectMode.Inverse){
					continue;
				}
				String key = "id";
				if(((ReferenceItemModelEntity) itemModelEntity).getSelectMode() != SelectMode.Multiple){
					if(itemModelEntity.getColumnModel() == null){
						continue;
					}
					key = itemModelEntity.getColumnModel().getColumnName();
				}
				ColumnModel referenceColumnModel = null;
				if(dataModel.isNew()){
					ColumnModel columnModel = new ColumnModel();
					BeanUtils.copyProperties(columnModelService.saveColumnModelEntity(new DataModelEntity(), "id"), columnModel, new String[]{"dataModel","columnReferences"});
					dataModel.getColumns().add(columnModel);
				}
				for(ColumnModel columnModel : dataModel.getColumns()){
					if(columnModel.getColumnName().equals(key)){
						referenceColumnModel = columnModel;
						break;
					}
				}
				FormModelEntity formModelEntity = formModelService.get(((ReferenceItemModelEntity) itemModelEntity).getReferenceFormId());
				if(referenceColumnModel != null){
					List<String> refenceTables = referenceColumnModel.getReferenceTables().parallelStream().map(ReferenceModel::getReferenceTable).collect(Collectors.toList());
					if(!refenceTables.contains(formModelEntity.getDataModels().get(0).getTableName())){
						ReferenceModel referenceModel = new ReferenceModel();
						referenceModel.setReferenceType(((ReferenceItemModelEntity) itemModelEntity).getReferenceType());
						referenceModel.setReferenceTable(formModelEntity.getDataModels().get(0).getTableName());
						referenceColumnModel.getReferenceTables().add(referenceModel);
					}
				}
			}
		}
	}

	//提交表单提交校验
	private void wrapFormModelSubmitCheck(FormModelEntity entity, FormModel formModel) {
		if(formModel.getSubmitChecks() != null){
			List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
			for(FormSubmitCheckModel model : formModel.getSubmitChecks()){
				FormSubmitCheckInfo checkInfo =  new FormSubmitCheckInfo();
				BeanUtils.copyProperties(model, checkInfo, new String[]{"formModel"});
				checkInfo.setFormModel(entity);
				checkInfos.add(checkInfo);
			}
			List<FormSubmitCheckInfo> checkInfoList = checkInfos.size() < 2 ? checkInfos : checkInfos.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			entity.setSubmitChecks(checkInfoList);
		}
	}

	private FormModelEntity wrapSubmitCheck(FormModel formModel) {
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks","functions"});

		if(formModel.getSubmitChecks() != null){
			List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
			for(FormSubmitCheckModel model : formModel.getSubmitChecks()){
				FormSubmitCheckInfo checkInfo =  new FormSubmitCheckInfo();
				BeanUtils.copyProperties(model, checkInfo, new String[]{"formModel"});
				checkInfo.setFormModel(entity);
				checkInfos.add(checkInfo);
			}
			List<FormSubmitCheckInfo> checkInfoList = checkInfos.size() < 2 ? checkInfos : checkInfos.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			entity.setSubmitChecks(checkInfoList);
		}
		return entity;
	}

	//设置表单功能
	private void wrapFormFunctions(FormModelEntity entity, FormModel formModel) {
		if(formModel.getSubmitChecks() != null){
			List<ListFunction> functions = new ArrayList<>();
			for(FunctionModel model : formModel.getFunctions()){
				ListFunction function =  new ListFunction();
				BeanUtils.copyProperties(model, function, new String[]{"formModel"});
				function.setFormModel(entity);
				functions.add(function);
			}
			List<ListFunction> functionList = functions.size() < 2 ? functions : functions.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			entity.setFunctions(functionList);
		}
	}

	//设计子表关联字段
	private void setMasterIdColumnEntity(DataModelEntity dataModelEntity){
		if(dataModelEntity.getMasterModel() == null || dataModelEntity.getMasterModel().getId() == null) {
			return;
		}
		DataModelEntity modelEntity = dataModelService.get(dataModelEntity.getMasterModel().getId());
		if(modelEntity == null){
			return;
		}
		List<ColumnModelEntity> columnModelEntities = modelEntity.getColumns();
		ColumnModelEntity parentIdColumnEntity = null;
		for(ColumnModelEntity columnModelEntity : columnModelEntities){
			if(columnModelEntity.getColumnName().equals("id")){
				parentIdColumnEntity = columnModelEntity;
				break;
			}
		}
		if(parentIdColumnEntity != null){
			setMasterIdColunm(dataModelEntity, parentIdColumnEntity);
		}
	}
	private void setMasterIdColunm(DataModelEntity dataModelEntity, ColumnModelEntity parentIdColumnEntity){
		//创建一个关联字段
		//ColumnModelEntity masterIdColumn = columnModelService.saveColumnModelEntity(dataModelEntity, "master_id");
		//关联关系未持久化到数据库
		//columnModelService.saveColumnReferenceEntity(parentIdColumnEntity, masterIdColumn, ReferenceType.OneToMany);
	}

	private void veryTableName(DataModelEntity oldDataModelEntity){
		List<DataModelEntity> list = dataModelService.findByProperty("tableName", oldDataModelEntity.getTableName());
		List<String> dataList = list.parallelStream().map(DataModelEntity::getId).collect(Collectors.toList());
		if(dataList != null && !dataList.isEmpty() && (dataList.size() > 1 || !dataList.get(0).equals(oldDataModelEntity.getId()))){
			throw new IFormException("表名重复了");
		}
	}

	//设置数据模型行
	private void setDataModelEntityColumns(DataModel newDataModel, DataModelEntity oldDataModelEntity, boolean needMasterId){

		veryTableName(oldDataModelEntity);

		List<String> newColumns = newDataModel.getColumns().parallelStream().map(ColumnModel::getColumnName).collect(Collectors.toList());


		//待更新的行
		List<String> newColumnIds = new ArrayList<>();
		ColumnModel idColumnModel = null;
		ColumnModel masterIdColumnModel= null;
		for(int i = 0 ; i < newDataModel.getColumns().size() ; i++){
			ColumnModel newColumnModel = newDataModel.getColumns().get(i);
			if(newColumnModel.getId() != null){
				newColumnIds.add(newColumnModel.getId());
			}
			if(newColumnModel.getColumnName().equals("id")){
				idColumnModel = newColumnModel;
			}
			if(newColumnModel.getColumnName().equals("master_id")){
				masterIdColumnModel = newColumnModel;
			}
		}

		if(idColumnModel != null){
			deleteReferenceModel( oldDataModelEntity,  newColumns,  idColumnModel);
		}

		//待保存的行
		List<ColumnModelEntity> saveModelEntities = new ArrayList<ColumnModelEntity>();

		if(idColumnModel == null){
			saveModelEntities.add(columnModelService.saveColumnModelEntity(oldDataModelEntity,"id"));
		}
		if(masterIdColumnModel == null && needMasterId){
			//创建获取关联字段未持久化到数据库
			saveModelEntities.add(columnModelService.saveColumnModelEntity(oldDataModelEntity, "master_id"));
		}

		//待删除的行
		for(int i = 0 ; i <  oldDataModelEntity.getColumns().size() ; i++ ){
			ColumnModelEntity columnModelEntity = oldDataModelEntity.getColumns().get(i);
			if(!newColumnIds.contains(columnModelEntity.getId())){
				List<ColumnReferenceEntity> referenceEntityList = columnModelEntity.getColumnReferences();
				for(int m = 0 ; m < referenceEntityList.size(); m++ ){
					ColumnReferenceEntity referenceEntity = referenceEntityList.get(m);
					List<ColumnReferenceEntity> toReferenceEntityList = referenceEntity.getToColumn().getColumnReferences();
					for(int j = 0 ; j < toReferenceEntityList.size(); j++){
						ColumnReferenceEntity columnReferenceEntity = toReferenceEntityList.get(j);
						if(columnReferenceEntity.getToColumn().getId().equals(columnModelEntity.getId())){
							if(columnReferenceEntity.getReferenceType() == ReferenceType.ManyToMany){
								columnModelService.deleteTable("if_"+columnReferenceEntity.getReferenceMiddleTableName()+"_list");
							}
							toReferenceEntityList.remove(columnReferenceEntity);
							j--;
							columnModelService.deleteColumnReferenceEntity(columnReferenceEntity);
							columnModelService.save(referenceEntity.getToColumn());
						}
					}
					referenceEntityList.remove(referenceEntity);
					m--;
					columnModelService.deleteColumnReferenceEntity(referenceEntity);
				}
				//columnModelService.save(columnModelEntity);

				if((needMasterId && "master_id".equals(columnModelEntity.getColumnName())) || "id".equals(columnModelEntity.getColumnName())){
					continue;
				}

				oldDataModelEntity.getColumns().remove(columnModelEntity);
				i--;
				List<ItemModelEntity> itemModelEntity = itemModelService.findByProperty("columnModel.id", columnModelEntity.getId());
				if(itemModelEntity != null) {
					for(ItemModelEntity itemModel : itemModelEntity) {
						itemModel.setColumnModel(null);
						itemModelService.save(itemModel);
					}
				}
				//删除数据库字段
				columnModelService.deleteTableColumn(newDataModel.getTableName(), columnModelEntity.getColumnName());
				columnModelService.delete(columnModelEntity);
			}
		}
		Map<String, Object> map = new HashMap<>();
		for(ColumnModel columnModel : newDataModel.getColumns()){
			ColumnModelEntity oldColumnModelEntity = setColumn(columnModel);
			List<ColumnReferenceEntity> oldColumnReferences = oldColumnModelEntity.getColumnReferences();
			for(int i = 0 ; i < oldColumnReferences.size() ; i++) {
				ColumnReferenceEntity columnReferenceEntity = oldColumnReferences.get(i);
				oldColumnReferences.remove(columnReferenceEntity);
				i--;
				columnModelService.deleteColumnReferenceEntity(columnReferenceEntity);
			}
			oldColumnModelEntity.setColumnReferences(new ArrayList<ColumnReferenceEntity>());
			oldColumnModelEntity.setDataModel(oldDataModelEntity);
			if(columnModel.getReferenceTables() != null){
				for(ReferenceModel columnReferenceEntity : columnModel.getReferenceTables()){
					if(map.get(columnModel.getColumnName()+"_"+columnReferenceEntity.getReferenceTable()+"_"+columnReferenceEntity.getReferenceValueColumn()) != null){
						continue;
					}else{
						 map.put(columnModel.getColumnName()+"_"+columnReferenceEntity.getReferenceTable()+"_"+columnReferenceEntity.getReferenceValueColumn(), System.currentTimeMillis());
					}

					DataModelEntity dataModelEntity = dataModelService.findUniqueByProperty("tableName", columnReferenceEntity.getReferenceTable());
					if(dataModelEntity == null){
						throw new IFormException("未找到【"+columnReferenceEntity.getReferenceTable()+"】对应的数据表");
					}
					ColumnModelEntity columnModelEntity = columnModelService.saveColumnModelEntity(dataModelEntity, columnReferenceEntity.getReferenceValueColumn());
					if(columnModelEntity == null){
						throw new IFormException("未找到【"+columnReferenceEntity.getReferenceValueColumn()+"】对应的字段");
					}
					ColumnModel referenceColumnModel = new ColumnModel();
					referenceColumnModel.setId(columnModelEntity.getId());
					columnModelService.saveColumnReferenceEntity(oldColumnModelEntity, setColumn(referenceColumnModel), columnReferenceEntity.getReferenceType(), columnReferenceEntity.getReferenceMiddleTableName());
				}
			}
			saveModelEntities.add(oldColumnModelEntity);
		}

		oldDataModelEntity.setColumns(saveModelEntities);
	}

	//删除主表id关联
	private void deleteReferenceModel(DataModelEntity oldDataModelEntity, List<String> newColumns, ColumnModel idColumnModel){
		List<ColumnReferenceEntity> list = columnModelService.saveColumnModelEntity(oldDataModelEntity,"id").getColumnReferences();
		List<String> deleteIds = new ArrayList<>();
		List<String> stringList = new ArrayList<>();
		for(int j = 0 ; j < list.size() ; j++){
			ColumnReferenceEntity columnReferenceEntity = list.get(j);
			if(!newColumns.contains(columnReferenceEntity.getFromColumn().getColumnName()) && columnReferenceEntity.getFromColumn().getDataModel().getId().equals(oldDataModelEntity.getId())){
				//删除自己字段
				deleteIds.add(columnReferenceEntity.getId());
				stringList.add(columnReferenceEntity.getToColumn().getId()+"_"+columnReferenceEntity.getFromColumn().getId());
			}
		}
		for(int j = 0 ; j < list.size() ; j++){
			ColumnReferenceEntity columnReferenceEntity = list.get(j);
			if(stringList.contains(columnReferenceEntity.getFromColumn().getId()+"_"+columnReferenceEntity.getToColumn().getId())){
				//删除自己字段
				deleteIds.add(columnReferenceEntity.getId());
			}
		}

		for(int j = 0 ; j < idColumnModel.getReferenceTables().size() ; j++){
			ReferenceModel referenceModel = idColumnModel.getReferenceTables().get(j);
			if(deleteIds.contains(referenceModel.getId())){
				idColumnModel.getReferenceTables().remove(j);
				j--;
			}
		}
	}

	private ColumnModelEntity setColumn(ColumnModel columnModel){
		ColumnModelEntity oldColumnModelEntity = new ColumnModelEntity();
		if(!columnModel.isNew()) {
			 oldColumnModelEntity = columnModelService.get(columnModel.getId());
		}
		BeanUtils.copyProperties(columnModel, oldColumnModelEntity, new String[]{"dataModel", "columnReferences","referenceTables"});
		if(columnModel.getDataModel() != null){
			DataModelEntity dataModelEntity = new DataModelEntity();
			if(!columnModel.getDataModel().isNew()) {
				dataModelEntity = dataModelService.get(columnModel.getDataModel().getId());
			}
			BeanUtils.copyProperties(columnModel.getDataModel(), dataModelEntity, new String[]{"masterModel", "slaverModels", "columns", "indexes" });
			oldColumnModelEntity.setDataModel(dataModelEntity);
		}
		return oldColumnModelEntity;
	}

	private List<ColumnModelEntity>  saveColumnModelEntity(DataModelEntity dataModelEntity, DataModel dataModel){
		List<ColumnModelEntity> columnlist = new ArrayList<>();
		for(ColumnModel columnModel : dataModel.getColumns()){
			if("id".equals(columnModel.getColumnName()) || "master_id".equals(columnModel.getColumnName())){
				continue;
			}
			ColumnModelEntity columnModelEntity = new ColumnModelEntity();
			if (!columnModel.isNew()) {
				columnModelEntity = columnModelService.find(columnModel.getId());
			}
			BeanUtils.copyProperties(columnModel, columnModelEntity, new String[]{"dataModel","slaverModels","columns","indexes"});
			columnModelEntity.setDataModel(dataModelEntity);
			columnlist.add(columnModelEntity);
		}
		return columnlist;
	}


	//获取主的数据模型
	private DataModelEntity getMasterDataModelEntity(List<DataModelEntity> dataModelEntities){
		for(DataModelEntity modelEntity : dataModelEntities){
			if(modelEntity.getModelType() == DataModelType.Master || modelEntity.getModelType() == DataModelType.Single){
				return modelEntity;
			}
		}
		return null;
	}

	private ItemModelEntity wrap(String sourceFormModelId, ItemModel itemModel, Map<String, ItemModelEntity> map) {
		if(itemModel.getType() == ItemType.ReferenceLabel){
			itemModel.setSelectMode(SelectMode.Attribute);
		}
		//TODO 根据类型映射对应的item
		ItemModelEntity entity = formModelService.getItemModelEntity(itemModel.getType());
		if(itemModel.getSystemItemType() == SystemItemType.SerialNumber){
			 entity = new SerialNumberItemModelEntity();
		}else if(itemModel.getSystemItemType() == SystemItemType.Creator){
			entity = new CreatorItemModelEntity();
		}else if(itemModel.getSystemItemType() == SystemItemType.CreateDate){
			entity = new TimeItemModelEntity();
		}
		//需要保持column
		BeanUtils.copyProperties(itemModel, entity, new String[] {"defaultValue","referenceList","parentItem", "searchItems","sortItems", "permissions", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});

		//设置控件字段
		setColumnModel(entity, itemModel);

		if(entity.getColumnModel() != null){
			map.put(entity.getColumnModel().getDataModel().getTableName()+"_"+entity.getColumnModel().getColumnName(), entity);
		}

		if(entity instanceof ReferenceItemModelEntity){
			((ReferenceItemModelEntity) entity).setSourceFormModelId(sourceFormModelId);
			if(StringUtils.hasText(itemModel.getUuid())){
				ItemModelEntity itemModelEntity = itemModelService.findUniqueByProperty("uuid", itemModel.getUuid());
				if(itemModelEntity != null && !itemModelEntity.getId().equals(itemModel.getId())){
					throw  new IFormException("关联控件【"+itemModel.getName()+"】UUID重复了");
				}
			}

			if(itemModel.getType() != ItemType.ReferenceLabel && (!StringUtils.hasText(itemModel.getReferenceFormId())
					|| itemModel.getReferenceList() == null || itemModel.getReferenceList().getId() == null)){
				throw  new IFormException("关联控件【"+itemModel.getName()+"】未找到关联表单或列表模型");
			}

			if(itemModel.getType() == ItemType.ReferenceLabel  &&
					!StringUtils.hasText(itemModel.getItemTableName()) && !StringUtils.hasText(itemModel.getItemColunmName())){
				throw  new IFormException("关联属性控件【"+itemModel.getName()+"】未找到关联控件");
			}

			if(itemModel.getType() == ItemType.ReferenceLabel){
				itemModel.setParentItem(null);
			}

			if(itemModel.getParentItem() != null) {
				ItemModel parentItemModel = itemModel.getParentItem();
				parentItemModel.setType(ItemType.ReferenceList);
				((ReferenceItemModelEntity) entity).setParentItem((ReferenceItemModelEntity) getParentItemModel(parentItemModel));
			}
			if(itemModel.getItemModelList() != null && itemModel.getItemModelList().size() > 0) {
				List<String> list = new ArrayList<>();
				for(ItemModel itemModel1 : itemModel.getItemModelList()) {
					list.add(itemModel1.getTableName()+"_"+itemModel1.getColumnName());
				}
				((ReferenceItemModelEntity) entity).setItemTableColunmName(String.join(",", list));
			}else{
				((ReferenceItemModelEntity) entity).setItemTableColunmName(null);
			}
			if(itemModel.getType() == ItemType.ReferenceLabel){
				if(StringUtils.hasText(itemModel.getItemTableName())) {
					((ReferenceItemModelEntity) entity).setItemTableColunmName(itemModel.getItemTableName() + "_" + itemModel.getItemColunmName());
				}else{
					((ReferenceItemModelEntity) entity).setItemTableColunmName(null);
				}
			}
			((ReferenceItemModelEntity) entity).setReferenceList(setItemModelByListModel(itemModel));
		}else if(entity instanceof SelectItemModelEntity){
			SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)entity;
			if(itemModel.getDefaultValue() != null && itemModel.getDefaultValue() instanceof List){
				selectItemModelEntity.setDefaultReferenceValue(String.join(",",(List)itemModel.getDefaultValue()));
			}else{
				selectItemModelEntity.setDefaultReferenceValue((String)itemModel.getDefaultValue());
			}
			if(itemModel.getMultiple() != null && itemModel.getMultiple() && itemModel.getOptions() != null && itemModel.getOptions().size() > 0 ){
				int i = 0;
				for(Option option: itemModel.getOptions()){
					if(option.getDefaultFlag() != null && option.getDefaultFlag()){
						i++;
					}
				}
				if(i > 1){
					throw new IFormException("控件"+itemModel.getName()+"为单选下拉框，不能默认多个值");
				}
			}
			selectItemModelEntity.setReferenceList(setItemModelByListModel(itemModel));
			if(itemModel.getDictionaryValueType() == DictionaryValueType.Linkage && (itemModel.getReferenceDictionaryId() == null || itemModel.getParentItem() == null)){
				throw new IFormException("控件"+itemModel.getName()+"未找到对应分类或联动目标");
			}else if(itemModel.getDictionaryValueType() == DictionaryValueType.Fixed && (itemModel.getOptions() == null || itemModel.getOptions().size() < 1)
					&& (itemModel.getReferenceDictionaryId() == null || itemModel.getReferenceDictionaryItemId() == null)){
				throw new IFormException("控件"+itemModel.getName()+"未找到对应分类或自定义值");
			}

            if(itemModel.getDictionaryValueType() == DictionaryValueType.Linkage && itemModel.getParentItem() != null){
				ItemModel parentItemModel = itemModel.getParentItem();
				parentItemModel.setType(ItemType.Select);
                selectItemModelEntity.setParentItem((SelectItemModelEntity) getParentItemModel(parentItemModel));
            }

		}else if(entity instanceof RowItemModelEntity){
			List<ItemModelEntity> rowList = new ArrayList<>() ;
			for(ItemModel rowItemModel : itemModel.getItems()) {
				rowList.add(wrap(sourceFormModelId, rowItemModel, map));
			}
			((RowItemModelEntity) entity).setItems(rowList);
		}else if(entity instanceof SubFormItemModelEntity){
			List<ItemModel> rowItemModels = itemModel.getItems();
			List<SubFormRowItemModelEntity> rowItemModelEntities = new ArrayList<>();
			for(ItemModel rowItemModelEntity : rowItemModels) {
				SubFormRowItemModelEntity subFormRowItemModelEntity = new SubFormRowItemModelEntity();
				BeanUtils.copyProperties(rowItemModelEntity, subFormRowItemModelEntity, new String[] {"searchItems","sortItems","items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
				List<ItemModelEntity> rowItemList = new ArrayList<>();
				for(ItemModel childrenItem : rowItemModelEntity.getItems()) {
					rowItemList.add(wrap(sourceFormModelId, childrenItem, map));
				}
				subFormRowItemModelEntity.setItems(rowItemList);
				rowItemModelEntities.add(subFormRowItemModelEntity);
			}
			((SubFormItemModelEntity) entity).setItems(rowItemModelEntities);
		}else if(entity instanceof TabsItemModelEntity){
			List<ItemModel> tabsItemModels = itemModel.getItems();
			List<TabPaneItemModelEntity> list = new ArrayList<>();
			if(tabsItemModels != null) {
				for (ItemModel itemModel1 : tabsItemModels){
					TabPaneItemModelEntity tabPaneItemModelEntity = new TabPaneItemModelEntity();
					BeanUtils.copyProperties(itemModel1, tabPaneItemModelEntity, new String[] {"searchItems","sortItems","items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
					List<ItemModelEntity> rowItemList = new ArrayList<>();
					if(itemModel1.getItems() != null) {
						for (ItemModel childrenItem : itemModel1.getItems()) {
							rowItemList.add(wrap(sourceFormModelId, childrenItem, map));
						}
					}
					tabPaneItemModelEntity.setParentItem((TabsItemModelEntity)entity);
					tabPaneItemModelEntity.setItems(rowItemList);
					list.add(tabPaneItemModelEntity);
				}
				((TabsItemModelEntity) entity).setItems(list);
			}
		} else if (entity instanceof TreeSelectItemModelEntity) {

			if (itemModel.getDefaultValue()!=null && itemModel.getDefaultValue() instanceof List) {
				((TreeSelectItemModelEntity) entity).setDefaultValue(String.join(",", (List)itemModel.getDefaultValue()));
			}else if (itemModel.getDefaultValue()!=null) {
                ((TreeSelectItemModelEntity) entity).setDefaultValue(itemModel.getDefaultValue().toString());
            }else{
                ((TreeSelectItemModelEntity) entity).setDefaultValue(null);
            }
			if (itemModel.getDefaultValueName()!=null && itemModel.getDefaultValueName() instanceof List ) {
                ((TreeSelectItemModelEntity) entity).setDefaultValueName(String.join(",", (List)itemModel.getDefaultValueName()));
			}else if (itemModel.getDefaultValueName()!=null) {
                ((TreeSelectItemModelEntity) entity).setDefaultValueName((String)itemModel.getDefaultValueName());
            }else{
                ((TreeSelectItemModelEntity) entity).setDefaultValueName(null);
            }
		}

		List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();
		if (itemModel.getActivities() != null) {
			for (ActivityInfo activityInfo : itemModel.getActivities()) {
				ItemActivityInfo activityInfoEntity = wrap(activityInfo);
				activityInfoEntity.setItemModel(entity);
				activities.add(activityInfoEntity);
			}
			entity.setActivities(activities);
		}

		if (itemModel.getOptions() != null) {
			List<ItemSelectOption> options = new ArrayList<>();
			for (Option option : itemModel.getOptions()) {
				ItemSelectOption itemSelectOption = new ItemSelectOption();
				BeanUtils.copyProperties(option, itemSelectOption, new String[]{"itemModel"});
				itemSelectOption.setItemModel(entity);
				options.add(itemSelectOption);
			}
			entity.setOptions(options);
		}

		if(itemModel.getColumnModel() != null && itemModel.getColumnModel().getColumnName() != null && itemModel.getColumnModel().getTableName() != null) {
			List<ColumnModelEntity> columnModelEntities = columnModelService.query().filterEqual("columnName", itemModel.getColumnModel().getColumnName()).filterEqual("dataModel.tableName", itemModel.getColumnModel().getTableName()).list();
			if(columnModelEntities != null && columnModelEntities.size() > 0) {
				ColumnModelEntity columnModelEntity = columnModelEntities.get(0);
				List<ColumnType> list = SystemItemType.getColumnType(itemModel.getSystemItemType());
				if(columnModelEntity.getDataType() != null && !list.contains(columnModelEntity.getDataType())){
					throw new IFormException("控件"+itemModel.getName()+"关联字段"+columnModelEntity.getColumnName()+"类型不符合");
				}
			}
		}
		return entity;
	}

	private ItemModelEntity getParentItemModel(ItemModel itemModel){
		ItemModelEntity parentItemModel = formModelService.getItemModelEntity(itemModel.getType());
		if(itemModel.getSystemItemType() == SystemItemType.SerialNumber){
			parentItemModel = new SerialNumberItemModelEntity();
		}else if(itemModel.getSystemItemType() == SystemItemType.Creator){
			parentItemModel = new CreatorItemModelEntity();
		}else if(itemModel.getSystemItemType() == SystemItemType.CreateDate){
			parentItemModel = new TimeItemModelEntity();
		}
		BeanUtils.copyProperties(itemModel, parentItemModel, new String[] {"referenceList","parentItem", "searchItems","sortItems", "permissions", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
		ColumnModelEntity columnModel = new ColumnModelEntity();
		columnModel.setColumnName(itemModel.getColumnName());
		DataModelEntity dataModelEntity = new DataModelEntity();
		dataModelEntity.setTableName(itemModel.getTableName());
		columnModel.setDataModel(dataModelEntity);
		parentItemModel.setColumnModel(columnModel);
		return parentItemModel;
	}

	//控件权限
	private void setItemPermissions(ItemPermissionModel itemPermissionModel, Map<String, ItemModelEntity> itemModelEntityMap){
		List<ItemPermissionInfo> itemPermissionInfos = new ArrayList<>();
		ItemModelEntity entity = itemModelEntityMap.get(itemPermissionModel.getItemModel().getTableName()+"_"+itemPermissionModel.getItemModel().getColumnName());
		if(entity == null){
			return;
		}
		if(itemPermissionModel.getAddPermissions() != null){
			ItemPermissionInfo itemAddPermissionInfo = new ItemPermissionInfo();
			BeanUtils.copyProperties(itemPermissionModel.getAddPermissions(), itemAddPermissionInfo, new String[]{"itemModel"});
			itemAddPermissionInfo.setDisplayTiming(DisplayTimingType.Add);
			itemAddPermissionInfo.setItemModel(entity);
			itemPermissionInfos.add(itemAddPermissionInfo);
		}
		if(itemPermissionModel.getUpdatePermissions() != null){
			ItemPermissionInfo itemUpdatePermissionInfo = new ItemPermissionInfo();
			BeanUtils.copyProperties(itemPermissionModel.getUpdatePermissions(), itemUpdatePermissionInfo, new String[]{"itemModel"});
			itemUpdatePermissionInfo.setDisplayTiming(DisplayTimingType.Update);
			itemUpdatePermissionInfo.setItemModel(entity);
			itemPermissionInfos.add(itemUpdatePermissionInfo);
		}

		if(itemPermissionModel.getCheckPermissions() != null){
			ItemPermissionInfo itemCheckPermissionInfo = new ItemPermissionInfo();
			BeanUtils.copyProperties(itemPermissionModel.getCheckPermissions(), itemCheckPermissionInfo, new String[]{"itemModel"});
			itemCheckPermissionInfo.setDisplayTiming(DisplayTimingType.Check);
			itemCheckPermissionInfo.setItemModel(entity);
			itemPermissionInfos.add(itemCheckPermissionInfo);
		}
		entity.setPermissions(itemPermissionInfos);

	}

	private void setColumnModel(ItemModelEntity entity, ItemModel itemModel){
		if(itemModel.getColumnModel() == null){
			entity.setColumnModel(null);
			if(itemModel.getSystemItemType() == SystemItemType.CreateDate){
				if(itemModel.getCreateType() == SystemCreateType.Create){
					createTableColumn( entity, "create_at", null, itemModel.getTableName());
				}else{
					createTableColumn( entity, "update_at", null, itemModel.getTableName());
				}
			}
			if(itemModel.getSystemItemType() == SystemItemType.Creator){
				if(itemModel.getCreateType() == SystemCreateType.Create){
					createTableColumn( entity, "create_by", null, itemModel.getTableName());
				}else{
					createTableColumn( entity, "update_by", null, itemModel.getTableName());
				}
			}
		}else{
			createTableColumn( entity,itemModel.getColumnModel().getColumnName(), itemModel.getColumnModel().getId(), itemModel.getColumnModel().getTableName());
		}
	}

	private void createTableColumn(ItemModelEntity entity, String columnName, String columnId, String tableName){
		ColumnModelEntity columnModelEntity = new ColumnModelEntity();
		columnModelEntity.setColumnName(columnName);
		columnModelEntity.setId(columnId);
		DataModelEntity dataModelEntity = new DataModelEntity();
		dataModelEntity.setTableName(tableName);
		columnModelEntity.setDataModel(dataModelEntity);
		entity.setColumnModel(columnModelEntity);
	}


	//关联的列表模型
	private ListModelEntity setItemModelByListModel(ItemModel itemModel){
		if(itemModel != null && itemModel.getReferenceList() != null){
			ListModelEntity listModelEntity = new ListModelEntity();
			BeanUtils.copyProperties(itemModel.getReferenceList(), listModelEntity, new String[]{"masterForm", "slaverForms","sortItems","searchItems","functions","displayItems"});
			return listModelEntity;
		}
		return null;
	}

	//关联的列表模型
	private ListModel getItemModelByEntity(ItemModelEntity itemModelEntity){
		ListModel ListModel = new ListModel();
		if(itemModelEntity == null){
			return ListModel;
		}
		ListModelEntity listModelEntity = null;
		if (itemModelEntity instanceof  ReferenceItemModelEntity){
			listModelEntity = ((ReferenceItemModelEntity) itemModelEntity).getReferenceList();
		}else if(itemModelEntity instanceof  SelectItemModelEntity){
			listModelEntity = ((SelectItemModelEntity) itemModelEntity).getReferenceList();
		}
		if(listModelEntity != null){
			BeanUtils.copyProperties(listModelEntity , ListModel, new String[]{"masterForm", "slaverForms","sortItems","searchItems","functions","displayItems"});
		}
		return ListModel;
	}

	private ItemActivityInfo wrap(ActivityInfo activityInfo) {
		ItemActivityInfo activityInfoEntity = new ItemActivityInfo();
		BeanUtils.copyProperties(activityInfo, activityInfoEntity, new String[]{"itemModel"});
		return activityInfoEntity;
	}

	private Page<FormModel> toDTO(Page<FormModelEntity> entities)  {
		Page<FormModel> formModels = Page.get(entities.getPage(), entities.getPagesize());
		formModels.data(entities.getTotalCount(), toDTO(entities.getResults()));
		return formModels;
	}

	private List<FormModel> toDTO(List<FormModelEntity> entities) {
		List<FormModel> formModels = new ArrayList<FormModel>();
		for (FormModelEntity entity : entities) {
			formModels.add(toDTO(entity));
		}
		return formModels;
	}

	private FormModel toDTO(FormModelEntity entity) {
		FormModel formModel = new FormModel();
		BeanUtils.copyProperties(entity, formModel, new String[] {"items","dataModels","permissions","submitChecks","functions"});
		if(entity.getDataModels() != null && entity.getDataModels().size() > 0){
			List<DataModel> dataModelList = new ArrayList<>();
			List<DataModelEntity> dataModelEntities = entity.getDataModels();
			for(DataModelEntity dataModelEntity : dataModelEntities){
				DataModel dataModel = new DataModel();
				BeanUtils.copyProperties(dataModelEntity, dataModel, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
				dataModelList.add(dataModel);
			}
			formModel.setDataModels(dataModelList);
		}

		return formModel;
	}

	private FormModel toDTODetail(FormModelEntity entity)  {
        FormModel formModel = new FormModel();

		entityToDTO( entity,  formModel, false);

		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			List<ItemModelEntity> itemModelEntities = entity.getItems() == null || entity.getItems().size() < 2 ? entity.getItems() : entity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

			//设置控件权限
			List<ItemPermissionModel> itemPermissionModels = getItemPermissions(entity.getItems());
			formModel.setPermissions(itemPermissionModels.size() > 0 ? itemPermissionModels : null);
			String tableName = entity.getDataModels() == null || entity.getDataModels().size() < 1 ? null :  entity.getDataModels().get(0).getTableName();
			for (ItemModelEntity itemModelEntity : itemModelEntities) {
				ItemModel itemModel = toDTO(itemModelEntity, false, tableName);
				if(itemModel.getSelectMode() == SelectMode.Attribute){
					itemModel.setTableName(tableName);
				}
				items.add(itemModel);
			}
			formModel.setItems(items);
		}

		if(entity.getSubmitChecks() != null && entity.getSubmitChecks().size() > 0){
            List<FormSubmitCheckModel> submitCheckModels = new ArrayList<>();
            for(FormSubmitCheckInfo info : entity.getSubmitChecks()){
                FormSubmitCheckModel checkModel = new FormSubmitCheckModel();
                BeanUtils.copyProperties(info, checkModel, new String[] {"formModel"});
				FormModel submitCheckFormModel = new FormModel();
				BeanUtils.copyProperties(entity, submitCheckFormModel, new String[] {"items","process","dataModels","permissions","submitChecks","functions"});
                checkModel.setFormModel(submitCheckFormModel);
                submitCheckModels.add(checkModel);
            }
			List<FormSubmitCheckModel> formSubmitCheckModels = submitCheckModels.size() < 2 ? submitCheckModels : submitCheckModels.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
            formModel.setSubmitChecks(formSubmitCheckModels);
        }



		if(entity.getDataModels() != null && entity.getDataModels().size() > 0){
			List<DataModel> dataModelList = new ArrayList<>();
			List<DataModelEntity> dataModelEntities = new ArrayList<>();
			for (DataModelEntity dataModelEntity : entity.getDataModels()) {
				dataModelEntities.add(dataModelEntity);
				if(dataModelEntity.getSlaverModels() != null && dataModelEntity.getSlaverModels().size() > 0) {
					dataModelEntities.addAll(dataModelEntity.getSlaverModels());
				}
			}
			for (DataModelEntity dataModelEntity : dataModelEntities) {
				dataModelList.add(formModelService.getDataModel(dataModelEntity));
			}
			formModel.setDataModels(dataModelList);
		}


		return formModel;
	}

	private List<ItemPermissionModel> getItemPermissions(List<ItemModelEntity> items){
		List<ItemPermissionModel> itemPermissionsList = new ArrayList<>();
		List<ItemModelEntity> columnItems = getColumnItem(items);
		for(ItemModelEntity columnItem : columnItems){
			if(columnItem.getPermissions() != null && columnItem.getPermissions().size() > 0){
				ItemPermissionModel itemPermissionModel = new ItemPermissionModel();
				itemPermissionModel.setId(columnItem.getId());
				itemPermissionModel.setName(columnItem.getName());
				ItemModel itemModel = new ItemModel();
				itemModel.setId(columnItem.getId());
				itemModel.setName(columnItem.getName());
				itemModel.setTableName(columnItem.getColumnModel().getDataModel().getTableName());
				itemModel.setColumnName(columnItem.getColumnModel().getColumnName());
				itemPermissionModel.setItemModel(itemModel);
				for(ItemPermissionInfo itemPermissionInfo : columnItem.getPermissions()) {
					ItemPermissionInfoModel itemPermissionInfoModel = new ItemPermissionInfoModel();
					BeanUtils.copyProperties(itemPermissionInfo, itemPermissionInfoModel, new String[]{"itemModel"});
					if(itemPermissionInfo.getDisplayTiming() == DisplayTimingType.Add) {
						itemPermissionModel.setAddPermissions(itemPermissionInfoModel);
					}else if(itemPermissionInfo.getDisplayTiming() == DisplayTimingType.Update){
						itemPermissionModel.setUpdatePermissions(itemPermissionInfoModel);
					}else {
						itemPermissionModel.setCheckPermissions(itemPermissionInfoModel);
					}
				}
				itemPermissionsList.add(itemPermissionModel);
			}
		}
		return itemPermissionsList;
	}

	private List<ItemModelEntity> getColumnItem(List<ItemModelEntity> allItems){
		List<ItemModelEntity> itemModels = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : allItems) {
			if (itemModelEntity instanceof SubFormItemModelEntity) {
				List<SubFormRowItemModelEntity> subRowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
				for (SubFormRowItemModelEntity rowItemModelEntity : subRowItems) {
					for (ItemModelEntity itemModel : rowItemModelEntity.getItems()) {
						if (itemModel.getColumnModel() != null) {
							itemModels.add(itemModel);
						}
					}
				}
			} else if (itemModelEntity instanceof RowItemModelEntity) {
				for (ItemModelEntity itemModel : ((RowItemModelEntity) itemModelEntity).getItems()) {
					if (itemModel.getColumnModel() != null) {
						itemModels.add(itemModel);
					}
				}
			} else if (itemModelEntity.getColumnModel() != null) {
				itemModels.add(itemModelEntity);
			}
		}
		return itemModels;
	}



	private PCFormModel toPCDTO(FormModelEntity entity) {
		PCFormModel formModel = new PCFormModel();
		entityToDTO( entity,  formModel, true);

		List<PCDataModel> dataModelList = new ArrayList<>();
		List<ItemModelEntity> itemModelEntities = formModelService.findAllItems(entity);
		Map<String, DataModelEntity> dataModelEntities = new HashMap<>();
		Map<String, List<String>> columnsMap = new HashMap<>();
		//关联表单
		Set<PCFormModel> referenceFormModelList = new HashSet<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getReferenceList() != null) {

				PCFormModel referencePCFormModel = new PCFormModel();
				entityToDTO((((ReferenceItemModelEntity) itemModelEntity).getReferenceList().getMasterForm()), referencePCFormModel, true);
				referenceFormModelList.add(referencePCFormModel);

				List<String> displayColuns = new ArrayList<>();
				ListModelEntity listModelEntity = ((ReferenceItemModelEntity) itemModelEntity).getReferenceList();
				if(listModelEntity == null || listModelEntity.getMasterForm() == null){
					continue;
				}
				Map<String, ItemModelEntity> itemModelEntityMap = new HashMap<>();
				for(ItemModelEntity itemModelEntity1 : listModelEntity.getDisplayItems()){
					itemModelEntityMap.put(itemModelEntity1.getId(), itemModelEntity1);

				}
				List<String> idList = new ArrayList<>();
				if(StringUtils.hasText(((ReferenceItemModelEntity) itemModelEntity).getReferenceList().getDisplayItemsSort())) {
					idList = Arrays.asList(((ReferenceItemModelEntity) itemModelEntity).getReferenceList().getDisplayItemsSort().split(","));
				}
				for(String id : idList){
					if(itemModelEntityMap.get(id) != null && itemModelEntityMap.get(id).getColumnModel() != null){
						displayColuns.add(itemModelEntityMap.get(id).getColumnModel().getColumnName());
					}
				}
				columnsMap.put(listModelEntity.getMasterForm().getId(), displayColuns);
				dataModelEntities.put(listModelEntity.getMasterForm().getId(), listModelEntity.getMasterForm().getDataModels().get(0));
			}
		}
		formModel.setReferenceFormModel(new ArrayList<>(referenceFormModelList));

		for(String formId : dataModelEntities.keySet()){
			PCDataModel dataModel = dataModelService.transitionToModel(formId, dataModelEntities.get(formId), columnsMap.get(formId));
			dataModelList.add(dataModel);
		}
		formModel.setDataModels(dataModelList);

		String tableName = entity.getDataModels() == null || entity.getDataModels().size() < 1 ? null :  entity.getDataModels().get(0).getTableName();
		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			List<ItemModelEntity> itemModelEntityList = entity.getItems() == null || entity.getItems().size() < 2 ? entity.getItems() : entity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for (ItemModelEntity itemModelEntity : itemModelEntityList) {
				items.add(toDTO(itemModelEntity, true, tableName));
			}
			formModel.setItems(items);
		}

		return formModel;
	}

	private void entityToDTO(FormModelEntity entity, Object object, boolean isPCForm){
		BeanUtils.copyProperties(entity, object, new String[] {"process","dataModels","items","permissions","submitChecks","functions"});
		if(entity.getFunctions() != null && entity.getFunctions().size() > 0){
			List<FunctionModel> functionModels = new ArrayList<>();
			for(ListFunction function : entity.getFunctions()){
				FunctionModel functionModel = new FunctionModel();
				BeanUtils.copyProperties(function, functionModel, new String[] {"formModel","itemModel"});
				functionModels.add(functionModel);
			}
			if(isPCForm) {
				((PCFormModel) object).setFunctions(functionModels);
			}else{
				((FormModel) object).setFunctions(functionModels);
			}
		}

		//数据标识
		if(StringUtils.hasText(entity.getItemModelIds())) {
			String[] strings = entity.getItemModelIds().split(",");
			List<String> resultList = new ArrayList<>();
			for(String str : strings){
				if(!resultList.contains(str)) {
					resultList.add(str);
				}
			}
			if(isPCForm) {
				((PCFormModel) object).setItemModelList(getItemModelList(resultList));
			}else{
				((FormModel) object).setItemModelList(getItemModelList(resultList));
			}
		}

		//二维码
		if(StringUtils.hasText(entity.getQrCodeItemModelIds())) {
			String[] strings = entity.getQrCodeItemModelIds().split(",");
			List<String> resultList = new ArrayList<>();
			for(String str : strings){
				if(!resultList.contains(str)) {
					resultList.add(str);
				}
			}
			if(isPCForm) {
				((PCFormModel) object).setQrCodeItemModelList(getItemModelList(resultList));
			}else{
				((FormModel) object).setQrCodeItemModelList(getItemModelList(resultList));
			}
		}
	}

	@Deprecated
	private PCFormModel toPCDTOold(FormModelEntity entity) throws InstantiationException, IllegalAccessException {
		PCFormModel formModel = BeanUtils.copy(entity, PCFormModel.class, new String[] {"dataModels"});
		List<ReferenceItemModel> pcReferenceItem = new ArrayList<>();
		List<ItemModelEntity> itemListModelEntities = new ArrayList<>();
		String tableName = entity.getDataModels() == null || entity.getDataModels().size() < 1 ? null :  entity.getDataModels().get(0).getTableName();
		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			for (ItemModelEntity itemModelEntity : entity.getItems()) {
				boolean flag = itemModelEntity instanceof ReferenceItemModelEntity || itemModelEntity instanceof SubFormItemModelEntity
						|| itemModelEntity instanceof SubFormRowItemModelEntity;
				if(!flag) {
					items.add(toDTO(itemModelEntity, true, tableName));
				}else if(((ReferenceItemModelEntity)itemModelEntity).getReferenceList() != null){
					itemListModelEntities.add(itemModelEntity);
				}
			}
			//formModel.setItems(items);
		}
		//关联数据模型
		for(ItemModelEntity itemModelEntity : itemListModelEntities){
			ListModelEntity listModelEntity = ((ReferenceItemModelEntity)itemModelEntity).getReferenceList();
			ReferenceItemModel referenceItem = new ReferenceItemModel();
			referenceItem.setId(itemModelEntity.getColumnModel().getDataModel().getId());
			referenceItem.setReferenceTable(itemModelEntity.getColumnModel().getDataModel().getTableName());
			referenceItem.setReferenceList(BeanUtils.copy(listModelEntity, ListModel.class, new String[] {"slaverForms","masterForm"}));
			pcReferenceItem.add(referenceItem);
		}

		//子表单模型
		List<SubFormItemModel> subDataModelList = new ArrayList<>();
		List<ItemModelEntity> itemModelEntity = entity.getItems().parallelStream().
				filter(t -> t.getType() == ItemType.SubForm).collect(Collectors.toList());

		for(ItemModelEntity itemModel : itemModelEntity) {
			SubFormItemModel subFormItemModel = BeanUtils.copy(itemModel, SubFormItemModel.class, new String[] {});
			subDataModelList.add(subFormItemModel);
		}
		//formModel.setSubFormItems(subDataModelList);
		//formModel.setReferenceItem(pcReferenceItem);
		return formModel;
	}

	private ItemModel toDTO(ItemModelEntity entity, boolean isPCItem, String tableName)  {
		//TODO 根据模型找到对应的参数
		ItemModel itemModel = new ItemModel();
		BeanUtils.copyProperties(entity, itemModel, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList", "defaultValue"});

		if(entity instanceof ReferenceItemModelEntity){
			if(((ReferenceItemModelEntity) entity).getItemModelIds() != null) {
				List<String> resultList = new ArrayList<>(Arrays.asList(((ReferenceItemModelEntity) entity).getItemModelIds().split(",")));
				itemModel.setItemModelList(getItemModelList(resultList));
			}
			String referenceItemId = ((ReferenceItemModelEntity) entity).getReferenceItemId();
			if(referenceItemId != null){
				ItemModelEntity itemModelEntity = itemModelService.get(referenceItemId);
				itemModel.setReferenceItemName(itemModelEntity == null ? null : itemModelEntity.getName());
			}
			String referenceFormId = ((ReferenceItemModelEntity) entity).getReferenceFormId();
			if(referenceFormId != null){
				FormModelEntity formModelEntity = formModelService.get(referenceFormId);
				itemModel.setReferenceFormName(formModelEntity == null ? null : formModelEntity.getName());
				if(formModelEntity.getDataModels() != null && formModelEntity.getDataModels().size() > 0) {
					itemModel.setTableName(formModelEntity.getDataModels().get(0).getTableName());
				}
			}
			if(isPCItem) {
				if(((ReferenceItemModelEntity) entity).getReferenceList() != null) {
					itemModel.setReferenceListId(((ReferenceItemModelEntity) entity).getReferenceList().getId());
				}
				if(entity.getType() == ItemType.ReferenceLabel) {
					if (((ReferenceItemModelEntity) entity).getParentItem() == null || ((ReferenceItemModelEntity) entity).getParentItem().getReferenceList() ==null) {
						itemModel.setReferenceListId(null);
					} else {
						itemModel.setReferenceListId(((ReferenceItemModelEntity) entity).getParentItem().getReferenceList().getId());
					}
//					itemModel.setReferenceListId(((ReferenceItemModelEntity) entity).getParentItem() == null ? null : ((ReferenceItemModelEntity) entity).getParentItem().getReferenceList().getId());
				}

			}
			if(entity.getType() == ItemType.ReferenceLabel && ((ReferenceItemModelEntity) entity).getReferenceItemId() != null){
				ItemModelEntity itemModelEntity = itemModelService.get(((ReferenceItemModelEntity) entity).getReferenceItemId());
				if(itemModelEntity != null && itemModelEntity.getColumnModel() != null) {
					itemModel.setItemTableName(itemModelEntity.getColumnModel().getDataModel().getTableName());
					itemModel.setItemColunmName(itemModelEntity.getColumnModel().getColumnName());
				}
			}

			if(((ReferenceItemModelEntity) entity).getReferenceType() == ReferenceType.ManyToMany){
				itemModel.setMultiple(true);
			}else if(((ReferenceItemModelEntity) entity).getReferenceType() == ReferenceType.OneToOne){
				itemModel.setMultiple(false);
			}else if(((ReferenceItemModelEntity) entity).getReferenceType() == ReferenceType.ManyToOne){
				if(((ReferenceItemModelEntity) entity).getType() == ItemType.ReferenceLabel){
					itemModel.setMultiple(true);
				}else{
					itemModel.setMultiple(false);
				}
			}else if(((ReferenceItemModelEntity) entity).getReferenceType() == ReferenceType.OneToMany){
				if(((ReferenceItemModelEntity) entity).getType() != ItemType.ReferenceLabel){
					itemModel.setMultiple(true);
				}else{
					itemModel.setMultiple(false);
				}
			}

			if(((ReferenceItemModelEntity) entity).getParentItem() != null){
				ItemModel itemModel1 = new ItemModel();
				BeanUtils.copyProperties(((ReferenceItemModelEntity) entity).getParentItem(), itemModel1, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
				if(((ReferenceItemModelEntity) entity).getParentItem() != null && ((ReferenceItemModelEntity) entity).getParentItem().getColumnModel()!=null && ((ReferenceItemModelEntity) entity).getParentItem().getColumnModel().getDataModel() != null) {
					itemModel1.setTableName(((ReferenceItemModelEntity) entity).getParentItem().getColumnModel().getDataModel().getTableName());
					itemModel1.setColumnName(((ReferenceItemModelEntity) entity).getParentItem().getColumnModel().getColumnName());
				}
				itemModel.setParentItem(itemModel1);
			}

			if(((ReferenceItemModelEntity) entity).getReferenceList() != null){
				ListModel referenceList = new ListModel();
				BeanUtils.copyProperties(((ReferenceItemModelEntity) entity).getReferenceList(), referenceList, new String[] {"masterForm", "slaverForms", "sortItems", "searchItems", "functions", "displayItems", "quickSearchItems"});
				itemModel.setReferenceList(referenceList);
			}

		}else if(entity instanceof SelectItemModelEntity){
			String defaultValue = ((SelectItemModelEntity) entity).getDefaultReferenceValue();
			if(defaultValue != null &&  !StringUtils.isEmpty(defaultValue) && (entity.getType() == ItemType.CheckboxGroup
					||entity.getType() == ItemType.RadioGroup ||entity.getType() == ItemType.Select)) {
				itemModel.setDefaultValue(Arrays.asList(defaultValue.split(",")));
			}else{
				itemModel.setDefaultValue((String)defaultValue);
			}
			if(entity.getOptions() != null && entity.getOptions().size() > 0){
				List<String> defaultList = new ArrayList<>();
				for(ItemSelectOption option : entity.getOptions()){
					if(option.getDefaultFlag() != null && option.getDefaultFlag()){
						defaultList.add(option.getId());
					}
				}
				itemModel.setDefaultValue(defaultList);
			}

			itemModel.setReferenceList(getItemModelByEntity(entity));

			if(((SelectItemModelEntity) entity).getReferenceDictionaryId() != null){
				DictionaryEntity dictionaryEntity = dictionaryService.get(((SelectItemModelEntity) entity).getReferenceDictionaryId());
				itemModel.setReferenceDictionaryName(dictionaryEntity == null ? null : dictionaryEntity.getName());
			}

			if(((SelectItemModelEntity) entity).getParentItem() != null){
				ItemModel parentItemModel = new ItemModel();
				BeanUtils.copyProperties(((SelectItemModelEntity) entity).getParentItem(), parentItemModel, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
				if(((SelectItemModelEntity) entity).getParentItem().getColumnModel() != null){
					ColumnModelInfo columnModel = new ColumnModelInfo();
					BeanUtils.copyProperties(((SelectItemModelEntity) entity).getParentItem().getColumnModel(), columnModel, new String[] {"dataModel","columnReferences"});
					if(((SelectItemModelEntity) entity).getParentItem().getColumnModel().getDataModel() != null){
						columnModel.setTableName(((SelectItemModelEntity) entity).getParentItem().getColumnModel().getDataModel().getTableName());
					}
					parentItemModel.setColumnName(columnModel.getColumnName());
                    parentItemModel.setTableName(columnModel.getTableName());
				}
				itemModel.setParentItem(parentItemModel);
			}

			//pc表单控件才有下拉子类
			if(isPCItem && ((SelectItemModelEntity) entity).getItems() != null && ((SelectItemModelEntity) entity).getItems().size() > 0){
				List<ItemModel> chiildrenItemModel = new ArrayList<>();
				for(SelectItemModelEntity selectItemModelEntity : ((SelectItemModelEntity) entity).getItems()) {
					ItemModel chiildItemModel = new ItemModel();
					BeanUtils.copyProperties(selectItemModelEntity, chiildItemModel, new String[]{"formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "items", "parentItem", "referenceList"});
					if (selectItemModelEntity.getColumnModel() != null) {
						ColumnModelInfo columnModel = new ColumnModelInfo();
						BeanUtils.copyProperties(selectItemModelEntity.getColumnModel(), columnModel, new String[]{"dataModel", "columnReferences"});
						if (selectItemModelEntity.getColumnModel().getDataModel() != null) {
							columnModel.setTableName(selectItemModelEntity.getColumnModel().getDataModel().getTableName());
						}
						chiildItemModel.setColumnModel(columnModel);
					}
					chiildrenItemModel.add(chiildItemModel);
				}
				itemModel.setItems(chiildrenItemModel);
			}

		}else if(entity instanceof RowItemModelEntity){
			List<ItemModel> rows = new ArrayList<>();
			List<ItemModelEntity> rowList = ((RowItemModelEntity) entity).getItems();
			List<ItemModelEntity> itemModelEntities = rowList == null || rowList.size() < 2 ? rowList : rowList.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for(ItemModelEntity itemModelEntity : itemModelEntities) {
				ItemModel itemModel1 = toDTO(itemModelEntity, isPCItem, tableName);
				if(itemModel1.getType() == ItemType.ReferenceLabel){
					itemModel1.setTableName(tableName);
				}
				rows.add(itemModel1);
			}
			itemModel.setItems(rows);
		}else if(entity instanceof SubFormItemModelEntity){
			List<ItemModel> subFormRows = new ArrayList<>();
			List<SubFormRowItemModelEntity> rowItemModelEntities = ((SubFormItemModelEntity) entity).getItems();

			List<SubFormRowItemModelEntity> subFormRowItemModelEntities = rowItemModelEntities == null || rowItemModelEntities.size() < 2 ? rowItemModelEntities : rowItemModelEntities.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

			for(SubFormRowItemModelEntity rowItemModelEntity : subFormRowItemModelEntities) {
				ItemModel subFormRowItem = new ItemModel();
				BeanUtils.copyProperties(rowItemModelEntity, subFormRowItem, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
				List<ItemModel> rows = new ArrayList<>();
				List<ItemModelEntity> itemModelEntities = rowItemModelEntity.getItems() == null || rowItemModelEntity.getItems().size() < 2 ? rowItemModelEntity.getItems() : rowItemModelEntity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
				for(ItemModelEntity childrenItem : itemModelEntities) {
					ItemModel childItem = toDTO(childrenItem, isPCItem, ((SubFormItemModelEntity) entity).getTableName());
					childItem.setTableName(((SubFormItemModelEntity) entity).getTableName());
					rows.add(childItem);
				}
				subFormRowItem.setTableName(((SubFormItemModelEntity) entity).getTableName());
				subFormRowItem.setItems(rows);
				subFormRows.add(subFormRowItem);
			}
			itemModel.setItems(subFormRows);
		}else if(entity instanceof TabsItemModelEntity){
			List<ItemModel> subFormRows = new ArrayList<>();
			List<TabPaneItemModelEntity> tabPaneItemModelEntities = ((TabsItemModelEntity) entity).getItems();

			List<TabPaneItemModelEntity> tabPaneItemModelEntityList = tabPaneItemModelEntities == null || tabPaneItemModelEntities.size() < 2 ? tabPaneItemModelEntities : tabPaneItemModelEntities.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for(TabPaneItemModelEntity tabPaneItemModelEntity : tabPaneItemModelEntityList) {
				ItemModel itemModel1 = new ItemModel();
				BeanUtils.copyProperties(tabPaneItemModelEntity, itemModel1, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
				List<ItemModel> children = new ArrayList<>();
				List<ItemModelEntity> itemModelEntities = tabPaneItemModelEntity.getItems() == null || tabPaneItemModelEntity.getItems().size() < 2 ? tabPaneItemModelEntity.getItems() : tabPaneItemModelEntity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
				for(ItemModelEntity childrenItem : itemModelEntities) {
					ItemModel childItem = toDTO(childrenItem, isPCItem, tableName);
					children.add(childItem);
				}
				itemModel1.setItems(children);
				subFormRows.add(itemModel1);
			}
			itemModel.setItems(subFormRows);
		} else if (entity instanceof TreeSelectItemModelEntity) {
			TreeSelectItemModelEntity treeSelectItemModelEntity = (TreeSelectItemModelEntity)entity;
			if (treeSelectItemModelEntity.getMultiple()) {
				if (!StringUtils.isEmpty(treeSelectItemModelEntity.getDefaultValue())) {
					itemModel.setDefaultValue(Arrays.asList(treeSelectItemModelEntity.getDefaultValue().split(",")));
                }
			} else {
				itemModel.setDefaultValue(treeSelectItemModelEntity.getDefaultValue());
            }
			if (!StringUtils.isEmpty(((TreeSelectItemModelEntity) entity).getDefaultValue())) {
				List<TreeSelectData> list = groupService.getTreeSelectDataSourceByIds(((TreeSelectItemModelEntity) entity).getDataSource().getValue(), ((TreeSelectItemModelEntity) entity).getDefaultValue().split(","));
				if(list != null && list.size() > 0) {
					itemModel.setDefaultValueName(list.parallelStream().map(TreeSelectData::getName).collect(Collectors.toList()));
				}
			}
		}

		if(entity.getColumnModel() != null) {
			ColumnModelInfo columnModel = new ColumnModelInfo();
			BeanUtils.copyProperties(entity.getColumnModel(), columnModel, new String[] {"dataModel","columnReferences"});
			if(entity.getColumnModel().getDataModel() != null){
				columnModel.setTableName(entity.getColumnModel().getDataModel().getTableName());
			}
			if(entity.getColumnModel().getColumnReferences() != null && entity.getColumnModel().getColumnReferences().size() > 0){
				List<ReferenceModel> referenceModelList = new ArrayList<>();
				//columnModel.setReferenceTables(referenceModelList);
			}
			itemModel.setColumnModel(columnModel);
		}

		if (entity.getActivities().size() > 0) {
			List<ActivityInfo> activities = new ArrayList<ActivityInfo>();
			for (ItemActivityInfo activityEntity : entity.getActivities()) {
				activities.add(toDTO(activityEntity));
			}
			itemModel.setActivities(activities);
		}

		if (entity.getOptions().size() > 0) {
			List<Option> options = new ArrayList<Option>();
			for (ItemSelectOption optionEntity : entity.getOptions()) {
				options.add(new Option(optionEntity.getId(), optionEntity.getLabel(), optionEntity.getValue(), optionEntity.getDefaultFlag()));
			}
			itemModel.setOptions(options);
		}
		if(isPCItem && entity.getPermissions() != null && entity.getPermissions().size() > 0){
			ItemPermissionModel itemPermissionModel = new ItemPermissionModel();
			itemPermissionModel.setId(entity.getId());
			itemPermissionModel.setName(entity.getName());
			ItemModel itemModel1 = new ItemModel();
			itemModel1.setId(entity.getId());
			itemModel1.setName(entity.getName());
			for(ItemPermissionInfo itemPermissionInfo : entity.getPermissions()){
				ItemPermissionInfoModel itemPermissionInfoModel = new ItemPermissionInfoModel();
				BeanUtils.copyProperties(itemPermissionInfo, itemPermissionInfoModel, new String[]{"itemModel"});
				if(itemPermissionInfo.getDisplayTiming() == DisplayTimingType.Add){
					itemPermissionModel.setAddPermissions(itemPermissionInfoModel);
				}else if(itemPermissionInfo.getDisplayTiming() == DisplayTimingType.Update){
					itemPermissionModel.setUpdatePermissions(itemPermissionInfoModel);
				}else{
					itemPermissionModel.setCheckPermissions(itemPermissionInfoModel);
				}
			}
			itemModel.setPermissions(itemPermissionModel);
		}
		return itemModel;
	}

	private List<ItemModel> getItemModelList(List<String> idResultList){
		if(idResultList == null || idResultList.size() < 1){
			return null;
		}
		List<ItemModelEntity> itemModelEntities = new ArrayList<>();
		for(String itemId : idResultList) {
			itemModelEntities.add(itemModelService.get(itemId));
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


	private ActivityInfo toDTO(ItemActivityInfo entity) {
		ActivityInfo activityInfo = new ActivityInfo();
		activityInfo.setId(entity.getId());
		activityInfo.setName(entity.getName());
		activityInfo.setActivityId(entity.getActivityId());
		activityInfo.setActivityName(entity.getActivityName());
		activityInfo.setVisible(entity.isVisible());
		activityInfo.setReadonly(entity.isReadonly());
		
		return activityInfo;		
	}
}
