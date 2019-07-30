package tech.ascs.icity.iform.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.googlecode.genericdao.search.Sort;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DictionaryDataItemModel;
import tech.ascs.icity.iform.api.model.DictionaryDataModel;
import tech.ascs.icity.iform.model.AreaCodeEntity;
import tech.ascs.icity.iform.model.DictionaryDataEntity;
import tech.ascs.icity.iform.model.DictionaryDataItemEntity;
import tech.ascs.icity.iform.service.DictionaryDataService;
import tech.ascs.icity.jpa.dao.exception.NotFoundException;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import javax.validation.constraints.NotNull;

public class DictionaryDataServiceImpl extends DefaultJPAService<DictionaryDataEntity> implements DictionaryDataService {

	private JPAManager<DictionaryDataItemEntity> dictionaryItemManager;
	private JPAManager<AreaCodeEntity> areaCodeManager;

	public DictionaryDataServiceImpl() {
		super(DictionaryDataEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		dictionaryItemManager = getJPAManagerFactory().getJPAManager(DictionaryDataItemEntity.class);
		areaCodeManager = getJPAManagerFactory().getJPAManager(AreaCodeEntity.class);
	}

	@Override
	public List<DictionaryDataItemEntity> findDictionaryItems(String dictionaryId) {
		DictionaryDataEntity dictionary = get(dictionaryId);
		return dictionary.getDictionaryItems();
	}

	@Override
	public void updateDictionaryItem(DictionaryDataItemModel dictionaryItemModel) {
		DictionaryDataItemEntity parentItemEntity = null;
		if (StringUtils.isNoneBlank(dictionaryItemModel.getParentId())) {
			parentItemEntity = getDictionaryItemById(dictionaryItemModel.getParentId());
		}
		DictionaryDataEntity dictionary = null;
		if (StringUtils.isNoneBlank(dictionaryItemModel.getDictionaryId())) {
			dictionary = get(dictionaryItemModel.getDictionaryId());
		}
		if (parentItemEntity == null && dictionary == null) {
			throw new IFormException("查询关联对象失败");
		}
		DictionaryDataItemEntity root = findRootDictionaryItem();
		if (parentItemEntity != null && !root.getId().equals(dictionaryItemModel.getParentId())) {
			dictionary = null;
		}
		DictionaryDataItemEntity item = getDictionaryItemById(dictionaryItemModel.getId());
		item.setCode(StringUtils.isBlank(dictionaryItemModel.getCode()) ? "key_"+System.currentTimeMillis() : dictionaryItemModel.getCode());

		item.setName(dictionaryItemModel.getName());
		item.setDescription(dictionaryItemModel.getDescription());
		item.setIcon(dictionaryItemModel.getIcon());
		for (int i = 0; i < root.getChildrenItem().size(); i++) {
			DictionaryDataItemEntity itemEntity = root.getChildrenItem().get(i);
			if(itemEntity.getId().equals(dictionaryItemModel.getId())){
				root.getChildrenItem().remove(itemEntity);
				i--;
			}
		}

		if (dictionary != null) {
			for (int i = 0; i < dictionary.getDictionaryItems().size(); i++) {
				DictionaryDataItemEntity itemEntity = dictionary.getDictionaryItems().get(i);
				if(itemEntity.getId().equals(dictionaryItemModel.getId())){
					dictionary.getDictionaryItems().remove(itemEntity);
					i--;
				}
			}
			item.setDictionary(dictionary);
			root.getChildrenItem().add(item);
			item.setParentItem(root);
			dictionary.getDictionaryItems().add(item);
		}else{
			for (int i = 0; i < parentItemEntity.getChildrenItem().size(); i++) {
				DictionaryDataItemEntity itemEntity = parentItemEntity.getChildrenItem().get(i);
				if (itemEntity.getId().equals(dictionaryItemModel.getId())) {
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
		DictionaryDataItemEntity item = dictionaryItemManager.get(itemId);
		if (item == null) {
			throw new NotFoundException(DictionaryDataItemEntity.class, itemId, null);
		}
		dictionaryItemManager.delete(item);
	}

	@Override
	public DictionaryDataItemEntity getDictionaryItemById(String itemId) {
		if (StringUtils.isBlank(itemId)) {
			return null;
		}
		DictionaryDataItemEntity item = dictionaryItemManager.find(itemId);
		/* if (item == null) {
			throw new IFormException("未找到对应的数据字典【" + itemId + "】");
		}
		*/
		return item;
	}

	@Override
	public DictionaryDataItemEntity saveDictionaryItem(DictionaryDataItemEntity itemEntity) {
		return dictionaryItemManager.save(itemEntity);
	}


	@Override
	public Integer maxDictionaryItemOrderNo() {
		List<Map<String, Object>> mapDataList = dictionaryItemManager.getJdbcTemplate().queryForList("select max(order_no) as order_no from ifm_dictionary_item ");
		if (mapDataList == null || mapDataList.size() < 1) {
			return 0;
		}
		Map<String, Object> map = mapDataList.get(0);
		if (map != null && map.get("order_no") != null) {
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		}
		return 0;
	}

	@Override
	public Integer maxDictionaryOrderNo() {
		List<Map<String, Object>> mapDataList = dictionaryItemManager.getJdbcTemplate().queryForList("select max(order_no) as order_no from ifm_dictionary ");
		if (mapDataList == null || mapDataList.size() < 1) {
			return 0;
		}
		Map<String, Object> map = mapDataList.get(0);
		if (map != null && map.get("order_no") != null) {
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		}
		return 0;
	}

	@Override
	public List<DictionaryDataItemEntity> findAllDictionaryItems(String dictionaryId) {
		if(StringUtils.isNoneBlank(dictionaryId)){
			return dictionaryItemManager.query().filterEqual("dictionary.id", dictionaryId).sort(Sort.asc("orderNo")).list();
		}
		return dictionaryItemManager.query().sort(Sort.asc("orderNo")).list();
	}

	@Override
	public DictionaryDataItemEntity findRootDictionaryItem() {
		return getRootItem();
	}

	@Override
	public List<DictionaryDataItemEntity> findByItemIds(String[] itemIds) {
		if (itemIds==null || itemIds.length==0) {
			return new ArrayList<>();
		}
		return dictionaryItemManager.query().filterIn("id", itemIds).list();
	}

	@Override
	public DictionaryDataModel getDictionaryByNameAndCode(String name, String code) {
		DictionaryDataEntity dictionaryEntity = null;
		if(StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(code)) {
			dictionaryEntity = query().filterEqual("name", name).filterEqual("code", code).first();
		}else if(StringUtils.isNotEmpty(name)){
			dictionaryEntity = query().filterEqual("name", name).first();
		}else if(StringUtils.isNotEmpty(code)){
			dictionaryEntity = query().filterEqual("code", code).first();
		}
		if(dictionaryEntity == null){
			return null;
		}
		return getByEntity(dictionaryEntity);
	}

	private synchronized DictionaryDataItemEntity getRootItem(){
		List<DictionaryDataItemEntity> dictionaryItems = dictionaryItemManager.findAll();
		DictionaryDataItemEntity rootDictionaryItemEntity = null;
		if(dictionaryItems != null) {
			for (DictionaryDataItemEntity dictionaryItemEntity : dictionaryItems){
				if(dictionaryItemEntity.getParentItem() == null && dictionaryItemEntity.getDictionary() == null && "root".equals(dictionaryItemEntity.getCode())){
					rootDictionaryItemEntity = dictionaryItemEntity;
					break;
				}
			}
		}
		if(rootDictionaryItemEntity == null) {
			rootDictionaryItemEntity = new DictionaryDataItemEntity();
			rootDictionaryItemEntity.setCode("root");
			rootDictionaryItemEntity.setName("根节点");
			rootDictionaryItemEntity.setDescription("根节点");
			rootDictionaryItemEntity.setOrderNo(-1000);
			dictionaryItemManager.save(rootDictionaryItemEntity);
		}
		return rootDictionaryItemEntity;
	}

	private void setItems(Set<DictionaryDataItemEntity> items, DictionaryDataItemEntity item){
		if(item.getDictionary() != null){
			//根节点
			if(item.getParentItem() != null){
				items.add(item.getParentItem());
			}
		}else {
			//子节点
			if(item.getChildrenItem() != null || item.getChildrenItem().size() > 0) {
				for(DictionaryDataItemEntity dictionaryItemEntity : item.getChildrenItem()) {
					setItems(items, dictionaryItemEntity);
				}
			}else{
				items.add(item.getParentItem());
			}
		}
	}

	private DictionaryDataItemModel getByEntity(DictionaryDataItemEntity dictionaryItemEntity) {
		DictionaryDataItemModel dictionaryItemModel = new DictionaryDataItemModel();
		BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});

		if (dictionaryItemEntity.getDictionary() != null) {
			dictionaryItemModel.setDictionaryId(dictionaryItemEntity.getDictionary().getId());
		}

		if (dictionaryItemEntity.getParentItem() != null) {
			dictionaryItemModel.setParentId(dictionaryItemEntity.getParentItem().getId());
		}

		if (dictionaryItemEntity.getChildrenItem() != null) {
			List<DictionaryDataItemModel> list = new ArrayList<>();
			for (DictionaryDataItemEntity childDictionaryItemEntity : dictionaryItemEntity.getChildrenItem()) {
				list.add(getByEntity(childDictionaryItemEntity));
			}
			dictionaryItemModel.setResources(list.size() < 1 ? null : list);
		}
		return dictionaryItemModel;
	}

	private DictionaryDataModel getByEntity(DictionaryDataEntity dictionaryEntity) {
		DictionaryDataModel dictionaryModel = new DictionaryDataModel();
		BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
		List<DictionaryDataItemEntity> dictionaryItems = dictionaryEntity.getDictionaryItems();
		if (dictionaryItems!= null && dictionaryItems.size() > 0) {
			List<DictionaryDataItemModel> list = new ArrayList<>();
			for (DictionaryDataItemEntity childDictionaryItemEntity : dictionaryItems) {
				list.add(getByEntity(childDictionaryItemEntity));
			}
			list.stream().sorted(Comparator.comparing(DictionaryDataItemModel::getOrderNo));
			dictionaryModel.setResources(list);
		}
		return dictionaryModel;
	}

	@Override
	public List<DictionaryDataItemModel> findDictionaryItems(String dictionaryId, @NotNull String itemName) {
		DictionaryDataEntity dictionaryEntity = find(dictionaryId);
		if (dictionaryEntity!=null) {
			List<DictionaryDataItemModel> list = itemTreeToList(getByEntity(dictionaryEntity).getResources());
			return list.stream().filter(item->item.getName()!=null && item.getName().contains(itemName)).collect(Collectors.toList());
		}
		return new ArrayList();
	}

	/**
	 * 树形结构转成平铺的List结构
	 * @param list
	 * @return
	 */
	private List<DictionaryDataItemModel> itemTreeToList(List<DictionaryDataItemModel> list) {
		List<DictionaryDataItemModel> returnList = new ArrayList<>();
		for (DictionaryDataItemModel item:list) {
			returnList.add(item);
			if (item.getResources()!=null && item.getResources().size()>0) {
				returnList.addAll(itemTreeToList(item.getResources()));
			}
		}
		return returnList;
	}

	@Override
	public List<DictionaryDataItemModel> queryAreaCodeOneLevel(String parentId) {
		if (StringUtils.isEmpty(parentId)) {
			// 查询所有
			List<AreaCodeEntity> list = areaCodeManager.query().filterNull("parent.id").sort(Sort.desc("orderNo")).list();
			return areaCodeEntityToDictionaryItem(list);
		} else {
			AreaCodeEntity areaCodeEntity = areaCodeManager.query().filterEqual("id", parentId).first();
			if (areaCodeEntity!=null) {
				List<AreaCodeEntity> children = areaCodeEntity.getChildren();
				return areaCodeEntityToDictionaryItem(children);
			}
		}
		return new ArrayList();
	}

	/**
	 * AreaCodeEntity实体类转成字典表，不转义子item
	 * @param list
	 * @return
	 */
	private List<DictionaryDataItemModel> areaCodeEntityToDictionaryItem(List<AreaCodeEntity> list) {
		if (list==null || list.size()==0) {
			return new ArrayList();
		}
		List<DictionaryDataItemModel> returnList = new ArrayList<>();
		for (AreaCodeEntity areaCodeEntity:list) {
			DictionaryDataItemModel dictionaryDataItemModel = new DictionaryDataItemModel();
			dictionaryDataItemModel.setId(areaCodeEntity.getId());
			dictionaryDataItemModel.setName(areaCodeEntity.getName());
			dictionaryDataItemModel.setCode(areaCodeEntity.getCode());
			dictionaryDataItemModel.setOrderNo(areaCodeEntity.getOrderNo());
			AreaCodeEntity parentAreaCode = new AreaCodeEntity();
			if (parentAreaCode!=null) {
				dictionaryDataItemModel.setParentId(parentAreaCode.getId());
			}
			returnList.add(dictionaryDataItemModel);
		}
		return returnList;
	}

	@Override
	public void addAreaCodeItem(DictionaryDataItemModel dictionaryItemModel) {
		String parentId = dictionaryItemModel.getParentId();
		AreaCodeEntity parent = findAreaCodeEntityById(parentId);
		AreaCodeEntity areaCodeEntity = new AreaCodeEntity();
		areaCodeEntity.setParent(parent);
		areaCodeEntity.setName(dictionaryItemModel.getName());
		areaCodeEntity.setCode(dictionaryItemModel.getCode());
		areaCodeEntity.setOrderNo(findMaxIndexAreaCodeEntity());
		areaCodeManager.save(areaCodeEntity);
	}

	@Override
	public AreaCodeEntity findAreaCodeEntityById(String id) {
		return areaCodeManager.find(id);
	}

	@Override
	public Integer findMaxIndexAreaCodeEntity() {
		List<Map<String, Object>> mapDataList = dictionaryItemManager.getJdbcTemplate().queryForList("select max(order_no) as order_no from ifm_area_code ");
		if (mapDataList == null || mapDataList.size() < 1) {
			return 0;
		}
		Map<String, Object> map = mapDataList.get(0);
		if (map != null && map.get("order_no") != null) {
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		}
		return 0;
	}
}
