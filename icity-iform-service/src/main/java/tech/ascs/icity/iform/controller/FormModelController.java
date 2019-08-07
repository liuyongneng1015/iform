package tech.ascs.icity.iform.controller;

import com.googlecode.genericdao.search.Sort;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.admin.api.model.Application;
import tech.ascs.icity.admin.client.ApplicationService;
import tech.ascs.icity.admin.client.GroupService;
import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iflow.api.model.Process;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Api(tags = "表单模型服务", description = "包含表单模型的增删改查等功能")
@RestController
public class FormModelController implements tech.ascs.icity.iform.api.service.FormModelService {

    private static Map<String, Object> concurrentmap = new ConcurrentHashMap<String, Object>();

	@Autowired
	private FormModelService formModelService;

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
	private ProcessService processService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ELProcessorService elProcessorService;

	@Override
	public List<FormModel> list(@RequestParam(name = "name", required = false) String name,
								@RequestParam(name = "type", required = false) String type,
								@RequestParam(name = "dataModelId", required = false) String dataModelId,
								@RequestParam(name = "applicationId", required = false) String applicationId,
								@RequestParam(name = "forProcessBindingOnly", defaultValue = "false") boolean forProcessBindingOnly) {
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
			if (forProcessBindingOnly) { // 仅返回可供流程模型绑定的表单模型
				query.filterNull("process.id").filterEqual("type", FormType.General);
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
			return formModelService.toDTODetail(entity);
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
                dataModelService.veryTableName(formModel.getDataModels().get(0));
            }
			formModelService.verifyFormModelName(formModel);
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
			FormModelEntity entity = formModelService.wrap(formModel);
			entity = formModelService.save(entity);
			// 同步列表中的导入导出数据模板
			listModelService.syncListModelTempltes(entity, entity.getItems());
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
			FormModelEntity entity = formModelService.wrap(formModel);
			FormModelEntity formModelEntity = formModelService.save(entity);
			// 提交表单的按钮功能的权限给admin服务
			listModelService.submitFormBtnPermission(formModelEntity);
			// 同步列表中的导入导出数据模板
			listModelService.syncListModelTempltes(formModelEntity, formModelEntity.getItems());
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
			DataModelEntity dataModelEntity = itemModelEntity1.getColumnModel().getDataModel();
			String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
            if(itemModelEntity1 instanceof SubFormItemModelEntity){
                columnModelService.deleteTable(tableName);
            }
        }
        formModelService.delete(formModelEntity);
    }

    // 校验表单是否被列表关联了
    public void checkFormModelCanDelete(List<FormModelEntity> list) {
        for (FormModelEntity formModel:list) {
            ListModelEntity listModel = listModelService.query().filterEqual("masterForm.id", formModel.getId()).first();
            if (listModel!=null) {
                throw new IFormException(formModel.getName()+"表单被"+listModel.getName()+"列表关联了");
            }
        }
    }

	@Override
	public AnalysisFormModel getPCFormModelById(@PathVariable(name="id") String id, @RequestParam Map<String, Object> parameters) {
		FormModelEntity entity = formModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "表单模型【" + id + "】不存在");
		}
		try {
			return formModelService.toAnalysisDTO(entity, parameters);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public FormModel getByItemModelId(@RequestParam(name="itemModelId") String itemModelId) {
		ItemModelEntity itemModelEntity = itemModelService.find(itemModelId);
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
			 FormModel formModel = formModelService.toDTO(formModelEntity, false);
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
				return formModelService.toAnalysisDTO(entity, null);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<ApplicationModel> findApplicationFormModel(@RequestParam(name="applicationId", required = false) String applicationId,
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
    public List<ApplicationModel> findProcessApplicationFormModel(@RequestParam(name="applicationId", required = false) String applicationId,
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
		if(!StringUtils.hasText(entity.getApplicationId())){
			return;
		}
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
		ColumnModelEntity columnModelEntity = columnModelService.find(columnId);
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
				FormModelEntity formModelEntity = formModelService.find(referenceItemModelEntity.getSourceFormModelId());
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
		FormModelEntity formModelEntity = formModelService.find(id);
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
		FormModelEntity formModelEntity = formModelService.find(referenceFormModelId);
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
			itemModelService.copyItemModelEntityToItemModel(entity, itemModel);
			Optional.ofNullable(entity.getTriggerIds())
					.filter(StringUtils::hasText)
					.ifPresent(ids -> itemModel.setTriggerIds(Arrays.asList(ids.split(","))));
			list.add(itemModel);
		}
		return list;
	}

	private ItemModel convertItemModelByEntity(ItemModelEntity itemModelEntity){
		ItemModel itemModel = new ItemModel();
		itemModelService.copyItemModelEntityToItemModel(itemModelEntity, itemModel);
		Optional.ofNullable(itemModelEntity.getTriggerIds())
				.filter(StringUtils::hasText)
				.ifPresent(ids -> itemModel.setTriggerIds(Arrays.asList(ids.split(","))));
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
                boolean isExpression = elProcessorService.checkExpressionState(model.getCueExpression());
                if (!isExpression){
                    throw new IFormException(model.getCueExpression() + " 不是一个正确的表达式");
                }
				FormSubmitCheckInfo checkInfo =  new FormSubmitCheckInfo();
				BeanUtils.copyProperties(model, checkInfo, new String[]{"formModel"});
				checkInfo.setName(model.getName());
				checkInfo.setFormModel(entity);
				checkInfos.add(checkInfo);
			}
			List<FormSubmitCheckInfo> checkInfoList = checkInfos.size() < 2 ? checkInfos : checkInfos.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			entity.setSubmitChecks(checkInfoList);
		}
		return entity;
	}

	private Page<FormModel> toDTO(Page<FormModelEntity> entities)  {
		Page<FormModel> formModels = Page.get(entities.getPage(), entities.getPagesize());
		formModels.data(entities.getTotalCount(), toDTO(entities.getResults(), true));
		return formModels;
	}

	private List<FormModel> toDTO(List<FormModelEntity> entities, boolean setFormProcessFlag) {
		List<FormModel> formModels = new ArrayList<FormModel>();
		for (FormModelEntity entity : entities) {
			formModels.add(formModelService.toDTO(entity, setFormProcessFlag));
		}
		return formModels;
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
					items.add(itemModelService.toDTO(itemModelEntity, true, tableName));
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

}
