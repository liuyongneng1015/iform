package tech.ascs.icity.iform.service.impl;

import com.googlecode.genericdao.search.Sort;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DictionaryDataItemModel;
import tech.ascs.icity.iform.api.model.DictionaryDataModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.api.model.DictionaryModelData;
import tech.ascs.icity.iform.model.DictionaryDataEntity;
import tech.ascs.icity.iform.model.DictionaryDataItemEntity;
import tech.ascs.icity.iform.model.DictionaryModelEntity;
import tech.ascs.icity.iform.service.DictionaryDataService;
import tech.ascs.icity.iform.service.DictionaryModelService;
import tech.ascs.icity.jpa.dao.exception.NotFoundException;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class DictionaryModelServiceImpl extends DefaultJPAService<DictionaryModelEntity> implements DictionaryModelService {

	private JPAManager<DictionaryModelEntity> dictionaryManager;

	public DictionaryModelServiceImpl() {
		super(DictionaryModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		dictionaryManager = getJPAManagerFactory().getJPAManager(DictionaryModelEntity.class);
	}


	@Override
	public List<DictionaryModel> findAllDictionary() {
		List<DictionaryModelEntity> list = query().sort(Sort.asc("orderNo")).list();
		List<DictionaryModel> dictionaryModels = null;
		if(list != null && list.size() > 0){
			dictionaryModels = new ArrayList<>();
			for(DictionaryModelEntity entity : list){
				dictionaryModels.add(dto(entity));
			}
		}
		return dictionaryModels;
	}

	private DictionaryModel dto(DictionaryModelEntity dictionaryModelEntity){
		DictionaryModel dictionaryModel = new DictionaryModel();
		BeanUtils.copyProperties(dictionaryModelEntity, dictionaryModel);
		return dictionaryModel;
	}

	@Override
	public void updateDictionaryModel(DictionaryModel dictionaryModel) {
		DictionaryModelEntity dictionaryModelEntity = find(dictionaryModel.getId());
		if(dictionaryModelEntity == null){
			throw  new IFormException("未找到【"+dictionaryModel.getId()+"】对应的字典建模");
		}
		BeanUtils.copyProperties(dictionaryModel, dictionaryModelEntity);
		if(!StringUtils.equalsIgnoreCase(dictionaryModel.getTableName(), dictionaryModelEntity.getTableName())){
			updateTableName(dictionaryModelEntity.getTableName(), dictionaryModel.getTableName());
		}
		dictionaryManager.save(dictionaryModelEntity);
	}

	@Override
	public void deleteDictionary(String id) {
		DictionaryModelEntity dictionaryModelEntity = get(id);
		deleteTable(dictionaryModelEntity.getTableName());
		delete(dictionaryModelEntity);
	}

	@Override
	public DictionaryModel getDictionaryById(String id) {
		DictionaryModelEntity dictionaryModelEntity = find(id);
		if(dictionaryModelEntity == null){
			throw  new IFormException("未找到【"+id+"】对应的字典建模");
		}
		return dto(dictionaryModelEntity);
	}

	@Override
	public IdEntity addDictionary(DictionaryModel dictionaryModel) {
		DictionaryModelEntity dictionaryModelEntity = new DictionaryModelEntity();
		BeanUtils.copyProperties(dictionaryModel, dictionaryModelEntity);
		dictionaryModelEntity.setOrderNo(maxDictionaryOrderNo()+1);
		dictionaryManager.save(dictionaryModelEntity);
		createDictionaryModelTable(dictionaryModel.getTableName());
		return new IdEntity(dictionaryModelEntity.getId());
	}

	//更新表名
	private void updateTableName(String oldTableName, String newTableName){
		if(StringUtils.isBlank(oldTableName) || StringUtils.isBlank(newTableName)){
			return;
		}
		dictionaryManager.getJdbcTemplate().execute("alter table "+oldTableName+" rename "+newTableName);

	}

	//删除表
	private void deleteTable(String tableName){
		if(StringUtils.isBlank(tableName)){
			return;
		}
		dictionaryManager.getJdbcTemplate().execute("DROP TABLE IF EXISTS `"+tableName);
	}

	//创建一个数据库表
	private void createDictionaryModelTable(String tableName){
		if(StringUtils.isBlank(tableName)){
			return;
		}
		deleteTable(tableName);
		StringBuffer sub = new StringBuffer();
		sub.append("CREATE TABLE `"+tableName+"` (\n" +
				"  `id` int NOT NULL AUTO_INCREMENT,\n" +
				"  `name` varchar(255) DEFAULT NULL,\n" +
				"  `code` varchar(255) DEFAULT NULL,\n" +
				"  `description` varchar(255) DEFAULT NULL,\n" +
				"  `parent_id` int(11) DEFAULT NULL,\n" +
				"  `order_no` int(11) DEFAULT NULL,\n" +
				"  `icon` varchar(255) DEFAULT NULL,\n" +
				"  PRIMARY KEY (`id`)\n" +
				") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4\n");
		dictionaryManager.getJdbcTemplate().execute(sub.toString());
		dictionaryManager.getJdbcTemplate().execute("INSERT INTO `ifm_dictionary_itedsfsfsm` VALUES ('1', '根节点', 'root', '根节点', null, '0', null)");
	}

	@Override
	public Integer maxDictionaryOrderNo() {
		return getMaxOrderNo("ifm_dictionary_model");
	}

	@Override
	public Page<DictionaryModel> page(int page, int pageSize, String name) {
		Page<DictionaryModelEntity> dictionaryModelEntities = null;
		if(StringUtils.isBlank(name)) {
			dictionaryModelEntities = query().page(page, pageSize).sort(Sort.asc("orderNo")).page();
		}else{
			dictionaryModelEntities = query().filterLike("name", "%"+ name + "%").page(page, pageSize).sort(Sort.asc("orderNo")).page();
		}
		Page<DictionaryModel> dictionaryModelPage =  Page.get(page, pageSize);
		List<DictionaryModel> list = new ArrayList<>();
		if(dictionaryModelEntities.getResults() != null){
			for(DictionaryModelEntity entity : dictionaryModelEntities.getResults()){
				list.add(dto(entity));
			}
		}
		return dictionaryModelPage.data(dictionaryModelEntities.getTotalCount(), list);
	}

	@Override
	public Integer maxTableOrderNo(String tableName) {
		return getMaxOrderNo(tableName);
	}

	@Override
	public void saveDictionaryModelData(DictionaryModelData dictionaryModelData) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryModelData.getDictionaryId());
		if(dictionaryModelData.getId() == null){
			Integer maxOrderNo = maxTableOrderNo(dictionaryModelModel.getTableName());
			dictionaryModelData.setOrderNo(maxOrderNo == null ? 1 :  maxOrderNo + 1);
			dictionaryModelData.setCode(StringUtils.isBlank(dictionaryModelData.getCode()) ? "key_"+dictionaryModelData.getOrderNo() : dictionaryModelData.getCode());
		}else{
			dictionaryModelData.setCode(StringUtils.isBlank(dictionaryModelData.getCode()) ? "key_"+System.currentTimeMillis() : dictionaryModelData.getCode());
		}
		saveData(dictionaryModelData);
	}

	private void saveData(DictionaryModelData dictionaryModelData){
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryModelData.getDictionaryId());
		String sql = null;
		if(dictionaryModelData.getId() == null){
			sql = "INSERT INTO `"+dictionaryModelModel.getTableName()+"` VALUES (null, '"+dictionaryModelData.getName()+"', '"+dictionaryModelData.getCode()+"', '"+dictionaryModelData.getDescription()+"', "+dictionaryModelData.getParentId()+", "+dictionaryModelData.getOrderNo()+", '"+dictionaryModelData.getIcon()+"')";
		}else{
			sql = "update `"+dictionaryModelModel.getTableName()+"` set name ='"+dictionaryModelData.getName()+"', code ='"+dictionaryModelData.getCode()+"', description ='"+dictionaryModelData.getDescription()+"', parent_id = "+dictionaryModelData.getParentId()+", order_no = "+dictionaryModelData.getOrderNo()+", icon = '"+dictionaryModelData.getIcon()+"')";
		}
		dictionaryManager.getJdbcTemplate().execute(sql);
	}

	@Override
	public void deleteDictionaryModelData(DictionaryModelData dictionaryModelData) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryModelData.getDictionaryId());
		dictionaryManager.getJdbcTemplate().execute("delete from "+dictionaryModelModel.getTableName()+" where id = "+dictionaryModelData.getId());
	}

	@Override
	public void updateDictionaryModelDataOrderNo(String dictionaryId, Integer id, String status) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select * from "+dictionaryModelModel.getTableName()+" where id="+id);
		if (map != null && map.get("parent_id") != null) {
			Integer parentId = Integer.parseInt(String.valueOf(map.get("parent_id")));
			List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select * from "+dictionaryModelModel.getTableName()+" where parent_id="+parentId + " order by order_no asc");
			if(mapList != null && mapList.size() > 0){
				Map<Integer, Object> dataMap = new HashMap<>();
				Integer j = null;
				Integer orderNo = null;
				for (int i = 0; i < mapList.size() ; i ++){
					dataMap.put(i, mapList.get(i));
					if(mapList.get(i).get("id") == id){
						j = i;
						orderNo =Integer.parseInt(String.valueOf(mapList.get(i).get("order_no")));
					}
				}
				Map<String, Object> newDataMap = null;
				if("up".equals(status) && j != null && j > 0) {
					newDataMap = ((Map<String, Object>)dataMap.get(j-1));
				}else if("down".equals(status) && j != null && j < mapList.size() -2){
					newDataMap = ((Map<String, Object>)dataMap.get(j+1));
				}
				if(newDataMap != null){
					dictionaryManager.getJdbcTemplate().execute("update "+dictionaryModelModel.getTableName()+" set order_no = "+newDataMap.get("order_no")+ "where id = "+id);
					dictionaryManager.getJdbcTemplate().execute("update "+dictionaryModelModel.getTableName()+" set order_no = "+orderNo+ "where id = "+newDataMap.get("id"));
				}
			}
		}
	}

	@Override
	public String getDictionaryModelDataName(String dictionaryId, Integer id) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select * from "+dictionaryModelModel.getTableName()+" where id="+id);
		if (map != null && map.get("name") != null) {
			return String.valueOf(map.get("name"));
		}
		return "";
	}

	@Override
	public DictionaryModelData getDictionaryModelDataByDictionaryId(String dictionaryId) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select * from "+dictionaryModelModel.getTableName()+" where id="+1);
		if (map == null) {
			return null;
		}
		DictionaryModelData rootDictionaryModelData = dictionaryModelData(dictionaryId, 1, map);
		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select * from "+dictionaryModelModel.getTableName()+" where id != 1 order by parent_id desc, order_no asc");
		if(mapList != null && mapList.size() > 0){
			Map<Integer, List<Map<String, Object>>> dataListMap = new HashMap<>();
			for(Map<String, Object> dataMap : mapList){
				List<Map<String, Object>> mapList2 = dataListMap.get(dataMap.get("parent_id"));
				if(mapList2 == null){
					mapList2 = new ArrayList<>();
				}
				mapList2.add(dataMap);
				dataListMap.put(Integer.parseInt(String.valueOf(dataMap.get("parent_id"))), mapList2);
			}
			setResources( dataListMap,  rootDictionaryModelData,  dictionaryId);
		}

		return rootDictionaryModelData;
	}

	private void setResources(Map<Integer, List<Map<String, Object>>> dataListMap, DictionaryModelData parentDictionaryModelData, String dictionaryId){
		List<Map<String, Object>> maps = dataListMap.get(parentDictionaryModelData.getId());
		if(maps == null){
			return;
		}
		List<DictionaryModelData> dictionaryModelDatas = new ArrayList<>();
		for(Map<String, Object> objectMap : maps){
			DictionaryModelData dictionaryModelData = dictionaryModelData(dictionaryId, Integer.parseInt(String.valueOf(objectMap.get("id"))), objectMap);
			setResources(dataListMap, dictionaryModelData, dictionaryId);
			dictionaryModelDatas.add(dictionaryModelData);
		}
		parentDictionaryModelData.setResources(dictionaryModelDatas);
	}

	@Override
	public DictionaryModelData getDictionaryModelDataById(String dictionaryId, Integer id) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select * from "+dictionaryModelModel.getTableName()+" where id="+id);
		if (map == null) {
			return null;
		}
		return dictionaryModelData(dictionaryId, id, map);
	}

	private DictionaryModelData dictionaryModelData(String dictionaryId, Integer id, Map<String, Object> map){
		DictionaryModelData dictionaryModelData = new DictionaryModelData();

		dictionaryModelData.setDictionaryId(dictionaryId);
		dictionaryModelData.setId(id);
		dictionaryModelData.setName((String)map.get("name"));
		dictionaryModelData.setCode((String)map.get("code"));
		dictionaryModelData.setDescription((String)map.get("description"));
		dictionaryModelData.setIcon((String)map.get("icon"));
		dictionaryModelData.setOrderNo(Integer.parseInt(String.valueOf(map.get("order_no"))));
		dictionaryModelData.setParentId(Integer.parseInt(String.valueOf(map.get("parent_id"))));
		return dictionaryModelData;
	}

	private Integer getMaxOrderNo(String tableName){
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from "+tableName);
		if (map != null && map.get("order_no") != null) {
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		}
		return 0;
	}
}
