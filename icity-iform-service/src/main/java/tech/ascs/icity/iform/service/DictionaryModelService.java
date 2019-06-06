package tech.ascs.icity.iform.service;
;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.api.model.DictionaryModelData;
import tech.ascs.icity.iform.model.DictionaryModelEntity;
import tech.ascs.icity.jpa.service.JPAService;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import java.util.List;


public interface DictionaryModelService extends JPAService<DictionaryModelEntity> {

	/**
	 * 获取字典建模列表
	 * 
	 * @return
	 */
	List<DictionaryModel> findAllDictionary();

	/**
	 * 更新字典建模
	 *
	 */
	void updateDictionaryModel(DictionaryModel dictionaryModel);

	/**
	 * 删除字典建模
	 * 
	 * @param idList
	 */
	void deleteDictionary(List<String> idList);

	/**
	 * 获取字典建模
	 *
	 * @param id
	 * @return
	 */
	DictionaryModel getDictionaryById(String id);

	/**
	 * 保存数据字典建模
	 *
	 * @param dictionaryModel 字典建模
	 * @return
	 */
	IdEntity addDictionary(DictionaryModel dictionaryModel);


	//查找字典建模最大排序号
	Integer maxDictionaryOrderNo();

	Page<DictionaryModel> page(int page, int pageSize, String name);

	//查找字典建模数据表最大排序号
	Integer maxTableOrderNo(String tableName);

	/**
	 * 上线移动模型
	 *
	 * @param id
	 * @return
	 */
	void updateDictionaryModelOrderNo(String id, String status);


	//保存字典建模数据表
	void saveDictionaryModelData(DictionaryModelData dictionaryModelData);

	//删除字典建模数据表
	void deleteDictionaryModelData(List<DictionaryModelData> dictionaryModelDataList);

	//更新排序
	void updateDictionaryModelDataOrderNo(String dictionaryId, String id, String status);

	//查询字典模型数据名称
	String getDictionaryModelDataName(String dictionaryId, List<String> ids);

	/**
	 * 根据字典模型id获取模型数据
	 *
	 * @param dictionaryId
	 * @return
	 */
	DictionaryModelData findDictionaryModelDataByDictionaryId(String dictionaryId);

	/**
	 * 根据id获取模型数据
	 *
	 * @param id
	 * @return
	 */
	DictionaryModelData getDictionaryModelDataById(String dictionaryId, String id);

	/**
	 * 根据id获取模型数据父级id集合
	 *
	 * @param id
	 * @return
	 */
	List<String> getAllParentIdsById(String dictionaryId, String id);

	/**
	 * 根据id获取模型数据子级id集合
	 *
	 * @param id
	 * @return
	 */
	List<String> getAllChildrenIdById(String dictionaryId, String id);

}

