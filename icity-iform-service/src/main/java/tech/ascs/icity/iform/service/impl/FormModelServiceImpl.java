package tech.ascs.icity.iform.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iflow.client.ProcessService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.ColumnModelService;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.iform.service.FormModelService;
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


	@Autowired
	ProcessService processService;

	@Autowired
	ColumnModelService columnModelService;

	@Autowired
	DataModelService dataModelService;


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
			Map<String, ColumnModelEntity> modelEntityMap = new HashMap<String, ColumnModelEntity>();
			Set<ColumnModelEntity> columnModelEntities = new HashSet<>();
			columnModelEntities.addAll(oldDataModelEntity.getColumns());
			if(oldDataModelEntity.getSlaverModels() != null){
				for(DataModelEntity dataModelEntity : oldDataModelEntity.getSlaverModels()){
					columnModelEntities.addAll(dataModelEntity.getColumns());
				}
			}

			//主键
			ColumnModelEntity idColumnModelEntity = null;
			for(ColumnModelEntity columnModelEntity : columnModelEntities){
				if(columnModelEntity.getColumnName().equals("id") && columnModelEntity.getDataModel().getId().equals(oldDataModelEntity.getId())){
					idColumnModelEntity = columnModelEntity;
				}
				modelEntityMap.put(columnModelEntity.getDataModel().getTableName() + "_" + columnModelEntity.getColumnName(), columnModelEntity);
			}



			BeanUtils.copyProperties(entity, old, new String[] {"dataModels", "items"});
			List<ItemModelEntity> oldItems = old.getItems();
			List<String> itemActivityIds = new ArrayList<String>();
			List<String> itemSelectOptionIds = new ArrayList<String>();

			//setOldItems(itemActivityIds,  itemSelectOptionIds ,  old );

			//包括所有的新的item(包括子item)
			List<ItemModelEntity> allItems = new ArrayList<>();

			//form直接的新的item
			List<ItemModelEntity> itemModelEntities = new ArrayList<ItemModelEntity>();
			for(ItemModelEntity oldItemModelEntity : entity.getItems()) {
				oldItemModelEntity.setFormModel(old);
				ItemModelEntity newItemModelEntity = getNewItemModelEntity(modelEntityMap, oldItemModelEntity);

				//包括所有的item(包括子item)
				allItems.add(newItemModelEntity);
				allItems.addAll(getChildRenItemModelEntity(newItemModelEntity));
				itemModelEntities.add(newItemModelEntity);
			}


			for(ItemModelEntity item : allItems) {
				if (!item.isNew()) {
					oldItems.remove(item.getId());
				}
				setAcitityOption(item);
			}


			//删除item
			deleteItems(old, oldItems, itemActivityIds, itemSelectOptionIds);

			old.setItems(itemModelEntities);

			//设置关联关系
			//setReferenceItems(deletedItemIds, idColumnModelEntity, allItems);
			return doSave(old, dataModelUpdateNeeded);
		}
		return doSave(entity, dataModelUpdateNeeded);

	}

	private ItemModelEntity  getNewItemModel(Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity oldItemModelEntity){
		ItemModelEntity newItemModelEntity = getItemModelEntity(oldItemModelEntity.getType());
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = itemManager.get(oldItemModelEntity.getId());
		}

		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"referenceList","items","formModel","columnModel","activities","options"});

		newItemModelEntity.setFormModel(oldItemModelEntity.getFormModel());

		//设置列表模型
		if (oldItemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) oldItemModelEntity).getReferenceList() != null) {
			ListModelEntity listModelEntity = listModelManager.find(((ReferenceItemModelEntity) oldItemModelEntity).getReferenceList().getId());
			((ReferenceItemModelEntity)newItemModelEntity).setReferenceList(listModelEntity);
		}


		if(oldItemModelEntity.getColumnModel() != null && oldItemModelEntity.getColumnModel().getDataModel() != null){
			ColumnModelEntity columnModelEntity = modelEntityMap.get(oldItemModelEntity.getColumnModel().getDataModel().getTableName()+"_"+oldItemModelEntity.getColumnModel().getColumnName());
			newItemModelEntity.setColumnModel(columnModelEntity);
		}else if(!newItemModelEntity.getName().equals("id")){
			newItemModelEntity.setColumnModel(null);
		}
		return newItemModelEntity;
	}

	private ItemModelEntity  getNewSubFormRowItemModel(SubFormRowItemModelEntity oldItemModelEntity){
		ItemModelEntity newItemModelEntity = new SubFormRowItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = itemManager.get(oldItemModelEntity.getId());
		}
		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"parentItem","referenceList","items","formModel","columnModel","activities","options"});
		return newItemModelEntity;
	}

	private ItemModelEntity  getNewSubFormItemModel(SubFormItemModelEntity oldItemModelEntity){
		ItemModelEntity newItemModelEntity = new SubFormItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = itemManager.get(oldItemModelEntity.getId());
		}
		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"referenceList","items","formModel","columnModel","activities","options"});
		return newItemModelEntity;
	}

	private ItemModelEntity  getNewRowItemModel(RowItemModelEntity oldItemModelEntity){
		ItemModelEntity newItemModelEntity = new RowItemModelEntity();
		if(!oldItemModelEntity.isNew()){
			newItemModelEntity = itemManager.get(oldItemModelEntity.getId());
		}
		BeanUtils.copyProperties(oldItemModelEntity, newItemModelEntity, new String[]{"referenceList","items","formModel","columnModel","activities","options"});
		return newItemModelEntity;
	}


	//删除item
	private void deleteItems(FormModelEntity old, List<ItemModelEntity> deleteItems, Collection<String> deleteItemActivityIds, Collection<String> deleteItemSelectOptionIds){
		if (deleteItems.size() > 0) {
			for(int i = 0 ; i < deleteItems.size() ; i++){
				ItemModelEntity itemModelEntity = deleteItems.get(i);
				if(itemModelEntity.getColumnModel() != null) {
					itemModelEntity.setColumnModel(null);
				}
				itemModelEntity.setFormModel(null);
				if(itemModelEntity instanceof RowItemModelEntity){
					List<ItemModelEntity> list = ((RowItemModelEntity) itemModelEntity).getItems();
					((RowItemModelEntity) itemModelEntity).setItems(null);
					for(int j = 0 ; j < list.size(); j++ ) {
						ItemModelEntity itemModelEntity1 = list.get(j);
						itemModelEntity1.setColumnModel(null);
						itemManager.delete(itemModelEntity1);
						j--;
					}
				}else if(itemModelEntity instanceof SubFormItemModelEntity){
					List<SubFormRowItemModelEntity> subFormRowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
					((SubFormItemModelEntity) itemModelEntity).setItems(null);
					for(int j = 0 ; j < subFormRowItems.size(); j++ ) {
						SubFormRowItemModelEntity itemModel = subFormRowItems.get(j);
						List<ItemModelEntity> list = itemModel.getItems();
						itemModel.setItems(null);
						for(int n = 0; n < list.size(); n++ ) {
							ItemModelEntity itemModelEntity1 = list.get(n);
							itemModelEntity1.setColumnModel(null);
							itemManager.delete(itemModelEntity1);
							n--;
						}
						itemManager.delete(itemModel);
						j--;
					}
				}else	if(itemModelEntity instanceof SubFormRowItemModelEntity){
					List<ItemModelEntity> list = ((SubFormRowItemModelEntity) itemModelEntity).getItems();
					((SubFormRowItemModelEntity) itemModelEntity).setItems(null);
					for( int n = 0 ; n < list.size() ; n++ ) {
						ItemModelEntity itemModel = list.get(0);
						itemModel.setColumnModel(null);
						itemManager.delete(itemModel);
						n--;
					}
				}
				deleteItems.remove(itemModelEntity);
				itemManager.delete(itemModelEntity);
				i--;
			}
		}
		if (deleteItemActivityIds.size() > 0) {
			itemActivityManager.deleteById(deleteItemActivityIds.toArray(new String[] {}));
		}
		if (deleteItemSelectOptionIds.size() > 0) {
			itemSelectOptionManager.deleteById(deleteItemSelectOptionIds.toArray(new String[] {}));
		}
	}


	//得到最新的item
	private ItemModelEntity setAcitityOption(ItemModelEntity item){
		List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();
		for (ItemActivityInfo activity : item.getActivities()) {
			activity.setId(null);
			activity.setItemModel(item);
			activities.add(activity);
		}
		item.setActivities(activities);
		List<ItemSelectOption> options = new ArrayList<ItemSelectOption>();
		for (ItemSelectOption option : item.getOptions()) {
			option.setId(null);
			option.setItemModel(item);
			options.add(option);
		}
		item.setOptions(options);
		return item;
	}

	//初始化item的值
	private void initItemData(Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity item){
		//设置行模型
		if (item.getColumnModel() != null && item.getColumnModel().getDataModel() != null) {
			item.setColumnModel(modelEntityMap.get(item.getColumnModel().getDataModel().getTableName() + "_" + item.getColumnModel().getColumnName()));
		}
	}

	//获取item子item
	private ItemModelEntity getNewItemModelEntity(Map<String, ColumnModelEntity> modelEntityMap, ItemModelEntity oldItemModelEntity){
		ItemModelEntity newModelEntity = getNewItemModel(modelEntityMap, oldItemModelEntity);
		if(oldItemModelEntity instanceof RowItemModelEntity){
			RowItemModelEntity rowItemModelEntity = (RowItemModelEntity)oldItemModelEntity;
			List<ItemModelEntity> rowItems = new ArrayList<ItemModelEntity>();
			for(ItemModelEntity rowItem :  rowItemModelEntity.getItems()) {
				if(rowItem instanceof ReferenceItemModelEntity) {
					verifyReference((ReferenceItemModelEntity)rowItem);
				}
				rowItems.add(getNewItemModel(modelEntityMap, oldItemModelEntity));
			}
			rowItemModelEntity.setItems(rowItems);
			((RowItemModelEntity)newModelEntity).setItems(rowItems);
		}else if(oldItemModelEntity instanceof SubFormItemModelEntity){
			List<SubFormRowItemModelEntity> subFormItems = new ArrayList<>();
			SubFormItemModelEntity subFormItemModel  = (SubFormItemModelEntity)oldItemModelEntity;
			for (SubFormRowItemModelEntity subFormRowItemModelEntity :  subFormItemModel.getItems()) {
				SubFormRowItemModelEntity subFormRowItemModel  = (SubFormRowItemModelEntity)getNewSubFormRowItemModel(subFormRowItemModelEntity);
				List<ItemModelEntity> rowItems = new ArrayList<>();
				for (ItemModelEntity childRenItem : subFormRowItemModelEntity.getItems()) {
					if(childRenItem instanceof ReferenceItemModelEntity) {
						verifyReference((ReferenceItemModelEntity)childRenItem);
					}
					rowItems.add(getNewItemModel(modelEntityMap, childRenItem));
				}
				subFormRowItemModel.setParentItem((SubFormItemModelEntity)newModelEntity);
				subFormRowItemModel.setItems(rowItems);
				subFormItems.add(subFormRowItemModel);
			}
			subFormItemModel.setItems(subFormItems);
			((SubFormItemModelEntity)newModelEntity).setItems(subFormItems);
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
		}
		return list;
	}

	//校验关联
	private void verifyReference(ReferenceItemModelEntity rowItem){
		FormModelEntity formModelEntity = findUniqueByName(rowItem.getReferenceTable());
		if(formModelEntity == null){
			throw new IFormException(404, "表单【" + rowItem.getReferenceTable() +"】， 未找到");
		}
		List<ItemModelEntity> itemModels = getAllColumnItems(formModelEntity.getItems());

		//关联表行
		ColumnModelEntity addToEntity = null;
		for(ItemModelEntity itemModelEntity : itemModels){
			if(itemModelEntity.getName().equals(rowItem.getReferenceValueColumn())){
				addToEntity = itemModelEntity.getColumnModel();
			}
		}

		if(addToEntity == null){
			throw new IFormException(404, "表单【" + rowItem.getReferenceTable() +"】， 未找到对应的【"+ rowItem.getReferenceValueColumn() +"】控件");
		}

		//关联表行
		/*ColumnModelEntity addToEntity =
				columnModelManager.query().filterEqual("columnName",  rowItem.getReferenceValueColumn()).filterEqual("dataModel.tableName", rowItem.getReferenceTable()).unique();
		*/
		if(addToEntity.getColumnReferences() != null){
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

	//获取旧的item数据
	private void setOldItems(List<String> itemActivityIds, List<String> itemSelectOptionIds , FormModelEntity old ){
		for (ItemModelEntity itemModelEntity : old.getItems()) {
			if(itemModelEntity instanceof RowItemModelEntity){
				setItemActivityOption(itemActivityIds, itemSelectOptionIds, itemModelEntity);
				for(ItemModelEntity childrenItem : ((RowItemModelEntity) itemModelEntity).getItems()){
					setItemActivityOption(itemActivityIds, itemSelectOptionIds, childrenItem);
				}
			}else if(itemModelEntity instanceof SubFormItemModelEntity){
				setItemActivityOption(itemActivityIds, itemSelectOptionIds, itemModelEntity);
				for(SubFormRowItemModelEntity item : ((SubFormItemModelEntity) itemModelEntity).getItems()) {
					setItemActivityOption(itemActivityIds, itemSelectOptionIds, item);
					for(ItemModelEntity childrenItem : item.getItems()) {
						setItemActivityOption(itemActivityIds, itemSelectOptionIds, childrenItem);
					}
				}
			}else{
				setItemActivityOption(itemActivityIds, itemSelectOptionIds, itemModelEntity);
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
		if(!formModel.isNew()){
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
		for(DataModel dataModel : formModel.getDataModels()) {
			DataModelEntity dataModelEntity = new DataModelEntity();
			if(!dataModel.isNew()){
				dataModelEntity = dataModelService.get(dataModel.getId());
			}
			BeanUtils.copyProperties(dataModel, dataModelEntity, new String[] {"masterModel","slaverModels","columns","indexes","referencesDataModel"});
			columnModelService.saveColumnModelEntity(dataModelEntity, "id");
			dataModelService.save(dataModelEntity);
			newDataModelIds.add(dataModelEntity.getId());
			newAddDataModel.add(dataModelEntity);
		}
		BeanUtils.copyProperties(formModel, oldEntity, new String[] {"items","indexes","dataModels"});
		if(formModel.getItems() != null){
			for(ItemModel itemModel : formModel.getItems()){
				ItemModelEntity itemModelEntity = new ItemModelEntity();
				if(!itemModel.isNew()){
					itemModelEntity = itemManager.get(itemModel.getId());
					if(itemModelEntity.getColumnModel() != null &&  !itemModelEntity.getColumnModel().getDataModel().getId().equals(newAddDataModel.get(0).getId())){
						itemModelEntity.setColumnModel(null);
					}
				}
				BeanUtils.copyProperties(itemModel, itemModelEntity, new String[] {"columnModel", "formModel", "activities", "options"});
				itemManager.save(itemModelEntity);
			}
		}

		List<DataModelEntity> oldDataModelEntities = oldEntity.getDataModels();
		for(DataModelEntity dataModelEntity : oldDataModelEntities){
			if(!newDataModelIds.contains(dataModelEntity.getId())){
				oldDataModelEntities.remove(dataModelEntity);
			}
		}
		oldDataModelEntities.addAll(newAddDataModel);
		oldEntity.setDataModels(oldDataModelEntities);
		return this.save(oldEntity);
	}
	//获取关联行的控件
	@Override
	public  List<ItemModelEntity> getAllColumnItems(List<ItemModelEntity> itemModelEntities){
		List<ItemModelEntity> itemModels = new ArrayList<>();
		for(ItemModelEntity itemModelEntity : itemModelEntities){
			if(itemModelEntity.getColumnModel() != null){
				itemModels.add(itemModelEntity);
				continue;
			}
			if(itemModelEntity instanceof SubFormItemModelEntity){
				List<SubFormRowItemModelEntity> subRowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
				for(SubFormRowItemModelEntity rowItemModelEntity : subRowItems){
					for(ItemModelEntity itemModel : rowItemModelEntity.getItems()) {
						if (itemModel.getColumnModel() != null) {
							itemModels.add(itemModel);
							continue;
						}
					}
				}
			}else if(itemModelEntity instanceof RowItemModelEntity){
				for(ItemModelEntity itemModel : ((RowItemModelEntity) itemModelEntity).getItems()) {
					if (itemModel.getColumnModel() != null) {
						itemModels.add(itemModel);
						continue;
					}
				}
			}
		}
		return itemModels;
	}

	@Override
	public ItemModelEntity getItemModelEntity(ItemType itemType){
		ItemModelEntity entity = new ItemModelEntity();
		if(itemType == null){
			return entity;
		}
		switch (itemType){
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
			default:
				entity = new ItemModelEntity();
				break;
		}
		return entity;
	}

	@Override
	public void deleteFormModelEntity(FormModelEntity formModelEntity) {
		List<ItemModelEntity> itemModelEntities = formModelEntity.getItems();
		for(int i = 0 ; i < itemModelEntities.size() ; i++){
			ItemModelEntity itemModelEntity = itemModelEntities.get(i);
			if(itemModelEntity.getColumnModel() != null){
				itemModelEntity.setColumnModel(null);
			}
			if(itemModelEntity instanceof RowItemModelEntity){
				List<ItemModelEntity> itemModelEntities1 = ((RowItemModelEntity) itemModelEntity).getItems();
				((RowItemModelEntity) itemModelEntity).setItems(null);
				if(itemModelEntities1 != null && itemModelEntities1.size() > 0) {
					itemManager.delete(itemModelEntities1.toArray(new ItemModelEntity[]{}));
				}
			}else if(itemModelEntity instanceof SubFormItemModelEntity){
				List<SubFormRowItemModelEntity> itemModelEntities1 = ((SubFormItemModelEntity) itemModelEntity).getItems();
				for(int j = 0 ; j < itemModelEntities1.size(); j++){
					SubFormRowItemModelEntity subFormRowItemModelEntity = itemModelEntities1.get(j);
					List<ItemModelEntity> itemModelEntities11 =  subFormRowItemModelEntity.getItems();
					subFormRowItemModelEntity.setItems(null);
					if(itemModelEntities11 != null && itemModelEntities11.size() > 0) {
						itemManager.delete(itemModelEntities11.toArray(new ItemModelEntity[]{}));
					}
					itemManager.delete(subFormRowItemModelEntity);
					j--;
				}
				((SubFormItemModelEntity) itemModelEntity).setItems(null);
				if(itemModelEntities1 != null && itemModelEntities1.size() > 0) {
					itemManager.delete(itemModelEntities1.toArray(new ItemModelEntity[]{}));
				}
			}
			itemManager.delete(itemModelEntity);
			i--;
		}
		delete(formModelEntity);
	}

	//设置旧的item参数
	private void setItemActivityOption(List<String> itemActivityIds, List<String> itemSelectOptionIds ,ItemModelEntity itemModelEntity){
		for (ItemActivityInfo itemActivity : itemModelEntity.getActivities()) {
			itemActivityIds.add(itemActivity.getId());
		}
		for (ItemSelectOption itemSelectOption : itemModelEntity.getOptions()) {
			itemSelectOptionIds.add(itemSelectOption.getId());
		}
	}

	//处理item关联关系
	private void setReferenceItems(Collection<String> deletedItemIds, ColumnModelEntity idColumnModelEntity, List<ItemModelEntity> allItems) {
		for(ItemModelEntity entity : allItems) {
			if(deletedItemIds.contains(entity)){
				return;
			}
			if (entity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) entity).getSelectMode() == SelectMode.Inverse) {
				//主表行
				ColumnModelEntity columnEntity = idColumnModelEntity;
				//关联表行
				ColumnModelEntity addToEntity =
						columnModelManager.query().filterEqual("columnName", ((ReferenceItemModelEntity) entity).getReferenceValueColumn()).filterEqual("dataModel.tableName",((ReferenceItemModelEntity) entity).getReferenceTable()).unique();

				if(addToEntity != null && addToEntity.getColumnReferences() != null){
					for(ColumnReferenceEntity columnReferenceEntity : addToEntity.getColumnReferences()){
						if(columnReferenceEntity.getFromColumn().getId().equals(columnEntity.getId())){
							//关联关系存在了
							return;
						}
					}
				}
				//保存关系持久化到数据库
				columnModelService.saveColumnReferenceEntity(columnEntity, addToEntity, ((ReferenceItemModelEntity) entity).getReferenceType());
				columnModelManager.save(columnEntity);
				columnModelManager.save(addToEntity);
			}
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
		return super.save(entity);
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
				idItemModelEntity.setProps("{id:组件id}");
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
