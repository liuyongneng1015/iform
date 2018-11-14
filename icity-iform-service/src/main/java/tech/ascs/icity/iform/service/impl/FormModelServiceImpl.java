package tech.ascs.icity.iform.service.impl;

import java.util.*;

import com.googlecode.genericdao.search.Filter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iflow.client.ProcessService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.ColumnType;
import tech.ascs.icity.iform.api.model.ItemModel;
import tech.ascs.icity.iform.api.model.ReferenceModel;
import tech.ascs.icity.iform.api.model.SelectItemModel;
import tech.ascs.icity.iform.model.*;
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

	private JPAManager<ColumnReferenceEntity> columnReferenceManager;

	@Autowired
	ProcessService processService;


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
		columnReferenceManager = getJPAManagerFactory().getJPAManager(ColumnReferenceEntity.class);
	}

	@Override
	public FormModelEntity save(FormModelEntity entity) {
		validate(entity);
		boolean dataModelUpdateNeeded = dataModelUpdateNeeded(entity);
		if (!entity.isNew()) { // 先删除所有字段然后重建
			FormModelEntity old = get(entity.getId());
			//主表数据模型
			DataModelEntity dataModelEntity = old.getDataModels().get(0);

			Map<String, ItemModelEntity> oldItems = new HashMap<String, ItemModelEntity>();
			List<String> itemActivityIds = new ArrayList<String>();
			List<String> itemSelectOptionIds = new ArrayList<String>();

			setOldItems(oldItems,  itemActivityIds,  itemSelectOptionIds ,  old );

			BeanUtils.copyProperties(entity, old, new String[] {"dataModels", "items"});
			//包括所有的item(包括子item)
			List<ItemModelEntity> allItems = new ArrayList<>();
			//form直接的item
			List<ItemModelEntity> itemModelEntities = new ArrayList<ItemModelEntity>();
			for(ItemModelEntity itemModelEntity : entity.getItems()) {
				itemModelEntity.setFormModel(old);
				itemModelEntities.add(itemModelEntity);
				//包括所有的item(包括子item)
				allItems.addAll(getChildrenItem(itemModelEntity));
			}

			for(ItemModelEntity item : allItems) {
				initItemData(dataModelEntity, item);
				if (!item.isNew()) {
					ItemModelEntity oldItem = oldItems.remove(item.getId());
					item = getNewItem(item, oldItem);
				}
				if (item.getColumnModel() != null) {
					ColumnModelEntity columnModelEntity = item.getColumnModel();
					columnModelEntity.setItemModel(item);
					item.setColumnModel(columnModelEntity);
				}
			}

			//设置item
			old.setItems(itemModelEntities);

			deleteItems(oldItems.keySet());

			//设置关联关系
			setReferenceItems(oldItems.keySet(), allItems);

			return doUpdate(old, dataModelUpdateNeeded, itemActivityIds, itemSelectOptionIds);
		} else {
			return doSave(entity, dataModelUpdateNeeded);
		}
	}

	//删除item
	private void deleteItems(Collection<String> deletedItemIds){
		if (deletedItemIds.size() > 0) {
			List<ItemModelEntity> list = itemManager.query().filterIn("id", deletedItemIds).list();
			for(ItemModelEntity itemModelEntity : list) {
				if(itemModelEntity.getColumnModel() != null && !itemModelEntity.getColumnModel().getColumnReferences().isEmpty()) {
					deleteOldColumnReferenceEntity(itemModelEntity.getColumnModel());
				}
				if(itemModelEntity.getColumnModel() != null) {
					ColumnModelEntity columnModelEntity = itemModelEntity.getColumnModel();
					//columnModelEntity.setItemModel(null);
					//itemModelEntity.setColumnModel(null);
					columnModelManager.delete(columnModelEntity);
					//itemManager.save(itemModelEntity);
				}
				System.out.println(itemModelEntity.getId());
				itemManager.deleteById(itemModelEntity.getId());
			}
		}
	}

	//得到最新的item
	private ItemModelEntity getNewItem(ItemModelEntity item, ItemModelEntity oldItem){
		BeanUtils.copyProperties(item, oldItem, new String[]{"columnModel", "formModel", "activities", "options","items"});

		List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();
		for (ItemActivityInfo activity : item.getActivities()) {
			activity.setId(null);
			activity.setItemModel(oldItem);
			activities.add(activity);
		}
		oldItem.setActivities(activities);
		List<ItemSelectOption> options = new ArrayList<ItemSelectOption>();
		for (ItemSelectOption option : item.getOptions()) {
			option.setId(null);
			option.setItemModel(oldItem);
			options.add(option);
		}
		oldItem.setOptions(options);

		BeanUtils.copyProperties(oldItem, item, new String[]{"columnModel", "formModel","items"});
		return item;
	}

	//初始化item的值
	private void initItemData(DataModelEntity dataModelEntity, ItemModelEntity item){
		//设置行模型
		if (!item.isNew() && item.getColumnModel() != null && item.getColumnModel().getId() != null) {
			ColumnModelEntity columnModelEntity = columnModelManager.get(item.getColumnModel().getId());
			BeanUtils.copyProperties(item.getColumnModel(), columnModelEntity, new String[]{"dataModel", "itemModel", "columnReferences"});
			item.setColumnModel(columnModelEntity);
		}
		//设置数据模型
		if (item.getColumnModel() != null && item.getColumnModel().getDataModel() != null && item.getColumnModel().getId() == null
				&&  !StringUtils.equals(dataModelEntity.getId(), item.getColumnModel().getDataModel().getId())) {
			item.getColumnModel().setDataModel(dataModelManager.find(item.getColumnModel().getDataModel().getId()));
		}else if(item.getColumnModel() != null){
			item.getColumnModel().setDataModel(dataModelEntity);
		}
		//设置列表模型
		if (item instanceof ReferenceItemModelEntity) {
			((ReferenceItemModelEntity) item).setReferenceList(listModelManager.find(((ReferenceItemModelEntity) item).getReferenceList().getId()));
		}
	}

	//获取item子item
	private List<ItemModelEntity> getChildrenItem(ItemModelEntity itemModelEntity){
		List<ItemModelEntity> childRenItemModelEntities = new ArrayList<ItemModelEntity>();
		if(itemModelEntity instanceof RowItemModelEntity){
			childRenItemModelEntities.add(itemModelEntity);
			List<ItemModelEntity> rowItems = ((RowItemModelEntity) itemModelEntity).getItems();
			childRenItemModelEntities.addAll(rowItems);
			for(ItemModelEntity rowItem : rowItems) {
				if(rowItem instanceof ReferenceItemModelEntity) {
					verifyReference((ReferenceItemModelEntity)rowItem);
				}
			}
		}else if(itemModelEntity instanceof SubFormItemModelEntity){
			childRenItemModelEntities.add(itemModelEntity);
			List<SubFormRowItemModelEntity> rowItems = ((SubFormItemModelEntity) itemModelEntity).getItems();
			if(rowItems != null) {
				childRenItemModelEntities.addAll(rowItems);
				for (SubFormRowItemModelEntity subFormRowItemModelEntity : rowItems) {
					subFormRowItemModelEntity.setFormModel(null);
					for (ItemModelEntity childRenItem : subFormRowItemModelEntity.getItems()) {
						childRenItem.setFormModel(null);
						if(childRenItem instanceof ReferenceItemModelEntity) {
							verifyReference((ReferenceItemModelEntity)childRenItem);
						}
					}
					childRenItemModelEntities.addAll(subFormRowItemModelEntity.getItems());
				}
			}
		}else{
			childRenItemModelEntities.add(itemModelEntity);
		}
		return childRenItemModelEntities;
	}

	//校验关联
	private void verifyReference(ReferenceItemModelEntity rowItem){
		//关联表行
		ColumnModelEntity addToEntity =
				columnModelManager.query().filterEqual("columnName",  rowItem.getReferenceValueColumn()).filterEqual("dataModel.tableName", rowItem.getReferenceTable()).unique();
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
	private void setOldItems(Map<String, ItemModelEntity> oldItems, List<String> itemActivityIds, List<String> itemSelectOptionIds , FormModelEntity old ){
		for (ItemModelEntity itemModelEntity : old.getItems()) {
			if(itemModelEntity instanceof RowItemModelEntity){
				setItemActivityOption(oldItems, itemActivityIds, itemSelectOptionIds, itemModelEntity);
				for(ItemModelEntity childrenItem : ((RowItemModelEntity) itemModelEntity).getItems()){
					setItemActivityOption(oldItems, itemActivityIds, itemSelectOptionIds, childrenItem);
				}
			}else if(itemModelEntity instanceof SubFormItemModelEntity){
				setItemActivityOption(oldItems, itemActivityIds, itemSelectOptionIds, itemModelEntity);
				for(SubFormRowItemModelEntity item : ((SubFormItemModelEntity) itemModelEntity).getItems()) {
					setItemActivityOption(oldItems, itemActivityIds, itemSelectOptionIds, item);
					for(ItemModelEntity childrenItem : item.getItems()) {
						setItemActivityOption(oldItems, itemActivityIds, itemSelectOptionIds, childrenItem);
					}
				}
			}else{
				setItemActivityOption(oldItems, itemActivityIds, itemSelectOptionIds, itemModelEntity);
			}
		}
	}


	//设置旧的item参数
	private void setItemActivityOption(Map<String, ItemModelEntity> oldItems, List<String> itemActivityIds, List<String> itemSelectOptionIds ,ItemModelEntity itemModelEntity){
		oldItems.put(itemModelEntity.getId(), itemModelEntity);
		for (ItemActivityInfo itemActivity : itemModelEntity.getActivities()) {
			itemActivityIds.add(itemActivity.getId());
		}
		for (ItemSelectOption itemSelectOption : itemModelEntity.getOptions()) {
			itemSelectOptionIds.add(itemSelectOption.getId());
		}
	}

	//处理item关联关系
	private void setReferenceItems(Collection<String> deletedItemIds, List<ItemModelEntity> allItems) {
		for(ItemModelEntity entity : allItems) {
			if(deletedItemIds.contains(entity)){
				return;
			}
			if (entity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) entity).getSelectMode() != null) {
				//主表行
				ColumnModelEntity columnEntity = entity.getColumnModel();
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

				//正向关联
				ColumnReferenceEntity addFromReferenceEntity = new ColumnReferenceEntity();
				addFromReferenceEntity.setFromColumn(columnEntity);
				addFromReferenceEntity.setToColumn(addToEntity);
				addFromReferenceEntity.setReferenceType(((ReferenceItemModelEntity) entity).getReferenceType());
				columnEntity.getColumnReferences().add(addFromReferenceEntity);

				//反向关联
				ColumnReferenceEntity addToReferenceEntity = new ColumnReferenceEntity();
				addToReferenceEntity.setFromColumn(addToEntity);
				addToReferenceEntity.setToColumn(columnEntity);
				addToReferenceEntity.setReferenceType(ReferenceModel.getToReferenceType(addFromReferenceEntity.getReferenceType()));
				addToEntity.getColumnReferences().add(addToReferenceEntity);


				//主表数据模型
				DataModelEntity dataModelEntity = dataModelManager.find(columnEntity.getDataModel().getId());

				//从表数据模型
				DataModelEntity addToDataModelEntity = addToEntity.getDataModel();

				Set<DataModelEntity> childrenModelEntities = dataModelEntity.getChildrenModels();
				if(childrenModelEntities.contains(addToDataModelEntity)){
					//已经存在了关系
					return;
				}
				childrenModelEntities.add(addToDataModelEntity);

				//主表上级模型
				Set<DataModelEntity> parentDataModelEntity = dataModelEntity.getParentsModel();
				parentDataModelEntity.add(addToDataModelEntity);

				//设置关联关系
				columnEntity.setDataModel(dataModelEntity);
				//addToEntity.setDataModel(addToDataModelEntity);
				//dataModelManager.save(dataModelEntity);
				//dataModelManager.save(addToDataModelEntity);
			}
		}
	}

	@Transactional(readOnly = false)
	protected FormModelEntity doUpdate(FormModelEntity entity, boolean dataModelUpdateNeeded, Collection<String> deleteItemActivityIds, Collection<String> deleteItemSelectOptionIds) {
		return doSave(entity, dataModelUpdateNeeded);
	}

	//删除旧的关联关系
	private void deleteOldColumnReferenceEntity(ColumnModelEntity columnEntity){
		//删除正向关联的关系
		List<ColumnReferenceEntity> oldReferenceEntityList = columnEntity.getColumnReferences();
		//删除旧的关联
		List<String> deleteOldToColumnIds = new ArrayList<>();
		List<ColumnModelEntity> deleteToColumnModelEntityList = new ArrayList<ColumnModelEntity>();
		for (ColumnReferenceEntity columnReferenceEntity : oldReferenceEntityList) {
			deleteToColumnModelEntityList.add(columnReferenceEntity.getToColumn());
			deleteOldToColumnIds.add(columnReferenceEntity.getToColumn().getId());
		}
		Set<DataModelEntity> dataModelEntities = columnEntity.getDataModel().getChildrenModels();
		Iterator<DataModelEntity> it = dataModelEntities.iterator();
		//Iterator<ColumnReferenceEntity> oldReference = oldReferenceEntityList.iterator();
		for(int i = 0 ; i < oldReferenceEntityList.size(); i++ ) {
			ColumnReferenceEntity referenceEntity = oldReferenceEntityList.get(i) ;
			if (deleteOldToColumnIds.contains(referenceEntity.getToColumn().getId())) {
				//删除数据模型的关系
				while (it.hasNext()) {
					DataModelEntity dataModelEntity = it.next();
					if (dataModelEntity.getTableName().equals(referenceEntity.getToColumn().getDataModel().getTableName())) {
						it.remove();
						referenceEntity.setToColumn(null);
						referenceEntity.setFromColumn(null);
					}
				}
				oldReferenceEntityList.remove(referenceEntity);
				i--;
				columnReferenceManager.delete(referenceEntity);
			}
		}
		//删除反向关联的关系
		for(ColumnModelEntity toEntity : deleteToColumnModelEntityList){
			List<ColumnReferenceEntity> toReferenceEntities = toEntity.getColumnReferences();
			//Iterator<ColumnReferenceEntity> toReference = toReferenceEntities.iterator();
			for(int i = 0; i < toReferenceEntities.size(); i++){
				ColumnReferenceEntity reference = toReferenceEntities.get(i);
				if(StringUtils.equals(reference.getToColumn().getId(), columnEntity.getId())){
					Set<DataModelEntity> referenceDataModelEntities = reference.getFromColumn().getDataModel().getChildrenModels();
					//删除关联关系
					Iterator<DataModelEntity> iterator = referenceDataModelEntities.iterator();
					while(iterator.hasNext()) {
						DataModelEntity dataModelEntity = iterator.next() ;
						if (dataModelEntity.getTableName().equals(columnEntity.getDataModel().getTableName())) {
							iterator.remove();
							reference.setToColumn(null);
							reference.setFromColumn(null);
						}
					}
					toReferenceEntities.remove(reference);
					i--;
					columnReferenceManager.delete(reference);
				}
			}
		}
	}

	@Transactional(readOnly = false)
	protected FormModelEntity doSave(FormModelEntity entity, boolean dataModelUpdateNeeded) {
		if (dataModelUpdateNeeded) {
			updateDataModel(entity.getDataModels().get(0));
		}
		return super.save(entity);
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
