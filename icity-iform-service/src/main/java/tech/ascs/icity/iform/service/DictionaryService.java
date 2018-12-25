package tech.ascs.icity.iform.service;

import java.util.List;

import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.jpa.service.JPAService;


public interface DictionaryService extends JPAService<DictionaryEntity> {

	/**
	 * 获取字典表选项列表
	 * 
	 * @param dictionaryId 父数据字典id
	 * @return
	 */
	List<DictionaryItemEntity> findDictionaryItems(String dictionaryId);

	/**
	 * 更新字典表选项
	 * 
	 * @param dictionaryId 字典表ID
	 * @param itemId 选项ID
	 * @param code 编码
	 * @param name 名称
	 * @param description 描述
	 */
	void updateDictionaryItem(String dictionaryId, String itemId, String code, String name, String description, String parentItemId);

	/**
	 * 删除字典表选项
	 * 
	 * @param itemId 选项ID
	 */
	void deleteDictionaryItem(String itemId);

	/**
	 * 获取字典表选项列表
	 *
	 * @param itemId 字典表ID
	 * @return
	 */
	DictionaryItemEntity getDictionaryItemById(String itemId);

	/**
	 * 保存数据字典项
	 *
	 * @param itemEntity 数据字典
	 * @return
	 */
	DictionaryItemEntity saveDictionaryItem(DictionaryItemEntity itemEntity);

	//查找系统代码最大排序号
	Integer maxDictionaryItemOrderNo();

	//查找系统代码分类最大排序号
	Integer maxDictionaryOrderNo();


	/**
	 * 获取所有字典表选项列表
	 *
	 * @return
	 */
	List<DictionaryItemEntity> findAllDictionaryItems(String dictionaryId);


	/**
	 * 获取获取根节点
	 *
	 * @return
	 */
	DictionaryItemEntity findRootDictionaryItem();

}

