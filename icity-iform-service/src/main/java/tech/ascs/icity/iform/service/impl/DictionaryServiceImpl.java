package tech.ascs.icity.iform.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.iform.service.DictionaryService;
import tech.ascs.icity.jpa.dao.exception.NotFoundException;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
		DictionaryItemEntity item = getDictionaryItemById(itemId);
		DictionaryItemEntity parentItem = getDictionaryItemById(parentItemId);
		DictionaryEntity dictionaryEntity = null;
		if(StringUtils.isNoneBlank(dictionaryId)){
			dictionaryEntity = get(dictionaryId);
		}
		item.setCode(code);
		item.setName(name);
		item.setDescription(description);
		if(parentItem != null){
			for( int i= 0; i < parentItem.getChildrenItem().size() ; i++){
				DictionaryItemEntity itemEntity = parentItem.getChildrenItem().get(i);
				if(itemEntity.getId().equals(itemId)){
					parentItem.getChildrenItem().remove(itemEntity);
					i--;
				}
			}
			parentItem.getChildrenItem().add(item);
			item.setParentItem(parentItem);
			item.setDictionary(null);
		}
		if(dictionaryEntity != null){
			for( int i= 0; i < dictionaryEntity.getDictionaryItems().size() ; i++){
				DictionaryItemEntity itemEntity = dictionaryEntity.getDictionaryItems().get(i);
				if(itemEntity.getId().equals(itemId)){
					parentItem.getChildrenItem().remove(itemEntity);
					i--;
				}
			}
			dictionaryEntity.getDictionaryItems().add(item);
		}
		dictionaryItemManager.save(item);
	}

	@Override
	public void deleteDictionaryItem(String dictionaryId, String itemId) {
		DictionaryItemEntity item = dictionaryItemManager.query().filterEqual("dictionary.id", dictionaryId).filterEqual("id", itemId).first();
		if (item == null) {
			throw new NotFoundException(DictionaryItemEntity.class, itemId, null);
		}
		dictionaryItemManager.delete(item);
	}

	@Override
	public DictionaryItemEntity getDictionaryItemById(String itemId) {
		if(StringUtils.isBlank(itemId)){
			return null;
		}
		DictionaryItemEntity item = dictionaryItemManager.get(itemId);
		if (item == null) {
			throw new IFormException("未找到对应的数据字典【"+itemId+"】");
		}
		return item;
	}

	@Override
	public DictionaryItemEntity saveDictionaryItem(DictionaryItemEntity itemEntity) {
		return dictionaryItemManager.save(itemEntity);
	}


	@Override
	public Integer maxDictionaryItemOrderNo() {
		 Map<String, Object> map =	dictionaryItemManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from ifm_dictionary_item ");
		 if(map != null && map.get("order_no") != null){
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		 }
		 return 0;
	}

	@Override
	public Integer maxDictionaryOrderNo() {
		Map<String, Object> map =	dictionaryItemManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from ifm_dictionary ");
		if(map != null && map.get("order_no") != null){
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		}
		return 0;
	}
}
