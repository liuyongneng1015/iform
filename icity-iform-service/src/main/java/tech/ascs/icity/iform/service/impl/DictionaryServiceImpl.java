package tech.ascs.icity.iform.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.googlecode.genericdao.search.Sort;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DictionaryItemModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.iform.service.DictionaryService;
import tech.ascs.icity.jpa.dao.exception.NotFoundException;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

public class DictionaryServiceImpl extends DefaultJPAService<DictionaryEntity> implements DictionaryService {

	private JPAManager<DictionaryItemEntity> dictionaryItemManager;

	public DictionaryServiceImpl() {
		super(DictionaryEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		dictionaryItemManager = getJPAManagerFactory().getJPAManager(DictionaryItemEntity.class);
	}

	@Override
	public List<DictionaryItemEntity> findDictionaryItems(String dictionaryId) {
		DictionaryEntity dictionary = get(dictionaryId);
		return dictionary.getDictionaryItems();
	}

	@Override
	public void updateDictionaryItem(String dictionaryId, String itemId, String code, String name, String description, String parentItemId) {
		DictionaryItemEntity parentItemEntity = null;
		if (StringUtils.isNoneBlank(parentItemId)) {
			parentItemEntity = getDictionaryItemById(parentItemId);
		}
		DictionaryEntity dictionary = null;
		if (StringUtils.isNoneBlank(dictionaryId)) {
			dictionary = get(dictionaryId);
		}
		if (parentItemEntity == null && dictionary == null) {
			throw new IFormException("查询关联对象失败");
		}
		if (parentItemEntity != null) {
			dictionary = null;
		}
		DictionaryItemEntity item = getDictionaryItemById(itemId);
		item.setCode(code);
		item.setName(name);
		item.setDescription(description);
		if (parentItemEntity != null) {
			for (int i = 0; i < parentItemEntity.getChildrenItem().size(); i++) {
				DictionaryItemEntity itemEntity = parentItemEntity.getChildrenItem().get(i);
				if (itemEntity.getId().equals(itemId)) {
					parentItemEntity.getChildrenItem().remove(itemEntity);
					i--;
				}
			}
			parentItemEntity.getChildrenItem().add(item);
			item.setParentItem(parentItemEntity);
			item.setDictionary(null);
		}
		if (dictionary != null) {
			for (int i = 0; i < dictionary.getDictionaryItems().size(); i++) {
				DictionaryItemEntity itemEntity = dictionary.getDictionaryItems().get(i);
				if (itemEntity.getId().equals(itemId)) {
					parentItemEntity.getChildrenItem().remove(itemEntity);
					i--;
				}
			}
			item.setParentItem(null);
			dictionary.getDictionaryItems().add(item);
		}
		dictionaryItemManager.save(item);
	}

	@Override
	public void deleteDictionaryItem(String itemId) {
		DictionaryItemEntity item = dictionaryItemManager.get(itemId);
		if (item == null) {
			throw new NotFoundException(DictionaryItemEntity.class, itemId, null);
		}
		dictionaryItemManager.delete(item);
	}

	@Override
	public DictionaryItemEntity getDictionaryItemById(String itemId) {
		if (StringUtils.isBlank(itemId)) {
			return null;
		}
		DictionaryItemEntity item = dictionaryItemManager.get(itemId);
		if (item == null) {
			throw new IFormException("未找到对应的数据字典【" + itemId + "】");
		}
		return item;
	}

	@Override
	public DictionaryItemEntity saveDictionaryItem(DictionaryItemEntity itemEntity) {
		return dictionaryItemManager.save(itemEntity);
	}


	@Override
	public Integer maxDictionaryItemOrderNo() {
		Map<String, Object> map = dictionaryItemManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from ifm_dictionary_item ");
		if (map != null && map.get("order_no") != null) {
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		}
		return 0;
	}

	@Override
	public Integer maxDictionaryOrderNo() {
		Map<String, Object> map = dictionaryItemManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from ifm_dictionary ");
		if (map != null && map.get("order_no") != null) {
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		}
		return 0;
	}

	@Override
	public List<DictionaryItemEntity> findAllDictionaryItems() {
		return dictionaryItemManager.query().sort(Sort.asc("orderNo")).list();
	}

	@Override
	public List<DictionaryModel> findDictionaryModels(String dictionaryId, String dictionaryItemId) {
		List<DictionaryEntity> list = query().sort(Sort.asc("orderNo")).list();
		for (DictionaryEntity dictionaryEntity : list) {
			dictionaryEntity.setDictionaryItems(sortedItem(dictionaryEntity.getDictionaryItems()));
		}
		List<DictionaryModel> dictionaryModels = new ArrayList<>();
		for (DictionaryEntity dictionaryEntity : list) {
			if(dictionaryEntity.getId().equals(dictionaryId)) {
				dictionaryModels.add(getByEntity(dictionaryItemId, dictionaryEntity));
				break;
			}
		}
		for (DictionaryEntity dictionaryEntity : list) {
			if(dictionaryEntity.getId().equals(dictionaryId)) {
				continue;
			}
			dictionaryModels.add(getByEntity(dictionaryEntity));
		}
		return dictionaryModels;
	}

	private List<DictionaryItemEntity> sortedItem(List<DictionaryItemEntity> list) {
		if (list == null || list.size() < 2) {
			return list;
		}
		List<DictionaryItemEntity> dictionaryItemEntities = list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
		for (DictionaryItemEntity dictionaryItemEntity : dictionaryItemEntities) {
			if (dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0) {
				dictionaryItemEntity.setChildrenItem(sortedItem(dictionaryItemEntity.getChildrenItem()));
			}
		}
		return dictionaryItemEntities;
	}


	private DictionaryModel getByEntity(DictionaryEntity dictionaryEntity) {
		DictionaryModel dictionaryModel = new DictionaryModel();
		BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
		if (dictionaryEntity.getDictionaryItems() != null) {
			List<DictionaryItemModel> itemModelList = new ArrayList<>();
			for (DictionaryItemEntity entity : dictionaryEntity.getDictionaryItems()) {
				itemModelList.add(getByEntity(entity));
			}
			dictionaryModel.setResources(itemModelList);
		}
		return dictionaryModel;
	}

	private DictionaryModel getByEntity(String dictionaryItemId, DictionaryEntity dictionaryEntity) {
		DictionaryModel dictionaryModel = new DictionaryModel();
		BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
		if (dictionaryEntity.getDictionaryItems() != null) {
			List<DictionaryItemModel> itemModelList = new ArrayList<>();
			for (DictionaryItemEntity entity : dictionaryEntity.getDictionaryItems()) {
				if(entity.getId().equals(dictionaryItemId)){
					itemModelList.add(getByEntity(entity));
					break;
				}
			}
			for (DictionaryItemEntity entity : dictionaryEntity.getDictionaryItems()) {
				if(entity.getId().equals(dictionaryItemId)){
					continue;
				}
				itemModelList.add(getByEntity(entity));
			}

			dictionaryModel.setResources(itemModelList);
		}
		return dictionaryModel;
	}


	private DictionaryItemModel getByEntity(DictionaryItemEntity dictionaryItemEntity) {
		DictionaryItemModel dictionaryItemModel = new DictionaryItemModel();
		BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});

		if (dictionaryItemEntity.getDictionary() != null) {
			dictionaryItemModel.setDictionaryId(dictionaryItemEntity.getDictionary().getId());
		}

		if (dictionaryItemEntity.getParentItem() != null) {
			dictionaryItemModel.setParentItemId(dictionaryItemEntity.getParentItem().getId());
		}

		if (dictionaryItemEntity.getChildrenItem() != null) {
			List<DictionaryItemModel> list = new ArrayList<>();
			for (DictionaryItemEntity childDictionaryItemEntity : dictionaryItemEntity.getChildrenItem()) {
				list.add(getByEntity(childDictionaryItemEntity));
			}
			dictionaryItemModel.setResources(list);
		}
		return dictionaryItemModel;
	}
}
