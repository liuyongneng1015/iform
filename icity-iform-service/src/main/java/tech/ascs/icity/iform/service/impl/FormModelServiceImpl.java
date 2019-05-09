package tech.ascs.icity.iform.service.impl;

import java.util.*;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import com.googlecode.genericdao.search.Filter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iflow.client.ProcessService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.utils.BeanUtils;

public class FormModelServiceImpl extends DefaultJPAService<FormModelEntity> implements FormModelService {

	private JPAManager<ItemModelEntity> itemManager;

	private JPAManager<DataModelEntity> dataModelManager;

	private JPAManager<ColumnModelEntity> columnModelManager;

	private JPAManager<ListModelEntity> listModelManager;

	private JPAManager<ItemActivityInfo> itemActivityManager;

	private JPAManager<ItemSelectOption> itemSelectOptionManager;

	private JPAManager<ItemPermissionInfo> itemPermissionManager;

	private JPAManager<FormSubmitCheckInfo> formSubmitCheckManager;

	private JPAManager<FormModelEntity> formModelManager;

	private JPAManager<ListFunction> listFunctionManager;


	private JPAManager<ListSearchItem> listSearchItemManager;

	private JPAManager<ListSortItem> listSortItemManager;

	private JPAManager<QuickSearchEntity> quickSearchEntityManager;

	private JPAManager<BusinessTriggerEntity> businessTriggerManager;

	@Autowired
	ProcessService processService;

	@Autowired
	ColumnModelService columnModelService;

	@Autowired
	DataModelService dataModelService;

    @Autowired
    FormSubmitCheckService formSubmitCheckService;

	@Autowired
	ListModelService listModelService;

	@Autowired
	FormFunctionsService formFunctionsService;

	@Autowired
	JdbcTemplate jdbcTemplate;

	public FormModelServiceImpl() {
		super(FormModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		itemManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
		dataModelManager = getJPAManagerFactory().getJPAManager(DataModelEntity.class);
		columnModelManager = getJPAManagerFactory().getJPAManager(ColumnModelEntity.class);
		listModelManager = getJPAManagerFactory().getJPAManager(ListModelEntity.class);
		itemActivityManager = getJPAManagerFactory().getJPAManager(ItemActivityInfo.class);
		itemSelectOptionManager = getJPAManagerFactory().getJPAManager(ItemSelectOption.class);
		itemPermissionManager = getJPAManagerFactory().getJPAManager(ItemPermissionInfo.class);
		formSubmitCheckManager = getJPAManagerFactory().getJPAManager(FormSubmitCheckInfo.class);
		formModelManager = getJPAManagerFactory().getJPAManager(FormModelEntity.class);
		listFunctionManager = getJPAManagerFactory().getJPAManager(ListFunction.class);
		listSearchItemManager = getJPAManagerFactory().getJPAManager(ListSearchItem.class);
		listSortItemManager = getJPAManagerFactory().getJPAManager(ListSortItem.class);
		quickSearchEntityManager = getJPAManagerFactory().getJPAManager(QuickSearchEntity.class);
		businessTriggerManager = getJPAManagerFactory().getJPAManager(BusinessTriggerEntity.class);
	}

	@Override
	public FormModelEntity save(FormModelEntity entity) {
		validate(entity);
		boolean dataModelUpdateNeeded = dataModelUpdateNeeded(entity);
		if (!entity.isNew()) { // 先删除所有字段然后重建
			//再次查询旧的表单模型
			FormModelEntity old = get(entity.getId());

			//主表数据模型
			DataModelEntity oldDataModelEntity = old.getDataModels().get(0);

			List<DataModelEntity> dataModelEntities = new ArrayList<>();
			dataModelEntities.add(oldDataModelEntity);
			//所以的自身与下级的行
			Map<String, ColumnModelEntity> columnModelEntityMap = new HashMap<String, ColumnModelEntity>();
			Set<ColumnModelEntity> columnModelEntities = new HashSet<>();
			columnModelEntities.addAll(oldDataModelEntity.getColumns());
			if(oldDataModelEntity.getSlaverModels() != null){
				for(DataModelEntity dataModelEntity : oldDataModelEntity.getSlaverModels()){
					columnModelEntities.addAll(dataModelEntity.getColumns());
				}
			}

			//所有字段
			for(ColumnModelEntity columnModelEntity : columnModelEntities){
				columnModelEntityMap.put(columnModelEntity.getDataModel().getTableName() + "_" + columnModelEntity.getColumnName(), columnModelEntity);
			}

			BeanUtils.copyProperties(entity, old, new String[] {"dataModels", "items", "permissions", "submitChecks","functions", "triggeres"});

			List<ItemModelEntity> oldItems = old.getItems();

			Map<String, ItemModelEntity> oldMapItmes = new HashMap<>();
			for(ItemModelEntity itemModelEntity : oldItems){
				oldMapItmes.put(itemModelEntity.getId(), itemModelEntity);
				List<ItemModelEntity> oldItemChildren = getChildRenItemModelEntity(itemModelEntity);
				for(ItemModelEntity itemModelEntity1 : oldItemChildren){
					oldMapItmes.put(itemModelEntity1.getId(), itemModelEntity1);
				}
			}

			//包括所有的新的item(包括子item)
			List<ItemModelEntity> allItems = new ArrayList<>();

			//form直接的新的item
			List<ItemModelEntity> itemModelEntities = new ArrayList<ItemModelEntity>();
			for(int i = 0; i < entity.getItems().size() ; i++) {
				ItemModelEntity paramerItemModelEntity = entity.getItems().get(i);
				paramerItemModelEntity.setOrderNo(i*20);
				ItemModelEntity newItemModelEntity = getNewItemModelEntity(oldMapItmes, columnModelEntityMap, paramerItemModelEntity);
				newItemModelEntity.setFormModel(old);
				//包括所有的item(包括子item)
				allItems.add(newItemModelEntity);
				allItems.addAll(getChildRenItemModelEntity(newItemModelEntity));
				itemModelEntities.add(newItemModelEntity);
			}

			for(ItemModelEntity item : allItems) {
				if (!item.isNew()) {
					if(oldMapItmes.containsKey(item.getId())) {
						oldMapItmes.remove(item.getId());
					}
				}
			}

			//下拉联动或关联动控件
			setParentItem(itemModelEntities);

			old.setItems(itemModelEntities);

			//设置表单功能
			saveFormModelFunctions(old, entity);

			//设置表单业务触发
			saveFormModelTriggeres(old, entity);

			//设计表单校验
			setFormSubmitChecks(old, entity);

			//删除item
			deleteItems(oldDataModelEntity, new ArrayList<>(oldMapItmes.values()));


			//保存表单
			FormModelEntity formModelEntity = doSave(old, dataModelUpdateNeeded);

			List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
			itemModelEntityList.addAll(formModelEntity.getItems());
			for(ItemModelEntity itemModelEntity : formModelEntity.getItems()) {
				itemModelEntityList.addAll(getChildRenItemModelEntity(itemModelEntity));
			}

			setFormItemModelIds(formModelEntity);
			setFormQrCodeItemModelIds(formModelEntity);

			//保存表单建模
			formModelManager.save(formModelEntity);

			//同步数据建模
			dataModelService.sync(formModelEntity.getDataModels().get(0));

			return formModelEntity;
		}
		FormModelEntity formModelEntity = doSave(entity, dataModelUpdateNeeded);
		setFormItemModelIds(formModelEntity);
		setFormQrCodeItemModelIds(formModelEntity);

		//保存表单建模
		formModelManager.save(formModelEntity);

		//同步数据建模
		dataModelService.sync(formModelEntity.getDataModels().get(0));
		return formModelEntity;

	}

	//设置对应数据标识
	private void setFormItemModelIds(FormModelEntity formModelEntity){
		if(StringUtils.isBlank(formModelEntity.getItemUuids())){
			formModelEntity.setItemModelIds(null);
			return;
		}
		Map<String, ItemModelEntity> itemModelEntityMap = getItemModelEntityMap(formModelEntity);
		String[] strings = formModelEntity.getItemUuids().split(",");
		List<String> list = new ArrayList<>();
		for(String str : strings){
			ItemModelEntity itemModelEntity = itemModelEntityMap.get(str);
			if(itemModelEntity != null){
				list.add(itemModelEntity.getId());
			}
		}
		formModelEntity.setItemModelIds(String.join(",", list));

	}

	//查找对应字段控件
	private Map<String, ItemModelEntity> getItemModelEntityMap(FormModelEntity formModelEntity){
		List<ItemModelEntity> itemModelEntityList = new ArrayList<>();
		itemModelEntityList.addAll(formModelEntity.getItems());
		for(ItemModelEntity itemModelEntity : formModelEntity.getItems()) {
			itemModelEntityList.addAll(getChildRenItemModelEntity(itemModelEntity));
		}
		Map<String, ItemModelEntity> itemModelEntityMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity : itemModelEntityList){
			itemModelEntityMap.put(itemModelEntity.getUuid(), itemModelEntity);
		}
		return itemModelEntityMap;
	}

	//设置二维码对应数据标识
	private void setFormQrCodeItemModelIds(FormModelEntity formModelEntity){
		if(StringUtils.isBlank(formModelEntity.getQrCodeItemUuids())){
			formModelEntity.setQrCodeItemModelIds(null);
			return;
		}
		Map<String, ItemModelEntity> itemModelEntityMap = getItemModelEntityMap(formModelEntity);
		String[] strings = formModelEntity.getQrCodeItemUuids().split(",");
		List<String> list = new ArrayList<>();
		for(String str : strings){
			ItemModelEntity itemModelEntity = itemModelEntityMap.get(str);
			if(itemModelEntity != null){
				list.add(itemModelEntity.getId());
			}
		}
		formModelEntity.setQrCodeItemModelIds(String.join(",", list));
	}

