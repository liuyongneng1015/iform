package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

import javax.persistence.*;

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
				query.filterLike("name", "%" + name + "%");
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
	public IdEntity saveFormModel(FormModel formModel) {
		FormModelEntity entity = null;
		try {
			entity = BeanUtils.copy(formModel, FormModelEntity.class, new String[] {"items"});
		} catch (Exception e) {
			throw new IFormException(404, "表单模型保存失败", e);
		}
		if (StringUtils.hasText(formModel.getId())) {
			FormModelEntity oldEntity = formModelService.find(formModel.getId());
			deleteOldDataModel(formModel, oldEntity);
		}
		return new IdEntity(formModelService.save(entity).getId());
	}

	//删除旧的数据建模
	private void deleteOldDataModel(FormModel formModel, FormModelEntity oldEntity){
		List<DataModelEntity> dataModelEntities = oldEntity.getDataModels();
		if(dataModelEntities == null || dataModelEntities.isEmpty()){
			return;
		}
		List<String> newDataModelIds = new ArrayList<>();
		if(formModel.getDataModels() != null && !formModel.getDataModels().isEmpty()) {
			newDataModelIds = formModel.getDataModels().parallelStream().
					map(DataModelInfo::getId).
					collect(Collectors.toList());
		}
		List<String> oldDataModelIds = dataModelEntities.parallelStream().
				map(DataModelEntity::getId).
				collect(Collectors.toList());
		oldDataModelIds.removeAll(newDataModelIds);
		if(!oldDataModelIds.isEmpty()){
			formModelService.deleteById(oldDataModelIds.toArray(new String[oldDataModelIds.size()]));
		}
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
			//TODO 需要处理数据返回给pc端
			return toPCDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	private FormModelEntity wrap(FormModel formModel) throws InstantiationException, IllegalAccessException {
		FormModelEntity entity = BeanUtils.copy(formModel, FormModelEntity.class, new String[] {"items"});
		boolean isNewFlag = entity.isNew();
		List<DataModelEntity> dataModelEntities = entity.getDataModels();
		if(dataModelEntities == null){
			dataModelEntities = new ArrayList<>();
		}
		//TODO 创建一个数据模型
		if(isNewFlag){
			DataModelEntity dataModelEntity = dataModelService.findUniqueByProperty("tableName", formModel.getName());
			if(dataModelEntity != null){
				throw new IFormException("表单名存在了，不要重复添加");
			}
			dataModelEntities.add(createDataModelEntity(formModel.getName(), formModel.getDescription(), DataModelType.Master, null));
		}
		DataModelEntity masterModelEntity = dataModelEntities.get(0);
		Set<DataModelEntity> parentModelEntities = masterModelEntity.getParentsModel();
		Set<DataModelEntity> childrenModelEntities = masterModelEntity.getChildrenModels();
		//创建子数据模型
		for (ItemModel itemModel : formModel.getItems()) {
			ItemModelEntity itemModelEntity = getItemModelEntity(itemModel);
			//TODO 如果是子表单需创建datamodel
			if(itemModelEntity instanceof SubFormItemModelEntity){
				parentModelEntities.add(masterModelEntity);
				childrenModelEntities.add(masterModelEntity);
				DataModelEntity dataModelEntity = dataModelService.findUniqueByProperty("tableName", ((SubFormItemModelEntity) itemModelEntity).getTableName());
				if(dataModelEntity == null) {
					dataModelEntity = createDataModelEntity(((SubFormItemModelEntity) itemModelEntity).getTableName(), itemModelEntity.getName(),DataModelType.Slaver, dataModelEntities);
				}
				childrenModelEntities.add(dataModelEntity);
				parentModelEntities.add(dataModelEntity);
			}else if(itemModelEntity instanceof ReferenceItemModelEntity){
				if(((ReferenceItemModelEntity) itemModelEntity).getSelectMode() != null){
					parentModelEntities.add(masterModelEntity);
					childrenModelEntities.add(masterModelEntity);
					childrenModelEntities.add(itemModelEntity.getColumnModel().getDataModel());
					parentModelEntities.add(itemModelEntity.getColumnModel().getDataModel());
				}
			}
		}

		List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();
		for (ItemModel itemModel : formModel.getItems()) {
			ItemModelEntity itemModelEntity = wrap(itemModel);
			itemModelEntity.setFormModel(entity);
			items.add(itemModelEntity);
		}
		entity.setDataModels(dataModelEntities);
		entity.setItems(items);

		return entity;
	}

	//创建数据模型
	private DataModelEntity createDataModelEntity(String tableName, String description, DataModelType dataModelType, List<DataModelEntity> dataModelEntities){
		DataModelEntity dataModelEntity = new DataModelEntity();
		dataModelEntity.setTableName(tableName);
		dataModelEntity.setDescription(description);
		dataModelEntity.setModelType(dataModelType);
		if(dataModelType == DataModelType.Slaver) {
			dataModelEntity.setMasterModel(getMasterDataModelEntity(dataModelEntities));
		}
		dataModelEntity.setSynchronized(false);
		dataModelService.save(dataModelEntity);
		return dataModelEntity;
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
		//需要保持column
		entity = BeanUtils.copy(itemModel, entity.getClass(), new String[] {"activities"});

		if(entity instanceof ReferenceItemModelEntity){
			((ReferenceItemModelEntity)entity).setListModel(setItemModelByListModel(itemModel));
		}else if(entity instanceof SelectItemModelEntity){
			((SelectItemModelEntity)entity).setListModel(setItemModelByListModel(itemModel));
		}
		//TODO 如果是子表如何处理

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
		ItemModelEntity itemModelEntity = itemModelService.save(entity);
		if(entity instanceof ReferenceItemModelEntity || entity instanceof SubFormItemModelEntity){
			ColumnModelEntity addToEntity = itemModelEntity.getColumnModel();
			ColumnModelEntity columnEntity = columnModelService.findUniqueByProperty("columnName", ((ReferenceItemModelEntity) entity).getReferenceValueColumn());

			List<ColumnReferenceEntity> oldReferenceEntityList = addToEntity.getColumnReferences();
			//正向关联
			ColumnReferenceEntity addFromReferenceEntity = new ColumnReferenceEntity();
			addFromReferenceEntity.setFromColumn(addToEntity);
			addFromReferenceEntity.setToColumn(columnEntity);
			addFromReferenceEntity.setReferenceType(((ReferenceItemModelEntity) entity).getReferenceType());
			oldReferenceEntityList.add(addFromReferenceEntity);

			//反向关联
			ColumnReferenceEntity addToReferenceEntity = new ColumnReferenceEntity();
			addToReferenceEntity.setFromColumn(columnEntity);
			addToReferenceEntity.setToColumn(addToEntity);
			addToReferenceEntity.setReferenceType(ReferenceModel.getToReferenceType(addFromReferenceEntity.getReferenceType()));
			addToEntity.getColumnReferences().add(addToReferenceEntity);
		}

		return itemModelEntity;
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
	private ListModelEntity setItemModelByListModel(ItemModel itemModel){
		if(itemModel != null && itemModel.getReferenceList() != null){
			return listModelService.find(itemModel.getReferenceList().getId());
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
		PCFormModel formModel = BeanUtils.copy(entity, PCFormModel.class, new String[] {"dataModels"});
		DataModelEntity dataModelEntity = entity.getDataModels().get(0);
		List<DataModelEntity> list = new ArrayList<>();
		list.add(dataModelEntity);
		list.addAll(dataModelEntity.getSlaverModels());
		list.addAll(dataModelEntity.getChildrenModels());
		 List<DataModel> dataModels = new ArrayList<DataModel>();
		for(DataModelEntity modelEntity : list){
			dataModels.add(BeanUtils.copy(modelEntity, DataModel.class, new String[] {"slaverModels","masterModel","parentsModel","childrenModels","indexes"}));
		}
		formModel.setDataModels(dataModels);
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
						|| itemModelEntity instanceof RowItemModelEntity;
				if(!flag) {
					items.add(toDTO(itemModelEntity));
				}else if(((ReferenceItemModelEntity)itemModelEntity).getListModel() != null){
					itemListModelEntities.add(itemModelEntity);
				}
			}
			//formModel.setItems(items);
		}
		//关联数据模型
		for(ItemModelEntity itemModelEntity : itemListModelEntities){
			ListModelEntity listModelEntity = ((ReferenceItemModelEntity)itemModelEntity).getListModel();
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
		ItemModel itemModel = BeanUtils.copy(entity.getClass(), ItemModel.class, new String[] {"activities", "options"});

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
