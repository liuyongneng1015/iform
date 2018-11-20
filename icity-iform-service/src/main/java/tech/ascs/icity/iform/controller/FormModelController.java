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

	@Override
	public List<FormModel> list(@RequestParam(name="name", defaultValue="") String name) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", CommonUtils.convertParamOfFuzzySearch(name));
			}
			List<FormModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public Page<FormModel> page(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name="page", defaultValue="1") int page, @RequestParam(name="pagesize", defaultValue="10") int pagesize) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", CommonUtils.convertParamOfFuzzySearch(name));
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
			return toDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public IdEntity saveFormModel(@RequestBody FormModel formModel) {
		FormModelEntity oldEntity = new FormModelEntity();
		if (StringUtils.hasText(formModel.getId())) {
			oldEntity = formModelService.find(formModel.getId());
		}
		if (StringUtils.hasText(formModel.getId())) {
			saveDataModel(formModel, oldEntity);
		}

		BeanUtils.copyProperties(formModel, oldEntity, new String[] {"indexes","dataModels"});

		return new IdEntity(formModelService.save(oldEntity).getId());
	}

	//删除或者保存旧的数据建模
	private void saveDataModel(FormModel formModel, FormModelEntity oldEntity){
		//新的
		List<String> newDataModelIds = new ArrayList<>();
		List<DataModelEntity> newAddDataModel = new ArrayList<>();
		if(formModel.getDataModels() != null ) {
			for(DataModel dataModel : formModel.getDataModels()) {
				if(!dataModel.isNew()) {
					newDataModelIds.add(dataModel.getId());
					newAddDataModel.add(dataModelService.find(dataModel.getId()));
				}else {
					DataModelEntity dataModelEntity = new DataModelEntity();
					BeanUtils.copyProperties(dataModel, DataModelEntity.class,new String[]{});
					dataModelService.save(dataModelEntity);
					newAddDataModel.add(dataModelEntity);
				}
			}
		}

		List<DataModelEntity> oldDataModelEntities = oldEntity.getDataModels();
		for(DataModelEntity dataModelEntity : oldDataModelEntities){
			if(!newDataModelIds.contains(dataModelEntity.getId())){
				oldDataModelEntities.remove(dataModelEntity);
			}
		}
		oldDataModelEntities.addAll(newAddDataModel);
		formModelService.save(oldEntity);
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
		formModelService.deleteById(id);
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
		String formModelName = ((ReferenceItemModelEntity) itemModelEntity).getReferenceTable();
		if(!(itemModelEntity instanceof  ReferenceItemModelEntity) || formModelName== null){
			throw new IFormException(404, "【" + itemModelId + "】控件不是关联类型");
		}
		List<String> stringList = Arrays.asList(((ReferenceItemModelEntity) itemModelEntity).getItemModelList().split(","));
		FormModelEntity formModelEntity = formModelService.findUniqueByProperty("name", formModelName);
		if (formModelEntity == null) {
			throw new IFormException(404, "【" + formModelName + "】表单模型不存在");
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

	private FormModelEntity wrap(FormModel formModel) throws InstantiationException, IllegalAccessException {
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels"});

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


		BeanUtils.copyProperties(masterDataModel, masterDataModelEntity, new String[]{"masterModel", "slaverModels", "columns", "indexes" });
		//创建获取主键未持久化到数据库
		ColumnModelEntity masterIdColumnEntity = columnModelService.saveColumnModelEntity(masterDataModelEntity, "id");
		//创建获取关联字段未持久化到数据库
		ColumnModelEntity referenceIdColumnEntity = null;
		if(masterDataModelEntity.getMasterModel() != null) {
			referenceIdColumnEntity = columnModelService.saveColumnModelEntity(masterDataModelEntity, "master_id");
		}
		if(masterDataModelEntity.getModelType() == DataModelType.Single && dataModels.size() > 1) {
			masterDataModelEntity.setModelType(DataModelType.Master);
		}
		masterDataModelEntity.setSynchronized(false);
		masterDataModelEntity.getColumns().add(masterIdColumnEntity);
		if(referenceIdColumnEntity != null){
			masterDataModelEntity.getColumns().add(referenceIdColumnEntity);
		}

		//设置数据模型行
		setDataModelEntityColumns(masterDataModel, masterDataModelEntity);

		if(!masterDataModel.getColumns().isEmpty()){
			//masterDataModelEntity.setColumns(saveColumnModelEntity( masterDataModelEntity, masterDataModel));
		}

		List<DataModelEntity> slaverDataModelEntities = new ArrayList<>();
		for(DataModel dataModel : dataModels) {
			if (dataModel.getMasterModel() != null) {
				DataModelEntity slaverDataModelEntity = new DataModelEntity();
				//创建关联字段
				boolean dataFlag = dataModel.isNew();
				if (!dataFlag) {
					slaverDataModelEntity = dataModelService.find(dataModel.getId());
				}
				//创建获取主键未持久化到数据库
				ColumnModelEntity idColumnReference = columnModelService.saveColumnModelEntity(slaverDataModelEntity, "id");
				//创建获取关联字段未持久化到数据库
				ColumnModelEntity columnReference = columnModelService.saveColumnModelEntity(slaverDataModelEntity, "master_id");

				//关联关系未持久化到数据库
				columnModelService.saveColumnReferenceEntity(masterIdColumnEntity, columnReference, ReferenceType.OneToMany);

				slaverDataModelEntity.setModelType(DataModelType.Slaver);
				slaverDataModelEntity.setSynchronized(false);
				slaverDataModelEntity.setMasterModel(masterDataModelEntity);
				BeanUtils.copyProperties(dataModel, slaverDataModelEntity, new String[]{"masterModel","slaverModels","columns","indexes"});

				//设置数据模型行
				setDataModelEntityColumns(dataModel, slaverDataModelEntity);
				if(!dataModel.getColumns().isEmpty()){
					//slaverDataModelEntity.setColumns(saveColumnModelEntity( slaverDataModelEntity, dataModel));
				}
				slaverDataModelEntity.getColumns().add(idColumnReference);
				slaverDataModelEntity.getColumns().add(columnReference);
				slaverDataModelEntities.add(slaverDataModelEntity);
			}
		}
		masterDataModelEntity.setSlaverModels(slaverDataModelEntities);

		//设置数据模型结构了
		List<DataModelEntity> dataModelEntities = new ArrayList<>();
		dataModelEntities.add(masterDataModelEntity);
		entity.setDataModels(dataModelEntities);

		dataModelService.save(masterDataModelEntity);


		List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();
		for (ItemModel itemModel : formModel.getItems()) {
			ItemModelEntity itemModelEntity = wrap(itemModel);
			itemModelEntity.setFormModel(entity);
			items.add(itemModelEntity);
		}
		entity.setItems(items);
		return entity;
	}

	//设置数据模型行
	private void setDataModelEntityColumns(DataModel newDataModel, DataModelEntity oldDataModelEntity){
		//待更新的行
		List<String> newColumnIds = new ArrayList<>();
		//待保存的行
		List<ColumnModelEntity> saveModelEntities = new ArrayList<ColumnModelEntity>();
		for(ColumnModel newColumnModel : newDataModel.getColumns()){
			if(newColumnModel.getId() != null){
				newColumnIds.add(newColumnModel.getId());
			}
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
				if("id".equals(columnModelEntity.getColumnName()) || ("master_id".equals(columnModelEntity.getColumnName()) && newDataModel.getMasterModel() != null)){
					continue;
				}
				oldDataModelEntity.getColumns().remove(columnModelEntity);
				i--;
				ItemModelEntity itemModelEntity = itemModelService.findUniqueByProperty("columnModel.id",columnModelEntity.getId());
				if(itemModelEntity != null) {
					itemModelEntity.setColumnModel(null);
					itemModelService.save(itemModelEntity);
				}
				columnModelService.delete(columnModelEntity);
			}
		}

		for(ColumnModel columnModel : newDataModel.getColumns()){
			ColumnModelEntity oldColumnModelEntity = setColumn(columnModel);
			oldColumnModelEntity.getColumnReferences().clear();
			if(columnModel.getColumnReferences() != null){
				for(ReferenceModel columnReferenceEntity : columnModel.getColumnReferences()){
					columnModelService.saveColumnReferenceEntity(oldColumnModelEntity, setColumn(columnReferenceEntity.getToColumn()), columnReferenceEntity.getReferenceType());
				}
			}
			saveModelEntities.add(oldColumnModelEntity);
		}
		oldDataModelEntity.getColumns().addAll(saveModelEntities);
	}

	private ColumnModelEntity setColumn(ColumnModel columnModel){
		ColumnModelEntity oldColumnModelEntity = new ColumnModelEntity();
		if(!columnModel.isNew()) {
			 oldColumnModelEntity = columnModelService.get(columnModel.getId());
		}
		BeanUtils.copyProperties(columnModel, oldColumnModelEntity, new String[]{"dataModel", "columnReferences"});
		if(columnModel.getDataModel() != null){
			DataModelEntity dataModelEntity = new DataModelEntity();
			if(!columnModel.getDataModel().isNew()) {
				dataModelEntity = dataModelService.get(columnModel.getDataModel().getId());
			}
			BeanUtils.copyProperties(columnModel.getDataModel(), dataModelEntity, new String[]{"masterModel", "slaverModels", "columns", "indexes" });
			oldColumnModelEntity.setDataModel(dataModelEntity);
		}
		if(columnModel.getColumnReferences() != null){
			//TODO 关联
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

	private ItemModelEntity wrap(ItemModel itemModel) throws InstantiationException, IllegalAccessException {

		//TODO 根据类型映射对应的item
		ItemModelEntity entity = getItemModelEntity(itemModel);
		if(itemModel.getId() != null){
			entity = itemModelService.find(itemModel.getId());
		}
		//需要保持column
		BeanUtils.copyProperties(itemModel, entity, new String[] {"formModel","dataModel", "columnReferences", "activities","options"});

		if(itemModel.getColumnModel() == null){
			entity.setColumnModel(null);
		}

		if(entity instanceof ReferenceItemModelEntity){
			((ReferenceItemModelEntity)entity).setReferenceList(setItemModelByListModel(entity));
		}else if(entity instanceof SelectItemModelEntity){
			((SelectItemModelEntity)entity).setListModel(setItemModelByListModel(entity));
		}else if(entity instanceof RowItemModelEntity){
			List<ItemModelEntity> rowList = ((RowItemModelEntity) entity).getItems();
			for(ItemModelEntity itemModelEntity : rowList) {
				if(entity instanceof ReferenceItemModelEntity) {
					((ReferenceItemModelEntity) entity).setReferenceList(setItemModelByListModel(itemModelEntity));
				}
			}
		}else if(entity instanceof SubFormItemModelEntity){
			List<SubFormRowItemModelEntity> rowItemModelEntities = ((SubFormItemModelEntity) entity).getItems();
			for(SubFormRowItemModelEntity rowItemModelEntity : rowItemModelEntities) {
				for(ItemModelEntity childrenItem : rowItemModelEntity.getItems()) {
					if(childrenItem instanceof ReferenceItemModelEntity) {
						((ReferenceItemModelEntity) entity).setReferenceList(setItemModelByListModel(childrenItem));
					}
				}
			}
		}

		List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();
		if (itemModel.getActivities() != null) {
			for (ActivityInfo activityInfo : itemModel.getActivities()) {
				ItemActivityInfo activityInfoEntity = wrap(activityInfo);
				activityInfoEntity.setItemModel(entity);
				activities.add(activityInfoEntity);
			}
		}
		entity.setActivities(activities);

		if (itemModel.getOptions() != null) {
			for (ItemSelectOption option : entity.getOptions()) {
				option.setItemModel(entity);
			}
		}

		return entity;
	}

	private ItemModelEntity getItemModelEntity(ItemModel itemModel){
		ItemModelEntity entity = null;
		switch (itemModel.getType()){
			case Image :
				entity = new FileItemModelEntity();
				break;
			case  Attachment:
				entity = new FileItemModelEntity();
				break;
			case  Select:
				entity = new SelectItemModelEntity();
				break;
			case  RadioGroup:
				entity = new SelectItemModelEntity();
				break;
			case  CheckboxGroup:
				entity = new SelectItemModelEntity();
				break;
			case  DatePicker:
				entity = new TimeItemModelEntity();
				break;
			case  SubForm:
				entity = new SubFormItemModelEntity();
				break;
			case  ReferenceList:
				entity = new ReferenceItemModelEntity();
				break;
			case  ReferenceLabel:
				entity = new ReferenceItemModelEntity();
				break;
			case  RowItem:
				entity = new SubFormRowItemModelEntity();
				break;
			case  Row:
				entity = new RowItemModelEntity();
				break;
			default:
				entity = new ItemModelEntity();
				break;
		}
		return entity;
	}

	//关联的列表模型
	private ListModelEntity setItemModelByListModel(ItemModelEntity itemModelEntity){
		if(itemModelEntity != null && ((ReferenceItemModelEntity)itemModelEntity).getReferenceList() != null){
			return listModelService.find(((ReferenceItemModelEntity)itemModelEntity).getReferenceList().getId());
		}
		return null;
	}

	private ItemActivityInfo wrap(ActivityInfo activityInfo) {
		ItemActivityInfo activityInfoEntity = new ItemActivityInfo();
		activityInfoEntity.setActivityId(activityInfo.getId());
		activityInfoEntity.setActivityName(activityInfo.getName());
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
		FormModel formModel = BeanUtils.copy(entity, FormModel.class, new String[] {"items"});
		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			for (ItemModelEntity itemModelEntity : entity.getItems()) {
				items.add(toDTO(itemModelEntity));
			}
			formModel.setItems(items);
		}
		return formModel;
	}

	private PCFormModel toPCDTO(FormModelEntity entity) throws InstantiationException, IllegalAccessException {
		PCFormModel formModel = BeanUtils.copy(entity, PCFormModel.class, new String[] {"dataModels","items"});
		List<DataModel> dataModelList = dataModelService.findDataModelByFormId(formModel.getId());
		List<ItemModelEntity> itemModelEntities = formModelService.getAllColumnItems(entity.getItems());
		Set<DataModelEntity> dataModelEntities = new HashSet<DataModelEntity>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			dataModelEntities.add(itemModelEntity.getColumnModel().getDataModel());
		}
		for(DataModelEntity dataModelEntity : dataModelEntities){
			DataModel dataModel = dataModelService.transitionToModel(dataModelEntity);
			dataModel.setModelType(DataModelType.Relevance);
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

	private ItemModel toDTO(ItemModelEntity entity) throws InstantiationException, IllegalAccessException {
		//TODO 根据模型找到对应的参数
		ItemModel itemModel = BeanUtils.copy(entity, ItemModel.class, new String[] {});

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
		activityInfo.setId(entity.getActivityId());
		activityInfo.setName(entity.getActivityName());
		activityInfo.setVisible(entity.isVisible());
		activityInfo.setReadonly(entity.isReadonly());
		
		return activityInfo;		
	}
}