	@Override
	public List<ItemModelEntity> getReferenceItemModelList(ReferenceItemModelEntity itemModelEntity){
		List<ItemModelEntity> itemModelEntityList = getAllColumnItems(get(itemModelEntity.getReferenceFormId()).getItems());
		Map<String, ItemModelEntity> uuidItemModelEntityMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity1 : itemModelEntityList){
			if(StringUtils.isNotBlank(itemModelEntity1.getUuid())) {
				uuidItemModelEntityMap.put(itemModelEntity1.getUuid(), itemModelEntity1);
			}
		}
		List<ItemModelEntity> list = new ArrayList<>();
		if(itemModelEntity.getItemUuids() != null){
			String[] strings =  itemModelEntity.getItemUuids().split(",");
			for(String str : strings){
				list.add(uuidItemModelEntityMap.get(str));
			}
		}
		return list;
	}

	private ItemModelEntity getItemModelByItemUUid(FormModelEntity formModelEntity, String uuid){
		List<ItemModelEntity> itemModelEntityList = findAllItems(formModelEntity);
		Map<String, ItemModelEntity> uuidItemModelEntityMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity1 : itemModelEntityList){
			uuidItemModelEntityMap.put(itemModelEntity1.getUuid(), itemModelEntity1);
		}
		return uuidItemModelEntityMap.get(uuid);
	}

	//设置关联关系
	private void setFormSubmitChecks(FormModelEntity formModelEntity, FormModelEntity entity){
		Map<String, FormSubmitCheckInfo> oldMap = new HashMap<>();
		List<FormSubmitCheckInfo> oldSubmitCheck = formModelEntity.getSubmitChecks();
		for(FormSubmitCheckInfo info : oldSubmitCheck){
			oldMap.put(info.getId(), info);
		}
		List<FormSubmitCheckInfo> newSubmitCheck = entity.getSubmitChecks();
		if(newSubmitCheck != null){
			List<FormSubmitCheckInfo> submitCheckInfos = new ArrayList<>();
			for(FormSubmitCheckInfo formSubmitCheckInfo : newSubmitCheck){
				FormSubmitCheckInfo checkInfo = null;
				boolean isNew = formSubmitCheckInfo.isNew();
				if(!isNew){
					checkInfo = oldMap.remove(formSubmitCheckInfo.getId());
				}else{
					checkInfo = new FormSubmitCheckInfo() ;
				}
				BeanUtils.copyProperties(formSubmitCheckInfo, checkInfo, new String[]{"formModel"});
				if(isNew){
					Integer orderNo = formSubmitCheckService.getMaxOrderNo();
					checkInfo.setOrderNo(orderNo == null ? 1 : orderNo + 1);
				}
				checkInfo.setFormModel(formModelEntity);
				submitCheckInfos.add(checkInfo);
			}
			formModelEntity.setSubmitChecks(submitCheckInfos);
		}
		for(String key : oldMap.keySet()){
			formSubmitCheckManager.deleteById(key);
		}
	}


	private  void setParentItem(List<ItemModelEntity> itemModelEntities){
		Map<String, ItemModelEntity> map = new HashMap<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities) {
			map.put(itemModelEntity.getUuid(), itemModelEntity);
			for(ItemModelEntity itemModel : getChildRenItemModelEntity(itemModelEntity)) {
				map.put(itemModel.getUuid(),itemModel);
			}
		}

		for(ItemModelEntity itemModelEntity : itemModelEntities) {
			if(itemModelEntity instanceof SelectItemModelEntity ){
				setSelectItem(map, itemModelEntity);
			}else if(itemModelEntity instanceof ReferenceItemModelEntity ){
				setReferenceItem(map, itemModelEntity);
			}

			for(ItemModelEntity itemModel : getChildRenItemModelEntity(itemModelEntity)) {
				if(itemModel instanceof SelectItemModelEntity){
					setSelectItem(map, itemModel);
				}else if(itemModel instanceof ReferenceItemModelEntity){
					setReferenceItem(map, itemModel);
				}
			}
		}
	}

	//设置关联控件父控件
	private void setSelectItem(Map<String, ItemModelEntity> map, ItemModelEntity itemModel){
		SelectItemModelEntity selectItemModelEntity = ((SelectItemModelEntity) itemModel);
		SelectItemModelEntity parentSelectItem = null;
		if(selectItemModelEntity.getParentItem() != null) {
			parentSelectItem = (SelectItemModelEntity) map.get(selectItemModelEntity.getParentItem().getUuid());
		}
		SelectItemModelEntity oldSelectItem = null;
		if(!selectItemModelEntity.isNew()){
			SelectItemModelEntity selectItemModelEntitys = (SelectItemModelEntity)itemManager.get(selectItemModelEntity.getId());
			oldSelectItem = selectItemModelEntitys.getParentItem();
		}

		if(oldSelectItem != null  && (parentSelectItem == null || parentSelectItem.getColumnModel() == null ||
				!oldSelectItem.getColumnModel().getColumnName().equals(parentSelectItem.getColumnModel().getColumnName())
				|| !oldSelectItem.getColumnModel().getDataModel().getTableName().equals(parentSelectItem.getColumnModel().getDataModel().getTableName()))){
			//旧数据子集
			List<SelectItemModelEntity> list = oldSelectItem.getItems();
			for(int i = 0; i < list.size(); i++){
				SelectItemModelEntity selectItemModelEntity1 = list.get(i);
				if(selectItemModelEntity1.getId().equals(itemModel.getId())){
					list.remove(selectItemModelEntity1);
					i--;
					itemManager.save(oldSelectItem);
				}
			}
		}
		((SelectItemModelEntity) itemModel).setParentItem(parentSelectItem);
		if(parentSelectItem != null) {
			for (int i = 0; i < parentSelectItem.getItems().size(); i++) {
				SelectItemModelEntity childItem = parentSelectItem.getItems().get(i);
				if (!selectItemModelEntity.isNew() && StringUtils.equals(childItem.getId(), selectItemModelEntity.getId())) {
					parentSelectItem.getItems().remove(childItem);
					i--;
				}
			}
			parentSelectItem.getItems().add(selectItemModelEntity);
		}
	}

	//设置关联控件父控件
	private void setReferenceItem(Map<String, ItemModelEntity> map, ItemModelEntity itemModel){
		ReferenceItemModelEntity referenceItemModelEntity = ((ReferenceItemModelEntity) itemModel);
		ReferenceItemModelEntity newParentItemModel = null;
		if(referenceItemModelEntity.getParentItem() != null){
			newParentItemModel = (ReferenceItemModelEntity) map.get(referenceItemModelEntity.getParentItem().getUuid());
		}

		ReferenceItemModelEntity oldReferenceItem = null;
		if(!referenceItemModelEntity.isNew()){
			ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemManager.get(referenceItemModelEntity.getId());
			if(referenceItemModelEntity1.getParentItem() != null) {
				oldReferenceItem = (ReferenceItemModelEntity) map.get(referenceItemModelEntity1.getParentItem().getUuid());
			}
		}

		if(oldReferenceItem != null && (newParentItemModel == null || StringUtils.equalsIgnoreCase(newParentItemModel.getUuid(), oldReferenceItem.getUuid()))){
			List<ReferenceItemModelEntity> list = oldReferenceItem.getItems();
			for(int i = 0; i < list.size(); i++){
				ReferenceItemModelEntity referenceItemModelEntity1 = list.get(i);
				if(referenceItemModelEntity1.getId().equals(itemModel.getId())){
					list.remove(referenceItemModelEntity1);
					i--;
				}
			}
		}
		if(newParentItemModel != null){
			((ReferenceItemModelEntity) itemModel).setParentItem(newParentItemModel);
			List<String> list = newParentItemModel.getItems().parallelStream().map(ReferenceItemModelEntity::getUuid).collect(Collectors.toList());
			if(list != null || !list.contains(itemModel.getUuid())){
				newParentItemModel.getItems().add((ReferenceItemModelEntity) itemModel);
			}
		}
	}

	private ItemModelEntity  getNewItemModel(Map<String, ItemModelEntity> oldMapItmes, Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity paramerItemModelEntity){
		ItemModelEntity saveItemModelEntity = getItemModelEntity(oldMapItmes,  paramerItemModelEntity);

		String oldColumnName = saveItemModelEntity.getColumnModel() == null ? null : saveItemModelEntity.getColumnModel().getColumnName();
		String newColunmName = paramerItemModelEntity.getColumnModel() == null ? null : paramerItemModelEntity.getColumnModel().getColumnName();
		ReferenceType oldReferenceType = null;
		String oldReferenceFormId = null;
		if(saveItemModelEntity  instanceof ReferenceItemModelEntity){
			oldReferenceType = ((ReferenceItemModelEntity)saveItemModelEntity).getReferenceType();
			oldReferenceFormId = ((ReferenceItemModelEntity) saveItemModelEntity).getReferenceFormId();
		}
		if(paramerItemModelEntity instanceof NumberItemModelEntity && saveItemModelEntity instanceof NumberItemModelEntity &&
						((NumberItemModelEntity)paramerItemModelEntity).getDecimalDigits() != ((NumberItemModelEntity)saveItemModelEntity).getDecimalDigits() &&
								(((NumberItemModelEntity)paramerItemModelEntity).getDecimalDigits() == 0 || ((NumberItemModelEntity)saveItemModelEntity).getDecimalDigits() == 0)){
			//删除字段
			if(oldColumnName != null) {
				columnModelService.deleteTableColumn(saveItemModelEntity.getColumnModel().getDataModel().getTableName(), oldColumnName);
			}
		}
		ReferenceType newReferenceType = null;
		String newReferenceFormId = null;
		if(paramerItemModelEntity  instanceof ReferenceItemModelEntity){
			newReferenceType = ((ReferenceItemModelEntity)paramerItemModelEntity).getReferenceType();
			newReferenceFormId = ((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceFormId();
		}

		BeanUtils.copyProperties(paramerItemModelEntity, saveItemModelEntity, new String[]{"referencesItemModels","parentItem", "searchItems","sortItems","permissions", "referenceList","items","formModel","columnModel","activities","options"});

		setOption(saveItemModelEntity, paramerItemModelEntity);
		saveItempermissions(saveItemModelEntity, paramerItemModelEntity);

		//设置列表模型
		if (paramerItemModelEntity instanceof ReferenceItemModelEntity ) {
			if(((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceList() != null) {
				ListModelEntity listModelEntity = listModelManager.find(((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceList().getId());
				((ReferenceItemModelEntity) saveItemModelEntity).setReferenceList(listModelEntity);
			}
			((ReferenceItemModelEntity)saveItemModelEntity).setParentItem(((ReferenceItemModelEntity) paramerItemModelEntity).getParentItem());
			((ReferenceItemModelEntity)saveItemModelEntity).setItemUuids(((ReferenceItemModelEntity) paramerItemModelEntity).getItemUuids());

			//删除字段删除索引
			if(oldColumnName != null && (!StringUtils.equalsIgnoreCase(oldColumnName, newColunmName) || !StringUtils.equalsIgnoreCase(newReferenceFormId, oldReferenceFormId))) {
				columnModelService.deleteTableColumn(saveItemModelEntity.getColumnModel().getDataModel().getTableName(), oldColumnName);
			}else if(oldColumnName != null && oldColumnName.equals(newColunmName) && oldReferenceType != newReferenceType){
				columnModelService.deleteTableColumnIndex(saveItemModelEntity.getColumnModel().getDataModel().getTableName(), oldColumnName);
			}
		}

		//设置下拉联动
		if (paramerItemModelEntity instanceof SelectItemModelEntity) {
			((SelectItemModelEntity)saveItemModelEntity).setReferenceDictionaryId(((SelectItemModelEntity) paramerItemModelEntity).getReferenceDictionaryId());
			((SelectItemModelEntity)saveItemModelEntity).setReferenceDictionaryItemId(((SelectItemModelEntity) paramerItemModelEntity).getReferenceDictionaryItemId());
			((SelectItemModelEntity)saveItemModelEntity).setParentItem(((SelectItemModelEntity) paramerItemModelEntity).getParentItem());
		}

		setColumnModelEntity(  modelEntityMap,  paramerItemModelEntity,  saveItemModelEntity);

		return saveItemModelEntity;
	}

	//得到表单控件
	private ItemModelEntity getItemModelEntity(Map<String, ItemModelEntity> oldMapItmes, ItemModelEntity paramerItemModelEntity){
		ItemModelEntity saveItemModelEntity = null;
		if(!paramerItemModelEntity.isNew()){
			saveItemModelEntity = oldMapItmes.get(paramerItemModelEntity.getId());
			if(saveItemModelEntity == null){
				throw new IFormException("未找到【"+paramerItemModelEntity.getId()+"】对应控件");
			}
		}else{
			saveItemModelEntity = getItemModelEntity(paramerItemModelEntity.getType(), paramerItemModelEntity.getSystemItemType());
		}
		return saveItemModelEntity;
	}

	//设置表单控件字段
	private void setColumnModelEntity(Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity paramerItemModelEntity, ItemModelEntity saveItemModelEntity){
		if(paramerItemModelEntity.getColumnModel() != null && paramerItemModelEntity.getColumnModel().getDataModel() != null){
			ColumnModelEntity columnModelEntity = modelEntityMap.get(paramerItemModelEntity.getColumnModel().getDataModel().getTableName()+"_"+paramerItemModelEntity.getColumnModel().getColumnName());
			if(columnModelEntity != null && saveItemModelEntity instanceof NumberItemModelEntity){
				if(((NumberItemModelEntity) saveItemModelEntity).getDecimalDigits() != null && ((NumberItemModelEntity) saveItemModelEntity).getDecimalDigits() > 0
						&& columnModelEntity.getDataType() != ColumnType.Double){
					columnModelEntity.setDataType(ColumnType.Double);
				}else if((((NumberItemModelEntity) saveItemModelEntity).getDecimalDigits() == null || ((NumberItemModelEntity) saveItemModelEntity).getDecimalDigits() < 1)
						&& columnModelEntity.getDataType() != ColumnType.Integer){
					columnModelEntity.setDataType(ColumnType.Integer);
				}
			} else if (columnModelEntity != null && saveItemModelEntity instanceof TreeSelectItemModelEntity) {
				TreeSelectItemModelEntity treeSelectItemModelEntity = (TreeSelectItemModelEntity)saveItemModelEntity;
				if (treeSelectItemModelEntity.getMultiple()!=null) {
					if (treeSelectItemModelEntity.getMultiple()==true) {
						columnModelEntity.setDataType(ColumnType.String);
						columnModelEntity.setLength(4096);
					}
				}
			}else if (columnModelEntity != null && saveItemModelEntity.getType() == ItemType.TimePicker) {//不带日期的时间控件为String类型
				columnModelEntity.setDataType(ColumnType.String);
				columnModelEntity.setLength(255);
			}else if(saveItemModelEntity instanceof ReferenceItemModelEntity){
				columnModelEntity.setPrefix(null);
			}
			saveItemModelEntity.setColumnModel(columnModelEntity);
		}else if(!"id".equals(saveItemModelEntity.getName())){
			saveItemModelEntity.setColumnModel(null);
		}
	}

	//设备表单权限
	private void saveItempermissions(ItemModelEntity saveItemModelEntity, ItemModelEntity paramerItemModelEntity){
		if(paramerItemModelEntity.getColumnModel() != null && !"id".equals(paramerItemModelEntity.getColumnModel().getColumnName())) {
			List<ItemPermissionInfo> list = new ArrayList<>();
			Map<String, ItemPermissionInfo> oldItemPermission = new HashMap<>();
			if(saveItemModelEntity.isNew() && (paramerItemModelEntity.getPermissions() == null || paramerItemModelEntity.getPermissions().size() < 1)) {
				list.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Add));
				list.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Update));
				list.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Check));
			}else{
				for(ItemPermissionInfo itemPermissionInfo : saveItemModelEntity.getPermissions()){
					oldItemPermission.put(itemPermissionInfo.getId(), itemPermissionInfo);
				}
				for(ItemPermissionInfo itemPermissionInfo : paramerItemModelEntity.getPermissions()){
					ItemPermissionInfo newItemPermiss = itemPermissionInfo.isNew() ? new ItemPermissionInfo() : oldItemPermission.remove(itemPermissionInfo.getId());
					if(newItemPermiss == null){
						newItemPermiss = new ItemPermissionInfo();
					}
					BeanUtils.copyProperties(itemPermissionInfo, newItemPermiss, new String[]{"itemModel"});
					newItemPermiss.setItemModel(saveItemModelEntity);
					list.add(newItemPermiss);
				}
			}
			saveItemModelEntity.setPermissions(list);
			for(String key: oldItemPermission.keySet()){
				ItemPermissionInfo permissionInfo = oldItemPermission.get(key);
				if(permissionInfo.getItemModel() != null){
					permissionInfo.getItemModel().getPermissions().remove(permissionInfo);
				}
				permissionInfo.setItemModel(null);
				itemPermissionManager.delete(permissionInfo);
			}
		}
	}

	private ItemPermissionInfo createItempermissionInfo(ItemModelEntity itemModelEntity, DisplayTimingType displayTimingType){
		ItemPermissionInfo itemPermissionInfo = new ItemPermissionInfo();
		itemPermissionInfo.setItemModel(itemModelEntity);
		//可见
		itemPermissionInfo.setVisible(true);
		//可填
		itemPermissionInfo.setCanFill(true);
		//必填
		itemPermissionInfo.setRequired(false);
		if(DisplayTimingType.Check == displayTimingType) {
			itemPermissionInfo.setCanFill(null);
			itemPermissionInfo.setRequired(null);
		}

		//显示时机 若为空标识所有时机都显示
		itemPermissionInfo.setDisplayTiming(displayTimingType);
		return itemPermissionInfo;
	}

	private ItemModelEntity  getNewSubFormRowItemModel(Map<String, ItemModelEntity> oldMapItmes, SubFormRowItemModelEntity oldItemModelEntity){
		ItemModelEntity newItemModelEntity = new SubFormRowItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = oldMapItmes.get(oldItemModelEntity.getId());
		}
		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"permissions","searchItems", "sortItems", "parentItem","referenceList","items","formModel","columnModel","activities","options"});
		return newItemModelEntity;
	}

	private TabPaneItemModelEntity  getNewTabPaneItemModel(Map<String, ItemModelEntity> oldMapItmes, TabPaneItemModelEntity oldItemModelEntity){
		TabPaneItemModelEntity newItemModelEntity = new TabPaneItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = (TabPaneItemModelEntity)oldMapItmes.get(oldItemModelEntity.getId());
		}
		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"permissions","searchItems", "sortItems", "parentItem","referenceList","items","formModel","columnModel","activities","options"});
		return newItemModelEntity;
	}

	private ItemModelEntity  getNewSubFormItemModel(SubFormItemModelEntity oldItemModelEntity){
		ItemModelEntity newItemModelEntity = new SubFormItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = itemManager.get(oldItemModelEntity.getId());
		}

		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"permissions","searchItems", "sortItems", "referenceList","items","formModel","columnModel","activities","options"});
		return newItemModelEntity;
	}

	private ItemModelEntity  getNewRowItemModel(RowItemModelEntity oldItemModelEntity){
		ItemModelEntity newItemModelEntity = new RowItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = itemManager.get(oldItemModelEntity.getId());
		}
		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"searchItems", "sortItems", "referenceList","items","formModel","columnModel","activities","options"});
		return newItemModelEntity;
	}


	//删除item
	private void deleteItems(DataModelEntity dataModelEntity, List<ItemModelEntity> deleteItems){
		if (deleteItems == null || deleteItems.size() < 1) {
			return;
		}
		List<ItemModelEntity> itemModelEntityList = deleteItems;
		List<String> manyTomanyFormIdList = new ArrayList<>();
		for(int i = 0 ; i < itemModelEntityList.size() ; i++){
			ItemModelEntity itemModelEntity = itemModelEntityList.get(i);
			if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getReferenceType() == ReferenceType.ManyToMany
				&& itemModelEntity.getType() == ItemType.ReferenceList){
				manyTomanyFormIdList.add(((ReferenceItemModelEntity) itemModelEntity).getReferenceFormId());
			}

			deleteItemOtherReferenceEntity(itemModelEntity);
			if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getSelectMode() == SelectMode.Multiple){
				ColumnModelEntity columnModelEntity = columnModelService.saveColumnModelEntity(dataModelEntity, "id");
				FormModelEntity toFormModelEntity = get(((ReferenceItemModelEntity) itemModelEntity).getReferenceFormId());
				ColumnModelEntity toColumnModelEntity = null;
				if(toFormModelEntity != null){
					toColumnModelEntity = columnModelService.saveColumnModelEntity(toFormModelEntity.getDataModels().get(0), "id");
				}
				if(toColumnModelEntity != null) {
					for (ColumnReferenceEntity columnReferenceEntity : columnModelEntity.getColumnReferences()) {
						if(columnReferenceEntity.getToColumn().getId().equals(toColumnModelEntity.getId())) {
							columnModelService.deleteTable("if_" + columnReferenceEntity.getReferenceMiddleTableName() + "_list");
						}
					}
				}
			}
		}
		deleteItem(itemModelEntityList);
		deleteManyToManyReference(dataModelEntity, manyTomanyFormIdList);
	}

	//删除多对多关联
	private void deleteManyToManyReference(DataModelEntity dataModelEntity, List<String> manyTomanyFormIdList){
		if(manyTomanyFormIdList == null || manyTomanyFormIdList.size() < 1){
			return;
		}
		List<String> deleteReferenceIds = new ArrayList<>();
		for(String str : manyTomanyFormIdList){
			FormModelEntity formModelEntity = formModelManager.get(str);
			if(formModelEntity != null && formModelEntity.getDataModels() != null && formModelEntity.getDataModels().size() > 0){
				deleteReferenceIds.add(columnModelService.saveColumnModelEntity(formModelEntity.getDataModels().get(0), "id").getId());
			}
		}
		if(deleteReferenceIds == null || deleteReferenceIds.size() < 1){
			return;
		}
		ColumnModelEntity columnModelEntity = columnModelService.saveColumnModelEntity(dataModelEntity, "id");
		for(int i = 0; i < columnModelEntity.getColumnReferences().size(); i++){
			columnModelService.deleteOldColumnReferenceEntity(columnModelEntity, deleteReferenceIds, columnModelEntity.getColumnReferences());
		}
	}

	private void deleteItem(List<ItemModelEntity> lists){
		List<ItemModelEntity> list = lists.parallelStream().sorted((d2, d1) -> d2.getOrderNo().compareTo(d1.getOrderNo())).collect(Collectors.toList());
		for(int i = 0 ; i <  list.size(); i ++){
			ItemModelEntity itemModelEntity = list.get(i);
			list.remove(itemModelEntity);
			i--;
			if(itemModelEntity instanceof ReferenceItemModelEntity ){
				if(((ReferenceItemModelEntity) itemModelEntity).getParentItem() != null){
					((ReferenceItemModelEntity) itemModelEntity).getParentItem().getItems().remove(itemModelEntity);
					((ReferenceItemModelEntity) itemModelEntity).setParentItem(null);
				}
				deleteItemAndColumn(itemModelEntity);
			}else if(itemModelEntity instanceof SelectItemModelEntity ){
				if(((SelectItemModelEntity) itemModelEntity).getParentItem() != null){
					((SelectItemModelEntity) itemModelEntity).getParentItem().getItems().remove(itemModelEntity);
					((SelectItemModelEntity) itemModelEntity).setParentItem(null);
				}
				itemManager.delete(itemModelEntity);
			}else if(itemModelEntity instanceof TabsItemModelEntity ){
				for(int t =0 ; t < ((TabsItemModelEntity) itemModelEntity).getItems().size(); t++) {
					TabPaneItemModelEntity tabPaneItemModelEntity = ((TabsItemModelEntity) itemModelEntity).getItems().get(t);
					List<ItemModelEntity> list1 = tabPaneItemModelEntity.getItems();
					list1.removeAll(list1);
					deleteItem(list1);
					tabPaneItemModelEntity.getParentItem().getItems().remove(tabPaneItemModelEntity);
					tabPaneItemModelEntity.setParentItem(null);
					itemManager.delete(tabPaneItemModelEntity);
				}
				itemManager.delete(itemModelEntity);
			}else if(itemModelEntity instanceof SubFormItemModelEntity){
				if(itemModelEntity.getColumnModel() != null) {//刪除子表
					columnModelService.deleteTable(itemModelEntity.getColumnModel().getDataModel().getTableName());
				}
				for(int t =0 ; t < ((SubFormItemModelEntity) itemModelEntity).getItems().size(); t++) {
					SubFormRowItemModelEntity subFormRowItemModelEntity = ((SubFormItemModelEntity) itemModelEntity).getItems().get(t);
					List<ItemModelEntity> list1 = subFormRowItemModelEntity.getItems();
					list1.removeAll(list1);
					deleteItem(list1);
					subFormRowItemModelEntity.getParentItem().getItems().remove(subFormRowItemModelEntity);
					subFormRowItemModelEntity.setParentItem(null);
					itemManager.delete(subFormRowItemModelEntity);
				}
				itemManager.delete(itemModelEntity);
			}else {
				deleteItemAndColumn(itemModelEntity);
			}
			if(i<-1){
				i=-1;
			}
		}
	}

	private void deleteItemAndColumn(ItemModelEntity itemModelEntity){
		if(itemModelEntity.getColumnModel() != null){
			columnModelService.deleteTableColumn(itemModelEntity.getColumnModel().getDataModel().getTableName(), itemModelEntity.getColumnModel().getColumnName());
		}
		itemManager.delete(itemModelEntity);
	}

	//软删除控件关联实体
	@Override
	public void deleteItemOtherReferenceEntity(ItemModelEntity itemModelEntity){
		List<ListSearchItem> listSearchItems = listSearchItemManager.query().filterEqual("itemModel.id", itemModelEntity.getId()).list();
		for(ListSearchItem listSearchItem : listSearchItems){
			listSearchItem.setItemModel(null);
			listSearchItemManager.save(listSearchItem);
		}

		List<ListSortItem> listSortItems = listSortItemManager.query().filterEqual("itemModel.id", itemModelEntity.getId()).list();
		for(ListSortItem ListSortItem : listSortItems){
			ListSortItem.setItemModel(null);
			listSortItemManager.save(ListSortItem);
		}

		List<QuickSearchEntity> quickSearchEntities = quickSearchEntityManager.query().filterEqual("itemModel.id", itemModelEntity.getId()).list();
		for(QuickSearchEntity quickSearch : quickSearchEntities){
			quickSearch.setItemModel(null);
			quickSearchEntityManager.save(quickSearch);
		}
		List<String> ids = new ArrayList<>();
		ids.add(itemModelEntity.getId());
		List<ListModelEntity> listModelEntities = listModelService.findListModelsByItemModelId(itemModelEntity.getId());
		for(int j = 0; j <  listModelEntities.size(); j++){
			ListModelEntity listModelEntity = listModelEntities.get(j);
			List<ItemModelEntity> itemModelEntities = listModelEntity.getDisplayItems();
			for(int i = 0; i < itemModelEntities.size(); i++){
				ItemModelEntity itemModelEntity1 = itemModelEntities.get(i);
				if(itemModelEntity1.getId().equals(itemModelEntity.getId())){
					listModelEntity.getDisplayItems().remove(itemModelEntity1);
					i--;
					listModelService.save(listModelEntity);
				}
			}
		}
	}

	//得到最新的item
	private ItemModelEntity setOption(ItemModelEntity newEntity, ItemModelEntity paramerItemModelEntity){
		//旧数据
		List<ItemSelectOption> itemSelectOptions = newEntity.getOptions();
		Map<String, ItemSelectOption> itemSelectOptionMap = new HashMap<>();
		if(itemSelectOptions != null) {
			for (ItemSelectOption option : itemSelectOptions) {
				itemSelectOptionMap.put(option.getId(), option);
			}
		}
		List<ItemSelectOption> options = new ArrayList<ItemSelectOption>();
		if(paramerItemModelEntity.getOptions() != null) {
			for (ItemSelectOption option : paramerItemModelEntity.getOptions()) {
				ItemSelectOption newOption = option.isNew() ? new ItemSelectOption() : itemSelectOptionMap.remove(option.getId());
				BeanUtils.copyProperties(option, newOption, new String[]{"itemModel"});
				newOption.setItemModel(newEntity);
				options.add(newOption);
			}
		}
		newEntity.setOptions(options);
		for(String key : itemSelectOptionMap.keySet()){
			itemSelectOptionManager.deleteById(key);
		}

		return newEntity;
	}

	//初始化item的值
	private void initItemData(Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity item){
		//设置行模型
		if (item.getColumnModel() != null && item.getColumnModel().getDataModel() != null) {
			item.setColumnModel(modelEntityMap.get(item.getColumnModel().getDataModel().getTableName() + "_" + item.getColumnModel().getColumnName()));
		}
	}

	//获取item子item
	private ItemModelEntity getNewItemModelEntity(Map<String, ItemModelEntity> oldMapItmes, Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity paramerItemModelEntity){
		ItemModelEntity newModelEntity = getNewItemModel(oldMapItmes, modelEntityMap, paramerItemModelEntity);
		if(paramerItemModelEntity instanceof RowItemModelEntity){
            setRowItemModelEntity((RowItemModelEntity)paramerItemModelEntity, oldMapItmes, modelEntityMap, newModelEntity);
		}else if(paramerItemModelEntity instanceof SubFormItemModelEntity){
            setSubFormItemModel((SubFormItemModelEntity)paramerItemModelEntity, oldMapItmes, modelEntityMap, newModelEntity);
		}else if(paramerItemModelEntity instanceof TabsItemModelEntity){
            setTabPaneItemModelEntity((TabsItemModelEntity)paramerItemModelEntity, oldMapItmes, modelEntityMap, newModelEntity);
		}
		return newModelEntity;
	}

	private void setRowItemModelEntity(RowItemModelEntity rowItemModelEntity, Map<String, ItemModelEntity> oldMapItmes,
                                       Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity newModelEntity){
        List<ItemModelEntity> rowItems = new ArrayList<ItemModelEntity>();
        for(int i = 0; i < rowItemModelEntity.getItems().size() ; i++) {
            ItemModelEntity rowItem = rowItemModelEntity.getItems().get(i);
            ItemModelEntity newRowItem = getNewItemModel(oldMapItmes, modelEntityMap, rowItem);
            newRowItem.setFormModel(null);
            if((newRowItem instanceof ReferenceItemModelEntity) && newRowItem.getType() != ItemType.ReferenceLabel) {
                verifyReference((ReferenceItemModelEntity)rowItem);
            }
            newRowItem.setOrderNo(newModelEntity.getOrderNo() + i);
            rowItems.add(newRowItem);
        }
        rowItemModelEntity.setItems(rowItems);
        ((RowItemModelEntity)newModelEntity).setItems(rowItems);
    }

    private void setSubFormItemModel(SubFormItemModelEntity subFormItemModel, Map<String, ItemModelEntity> oldMapItmes,
                                       Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity newModelEntity){
        List<SubFormRowItemModelEntity> subFormItems = new ArrayList<>();
        for (int i = 0; i < subFormItemModel.getItems().size() ; i++) {
            SubFormRowItemModelEntity subFormRowItemModelEntity = subFormItemModel.getItems().get(i);
            SubFormRowItemModelEntity subFormRowItemModel  = (SubFormRowItemModelEntity)getNewSubFormRowItemModel(oldMapItmes, subFormRowItemModelEntity);
            subFormRowItemModel.setFormModel(null);
            subFormRowItemModel.setOrderNo(newModelEntity.getOrderNo() + i);
            List<ItemModelEntity> rowItems = new ArrayList<>();
            for (int j = 0; j < subFormRowItemModelEntity.getItems().size() ; j ++) {
                ItemModelEntity childRenItem = subFormRowItemModelEntity.getItems().get(j);
                ItemModelEntity newRowItem = getNewItemModel(oldMapItmes, modelEntityMap, childRenItem);
                newRowItem.setFormModel(null);
                if((newRowItem instanceof ReferenceItemModelEntity) && newRowItem.getType() != ItemType.ReferenceLabel) {
                    verifyReference((ReferenceItemModelEntity)newRowItem);
                }
                newRowItem.setOrderNo(newModelEntity.getOrderNo() + j);
                rowItems.add(getNewItemModel(oldMapItmes, modelEntityMap, newRowItem));
            }
            subFormRowItemModel.setParentItem((SubFormItemModelEntity)newModelEntity);
            subFormRowItemModel.setItems(rowItems);
            subFormItems.add(subFormRowItemModel);
        }
        ((SubFormItemModelEntity)newModelEntity).setItems(subFormItems);
    }

    private void setTabPaneItemModelEntity(TabsItemModelEntity tabPaneItemModelEntity, Map<String, ItemModelEntity> oldMapItmes,
                                     Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity newModelEntity){
        List<TabPaneItemModelEntity> tabPaneItemModelEntities = new ArrayList<>();
        for (int j = 0; j < tabPaneItemModelEntity.getItems().size() ; j ++) {
            TabPaneItemModelEntity childRenItem = tabPaneItemModelEntity.getItems().get(j);
            TabPaneItemModelEntity newRowItem = getNewTabPaneItemModel(oldMapItmes, childRenItem);
            List<ItemModelEntity> list = new ArrayList<>();
            for(int k = 0 ; k < childRenItem.getItems().size() ; k++){
                ItemModelEntity itemModelEntity = childRenItem.getItems().get(k);
                ItemModelEntity itemModelEntity1 = getNewItemModelEntity(oldMapItmes, modelEntityMap, itemModelEntity);
                itemModelEntity1.setFormModel(null);
                itemModelEntity1.setOrderNo(newModelEntity.getOrderNo() + k);
                list.add(itemModelEntity1);
            }
            newRowItem.setFormModel(null);
            newRowItem.setParentItem((TabsItemModelEntity) newModelEntity);
            newRowItem.setOrderNo(newModelEntity.getOrderNo()+ j);
            newRowItem.setItems(list);
            tabPaneItemModelEntities.add(newRowItem);
        }
        ((TabsItemModelEntity)newModelEntity).setItems(tabPaneItemModelEntities);
    }

	//获取item子item
	@Override
	public List<ItemModelEntity> getChildRenItemModelEntity(ItemModelEntity itemModelEntity){
		Set<ItemModelEntity> list = new HashSet<>();
		if(itemModelEntity instanceof RowItemModelEntity){
			list.addAll(((RowItemModelEntity) itemModelEntity).getItems());
		}else if(itemModelEntity instanceof SubFormItemModelEntity){
			list.addAll(((SubFormItemModelEntity) itemModelEntity).getItems());
			for (SubFormRowItemModelEntity subFormRowItemModelEntity :  ((SubFormItemModelEntity)itemModelEntity).getItems()) {
				list.addAll(subFormRowItemModelEntity.getItems());
				for(ItemModelEntity itemModelEntity1 : subFormRowItemModelEntity.getItems()){
					list.addAll(getChildRenItemModelEntity(itemModelEntity1));
				}
			}
		}else if(itemModelEntity instanceof TabsItemModelEntity){
			list.addAll(((TabsItemModelEntity) itemModelEntity).getItems());
			for (TabPaneItemModelEntity tabPaneItemModelEntity :  ((TabsItemModelEntity)itemModelEntity).getItems()) {
				list.addAll(tabPaneItemModelEntity.getItems());
				for(ItemModelEntity itemModelEntity1 : tabPaneItemModelEntity.getItems()){
					list.addAll(getChildRenItemModelEntity(itemModelEntity1));
				}
			}
		}
		return new ArrayList<>(list);
	}

	//获取item子item
	public List<ItemModel> getChildRenItemModel(ItemModel itemModel){
		Set<ItemModel> list = new HashSet<>();
		if(itemModel.getType() == ItemType.ReferenceList && itemModel.getItems() != null){
			list.addAll(itemModel.getItems());
		}else if(itemModel.getType() == ItemType.SubForm && itemModel.getItems() != null){
			list.addAll(itemModel.getItems());
			for (ItemModel subFormRowItemModel :  itemModel.getItems()) {
				list.addAll(subFormRowItemModel.getItems());
				for(ItemModel itemModel1 : subFormRowItemModel.getItems()){
					list.addAll(getChildRenItemModel(itemModel1));
				}
			}
		}else if(itemModel.getType() == ItemType.Tabs && itemModel.getItems() != null ){
			list.addAll(itemModel.getItems());
			for (ItemModel tabPaneItemModel :  itemModel.getItems()) {
				list.addAll(tabPaneItemModel.getItems());
				for(ItemModel itemModel1 : tabPaneItemModel.getItems()){
					list.addAll(getChildRenItemModel(itemModel1));
				}
			}
		}
		return new ArrayList<>(list);
	}

	//校验关联
	private void verifyReference(ReferenceItemModelEntity rowItem){
		FormModelEntity formModelEntity = find(rowItem.getReferenceFormId());
		if(formModelEntity == null){
			throw new IFormException(404, "表单【" + rowItem.getReferenceFormId() +"】， 未找到");
		}

		//关联表单字段
		ColumnModelEntity addToEntity = null;
		if(StringUtils.isNotEmpty(rowItem.getReferenceItemId())) {
			ItemModelEntity itemModelEntity = itemManager.find(rowItem.getReferenceItemId());
			if (itemModelEntity != null) {
				addToEntity = itemModelEntity.getColumnModel();
			}
		}

		if(StringUtils.isNotEmpty(rowItem.getReferenceItemId()) && addToEntity == null){
			throw new IFormException(404, "表单【" + rowItem.getReferenceFormId() +"】， 未找到对应的【"+ rowItem.getReferenceItemId() +"】控件");
		}

		//关联表行
		/*ColumnModelEntity addToEntity =
				columnModelManager.query().filterEqual("columnName",  rowItem.getReferenceValueColumn()).filterEqual("dataModel.tableName", rowItem.getReferenceTable()).unique();
		*/
		if(addToEntity != null && addToEntity.getColumnReferences() != null){
			List<String> dataModelIds= new ArrayList<String>();
			List<String> items = new ArrayList<String>();
			for(ColumnReferenceEntity columnReferenceEntity : addToEntity.getColumnReferences()){
				dataModelIds.add(columnReferenceEntity.getFromColumn().getDataModel().getId());
				items.add(columnReferenceEntity.getFromColumn().getId());
			}
			if(rowItem.getColumnModel() != null && !items.contains(rowItem.getColumnModel().getId())
					&& dataModelIds.contains(rowItem.getColumnModel().getDataModel().getId())){
				throw new IFormException(404, "表单存在关联了，不要多字段关联");
			}
		}
	}

	//删除旧的item活动跟选项数据
	private void deleteOldItems(FormModelEntity old ){
		for (ItemModelEntity itemModelEntity : old.getItems()) {
			if(itemModelEntity instanceof RowItemModelEntity){
				deleteItemActivityOption(itemModelEntity);
				for(int i = 0; i < ((RowItemModelEntity) itemModelEntity).getItems().size(); i++){
					ItemModelEntity childrenItem = ((RowItemModelEntity) itemModelEntity).getItems().get(i);
					deleteItemActivityOption(childrenItem);
				}
			}else if(itemModelEntity instanceof SubFormItemModelEntity){
				deleteItemActivityOption(itemModelEntity);
				for(int i = 0; i < ((SubFormItemModelEntity) itemModelEntity).getItems().size() ; i++) {
					SubFormRowItemModelEntity item = ((SubFormItemModelEntity) itemModelEntity).getItems().get(i);
					deleteItemActivityOption(item);
					for(int j = 0; j < item.getItems().size() ; j++) {
						ItemModelEntity childrenItem = item.getItems().get(j);
						deleteItemActivityOption(childrenItem);
					}
				}
			}else if(itemModelEntity instanceof TabsItemModelEntity){
				deleteItemActivityOption(itemModelEntity);
				for(int i = 0;i < ((TabsItemModelEntity) itemModelEntity).getItems().size();i++) {
					TabPaneItemModelEntity item = ((TabsItemModelEntity) itemModelEntity).getItems().get(i);
					deleteItemActivityOption(item);
					for(ItemModelEntity childrenItem : item.getItems()) {
						deleteItemActivityOption(childrenItem);
					}
				}
			}else{
				deleteItemActivityOption(itemModelEntity);
			}
		}
	}

	@Override
	public FormModelEntity saveFormModel(FormModel formModel) {
		return saveDataModel(formModel);
	}

	//删除或者保存旧的数据建模
	private FormModelEntity saveDataModel(FormModel formModel){
		FormModelEntity oldEntity = new FormModelEntity();
		boolean newFlag = formModel.isNew();
		if(!newFlag){
			oldEntity = find(formModel.getId());
			if(oldEntity == null){
				throw new IFormException("未找到【" + formModel.getId() + "】对应表单模型");
			}
		}
		if(formModel.getDataModels() == null || formModel.getDataModels().isEmpty() ) {
			throw new IFormException("添加表单模型失败：需要关联数据模型");
		}

		List<String> newDataModelIds = new ArrayList<>();
		List<DataModelEntity> newAddDataModel = new ArrayList<>();
		if(formModel.getDataModels() != null && formModel.getDataModels().size() > 0) {
            DataModel dataModel = formModel.getDataModels().get(0);
            DataModelEntity dataModelEntity = new DataModelEntity();
            if (!dataModel.isNew()) {
                dataModelEntity = dataModelService.get(dataModel.getId());
            }
            BeanUtils.copyProperties(dataModel, dataModelEntity, new String[]{"masterModel", "slaverModels", "columns", "indexes", "referencesDataModel"});
            if (dataModel.isNew()) {
                dataModelEntity.setId(null);
            }
            columnModelService.saveColumnModelEntity(dataModelEntity, "id");
			columnModelService.saveColumnModelEntity(dataModelEntity, "create_at");
			columnModelService.saveColumnModelEntity(dataModelEntity, "update_at");
			columnModelService.saveColumnModelEntity(dataModelEntity, "create_by");
			columnModelService.saveColumnModelEntity(dataModelEntity, "update_by");
            dataModelService.save(dataModelEntity);
            newDataModelIds.add(dataModelEntity.getId());
            newAddDataModel.add(dataModelEntity);
        }
		BeanUtils.copyProperties(formModel, oldEntity, new String[] {"items","indexes","dataModels","permissions","submitChecks","functions", "triggeres"});
        if(formModel.isNew()){
            oldEntity.setId(null);
        }

        List<DataModelEntity> oldDataModelEntities = oldEntity.getDataModels();
        for(int i = 0 ; i < oldDataModelEntities.size() ; i ++){
            DataModelEntity dataModelEntity = oldDataModelEntities.get(i);
            if(!newDataModelIds.contains(dataModelEntity.getId())){
                oldDataModelEntities.remove(dataModelEntity);
                i--;
            }
        }
        oldEntity.setDataModels(newAddDataModel);
        FormModelEntity formModelEntity = this.doSave(oldEntity, dataModelUpdateNeeded(oldEntity));
		if(newFlag){
			//创建默认的表单功能
			formFunctionsService.createDefaultFormFunctions(formModelEntity);
			//提交表单权限
//			listModelService.submitFormBtnPermission(formModelEntity);
		}
		return formModelEntity;
	}
	//获取关联字段的控件
	@Override
	public  List<ItemModelEntity> getAllColumnItems(List<ItemModelEntity> itemModelEntities){
		Set<ItemModelEntity> itemModels = new HashSet<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			if(itemModelEntity.getColumnModel() != null){
				itemModels.add(itemModelEntity);
			}
			if(itemModelEntity instanceof SubFormItemModelEntity){
				List<SubFormRowItemModelEntity> subRowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
				for(SubFormRowItemModelEntity rowItemModelEntity : subRowItems){
					for(ItemModelEntity itemModel : rowItemModelEntity.getItems()) {
						if (itemModel.getColumnModel() != null) {
							itemModels.add(itemModel);
						}
					}
				}
			}else if(itemModelEntity instanceof RowItemModelEntity){
				for(ItemModelEntity itemModel : ((RowItemModelEntity) itemModelEntity).getItems()) {
					if (itemModel.getColumnModel() != null) {
						itemModels.add(itemModel);
					}
				}
			}else if(itemModelEntity instanceof TabsItemModelEntity){
				for(TabPaneItemModelEntity tabPaneItemModelEntity : ((TabsItemModelEntity) itemModelEntity).getItems()) {
					itemModels.addAll(getAllColumnItems(tabPaneItemModelEntity.getItems()));
				}
			}
		}
		return new ArrayList<>(itemModels);
	}

	@Override
	public ItemModelEntity getItemModelEntity(ItemType itemType, SystemItemType systemItemType){
		ItemModelEntity entity = new ItemModelEntity();
		switch (itemType){
			case InputNumber:
				entity = new NumberItemModelEntity();
				break;
			case Media:
				entity = new FileItemModelEntity();
				break;
			case  Attachment:
				entity = new FileItemModelEntity();
				break;
			case  DatePicker:
				entity = new TimeItemModelEntity();
				break;
            case  TimePicker:
                entity = new TimeItemModelEntity();
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
			case  Tabs:
				entity = new TabsItemModelEntity();
				break;
			case  TabPane:
				entity = new TabPaneItemModelEntity();
				break;
			case  Treeselect:
				entity = new TreeSelectItemModelEntity();
				break;
			case  Location:
				entity = new LocationItemModelEntity();
				break;
			default:
				entity = new ItemModelEntity();
				break;
		}
		if(systemItemType == SystemItemType.SerialNumber){
			entity = new SerialNumberItemModelEntity();
		}else if(systemItemType == SystemItemType.Creator){
			entity = new ReferenceItemModelEntity();
		}else if(systemItemType == SystemItemType.CreateDate){
			entity = new TimeItemModelEntity();
		}
		return entity;
	}

	@Override
	public void deleteFormModelEntityById(String id) {
		FormModelEntity formModelEntity = get(id);
		if(formModelEntity == null){
			throw new IFormException("未找到【"+id+"】对应的表单建模");
		}

		List<ListModelEntity> listModelEntities = listModelManager.query().filterEqual("masterForm.id", id).list();
		if (listModelEntities!=null && listModelEntities.size()>0) {
			throw new IFormException("该表单被列表模型关联了，不能删除该表单");
		}
//		for(int i = 0 ; i < listModelEntities.size() ; i ++){
//			ListModelEntity listModelEntity = listModelEntities.get(i);
//			listModelEntity.setMasterForm(null);
//			listModelEntity.setSlaverForms(null);
//			listModelManager.save(listModelEntity);
//		}

		List<ItemModelEntity> itemModelEntities = formModelEntity.getItems();
		if(itemModelEntities != null && itemModelEntities.size() > 0){
			for(int i = 0 ; i < itemModelEntities.size(); i++) {
				ItemModelEntity itemModelEntity = itemModelEntities.get(i);
				deleteItemOtherReferenceEntity(itemModelEntity);
				itemManager.save(itemModelEntity);
			}
		}
		formModelManager.delete(formModelEntity);
		// 去admin服务清空该表单，以及关联了该表单的所有列表的功能按钮权限
		listModelService.deleteFormBtnPermission(id, listModelEntities.stream().map(item->item.getId()).collect(Collectors.toList()));
	}

	@Override
	public List<ItemModelEntity> findAllItems(FormModelEntity entity) {
		List<ItemModelEntity> itemModels = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : entity.getItems()){
			itemModels.add(itemModelEntity);
			itemModels.addAll(getChildRenItemModelEntity(itemModelEntity));
		}
		return itemModels;
	}


	@Override
	public FormModelEntity saveFormModelSubmitCheck(FormModelEntity entity) {
        FormModelEntity formModelEntity = get(entity.getId());
		BeanUtils.copyProperties(entity, formModelEntity, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});

		Map<String, FormSubmitCheckInfo> oldMap = new HashMap<>();
		List<FormSubmitCheckInfo> oldSubmitCheck = formModelEntity.getSubmitChecks();
		for(FormSubmitCheckInfo info : oldSubmitCheck){
			oldMap.put(info.getId(), info);
		}
		List<FormSubmitCheckInfo> newSubmitCheck = entity.getSubmitChecks();
		if(newSubmitCheck != null){
			List<FormSubmitCheckInfo> submitCheckInfos = new ArrayList<>();
			for(FormSubmitCheckInfo formSubmitCheckInfo : newSubmitCheck){
				FormSubmitCheckInfo checkInfo = null;
				boolean isNew = formSubmitCheckInfo.isNew();
				if(!isNew){
					checkInfo = oldMap.remove(formSubmitCheckInfo.getId());
				}else{
					 checkInfo = new FormSubmitCheckInfo() ;
				}
				BeanUtils.copyProperties(formSubmitCheckInfo, checkInfo, new String[]{"formModel"});
				if(isNew){
                    Integer orderNo = formSubmitCheckService.getMaxOrderNo();
                    checkInfo.setOrderNo(orderNo == null ? 1 : orderNo + 1);
                }
				checkInfo.setFormModel(formModelEntity);
				submitCheckInfos.add(checkInfo);
			}
            formModelEntity.setSubmitChecks(submitCheckInfos);
		}
		for(String key : oldMap.keySet()){
			formSubmitCheckManager.deleteById(key);
		}
		formModelManager.save(formModelEntity);
		return formModelEntity;
	}

	@Override
	public FormModelEntity saveFormModelProcessBind(FormModelEntity entity) {
		FormModelEntity oldFormModelEntity = get(entity.getId());
		BeanUtils.copyProperties(entity, oldFormModelEntity, new String[] {"items","dataModels","permissions","submitChecks","functions", "triggeres"});

		List<ItemModelEntity> parameterItems = entity.getItems();

		//参数
		Map<String, List<ItemActivityInfo>> parameterMap = new HashMap<>();
		for(ItemModelEntity parameterItem : parameterItems){
			if(parameterItem.getActivities() != null && parameterItem.getActivities().size() > 0) {
				parameterMap.put(parameterItem.getId(), parameterItem.getActivities());
			}
		}

		//旧数据
		Map<String, List<ItemActivityInfo>> oldMap = new HashMap<>();
		List<ItemModelEntity> oldItems = findAllItems(oldFormModelEntity);
		Map<String, ItemModelEntity> oldItemsMap = new HashMap<>();
		for(ItemModelEntity oldItem : oldItems){
			oldItemsMap.put(oldItem.getId(), oldItem);
			if(oldItem.getActivities() != null && oldItem.getActivities().size() > 0) {
				oldMap.put(oldItem.getId(), oldItem.getActivities());
			}
		}


		for(String key : parameterMap.keySet()){
			List<ItemActivityInfo> oldItemActivity = oldMap.remove(key);
			Map<String, ItemActivityInfo> oldItemActivityMap = new HashMap<>();
			if(oldItemActivity != null){
				for(ItemActivityInfo activityInfo : oldItemActivity){
					oldItemActivityMap.put(activityInfo.getId(), activityInfo);
				}
			}
			ItemModelEntity itemModelEntity1 = oldItemsMap.get(key);
			List<ItemActivityInfo> newItemActivity = new ArrayList<>();
			for(ItemActivityInfo parameterItemActivity : parameterMap.get(key)){
				ItemActivityInfo info = parameterItemActivity.isNew() ? new ItemActivityInfo() : oldItemActivityMap.remove(parameterItemActivity.getId());
				BeanUtils.copyProperties(parameterItemActivity, info, new String[]{"itemModel"});
				info.setItemModel(itemModelEntity1);
				newItemActivity.add(info);
			}
			itemModelEntity1.setActivities(newItemActivity);
			for(String actvityKey : oldItemActivityMap.keySet()){
				deleteItemActivityInfo(oldItemActivityMap.get(actvityKey));
			}
		}
		for(String key : oldMap.keySet()){
			ItemModelEntity oldItem = oldItemsMap.get(key);
			oldItem.setActivities(null);
			List<ItemActivityInfo> list = oldMap.get(key);
			for(int i = 0 ; i < list.size(); i++){
				ItemActivityInfo info = list.get(i);
				list.remove(info);
				deleteItemActivityInfo(info);
				i--;
			}
		}

		//关联流程表单删除表单功能
		for(int i = 0; i < oldFormModelEntity.getFunctions().size(); i ++){
			ListFunction function = oldFormModelEntity.getFunctions().get(i);
			oldFormModelEntity.getFunctions().remove(function);
			listFunctionManager.delete(function);
			i--;
		}

		formModelManager.save(oldFormModelEntity);
		return oldFormModelEntity;
	}

	private void deleteItemActivityInfo(ItemActivityInfo old){
		old.setItemModel(null);
		itemActivityManager.delete(old);
	}

	@Override
	public List<FormModelEntity> listByDataModel(DataModelEntity dataModelEntity) {
		List<String> idlist = jdbcTemplate.query("select fd.form_model from ifm_form_data_bind fd where fd.data_model='"+dataModelEntity.getId()+"'",
				(rs, rowNum) -> rs.getString("form_model"));
		if(idlist == null || idlist.size() < 1){
			return null;
		}
		List<FormModelEntity> formModelEntities = query().filterIn("id",idlist).list();
		return formModelEntities;
	}

	@Override
	public List<FormModelEntity> listByDataModelIds(List<String> dataModelIds) {
		if(dataModelIds == null || dataModelIds.size() < 1){
			return new ArrayList<>();
		}
		StringBuffer stringBuffer = new StringBuffer("('null'");
		for(String str : dataModelIds){
			stringBuffer.append(",'"+str+"'");
		}
		stringBuffer.append(")");
		List<String> idlist = jdbcTemplate.query("select fd.form_model from ifm_form_data_bind fd where fd.data_model in "+stringBuffer,
				(rs, rowNum) -> rs.getString("form_model"));
		if(idlist == null || idlist.size() < 1){
			return new ArrayList<>();
		}
		List<FormModelEntity> formModelEntities = query().filterIn("id",idlist).list();
		return formModelEntities;
	}

	@Override
	public DataModel getDataModel(DataModelEntity dataModelEntity){
		DataModel  dataModel = new DataModel();
		BeanUtils.copyProperties(dataModelEntity, dataModel, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
		if(dataModelEntity.getMasterModel() != null) {
			DataModelInfo masterModel = new DataModelInfo();
			BeanUtils.copyProperties(dataModelEntity.getMasterModel(), masterModel, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
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
						if(referenceEntity.getToColumn() == null || referenceEntity.getToColumn().getDataModel() == null){
							continue;
						}
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

	@Override
	public ItemModelEntity findItemByTableAndColumName(String tableName, String columnName) {
		ColumnModelEntity columnModel = columnModelManager.query().filterEqual("dataModel.tableName", tableName).filterEqual("columnName", columnName).first();
		if (columnModel!=null) {
			return itemManager.query().filterEqual("columnModel.id", columnModel.getId()).first();
		}
		return null;
	}

	@Override
	public FormModelEntity findByTableName(String tableName) {
		List<String> idlist = jdbcTemplate.query(" select fd.form_model from ifm_form_data_bind fd,ifm_data_model d where fd.data_model=d.id and d.table_name ='"+tableName+"'",
				(rs, rowNum) -> rs.getString("form_model"));
		if(idlist == null || idlist.size() < 1){
			return null;
		}
		return query().filterIn("id", idlist).first();
	}

	@Override
	public List<FormModelEntity> findProcessApplicationFormModel(String key) {
		List<FormModelEntity>  list = null;
		if(StringUtils.isNotBlank(key)) {
			list = formModelManager.query().filterOr(Filter.isEmpty("process.key"), Filter.equal("process.key", key)).list();
		}else{
			list = formModelManager.query().filterEmpty("process.key").list();
		}
		return list;
	}

	@Override
	public void saveFormModelProcess(FormModel formModel) {
		FormModelEntity formModelEntity = formModel.isNew() ? new FormModelEntity() : formModelManager.get(formModel.getId());
		if(formModelEntity == null){
			throw  new IFormException("未找到【"+formModel.getId()+"】对应的表单模型");
		}
		FormProcessInfo processInfo = null;
		if(formModel.getProcess() != null) {
			processInfo = new FormProcessInfo();
			BeanUtils.copyProperties(formModel.getProcess(), processInfo);
		}
		formModelEntity.setProcess(processInfo);
		formModelManager.save(formModelEntity);
	}

	@Override
	public List<ItemModel> findAllItemModels(List<ItemModel> itemModels) {
		List<ItemModel> itemModelList = new ArrayList<>();
		itemModelList.addAll(itemModels);
		for(ItemModel itemModel : itemModels){
			itemModelList.addAll(getChildRenItemModel(itemModel));
		}
		return itemModelList;
	}

	//设置表单功能
	private void saveFormModelFunctions(FormModelEntity formModelEntity, FormModelEntity paramerEntity) {

		Map<String, ListFunction> oldMap = new HashMap<>();
		for(ListFunction function : formModelEntity.getFunctions()){
			oldMap.put(function.getId(), function);
		}
		List<ListFunction> newFunctions= paramerEntity.getFunctions();
		List<ListFunction> submitFunctions = new ArrayList<>();
		if(newFunctions != null){
			for(ListFunction function : newFunctions){
				boolean isNew = function.isNew();
				ListFunction listFunction = isNew ? new ListFunction() : oldMap.remove(function.getId());
				BeanUtils.copyProperties(function, listFunction, new String[]{"listModel", "formModel"});
				if(isNew){
					Integer orderNo = formFunctionsService.getMaxOrderNo();
					listFunction.setOrderNo(orderNo == null ? 1 : orderNo + 1);
				}
				listFunction.setFormModel(formModelEntity);
				submitFunctions.add(listFunction);
			}
		}
		formModelEntity.setFunctions(submitFunctions);
		for(String key : oldMap.keySet()){
			listFunctionManager.deleteById(key);
		}
	}

	//设置表单业务触发
	private void saveFormModelTriggeres(FormModelEntity formModelEntity, FormModelEntity paramerEntity) {

		Map<String, BusinessTriggerEntity> oldMap = new HashMap<>();
		for(BusinessTriggerEntity triggerEntity : formModelEntity.getTriggeres()){
			oldMap.put(triggerEntity.getId(), triggerEntity);
		}
		List<BusinessTriggerEntity> paramerTriggeres= paramerEntity.getTriggeres();
		List<BusinessTriggerEntity> submitTriggeres = new ArrayList<>();
		if(paramerTriggeres != null){
			for(BusinessTriggerEntity triggerEntity : paramerTriggeres){
				boolean isNew = triggerEntity.isNew();
				BusinessTriggerEntity trigger = isNew ? new BusinessTriggerEntity() : oldMap.remove(triggerEntity.getId());
				BeanUtils.copyProperties(triggerEntity, trigger, new String[]{"formModel"});
				triggerEntity.setFormModel(formModelEntity);
				submitTriggeres.add(triggerEntity);
			}
		}
		formModelEntity.setTriggeres(submitTriggeres);
		for(String key : oldMap.keySet()){
			businessTriggerManager.deleteById(key);
		}
	}

	//设置旧的item参数
	private void deleteItemActivityOption(ItemModelEntity itemModelEntity){
		for (int i = 0 ; i < itemModelEntity.getActivities().size() ; i++) {
			ItemActivityInfo itemActivity = itemModelEntity.getActivities().get(i);
			itemModelEntity.getActivities().remove(itemActivity);
			itemActivityManager.delete(itemActivity);
			i--;
		}
		/*for (int i = 0 ; i < itemModelEntity.getOptions().size() ; i++) {
			ItemSelectOption itemSelectOption = itemModelEntity.getOptions().get(i);
			itemModelEntity.getOptions().remove(itemSelectOption);
			itemSelectOptionManager.delete(itemSelectOption);
			i--;
		}*/
	}


	@Transactional(readOnly = false)
	protected FormModelEntity doUpdate(FormModelEntity entity, boolean dataModelUpdateNeeded, Collection<String> deleteItemActivityIds, Collection<String> deleteItemSelectOptionIds) {
		return doSave(entity, dataModelUpdateNeeded);
	}



	@Transactional(readOnly = false)
	protected FormModelEntity doSave(FormModelEntity entity, boolean dataModelUpdateNeeded) {
		if (dataModelUpdateNeeded) {
			updateDataModel(entity.getDataModels().get(0));
		}
		if(entity.getDataModels() != null && entity.getDataModels().size() > 0 && entity.getDataModels().get(0).getSlaverModels() != null){
			for(DataModelEntity dataModelEntity : entity.getDataModels().get(0).getSlaverModels()){
				for(ItemModelEntity itemModelEntity : entity.getItems()) {
					setItemColumnModel( itemModelEntity, dataModelEntity);
				}
			}
		}

		ItemModelEntity idItemModelEntity = creatItemModelEntityByName(entity,"id");
		if(entity.getDataModels() != null){
			List<ColumnModelEntity> columns = entity.getDataModels().get(0).getColumns();
			for(ColumnModelEntity columnModelEntity : columns){
				if(columnModelEntity.getColumnName().equals("id")){
					idItemModelEntity.setColumnModel(columnModelEntity);
					break;
				}
			}
		}
		FormModelEntity formModelEntity = super.save(entity);
		//提交表单权限
//		listModelService.submitFormBtnPermission(formModelEntity);
		return formModelEntity;
	}

	private void setItemColumnModel(ItemModelEntity itemModelEntity, DataModelEntity dataModelEntity){
		if(itemModelEntity instanceof SubFormItemModelEntity && ((SubFormItemModelEntity) itemModelEntity).getTableName().equals(dataModelEntity.getTableName())) {
			for (ColumnModelEntity columnModelEntity : dataModelEntity.getColumns()) {
				if (columnModelEntity.getColumnName().equals("id")) {
					itemModelEntity.setColumnModel(columnModelEntity);
					break;
				}
			}
		}else if(itemModelEntity instanceof TabsItemModelEntity){
			for(TabPaneItemModelEntity tabPaneItemModelEntity : ((TabsItemModelEntity)itemModelEntity).getItems()){
				for(ItemModelEntity itemModelEntity1 : tabPaneItemModelEntity.getItems()) {
					if (itemModelEntity1 instanceof SubFormItemModelEntity && ((SubFormItemModelEntity) itemModelEntity1).getTableName().equals(dataModelEntity.getTableName())) {
						for (ColumnModelEntity columnModelEntity : dataModelEntity.getColumns()) {
							if (columnModelEntity.getColumnName().equals("id")) {
								itemModelEntity1.setColumnModel(columnModelEntity);
								break;
							}
						}
					}
				}
			}
		}
	}

	private ItemModelEntity creatItemModelEntityByName(FormModelEntity entity, String name){
		List<ItemModelEntity> items = entity.getItems();
		ItemModelEntity idItemModelEntity = null;
		for(int i = 0; i < items.size() ; i++){
			if(items.get(i).getName().equals(name)){
				idItemModelEntity = items.get(i);
				break;
			}
		}
		if(idItemModelEntity == null) {
			idItemModelEntity = new ItemModelEntity();
			idItemModelEntity.setUuid(UUID.randomUUID().toString().replace("-",""));
			idItemModelEntity.setName(name);
			idItemModelEntity.setFormModel(entity);
			idItemModelEntity.setColumnModel(null);
			idItemModelEntity.setType(ItemType.Input);
			if ("id".equals(name)) {
				idItemModelEntity.setProps("{}");
				idItemModelEntity.setSystemItemType(SystemItemType.ID);
			} else {
				idItemModelEntity.setProps(name);
				idItemModelEntity.setSystemItemType(SystemItemType.ChildId);
			}
			items.add(idItemModelEntity);
		}
		return  idItemModelEntity;
	}

	protected void validate(FormModelEntity entity) {
		if (entity.getDataModels().size() == 0) {
			throw new IFormException("必须至少绑定一个数据模型");
		}
		for (DataModelEntity dataModel : entity.getDataModels()) {
			if (dataModel.getId() != null && dataModelManager.find(dataModel.getId()) == null) {
				throw new IFormException(404, "数据模型【" + dataModel.getId() + "】不存在");
			}
		}

		if (entity.getProcess() != null && entity.getProcess().getKey() != null) {
			tech.ascs.icity.iflow.api.model.Process process = processService.get(entity.getProcess().getKey());
			if(process != null) {
				entity.getProcess().setId(process.getId());
				entity.getProcess().setName(process.getName());
				if (process.getActivities() != null && process.getActivities().size() > 0) {
					entity.getProcess().setStartActivity(process.getActivities().get(0).getId());
				}
			}else{
				entity.setProcess(null);
			}
		}
	}

	protected boolean dataModelUpdateNeeded(FormModelEntity entity) {
		if (entity.getProcess() != null && entity.getProcess().getKey() != null) {
			return columnModelManager.query().filterEqual("dataModel.tableName", entity.getDataModels().get(0).getTableName()).filterEqual("columnName", "PROCESS_ID").count() == 0;
		}
		return false;
	}

	protected void updateDataModel(DataModelEntity dataModel) {
		dataModel = dataModelManager.get(dataModel.getId());
		createColumnModel(dataModel, "PROCESS_ID", "流程ID", 64);
		createColumnModel(dataModel, "PROCESS_INSTANCE", "流程实例ID", 64);
		createColumnModel(dataModel, "ACTIVITY_ID", "环节ID", 255);
		createColumnModel(dataModel, "ACTIVITY_INSTANCE", "环节实例ID", 255);
		dataModel = dataModelManager.save(dataModel);
		try {
//			tableUtilService.createTable(dataModel);
		} catch (Exception e) {
			throw new IFormException("更新数据模型【" + dataModel.getName() + "】失败", e);
		}
	}

	protected void createColumnModel(DataModelEntity dataModel, String columnName, String columnDesc, int length) {
		ColumnModelEntity columnModel = new ColumnModelEntity();
		columnModel.setDataModel(dataModel);
		columnModel.setColumnName(columnName);
		columnModel.setName(columnDesc);
		columnModel.setDataType(ColumnType.String);
		columnModel.setLength(length);
		dataModel.getColumns().add(columnModel);
		//columnModelManager.save(columnModel);
	}
}
