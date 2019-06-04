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
import tech.ascs.icity.iform.utils.CommonUtils;
import tech.ascs.icity.jpa.dao.exception.NotFoundException;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.regex.Pattern;
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
		String oldTableName = dictionaryModelEntity.getTableName();
		verifyTableName(dictionaryModel);
		BeanUtils.copyProperties(dictionaryModel, dictionaryModelEntity);
		if(!StringUtils.equalsIgnoreCase(dictionaryModel.getTableName(), oldTableName)){
			updateTableName(oldTableName, dictionaryModel.getTableName());
		}
		dictionaryManager.save(dictionaryModelEntity);
	}

	@Override
	public void deleteDictionary(List<String> idList) {
		for(String id : idList) {
			DictionaryModelEntity dictionaryModelEntity = get(id);
			deleteTable(dictionaryModelEntity.getTableName());
			delete(dictionaryModelEntity);
		}
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
		verifyTableName(dictionaryModel);
		DictionaryModelEntity dictionaryModelEntity = new DictionaryModelEntity();
		BeanUtils.copyProperties(dictionaryModel, dictionaryModelEntity);
		dictionaryModelEntity.setOrderNo(maxDictionaryOrderNo()+1);
		dictionaryManager.save(dictionaryModelEntity);
		createDictionaryModelTable(dictionaryModel.getTableName());
		return new IdEntity(dictionaryModelEntity.getId());
	}

	//校验数据表名
	private void verifyTableName(DictionaryModel dictionaryModel){
		if(StringUtils.isBlank(dictionaryModel.getTableName()) || StringUtils.isBlank(dictionaryModel.getName())){
			throw new IFormException("数据表或字典名称为空了");
		}
		if (!Pattern.matches(CommonUtils.regEx, dictionaryModel.getTableName())) {
			throw new IFormException("数据表必须以字母开头，只能包含数字，字母，下划线，不能包含中文，横杆等特殊字符");
		}

		List<DictionaryModelEntity> list = dictionaryManager.query().filterEqual("tableName", dictionaryModel.getTableName()).list();
		if(list == null || list.size() < 1){
			return;
		}
		if(StringUtils.isBlank(dictionaryModel.getId())){
			throw new IFormException("数据表名【" + dictionaryModel.getTableName() + "】已经存在");
		}
		for(DictionaryModelEntity entity : list){
			if(!entity.getId().equals(dictionaryModel.getId())){
				throw new IFormException("数据表名【" + dictionaryModel.getTableName() + "】已经存在");
			}
		}
	}

	//更新表名
	private void updateTableName(String oldTableName, String newTableName){
		if(StringUtils.isBlank(oldTableName) || StringUtils.isBlank(newTableName)){
			return;
		}
		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select table_name from information_schema.tables where table_schema='iform'");
		if(mapList == null || mapList.size() < 1) {
			return;
		}
		for(Map<String, Object> map : mapList){
			if(map.get("table_name").equals(oldTableName)){
				dictionaryManager.getJdbcTemplate().execute("alter table `" + oldTableName + "` rename `" + newTableName + "`");
			}
		}
	}

	//删除表
	private void deleteTable(String tableName){
		if(StringUtils.isBlank(tableName)){
			return;
		}
		dictionaryManager.getJdbcTemplate().execute("DROP TABLE IF EXISTS `"+tableName+"`");
	}

	//创建一个数据库表
	private void createDictionaryModelTable(String tableName){
		if(StringUtils.isBlank(tableName)){
			return;
		}
		//deleteTable(tableName);
		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select table_name from information_schema.tables where table_schema='iform'");
		if(mapList == null || mapList.size() < 1) {
			return;
		}
		for(Map<String, Object> map : mapList){
			if(map.get("table_name").equals(tableName)){
				return;
			}
		}
		StringBuffer sub = new StringBuffer();
		sub.append("CREATE TABLE `"+tableName+"` (\n" +
				"  `id` varchar(32) NOT NULL COMMENT '主键',\n" +
				"  `name` varchar(255) DEFAULT NULL COMMENT '名称',\n" +
				"  `code` varchar(255) DEFAULT NULL COMMENT '编码',\n" +
				"  `description` varchar(255) DEFAULT NULL COMMENT '描述',\n" +
				"  `parent_id` varchar(32) DEFAULT NULL COMMENT '父级id',\n" +
				"  `order_no` int(11) DEFAULT NULL COMMENT '排序号',\n" +
				"  `icon` varchar(255) DEFAULT NULL COMMENT '图片icon',\n" +
				"  `size` int(11) DEFAULT NULL COMMENT '大小',\n" +
				"  `update_date` date DEFAULT NULL COMMENT '更新日期',\n" +
				"  PRIMARY KEY (`id`)\n" +
				")ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典建模表'\n");
		dictionaryManager.getJdbcTemplate().execute(sub.toString());
		dictionaryManager.getJdbcTemplate().execute("INSERT INTO `"+tableName+"` VALUES ('root', '根节点', 'root', '根节点', null, '0', null,0,'"+ CommonUtils.currentDateStr()+"')");
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
	public void updateDictionaryModelOrderNo(String id, String status) {
		List<DictionaryModelEntity> list = query().sort(Sort.asc("orderNo")).list();
		if(list == null || list.size() < 1){
			return;
		}
		Integer orderNo = null;
		Integer j = null;
		DictionaryModelEntity oldEntity = null;
		for(int i = 0; i < list.size(); i++){
			DictionaryModelEntity entity = list.get(i);
			if(entity.getId().equals(id)){
				j = i;
				orderNo = entity.getOrderNo();
				oldEntity = list.get(i);
			}
		}
		if(oldEntity == null){
			return;
		}
		DictionaryModelEntity upEntity = null;
		if("up".equals(status) && j > 0){
			upEntity = list.get(j-1);
		}else if("down".equals(status) && j < list.size() - 1){
			upEntity = list.get(j+1);
		}
		if(upEntity != null){
			oldEntity.setOrderNo(upEntity.getOrderNo());
			save(oldEntity);
			upEntity.setOrderNo(orderNo);
			save(upEntity);
		}
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
		String icon = dictionaryModelData.getIcon() == null ? null : "'"+dictionaryModelData.getIcon()+"'";
		String parentId = dictionaryModelData.getParentId() == null ? null : "'"+dictionaryModelData.getParentId()+"'";
		String description = dictionaryModelData.getDescription() == null ? null : "'"+dictionaryModelData.getDescription()+"'";
		if(dictionaryModelData.getId() == null){
			String id = UUID.randomUUID().toString().replace("-", "");
			sql = "INSERT INTO `"+dictionaryModelModel.getTableName()+"` VALUES ('"+id+"', '"+dictionaryModelData.getName()+"', '"+dictionaryModelData.getCode()+"', "+description+", "+parentId+", "+dictionaryModelData.getOrderNo()+","+icon+",0,'"+ CommonUtils.currentDateStr()+"')";
		}else{
			sql = "update `"+dictionaryModelModel.getTableName()+"` set name ='"+dictionaryModelData.getName()+"', code ='"+dictionaryModelData.getCode()+"', description ="+description+", parent_id = "+parentId+", order_no = "+dictionaryModelData.getOrderNo()+", icon = "+icon+",size = "+dictionaryModelData.getSize()+",update_date='"+ CommonUtils.currentDateStr()+"')";
		}
		dictionaryManager.getJdbcTemplate().execute(sql);
	}

	@Override
	public void deleteDictionaryModelData(List<DictionaryModelData> dictionaryModelDataList) {
		for(DictionaryModelData dictionaryModelData : dictionaryModelDataList) {
			DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryModelData.getDictionaryId());
			List<String> idList = new ArrayList<>();
			idList.add(dictionaryModelData.getId());
			findAllChildrenId(idList, dictionaryModelModel.getTableName(), dictionaryModelData.getId());
			if(idList == null || idList.size() < 1){
				continue;
			}
			StringBuffer sub = new StringBuffer("('");
			for (int i = 0; i < idList.size(); i++) {
				if (i == 0) {
					sub.append(idList.get(i));
				} else {
					sub.append("','" + idList.get(i));
				}
			}
			sub.append("')");
			dictionaryManager.getJdbcTemplate().execute("delete from " + dictionaryModelModel.getTableName() + " where id in " + sub.toString());
		}
	}



	@Override
	public void updateDictionaryModelDataOrderNo(String dictionaryId, String id, String status) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select * from "+dictionaryModelModel.getTableName()+" where id='"+id+"'");
		if (map != null && map.get("parent_id") != null) {
			String parentId = String.valueOf(map.get("parent_id"));
			List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select * from "+dictionaryModelModel.getTableName()+" where parent_id='"+parentId + "' order by order_no asc");
			if(mapList != null && mapList.size() > 0){
				Map<Integer, Object> dataMap = new HashMap<>();
				Integer j = null;
				Integer orderNo = null;
				for (int i = 0; i < mapList.size() ; i ++){
					dataMap.put(i, mapList.get(i));
					if(mapList.get(i).get("id").equals(id)){
						j = i;
						orderNo = mapList.get(i).get("order_no") == null ? 0 : Integer.parseInt(String.valueOf(mapList.get(i).get("order_no")));
					}
				}
				Map<String, Object> newDataMap = null;
				if("up".equals(status) && j != null && j > 0) {
					newDataMap = ((Map<String, Object>)dataMap.get(j-1));
				}else if("down".equals(status) && j != null && j < mapList.size() -1){
					newDataMap = ((Map<String, Object>)dataMap.get(j+1));
				}
				if(newDataMap != null){
					dictionaryManager.getJdbcTemplate().execute("update "+dictionaryModelModel.getTableName()+" set order_no = "+newDataMap.get("order_no")+ " where id = '"+id+"'");
					dictionaryManager.getJdbcTemplate().execute("update "+dictionaryModelModel.getTableName()+" set order_no = "+orderNo+ " where id = '"+newDataMap.get("id")+"'");
				}
			}
		}
	}

	@Override
	public String getDictionaryModelDataName(String dictionaryId, List<String> ids) {

		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		StringBuffer sub = new StringBuffer("('");
		for(int i = 0 ; i < ids.size() ; i++){
			if(i == 0){
				sub.append(ids.get(i));
			}else{
				sub.append("','"+ids.get(i));
			}
		}
		sub.append("')");

		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select name from "+dictionaryModelModel.getTableName()+" where id in "+sub.toString());
		if (mapList == null || mapList.size() < 1) {
			return "";
		}
		List<String> list = new ArrayList<>();
		for(Map<String, Object> map  : mapList){
			list.add(map.get("name") == null ? "" : (String)map.get("name"));
		}
		return String.join(",", list);
	}

	@Override
	public DictionaryModelData findDictionaryModelDataByDictionaryId(String dictionaryId) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select * from "+dictionaryModelModel.getTableName()+" where id='root'");
		if (map == null) {
			return null;
		}
		DictionaryModelData rootDictionaryModelData = dictionaryModelData(dictionaryId, "root", map);
		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select * from "+dictionaryModelModel.getTableName()+" where id != 'root' order by order_no asc,parent_id desc,id desc ");
		if(mapList != null && mapList.size() > 0){
			Map<String, List<Map<String, Object>>> dataListMap = new HashMap<>();
			for(Map<String, Object> dataMap : mapList){
				if(dataMap.get("parent_id") == null){
					continue;
				}
				List<Map<String, Object>> mapList2 = dataListMap.get(dataMap.get("parent_id"));
				if(mapList2 == null){
					mapList2 = new ArrayList<>();
				}
				mapList2.add(dataMap);
				dataListMap.put(String.valueOf(dataMap.get("parent_id")), mapList2);
			}
			setResources( dataListMap,  rootDictionaryModelData,  dictionaryId);
		}

		return rootDictionaryModelData;
	}

	private void setResources(Map<String, List<Map<String, Object>>> dataListMap, DictionaryModelData parentDictionaryModelData, String dictionaryId){
		List<Map<String, Object>> maps = dataListMap.get(parentDictionaryModelData.getId());
		if(maps == null){
			return;
		}
		List<DictionaryModelData> dictionaryModelDatas = new ArrayList<>();
		for(Map<String, Object> objectMap : maps){
			DictionaryModelData dictionaryModelData = dictionaryModelData(dictionaryId, String.valueOf(objectMap.get("id")), objectMap);
			setResources(dataListMap, dictionaryModelData, dictionaryId);
			dictionaryModelDatas.add(dictionaryModelData);
		}
		parentDictionaryModelData.setResources(dictionaryModelDatas);
	}

	@Override
	public DictionaryModelData getDictionaryModelDataById(String dictionaryId, String id) {
		DictionaryModel dictionaryModelModel = getDictionaryById(dictionaryId);
		Map<String, Object> map = dictionaryManager.getJdbcTemplate().queryForMap("select * from "+dictionaryModelModel.getTableName()+" where id='"+id+"'");
		if (map == null) {
			return null;
		}
		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select * from "+dictionaryModelModel.getTableName()+" where parent_id='"+map.get("parent_id") + "' order by order_no asc");
		DictionaryModelData dictionaryModelData = dictionaryModelData(dictionaryId, id, map);
		if(mapList != null && mapList.size() > 0){
			List<DictionaryModelData> list = new ArrayList<>();
			for(Map<String, Object> mapData : mapList){
				list.add(dictionaryModelData(dictionaryId, (String)mapData.get("id"), mapData));
			}
			dictionaryModelData.setResources(list);
		}
		return dictionaryModelData;
	}

	@Override
	public List<String> getAllParentIdsById(String dictionaryId, String id) {
		DictionaryModelData dictionaryModelData = getDictionaryModelDataById(dictionaryId, id);
		List<String> idList = new ArrayList<>();
		idList.add(id);
		findAllParentIds( idList,dictionaryModelData.getName(), id);
		return idList;
	}

	@Override
	public List<String> getAllChildrenIdById(String dictionaryId, String id) {
		DictionaryModelData dictionaryModelData = getDictionaryModelDataById(dictionaryId, id);
		List<String> idList = new ArrayList<>();
		idList.add(id);
		findAllChildrenId( idList,dictionaryModelData.getName(), id);
		return idList;
	}

	private void findAllParentIds(List<String> idList, String tableName, String id){
		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select parent_id from "+tableName+" where id='"+id+"'");
		if(mapList != null && mapList.size() > 0){
			for(Map<String, Object> map : mapList){
				String idstr = (String)map.get("parent_id");
				idList.add(idstr);
				findAllChildrenId(idList, tableName, idstr);
			}
		}
	}

	private void findAllChildrenId(List<String> idList, String tableName, String id){
		List<Map<String, Object>> mapList = dictionaryManager.getJdbcTemplate().queryForList("select id from "+tableName+" where parent_id='"+id+"'");
		if(mapList != null && mapList.size() > 0){
			for(Map<String, Object> map : mapList){
				String parentId = (String)map.get("id");
				idList.add(parentId);
				findAllChildrenId(idList, tableName, parentId);
			}
		}
	}


	private DictionaryModelData dictionaryModelData(String dictionaryId, String id, Map<String, Object> map){
		DictionaryModelData dictionaryModelData = new DictionaryModelData();

		dictionaryModelData.setDictionaryId(dictionaryId);
		dictionaryModelData.setId(id);
		dictionaryModelData.setName((String)map.get("name"));
		dictionaryModelData.setCode((String)map.get("code"));
		dictionaryModelData.setDescription((String)map.get("description"));
		dictionaryModelData.setIcon((String)map.get("icon"));
		dictionaryModelData.setOrderNo(map.get("order_no") == null ? null : Integer.parseInt(String.valueOf(map.get("order_no"))));
		dictionaryModelData.setParentId(map.get("parent_id") == null ? null : String.valueOf(map.get("parent_id")));
		dictionaryModelData.setSize(map.get("size") == null ? null : Integer.parseInt(String.valueOf(map.get("size"))));
		dictionaryModelData.setUpdateDate(map.get("update_date") == null ? null : (Date)(map.get("update_date")));

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
