package tech.ascs.icity.iform.service;

import java.util.List;

import tech.ascs.icity.iform.api.model.DictionaryDataItemModel;
import tech.ascs.icity.iform.api.model.DictionaryDataModel;
import tech.ascs.icity.iform.model.DictionaryDataEntity;
import tech.ascs.icity.iform.model.DictionaryDataItemEntity;
import tech.ascs.icity.jpa.service.JPAService;

import javax.validation.constraints.NotNull;


public interface DictionaryDataService extends JPAService<DictionaryDataEntity> {

	/**
	 * 获取字典表选项列表
	 * 
	 * @param dictionaryId 父数据字典id
	 * @return
	 */
	List<DictionaryDataItemEntity> findDictionaryItems(String dictionaryId);

	/**
	 * 更新字典表选项
	 *
	 */
	void updateDictionaryItem(DictionaryDataItemModel dictionaryItemModel);

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
	DictionaryDataItemEntity getDictionaryItemById(String itemId);

	/**
	 * 保存数据字典项
	 *
	 * @param itemEntity 数据字典
	 * @return
	 */
	DictionaryDataItemEntity saveDictionaryItem(DictionaryDataItemEntity itemEntity);

	//查找系统代码最大排序号
	Integer maxDictionaryItemOrderNo();

	//查找系统代码分类最大排序号
	Integer maxDictionaryOrderNo();


	/**
	 * 获取所有字典表选项列表
	 *
	 * @return
	 */
	List<DictionaryDataItemEntity> findAllDictionaryItems(String dictionaryId);


	/**
	 * 获取获取根节点
	 *
	 * @return
	 */
	DictionaryDataItemEntity findRootDictionaryItem();

	/**
	 * 通过itemIds获取相应的字典表item项
	 * @return
	 */
	List<DictionaryDataItemEntity> findByItemIds(String[] itemIds);

    DictionaryDataModel getDictionaryByNameAndCode(String name, String code);

	/**
	 * 模糊搜索字典表,返回结构不是树形结构,是平铺的集合
	 *
	 * @param dictionaryId 父数据字典id
	 * @return
	 */
	List<DictionaryDataItemModel> findDictionaryItems(String dictionaryId, @NotNull String itemName);



	// 查询行政区划的树形结构
	/**
	 * 返回树形结构
	 * @param parentId
	 * @return
	 */
	List<DictionaryDataItemModel> queryAreaCodeTreeList(String parentId);

}

