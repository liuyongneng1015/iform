package tech.ascs.icity.iform.service.impl;

import java.util.*;
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

import javax.persistence.Column;

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
		DictionaryItemEntity   root = findRootDictionaryItem();
		if (parentItemEntity != null && !root.getId().equals(parentItemId)) {
			dictionary = null;
		}
		DictionaryItemEntity item = getDictionaryItemById(itemId);
		item.setCode(code);
		item.setName(name);
		item.setDescription(description);
		for (int i = 0; i < root.getChildrenItem().size(); i++) {
			DictionaryItemEntity itemEntity = root.getChildrenItem().get(i);
			if(itemEntity.getId().equals(itemId)){
				root.getChildrenItem().remove(itemEntity);
				i--;
			}
		}

		if (dictionary != null) {
			for (int i = 0; i < dictionary.getDictionaryItems().size(); i++) {
				DictionaryItemEntity itemEntity = dictionary.getDictionaryItems().get(i);
				if(itemEntity.getId().equals(itemId)){
					dictionary.getDictionaryItems().remove(itemEntity);
					i--;
				}
			}
			root.getChildrenItem().add(item);
			item.setParentItem(root);
			dictionary.getDictionaryItems().add(item);
		}else{
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
	public List<DictionaryItemEntity> findAllDictionaryItems(String dictionaryId) {
		if(StringUtils.isNoneBlank(dictionaryId)){
			return dictionaryItemManager.query().filterEqual("dictionary.id", dictionaryId).sort(Sort.asc("orderNo")).list();
		}
		return dictionaryItemManager.query().sort(Sort.asc("orderNo")).list();
	}

	@Override
	public DictionaryItemEntity findRootDictionaryItem() {
		return getRootItem();
	}

	private synchronized DictionaryItemEntity getRootItem(){
		List<DictionaryItemEntity> dictionaryItems = dictionaryItemManager.findAll();
		DictionaryItemEntity rootDictionaryItemEntity = null;
		if(dictionaryItems != null) {
			for (DictionaryItemEntity dictionaryItemEntity : dictionaryItems){
				if(dictionaryItemEntity.getParentItem() == null && dictionaryItemEntity.getDictionary() == null && "root".equals(dictionaryItemEntity.getCode())){
					rootDictionaryItemEntity = dictionaryItemEntity;
					break;
				}
			}
		}
		if(rootDictionaryItemEntity == null) {
			rootDictionaryItemEntity = new DictionaryItemEntity();
			rootDictionaryItemEntity.setCode("root");
			rootDictionaryItemEntity.setName("根节点");
			rootDictionaryItemEntity.setDescription("根节点");
			rootDictionaryItemEntity.setOrderNo(-1000);
			dictionaryItemManager.save(rootDictionaryItemEntity);
		}
		return rootDictionaryItemEntity;
	}

	private void setItems(Set<DictionaryItemEntity> items, DictionaryItemEntity item){
		if(item.getDictionary() != null){
			//根节点
			if(item.getParentItem() != null){
				items.add(item.getParentItem());
			}
		}else {
			//子节点
			if(item.getChildrenItem() != null || item.getChildrenItem().size() > 0) {
				for(DictionaryItemEntity dictionaryItemEntity : item.getChildrenItem()) {
					setItems(items, dictionaryItemEntity);
				}
			}else{
				items.add(item.getParentItem());
			}
		}
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
			dictionaryItemModel.setResources(list.size() < 1 ? null : list);
		}
		return dictionaryItemModel;
	}
}
