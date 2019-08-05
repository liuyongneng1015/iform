package tech.ascs.icity.iform.service.impl;

import com.googlecode.genericdao.search.Filter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.iflow.api.model.Process;
import tech.ascs.icity.iflow.client.ProcessService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.utils.DtoUtils;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.utils.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

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

	@Autowired
	ItemModelService itemModelService;

	@Autowired
	FormInstanceServiceEx formInstanceServiceEx;

	@Autowired
	ELProcessorService elProcessorService;

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
			//主键id
			for(String key : new ArrayList<>(oldMapItmes.keySet())){
				ItemModelEntity itemModelEntity = oldMapItmes.get(key);
				if(itemModelEntity.getSystemItemType() == SystemItemType.ID && itemModelEntity.getFormModel() != null && itemModelEntity.getFormModel().getId().equals(entity.getId())){
					itemModelEntities.add(oldMapItmes.get(key));
					oldMapItmes.remove(key);
					break;
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
		if((paramerItemModelEntity instanceof NumberItemModelEntity && saveItemModelEntity instanceof NumberItemModelEntity &&
						((NumberItemModelEntity)paramerItemModelEntity).getDecimalDigits() != ((NumberItemModelEntity)saveItemModelEntity).getDecimalDigits() &&
								(((NumberItemModelEntity)paramerItemModelEntity).getDecimalDigits() == 0 || ((NumberItemModelEntity)saveItemModelEntity).getDecimalDigits() == 0))
		|| (paramerItemModelEntity instanceof TimeItemModelEntity && saveItemModelEntity instanceof TimeItemModelEntity &&
				!((TimeItemModelEntity) paramerItemModelEntity).getTimeFormat().equals(((TimeItemModelEntity) saveItemModelEntity).getTimeFormat()))){
			//删除字段
			if(oldColumnName != null) {
				DataModelEntity dataModelEntity = saveItemModelEntity.getColumnModel().getDataModel();
				String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
				ColumnModelEntity column = saveItemModelEntity.getColumnModel();
				String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
				columnModelService.deleteTableColumn(tableName, columnName);
			}
		}
		ReferenceType newReferenceType = null;
		String newReferenceFormId = null;
		if(paramerItemModelEntity  instanceof ReferenceItemModelEntity){
			newReferenceType = ((ReferenceItemModelEntity)paramerItemModelEntity).getReferenceType();
			newReferenceFormId = ((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceFormId();
		}
		setItempermissions(saveItemModelEntity, paramerItemModelEntity);
		setOptions(saveItemModelEntity, paramerItemModelEntity);


		BeanUtils.copyProperties(paramerItemModelEntity, saveItemModelEntity, new String[]{"referencesItemModels","parentItem", "searchItems","sortItems","permissions", "referenceList","items","formModel","columnModel","activities","options"});

		// BeanUtils.copyProperties忽略null的属性，树形下拉控件的默认值没传值过来时，应该要把树形下拉控件的默认值置空，但是copyProperties忽略null的属性
		if (paramerItemModelEntity instanceof TreeSelectItemModelEntity) {
			TreeSelectItemModelEntity treeSelectItemEntity = (TreeSelectItemModelEntity)paramerItemModelEntity;
			((TreeSelectItemModelEntity)saveItemModelEntity).setDefaultValue(treeSelectItemEntity.getDefaultValue());
		}

		//设置列表模型
		if (paramerItemModelEntity instanceof ReferenceItemModelEntity ) {
			if(((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceList() != null) {
				ListModelEntity listModelEntity = listModelManager.find(((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceList().getId());
				((ReferenceItemModelEntity) saveItemModelEntity).setReferenceList(listModelEntity);
			}
			((ReferenceItemModelEntity)saveItemModelEntity).setParentItem(((ReferenceItemModelEntity) paramerItemModelEntity).getParentItem());
			((ReferenceItemModelEntity)saveItemModelEntity).setItemUuids(((ReferenceItemModelEntity) paramerItemModelEntity).getItemUuids());

			//删除字段删除索引
			if(oldColumnName != null && (!StringUtils.equalsIgnoreCase(oldColumnName, newColunmName)
					|| !StringUtils.equalsIgnoreCase(newReferenceFormId, oldReferenceFormId) || oldReferenceType != newReferenceType)) {
				DataModelEntity dataModelEntity = saveItemModelEntity.getColumnModel().getDataModel();
				String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
				ColumnModelEntity column = saveItemModelEntity.getColumnModel();
				String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
				columnModelService.deleteTableColumn(tableName, columnName);
			}
		}

		//设置下拉联动
		if (paramerItemModelEntity instanceof SelectItemModelEntity) {
			((SelectItemModelEntity)saveItemModelEntity).setReferenceDictionaryId(((SelectItemModelEntity) paramerItemModelEntity).getReferenceDictionaryId());
			((SelectItemModelEntity)saveItemModelEntity).setReferenceDictionaryItemId(((SelectItemModelEntity) paramerItemModelEntity).getReferenceDictionaryItemId());
			((SelectItemModelEntity)saveItemModelEntity).setParentItem(((SelectItemModelEntity) paramerItemModelEntity).getParentItem());
		}

		setColumnModelEntity(modelEntityMap,  paramerItemModelEntity,  saveItemModelEntity);

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
		setItempermissions(newItemModelEntity, oldItemModelEntity);
		setOptions(newItemModelEntity, oldItemModelEntity);

		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"permissions","searchItems", "sortItems", "parentItem","referenceList","items","formModel","columnModel","activities","options"});

		return newItemModelEntity;
	}

	private TabPaneItemModelEntity  getNewTabPaneItemModel(Map<String, ItemModelEntity> oldMapItmes, TabPaneItemModelEntity oldItemModelEntity){
		TabPaneItemModelEntity newItemModelEntity = new TabPaneItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = (TabPaneItemModelEntity)oldMapItmes.get(oldItemModelEntity.getId());
		}
		setItempermissions(newItemModelEntity, oldItemModelEntity);
		setOptions(newItemModelEntity, oldItemModelEntity);
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
							columnModelService.deleteTable( columnReferenceEntity.getReferenceMiddleTableName() + "_list");
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
					DataModelEntity dataModelEntity = itemModelEntity.getColumnModel().getDataModel();
					String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
					columnModelService.deleteTable(tableName);
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
			DataModelEntity dataModelEntity = itemModelEntity.getColumnModel().getDataModel();
			String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
			ColumnModelEntity column = itemModelEntity.getColumnModel();
			String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
			columnModelService.deleteTableColumn(tableName, columnName);
		}
		itemManager.delete(itemModelEntity);
	}

	//软删除控件关联实体
	@Override
	public void deleteItemOtherReferenceEntity(ItemModelEntity itemModelEntity){
		List<ListSearchItem> listSearchItems = listSearchItemManager.query().filterEqual("itemModel.id", itemModelEntity.getId()).list();
		for(int i = 0 ; i < listSearchItems.size() ; i++){
			ListSearchItem listSearchItem = listSearchItems.get(i);
			listSearchItem.setItemModel(null);
			listSearchItems.remove(listSearchItem);
			i--;
			listSearchItemManager.delete(listSearchItem);
		}

		List<ListSortItem> listSortItems = listSortItemManager.query().filterEqual("itemModel.id", itemModelEntity.getId()).list();
		for(int i = 0 ; i < listSortItems.size() ; i++){
			ListSortItem listSortItem = listSortItems.get(i);
			listSortItem.setItemModel(null);
			listSortItems.remove(listSortItem);
			i--;
			listSortItemManager.delete(listSortItem);
		}

		List<QuickSearchEntity> quickSearchEntities = quickSearchEntityManager.query().filterEqual("itemModel.id", itemModelEntity.getId()).list();
		for(int i = 0 ; i < quickSearchEntities.size() ; i++){
			QuickSearchEntity quickSearch = quickSearchEntities.get(i);
			quickSearch.setItemModel(null);
			quickSearchEntities.remove(quickSearch);
			i--;
			quickSearchEntityManager.delete(quickSearch);
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
					listModelManager.save(listModelEntity);
				}
			}
		}
	}

	//设备表单权限
	private void setItempermissions(ItemModelEntity saveItemModelEntity, ItemModelEntity paramerItemModelEntity){
		if(paramerItemModelEntity.getColumnModel() != null && !(paramerItemModelEntity instanceof SubFormItemModelEntity) && "id".equals(paramerItemModelEntity.getColumnModel().getColumnName())) {
			return;
		}
		//旧数据
		List<ItemPermissionInfo> itemPermissionInfos = saveItemModelEntity.getPermissions();
		Map<String, ItemPermissionInfo> oldItemPermission = new HashMap<>();
		if(itemPermissionInfos != null) {
			for (ItemPermissionInfo itemPermissionInfo : itemPermissionInfos) {
				oldItemPermission.put(itemPermissionInfo.getId(), itemPermissionInfo);
			}
		}

		List<ItemPermissionInfo> newItemPermissionInfos = new ArrayList<>();
		if(saveItemModelEntity.isNew() && (paramerItemModelEntity.getPermissions() == null || paramerItemModelEntity.getPermissions().size() < 1)) {
			newItemPermissionInfos.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Add));
			newItemPermissionInfos.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Update));
			newItemPermissionInfos.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Check));
		}else{
			for(ItemPermissionInfo itemPermissionInfo : paramerItemModelEntity.getPermissions()){
				ItemPermissionInfo newItemPermiss = itemPermissionInfo.isNew() ? new ItemPermissionInfo() : oldItemPermission.remove(itemPermissionInfo.getId());
				BeanUtils.copyProperties(itemPermissionInfo, newItemPermiss, new String[]{"itemModel"});
				newItemPermiss.setItemModel(saveItemModelEntity);
				newItemPermissionInfos.add(newItemPermiss);
			}
		}
		List<ItemPermissionInfo> list = saveItemModelEntity.getPermissions();
		saveItemModelEntity.setPermissions(newItemPermissionInfos);
		List<String> idList = new ArrayList<>(oldItemPermission.keySet());
		if(idList.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                ItemPermissionInfo info = list.get(i);
                if (idList.contains(info.getId())) {
                    list.remove(info);
                    info.setItemModel(null);
                    i--;
                    itemPermissionManager.delete(info);
                }
            }
        }
	}

	//得到最新的item
	private void setOptions(ItemModelEntity newEntity, ItemModelEntity paramerItemModelEntity){
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
		List<ItemSelectOption> list = newEntity.getOptions();
		newEntity.setOptions(options);
		List<String> idList = new ArrayList<>(itemSelectOptionMap.keySet());
		if(idList.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				ItemSelectOption info = list.get(i);
				if (idList.contains(info.getId())) {
					list.remove(info);
					info.setItemModel(null);
					i--;
					itemSelectOptionManager.delete(info);
				}
			}
		}
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
		ItemModelEntity entity = null;
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
			case  ProcessLog:
				entity = new ProcessLogItemModelEntity();
				break;
            case ReferenceInnerLabel:
                entity = new ReferenceInnerItemModelEntity();
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
		}else if(systemItemType == SystemItemType.ProcessStatus
				|| systemItemType == SystemItemType.ProcessPrivateStatus){
			entity = new ProcessStatusItemModelEntity();
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
		FormProcessInfo oldProcessInfo = oldFormModelEntity.getProcess();
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

		DataModelEntity dataModelEntity = oldFormModelEntity.getDataModels().get(0);
		//流程有关字段
		columnModelService.saveColumnModelEntity(dataModelEntity, "PROCESS_ID");
		columnModelService.saveColumnModelEntity(dataModelEntity, "PROCESS_INSTANCE");
		columnModelService.saveColumnModelEntity(dataModelEntity, "ACTIVITY_ID");
		columnModelService.saveColumnModelEntity(dataModelEntity, "ACTIVITY_INSTANCE");

		formModelManager.save(oldFormModelEntity);
		//同步流程字段
		dataModelService.sync(oldFormModelEntity.getDataModels().get(0));

		if((oldProcessInfo == null && entity.getProcess() != null) 	|| (oldProcessInfo != null && entity.getProcess() == null )
				|| (oldProcessInfo != null && entity.getProcess() != null && !oldProcessInfo.getKey().equals(entity.getProcess().getKey()))){
			//删除旧数据
			formInstanceServiceEx.deleteFormData(oldFormModelEntity);
		}
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

		DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
		//流程有关的字段
		columnModelService.saveColumnModelEntity(dataModelEntity, "PROCESS_ID");
		columnModelService.saveColumnModelEntity(dataModelEntity, "PROCESS_INSTANCE");
		columnModelService.saveColumnModelEntity(dataModelEntity, "ACTIVITY_ID");
		columnModelService.saveColumnModelEntity(dataModelEntity, "ACTIVITY_INSTANCE");

		//旧的流程
		FormProcessInfo oldProcessInfo = formModelEntity.getProcess();

		FormProcessInfo processInfo = null;
		if(formModel.getProcess() != null) {
			processInfo = new FormProcessInfo();
			BeanUtils.copyProperties(formModel.getProcess(), processInfo);
		}

		formModelEntity.setProcess(processInfo);
		formModelManager.save(formModelEntity);
		//同步流程字段
		dataModelService.sync(formModelEntity.getDataModels().get(0));

		if((oldProcessInfo == null && formModel.getProcess() != null) 	|| (oldProcessInfo != null && formModel.getProcess() == null )
				|| (oldProcessInfo != null && formModel.getProcess() != null && !oldProcessInfo.getKey().equals(formModel.getProcess().getKey()))){
			//删除旧数据
			formInstanceServiceEx.deleteFormData(formModelEntity);
		}
	}

	@Override
	public AnalysisFormModel toAnalysisDTO(FormModelEntity entity, Map<String, Object> parameters) {
		String parseArea = parameters == null ? null : (String)parameters.get("parseArea");
		DefaultFunctionType functionType = parameters == null ? null : DefaultFunctionType.getByValue((String)parameters.get("functionType"));
		if(parameters != null) {
			parameters.remove("parseArea");
			parameters.remove("functionType");
		}

		AnalysisFormModel formModel = new AnalysisFormModel();
		entityToDTO(entity,  formModel, true, parseArea, functionType);

		List<AnalysisDataModel> dataModelList = new ArrayList<>();
		List<ItemModelEntity> itemModelEntities = findAllItems(entity);
		boolean isFlowForm = false;
		setFlowParams(entity, null, isFlowForm);

		Map<String, DataModelEntity> dataModelEntities = new HashMap<>();
		Map<String, List<String>> columnsMap = new HashMap<>();
		//关联表单
		Set<AnalysisFormModel> referenceFormModelList = new HashSet<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			if(itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getReferenceList() != null) {
				setPCReferenceItemModel((ReferenceItemModelEntity)itemModelEntity, referenceFormModelList, dataModelEntities, columnsMap, parseArea);
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
				items.add(itemModelService.toDTO(itemModelEntity, true, tableName));
			}
			formModel.setItems(items);
			updateItemDefaultName( items, parameters);
		}else{
			formModel.setItems(null);
		}
		return formModel;
	}

	//设置表单流程数据
	@Override
	public void setFlowParams(FormModelEntity entity, List<Activity> activities , boolean isFlowForm){
		if(entity.getProcess() == null || !org.springframework.util.StringUtils.hasText(entity.getProcess().getKey())){
			return;
		}
		try {
			Process process = null ;
			if(entity.getProcess().getKey() != null) {
				process = processService.get(entity.getProcess().getKey());
				if (process != null) {
					isFlowForm = true;
				}
			}
			if(activities != null && process != null) {
				activities.addAll(process.getActivities());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//设置表单控件数据标识
	@Override
	public List<ItemModel> getItemModelList(List<String> idResultList){
		if(idResultList == null || idResultList.size() < 1){
			return null;
		}
		List<ItemModelEntity> itemModelEntities = new ArrayList<>();
		for(String itemId : idResultList) {
			ItemModelEntity itemModelEntity = itemModelService.find(itemId);
			if (itemModelEntity!=null) {
				itemModelEntities.add(itemModelEntity);
			}
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

	//设置表单实体装模型
	@Override
	public FormModel toDTODetail(FormModelEntity entity)  {
		FormModel formModel = new FormModel();
		entityToDTO( entity,  formModel, false, null, null);
		List<Activity> activities = new ArrayList<>();
		//是否流程表单
		boolean isFlowForm = false;
		setFlowParams(entity, activities, isFlowForm);

		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			List<ItemModelEntity> itemModelEntities = entity.getItems() == null || entity.getItems().size() < 2 ? entity.getItems() : entity.getItems().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

			//设置控件权限
			List<ItemPermissionModel> itemPermissionModels = setItemPermissions(findAllItems(entity), isFlowForm);
			formModel.setPermissions(itemPermissionModels);
			String tableName = entity.getDataModels() == null || entity.getDataModels().size() < 1 ? null :  entity.getDataModels().get(0).getTableName();
			for (ItemModelEntity itemModelEntity : itemModelEntities) {
				ItemModel itemModel = itemModelService.toDTO(itemModelEntity, false, tableName);
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
				dataModelList.add(getDataModel(dataModelEntity));
			}
			formModel.setDataModels(dataModelList);
		}

		List<ItemModel> itemModels = findAllItemModels(formModel.getItems());
		itemModelService.setFormItemActvitiy(itemModels,  activities);

		return formModel;
	}

	//模型转实体
	@Override
	public FormModelEntity wrap(FormModel formModel) {
		veryFormModel(formModel);
		FormModelEntity entity = new FormModelEntity();
		BeanUtils.copyProperties(formModel, entity, new String[] {"items","dataModels","permissions","submitChecks","functions","triggeres"});

		verifyFormModelName(formModel);

		//TODO 获取主数据模型
		DataModel masterDataModel = null;
		for(DataModel dataModel : formModel.getDataModels()){
			if(dataModel.getMasterModel() == null){
				masterDataModel = dataModel;
				break;
			}
		}
		dataModelService.verifyDataModel(formModel,  masterDataModel);

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
			itemModelService.setItemModelEntity(formModel, itemModel, entity, items,	itemModelEntityList,  formMap);
		}
		// 同步数据库中以后的模板参数
		syncExportItemParams(entity, itemModelEntityList);
		formMap.put(masterDataModel.getTableName(), itemModelEntityList);

		Map<String, DataModel> newDataModelMap = new HashMap<>();
		for(DataModel dataModel : formModel.getDataModels()){
			newDataModelMap.put(dataModel.getTableName(), dataModel);
		}

		for(String key : formMap.keySet()){
			setReference(newDataModelMap.get(key), formMap.get(key));
		}

		//设置主表字段
		dataModelService.setMasterDataModelEntity(masterDataModelEntity, masterDataModel, formModel, oldMasterDataModelMap);

		//设置数据模型结构了
		List<DataModelEntity> dataModelEntities = new ArrayList<>();
		dataModelEntities.add(masterDataModelEntity);
		entity.setDataModels(dataModelEntities);
		entity.setItems(items);

		Map<String, ItemModelEntity> uuidItemModelEntityMap = new HashMap<>();

		List<ItemModelEntity> itemlist = findAllItems(entity);
		verifyProcessItemModel(itemlist);

		for(ItemModelEntity itemModelEntity : itemlist){
			if(org.springframework.util.StringUtils.hasText(itemModelEntity.getUuid())) {
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
			deleteDataModel(oldMasterDataModelMap.get(key));
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
				if(org.springframework.util.StringUtils.hasText(itemModel1.getUuid())) {
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

	//删除数据建模
	private void deleteDataModel(DataModelEntity dataModelEntity){
		dataModelService.deleteDataModelWithoutVerify(dataModelEntity);
	}


	//提交表单提交校验
	private void wrapFormModelSubmitCheck(FormModelEntity entity, FormModel formModel) {
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
				checkInfos.add(checkInfo);
			}
			List<FormSubmitCheckInfo> checkInfoList = checkInfos.size() < 2 ? checkInfos : checkInfos.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			entity.setSubmitChecks(checkInfoList);
		}
	}

	//设置表单功能
	private void wrapFormFunctions(FormModelEntity entity, FormModel formModel) {
		if(formModel.getFunctions() != null){
			List<ListFunction> functions = new ArrayList<>();
			for (int i = 0; i < formModel.getFunctions().size(); i++) {
				FunctionModel model = formModel.getFunctions().get(i);
				ListFunction function =  new ListFunction();
				BeanUtils.copyProperties(model, function, new String[]{"formModel", "parseArea"});
				if (model.getParseArea()!=null && model.getParseArea().size()>0) {
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

	//校验流程表单控件
	private void verifyProcessItemModel(List<ItemModelEntity> list){
		Map<String, Object> processItemModelMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity : list) {
			if (itemModelEntity.getSystemItemType() != null && (itemModelEntity.getSystemItemType() == SystemItemType.ProcessStatus
					|| itemModelEntity.getSystemItemType() == SystemItemType.ProcessLog
					|| itemModelEntity.getSystemItemType() == SystemItemType.ProcessPrivateStatus)) {
				if (processItemModelMap.get(itemModelEntity.getSystemItemType().getValue()) != null) {
					if (itemModelEntity.getSystemItemType() == SystemItemType.ProcessStatus) {
						throw new IFormException("事件流程状态控件重复");
					}
					if (itemModelEntity.getSystemItemType() == SystemItemType.ProcessPrivateStatus) {
						throw new IFormException("个人流程状态控件重复");
					}
					if (itemModelEntity.getSystemItemType() == SystemItemType.ProcessLog) {
						throw new IFormException("流程日志控件重复");
					}
				}
				processItemModelMap.put(itemModelEntity.getSystemItemType().getValue(), System.currentTimeMillis());
			}
		}
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
				FormModelEntity formModelEntity = find(((ReferenceItemModelEntity) itemModelEntity).getReferenceFormId());
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

	//校验表单建模
	private void veryFormModel(FormModel formModel){
		if(!formModel.isNew()){
			FormModelEntity formModelEntity = find(formModel.getId());
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
				if(!org.springframework.util.StringUtils.hasText(function.getAction()) || !org.springframework.util.StringUtils.hasText(function.getLabel())){
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
				if(!org.springframework.util.StringUtils.hasText(triggerModel.getUrl()) || !triggerModel.getUrl().startsWith("http") ){
					throw new IFormException("调用微服务地址格式错误");
				}
				if(map.get(triggerModel.getType().getValue()) != null){
					throw new IFormException("功能编码重复");
				}
				map.put(triggerModel.getType().getValue(), triggerModel.getUrl());
			}
		}
	}


	//校验表单名称
	@Override
	public void verifyFormModelName(FormModel formModel){
		if(formModel == null || org.springframework.util.StringUtils.isEmpty(formModel.getName())){
			return;
		}
		if(org.springframework.util.StringUtils.isEmpty(formModel.getApplicationId())){
			throw new IFormException("表单未关联应用");
		}
		List<FormModelEntity> list  = query().filterEqual("name", formModel.getName()).filterEqual("applicationId", formModel.getApplicationId()).list();
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

	@Override
	public FormModel toDTO(FormModelEntity entity, boolean setFormProcessFlag) {
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
		List<ItemModelEntity> itemModelEntityList = getAllColumnItems(entity.getItems());
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
		if(entity.getProcess() != null && org.springframework.util.StringUtils.hasText(entity.getProcess().getKey())){
			try {
				Process process = processService.get(entity.getProcess().getKey());
				if(process != null){
					activities.addAll(process.getActivities());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		itemModelService.setFormItemActvitiy( formModel.getItems(),  activities);
	}

	private List<ItemPermissionModel> setItemPermissions(List<ItemModelEntity> items, boolean isFlowForm){
		List<ItemPermissionModel> itemPermissionsList = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : items){
			ColumnModelEntity columnModelEntity = itemModelEntity.getColumnModel();
			ItemPermissionModel itemPermissionModel = setItemPermissionModel(itemModelEntity);
			if(itemModelEntity.getPermissions() != null && itemModelEntity.getPermissions().size() > 0){
				for(ItemPermissionInfo itemPermissionInfo : itemModelEntity.getPermissions()) {
					ItemPermissionInfoModel itemPermissionInfoModel = new ItemPermissionInfoModel();
					BeanUtils.copyProperties(itemPermissionInfo, itemPermissionInfoModel, new String[]{"itemModel"});
					if(isFlowForm){
						if(itemPermissionInfoModel.getCanFill() == null){
							itemPermissionInfoModel.setCanFill(false);
						}
						if(itemPermissionInfoModel.getVisible() == null){
							itemPermissionInfoModel.setVisible(false);
						}
						if(itemPermissionInfoModel.getRequired() == null){
							itemPermissionInfoModel.setRequired(false);
						}
					}
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
				if(columnModelEntity != null){
					displayTimingTypes.add(DisplayTimingType.Add);
					if(!isFlowForm) {
						displayTimingTypes.add(DisplayTimingType.Update);
						displayTimingTypes.add(DisplayTimingType.Check);
					}
				}else{
					if(!isFlowForm) {
						displayTimingTypes.add(DisplayTimingType.Check);
					}
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
						if(!isFlowForm) {
							permissionInfoModel.setCanFill(null);
							permissionInfoModel.setRequired(null);
						}
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


	//更新表单默认值
	private void updateItemDefaultName(List<ItemModel> items, Map<String, Object> parameters){
		List<ItemModel> itemModels = findAllItemModels(items);
		for(ItemModel itemModel : itemModels){
			if(parameters == null || itemModel.getColumnModel() == null ){
				continue;
			}
			Object defaultValue = parameters.get(itemModel.getId());
			if(defaultValue == null || !org.springframework.util.StringUtils.hasText(defaultValue.toString())) {
				defaultValue = parameters.get(itemModel.getColumnModel().getColumnName());
			}
			if(defaultValue == null || !org.springframework.util.StringUtils.hasText(defaultValue.toString())){
				continue;
			}
			ItemModelEntity itemModelEntity = itemModelService.find(itemModel.getId());
			ItemInstance itemInstance = new ItemInstance();
			if(defaultValue instanceof List){
				defaultValue = String.join(",", (List<String>)defaultValue);
			}else if(defaultValue instanceof String[]){
				defaultValue = String.join(",", Arrays.asList((String[])defaultValue));
			}else if(itemModel.getSystemItemType() == SystemItemType.CreateDate || itemModel.getType() == ItemType.DatePicker){
				defaultValue = new Date(Long.parseLong(String.valueOf(defaultValue)));
			}
			formInstanceServiceEx.updateValue(itemModelEntity, itemInstance, defaultValue);
			itemModel.setDefaultValue(itemInstance.getValue());
			itemModel.setDefaultValueName(itemInstance.getDisplayValue());
		}
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
										 Map<String, List<String>> columnsMap, String parseArea){
		AnalysisFormModel referencePCFormModel = new AnalysisFormModel();
		entityToDTO(itemModelEntity.getReferenceList().getMasterForm(), referencePCFormModel, true, parseArea, null);
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
		if(org.springframework.util.StringUtils.hasText(itemModelEntity.getReferenceList().getDisplayItemsSort())) {
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

	@Override
	public void entityToDTO(FormModelEntity entity, Object object, boolean isAnalysisForm, String parseArea, DefaultFunctionType functionType){
		BeanUtils.copyProperties(entity, object, new String[] {"dataModels","items","permissions","submitChecks","functions", "triggeres"});
		if(entity.getFunctions() != null && entity.getFunctions().size() > 0){
			List<ListFunction> functions = entity.getFunctions().parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
			List<FunctionModel> functionModels = new ArrayList<>();
			for (int i = 0; i < functions.size(); i++) {
				ListFunction function = functions.get(i);
				if(parseArea != null && (function.getParseArea() == null || !function.getParseArea().contains(parseArea))){
					continue;
				}
				FunctionModel functionModel = new FunctionModel();
				BeanUtils.copyProperties(function, functionModel, new String[] {"formModel","itemModel", "parseArea"});
				if (org.springframework.util.StringUtils.hasText(function.getParseArea())) {
					functionModel.setParseArea(Arrays.asList(function.getParseArea().split(",")));
				}
				functionModel.setOrderNo(i+1);
				functionModels.add(functionModel);
			}
			if(isAnalysisForm) {
				((AnalysisFormModel) object).setFunctions(functionModels.size() < 1 ? null : functionModels);
				if(entity.getProcess() != null && functionType != null && functionType != DefaultFunctionType.Add) {
					((AnalysisFormModel) object).setFunctions(null);
				}
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
		if(org.springframework.util.StringUtils.hasText(entity.getItemModelIds())) {
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
		if(org.springframework.util.StringUtils.hasText(entity.getQrCodeItemModelIds())) {
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

		//表单提交校验
		if (entity.getSubmitChecks() != null) {
			List<FormSubmitCheckModel> submitChecks = entity.getSubmitChecks().stream()
					.map(DtoUtils::toFormSubmitCheckModel)
					.sorted()
					.collect(Collectors.toList());
			if(isAnalysisForm) {
				((AnalysisFormModel) object).setSubmitChecks(submitChecks);
			}else{
				((FormModel) object).setSubmitChecks(submitChecks);
			}
		}

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
                setFunction(function, listFunction);
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

	//设置表单功能
	private void setFunction(ListFunction functionParams, ListFunction newListFunction){
        newListFunction.setUrl(functionParams.getUrl());
        // 请求方式，GET、HEAD、POST、PUT、DELETE、CONNECT、OPTIONS、TRACE
        newListFunction.setMethod(functionParams.getMethod());
        newListFunction.setIcon(functionParams.getIcon());
        newListFunction.setStyle(functionParams.getStyle());
        newListFunction.setParamCondition(functionParams.getParamCondition());
        newListFunction.setFunctionType(functionParams.getFunctionType());
        newListFunction.setHasConfirmForm(functionParams.getHasConfirmForm());
        newListFunction.setConfirmForm(functionParams.getConfirmForm());
        newListFunction.setReturnOperation(functionParams.getReturnOperation());
        newListFunction.setJumpNewUrl(functionParams.getJumpNewUrl());
        //显示时机 若为空标识所有时机都显示
        newListFunction.setDisplayTiming(functionParams.getDisplayTiming());
        // 返回结果
        newListFunction.setReturnResult(functionParams.getReturnResult());
        // 解析区域
        newListFunction.setParseArea(functionParams.getParseArea());
        // 是否是系统的按钮
        newListFunction.setSystemBtn(functionParams.getSystemBtn());
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

	private void syncExportItemParams(FormModelEntity formModelEntity, List<ItemModelEntity> entities) {
		Map<String, ItemModelEntity> idMapping = itemModelService.findByProperty("formModel", formModelEntity)
				.stream()
				.collect(Collectors.toMap(ItemModelEntity::getId, i -> i));
		entities.stream().filter(item -> org.springframework.util.StringUtils.hasText(item.getId())).forEach(item -> {
			ItemModelEntity itemEntity = idMapping.get(item.getId());
			if (itemEntity != null) {
				item.setTemplateName(itemEntity.getName());
				item.setExampleData(itemEntity.getExampleData());
				item.setTemplateSelected(itemEntity.isTemplateSelected());
				item.setDataImported(itemEntity.isDataImported());
				item.setMatchKey(itemEntity.isMatchKey());
			}else {
				item.setTemplateName(item.getName());
			}

		});
		entities.stream().filter(item -> org.springframework.util.StringUtils.isEmpty(item.getId())).forEach(item -> {
			item.setTemplateName(item.getName());
		});
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
			idItemModelEntity.setProps("{}");
			if ("id".equals(name)) {
				idItemModelEntity.setSystemItemType(SystemItemType.ID);
				idItemModelEntity.setTypeKey(SystemItemType.ID.getValue());
			} else {
				idItemModelEntity.setSystemItemType(SystemItemType.ChildId);
			}
			items.add(idItemModelEntity);
			entity.setItems(items);
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
			tech.ascs.icity.iflow.api.model.Process process = null;
			try {
				process =processService.get(entity.getProcess().getKey());
			} catch (Exception e){
				e.printStackTrace();
			}
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
