package tech.ascs.icity.iform.service.impl;

import java.util.*;
import java.util.stream.Collectors;

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


	private JPAManager<ListSearchItem> listSearchItemManager;

	private JPAManager<ListSortItem> listSortItemManager;

	private JPAManager<QuickSearchEntity> quickSearchEntityManager;


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
		listSearchItemManager = getJPAManagerFactory().getJPAManager(ListSearchItem.class);
		listSortItemManager = getJPAManagerFactory().getJPAManager(ListSortItem.class);
		quickSearchEntityManager = getJPAManagerFactory().getJPAManager(QuickSearchEntity.class);
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

			BeanUtils.copyProperties(entity, old, new String[] {"dataModels", "items", "permissions", "submitChecks","functions"});

			//删除活动
			deleteOldItems(old);

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
				ItemModelEntity oldItemModelEntity = entity.getItems().get(i);
				//oldItemModelEntity.setFormModel(old);
				ItemModelEntity newItemModelEntity = getNewItemModelEntity(oldMapItmes, columnModelEntityMap, oldItemModelEntity);
				newItemModelEntity.setFormModel(old);
				newItemModelEntity.setOrderNo(i);
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

			//删除item
			deleteItems(new ArrayList<>(oldMapItmes.values()));

			//下拉数据字典联动控件
			setParentItem(itemModelEntities);

			old.setItems(itemModelEntities);

			//设置表单功能
			saveFormModelFunctions(old, entity);

			//设计表单校验
			setFormSubmitChecks(old, entity);

			//设置关联关系
			//setReferenceItems(deletedItemIds, idColumnModelEntity, allItems);
			FormModelEntity formModelEntity = doSave(old, dataModelUpdateNeeded);

			List<ItemModelEntity> allColumns = new ArrayList<>();
			allColumns.addAll(formModelEntity.getItems());
			for(ItemModelEntity itemModelEntity : formModelEntity.getItems()) {
				allColumns.addAll(getChildRenItemModelEntity(itemModelEntity));
			}
			for(int i = 0 ;i <  allColumns.size() ; i++){
				ItemModelEntity itemModelEntity = allColumns.get(i);
				if(itemModelEntity instanceof ReferenceItemModelEntity){
					if(((ReferenceItemModelEntity) itemModelEntity).getItemTableColunmName() != null && ((ReferenceItemModelEntity) itemModelEntity).getType() == ItemType.ReferenceList ){
						((ReferenceItemModelEntity) itemModelEntity).setItemModelIds(String.join(",",
								getReferenceItemModelList((ReferenceItemModelEntity)itemModelEntity).parallelStream().map(ItemModelEntity::getId).collect(Collectors.toList())));
					}else{
						((ReferenceItemModelEntity) itemModelEntity).setItemModelIds(null);
					}
					if(((ReferenceItemModelEntity) itemModelEntity).getSelectMode() == SelectMode.Attribute ){

						if(((ReferenceItemModelEntity) itemModelEntity).getReferenceUuid() != null && ((ReferenceItemModelEntity) itemModelEntity).getParentItem() == null){
							ItemModelEntity referenceItemModelEntity = itemManager.query().filterEqual("uuid", ((ReferenceItemModelEntity) itemModelEntity).getReferenceUuid()).first();
							((ReferenceItemModelEntity) itemModelEntity).setParentItem((ReferenceItemModelEntity)referenceItemModelEntity);
						}

						if(((ReferenceItemModelEntity) itemModelEntity).getParentItem() != null) {
							FormModelEntity formModelEntity1 = formModelManager.find(((ReferenceItemModelEntity) itemModelEntity).getParentItem().getReferenceFormId());
							if(formModelEntity1 != null) {
								ItemModelEntity itemModelEntity1 = getItemModelByTableAndColumn(formModelEntity1, ((ReferenceItemModelEntity) itemModelEntity).getItemTableColunmName());
								((ReferenceItemModelEntity) itemModelEntity).setReferenceItemId(itemModelEntity1 == null ? null : itemModelEntity1.getId());
							}
						}

					}

					itemManager.save(itemModelEntity);
				}
			}

			return formModelEntity;
		}
		return doSave(entity, dataModelUpdateNeeded);

	}

	@Override
	public List<ItemModelEntity> getReferenceItemModelList(ReferenceItemModelEntity itemModelEntity){
		List<ItemModelEntity> itemModelEntityList = getAllColumnItems(get(itemModelEntity.getReferenceFormId()).getItems());
		Map<String, ItemModelEntity> colunmMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity1 : itemModelEntityList){
			colunmMap.put(itemModelEntity1.getColumnModel().getDataModel().getTableName()+"_"+itemModelEntity1.getColumnModel().getColumnName(), itemModelEntity1);
		}
		List<ItemModelEntity> list = new ArrayList<>();
		if(itemModelEntity.getItemTableColunmName() != null){
			String[] strings =  itemModelEntity.getItemTableColunmName().split(",");
			for(String str : strings){
				list.add(colunmMap.get(str));
			}
		}
		return list;
	}

	private ItemModelEntity getItemModelByTableAndColumn(FormModelEntity formModelEntity, String key){
		List<ItemModelEntity> itemModelEntityList = getAllColumnItems(formModelEntity.getItems());
		Map<String, ItemModelEntity> colunmMap = new HashMap<>();
		for(ItemModelEntity itemModelEntity1 : itemModelEntityList){
			colunmMap.put(itemModelEntity1.getColumnModel().getDataModel().getTableName()+"_"+itemModelEntity1.getColumnModel().getColumnName(), itemModelEntity1);
		}
		return colunmMap.get(key);
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
			if(itemModelEntity.getColumnModel() != null && itemModelEntity.getColumnModel().getDataModel() != null){
				map.put(itemModelEntity.getColumnModel().getDataModel().getTableName()+"_"+itemModelEntity.getColumnModel().getColumnName(), itemModelEntity);
			}
			for(ItemModelEntity itemModel : getChildRenItemModelEntity(itemModelEntity)) {
				if(itemModel.getColumnModel() != null && itemModel.getColumnModel().getDataModel() != null){
					map.put(itemModel.getColumnModel().getDataModel().getTableName()+"_"+itemModel.getColumnModel().getColumnName(), itemModel);
				}
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
		if(selectItemModelEntity.getParentItem() != null && selectItemModelEntity.getParentItem().getColumnModel() != null) {
			parentSelectItem = (SelectItemModelEntity) map.get(selectItemModelEntity.getParentItem().getColumnModel().getDataModel().getTableName() + "_" + selectItemModelEntity.getParentItem().getColumnModel().getColumnName());
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

		ReferenceItemModelEntity oldReferenceItem = null;
		if(!referenceItemModelEntity.isNew()){
			ReferenceItemModelEntity referenceItemModelEntity1 = (ReferenceItemModelEntity)itemManager.get(referenceItemModelEntity.getId());
			oldReferenceItem = referenceItemModelEntity1.getParentItem();
		}

		if(oldReferenceItem != null){
			List<ReferenceItemModelEntity> list = oldReferenceItem.getItems();
			for(int i = 0; i < list.size(); i++){
				ReferenceItemModelEntity referenceItemModelEntity1 = list.get(i);
				if(referenceItemModelEntity1.getId().equals(itemModel.getId())){
					list.remove(referenceItemModelEntity1);
					i--;
					itemManager.save(oldReferenceItem);
				}
			}
		}
	}

	private ItemModelEntity  getNewItemModel(Map<String, ItemModelEntity> oldMapItmes, Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity paramerItemModelEntity){
		ItemModelEntity saveItemModelEntity = getItemModelEntity(paramerItemModelEntity.getType());
		if(paramerItemModelEntity.getSystemItemType() == SystemItemType.SerialNumber){
			saveItemModelEntity = new SerialNumberItemModelEntity();
		}else if(paramerItemModelEntity.getSystemItemType() == SystemItemType.Creator){
			saveItemModelEntity = new CreatorItemModelEntity();
		}
		if(!paramerItemModelEntity.isNew()){
			saveItemModelEntity = oldMapItmes.get(paramerItemModelEntity.getId());
		}

		BeanUtils.copyProperties(paramerItemModelEntity, saveItemModelEntity, new String[]{"referencesItemModels","parentItem", "searchItems","sortItems","permissions", "referenceList","items","formModel","columnModel","activities","options"});

		//newItemModelEntity.setFormModel(oldItemModelEntity.getFormModel());

		//设置列表模型
		if (paramerItemModelEntity instanceof ReferenceItemModelEntity ) {
			if(((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceList() != null) {
				ListModelEntity listModelEntity = listModelManager.find(((ReferenceItemModelEntity) paramerItemModelEntity).getReferenceList().getId());
				((ReferenceItemModelEntity) saveItemModelEntity).setReferenceList(listModelEntity);
			}
			((ReferenceItemModelEntity)saveItemModelEntity).setParentItem(((ReferenceItemModelEntity) paramerItemModelEntity).getParentItem());
			((ReferenceItemModelEntity)saveItemModelEntity).setItemTableColunmName(((ReferenceItemModelEntity) paramerItemModelEntity).getItemTableColunmName());
		}

		//设置下拉联动
		if (paramerItemModelEntity instanceof SelectItemModelEntity) {
			((SelectItemModelEntity)saveItemModelEntity).setReferenceDictionaryId(((SelectItemModelEntity) paramerItemModelEntity).getReferenceDictionaryId());
			((SelectItemModelEntity)saveItemModelEntity).setReferenceDictionaryItemId(((SelectItemModelEntity) paramerItemModelEntity).getReferenceDictionaryItemId());
			((SelectItemModelEntity)saveItemModelEntity).setParentItem(((SelectItemModelEntity) paramerItemModelEntity).getParentItem());
		}

		setAcitityOption(saveItemModelEntity, paramerItemModelEntity);


		if(paramerItemModelEntity.getColumnModel() != null && paramerItemModelEntity.getColumnModel().getDataModel() != null){
			ColumnModelEntity columnModelEntity = modelEntityMap.get(paramerItemModelEntity.getColumnModel().getDataModel().getTableName()+"_"+paramerItemModelEntity.getColumnModel().getColumnName());
			saveItemModelEntity.setColumnModel(columnModelEntity);
		}else if(!"id".equals(saveItemModelEntity.getName())){
			saveItemModelEntity.setColumnModel(null);
		}

		saveItempermissions(saveItemModelEntity, paramerItemModelEntity);


		return saveItemModelEntity;
	}

	//设备表单权限
	private void saveItempermissions(ItemModelEntity saveItemModelEntity, ItemModelEntity paramerItemModelEntity){
		if(saveItemModelEntity.getColumnModel() != null && !"id".equals(saveItemModelEntity.getColumnModel().getColumnName())
				&& !"master_id".equals(saveItemModelEntity.getColumnModel().getColumnName())) {
			List<ItemPermissionInfo> list = new ArrayList<>();
			if(saveItemModelEntity.isNew()) {
				list.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Add));
				list.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Update));
				list.add(createItempermissionInfo(saveItemModelEntity, DisplayTimingType.Check));
			}else{
				Map<String, ItemPermissionInfo> oldItemPermission = new HashMap<>();
				for(ItemPermissionInfo itemPermissionInfo : saveItemModelEntity.getPermissions()){
					oldItemPermission.put(itemPermissionInfo.getId(), itemPermissionInfo);
				}
				for(ItemPermissionInfo itemPermissionInfo : paramerItemModelEntity.getPermissions()){
					ItemPermissionInfo newItemPermiss = itemPermissionInfo.isNew() ? new ItemPermissionInfo() : oldItemPermission.remove(itemPermissionInfo.getId());
					BeanUtils.copyProperties(itemPermissionInfo, newItemPermiss, new String[]{"itemModel"});
					newItemPermiss.setItemModel(saveItemModelEntity);
					list.add(newItemPermiss);
				}
				for(String key: oldItemPermission.keySet()){
					itemPermissionManager.deleteById(key);
				}
			}
			saveItemModelEntity.setPermissions(list);
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
	private void deleteItems(List<ItemModelEntity> deleteItems){
		if (deleteItems == null || deleteItems.size() < 1) {
			return;
		}
		List<ItemModelEntity> itemModelEntityList = deleteItems;
		for(int i = 0 ; i < itemModelEntityList.size() ; i++){
			ItemModelEntity itemModelEntity = itemModelEntityList.get(i);
			if(itemModelEntity.getColumnModel() != null) {
				itemModelEntity.setColumnModel(null);
			}
			itemModelEntity.setFormModel(null);
			if(itemModelEntity instanceof RowItemModelEntity){
				List<ItemModelEntity> list = ((RowItemModelEntity) itemModelEntity).getItems();
				for(int j = 0 ; j < list.size(); j++ ) {
					deleteItem(list, list.get(j));
					j--;
				}
			}else if(itemModelEntity instanceof SubFormItemModelEntity){
				List<SubFormRowItemModelEntity> subFormRowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
				for(int j = 0 ; j < subFormRowItems.size(); j++ ) {
					SubFormRowItemModelEntity itemModel = subFormRowItems.get(j);
					List<ItemModelEntity> list = itemModel.getItems();
					for(int n = 0; n < list.size(); n++ ) {
						deleteItem(list, list.get(n));
						n--;
					}
					subFormRowItems.remove(itemModel);
					itemManager.delete(itemModel);
					j--;
				}
			}else	if(itemModelEntity instanceof SubFormRowItemModelEntity){
				List<ItemModelEntity> list = ((SubFormRowItemModelEntity) itemModelEntity).getItems();
				for( int n = 0 ; n < list.size() ; n++ ) {
					deleteItem(list, list.get(n));
					n--;
				}
			}
			deleteItem(itemModelEntityList, itemModelEntity);
			i--;
		}
	}

	private void deleteItem(List<ItemModelEntity> list,  ItemModelEntity itemModelEntity){
		deleteItemOtherReferenceEntity(itemModelEntity);
		if(itemModelEntity instanceof SelectItemModelEntity){
			((SelectItemModelEntity) itemModelEntity).setParentItem(null);
			((SelectItemModelEntity) itemModelEntity).setItems(null);
			itemManager.save(itemModelEntity);
		}
		list.remove(itemModelEntity);
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
	private ItemModelEntity setAcitityOption(ItemModelEntity newEntity, ItemModelEntity oldEntity){
		List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();

		for (ItemActivityInfo activity : oldEntity.getActivities()) {
            ItemActivityInfo newItemActivity = new ItemActivityInfo();
            BeanUtils.copyProperties(activity, newItemActivity, new String[]{"itemModel"});
			newItemActivity.setId(null);
            newItemActivity.setItemModel(newEntity);
			activities.add(newItemActivity);
		}

		List<ItemSelectOption> options = new ArrayList<ItemSelectOption>();
		for (ItemSelectOption option : oldEntity.getOptions()) {
            ItemSelectOption newOption = new ItemSelectOption();
            BeanUtils.copyProperties(option, newOption, new String[]{"itemModel"});
			newOption.setId(null);
            newOption.setItemModel(newEntity);
			options.add(newOption);
		}
		newEntity.setOptions(options);
		newEntity.setActivities(activities);

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
			RowItemModelEntity rowItemModelEntity = (RowItemModelEntity)paramerItemModelEntity;
			List<ItemModelEntity> rowItems = new ArrayList<ItemModelEntity>();
			for(int i = 0; i < rowItemModelEntity.getItems().size() ; i++) {
				ItemModelEntity rowItem = rowItemModelEntity.getItems().get(i);
				ItemModelEntity newRowItem = getNewItemModel(oldMapItmes, modelEntityMap, rowItem);
				newRowItem.setFormModel(null);
				if(newRowItem instanceof ReferenceItemModelEntity) {
					verifyReference((ReferenceItemModelEntity)rowItem);
				}
				newRowItem.setOrderNo(i);
				rowItems.add(newRowItem);
			}
			rowItemModelEntity.setItems(rowItems);
			((RowItemModelEntity)newModelEntity).setItems(rowItems);
		}else if(paramerItemModelEntity instanceof SubFormItemModelEntity){
			List<SubFormRowItemModelEntity> subFormItems = new ArrayList<>();
			SubFormItemModelEntity subFormItemModel  = (SubFormItemModelEntity)paramerItemModelEntity;
			for (int i = 0; i < subFormItemModel.getItems().size() ; i++) {
				SubFormRowItemModelEntity subFormRowItemModelEntity = subFormItemModel.getItems().get(i);
				SubFormRowItemModelEntity subFormRowItemModel  = (SubFormRowItemModelEntity)getNewSubFormRowItemModel(oldMapItmes, subFormRowItemModelEntity);
				subFormRowItemModel.setFormModel(null);
				List<ItemModelEntity> rowItems = new ArrayList<>();
				for (int j = 0; j < subFormRowItemModelEntity.getItems().size() ; j ++) {
					ItemModelEntity childRenItem = subFormRowItemModelEntity.getItems().get(j);
					ItemModelEntity newRowItem = getNewItemModel(oldMapItmes, modelEntityMap, childRenItem);
					newRowItem.setFormModel(null);
					if(newRowItem instanceof ReferenceItemModelEntity) {
						verifyReference((ReferenceItemModelEntity)newRowItem);
					}
					newRowItem.setOrderNo(j);
					rowItems.add(getNewItemModel(oldMapItmes, modelEntityMap, newRowItem));
				}
				subFormRowItemModel.setParentItem((SubFormItemModelEntity)newModelEntity);
				subFormRowItemModel.setItems(rowItems);
				subFormItems.add(subFormRowItemModel);
			}
			((SubFormItemModelEntity)newModelEntity).setItems(subFormItems);
		}else if(paramerItemModelEntity instanceof TabsItemModelEntity){
			TabsItemModelEntity tabPaneItemModelEntity = (TabsItemModelEntity)paramerItemModelEntity;
			List<TabPaneItemModelEntity> tabPaneItemModelEntities = new ArrayList<>();
			for (int j = 0; j < tabPaneItemModelEntity.getItems().size() ; j ++) {
				TabPaneItemModelEntity childRenItem = tabPaneItemModelEntity.getItems().get(j);
				TabPaneItemModelEntity newRowItem = getNewTabPaneItemModel(oldMapItmes, childRenItem);
				List<ItemModelEntity> list = new ArrayList<>();
				for(ItemModelEntity itemModelEntity : childRenItem.getItems()){
					list.add(getNewItemModelEntity(oldMapItmes, modelEntityMap, itemModelEntity));
				}
				newRowItem.setParentItem((TabsItemModelEntity) newModelEntity);
				newRowItem.setOrderNo(j);
				newRowItem.setItems(list);
				tabPaneItemModelEntities.add(newRowItem);
			}
			((TabsItemModelEntity)newModelEntity).setItems(tabPaneItemModelEntities);
		}
		return newModelEntity;
	}

	//获取item子item
	private List<ItemModelEntity> getChildRenItemModelEntity(ItemModelEntity itemModelEntity){
		List<ItemModelEntity> list = new ArrayList<>();
		if(itemModelEntity instanceof RowItemModelEntity){
			list.addAll(((RowItemModelEntity) itemModelEntity).getItems());
		}else if(itemModelEntity instanceof SubFormItemModelEntity){
			list.addAll(((SubFormItemModelEntity) itemModelEntity).getItems());
			for (SubFormRowItemModelEntity subFormRowItemModelEntity :  ((SubFormItemModelEntity)itemModelEntity).getItems()) {
				list.addAll(subFormRowItemModelEntity.getItems());
			}
		}else if(itemModelEntity instanceof TabsItemModelEntity){
			list.addAll(((TabsItemModelEntity) itemModelEntity).getItems());
			for (TabPaneItemModelEntity tabPaneItemModelEntity :  ((TabsItemModelEntity)itemModelEntity).getItems()) {
				list.addAll(tabPaneItemModelEntity.getItems());
			}
		}
		return list;
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
            dataModelService.save(dataModelEntity);
            newDataModelIds.add(dataModelEntity.getId());
            newAddDataModel.add(dataModelEntity);
        }
		BeanUtils.copyProperties(formModel, oldEntity, new String[] {"items","indexes","dataModels","permissions","submitChecks","functions"});
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
	//获取关联行的控件
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
			}
		}
		return new ArrayList<>(itemModels);
	}

	@Override
	public ItemModelEntity getItemModelEntity(ItemType itemType){
		ItemModelEntity entity = new ItemModelEntity();
		if(itemType == null){
			return entity;
		}
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
			case  Tabs:
				entity = new TabsItemModelEntity();
				break;
			case  TabPane:
				entity = new TabPaneItemModelEntity();
				break;
			case  Treeselect:
				entity = new TreeSelectItemModelEntity();
				break;
			default:
				entity = new ItemModelEntity();
				break;
		}
		return entity;
	}

	@Override
	public void deleteFormModelEntityById(String id) {
		FormModelEntity formModelEntity = get(id);
		if(formModelEntity == null){
			throw new IFormException("未找到【"+id+"】对应的表单建模");
		}
		List<ItemModelEntity> itemModelEntities = formModelEntity.getItems();
		if(itemModelEntities != null && itemModelEntities.size() > 0){
			for(int i = 0 ; i < itemModelEntities.size(); i++) {
				ItemModelEntity itemModelEntity = itemModelEntities.get(i);
				deleteItemOtherReferenceEntity(itemModelEntity);
				itemManager.save(itemModelEntity);
			}
		}
		List<ListModelEntity> listModelEntities = listModelManager.query().filterEqual("masterForm.id", id).list();
		for(int i = 0 ; i < listModelEntities.size() ; i ++){
			ListModelEntity listModelEntity = listModelEntities.get(i);
			listModelEntity.setMasterForm(null);
			listModelEntity.setSlaverForms(null);
			listModelManager.save(listModelEntity);
		}
		formModelManager.delete(formModelEntity);
	}

	@Override
	public List<ItemModelEntity> findAllItems(FormModelEntity entity) {
		List<ItemModelEntity> itemModels = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : entity.getItems()){
			itemModels.add(itemModelEntity);
			if(itemModelEntity instanceof SubFormItemModelEntity){
				List<SubFormRowItemModelEntity> subRowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
				for(SubFormRowItemModelEntity rowItemModelEntity : subRowItems){
					itemModels.add(rowItemModelEntity);
					for(ItemModelEntity itemModel : rowItemModelEntity.getItems()) {
						itemModels.add(itemModel);
					}
				}
			}else if(itemModelEntity instanceof RowItemModelEntity){
				for(ItemModelEntity itemModel : ((RowItemModelEntity) itemModelEntity).getItems()) {
					itemModels.add(itemModel);
				}
			}
		}
		return itemModels;
	}

	@Override
	public FormModelEntity saveFormModelSubmitCheck(FormModelEntity entity) {
        FormModelEntity formModelEntity = get(entity.getId());
		BeanUtils.copyProperties(entity, formModelEntity, new String[] {"items","dataModels","permissions","submitChecks","functions"});

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

	//设置表单功能
	private void saveFormModelFunctions(FormModelEntity formModelEntity, FormModelEntity paramerEntity) {

		Map<String, ListFunction> oldMap = new HashMap<>();
		for(ListFunction function : formModelEntity.getFunctions()){
			oldMap.put(function.getId(), function);
		}
		List<ListFunction> newFunctions= paramerEntity.getFunctions();
		if(newFunctions != null){
			List<ListFunction> submitFunctions = new ArrayList<>();
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
			formModelEntity.setFunctions(submitFunctions);
		}
		for(String key : oldMap.keySet()){
			formFunctionsService.deleteById(key);
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
		for (int i = 0 ; i < itemModelEntity.getOptions().size() ; i++) {
			ItemSelectOption itemSelectOption = itemModelEntity.getOptions().get(i);
			itemModelEntity.getOptions().remove(itemSelectOption);
			itemSelectOptionManager.delete(itemSelectOption);
			i--;
		}
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
					if(itemModelEntity instanceof SubFormItemModelEntity && ((SubFormItemModelEntity) itemModelEntity).getTableName().equals(dataModelEntity.getTableName())) {
						for (ColumnModelEntity columnModelEntity : dataModelEntity.getColumns()) {
							if (columnModelEntity.getColumnName().equals("id")) {
								itemModelEntity.setColumnModel(columnModelEntity);
								break;
							}
						}
					}
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
			entity.getProcess().setId(process.getId());
			entity.getProcess().setName(process.getName());
			entity.getProcess().setStartActivity(process.getActivities().get(0).getId());
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
