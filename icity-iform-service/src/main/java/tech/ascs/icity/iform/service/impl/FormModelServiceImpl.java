package tech.ascs.icity.iform.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.ascs.icity.iflow.client.ProcessService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.model.ColumnData;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemActivityInfo;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ItemSelectOption;
import tech.ascs.icity.iform.model.TabInfo;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.table.service.TableUtilService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.utils.BeanUtils;

public class FormModelServiceImpl extends DefaultJPAService<FormModelEntity> implements FormModelService {

	private JPAManager<ItemModelEntity> itemManager;

	private JPAManager<ItemActivityInfo> itemActivityManager;

	private JPAManager<ItemSelectOption> itemSelectOptionManager;

	private JPAManager<TabInfo> dataModelManager;

	private JPAManager<ColumnData> columnModelManager;

	@Autowired
	TableUtilService tableUtilService;

	@Autowired
	ProcessService processService;

	public FormModelServiceImpl() {
		super(FormModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		itemManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
		itemActivityManager = getJPAManagerFactory().getJPAManager(ItemActivityInfo.class);
		itemSelectOptionManager = getJPAManagerFactory().getJPAManager(ItemSelectOption.class);
		dataModelManager = getJPAManagerFactory().getJPAManager(TabInfo.class);
		columnModelManager = getJPAManagerFactory().getJPAManager(ColumnData.class);
	}

	@Override
	public FormModelEntity save(FormModelEntity entity) {
		validate(entity);
		boolean dataModelUpdateNeeded = dataModelUpdateNeeded(entity);
		if (!entity.isNew()) { // 先删除所有字段然后重建
			FormModelEntity old = get(entity.getId());
			Map<String, ItemModelEntity> oldItems = new HashMap<String, ItemModelEntity>();
//			List<String> itemIds = new ArrayList<String>();
			List<String> itemActivityIds = new ArrayList<String>();
			List<String> itemSelectOptionIds = new ArrayList<String>();
			for (ItemModelEntity item : old.getItems()) {
				oldItems.put(item.getId(), item);
				for (ItemActivityInfo itemActivity : item.getActivities()) {
					itemActivityIds.add(itemActivity.getId());
				}
				for (ItemSelectOption itemSelectOption : item.getOptions()) {
					itemSelectOptionIds.add(itemSelectOption.getId());
				}
			}

			BeanUtils.copyProperties(entity, old, new String[] {"dataModels", "items"});
			old.setDataModels(entity.getDataModels());
			List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();
			for (ItemModelEntity item : entity.getItems()) {
				if (!item.isNew()) {
					ItemModelEntity oldItem = oldItems.remove(item.getId());
					BeanUtils.copyProperties(item, oldItem, new String[] {"formModel", "activities", "options"});
					
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
					
					items.add(oldItem);
				} else {
					items.add(item);
				}
			}
			old.setItems(items);

			return doUpdate(old, dataModelUpdateNeeded, oldItems.keySet(), itemActivityIds, itemSelectOptionIds);
		} else {
			return doSave(entity, dataModelUpdateNeeded);
		}
	}

	@Transactional(readOnly = false)
	protected FormModelEntity doUpdate(FormModelEntity entity, boolean dataModelUpdateNeeded, Collection<String> deletedItemIds, Collection<String> deleteItemActivityIds, Collection<String> deleteItemSelectOptionIds) {
		if (deletedItemIds.size() > 0) {
			itemManager.deleteById(deletedItemIds.toArray(new String[] {}));
		}
		if (deleteItemActivityIds.size() > 0) {
			itemActivityManager.deleteById(deleteItemActivityIds.toArray(new String[] {}));
		}
		if (deleteItemSelectOptionIds.size() > 0) {
			itemSelectOptionManager.deleteById(deleteItemSelectOptionIds.toArray(new String[] {}));
		}
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
		for (TabInfo dataModel : entity.getDataModels()) {
			if (dataModelManager.find(dataModel.getId()) == null) {
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
			return columnModelManager.query().filterEqual("tabName", entity.getDataModels().get(0).getTabName()).filterEqual("colName", "PROCESS_ID").count() == 0;
		}
		return false;
	}

	protected void updateDataModel(TabInfo dataModel) {
		dataModel = dataModelManager.get(dataModel.getId());
		createColumnModel(dataModel, "PROCESS_ID", "流程ID", 64);
		createColumnModel(dataModel, "PROCESS_INSTANCE", "流程实例ID", 64);
		createColumnModel(dataModel, "ACTIVITY_ID", "环节ID", 255);
		createColumnModel(dataModel, "ACTIVITY_INSTANCE", "环节实例ID", 255);
		dataModel = dataModelManager.save(dataModel);
		try {
			tableUtilService.createTable(dataModel);
		} catch (Exception e) {
			throw new IFormException("更新数据模型【" + dataModel.getName() + "】失败", e);
		}
	}

	protected void createColumnModel(TabInfo dataModel, String columnName, String columnDesc, int length) {
		ColumnData columnModel = new ColumnData();
		columnModel.setTabInfoId(dataModel.getId());
		columnModel.setColName(columnName);
		columnModel.setColNameDesc(columnDesc);
		columnModel.setType("String");
		columnModel.setLength(length);
		columnModel.setTabName(dataModel.getTabName());
		dataModel.getColumnDatas().add(columnModel);
		//columnModelManager.save(columnModel);
	}
}
