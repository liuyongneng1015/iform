package tech.ascs.icity.iform.service.impl;

import java.util.List;
import java.util.Map;

import com.googlecode.genericdao.search.Sort;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.iform.IFormException;
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
		if(StringUtils.isNoneBlank(parentItemId)) {
			parentItemEntity = getDictionaryItemById(parentItemId);
		}
		DictionaryEntity dictionary = null;
		if(StringUtils.isNoneBlank(dictionaryId)) {
			dictionary = get(dictionaryId);
		}
		if(parentItemEntity == null && dictionary == null){
			throw new IFormException("查询关联对象失败");
		}
		if(parentItemEntity != null){
			dictionary = null;
		}
		DictionaryItemEntity item = getDictionaryItemById(itemId);
		item.setCode(code);
		item.setName(name);
		item.setDescription(description);
		if(parentItemEntity != null){
			for(int i = 0; i < parentItemEntity.getChildrenItem().size() ; i++){
				DictionaryItemEntity itemEntity = parentItemEntity.getChildrenItem().get(i);
				if(itemEntity.getId().equals(itemId)){
					parentItemEntity.getChildrenItem().remove(itemEntity);
					i--;
				}
			}
			parentItemEntity.getChildrenItem().add(item);
			item.setParentItem(parentItemEntity);
			item.setDictionary(null);
		}
		if(dictionary != null){
			for(int i = 0; i < dictionary.getDictionaryItems().size() ; i++){
				DictionaryItemEntity itemEntity = dictionary.getDictionaryItems().get(i);
				if(itemEntity.getId().equals(itemId)){
					dictionary.getDictionaryItems().remove(itemEntity);
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

	@Override
	public List<DictionaryItemEntity> findAllDictionaryItems() {
		return dictionaryItemManager.query().sort(Sort.asc("orderNo")).list();
	}
}
