package tech.ascs.icity.iform.service;

import java.util.List;

import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.jpa.service.JPAService;


public interface DictionaryService extends JPAService<DictionaryEntity> {

	/**
	 * 获取字典表选项列表
	 * 
	 * @param dictionaryId 字典表ID
	 * @return
	 */
	List<DictionaryItemEntity> getDictionaryItems(String dictionaryId);

	/**
	 * 更新字典表选项
	 * 
	 * @param dictionaryId 字典表ID
	 * @param itemId 选项ID
	 * @param code 编码
	 * @param name 名称
	 * @param description 描述
	 */
	void updateDictionaryItem(String dictionaryId, String itemId, String code, String name, String description);

	/**
	 * 删除字典表选项
	 * 
	 * @param dictionaryId 字典表ID
	 * @param itemId 选项ID
	 */
	void deleteDictionaryItem(String dictionaryId, String itemId);
	
//	
//	void postDicData(Long id, String name, String description, Long parentId);
//
//	void deleteDicData(Long id);
}

