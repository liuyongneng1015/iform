package tech.ascs.icity.iform.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.googlecode.genericdao.search.Sort;
import freemarker.ext.beans.DateModel;
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

	@Override
	public List<FormModel> list(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query();
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
			Page<FormModelEntity> entities = query.page(page, pagesize).page();
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
		//校验表名
		if(formModel != null && formModel.getDataModels() != null && formModel.getDataModels().size() > 0) {
			DataModel dataModel = formModel.getDataModels().get(0);
			DataModelEntity dataModelEntity = new DataModelEntity();
			dataModelEntity.setId(dataModel.isNew()? null : dataModel.getId());
			dataModelEntity.setTableName(dataModel.getTableName());
			veryTableName(dataModelEntity);
		}
		verifyFormModelName(formModel);
 		FormModelEntity oldEntity = formModelService.saveFormModel(formModel);
		return new IdEntity(oldEntity.getId());
	}



	@Override
	public IdEntity createFormModel(@RequestBody FormModel formModel) {
		if (StringUtils.hasText(formModel.getId())) {
			throw new IFormException("表单模型ID不为空，请使用更新操作");
		}
		try {
			FormModelEntity entity = wrap(formModel);
			entity = formModelService.save(entity);
			return new IdEntity(entity.getId());
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void updateFormModel(@PathVariable(name = "id") String id, @RequestBody FormModel formModel) {
		if (!StringUtils.hasText(formModel.getId()) || !id.equals(formModel.getId())) {
			throw new IFormException("表单模型ID不一致");
		}
		try {
			FormModelEntity entity = wrap(formModel);
			formModelService.save(entity);
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void removeFormModel(@PathVariable(name="id") String id) {
		FormModelEntity formModelEntity = formModelService.get(id);
		if(formModelEntity != null) {
			formModelService.deleteFormModelEntity(formModelEntity);
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
	public void saveFormModelPermission(@PathVariable(name="id", required = true) String id,@RequestBody FormModel formModel) {
		if (!StringUtils.hasText(formModel.getId()) || !id.equals(formModel.getId())) {
			throw new IFormException("表单模型ID不一致");
		}
		try {
			FormModelEntity entity = wrapPermission(formModel);
			formModelService.saveFormModelPermission(entity);
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
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
	public List<ApplicationModel> findApplicationFormModel(@RequestParam(name="applicationId", required = true) String applicationId) {
		List<FormModelEntity> formModels = formModelService.findAll();
		List<FormModel> formModelList = new ArrayList<>();
		Map<String, List<FormModel>> map = new HashMap<>();
		for(FormModelEntity entity : formModels){

			FormModel formModel = new FormModel();
			BeanUtils.copyProperties(entity, formModel, new String[] {"items","dataModels","permissions","submitChecks"});
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
					itemModelList.add(convertItemModelByEntity(itemModelEntity));
					break;
				}
			}
			for (ItemModelEntity itemModelEntity : list){
				if(itemId != null && itemModelEntity.getId().equals(itemId)) {
					continue;
				}
				itemModelList.add(convertItemModelByEntity(itemModelEntity));
			}
		}
		return itemModelList;
	}

	private ItemModel convertItemModelByEntity(ItemModelEntity itemModelEntity){
		ItemModel itemModel = new ItemModel();
		BeanUtils.copyProperties(itemModelEntity, itemModel, new String[]{"formModel", "columnModel", "activities", "options", "permission","items","parentItem","referenceList"});
		if(itemModelEntity.getColumnModel() != null){
			ColumnModelInfo columnModel = new ColumnModelInfo();
			BeanUtils.copyProperties(itemModelEntity.getColumnModel(), columnModel, new String[]{"dataModel", "columnReferences"});
			columnModel.setItemId(itemModel.getId());
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

	private FormModelEntity wrap(FormModel formModel) {
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks"});

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
		for(DataModel dataModel : formModel.getDataModels()){
			List<String> columnModelEntities = dataModel.getColumns().parallelStream().map(ColumnModel::getColumnName).collect(Collectors.toList());
			Map<String, Object> map = new HashMap<String, Object>();
			for(String string : columnModelEntities){
				if(map.containsKey(string)){
					throw new IFormException("字段重复了");
				}
				map.put(string, string);
			}
		}


		//主表的数据建模
		DataModelEntity masterDataModelEntity = dataModelService.find(masterDataModel.getId());
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

		//创建获取关联字段未持久化到数据库
		setMasterIdColumnEntity(masterDataModelEntity);

		if(masterDataModelEntity.getModelType() == DataModelType.Single && dataModels.size() > 1) {
			masterDataModelEntity.setModelType(DataModelType.Master);
		}
		masterDataModelEntity.setSynchronized(false);

		List<DataModelEntity> slaverDataModelEntities = new ArrayList<>();
		for(DataModel dataModel : dataModels) {
			if (dataModel.getMasterModel() != null) {
				DataModelEntity slaverDataModelEntity = new DataModelEntity();
				//创建关联字段
				boolean dataFlag = dataModel.isNew();
				if (!dataFlag) {
					slaverDataModelEntity = dataModelService.find(dataModel.getId());
				}

				slaverDataModelEntity.setModelType(DataModelType.Slaver);
				slaverDataModelEntity.setSynchronized(false);
				slaverDataModelEntity.setMasterModel(masterDataModelEntity);
				BeanUtils.copyProperties(dataModel, slaverDataModelEntity, new String[]{"masterModel","slaverModels","columns","indexes"});

				//设置数据模型行
				setDataModelEntityColumns(dataModel, slaverDataModelEntity, true);

				//创建获取主键未持久化到数据库
				ColumnModelEntity idColumnReference = columnModelService.saveColumnModelEntity(slaverDataModelEntity, "id");

				//子表不需要关联
				setMasterIdColunm(slaverDataModelEntity, masterIdColumnEntity);

				slaverDataModelEntities.add(slaverDataModelEntity);
			}
		}
		//masterDataModelEntity.setSlaverModels(slaverDataModelEntities);

		//设置数据模型结构了
		List<DataModelEntity> dataModelEntities = new ArrayList<>();
		dataModelEntities.add(masterDataModelEntity);
		entity.setDataModels(dataModelEntities);

		dataModelService.save(masterDataModelEntity);


		List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();
		Map<String, ItemModelEntity> itemMap = new HashMap<>();
		for (ItemModel itemModel : formModel.getItems()) {
			ItemModelEntity itemModelEntity = wrap(itemModel);
			itemModelEntity.setFormModel(entity);
			items.add(itemModelEntity);
			itemMap.put(itemModel.getName(), itemModelEntity);
		}
		entity.setItems(items);
		return entity;
	}

	private FormModelEntity wrapPermission(FormModel formModel) {
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks"});

		if(formModel.getPermissions() != null){
			List<ItemPermissionInfo> permissionInfos = new ArrayList<>();
			for(ItemPermissionModel model : formModel.getPermissions()){
				ItemPermissionInfo permissionInfo = new ItemPermissionInfo();
				BeanUtils.copyProperties(model, permissionInfo, new String[]{"formModel" ,"itemModel"});
				if(model.getItemModel() != null){
					ItemModelEntity itemModelEntity = new ItemModelEntity();
					BeanUtils.copyProperties(model.getItemModel(), itemModelEntity, new String[] {"formModel","columnModel","activities","options","permission"});
					permissionInfo.setItemModel(itemModelEntity);
					itemModelEntity.setPermission(permissionInfo);
				}
				permissionInfo.setFormModel(entity);
				permissionInfos.add(permissionInfo);
			}
			entity.setPermissions(permissionInfos);
		}

		return entity;
	}

	private FormModelEntity wrapSubmitCheck(FormModel formModel) {
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks"});

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

		//待更新的行
		List<String> newColumnIds = new ArrayList<>();
		ColumnModel idColumnModel= null;
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
				columnModelService.save(columnModelEntity);

				if("id".equals(columnModelEntity.getColumnName()) || (needMasterId && "master_id".equals(columnModelEntity.getColumnName()))){
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
				columnModelService.delete(columnModelEntity);
			}
		}

		for(ColumnModel columnModel : newDataModel.getColumns()){
			ColumnModelEntity oldColumnModelEntity = setColumn(columnModel);
			List<ColumnReferenceEntity> oldColumnReferences = oldColumnModelEntity.getColumnReferences();
			for(int i = 0 ; i < oldColumnReferences.size() ; i++) {
				ColumnReferenceEntity columnReferenceEntity = oldColumnReferences.get(i);
				oldColumnReferences.remove(i);
				i--;
				columnModelService.deleteColumnReferenceEntity(columnReferenceEntity);
			}
			oldColumnModelEntity.setColumnReferences(null);

			oldColumnModelEntity.setDataModel(oldDataModelEntity);
			if(columnModel.getReferenceTables() != null){
				for(ReferenceModel columnReferenceEntity : columnModel.getReferenceTables()){
					DataModelEntity dataModelEntity = dataModelService.findUniqueByProperty("tableName", columnReferenceEntity.getReferenceTable());
					ColumnModelEntity columnModelEntity = columnModelService.saveColumnModelEntity(dataModelEntity, columnReferenceEntity.getReferenceValueColumn());
					ColumnModel referenceColumnModel = new ColumnModel();
					referenceColumnModel.setId(columnModelEntity.getId());
					columnModelService.saveColumnReferenceEntity(oldColumnModelEntity, setColumn(referenceColumnModel), columnReferenceEntity.getReferenceType(), columnReferenceEntity.getReferenceMiddleTableName());
				}
			}
			saveModelEntities.add(oldColumnModelEntity);
		}

		oldDataModelEntity.setColumns(saveModelEntities);
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

	private ItemModelEntity wrap(ItemModel itemModel) {

		//TODO 根据类型映射对应的item
		ItemModelEntity entity = formModelService.getItemModelEntity(itemModel.getType());
		if(itemModel.getSystemItemType() == SystemItemType.SerialNumber){
			 entity = new SerialNumberItemModelEntity();
		}
		//需要保持column
		BeanUtils.copyProperties(itemModel, entity, new String[] {"searchItems","sortItems", "permission", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});

		if(!itemModel.isNew()){
			ItemModelEntity itemModelEntity = itemModelService.find(itemModel.getId());
			if(itemModelEntity != null && itemModelEntity.getPermission() != null){
				ItemPermissionInfo itemPermissionInfo = new ItemPermissionInfo();
				BeanUtils.copyProperties(itemModelEntity.getPermission(), itemPermissionInfo, new String[]{"formModel", "itemModel"});
				entity.setPermission(itemPermissionInfo);
				itemPermissionInfo.setItemModel(entity);
				FormModelEntity formModelEntity = new FormModelEntity();
				BeanUtils.copyProperties(itemModelEntity.getFormModel(), formModelEntity, new String[] {"dataModels", "items","permissions","submitChecks"});
				itemPermissionInfo.setFormModel(formModelEntity);
			}
		}

		setColumnModel(entity, itemModel);

		if(entity instanceof ReferenceItemModelEntity){
			((ReferenceItemModelEntity) entity).setItemModelIds(org.apache.commons.lang3.StringUtils.join(itemModel.getItemModelList(),","));
			((ReferenceItemModelEntity) entity).setReferenceList(setItemModelByListModel(itemModel));
		}else if(entity instanceof SelectItemModelEntity){
			if(itemModel.getDefaultValue() != null && itemModel.getDefaultValue().size() > 0){
				((SelectItemModelEntity) entity).setDefaultReferenceValue(org.apache.commons.lang3.StringUtils.join(itemModel.getDefaultValue(),","));
			}
			((SelectItemModelEntity)entity).setReferenceList(setItemModelByListModel(itemModel));

		}else if(entity instanceof RowItemModelEntity){
			List<ItemModelEntity> rowList = new ArrayList<>() ;
			for(ItemModel rowItemModel : itemModel.getItems()) {
				rowList.add(wrap(rowItemModel));
			}
			((RowItemModelEntity) entity).setItems(rowList);
		}else if(entity instanceof SubFormItemModelEntity){
			List<ItemModel> rowItemModels = itemModel.getItems();
			List<SubFormRowItemModelEntity> rowItemModelEntities = new ArrayList<>();
			for(ItemModel rowItemModelEntity : rowItemModels) {
				SubFormRowItemModelEntity subFormRowItemModelEntity = new SubFormRowItemModelEntity();
				BeanUtils.copyProperties(rowItemModelEntity, subFormRowItemModelEntity, new String[] {"items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
				List<ItemModelEntity> rowItemList = new ArrayList<>();
				for(ItemModel childrenItem : rowItemModelEntity.getItems()) {
					rowItemList.add(wrap(childrenItem));
				}
				subFormRowItemModelEntity.setItems(rowItemList);
				rowItemModelEntities.add(subFormRowItemModelEntity);
			}
			((SubFormItemModelEntity) entity).setItems(rowItemModelEntities);
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
				itemSelectOption.setItemModel(entity);
				itemSelectOption.setLabel(option.getLabel());
				itemSelectOption.setValue(option.getValue());
				options.add(itemSelectOption);
			}
			entity.setOptions(options);
		}

		return entity;
	}

	private void setColumnModel(ItemModelEntity entity, ItemModel itemModel){
		if(itemModel.getColumnModel() == null){
			entity.setColumnModel(null);
		}else{
			ColumnModelEntity columnModelEntity = columnModelService.query().filterEqual("columnName", itemModel.getColumnModel().getColumnName()).filterEqual("dataModel.tableName", itemModel.getColumnModel().getTableName()).unique();
			entity.setColumnModel(columnModelEntity);
		}
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
		//activityInfoEntity.setId(activityInfo.getId());
		activityInfoEntity.setName(activityInfo.getName());
		activityInfoEntity.setActivityId(activityInfo.getActivityId());
		activityInfoEntity.setActivityName(activityInfo.getActivityName());
		activityInfoEntity.setVisible(activityInfo.isVisible());
		activityInfoEntity.setReadonly(activityInfo.isReadonly());
		return activityInfoEntity;
	}

	private Page<FormModel> toDTO(Page<FormModelEntity> entities) throws InstantiationException, IllegalAccessException {
		Page<FormModel> formModels = Page.get(entities.getPage(), entities.getPagesize());
		formModels.data(entities.getTotalCount(), toDTO(entities.getResults()));
		return formModels;
	}

	private List<FormModel> toDTO(List<FormModelEntity> entities) throws InstantiationException, IllegalAccessException {
		List<FormModel> formModels = new ArrayList<FormModel>();
		for (FormModelEntity entity : entities) {
			formModels.add(toDTO(entity));
		}
		return formModels;
	}

	private FormModel toDTO(FormModelEntity entity) throws InstantiationException, IllegalAccessException {
		FormModel formModel = new FormModel();
		BeanUtils.copyProperties(entity, formModel, new String[] {"items","dataModels","permissions","submitChecks"});
		if(entity.getDataModels() != null && entity.getDataModels().size() > 0){
			List<DataModel> dataModelList = new ArrayList<>();
			List<DataModelEntity> dataModelEntities = entity.getDataModels();
			for(DataModelEntity dataModelEntity : dataModelEntities){
				DataModel  dataModel = new DataModel();
				BeanUtils.copyProperties(dataModelEntity, dataModel, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
				dataModelList.add(dataModel);
			}
			formModel.setDataModels(dataModelList);
		}

		return formModel;
	}

	private FormModel toDTODetail(FormModelEntity entity)  {
        FormModel formModel = new FormModel();
		BeanUtils.copyProperties(entity, formModel, new String[] {"items","dataModels","permissions","submitChecks"});
		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			List<ItemModelEntity> itemModelEntities = entity.getItems() == null || entity.getItems().size() < 2 ? entity.getItems() : entity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for (ItemModelEntity itemModelEntity : itemModelEntities) {
				items.add(toDTO(itemModelEntity));
			}
			formModel.setItems(items);
		}

		if(entity.getSubmitChecks() != null){
            List<FormSubmitCheckModel> submitCheckModels = new ArrayList<>();
            for(FormSubmitCheckInfo info : entity.getSubmitChecks()){
                FormSubmitCheckModel checkModel = new FormSubmitCheckModel();
                BeanUtils.copyProperties(info, checkModel, new String[] {"formModel"});
				FormModel perimissionFormModel = new FormModel();
				BeanUtils.copyProperties(entity, perimissionFormModel, new String[] {"items","process","dataModels","permissions","submitChecks"});
                checkModel.setFormModel(perimissionFormModel);
                submitCheckModels.add(checkModel);
            }
			List<FormSubmitCheckModel> formSubmitCheckModels = submitCheckModels.size() < 2 ? submitCheckModels : submitCheckModels.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
            formModel.setSubmitChecks(formSubmitCheckModels);
        }

        if(entity.getPermissions() != null){
            List<ItemPermissionModel> permissionModels = new ArrayList<>();
            for(ItemPermissionInfo info : entity.getPermissions()){
                ItemPermissionModel permissionModel = new ItemPermissionModel();
                BeanUtils.copyProperties(info, permissionModel, new String[] {"formModel","itemModel"});
				FormModel perimissionFormModel = new FormModel();
				BeanUtils.copyProperties(entity, perimissionFormModel, new String[] {"items","process","dataModels","permissions","submitChecks"});
                permissionModel.setFormModel(perimissionFormModel);
                if(info.getItemModel() != null){
                    ItemModel itemModel = new ItemModel();
                    BeanUtils.copyProperties(info.getItemModel(), itemModel, new String[] {"formModel","columnModel","defaultValue","activities","options","items","permission","referenceList"});
                    permissionModel.setItemModel(itemModel);
                }
                permissionModels.add(permissionModel);
            }
            formModel.setPermissions(permissionModels);
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
				dataModelList.add(getDataModel(dataModelEntity));
			}
			formModel.setDataModels(dataModelList);
		}
		return formModel;
	}

	private DataModel getDataModel(DataModelEntity dataModelEntity){
		DataModel  dataModel = new DataModel();
		BeanUtils.copyProperties(dataModelEntity, dataModel, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
		if(dataModelEntity.getMasterModel() != null) {
			DataModelInfo masterModel = new DataModelInfo();
			BeanUtils.copyProperties(dataModelEntity, dataModel, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
			dataModel.setMasterModel(masterModel);
		}
		if(dataModelEntity.getColumns() != null && dataModelEntity.getColumns().size() > 0){
			List<ColumnModel> columnModels = new ArrayList<>();
			for(ColumnModelEntity columnModelEntity : dataModelEntity.getColumns()) {
				ColumnModel columnModel = new ColumnModel();
				BeanUtils.copyProperties(columnModelEntity, columnModel, new String[] {"dataModel","columnReferences"});
				if(columnModelEntity.getColumnReferences() != null && columnModelEntity.getColumnReferences().size() > 0){
					List<ReferenceModel> referenceModelList = new ArrayList<>();
					for(ColumnReferenceEntity referenceEntity : columnModelEntity.getColumnReferences()){
						ReferenceModel referenceModel = new ReferenceModel();
						referenceModel.setReferenceType(referenceEntity.getReferenceType());
						referenceModel.setReferenceTable(referenceEntity.getToColumn().getDataModel().getTableName());
						referenceModel.setReferenceValueColumn(referenceEntity.getToColumn().getColumnName());
						referenceModel.setId(referenceEntity.getId());
						referenceModel.setName(referenceEntity.getName());
						referenceModel.setReferenceMiddleTableName(referenceEntity.getReferenceMiddleTableName());
						referenceModelList.add(referenceModel);
					}
					columnModel.setReferenceTables(referenceModelList);
				}
				columnModels.add(columnModel);
			}
			dataModel.setColumns(columnModels);
		}
		return dataModel;
	}

	private PCFormModel toPCDTO(FormModelEntity entity) {
		PCFormModel formModel = new PCFormModel();
		BeanUtils.copyProperties(entity, formModel, new String[] {"dataModels","items","permissions","submitChecks"});

		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			for (ItemModelEntity itemModelEntity : entity.getItems()) {
				items.add(toDTO(itemModelEntity));
			}
			formModel.setItems(items);
		}

		List<PCDataModel> dataModelList = new ArrayList<>();
		List<ItemModelEntity> itemModelEntities = formModelService.findAllItems(entity);
		Map<String, DataModelEntity> dataModelEntities = new HashMap<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getReferenceList() != null) {
				ListModelEntity listModelEntity = ((ReferenceItemModelEntity) itemModelEntity).getReferenceList();
				if(listModelEntity == null || listModelEntity.getMasterForm() == null){
					continue;
				}
				dataModelEntities.put(listModelEntity.getMasterForm().getId(), listModelEntity.getMasterForm().getDataModels().get(0));
			}
		}
		for(String formId : dataModelEntities.keySet()){
			PCDataModel dataModel = dataModelService.transitionToModel(formId, dataModelEntities.get(formId));
			dataModelList.add(dataModel);
		}
		formModel.setDataModels(dataModelList);
		return formModel;
	}

	@Deprecated
	private PCFormModel toPCDTOold(FormModelEntity entity) throws InstantiationException, IllegalAccessException {
		PCFormModel formModel = BeanUtils.copy(entity, PCFormModel.class, new String[] {"dataModels"});
		List<ReferenceItemModel> pcReferenceItem = new ArrayList<>();
		List<ItemModelEntity> itemListModelEntities = new ArrayList<>();
		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			for (ItemModelEntity itemModelEntity : entity.getItems()) {
				boolean flag = itemModelEntity instanceof ReferenceItemModelEntity || itemModelEntity instanceof SubFormItemModelEntity
						|| itemModelEntity instanceof SubFormRowItemModelEntity;
				if(!flag) {
					items.add(toDTO(itemModelEntity));
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

	private ItemModel toDTO(ItemModelEntity entity)  {
		//TODO 根据模型找到对应的参数
		ItemModel itemModel = new ItemModel();
		BeanUtils.copyProperties(entity, itemModel, new String[]{"formModel", "columnModel", "activities", "options", "permission","items","parentItem","referenceList"});

		if(entity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) entity).getItemModelIds() != null){
			List<String> resultList= new ArrayList<>(Arrays.asList(((ReferenceItemModelEntity) entity).getItemModelIds().split(",")));
			itemModel.setItemModelList(resultList);
			String referenceItemId = ((ReferenceItemModelEntity) entity).getReferenceItemId();
			if(referenceItemId != null){
				ItemModelEntity itemModelEntity = itemModelService.get(referenceItemId);
				itemModel.setReferenceItemName(itemModelEntity == null ? null : itemModelEntity.getName());
			}
			String referenceFormId = ((ReferenceItemModelEntity) entity).getReferenceFormId();
			if(referenceFormId != null){
				FormModelEntity formModelEntity = formModelService.get(referenceFormId);
				itemModel.setReferenceFormName(formModelEntity == null ? null : formModelEntity.getName());
			}
		}else if(entity instanceof SelectItemModelEntity){
			String defaultValue = ((SelectItemModelEntity) entity).getDefaultReferenceValue();
			if( defaultValue != null && !StringUtils.isEmpty(defaultValue)) {
				itemModel.setDefaultValue(Arrays.asList(defaultValue.split(",")));
			}
			itemModel.setReferenceList(getItemModelByEntity(entity));

			if(((SelectItemModelEntity) entity).getReferenceDictionaryId() != null){
				DictionaryEntity dictionaryEntity = dictionaryService.get(((SelectItemModelEntity) entity).getReferenceDictionaryId());
				itemModel.setReferenceDictionaryName(dictionaryEntity == null ? null : dictionaryEntity.getName());
			}

			if(((SelectItemModelEntity) entity).getReferenceDictionaryItemId() != null){
				DictionaryItemEntity dictionaryItemEntity = dictionaryService.getDictionaryItemById(((SelectItemModelEntity) entity).getReferenceDictionaryItemId());
				if(dictionaryItemEntity != null && dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0 ) {
					List<DictionaryItemModel> dictionaryItemModels = new ArrayList<>();
					for(DictionaryItemEntity itemEntity : dictionaryItemEntity.getChildrenItem()) {
						DictionaryItemModel dictionaryItemModel = new DictionaryItemModel();
						org.springframework.beans.BeanUtils.copyProperties(itemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});

						if(itemEntity.getDictionary() != null){
							dictionaryItemModel.setDictionaryId(itemEntity.getDictionary().getId());
						}

						if(itemEntity.getParentItem() != null){
							dictionaryItemModel.setParentItemId(itemEntity.getParentItem().getId());
						}
						dictionaryItemModels.add(dictionaryItemModel);
					}
					itemModel.setReferenceDictionaryItemList(dictionaryItemModels);
				}
			}

		}else if(entity instanceof RowItemModelEntity){
			List<ItemModel> rows = new ArrayList<>();
			List<ItemModelEntity> rowList = ((RowItemModelEntity) entity).getItems();
			List<ItemModelEntity> itemModelEntities = rowList == null || rowList.size() < 2 ? rowList : rowList.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			for(ItemModelEntity itemModelEntity : itemModelEntities) {
				rows.add(toDTO(itemModelEntity));
			}
			itemModel.setItems(rows);
		}else if(entity instanceof SubFormItemModelEntity){
			List<ItemModel> subFormRows = new ArrayList<>();
			List<SubFormRowItemModelEntity> rowItemModelEntities = ((SubFormItemModelEntity) entity).getItems();

			List<SubFormRowItemModelEntity> subFormRowItemModelEntities = rowItemModelEntities == null || rowItemModelEntities.size() < 2 ? rowItemModelEntities : rowItemModelEntities.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

			for(SubFormRowItemModelEntity rowItemModelEntity : subFormRowItemModelEntities) {
				ItemModel subFormRowItem = new ItemModel();
				BeanUtils.copyProperties(rowItemModelEntity, subFormRowItem, new String[] {"formModel","columnModel","activities","options","items","itemModelIds"});
				List<ItemModel> rows = new ArrayList<>();
				List<ItemModelEntity> itemModelEntities = rowItemModelEntity.getItems() == null || rowItemModelEntity.getItems().size() < 2 ? rowItemModelEntity.getItems() : rowItemModelEntity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
				for(ItemModelEntity childrenItem : itemModelEntities) {
					rows.add(toDTO(childrenItem));
				}
				subFormRowItem.setItems(rows);
				subFormRows.add(subFormRowItem);
			}
			itemModel.setItems(subFormRows);
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
				options.add(new Option(optionEntity.getId(), optionEntity.getLabel(), optionEntity.getValue()));
			}
			itemModel.setOptions(options);
		}
		return itemModel;
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
