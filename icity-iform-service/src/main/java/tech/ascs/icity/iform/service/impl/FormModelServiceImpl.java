package tech.ascs.icity.iform.service.impl;

import java.util.*;

import com.googlecode.genericdao.search.Filter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iflow.client.ProcessService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.ColumnType;
import tech.ascs.icity.iform.api.model.ReferenceModel;
import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.api.model.SelectMode;
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
			//主表新数据模型
			DataModelEntity newDataModelEntity = entity.getDataModels().get(0);
			DataModelEntity oldDataModelEntity = dataModelManager.get(newDataModelEntity.getId());

			//所以的自身与下级的行
			Map<String, ColumnModelEntity> modelEntityMap = new HashMap<String, ColumnModelEntity>();
			Set<ColumnModelEntity> columnModelEntities = new HashSet<>();
			columnModelEntities.addAll(oldDataModelEntity.getColumns());
			if(oldDataModelEntity.getSlaverModels() != null){
				for(DataModelEntity dataModelEntity : oldDataModelEntity.getSlaverModels()){
					columnModelEntities.addAll(dataModelEntity.getColumns());
				}
			}
			ColumnModelEntity idColumnModelEntity = null;
			for(ColumnModelEntity columnModelEntity : columnModelEntities){
				if(columnModelEntity.getColumnName().equals("id") && columnModelEntity.getDataModel().getId().equals(newDataModelEntity.getId())){
					idColumnModelEntity = columnModelEntity;
				}
				modelEntityMap.put(columnModelEntity.getDataModel().getTableName() + "_" + columnModelEntity.getColumnName(), columnModelEntity);
			}

			//再次查询旧的表单模型
			FormModelEntity old = get(entity.getId());


			Map<String, ItemModelEntity> oldItems = new HashMap<String, ItemModelEntity>();
			List<String> itemActivityIds = new ArrayList<String>();
			List<String> itemSelectOptionIds = new ArrayList<String>();

			setOldItems(oldItems,  itemActivityIds,  itemSelectOptionIds ,  old );

			BeanUtils.copyProperties(entity, old, new String[] {"dataModels", "items"});

			//包括所有的新的item(包括子item)
			List<ItemModelEntity> allItems = new ArrayList<>();
			//form直接的新的item
			List<ItemModelEntity> itemModelEntities = new ArrayList<ItemModelEntity>();
			for(ItemModelEntity itemModelEntity : entity.getItems()) {
				itemModelEntity.setFormModel(old);
				itemModelEntities.add(itemModelEntity);
				//包括所有的item(包括子item)
				allItems.addAll(getChildrenItem(itemModelEntity));
			}

			for(ItemModelEntity item : allItems) {
				initItemData(modelEntityMap, item);
				if (!item.isNew()) {
					oldItems.remove(item.getId());
				}
				 setAcitityOption(item);
			}
			//设置item
			old.setItems(itemModelEntities);

			//删除item
			deleteItems(oldItems.keySet(), itemActivityIds, itemSelectOptionIds);

			//设置关联关系
			setReferenceItems(oldItems.keySet(), idColumnModelEntity, allItems);
			return doSave(old, dataModelUpdateNeeded);
		}
		return doSave(entity, dataModelUpdateNeeded);

	}

	//删除item
	private void deleteItems(Collection<String> deletedItemIds, Collection<String> deleteItemActivityIds, Collection<String> deleteItemSelectOptionIds){
		if (deletedItemIds.size() > 0) {
			itemManager.deleteById(deletedItemIds.toArray(new String[]{}));
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
		//设置列表模型
		if (item instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) item).getReferenceList() != null) {
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
