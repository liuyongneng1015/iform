package tech.ascs.icity.iform.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.googlecode.genericdao.search.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iflow.api.model.Process;
import tech.ascs.icity.iflow.api.model.ProcessModel;
import tech.ascs.icity.iflow.client.ProcessService;
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
	private DictionaryDataService dictionaryService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private ProcessService processService;

	@Autowired
	private FormInstanceServiceEx formInstanceServiceEx;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<FormModel> list(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name = "type", required = false ) String type,
								@RequestParam(name = "dataModelId", required = false ) String dataModelId, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query().sort(Sort.desc("id"));
			if (StringUtils.hasText(name)) {
				query.filterLike("name", CommonUtils.convertParamOfFuzzySearch(name));
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}
			FormType formType = FormType.getByType(type);
			if (formType != null) {
				query.filterEqual("type",  formType);
			}
			if(StringUtils.hasText(dataModelId)) {
				List<String> idlist = getFormIdByDataModelId(dataModelId);
				if(idlist != null && idlist.size() > 0){
					query.filterIn("id",  idlist);
				}else{
					return new ArrayList<>();
				}
			}

			List<FormModelEntity> entities = query.list();
			return toDTO(entities, false);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	private List<String> getFormIdByDataModelId(String dataModelId){
		List<String> idlist = jdbcTemplate.query("select fd.form_model from ifm_form_data_bind fd where fd.data_model='"+dataModelId+"'",
				(rs, rowNum) -> rs.getString("form_model"));
		return idlist;
	}

	@Override
	public Page<FormModel> page(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name = "type", required = false ) String type,
								@RequestParam(name="page", defaultValue="1") int page, @RequestParam(name = "dataModelId", required = false ) String dataModelId,
								@RequestParam(name="pagesize", defaultValue="10") int pagesize, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", CommonUtils.convertParamOfFuzzySearch(name));
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}
			FormType formType = FormType.getByType(type);
			if (formType != null) {
				query.filterEqual("type",  formType);
			}
			if(StringUtils.hasText(dataModelId)) {
				List<String> idlist = getFormIdByDataModelId(dataModelId);
				if(idlist != null && idlist.size() > 0){
					query.filterIn("id",  idlist);
				}else{
					return Page.get(page, pagesize);
				}
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
        String key = formModel.getId() + "_" + formModel.getName() + "_saveFormDataModel";
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
            if(e instanceof ICityException){
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
        String key = formModel.getId()+"_"+formModel.getName() + "_createFormModel";
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
        String key = formModel.getId() + "_" + formModel.getName() + "_updateFormModel";
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
	public void saveFormModelProcess(@RequestBody FormModel formModel) {
		String key = formModel.getId() + "_" + formModel.getName() +"_updateFormModelProcess";
		try {
			if(concurrentmap.get(key) != null){
				throw  new IFormException("请不要重复提交");
			}
			concurrentmap.put(key, System.currentTimeMillis());
			formModelService.saveFormModelProcess(formModel);
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
            concurrentmap.put(key, System.currentTimeMillis());
			FormModelEntity formModelEntity = formModelService.get(id);
            checkFormModelCanDelete(Arrays.asList(formModelEntity));
            deleteFormModel(formModelEntity);
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
	}

	@Override
	public void removeFormModels(@RequestBody List<String> ids) {
	    if (ids!=null && ids.size()>0) {
	        List<FormModelEntity> list = formModelService.query().filterIn("id", ids).list();
	        checkFormModelCanDelete(list);
	        for (FormModelEntity formModelEntity:list) {
                deleteFormModel(formModelEntity);
            }
        }
	}

    public void deleteFormModel(FormModelEntity formModelEntity) {
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
    }

    // 校验表单是否被列表关联了
    public void checkFormModelCanDelete(List<FormModelEntity> list) {
        for (FormModelEntity formModel:list) {
            ListModelEntity listModel = listModelService.query().filterEqual("masterForm.id", formModel.getId()).first();
            if (listModel!=null) {
                throw new IFormException("\""+formModel.getName()+"\"表单被\""+listModel.getName()+"\"列表关联了");
            }
        }
    }

	@Override
	public AnalysisFormModel getPCFormModelById(@PathVariable(name="id") String id, @RequestParam(value = "deviceType", required = false) String deviceType,
												@RequestParam(value = "functionType", required = false) String functionType) {
		FormModelEntity entity = formModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "表单模型【" + id + "】不存在");
		}
		DeviceType type = DeviceType.getByValue(deviceType);
		DefaultFunctionType function = DefaultFunctionType.getByValue(functionType);
		try {
			return toAnalysisDTO(entity, type, function);
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
			 FormModel formModel = toDTO(formModelEntity, false);
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
	public List<Process> getAllProcess() {
		return processService.list();
	}

	@Override
	public void saveFormModelProcessBind(@PathVariable(name="id", required = true) String id,@RequestBody FormModel formModel) {
		if (!StringUtils.hasText(formModel.getId()) || !id.equals(formModel.getId())) {
			throw new IFormException("表单模型ID不一致");
		}
		try {
			FormModelEntity oldEntity = formModelService.get(id);
			boolean updatePermissFlag = oldEntity.getFunctions() != null && oldEntity.getFunctions().size() > 0;
			FormModelEntity entity = wrapProcessActivityBind(formModel);
			formModelService.saveFormModelProcessBind(entity);
			if(updatePermissFlag) {
				listModelService.submitFormBtnPermission(entity);
			}
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}
	}


	@Override
	public ItemModel findItemByTableAndColumName(@RequestParam(name = "tableName", defaultValue = "") String tableName,
												 @RequestParam(name = "columnName", defaultValue = "") String columnName) {
		if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(columnName)) {
			return null;
		}
		ItemModelEntity itemEntity = formModelService.findItemByTableAndColumName(tableName, columnName);
		if (itemEntity!=null) {
			ItemModel itemModel = new ItemModel();
			itemModel.setId(itemEntity.getId());
			itemModel.setName(itemEntity.getName());
			itemModel.setType(itemEntity.getType());
			itemModel.setSystemItemType(itemEntity.getSystemItemType());
			return itemModel;
		}
		return null;
	}

	public IdEntity findIdByTableName(@RequestParam(name = "tableName", defaultValue = "") String tableName) {
		if(StringUtils.isEmpty(tableName)){
			throw new IFormException("参数不能为空");
		}
		FormModelEntity formModelEntity = formModelService.query().filterEqual("dataModels.tableName", tableName).first();
		if (formModelEntity!=null) {
			return new IdEntity(formModelEntity.getId());
		} else {
			return null;
		}
	}

	@Override
	public AnalysisFormModel findByIdAndTableName(@RequestParam(name = "id", required = false) String id, @RequestParam(name = "tableName", required = false) String tableName) {
		if(!StringUtils.hasText(id) &&  !StringUtils.hasText(tableName)){
			throw new IFormException("参数不能为空");
		}
		FormModelEntity entity = null;
		if(StringUtils.hasText(id)) {
			entity = formModelService.find(id);
			if (entity == null) {
				throw new IFormException("表单模型【" + id + "】不存在");
			}
		}else{
			entity = formModelService.findByTableName(tableName);
		}
		try {
			if (entity != null) {
				return toAnalysisDTO(entity, null, null);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<ApplicationModel> findApplicationFormModel(@RequestParam(name="applicationId", required = true) String applicationId,
														   @RequestParam(name="columnId", required = false) String columnId,
														   @RequestParam(name="formModelId", required = false) String formModelId,
														   @RequestParam(name="type", required = false) String type) {
		List<FormModelEntity> formModels = null;
		if(StringUtils.hasText(columnId)){
			formModels = findFormModelsByColumnId(columnId);//关联表单
		}else if(StringUtils.hasText(formModelId)){
			formModels = findFormModelsByFormModelId(formModelId);//单个表单
			if(formModels == null || formModels.size() < 1){
				return new ArrayList<>();
			}
		}

		if(formModels == null || formModels.size() < 1) {
			FormType formType = FormType.getByType(type);
			if(formType != null){
				formModels = formModelService.query().filterEqual("type", formType).list();
			}else {
				formModels = formModelService.findAll();
			}
		}
		List<FormModelEntity> formModelEntityList = formModels.parallelStream().sorted(Comparator.comparing(FormModelEntity::getId).reversed()).collect(Collectors.toList());
        return getApplicationModels(formModelEntityList, applicationId, true, null);
	}

    @Override
    public List<ApplicationModel> findProcessApplicationFormModel(@RequestParam(name="applicationId", required = true) String applicationId,
                                                                  @RequestParam(name="key", required = false) String key) {
        List<FormModelEntity> formModelEntityList = formModelService.findProcessApplicationFormModel(key);
        return getApplicationModels(formModelEntityList, applicationId, false, key);
    }

    private List<ApplicationModel> getApplicationModels(List<FormModelEntity> formModelEntityList, String applicationId, boolean isNeedDataModel, String processKey){
        List<FormModel> formModelList = new ArrayList<>();
        Map<String, List<FormModel>> map = new HashMap<>();
        for(int i = 0; i < formModelEntityList.size(); i++){
			FormModelEntity entity = formModelEntityList.get(i);
        	if(StringUtils.hasText(processKey) && entity.getProcess() != null &&  processKey.equals(entity.getProcess().getKey())) {
				setFormModel(entity, formModelList, map, isNeedDataModel);
				formModelEntityList.remove(entity);
				i--;
			}
        }
		for(FormModelEntity entity : formModelEntityList){
			setFormModel(entity, formModelList,  map, isNeedDataModel);
		}
        List<ApplicationModel> applicationFormModels = new ArrayList<>();
        if(map != null && map.size() > 0) {
            setApplicationFormModels( map, applicationFormModels, applicationId);
        }
        return applicationFormModels;
    }

    private void setFormModel(FormModelEntity entity, List<FormModel> formModelList, Map<String, List<FormModel>> map, boolean isNeedDataModel){
		FormModel formModel = new FormModel();
		BeanUtils.copyProperties(entity, formModel, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});
		if(entity.getDataModels() != null && isNeedDataModel){
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
			return;
		}
		List<FormModel> list = map.get(entity.getApplicationId());
		if(list == null){
			list = new ArrayList<>();
		}
		list.add(formModel);
		map.put(entity.getApplicationId(), list);
	}

	private void setApplicationFormModels(Map<String, List<FormModel>> map, List<ApplicationModel> applicationFormModels, String applicationId){
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

		return getAllItemModel(formModelEntity, itemId);
	}


	private List<ItemModel> getAllItemModel(FormModelEntity formModelEntity, String itemId){
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
	public List<ItemModel> findItemsByProcessId(@PathVariable(name="processId", required = true) String processId) {
		FormModelEntity formModelEntity = formModelService.query().filterEqual("process.id", processId).first();
		if(formModelEntity == null){
			throw new IFormException("未找到流程【"+processId+"】关联的表单");
		}
		return getAllItemModel(formModelEntity, null);
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
		}

		if(formModel.getDataModels() == null || formModel.getDataModels().isEmpty()){
			throw new IFormException("请先关联数据模型");
		}

		if(formModel.getFunctions() != null && formModel.getFunctions().size() > 0){
			Map<String, String> map = new HashMap<>();
			for(FunctionModel function : formModel.getFunctions()){
				if(!StringUtils.hasText(function.getAction()) || !StringUtils.hasText(function.getLabel())){
					throw new IFormException("功能编码或者功能名为空");
				}
				if(map.get(function.getAction()) != null){
					throw new IFormException("功能编码重复");
				}

				if(function.getAction().length() > 20){
					throw new IFormException("功能编码超长");
				}

				if(function.getLabel().length() > 20){
					throw new IFormException("功能名超长");
				}

				map.put(function.getAction(), function.getLabel());
			}
		}

		if(formModel.getTriggeres() != null && formModel.getTriggeres().size() > 0){
			Map<String, String> map = new HashMap<>();
			for(BusinessTriggerModel triggerModel : formModel.getTriggeres()){
				if(triggerModel.getType() == null){
					throw new IFormException("业务触发类型不能为空");
				}
				if(!StringUtils.hasText(triggerModel.getUrl()) || !triggerModel.getUrl().startsWith("http") ){
					throw new IFormException("调用微服务地址格式错误");
				}
				if(map.get(triggerModel.getType().getValue()) != null){
					throw new IFormException("功能编码重复");
				}
				map.put(triggerModel.getType().getValue(), triggerModel.getUrl());
			}
		}
	}

	private FormModelEntity wrap(FormModel formModel) {
		veryFormModel(formModel);
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks","functions","triggeres"});

		if(formModel.getProcess() != null){
			formModel.setFunctions(null);
		}

		verifyFormModelName(formModel);

		//TODO 获取主数据模型
		DataModel masterDataModel = null;
		for(DataModel dataModel : formModel.getDataModels()){
			if(dataModel.getMasterModel() == null){
				masterDataModel = dataModel;
				break;
			}
		}
		verifyDataModel(formModel,  masterDataModel);

		//主表的数据建模
		DataModelEntity masterDataModelEntity = dataModelService.find(masterDataModel.getId());

		//旧的子数据建模
		Map<String, DataModelEntity> oldMasterDataModelMap = new HashMap<>();
		for(DataModelEntity dataModelEntity : masterDataModelEntity.getSlaverModels()){
			oldMasterDataModelMap.put(dataModelEntity.getId(), dataModelEntity);
		}


		List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();
		List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
		//为了设置关联
		Map<String, List<ItemModelEntity>> formMap = new HashMap<>();
		for (ItemModel itemModel : formModel.getItems()) {
			setItemModelEntity(formModel, itemModel, entity, items,	itemModelEntityList,  formMap);
		}
		formMap.put(masterDataModel.getTableName(), itemModelEntityList);

		Map<String, DataModel> newDataModelMap = new HashMap<>();
		for(DataModel dataModel : formModel.getDataModels()){
			newDataModelMap.put(dataModel.getTableName(), dataModel);
		}

		for(String key : formMap.keySet()){
			setReference(newDataModelMap.get(key), formMap.get(key));
		}

		//设置主表字段
		setMasterDataModelEntity(masterDataModelEntity, masterDataModel, formModel, oldMasterDataModelMap);

		//设置数据模型结构了
		List<DataModelEntity> dataModelEntities = new ArrayList<>();
		dataModelEntities.add(masterDataModelEntity);
		entity.setDataModels(dataModelEntities);
		entity.setItems(items);

		Map<String, ItemModelEntity> uuidItemModelEntityMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity : formModelService.findAllItems(entity)){
			if(StringUtils.hasText(itemModelEntity.getUuid())) {
				uuidItemModelEntityMap.put(itemModelEntity.getUuid(), itemModelEntity);
			}
		}
		//设置控件权限
		if(formModel.getPermissions() != null && formModel.getPermissions().size() > 0) {
			List<ItemPermissionModel> itemPermissionModels = formModel.getPermissions();
			for(int i = 0 ;i < itemPermissionModels.size() ; i++) {
				ItemPermissionModel itemPermissionModel = itemPermissionModels.get(i);
				setItemPermissions(itemPermissionModel, uuidItemModelEntityMap);
			}
		}

		if(formModel.getFunctions() != null && formModel.getFunctions().size() > 0){
			wrapFormFunctions(entity, formModel);
		}

		if(formModel.getTriggeres() != null && formModel.getTriggeres().size() > 0){
			wrapFormTriggeres(entity, formModel);
		}

		if(formModel.getSubmitChecks() != null && formModel.getSubmitChecks().size() > 0){
			wrapFormModelSubmitCheck(entity, formModel);
		}

		for(String key : oldMasterDataModelMap.keySet()){
			deleteSalverDataModel(oldMasterDataModelMap.get(key));
		}

		//数据标识对应的字段
		if(formModel.getItemModelList() != null && formModel.getItemModelList().size() > 0) {
			List<String> list = new ArrayList<>();
			for(ItemModel itemModel1 : formModel.getItemModelList()) {
				list.add(itemModel1.getUuid());
			}
			entity.setItemUuids(String.join(",", list));
		}else{
			entity.setItemUuids(null);
		}

		//二维码数据标识对应的字段
		if(formModel.getQrCodeItemModelList() != null && formModel.getQrCodeItemModelList().size() > 0) {
			List<String> list = new ArrayList<>();
			for(ItemModel itemModel1 : formModel.getQrCodeItemModelList()) {
				if(StringUtils.hasText(itemModel1.getUuid())) {
					list.add(itemModel1.getUuid());
				}
			}
			entity.setQrCodeItemUuids(String.join(",", list));
		}else{
			entity.setQrCodeItemUuids(null);
		}
		//保存数据模型
		dataModelService.save(masterDataModelEntity);

		return entity;
	}

	//更新表单建模
	private void deleteSalverDataModel(DataModelEntity dataModelEntity){
		dataModelService.deleteDataModelWithoutVerify(dataModelEntity);
	}

	private void setMasterDataModelEntity(DataModelEntity masterDataModelEntity, DataModel masterDataModel, FormModel formModel, Map<String, DataModelEntity> oldMasterDataModelMap){
		//是否需要关联字段
		boolean needMasterId = false;
		if(masterDataModelEntity.getMasterModel() != null) {
			needMasterId = true;
		}
		//设置数据模型行
		setDataModelEntityColumns(masterDataModel, masterDataModelEntity, needMasterId);

		BeanUtils.copyProperties(masterDataModel, masterDataModelEntity, new String[]{"masterModel", "slaverModels", "columns", "indexes" });

		//创建获取主键未持久化到数据库
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "create_at");
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "update_at");
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "create_by");
		columnModelService.saveColumnModelEntity(masterDataModelEntity, "update_by");

		if(masterDataModelEntity.getModelType() == DataModelType.Single && formModel.getDataModels().size() > 1) {
			masterDataModelEntity.setModelType(DataModelType.Master);
		}
		masterDataModelEntity.setSynchronized(false);

		List<DataModelEntity> slaverDataModelEntities = new ArrayList<>();
		for(int i = 1; i < formModel.getDataModels().size(); i++) {//第一个是主表
			DataModel dataModel = formModel.getDataModels().get(i);
			if (dataModel.getMasterModel() == null) {
				setSlaverDataModel(dataModel, oldMasterDataModelMap, null, slaverDataModelEntities);
			}else {
				setSlaverDataModel(dataModel, oldMasterDataModelMap, masterDataModelEntity, slaverDataModelEntities);
			}
		}
		masterDataModelEntity.setSlaverModels(slaverDataModelEntities);
	}

	private void verifyDataModel(FormModel formModel, DataModel masterDataModel){
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
			if (!Pattern.matches(CommonUtils.regEx, dataModel.getTableName())) {
				throw new IFormException("表名必须以字母开头，只能包含数字，字母，下划线，不能包含中文，横杆等特殊字符");
			}
			List<String> columnModelEntities = dataModel.getColumns().parallelStream().map(ColumnModel::getColumnName).collect(Collectors.toList());
			Map<String, Object> map = new HashMap<String, Object>();
			for(String string : columnModelEntities){
				String key = string.toLowerCase();
				if(!StringUtils.hasText(key.trim())){
					throw new IFormException("字段名不能为空");
				}
				if(map.containsKey(key)){
					throw new IFormException(key+"字段重复了");
				}
				map.put(key, string);
			}
		}

		if(masterDataModel == null || masterDataModel.getId() == null){
			throw new IFormException("未找到列表对应的数据建模");
		}
	}

	private void setItemModelEntity(FormModel formModel, ItemModel itemModel, FormModelEntity entity, List<ItemModelEntity> items,
					List<ItemModelEntity> itemModelEntityList, Map<String, List<ItemModelEntity>> formMap){
		ItemModelEntity itemModelEntity = wrap(formModel.getId(), itemModel);
		itemModelEntity.setFormModel(entity);
		items.add(itemModelEntity);
		itemModelEntityList.add(itemModelEntity);
		if(itemModelEntity instanceof TabsItemModelEntity){
			for(TabPaneItemModelEntity tabPaneItemModelEntity : ((TabsItemModelEntity)itemModelEntity).getItems()) {
				for(ItemModelEntity itemModelEntity1 : tabPaneItemModelEntity.getItems()) {
					if(itemModelEntity1 instanceof  SubFormItemModelEntity) {
						formMap.put(((SubFormItemModelEntity) itemModelEntity1).getTableName(), formModelService.getChildRenItemModelEntity(itemModelEntity1));
					}else{
						itemModelEntityList.add(itemModelEntity1);
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


	private void setSlaverDataModel(DataModel dataModel, Map<String, DataModelEntity> oldMasterDataModelMap, DataModelEntity masterDataModelEntity,
									List<DataModelEntity> slaverDataModelEntities){
		//创建关联字段
		DataModelEntity dataModelEntity = dataModel.isNew() ? new DataModelEntity() :  oldMasterDataModelMap.remove(dataModel.getId());
		if(dataModelEntity == null && !dataModel.isNew()){
			dataModelEntity = dataModelService.get(dataModel.getId());
		}
		if(dataModelEntity == null){
			throw  new IFormException("未找到"+dataModel.getTableName()+"对应的数据模型");
		}

		BeanUtils.copyProperties(dataModel, dataModelEntity, new String[]{"masterModel","slaverModels","columns","indexes"});
		dataModelEntity.setSynchronized(false);
		if(masterDataModelEntity != null){
			dataModelEntity.setModelType(DataModelType.Slaver);
			dataModelEntity.setMasterModel(masterDataModelEntity);
		}else{
			dataModelEntity.setMasterModel(null);
		}


		//设置数据模型行
		setDataModelEntityColumns(dataModel, dataModelEntity, true);

		//获取主键未持久化到数据库
		columnModelService.saveColumnModelEntity(dataModelEntity, "id");
		columnModelService.saveColumnModelEntity(dataModelEntity, "create_at");
		columnModelService.saveColumnModelEntity(dataModelEntity, "update_at");
		columnModelService.saveColumnModelEntity(dataModelEntity, "create_by");
		columnModelService.saveColumnModelEntity(dataModelEntity, "update_by");
		if(masterDataModelEntity != null) {
			slaverDataModelEntities.add(dataModelEntity);
		}else{
			dataModelService.save(dataModelEntity);
		}
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
				if(((ReferenceItemModelEntity) itemModelEntity).getCreateForeignKey() == null || !((ReferenceItemModelEntity) itemModelEntity).getCreateForeignKey()){
					continue;
				}
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
                    Map<String, Object> map = new HashMap<>();
                    for(int i = 0; i <  referenceColumnModel.getReferenceTables().size(); i ++){
                        ReferenceModel referenceModel  = referenceColumnModel.getReferenceTables().get(i);
                        String referenceKey = referenceModel.getReferenceTable()+"_"+referenceModel.getReferenceType().getValue();
                        if(map.get(referenceKey) != null){
                            referenceColumnModel.getReferenceTables().remove(referenceModel);
                            i--;
                        }
                        map.put(referenceKey, System.currentTimeMillis());
                    }
					List<String> refenceTables = referenceColumnModel.getReferenceTables().parallelStream().map(ReferenceModel::getReferenceTable).collect(Collectors.toList());
					if(!refenceTables.contains(formModelEntity.getDataModels().get(0).getTableName()) ||
                            map.get(formModelEntity.getDataModels().get(0).getTableName()+"_"+((ReferenceItemModelEntity) itemModelEntity).getReferenceType().getValue()) == null){
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
				checkInfos.add(checkInfo);
			}
			List<FormSubmitCheckInfo> checkInfoList = checkInfos.size() < 2 ? checkInfos : checkInfos.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			entity.setSubmitChecks(checkInfoList);
		}
	}

	private FormModelEntity wrapProcessActivityBind(FormModel formModel) {
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});

		//流程
		if(formModel.getProcess() != null){
			FormModel.ProceeeModel proceeeModel = formModel.getProcess();
			FormProcessInfo processInfo = new FormProcessInfo();
			BeanUtils.copyProperties(proceeeModel, processInfo);
			entity.setProcess(processInfo);
		}

		//流程环节绑定
		if(formModel.getItems() != null){
			List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
			for(ItemModel itemModel : formModel.getItems()){
				ItemModelEntity itemModelEntity1 = new ItemModelEntity();
				itemModelEntity1.setId(itemModel.getId());
				itemModelEntity1.setName(itemModel.getName());
				if(itemModel.getActivities() != null) {
					List<ItemActivityInfo> itemActivityInfos = new ArrayList<>();
					for (ActivityInfo activityInfo : itemModel.getActivities()) {
						ItemActivityInfo activityInfo1 = new ItemActivityInfo();
						BeanUtils.copyProperties(activityInfo, activityInfo1, new String[]{"itemModel"});
						activityInfo1.setItemModel(itemModelEntity1);
						itemActivityInfos.add(activityInfo1);
					}
					itemModelEntity1.setActivities(itemActivityInfos);
				}
				itemModelEntityList.add(itemModelEntity1);
			}
			entity.setItems(itemModelEntityList);
		}
		return entity;
	}

	private FormModelEntity wrapSubmitCheck(FormModel formModel) {
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});

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
		if(formModel.getFunctions() != null){
			List<ListFunction> functions = new ArrayList<>();
			for (int i = 0; i < formModel.getFunctions().size(); i++) {
			    FunctionModel model = formModel.getFunctions().get(i);
				ListFunction function =  new ListFunction();
				BeanUtils.copyProperties(model, function, new String[]{"formModel", "parseArea"});
				if (model.getParseArea()!=null && model.getParseArea().length>0) {
					function.setParseArea(String.join(",", model.getParseArea()));
				}
				function.setOrderNo(i+1);
				functions.add(function);
			}
			entity.setFunctions(functions);
		}
	}

	//设置表单业务触发
	private void wrapFormTriggeres(FormModelEntity entity, FormModel formModel) {
		if(formModel.getTriggeres() != null){
			List<BusinessTriggerEntity> list = new ArrayList<>();
			for (int i = 0; i < formModel.getTriggeres().size(); i++) {
				BusinessTriggerModel model = formModel.getTriggeres().get(i);
				BusinessTriggerEntity triggerEntity =  new BusinessTriggerEntity();
				BeanUtils.copyProperties(model, triggerEntity, new String[]{"formModel"});
				triggerEntity.setOrderNo(i+1);
				list.add(triggerEntity);
			}
			entity.setTriggeres(list);
		}
	}

	private String regEx = "[a-zA-Z]{1,}[a-zA-Z0-9_]{0,}";

	private void veryTableName(DataModelEntity oldDataModelEntity){
		if (!Pattern.matches(regEx, oldDataModelEntity.getTableName())) {
			throw new IFormException("表名必须以字母开头，只能包含数字，字母，下划线，不能包含中文，横杆等特殊字符");
		}
		List<DataModelEntity> list = dataModelService.findByProperty("tableName", oldDataModelEntity.getTableName());
		if(!StringUtils.hasText(oldDataModelEntity.getId()) && list.size() > 0){
			throw new IFormException("数据模型表名重复了");
		}
		for(DataModelEntity dataModelEntity : list){
			if(!dataModelEntity.getId().equals(oldDataModelEntity.getId())){
				throw new IFormException("数据模型表名重复了");
			}
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
			deleteColumnModelEntity(newColumnIds, columnModelEntity, needMasterId, oldDataModelEntity, i, newDataModel);
		}
		Map<String, Object> map = new HashMap<>();
		for(ColumnModel columnModel : newDataModel.getColumns()){
			ColumnModelEntity oldColumnModelEntity = setColumn(columnModel);
			setColumnModel(oldColumnModelEntity, oldDataModelEntity, columnModel, map);
			saveModelEntities.add(oldColumnModelEntity);
		}

		oldDataModelEntity.setColumns(saveModelEntities);
	}

	private void deleteColumnModelEntity(List<String> newColumnIds, ColumnModelEntity columnModelEntity, boolean needMasterId, DataModelEntity oldDataModelEntity, int i, DataModel newDataModel){
		//删除字段索引
		if(!newColumnIds.contains(columnModelEntity.getId())){
			List<ColumnReferenceEntity> referenceEntityList = columnModelEntity.getColumnReferences();
			for(int m = 0 ; m < referenceEntityList.size(); m++ ){
				ColumnReferenceEntity referenceEntity = referenceEntityList.get(m);
				if(columnModelEntity.getColumnName().equals("id") &&  referenceEntity.getReferenceType() != ReferenceType.ManyToMany){
					continue;
				}
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

			if((needMasterId && "master_id".equals(columnModelEntity.getColumnName())) || "id".equals(columnModelEntity.getColumnName())){
				return;
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
			//更新字段索引
			columnModelService.updateColumnModelEntityIndex(columnModelEntity);
			columnModelService.delete(columnModelEntity);
		}
	}

	private void setColumnModel(ColumnModelEntity oldColumnModelEntity, DataModelEntity oldDataModelEntity, ColumnModel columnModel, Map<String, Object> map){
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

	private ItemModelEntity wrap(String sourceFormModelId, ItemModel itemModel) {
		if(itemModel.getType() == ItemType.ReferenceLabel){
			if(itemModel.getParentItem() == null || (!StringUtils.hasText(itemModel.getReferenceItemId()) && !StringUtils.hasText(itemModel.getReferenceUuid()))){
				throw  new IFormException(itemModel.getName()+"没有对应关联表单控件或关联控件");
			}
			itemModel.setSelectMode(SelectMode.Attribute);
		}
		//TODO 根据类型映射对应的item
		ItemModelEntity entity = formModelService.getItemModelEntity(itemModel.getType(), itemModel.getSystemItemType());

		if(itemModel.getType() == ItemType.CheckboxGroup){
			itemModel.setMultiple(true);
		}else if(itemModel.getType() == ItemType.RadioGroup){
			itemModel.setMultiple(false);
		}

		if(itemModel.getSystemItemType() == SystemItemType.Creator){
			//创建人赋值关联关系
			ListModel listModel = listModelService.getFirstListModelByTableName(itemModel.getReferenceTableName());
			itemModel.setReferenceList(listModel);
			itemModel.setReferenceFormId(listModel == null || listModel.getMasterForm() == null ? null : listModel.getMasterForm().getId());
			itemModel.setCreateForeignKey(false);
			itemModel.setReferenceType(ReferenceType.ManyToOne);
			itemModel.setSelectMode(SelectMode.Single);
		}

		//需要保持column
		BeanUtils.copyProperties(itemModel, entity, new String[] {"defaultValue","referenceList","parentItem", "searchItems","sortItems", "permissions", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});

		//设置控件字段
		setColumnModel(entity, itemModel);
		entity.setSourceFormModelId(sourceFormModelId);

		if(!(entity instanceof RowItemModelEntity) && !(entity instanceof TabsItemModelEntity)
				&& !(entity instanceof SubFormItemModelEntity) && !(entity instanceof SubFormRowItemModelEntity)
				&& !(entity instanceof ReferenceItemModelEntity) && !(entity instanceof TabPaneItemModelEntity)
				&& entity.getType() != ItemType.Label && entity.getColumnModel() == null){
			throw  new IFormException("控件"+entity.getName()+"没有对应字段");
		}
		
		if(entity instanceof ReferenceItemModelEntity){
			setReferenceItemModel((ReferenceItemModelEntity)entity, sourceFormModelId, itemModel);
		}else if(entity instanceof SelectItemModelEntity){
			setSelectItemModel((SelectItemModelEntity)entity, itemModel);
		}else if(entity instanceof RowItemModelEntity){
			List<ItemModelEntity> rowList = new ArrayList<>() ;
			for(ItemModel rowItemModel : itemModel.getItems()) {
                ItemModelEntity itemModelEntity1 = wrap(sourceFormModelId, rowItemModel);
                itemModelEntity1.setFormModel(null);
				rowList.add(itemModelEntity1);
			}
			((RowItemModelEntity) entity).setItems(rowList);
		}else if(entity instanceof SubFormItemModelEntity){
			setSubFormItemModelEntity(itemModel, sourceFormModelId, (SubFormItemModelEntity) entity);
		}else if(entity instanceof TabsItemModelEntity){
			setTabsItemModelEntity( itemModel, sourceFormModelId, (TabsItemModelEntity)entity);
		} else if (entity instanceof TreeSelectItemModelEntity) {
			setTreeSelectItemModel(itemModel,  (TreeSelectItemModelEntity)entity);
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
		List<ItemSelectOption> options = new ArrayList<>();
		if (itemModel.getOptions() != null) {
			for (Option option : itemModel.getOptions()) {
				ItemSelectOption itemSelectOption = new ItemSelectOption();
				BeanUtils.copyProperties(option, itemSelectOption, new String[]{"itemModel"});
				itemSelectOption.setItemModel(entity);
				options.add(itemSelectOption);
			}
		}
		entity.setOptions(options);

		if(entity instanceof SelectItemModelEntity ){
			if(options.size() > 0) {
				((SelectItemModelEntity) entity).setSelectReferenceType(SelectReferenceType.Fixed);
			}else if(((SelectItemModelEntity) entity).getReferenceDictionaryId() != null){
				((SelectItemModelEntity) entity).setSelectReferenceType(SelectReferenceType.Dictionary);
			}
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

	private void setTreeSelectItemModel(ItemModel itemModel, TreeSelectItemModelEntity entity){
		if (itemModel.getDataSource()==null) {
			throw new ICityException(itemModel.getName()+" 树形下拉框控件必须设置数据源");
		}
		if (StringUtils.isEmpty(itemModel.getDataRange())) {
			throw new ICityException(itemModel.getName()+" 树形下拉框控件必须设置数据范围");
		}
		Object defaultValue = itemModel.getDefaultValue();
		if (defaultValue!=null && defaultValue instanceof List) {
			List<String> defaultValues = (List)defaultValue;
			if (defaultValues.size()>0) {
				List<TreeSelectData> result = formInstanceServiceEx.getTreeSelectData(itemModel.getDataSource(), defaultValues.toArray(new String[]{}));
				if (result == null || result.size() != defaultValues.size()) {
					throw new ICityException("树形下拉框的默认值与数据源设置的类型不一致");
				}
				entity.setDefaultValue(String.join(",", defaultValues));
			} else {
				entity.setDefaultValue("");
			}
		}else if (defaultValue!=null && defaultValue instanceof String && !StringUtils.isEmpty(defaultValue)) {
			List<TreeSelectData> result = formInstanceServiceEx.getTreeSelectData(itemModel.getDataSource(),  new String[] {(String)defaultValue});
			if (result==null || result.size()!=1) {
				throw new ICityException("树形下拉框的默认值与数据源设置的类型不一致");
			}
			entity.setDefaultValue(itemModel.getDefaultValue().toString());
		}else{
			entity.setDefaultValue("");
		}
	}

	private void setTabsItemModelEntity(ItemModel itemModel, String sourceFormModelId, TabsItemModelEntity entity){
		List<ItemModel> tabsItemModels = itemModel.getItems();
		List<TabPaneItemModelEntity> list = new ArrayList<>();
		if(tabsItemModels != null) {
			for (ItemModel itemModel1 : tabsItemModels){
				TabPaneItemModelEntity tabPaneItemModelEntity = new TabPaneItemModelEntity();
				BeanUtils.copyProperties(itemModel1, tabPaneItemModelEntity, new String[] {"searchItems","sortItems","items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
				List<ItemModelEntity> rowItemList = new ArrayList<>();
				if(itemModel1.getItems() != null) {
					for (ItemModel childrenItem : itemModel1.getItems()) {
						ItemModelEntity itemModelEntity1 = wrap(sourceFormModelId, childrenItem);
						itemModelEntity1.setFormModel(null);
						rowItemList.add(itemModelEntity1);
					}
				}
				tabPaneItemModelEntity.setParentItem(entity);
				tabPaneItemModelEntity.setFormModel(null);
				tabPaneItemModelEntity.setItems(rowItemList);
				list.add(tabPaneItemModelEntity);
			}
			entity.setItems(list);
		}
	}

	private void setSubFormItemModelEntity(ItemModel itemModel, String sourceFormModelId, SubFormItemModelEntity entity){
		List<ItemModel> rowItemModels = itemModel.getItems();
		List<SubFormRowItemModelEntity> rowItemModelEntities = new ArrayList<>();
		for(ItemModel rowItemModelEntity : rowItemModels) {
			SubFormRowItemModelEntity subFormRowItemModelEntity = new SubFormRowItemModelEntity();
			BeanUtils.copyProperties(rowItemModelEntity, subFormRowItemModelEntity, new String[] {"searchItems","sortItems","items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
			List<ItemModelEntity> rowItemList = new ArrayList<>();
			for(ItemModel childrenItem : rowItemModelEntity.getItems()) {
				ItemModelEntity itemModelEntity1 = wrap(sourceFormModelId, childrenItem);
				itemModelEntity1.setFormModel(null);
				rowItemList.add(itemModelEntity1);
			}
			subFormRowItemModelEntity.setFormModel(null);
			subFormRowItemModelEntity.setItems(rowItemList);
			rowItemModelEntities.add(subFormRowItemModelEntity);
		}
		entity.setItems(rowItemModelEntities);
	}

	private void setSelectItemModel(SelectItemModelEntity selectItemModelEntity, ItemModel itemModel ){
		if(itemModel.getDefaultValue() != null && itemModel.getDefaultValue() instanceof List){
			List<Object> objects = (List<Object>)itemModel.getDefaultValue();
			List<String> stringList = new ArrayList<>();
			for(Object o : objects){
				stringList.add(String.valueOf(o));
			}
			selectItemModelEntity.setDefaultReferenceValue(String.join(",",stringList));
		}else if(itemModel.getDefaultValue() != null){
			selectItemModelEntity.setDefaultReferenceValue(String.valueOf(itemModel.getDefaultValue()));
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
	}

	private ItemModelEntity getParentItemModel(ItemModel itemModel){
		ItemModelEntity parentItemModel = formModelService.getItemModelEntity(itemModel.getType(), itemModel.getSystemItemType());
		BeanUtils.copyProperties(itemModel, parentItemModel, new String[] {"referenceList","parentItem", "searchItems","sortItems", "permissions", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
		ColumnModelEntity columnModel = new ColumnModelEntity();
		columnModel.setColumnName(itemModel.getColumnName());
		DataModelEntity dataModelEntity = new DataModelEntity();
		dataModelEntity.setTableName(itemModel.getTableName());
		columnModel.setDataModel(dataModelEntity);
		parentItemModel.setColumnModel(columnModel);
		return parentItemModel;
	}

	private void setReferenceItemModel(ReferenceItemModelEntity entity, String sourceFormModelId, ItemModel itemModel){
		entity.setSourceFormModelId(sourceFormModelId);
		if(StringUtils.hasText(itemModel.getUuid())){
			ItemModelEntity itemModelEntity = itemModelService.findUniqueByProperty("uuid", itemModel.getUuid());
			if(itemModelEntity != null && !itemModelEntity.getId().equals(itemModel.getId())){
				throw  new IFormException("关联控件【"+itemModel.getName()+"】UUID重复了");
			}
		}

		if(StringUtils.hasText(itemModel.getItemUuids())){
			ItemModelEntity itemModelEntity = itemModelService.findUniqueByProperty("uuid", itemModel.getItemUuids());
			entity.setReferenceItemId(itemModelEntity == null ? null : itemModelEntity.getId());
		}

		if(itemModel.getType() != ItemType.ReferenceLabel && (!StringUtils.hasText(itemModel.getReferenceFormId())
				|| itemModel.getReferenceList() == null || itemModel.getReferenceList().getId() == null)){
			throw  new IFormException("关联属性控件【"+itemModel.getName()+"】未找到关联表单或列表模型");
		}

		if(itemModel.getType() == ItemType.ReferenceLabel && itemModel.getParentItem() == null){
            throw  new IFormException("关联控件【"+itemModel.getName()+"】未找到关联控件");
		}

		if(itemModel.getParentItem() != null) {
			ItemModel parentItemModel = itemModel.getParentItem();
			parentItemModel.setType(ItemType.ReferenceList);
            parentItemModel.setSystemItemType(SystemItemType.ReferenceList);
			entity.setParentItem((ReferenceItemModelEntity) getParentItemModel(parentItemModel));
		}

		//关联控件数据标识
		if(itemModel.getItemModelList() != null && itemModel.getItemModelList().size() > 0) {
			List<String> list = new ArrayList<>();
			for(ItemModel itemModel1 : itemModel.getItemModelList()) {
				list.add(itemModel1.getUuid());
			}
			entity.setItemUuids(String.join(",", list));
		}else{
			entity.setItemUuids(null);
		}

		//关联属性关联的控件
		if(itemModel.getType() == ItemType.ReferenceLabel){
			if(StringUtils.hasText(itemModel.getItemUuids())) {
				entity.setItemUuids(itemModel.getItemUuids());
			}else{
				entity.setItemUuids(null);
			}
		}
		entity.setReferenceList(setItemModelByListModel(itemModel));
	}

	//控件权限
	private void setItemPermissions(ItemPermissionModel itemPermissionModel, Map<String, ItemModelEntity> uuidItemModelEntityMap){
		List<ItemPermissionInfo> itemPermissionInfos = new ArrayList<>();
		ItemModelEntity entity = uuidItemModelEntityMap.get(itemPermissionModel.getUuid());
		if(entity == null || entity.getSystemItemType() == SystemItemType.ID ){
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
		formModels.data(entities.getTotalCount(), toDTO(entities.getResults(), true));
		return formModels;
	}

	private List<FormModel> toDTO(List<FormModelEntity> entities, boolean setFormProcessFlag) {
		List<FormModel> formModels = new ArrayList<FormModel>();
		for (FormModelEntity entity : entities) {
			formModels.add(toDTO(entity, setFormProcessFlag));
		}
		return formModels;
	}

	private FormModel toDTO(FormModelEntity entity, boolean setFormProcessFlag) {
		FormModel formModel = new FormModel();
		BeanUtils.copyProperties(entity, formModel, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});
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
		if(setFormProcessFlag) {
			setFormItemColumn(entity, formModel);
		}
		return formModel;
	}

	//设置表单流程字段
	private void setFormItemColumn(FormModelEntity entity, FormModel formModel){
		List<ItemModelEntity> itemModelEntityList = formModelService.getAllColumnItems(entity.getItems());
		List<ItemModel> itemModels = new ArrayList<>();
		for(ItemModelEntity entity1 : itemModelEntityList){
			ColumnModelEntity columnModelEntity = entity1.getColumnModel();
			if(columnModelEntity.getColumnName().equals("id") || columnModelEntity.getColumnName().equals("master_id")
					|| !columnModelEntity.getDataModel().getTableName().equals(entity.getDataModels().get(0).getTableName())){
				continue;
			}
			ItemModel itemModel = new ItemModel();
			itemModel.setName(entity1.getName());
			itemModel.setId(entity1.getId());
			if(entity1.getActivities() != null && entity1.getActivities().size() > 0) {
				List<ActivityInfo> activityInfos = new ArrayList<>();
				for(ItemActivityInfo info : entity1.getActivities()){
					ActivityInfo activityInfo = new ActivityInfo();
					BeanUtils.copyProperties(info, activityInfo, new String[]{"itemModel"});
					activityInfos.add(activityInfo);
				}
				itemModel.setActivities(activityInfos);
			}

			itemModels.add(itemModel);
		}
		formModel.setItems(itemModels);
		List<Activity> activities = new ArrayList<>();
		if(entity.getProcess() != null && StringUtils.hasText(entity.getProcess().getKey())){
			try {
				Process process = processService.get(entity.getProcess().getKey());
				if(process != null){
					activities.addAll(process.getActivities());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setFormItemActvitiy( formModel.getItems(),  activities);
	}

	private FormModel toDTODetail(FormModelEntity entity)  {
        FormModel formModel = new FormModel();

		entityToDTO( entity,  formModel, false, null);
		List<Activity> activities = new ArrayList<>();
		//是否流程表单
		boolean isFlowForm = false;
		setFlowParams(entity, activities, isFlowForm);

		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			List<ItemModelEntity> itemModelEntities = entity.getItems() == null || entity.getItems().size() < 2 ? entity.getItems() : entity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

			//设置控件权限
			List<ItemPermissionModel> itemPermissionModels = setItemPermissions(formModelService.findAllItems(entity), isFlowForm);
			formModel.setPermissions(itemPermissionModels);
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
				BeanUtils.copyProperties(entity, submitCheckFormModel, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});
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

		List<ItemModel> itemModels = formModelService.findAllItemModels(formModel.getItems());
		setFormItemActvitiy(itemModels,  activities);

		return formModel;
	}

	private void setFlowParams(FormModelEntity entity, List<Activity> activities , boolean isFlowForm){
		if(entity.getProcess() == null || !StringUtils.hasText(entity.getProcess().getKey())){
			return;
		}
		try {
			ProcessModel processModel = processService.getModel(entity.getProcess().getId());
			if(processModel != null){
				isFlowForm = true;
			}
			if(activities != null) {
				Process process = processService.get(entity.getProcess().getKey());
				if (process != null) {
					activities.addAll(process.getActivities());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//设置控件权限
	private void setFormItemActvitiy(List<ItemModel> itemModels, List<Activity> activities){
		if(activities.size() > 0) {
			Map<String, ActivityInfo> activityInfos = new HashMap<>();
			for(Activity activity : activities){
				ActivityInfo activityInfo = new ActivityInfo();
				activityInfo.setId(null);
				activityInfo.setActivityId(activity.getId());
				activityInfo.setActivityName(activity.getName());
				activityInfo.setFormKey(activity.getFormKey());
				activityInfo.setReadonly(true);
				activityInfo.setVisible(true);
				activityInfos.put(activity.getId(), activityInfo);
			}
			for (ItemModel itemModel :itemModels){
				if(itemModel.getType() == ItemType.Row || itemModel.getType() == ItemType.SubForm || itemModel.getType() == ItemType.RowItem
						|| itemModel.getType() == ItemType.Tabs || itemModel.getType() == ItemType.TabPane 	|| itemModel.getSystemItemType() == SystemItemType.ID){
					continue;
				}
				if(itemModel.getActivities() == null || itemModel.getActivities().size() < 1){
					itemModel.setActivities(new ArrayList<>(activityInfos.values()));
					continue;
				}
				if(itemModel.getActivities().size()>= activities.size()){
					continue;
				}
				List<String> activityIds = itemModel.getActivities().parallelStream().map(ActivityInfo::getActivityId).collect(Collectors.toList());
				for(String key : activityInfos.keySet()){
					if(!activityIds.contains(key)){
						itemModel.getActivities().add(activityInfos.get(key));
					}
				}
			}
		}
	}

	private List<ItemPermissionModel> setItemPermissions(List<ItemModelEntity> items, boolean isFlowForm){
		List<ItemPermissionModel> itemPermissionsList = new ArrayList<>();
		List<ItemModelEntity> columnItems = getColumnItem(items).parallelStream().sorted(Comparator.comparing(ItemModelEntity::getOrderNo).reversed()).collect(Collectors.toList());
		for(ItemModelEntity itemModelEntity1 : columnItems){
		    if(itemModelEntity1.getColumnModel() == null){
		        continue;
            }
            ItemPermissionModel itemPermissionModel = setItemPermissionModel(itemModelEntity1);
            if(itemModelEntity1.getPermissions() != null && itemModelEntity1.getPermissions().size() > 0){
				for(ItemPermissionInfo itemPermissionInfo : itemModelEntity1.getPermissions()) {
					ItemPermissionInfoModel itemPermissionInfoModel = new ItemPermissionInfoModel();
					BeanUtils.copyProperties(itemPermissionInfo, itemPermissionInfoModel, new String[]{"itemModel"});
					if(itemPermissionInfo.getDisplayTiming() == DisplayTimingType.Add) {
						itemPermissionModel.setAddPermissions(itemPermissionInfoModel);
					}else if(itemPermissionInfo.getDisplayTiming() == DisplayTimingType.Update && !isFlowForm){
						itemPermissionModel.setUpdatePermissions(itemPermissionInfoModel);
					}else if(!isFlowForm) {
						itemPermissionModel.setCheckPermissions(itemPermissionInfoModel);
					}
				}
			}else{
			    List<DisplayTimingType> displayTimingTypes = new ArrayList<>();
			    displayTimingTypes.add(DisplayTimingType.Add);
			    if(!isFlowForm) {
                    displayTimingTypes.add(DisplayTimingType.Update);
                    displayTimingTypes.add(DisplayTimingType.Check);
                }
                for(DisplayTimingType displayTimingType : displayTimingTypes){
                    ItemPermissionInfoModel permissionInfoModel = new ItemPermissionInfoModel();
                    permissionInfoModel.setVisible(false);
                    permissionInfoModel.setCanFill(false);
                    permissionInfoModel.setRequired(false);
                    permissionInfoModel.setDisplayTiming(displayTimingType);
                    if(displayTimingType == DisplayTimingType.Add) {
                        itemPermissionModel.setAddPermissions(permissionInfoModel);
                    }else if(displayTimingType == DisplayTimingType.Update) {
                        itemPermissionModel.setUpdatePermissions(permissionInfoModel);
                    }else {
                        permissionInfoModel.setCanFill(null);
                        permissionInfoModel.setRequired(null);
                        itemPermissionModel.setCheckPermissions(permissionInfoModel);
                    }
                }
            }
            itemPermissionsList.add(itemPermissionModel);
        }
		return itemPermissionsList;
	}

	private ItemPermissionModel setItemPermissionModel(ItemModelEntity itemModelEntity1){
        ItemPermissionModel itemPermissionModel = new ItemPermissionModel();
        itemPermissionModel.setId(itemModelEntity1.getId());
        itemPermissionModel.setName(itemModelEntity1.getName());
        itemPermissionModel.setUuid(itemModelEntity1.getUuid());
        itemPermissionModel.setTypeKey(itemModelEntity1.getTypeKey());
        return itemPermissionModel;
    }

	private List<ItemModelEntity> getColumnItem(List<ItemModelEntity> allItems){
		List<ItemModelEntity> itemModels = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : allItems) {
			if (itemModelEntity.getColumnModel() != null && !org.apache.commons.lang3.StringUtils.equalsIgnoreCase("id",itemModelEntity.getColumnModel().getColumnName())) {
				itemModels.add(itemModelEntity);
			}
		}
		return itemModels;
	}



	private AnalysisFormModel toAnalysisDTO(FormModelEntity entity, DeviceType deviceType, DefaultFunctionType function) {
		AnalysisFormModel formModel = new AnalysisFormModel();
		entityToDTO( entity,  formModel, true, deviceType);

		List<AnalysisDataModel> dataModelList = new ArrayList<>();
		List<ItemModelEntity> itemModelEntities = formModelService.findAllItems(entity);
		boolean isFlowForm = false;
		setFlowParams(entity, null, isFlowForm);
		//设置控件权限
		List<ItemPermissionModel> itemPermissionModels = setItemPermissions(itemModelEntities, isFlowForm);
		formModel.setPermissions(itemPermissionModels);

		Map<String, DataModelEntity> dataModelEntities = new HashMap<>();
		Map<String, List<String>> columnsMap = new HashMap<>();
		//关联表单
		Set<AnalysisFormModel> referenceFormModelList = new HashSet<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getReferenceList() != null) {
                setPCReferenceItemModel((ReferenceItemModelEntity)itemModelEntity, referenceFormModelList, dataModelEntities, columnsMap, deviceType);
			}
		}
		formModel.setReferenceFormModel(new ArrayList<>(referenceFormModelList));

		for(String formId : dataModelEntities.keySet()){
			AnalysisDataModel dataModel = dataModelService.transitionToModel(formId, dataModelEntities.get(formId), columnsMap.get(formId));
			dataModelList.add(dataModel);
		}
		formModel.setDataModels(dataModelList.size() < 1 ? null : dataModelList);

		String tableName = entity.getDataModels() == null || entity.getDataModels().size() < 1 ? null :  entity.getDataModels().get(0).getTableName();
		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			List<ItemModelEntity> itemModelEntityList = entity.getItems() == null || entity.getItems().size() < 2 ? entity.getItems() : entity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for (ItemModelEntity itemModelEntity : itemModelEntityList) {
				items.add(toDTO(itemModelEntity, true, tableName));
			}
			formModel.setItems(items);
		}else{
			formModel.setItems(null);
		}
		/*//联动控件
		List<LinkedItemModel> linkedItemModelList = new ArrayList<>();
		List<ItemModel> itemModels = formModelService.findAllItemModels(formModel.getItems());
		for(ItemModel itemModel : itemModels){
			if((itemModel.getType() == ItemType.Select || itemModel.getType() == ItemType.RadioGroup || itemModel.getType() == ItemType.CheckboxGroup) &&
					(itemModel.getReferenceRootFlag() != null && itemModel.getReferenceRootFlag())){
				SelectItemModelEntity rootItemModelEntity = (SelectItemModelEntity) itemModelService.get(itemModel.getId());
				LinkedItemModel rootItemModel = new LinkedItemModel();
				rootItemModel.setId(rootItemModelEntity.getId());
				//rootItemModel.setReferenceDictionaryId(rootItemModelEntity.getReferenceDictionaryId());
				//rootItemModel.setReferenceDictionaryItemId(rootItemModelEntity.getReferenceDictionaryItemId());
				//rootItemModel.setDefaultValue(rootItemModelEntity.getDefaultReferenceValue());
				getSelectItemChildRenItems(rootItemModel, rootItemModelEntity);
				linkedItemModelList.add(rootItemModel);
			}
		}
		formModel.setLinkedItemModelList(linkedItemModelList.size() > 0 ? linkedItemModelList : null);*/
		return formModel;
	}

	private void getSelectItemChildRenItems(LinkedItemModel itemModel,SelectItemModelEntity itemModelEntity){
		List<LinkedItemModel> childrenItemModel = new ArrayList<>();
		for (SelectItemModelEntity selectItemModelEntity : itemModelEntity.getItems()) {
			LinkedItemModel childItemModel = new LinkedItemModel();
			childItemModel.setId(selectItemModelEntity.getId());
			childItemModel.setParentItemId(itemModelEntity.getId());
			//chiildItemModel.setReferenceDictionaryId(selectItemModelEntity.getReferenceDictionaryId());
			getSelectItemChildRenItems(childItemModel, selectItemModelEntity);
			childrenItemModel.add(childItemModel);
		}
		itemModel.setItems(childrenItemModel.size() > 0 ? childrenItemModel : null);
	}

	private void setPCReferenceItemModel(ReferenceItemModelEntity itemModelEntity, Set<AnalysisFormModel> referenceFormModelList, Map<String, DataModelEntity> dataModelEntities,
										 Map<String, List<String>> columnsMap, DeviceType deviceType){
        AnalysisFormModel referencePCFormModel = new AnalysisFormModel();
        entityToDTO(itemModelEntity.getReferenceList().getMasterForm(), referencePCFormModel, true, deviceType);
        referenceFormModelList.add(referencePCFormModel);

        List<String> displayColuns = new ArrayList<>();
        ListModelEntity listModelEntity = itemModelEntity.getReferenceList();
        if(listModelEntity == null || listModelEntity.getMasterForm() == null){
            return;
        }
        Map<String, ItemModelEntity> itemModelEntityMap = new HashMap<>();
        for(ItemModelEntity itemModelEntity1 : listModelEntity.getDisplayItems()){
            itemModelEntityMap.put(itemModelEntity1.getId(), itemModelEntity1);

        }
        List<String> idList = new ArrayList<>();
        if(StringUtils.hasText(itemModelEntity.getReferenceList().getDisplayItemsSort())) {
            idList = Arrays.asList(itemModelEntity.getReferenceList().getDisplayItemsSort().split(","));
        }
        for(String id : idList){
            if(itemModelEntityMap.get(id) != null && itemModelEntityMap.get(id).getColumnModel() != null){
                displayColuns.add(itemModelEntityMap.get(id).getColumnModel().getColumnName());
            }
        }
        columnsMap.put(listModelEntity.getMasterForm().getId(), displayColuns);
        dataModelEntities.put(listModelEntity.getMasterForm().getId(), listModelEntity.getMasterForm().getDataModels().get(0));
    }

	private void entityToDTO(FormModelEntity entity, Object object, boolean isAnalysisForm, DeviceType deviceType){
		BeanUtils.copyProperties(entity, object, new String[] {"dataModels","items","permissions","submitChecks","functions", "triggeres"});
		if(entity.getFunctions() != null && entity.getFunctions().size() > 0){
			List<ListFunction> functions = entity.getFunctions().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			List<FunctionModel> functionModels = new ArrayList<>();
			for (int i = 0; i < functions.size(); i++) {
				ListFunction function = functions.get(i);
				if(deviceType != null && (function.getParseArea() == null || !function.getParseArea().contains(deviceType.getValue()))){
					continue;
				}
				FunctionModel functionModel = new FunctionModel();
				BeanUtils.copyProperties(function, functionModel, new String[] {"formModel","itemModel", "parseArea"});
				if (StringUtils.hasText(function.getParseArea())) {
					functionModel.setParseArea(function.getParseArea().split(","));
				}
				functionModel.setOrderNo(i+1);
				functionModels.add(functionModel);
			}
			if(isAnalysisForm) {
				((AnalysisFormModel) object).setFunctions(functionModels.size() < 1 ? null : functionModels);
			}else{
				((FormModel) object).setFunctions(functionModels.size() < 1 ? null : functionModels);
			}
		}

		if(entity.getTriggeres() != null && entity.getTriggeres().size() > 0){
			List<BusinessTriggerEntity> triggerEntityList = entity.getTriggeres().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			List<BusinessTriggerModel> triggerModels = new ArrayList<>();
			for (int i = 0; i < triggerEntityList.size(); i++) {
				BusinessTriggerEntity triggerEntity = triggerEntityList.get(i);
				BusinessTriggerModel model = new BusinessTriggerModel();
				BeanUtils.copyProperties(triggerEntity, model, new String[] {"formModel"});
				triggerModels.add(model);
			}
			if(isAnalysisForm) {
				((AnalysisFormModel) object).setTriggeres(triggerModels.size() < 1 ? null : triggerModels);
			}else{
				((FormModel) object).setTriggeres(triggerModels.size() < 1 ? null : triggerModels);
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
			if(isAnalysisForm) {
				((AnalysisFormModel) object).setItemModelList(getItemModelList(resultList));
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
			if(isAnalysisForm) {
				((AnalysisFormModel) object).setQrCodeItemModelList(getItemModelList(resultList));
			}else{
				((FormModel) object).setQrCodeItemModelList(getItemModelList(resultList));
			}
		}
	}

	@Deprecated
	private AnalysisFormModel toPCDTOold(FormModelEntity entity) throws InstantiationException, IllegalAccessException {
		AnalysisFormModel formModel = BeanUtils.copy(entity, AnalysisFormModel.class, new String[] {"dataModels"});
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

	private ItemModel toDTO(ItemModelEntity entity, boolean isAnalysisItem, String tableName)  {
		//TODO 根据模型找到对应的参数
		ItemModel itemModel = new ItemModel();
		BeanUtils.copyProperties(entity, itemModel, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList", "defaultValue"});

		if(itemModel.getType() == ItemType.ReferenceLabel || itemModel.getSystemItemType() == SystemItemType.ReferenceLabel){
			itemModel.setTableName(tableName);
		}

		if(entity instanceof ReferenceItemModelEntity){
			setReferenceItemModel( entity, itemModel,  isAnalysisItem);
		}else if(entity instanceof SelectItemModelEntity){
			setSelectItemModel( entity,  itemModel,  isAnalysisItem);
		}else if(entity instanceof RowItemModelEntity){
			setRowItemModel(entity, itemModel, isAnalysisItem, tableName);
		}else if(entity instanceof SubFormItemModelEntity){
			setSubFormItemModelEntity(entity, itemModel, isAnalysisItem);
		}else if(entity instanceof TabsItemModelEntity){
			setTabsItemModel(entity, itemModel, isAnalysisItem, tableName);
		} else if (entity instanceof TreeSelectItemModelEntity) {
			setTreeSelectItemModel(entity, itemModel, isAnalysisItem);
		}

		if(entity.getColumnModel() != null) {
			ColumnModelInfo columnModel = new ColumnModelInfo();
			BeanUtils.copyProperties(entity.getColumnModel(), columnModel, new String[] {"dataModel","columnReferences"});
			if(entity.getColumnModel().getDataModel() != null){
				columnModel.setTableName(entity.getColumnModel().getDataModel().getTableName());
			}
			itemModel.setColumnModel(columnModel);
		}

		if (entity.getActivities() != null && entity.getActivities().size() > 0) {
			List<ActivityInfo> activities = new ArrayList<ActivityInfo>();
			for (ItemActivityInfo activityEntity : entity.getActivities()) {
				activities.add(toDTO(activityEntity));
			}
			itemModel.setActivities(activities);
		}

		if (entity.getOptions().size() > 0) {
			List<Option> options = new ArrayList<Option>();
			for (ItemSelectOption optionEntity : entity.getOptions()) {
				if(isAnalysisItem) {
					options.add(new Option(optionEntity.getId(), optionEntity.getLabel(), optionEntity.getId(), optionEntity.getDefaultFlag()));
				}else{
					options.add(new Option(optionEntity.getId(), optionEntity.getLabel(), optionEntity.getValue(), optionEntity.getDefaultFlag()));
				}
			}
			itemModel.setOptions(options);
		}
		if(isAnalysisItem && entity.getPermissions() != null && entity.getPermissions().size() > 0){
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

		if(entity.getActivities() != null && entity.getActivities().size() > 0) {
			List<ActivityInfo> activityInfos = new ArrayList<>();
			for(ItemActivityInfo info : entity.getActivities()){
				ActivityInfo activityInfo = new ActivityInfo();
				BeanUtils.copyProperties(info, activityInfo, new String[]{"itemModel"});
				activityInfos.add(activityInfo);
			}
			itemModel.setActivities(activityInfos);
		}

		return itemModel;
	}

	private void setRowItemModel(ItemModelEntity entity, ItemModel itemModel, boolean isAnalysisItem, String tableName){
		List<ItemModel> rows = new ArrayList<>();
		List<ItemModelEntity> rowList = ((RowItemModelEntity) entity).getItems();
		List<ItemModelEntity> itemModelEntities = rowList == null || rowList.size() < 2 ? rowList : rowList.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
		for(ItemModelEntity itemModelEntity : itemModelEntities) {
			ItemModel itemModel1 = toDTO(itemModelEntity, isAnalysisItem, tableName);
			if(itemModel1.getType() == ItemType.ReferenceLabel){
				itemModel1.setTableName(tableName);
			}
			rows.add(itemModel1);
		}
		itemModel.setItems(rows);
	}

	private void setTabsItemModel(ItemModelEntity entity, ItemModel itemModel, boolean isAnalysisItem, String tableName){
		List<ItemModel> subFormRows = new ArrayList<>();
		List<TabPaneItemModelEntity> tabPaneItemModelEntities = ((TabsItemModelEntity) entity).getItems();

		List<TabPaneItemModelEntity> tabPaneItemModelEntityList = tabPaneItemModelEntities == null || tabPaneItemModelEntities.size() < 2 ? tabPaneItemModelEntities : tabPaneItemModelEntities.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
		for(TabPaneItemModelEntity tabPaneItemModelEntity : tabPaneItemModelEntityList) {
			ItemModel itemModel1 = new ItemModel();
			BeanUtils.copyProperties(tabPaneItemModelEntity, itemModel1, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
			List<ItemModel> children = new ArrayList<>();
			List<ItemModelEntity> itemModelEntities = tabPaneItemModelEntity.getItems() == null || tabPaneItemModelEntity.getItems().size() < 2 ? tabPaneItemModelEntity.getItems() : tabPaneItemModelEntity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for(ItemModelEntity childrenItem : itemModelEntities) {
				ItemModel childItem = toDTO(childrenItem, isAnalysisItem, tableName);
				children.add(childItem);
			}
			itemModel1.setItems(children);
			subFormRows.add(itemModel1);
		}
		itemModel.setItems(subFormRows);
	}

	private void setSubFormItemModelEntity(ItemModelEntity entity, ItemModel itemModel, boolean isAnalysisItem){
		List<ItemModel> subFormRows = new ArrayList<>();
		List<SubFormRowItemModelEntity> rowItemModelEntities = ((SubFormItemModelEntity) entity).getItems();

		List<SubFormRowItemModelEntity> subFormRowItemModelEntities = rowItemModelEntities == null || rowItemModelEntities.size() < 2 ? rowItemModelEntities : rowItemModelEntities.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

		for(SubFormRowItemModelEntity rowItemModelEntity : subFormRowItemModelEntities) {
			ItemModel subFormRowItem = new ItemModel();
			BeanUtils.copyProperties(rowItemModelEntity, subFormRowItem, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList"});
			List<ItemModel> rows = new ArrayList<>();
			List<ItemModelEntity> itemModelEntities = rowItemModelEntity.getItems() == null || rowItemModelEntity.getItems().size() < 2 ? rowItemModelEntity.getItems() : rowItemModelEntity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for(ItemModelEntity childrenItem : itemModelEntities) {
				ItemModel childItem = toDTO(childrenItem, isAnalysisItem, ((SubFormItemModelEntity) entity).getTableName());
				childItem.setTableName(((SubFormItemModelEntity) entity).getTableName());
				rows.add(childItem);
			}
			subFormRowItem.setTableName(((SubFormItemModelEntity) entity).getTableName());
			subFormRowItem.setItems(rows);
			subFormRows.add(subFormRowItem);
		}
		itemModel.setTableName(((SubFormItemModelEntity) entity).getTableName());
		itemModel.setItems(subFormRows);
	}

	private void setSelectItemModel(ItemModelEntity entity, ItemModel itemModel, boolean isAnalysisItem){
		String defaultValue = ((SelectItemModelEntity) entity).getDefaultReferenceValue();
		if(StringUtils.hasText(defaultValue) && (entity.getType() == ItemType.CheckboxGroup
				||entity.getType() == ItemType.RadioGroup ||entity.getType() == ItemType.Select)) {
			List<String> list = Arrays.asList(defaultValue.split(","));
			itemModel.setDefaultValue(list);
			itemModel.setDefaultValueName(formInstanceServiceEx.setSelectItemDisplayValue(null, (SelectItemModelEntity) entity, list));
		}else if(StringUtils.hasText(defaultValue)){
			itemModel.setDefaultValue(defaultValue);
			List<String> list = new ArrayList<>();
			list.add(defaultValue);
			itemModel.setDefaultValueName(formInstanceServiceEx.setSelectItemDisplayValue(null, (SelectItemModelEntity) entity, list));
		}
		if(entity.getOptions() != null && entity.getOptions().size() > 0){
			List<String> defaultList = new ArrayList<>();
			List<String> displayList = new ArrayList<>();

			for(ItemSelectOption option : entity.getOptions()){
				if(option.getDefaultFlag() != null && option.getDefaultFlag()){
					defaultList.add(option.getId());
					displayList.add(option.getLabel());
				}
			}
			itemModel.setDefaultValue(defaultList);
			itemModel.setDefaultValueName(displayList);
			if(itemModel.getSelectDataSourceType() == null){
				itemModel.setSelectDataSourceType(SelectDataSourceType.Option);
			}
		}

		itemModel.setReferenceList(getItemModelByEntity(entity));

		if(((SelectItemModelEntity) entity).getReferenceDictionaryId() != null){
			if(itemModel.getSelectDataSourceType() == null){
				itemModel.setSelectDataSourceType(SelectDataSourceType.DictionaryData);
			}
			DictionaryDataEntity dictionaryEntity = dictionaryService.get(((SelectItemModelEntity) entity).getReferenceDictionaryId());
			itemModel.setReferenceDictionaryName(dictionaryEntity == null ? null : dictionaryEntity.getName());
		}

		if(((SelectItemModelEntity) entity).getParentItem() != null){
			if(!isAnalysisItem) {
				ItemModel parentItemModel = new ItemModel();
				BeanUtils.copyProperties(((SelectItemModelEntity) entity).getParentItem(), parentItemModel, new String[]{"formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "items", "parentItem", "referenceList"});
				if (((SelectItemModelEntity) entity).getParentItem().getColumnModel() != null) {
					ColumnModelInfo columnModel = new ColumnModelInfo();
					BeanUtils.copyProperties(((SelectItemModelEntity) entity).getParentItem().getColumnModel(), columnModel, new String[]{"dataModel", "columnReferences"});
					if (((SelectItemModelEntity) entity).getParentItem().getColumnModel().getDataModel() != null) {
						columnModel.setTableName(((SelectItemModelEntity) entity).getParentItem().getColumnModel().getDataModel().getTableName());
					}
					parentItemModel.setColumnName(columnModel.getColumnName());
					parentItemModel.setTableName(columnModel.getTableName());
				}
				itemModel.setParentItem(parentItemModel);
				itemModel.setParentItemId(parentItemModel.getId());
			}else {
				itemModel.setParentItemId(((SelectItemModelEntity) entity).getParentItem().getId());
			}
		}

		//pc表单控件才有下拉子类
		if(isAnalysisItem && ((SelectItemModelEntity) entity).getItems() != null && ((SelectItemModelEntity) entity).getItems().size() > 0){
		    if(((SelectItemModelEntity) entity).getParentItem() != null){
                itemModel.setReferenceRootFlag(false);
            }else{
                itemModel.setReferenceRootFlag(true);
            }

			List<ItemModel> chiildrenItemModel = new ArrayList<>();
			for(SelectItemModelEntity selectItemModelEntity : ((SelectItemModelEntity) entity).getItems()) {
				ItemModel chiildItemModel = new ItemModel();
			    BeanUtils.copyProperties(selectItemModelEntity, chiildItemModel, new String[]{"formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "items", "parentItem", "referenceList"});
				chiildItemModel.setId(selectItemModelEntity.getId());
				chiildItemModel.setReferenceDictionaryId(selectItemModelEntity.getReferenceDictionaryId());
			    /*if (selectItemModelEntity.getColumnModel() != null) {
					ColumnModelInfo columnModel = new ColumnModelInfo();
					BeanUtils.copyProperties(selectItemModelEntity.getColumnModel(), columnModel, new String[]{"dataModel", "columnReferences"});
					if (selectItemModelEntity.getColumnModel().getDataModel() != null) {
						columnModel.setTableName(selectItemModelEntity.getColumnModel().getDataModel().getTableName());
					}
					chiildItemModel.setColumnModel(columnModel);
				}*/
				chiildrenItemModel.add(chiildItemModel);
			}
			itemModel.setItems(chiildrenItemModel);
		}
	}


	private void setTreeSelectItemModel(ItemModelEntity entity, ItemModel itemModel, boolean isAnalysisItem){
		TreeSelectItemModelEntity treeSelectEntity = (TreeSelectItemModelEntity)entity;
		if (treeSelectEntity.getMultiple()) {
			if (!StringUtils.isEmpty(treeSelectEntity.getDefaultValue())) {
				itemModel.setDefaultValue(Arrays.asList(treeSelectEntity.getDefaultValue().split(",")));
			}
		} else {
			if (!StringUtils.isEmpty(treeSelectEntity.getDefaultValue())) {
				itemModel.setDefaultValue(treeSelectEntity.getDefaultValue());
			}
		}
		if (!StringUtils.isEmpty(treeSelectEntity.getDefaultValue())) {
			List<TreeSelectData> list = formInstanceServiceEx.getTreeSelectData(treeSelectEntity.getDataSource(), treeSelectEntity.getDefaultValue().split(","));
			if(list != null && list.size() > 0) {
				List<String> defalueVlaues = list.parallelStream().map(TreeSelectData::getName).collect(Collectors.toList());
				if(treeSelectEntity.getMultiple() != null && treeSelectEntity.getMultiple()) {
					itemModel.setDefaultValueName(defalueVlaues);
				}else{
					itemModel.setDefaultValueName(defalueVlaues.get(0));
				}
			}
		}
	}

	private void setReferenceItemModel(ItemModelEntity entity, ItemModel itemModel, boolean isAnalysisItem){
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
		if(isAnalysisItem) {
			if(((ReferenceItemModelEntity) entity).getReferenceList() != null) {
				itemModel.setReferenceListId(((ReferenceItemModelEntity) entity).getReferenceList().getId());
			}
			if(entity.getType() == ItemType.ReferenceLabel) {
				if (((ReferenceItemModelEntity) entity).getParentItem() == null || ((ReferenceItemModelEntity) entity).getParentItem().getReferenceList() ==null) {
					itemModel.setReferenceListId(null);
				} else {
					itemModel.setReferenceListId(((ReferenceItemModelEntity) entity).getParentItem().getReferenceList().getId());
				}
			}
			if(entity.getSystemItemType() == SystemItemType.Creator) {
				itemModel.setControlType(ControlType.Input);
				itemModel.setType(ItemType.ReferenceList);
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
			if(entity.getType() == ItemType.ReferenceLabel){
				itemModel.setMultiple(true);
			}else{
				itemModel.setMultiple(false);
			}
		}else if(((ReferenceItemModelEntity) entity).getReferenceType() == ReferenceType.OneToMany){
			if(entity.getType() != ItemType.ReferenceLabel){
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
			itemModel.setParentItemId(itemModel1.getId());
		}

		if(((ReferenceItemModelEntity) entity).getReferenceList() != null){
			ListModel referenceList = new ListModel();
			BeanUtils.copyProperties(((ReferenceItemModelEntity) entity).getReferenceList(), referenceList, new String[] {"masterForm", "slaverForms", "sortItems", "searchItems", "functions", "displayItems", "quickSearchItems"});
			itemModel.setReferenceList(referenceList);
		}
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
			itemModel.setUuid(itemModelEntity.getUuid());
			itemModel.setTypeKey(itemModelEntity.getTypeKey());
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
