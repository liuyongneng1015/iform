package tech.ascs.icity.iform.service.impl;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.admin.api.model.ListFormIds;
import tech.ascs.icity.admin.api.model.TreeSelectData;
import tech.ascs.icity.admin.client.ResourceService;
import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ItemModelServiceImpl extends DefaultJPAService<ItemModelEntity> implements ItemModelService {


	private JPAManager<ReferenceItemModelEntity> referenceItemModelEntityManager;


	@Autowired
	private FormModelService formModelService;

	@Autowired
	private FormInstanceServiceEx formInstanceServiceEx;

	@Autowired
	private DictionaryModelService dictionaryModelService;

	@Autowired
	private DictionaryDataService dictionaryDataService;

	@Autowired
	private ListModelService listModelService;

	@Autowired
	private ColumnModelService columnModelService;

	public ItemModelServiceImpl() {
		super(ItemModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		referenceItemModelEntityManager = getJPAManagerFactory().getJPAManager(ReferenceItemModelEntity.class);
	}


	@Override
	public List<ReferenceItemModelEntity> findRefenceItemByFormModelId(String formModelId) {
		return referenceItemModelEntityManager.query().filterEqual("referenceFormId", formModelId).list();
	}

	@Override
	public ItemModelEntity saveItemModelEntity(FormModelEntity formModelEntity, String itemModelName) {
		List<ItemModelEntity> list = formModelService.findAllItems(formModelEntity);
		if (list != null) {
			for (ItemModelEntity itemModelEntity : list) {
				if(itemModelEntity.getName() == null){
					continue;
				}
				if (itemModelEntity.getName().equals(itemModelName)) {
					return itemModelEntity;
				}
			}
		}
		return saveItem(formModelEntity, itemModelName);
	}

	@Override
	public Map<String, ItemPermissionInfo> findItemPermissionByDisplayTimingType(FormModelEntity formModelEntity, DisplayTimingType displayTimingType) {
		Map<String, ItemPermissionInfo> map = new HashMap<>();
		List<ItemModelEntity> list = formModelService.findAllItems(formModelEntity);
		if (list != null) {
			for (ItemModelEntity itemModelEntity : list) {
				for(ItemPermissionInfo permissionInfo : itemModelEntity.getPermissions()){
					if(permissionInfo.getDisplayTiming() == displayTimingType){
						map.put(itemModelEntity.getId(), permissionInfo);
						break;
					}
				}
			}
		}
		return map;
	}

	private ItemModelEntity saveItem(FormModelEntity formModelEntity, String itemModelName) {
		ItemModelEntity itemModelEntity = new ItemModelEntity();

		itemModelEntity.setProps("{\"props\":{\"type\":\"text\",\"placeholder\":\"\"},\"appProps\":{\"type\":\"text\",\"placeholder\":\"\"},\"typeKey\":\"text\"}");
		itemModelEntity.setType(ItemType.Input);
		itemModelEntity.setSystemItemType(SystemItemType.Input);

		itemModelEntity.setName(itemModelName);
		itemModelEntity.setFormModel(formModelEntity);
		itemModelEntity.setUuid(UUID.randomUUID().toString().replace("-",""));

		formModelEntity.getItems().add(itemModelEntity);
		return itemModelEntity;
	}

	@Override
	public void copyItemModelEntityToItemModel(ItemModelEntity itemModelEntity, ItemModel itemModel) {
		if (itemModelEntity!=null && itemModel!=null) {
			BeanUtils.copyProperties(itemModelEntity, itemModel, new String[]{"formModel","columnModel","activities","options","permissions","items","parentItem","defaultValue","itemModelList","dataModel","columnReferences","referenceTables","referenceList","triggerIds"});
			if(itemModelEntity.getPermissions() != null && itemModelEntity.getPermissions().size() > 0){
				ItemPermissionModel itemPermissionModel = new ItemPermissionModel();
				itemPermissionModel.setId(itemModelEntity.getId());
				itemPermissionModel.setName(itemModelEntity.getName());
				ItemModel itemModel1 = new ItemModel();
				itemModel1.setId(itemModelEntity.getId());
				itemModel1.setName(itemModelEntity.getName());
				for(ItemPermissionInfo itemPermissionInfo : itemModelEntity.getPermissions()){
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
		}

	}

	@Override
	public void copyItemModelToItemModelEntity(ItemModel itemModel, ItemModelEntity itemModelEntity) {
		if (itemModelEntity!=null && itemModel!=null) {
			BeanUtils.copyProperties(itemModel, itemModelEntity, new String[]{"formModel","columnModel","activities","options","permissions","items","parentItem","defaultValue","itemModelList","dataModel","columnReferences","referenceTables","referenceList","triggerIds"});
		}
	}



	//控件实体装模型
	@Override
	public ItemModel toDTO(ItemModelEntity entity, boolean isAnalysisItem, String tableName)  {

		ItemModel itemModel = new ItemModel();
//		BeanUtils.copyProperties(entity, itemModel, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList", "defaultValue", "triggerIds"});
		copyItemModelEntityToItemModel(entity, itemModel);
		Optional.ofNullable(entity.getTriggerIds())
				.filter(StringUtils::hasText)
				.ifPresent(ids -> itemModel.setTriggerIds(Arrays.asList(ids.split(","))));

		if(itemModel.getType() == ItemType.ReferenceLabel || itemModel.getSystemItemType() == SystemItemType.ReferenceLabel){
			itemModel.setTableName(tableName);
		}

		if(itemModel.getSystemItemType() == SystemItemType.ID && !StringUtils.hasText(itemModel.getTypeKey())){
			itemModel.setTypeKey(SystemItemType.ID.getValue());
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

		if(entity instanceof ProcessStatusItemModelEntity && ((ProcessStatusItemModelEntity) entity).getProcessStatus() != null){
			itemModel.setOptions(JSON.parseArray(((ProcessStatusItemModelEntity) entity).getProcessStatus(),Option.class));
		}

		if(entity  instanceof ProcessLogItemModelEntity  && ((ProcessLogItemModelEntity) entity).getDisplayFields() != null){
			itemModel.setDisplayField(Arrays.asList((((ProcessLogItemModelEntity) entity).getDisplayFields()).split(",")));
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


	//设置控件权限
	public void setFormItemActvitiy(List<ItemModel> itemModels, List<Activity> activities){
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


	//控件模型转实体
	public ItemModelEntity wrap(String sourceFormModelId, ItemModel itemModel) {
		if(itemModel.getType() == ItemType.ReferenceLabel){
			if(itemModel.getParentItem() == null || (!StringUtils.hasText(itemModel.getReferenceItemId()) && !StringUtils.hasText(itemModel.getReferenceUuid()))){
				throw  new IFormException(itemModel.getName()+"没有对应关联表单控件或关联控件");
			}
			itemModel.setSelectMode(SelectMode.Attribute);
		}

		ItemModelEntity entity = formModelService.getItemModelEntity(itemModel.getType(), itemModel.getSystemItemType());

		// 设置隐藏, 触发, 赋值
		entity.setEvaluateExpression(itemModel.getEvaluateExpression());
		entity.setHideExpression(itemModel.getHideExpression());
		entity.setTriggerIds(String.join(",", itemModel.getTriggerIds()));

		if(itemModel.getType() == ItemType.CheckboxGroup){
			itemModel.setMultiple(true);
		}else if(itemModel.getType() == ItemType.RadioGroup){
			itemModel.setMultiple(false);
		}
		// 判断是否为创建者控件
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
		copyItemModelToItemModelEntity(itemModel, entity);
		//设置控件字段
		setColumnModel(entity, itemModel);
		entity.setSourceFormModelId(sourceFormModelId);

		if(!(entity instanceof RowItemModelEntity) && !(entity instanceof TabsItemModelEntity)
				&& !(entity instanceof SubFormItemModelEntity) && !(entity instanceof SubFormRowItemModelEntity)
				&& !(entity instanceof ReferenceItemModelEntity) && !(entity instanceof TabPaneItemModelEntity)
				&& !(entity instanceof ReferenceInnerItemModelEntity)
				&& entity.getType() != ItemType.Label && entity.getSystemItemType() != SystemItemType.ProcessStatus
				&& entity.getSystemItemType() != SystemItemType.ProcessPrivateStatus
				&& entity.getType() != ItemType.ProcessLog  && entity.getColumnModel() == null){
			throw  new IFormException("控件"+entity.getName()+"没有对应字段");
		}
		//TODO 需要单独预处理的控件
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
		}else if (entity instanceof ReferenceInnerItemModelEntity) {
			setReferenceInnerItemModel(itemModel, (ReferenceInnerItemModelEntity) entity);
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

		List<ItemSelectOption> options = null;
		if (itemModel.getOptions() != null) {
			options = new ArrayList<>();
			if (!(entity instanceof ProcessStatusItemModelEntity)) {
				for (Option option : itemModel.getOptions()) {
					ItemSelectOption itemSelectOption = new ItemSelectOption();
					BeanUtils.copyProperties(option, itemSelectOption, new String[]{"itemModel"});
					itemSelectOption.setItemModel(entity);
					options.add(itemSelectOption);
				}
				entity.setOptions(options);
			} else {
				String processStatus = String.valueOf(JSON.toJSON(itemModel.getOptions()));
				((ProcessStatusItemModelEntity) entity).setProcessStatus(processStatus);
			}
		}
		if(entity instanceof SelectItemModelEntity ){
			if(options != null && options.size() > 0) {
				((SelectItemModelEntity) entity).setSelectReferenceType(SelectReferenceType.Fixed);
			}else if(((SelectItemModelEntity) entity).getReferenceDictionaryId() != null){
				((SelectItemModelEntity) entity).setSelectReferenceType(SelectReferenceType.Dictionary);
			}
		}
		//控件和字段的输入类型和字段类型校验
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

		if(entity.getType() == ItemType.ProcessLog && itemModel.getDisplayField() != null){
			List<String> list = itemModel.getDisplayField();
			((ProcessLogItemModelEntity)entity).setDisplayFields(String.join(",", list));
		}

		return entity;
	}

	//更新控件实体
	@Override
	public void setItemModelEntity(FormModel formModel, ItemModel itemModel, FormModelEntity entity, List<ItemModelEntity> items,
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

	private ItemActivityInfo wrap(ActivityInfo activityInfo) {
		ItemActivityInfo activityInfoEntity = new ItemActivityInfo();
		BeanUtils.copyProperties(activityInfo, activityInfoEntity, new String[]{"itemModel"});
		return activityInfoEntity;
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
				if (result != null) {
					entity.setDefaultValue(String.join(",", result.stream().map(item->item.getId()).collect(Collectors.toList())));
				}else {
					entity.setDefaultValue(null);
				}
			} else {
				entity.setDefaultValue(null);
			}
		}else if (defaultValue!=null && defaultValue instanceof String && !StringUtils.isEmpty(defaultValue)) {
			List<TreeSelectData> result = formInstanceServiceEx.getTreeSelectData(itemModel.getDataSource(),  new String[] {(String)defaultValue});
			if (result == null || result.size() < 1) {
				entity.setDefaultValue(null);
			}else {
				entity.setDefaultValue(itemModel.getDefaultValue().toString());
			}
		}else{
			entity.setDefaultValue(null);
		}
	}




	/**
	 * 设置 ReferenceInnerItemModelEntity 内的id, 前端传过来的是uuid, 这里会把外部表单的uuid转成id存放
	 * @param itemModel 控件模型
	 * @param entity 控件实体
	 */
	private void setReferenceInnerItemModel(ItemModel itemModel, ReferenceInnerItemModelEntity entity) {
		entity.setReferenceItemUuid(itemModel.getReferenceItemUuid());
		String refenceItemId = findUniqueByProperty("uuid", entity.getReferenceItemUuid()).getId();
		String refenceOutsideItemId;
		if (StringUtils.hasText(entity.getReferenceOutsideItemUuid())){
			refenceOutsideItemId = findUniqueByProperty("uuid", entity.getReferenceOutsideItemUuid()).getId();
		}else {
			ItemModelEntity itemModelEntity = formModelService.find(itemModel.getReferenceOutsideFormId()).getItems().stream().filter(modelEntity -> "id".equals(modelEntity.getName())).findAny().get();
			refenceOutsideItemId = itemModelEntity.getId();
			entity.setReferenceOutsideItemUuid(itemModelEntity.getUuid());
		}
		entity.setReferenceItemId(refenceItemId);
		entity.setReferenceOutsideItemId(refenceOutsideItemId);
	}

	private ItemModelEntity getParentItemModel(ItemModel itemModel){
		ItemModelEntity parentItemModel = formModelService.getItemModelEntity(itemModel.getType(), itemModel.getSystemItemType());
		copyItemModelToItemModelEntity(itemModel, parentItemModel);
		ColumnModelEntity columnModel = new ColumnModelEntity();
		columnModel.setColumnName(itemModel.getColumnName());
		DataModelEntity dataModelEntity = new DataModelEntity();
		dataModelEntity.setTableName(itemModel.getTableName());
		columnModel.setDataModel(dataModelEntity);
		parentItemModel.setColumnModel(columnModel);
		return parentItemModel;
	}


	private void setTabsItemModelEntity(ItemModel itemModel, String sourceFormModelId, TabsItemModelEntity entity){
		List<ItemModel> tabsItemModels = itemModel.getItems();
		List<TabPaneItemModelEntity> list = new ArrayList<>();
		if(tabsItemModels != null) {
			for (ItemModel itemModel1 : tabsItemModels){
				TabPaneItemModelEntity tabPaneItemModelEntity = new TabPaneItemModelEntity();
				copyItemModelToItemModelEntity(itemModel1, tabPaneItemModelEntity);
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
		for(ItemModel rowItemModel : rowItemModels) {
			SubFormRowItemModelEntity subFormRowItemModelEntity = new SubFormRowItemModelEntity();
			copyItemModelToItemModelEntity(rowItemModel, subFormRowItemModelEntity);
			List<ItemModelEntity> rowItemList = new ArrayList<>();
			for(ItemModel childrenItem : rowItemModel.getItems()) {
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
			throw new IFormException("控件"+itemModel.getName()+"未找到对应的分类或数据分类");
		}

		if(itemModel.getDictionaryValueType() == DictionaryValueType.Linkage && itemModel.getParentItem() != null){
			ItemModel parentItemModel = itemModel.getParentItem();
			parentItemModel.setType(ItemType.Select);
			selectItemModelEntity.setParentItem((SelectItemModelEntity) getParentItemModel(parentItemModel));
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

	private void setReferenceItemModel(ReferenceItemModelEntity entity, String sourceFormModelId, ItemModel itemModel){
		entity.setSourceFormModelId(sourceFormModelId);
		if(StringUtils.hasText(itemModel.getUuid())){
			ItemModelEntity itemModelEntity = findUniqueByProperty("uuid", itemModel.getUuid());
			if(itemModelEntity != null && !itemModelEntity.getId().equals(itemModel.getId())){
				throw  new IFormException("关联控件【"+itemModel.getName()+"】UUID重复了");
			}
		}

		if(StringUtils.hasText(itemModel.getItemUuids())){
			ItemModelEntity itemModelEntity = findUniqueByProperty("uuid", itemModel.getItemUuids());
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

	// 判断是否存在对应的ColumnMode, 不存在则新建字段
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
//			BeanUtils.copyProperties(tabPaneItemModelEntity, itemModel1, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList","triggerIds"});
			copyItemModelEntityToItemModel(tabPaneItemModelEntity, itemModel1);
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
			copyItemModelEntityToItemModel(rowItemModelEntity, subFormRowItem);
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
			if(SelectDataSourceType.DictionaryData == itemModel.getSelectDataSourceType()) {
				DictionaryDataEntity dictionaryEntity = dictionaryDataService.find(((SelectItemModelEntity) entity).getReferenceDictionaryId());
				itemModel.setReferenceDictionaryName(dictionaryEntity == null ? null : dictionaryEntity.getName());
			}else if(SelectDataSourceType.DictionaryModel == itemModel.getSelectDataSourceType()) {
				DictionaryModel dictionaryModel = dictionaryModelService.getDictionaryById(((SelectItemModelEntity) entity).getReferenceDictionaryId());
				itemModel.setReferenceDictionaryName(dictionaryModel == null ? null : dictionaryModel.getName());
			}
		}

		if(((SelectItemModelEntity) entity).getParentItem() != null){
			if(!isAnalysisItem) {
				ItemModel parentItemModel = new ItemModel();
//				BeanUtils.copyProperties(((SelectItemModelEntity) entity).getParentItem(), parentItemModel, new String[]{"formModel", "columnModel", "activities", "options", "searchItems", "sortItems", "permissions", "items", "parentItem", "referenceList","triggerIds"});
				copyItemModelEntityToItemModel(((SelectItemModelEntity) entity).getParentItem(), parentItemModel);
				Optional.ofNullable(entity.getTriggerIds())
						.filter(StringUtils::hasText)
						.ifPresent(ids -> parentItemModel.setTriggerIds(Arrays.asList(ids.split(","))));

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
				copyItemModelEntityToItemModel(selectItemModelEntity, chiildItemModel);
				chiildItemModel.setId(selectItemModelEntity.getId());
				chiildItemModel.setReferenceDictionaryId(selectItemModelEntity.getReferenceDictionaryId());

				chiildrenItemModel.add(chiildItemModel);
			}
			itemModel.setItems(chiildrenItemModel);
		}
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
			itemModel.setItemModelList(formModelService.getItemModelList(resultList));
		}
		String referenceItemId = ((ReferenceItemModelEntity) entity).getReferenceItemId();
		if(referenceItemId != null){
			ItemModelEntity itemModelEntity = find(referenceItemId);
			itemModel.setReferenceItemName(itemModelEntity == null ? null : itemModelEntity.getName());
		}
		String referenceFormId = ((ReferenceItemModelEntity) entity).getReferenceFormId();
		if(referenceFormId != null){
			FormModelEntity formModelEntity = formModelService.find(referenceFormId);
			if(formModelEntity != null){
				itemModel.setReferenceFormName(formModelEntity.getName());
				if(formModelEntity.getDataModels() != null && formModelEntity.getDataModels().size() > 0) {
					itemModel.setTableName(formModelEntity.getDataModels().get(0).getTableName());
				}
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
			ItemModelEntity itemModelEntity = find(((ReferenceItemModelEntity) entity).getReferenceItemId());
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
//			BeanUtils.copyProperties(((ReferenceItemModelEntity) entity).getParentItem(), itemModel1, new String[]{"formModel", "columnModel", "activities", "options","searchItems","sortItems", "permissions","items","parentItem","referenceList", "triggerIds"});
			copyItemModelEntityToItemModel(((ReferenceItemModelEntity) entity).getParentItem(), itemModel1);
			if(((ReferenceItemModelEntity) entity).getParentItem() != null && ((ReferenceItemModelEntity) entity).getParentItem().getColumnModel()!=null && ((ReferenceItemModelEntity) entity).getParentItem().getColumnModel().getDataModel() != null) {
				itemModel1.setTableName(((ReferenceItemModelEntity) entity).getParentItem().getColumnModel().getDataModel().getTableName());
				itemModel1.setColumnName(((ReferenceItemModelEntity) entity).getParentItem().getColumnModel().getColumnName());
			}
			itemModel.setParentItem(itemModel1);
			itemModel.setParentItemId(itemModel1.getId());
		}

		if(((ReferenceItemModelEntity) entity).getReferenceList() != null){
			ListModel referenceList = new ListModel();
			BeanUtils.copyProperties(((ReferenceItemModelEntity) entity).getReferenceList(), referenceList, new String[] {"masterForm", "slaverForms", "sortItems", "searchItems", "functions", "displayItems", "quickSearchItems", "appListTemplate", "protalListTemplate"});
			itemModel.setReferenceList(referenceList);
		}
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

	/**
	 * 返回
	 * {
	 *     "item": itemModelEntity,
	 *     "level": int
	 * }
	 */
	// 联动数据解绑查询时，要找到最原始节点的item控件，以及它们之间相隔了多少层来获取对应层数的数据
	@Override
	public Map<String, Object> findLinkageOriginItemModelEntity(String id) {
		ItemModelEntity itemModelEntity = find(id);
		if (itemModelEntity==null || (itemModelEntity instanceof SelectItemModelEntity)==false) {
			return null;
		}
		SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity)itemModelEntity;
		Map<String, Object> map = new HashMap<>();
		map.put("level", 1);
		map.put("item", selectItemModelEntity);
		parentLinkageItemModelEntity(selectItemModelEntity, map);
		return map;
	}

	private void parentLinkageItemModelEntity(SelectItemModelEntity selectItemModelEntity, Map<String, Object> map) {
		if (DictionaryValueType.Linkage != selectItemModelEntity.getDictionaryValueType()) {
			return;
		}
		selectItemModelEntity = selectItemModelEntity.getParentItem();
		if (selectItemModelEntity==null) {
			return;
		}
		map.put("level", (Integer) map.get("level")+1);
		map.put("item", selectItemModelEntity);
		parentLinkageItemModelEntity(selectItemModelEntity, map);
	}
}
