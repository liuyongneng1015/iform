package tech.ascs.icity.iform.service;

import java.util.List;

import tech.ascs.icity.iform.api.model.DictionaryItemModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.jpa.service.JPAService;

import javax.validation.constraints.NotNull;


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
	 */
	void updateDictionaryItem(DictionaryItemModel dictionaryItemModel);

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

	/**
	 * 通过itemIds获取相应的字典表item项
	 * @return
	 */
	List<DictionaryItemEntity> findByItemIds(String[] itemIds);

    DictionaryModel getDictionaryByNameAndCode(String name, String code);

	/**
	 * 模糊搜索字典表,返回结构不是树形结构,是平铺的集合
	 *
	 * @param dictionaryId 父数据字典id
	 * @return
	 */
	List<DictionaryItemModel> findDictionaryItems(String dictionaryId, @NotNull String itemName);
}

