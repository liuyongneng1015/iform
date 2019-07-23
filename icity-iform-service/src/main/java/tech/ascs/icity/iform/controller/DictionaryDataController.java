package tech.ascs.icity.iform.controller;

import com.googlecode.genericdao.search.Sort;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.admin.api.model.Resource;
import tech.ascs.icity.iform.api.model.SystemCodeModel;
import tech.ascs.icity.iform.model.SelectItemModelEntity;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DictionaryDataItemModel;
import tech.ascs.icity.iform.api.model.DictionaryDataModel;
import tech.ascs.icity.iform.model.DictionaryDataEntity;
import tech.ascs.icity.iform.model.DictionaryDataItemEntity;
import tech.ascs.icity.iform.service.DictionaryDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Api(tags = "字典数据表管理",description = "字典数据表管理服务")
@RestController
public class DictionaryDataController implements tech.ascs.icity.iform.api.service.DictionaryDataService {

	// 行政区划分类ID
	@Value("${icity.dictionary.areaCode.id:9fddec6151b14a04bfe21d80d7a4474b}")
	private String areaCodeDictionaryId;

	private Logger log = LoggerFactory.getLogger(DictionaryDataController.class);


	@Autowired
	private DictionaryDataService dictionaryService;
	@Autowired
    private ItemModelService itemModelService;

	@Override
	public SystemCodeModel list() {
		List<DictionaryDataEntity> list = dictionaryService.query().sort(Sort.asc("orderNo")).list();
		DictionaryDataItemEntity rootDictionaryItem = dictionaryService.findRootDictionaryItem();

		for(DictionaryDataEntity dictionaryEntity : list){
			rootDictionaryItem.getChildrenItem().addAll(sortedItem(dictionaryEntity.getDictionaryItems()));
		}
        List<DictionaryDataModel> dictionaryModels = new ArrayList<>();
		for(DictionaryDataEntity dictionaryEntity : list){
            dictionaryModels.add(getByEntity(dictionaryEntity));
        }
		DictionaryDataItemModel dictionaryItemModel = getByEntity(rootDictionaryItem);
		SystemCodeModel systemCodeModel = new SystemCodeModel();
		systemCodeModel.setDictionaryModels(dictionaryModels);
		systemCodeModel.setDictionaryItemModel(dictionaryItemModel);
		return systemCodeModel;
	}

	@Override
	public List<DictionaryDataItemModel> listDictionaryItemModel(@PathVariable(name = "id",required = true) String id) {
		// 查行政区划
		if (areaCodeDictionaryId.equals(id)) {
			return queryAreaCodeOneLevelList(id);
		}
		DictionaryDataEntity dictionaryEntity = dictionaryService.get(id);
		if(dictionaryEntity == null){
			throw new IFormException("未找到【"+id+"】对应的分类");
		}
		List<DictionaryDataItemEntity> list = new ArrayList<>();
		DictionaryDataItemEntity rootDictionaryItem = dictionaryService.findRootDictionaryItem();
		rootDictionaryItem.setChildrenItem(dictionaryEntity.getDictionaryItems());
		list.add(rootDictionaryItem);
		List<DictionaryDataItemModel> itemModels = new ArrayList<>();
		if(list != null) {
			for (DictionaryDataItemEntity itemEntity : list) {
				itemModels.add(getByEntity(itemEntity));
			}
		}
		return itemModels;
	}

	/**
	 * 查询行政区划的一层的所有节点
	 * @param id
	 * @return
	 */
	private List<DictionaryDataItemModel> queryAreaCodeOneLevelList(String id) {
		DictionaryDataItemEntity rootDictionaryItem = dictionaryService.findRootDictionaryItem();
		if (rootDictionaryItem==null) {
			return new ArrayList();
		}
		List<DictionaryDataItemModel> areaCodeList = new ArrayList<>();
		DictionaryDataItemModel root = new DictionaryDataItemModel();
		root.setId(rootDictionaryItem.getId());
		root.setName(rootDictionaryItem.getName());
		root.setCode(rootDictionaryItem.getCode());
		areaCodeList.add(root);

		List<DictionaryDataItemModel> areaCodes = dictionaryService.queryAreaCodeOneLevel(null);
		if (areaCodes!=null) {
			root.setResources(areaCodes);
		}
		return areaCodeList;
	}

	private DictionaryDataModel getByEntity(DictionaryDataEntity dictionaryEntity){
        DictionaryDataModel dictionaryModel = new DictionaryDataModel();
        BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
        return dictionaryModel;
    }

	private DictionaryDataModel getDictionaryModelByEntity(DictionaryDataEntity dictionaryEntity){
		DictionaryDataModel dictionaryModel = new DictionaryDataModel();
		BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
		return dictionaryModel;
	}

	private DictionaryDataItemModel getByEntity(DictionaryDataItemEntity dictionaryItemEntity){
        DictionaryDataItemModel dictionaryItemModel = new DictionaryDataItemModel();
        BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});

        if(dictionaryItemEntity.getDictionary() != null){
			dictionaryItemModel.setDictionaryId(dictionaryItemEntity.getDictionary().getId());
		}

		if(dictionaryItemEntity.getParentItem() != null){
			dictionaryItemModel.setParentId(dictionaryItemEntity.getParentItem().getId());
		}

        if(dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0) {
            List<DictionaryDataItemModel> list = new ArrayList<>();
            for (DictionaryDataItemEntity childDictionaryItemEntity : dictionaryItemEntity.getChildrenItem()) {
                list.add(getByEntity(childDictionaryItemEntity));
            }
            dictionaryItemModel.setResources(list);
        }
        return dictionaryItemModel;
    }

	private DictionaryDataItemModel getItemModelByEntity(DictionaryDataItemEntity dictionaryItemEntity){
		DictionaryDataItemModel dictionaryItemModel = new DictionaryDataItemModel();
		BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});
		if(dictionaryItemEntity.getDictionary() != null){
			dictionaryItemModel.setDictionaryId(dictionaryItemEntity.getDictionary().getId());
		}

		if(dictionaryItemEntity.getParentItem() != null){
			dictionaryItemModel.setParentId(dictionaryItemEntity.getParentItem().getId());
		}
		return dictionaryItemModel;
	}

	private List<DictionaryDataItemEntity> sortedItem(List<DictionaryDataItemEntity> list){
		if(list == null || list.size() < 1){
			return list;
		}
		List<DictionaryDataItemEntity> dictionaryItemEntities = list.size() < 2 ? list : list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
		for(DictionaryDataItemEntity dictionaryItemEntity : dictionaryItemEntities){
			if(dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0){
				dictionaryItemEntity.setChildrenItem(sortedItem(dictionaryItemEntity.getChildrenItem()));
			}
		}
		return dictionaryItemEntities;
	}

	@Override
	public Page<DictionaryDataModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pagesize", defaultValue = "12") int pagesize,
										  @RequestParam(name = "name",required = false) String name) {
		Page<DictionaryDataEntity> pageEntity = null;
		if(StringUtils.isBlank(name)) {
			pageEntity = dictionaryService.query().sort(Sort.asc("orderNo")).page(page, pagesize).page();
		}else{
			pageEntity = dictionaryService.query().filterLike("name", "%" + name + "%").sort(Sort.asc("orderNo")).page(page, pagesize).page();
		}
		List<DictionaryDataModel> dictionaryModels = new ArrayList<>();
		for(DictionaryDataEntity dictionaryEntity : pageEntity.getResults()){
			dictionaryModels.add(getDictionaryModelByEntity(dictionaryEntity));
		}
		Page<DictionaryDataModel> listModels = Page.get(page, pagesize);
		listModels.data(pageEntity.getTotalCount(), dictionaryModels);
		return listModels;
	}

	@Override
	public DictionaryDataModel getByNameAndCode(@RequestParam(name = "name", required = false) String name, @RequestParam(name="code", required = false) String code) {
		if(StringUtils.isBlank(name) && StringUtils.isBlank(code)){
			throw new IFormException("参数不能为空");
		}
		return dictionaryService.getDictionaryByNameAndCode(name, code);
	}

	@Override
    public void add(@RequestBody(required = true) DictionaryDataModel dictionaryModel) {
		veryDictionaryByName(null, dictionaryModel.getName(), dictionaryModel.getCode());
		DictionaryDataEntity dictionary = new DictionaryDataEntity();
    	dictionary.setName(dictionaryModel.getName());
    	dictionary.setDescription(dictionaryModel.getDescription());
		dictionary.setCode(dictionaryModel.getCode());
		dictionary.setOrderNo(dictionaryService.maxDictionaryOrderNo() + 1);
    	dictionaryService.save(dictionary);
    }

    private  void veryDictionaryByName(String id, String name, String code){
		if(StringUtils.isBlank(name)){
			throw new IFormException("数据字典分类名称不能为空");
		}
		List<DictionaryDataEntity> list = null;
		if(StringUtils.isBlank(code)) {
			list = dictionaryService.findByProperty("name", name);
		}else{
			list = dictionaryService.query().filterEqual("code", code).list();
		}
		if(list == null || list.size() < 1){
			return;
		}
		if(StringUtils.isBlank(id)){
			throw new IFormException("数据字典分类名称不能重复");
		}
		for(DictionaryDataEntity dictionaryEntity : list) {
			if (!StringUtils.equals(dictionaryEntity.getId(), id)) {
				throw new IFormException("数据字典分类名称不能重复");
			}
		}

	}

	@Override
    public void update(@PathVariable(name="id") String id, @RequestBody(required = true) DictionaryDataModel dictionaryModel) {
		if(!StringUtils.equals(id, dictionaryModel.getId())){
			throw new IFormException("更新系统分类失败，id不一致");
		}
		veryDictionaryByName(id, dictionaryModel.getName(), dictionaryModel.getCode());
    	DictionaryDataEntity dictionary = dictionaryService.get(id);
    	if(dictionary == null){
			throw new IFormException("未查到对应的系统代码分类");
		}
    	if (StringUtils.isNoneBlank(dictionaryModel.getName())) {
        	dictionary.setName(dictionaryModel.getName());
    	}
		if (StringUtils.isNoneBlank(dictionaryModel.getCode())) {
			dictionary.setCode(dictionaryModel.getCode());
		}
    	if (StringUtils.isNoneBlank(dictionaryModel.getDescription())) {
        	dictionary.setDescription(dictionaryModel.getDescription());
    	}
    	dictionaryService.save(dictionary);
    }

	@Override
    public void delete(@PathVariable(name="id") String id) {
		if (areaCodeDictionaryId.equals(id)) {
			throw new ICityException("行政区划的系统分类不能删除");
		}
		DictionaryDataEntity dictionary = dictionaryService.get(id);
		if(dictionary == null){
			throw new IFormException("未找到【"+id+"】对应的系统代码分类");
		}
    	dictionaryService.delete(dictionary);
    }

	@Override
	public List<DictionaryDataItemModel> listItem(@PathVariable(name="id", required = true) String id) {
		if (areaCodeDictionaryId.equals(id)) {
			return dictionaryService.queryAreaCodeOneLevel(null);
		}
    	DictionaryDataEntity dictionary = dictionaryService.find(id);
    	if(dictionary == null){
			throw new IFormException("未找到【"+id+"】对应的系统代码分类");
		}
		//根节点
		DictionaryDataItemEntity rootDictionaryItem = dictionaryService.findRootDictionaryItem();
		rootDictionaryItem.setChildrenItem(dictionary.getDictionaryItems());
		List<DictionaryDataItemEntity> rootItems = new ArrayList<>();
		rootItems.add(rootDictionaryItem);

		dictionary.setDictionaryItems(sortedItem(rootItems));
		List<DictionaryDataItemModel> list = new ArrayList<>();
		if(dictionary.getDictionaryItems() != null) {
			for (DictionaryDataItemEntity dictionaryItem : dictionary.getDictionaryItems()){
				list.add(getByEntity(dictionaryItem));
			}
		}
		return list;
	}

	@Override
    public void addItem(@RequestBody(required = true) DictionaryDataItemModel dictionaryItemModel ) {
		if (areaCodeDictionaryId.equals(dictionaryItemModel.getDictionaryId())) {
			dictionaryService.addAreaCodeItem(dictionaryItemModel);
			return;
		}
		veryDictionaryItemByCode(dictionaryItemModel);
		DictionaryDataItemEntity parentItemEntity = null;
		if(StringUtils.isNoneBlank(dictionaryItemModel.getParentId())) {
			parentItemEntity = dictionaryService.getDictionaryItemById(dictionaryItemModel.getParentId());
		}
		DictionaryDataEntity dictionary = null;
		if(StringUtils.isNoneBlank(dictionaryItemModel.getDictionaryId())) {
			dictionary = dictionaryService.get(dictionaryItemModel.getDictionaryId());
		}
		if(parentItemEntity == null && dictionary == null){
			throw new IFormException("查询关联对象失败");
		}
		//根节点
		DictionaryDataItemEntity root = dictionaryService.findRootDictionaryItem();
		if(parentItemEntity != null && !root.getId().equals(dictionaryItemModel.getParentId())){
			dictionary = null;
		}
    	DictionaryDataItemEntity item  = new DictionaryDataItemEntity();
    	item.setName(dictionaryItemModel.getName());
    	Integer maxOrderNo = dictionaryService.maxDictionaryItemOrderNo();
		item.setOrderNo(maxOrderNo == null ? 1 :  maxOrderNo + 1);
		item.setCode(StringUtils.isBlank(dictionaryItemModel.getCode()) ? "key_"+item.getOrderNo() : dictionaryItemModel.getCode());
		item.setDescription(dictionaryItemModel.getDescription());
		item.setIcon(dictionaryItemModel.getIcon());
    	if(dictionary != null) {
			item.setParentItem(root);
			item.setDictionary(dictionary);
			root.getChildrenItem().add(item);
			dictionary.getDictionaryItems().add(item);
    		dictionaryService.save(dictionary);
		}else{
			item.setDictionary(null);
			item.setParentItem(parentItemEntity);
			parentItemEntity.getChildrenItem().add(item);
			dictionaryService.saveDictionaryItem(parentItemEntity);
		}
    }

	private  void veryDictionaryItemByCode(DictionaryDataItemModel dictionaryItemModel){
		if(StringUtils.equals(dictionaryItemModel.getCode(),"root") || StringUtils.equals(dictionaryItemModel.getName(),"根节点")){
			throw new IFormException("不允许创建key为root或名称为根节点数据字典");
		}
		 if(StringUtils.isNoneBlank(dictionaryItemModel.getDictionaryId())){
			DictionaryDataEntity dictionaryEntity = dictionaryService.get(dictionaryItemModel.getDictionaryId());
			if(dictionaryEntity == null){
				throw new IFormException("数据字典分类未找到");
			}
			List<DictionaryDataItemEntity> itemEntities =  new ArrayList<>();
			veryDictionaryItem(dictionaryItemModel, getDictionaryItems(itemEntities, dictionaryEntity));
		}
	}

	private List<DictionaryDataItemEntity> getDictionaryItems(List<DictionaryDataItemEntity> list, DictionaryDataEntity dictionaryEntity){
		if(dictionaryEntity != null && dictionaryEntity.getDictionaryItems() != null){
			list.addAll(dictionaryEntity.getDictionaryItems());
			for(DictionaryDataItemEntity dictionaryItem : dictionaryEntity.getDictionaryItems()) {
				getAllChildrenItem(list, dictionaryItem);
			}
		}
		log.error("getDictionaryItems size="+list.size());
		return list;
	}

	private void getAllChildrenItem(List<DictionaryDataItemEntity> list, DictionaryDataItemEntity dictionaryItemEntity){
		if(dictionaryItemEntity != null && dictionaryItemEntity.getChildrenItem() != null){
			list.addAll(dictionaryItemEntity.getChildrenItem());
			for(DictionaryDataItemEntity dictionaryItem : dictionaryItemEntity.getChildrenItem()) {
				getAllChildrenItem(list, dictionaryItem);
			}
		}
	}



	//校验数据字典key
	private void veryDictionaryItem(DictionaryDataItemModel dictionaryItemModel, List<DictionaryDataItemEntity> itemEntities){
		if(itemEntities == null || itemEntities.size() < 1){
			return;
		}
		if(StringUtils.isBlank(dictionaryItemModel.getId()) && itemEntities.parallelStream().map(DictionaryDataItemEntity::getCode).collect(Collectors.toList()).contains(dictionaryItemModel.getCode())){
			throw new IFormException("数据字典key不能重复");
		}
		if(StringUtils.isNoneBlank(dictionaryItemModel.getId())){
			for(DictionaryDataItemEntity entity : itemEntities) {
				if(StringUtils.equals(entity.getCode(), dictionaryItemModel.getCode()) && !StringUtils.equals(entity.getId(), dictionaryItemModel.getId())) {
					throw new IFormException("数据字典key不能重复");
				}
			}
		}
	}

	@Override
    public void updateItem(@PathVariable(name="id", required = true) String id,
						   @RequestBody(required = true) DictionaryDataItemModel dictionaryItemModel) {
		if(!StringUtils.equals(id, dictionaryItemModel.getId())){
			throw new IFormException("更新系统变量失败，id不一致");
		}
		veryDictionaryItemByCode(dictionaryItemModel);
		dictionaryService.updateDictionaryItem(dictionaryItemModel);
    }

	@Override
    public void deleteItem(@PathVariable(name="id") String id) {
    	dictionaryService.deleteDictionaryItem(id);
    }

	@Override
	public void updateItemOrderNo(@PathVariable(name="id",required = true) String id, @PathVariable(name="status", required = true) String status) {
		DictionaryDataItemEntity itemEntity = dictionaryService.getDictionaryItemById(id);
		if(itemEntity == null && itemEntity == null){
			throw new IFormException("查询系统分类代码失败");
		}
		Integer oldOrderNo = itemEntity.getOrderNo();
		List<DictionaryDataItemEntity> list = new ArrayList<>();
		if(itemEntity.getParentItem() == null){
			list = dictionaryService.findDictionaryItems(itemEntity.getDictionary().getId());
		}else{
			list = itemEntity.getParentItem().getChildrenItem();
		}
		if(list == null){
			list = new ArrayList<>();
		}
		List<DictionaryDataItemEntity> dictionaryItemEntities = list.size() < 2 ? list : list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

		for(int i = 0 ; i < dictionaryItemEntities.size(); i++){
			DictionaryDataItemEntity dictionaryItemEntity = dictionaryItemEntities.get(i);
			if(dictionaryItemEntity.getId().equals(id)){
				//上移up
				if("up".equals(status) && i > 0){
					DictionaryDataItemEntity dictionaryItem = dictionaryItemEntities.get(i-1);
					Integer newOrderNo = dictionaryItem.getOrderNo();
					dictionaryItem.setOrderNo(oldOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItem);

					dictionaryItemEntity.setOrderNo(newOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItemEntity);
				}else if("down".equals(status) && i+1 < dictionaryItemEntities.size()){
					//下移
					DictionaryDataItemEntity dictionaryItem = dictionaryItemEntities.get(i+1);
					Integer newOrderNo = dictionaryItem.getOrderNo();
					dictionaryItem.setOrderNo(oldOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItem);

					dictionaryItemEntity.setOrderNo(newOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItemEntity);
				}
			}
		}

	}

	@Override
	public void updateDictionaryOrderNo(@PathVariable(name="id",required = true) String id, @PathVariable(name="status", required = true) String status) {
		DictionaryDataEntity dictionaryEntity = dictionaryService.get(id);
		if(dictionaryEntity == null && dictionaryEntity == null){
			throw new IFormException("未找到【" + id + "】对应的数据字典分类");
		}
		Integer oldOrderNo = dictionaryEntity.getOrderNo();
		List<DictionaryDataEntity> list = dictionaryService.query().sort(Sort.asc("orderNo")).list();
		if(list == null){
			list = new ArrayList<>();
		}
		List<DictionaryDataEntity> dictionaryEntities = list.size() < 2 ? list : list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

		for(int i = 0 ; i < dictionaryEntities.size(); i++){
			DictionaryDataEntity dictionary = dictionaryEntities.get(i);
			if(dictionary.getId().equals(id)){
				//上移up
				if("up".equals(status) && i > 0){
					DictionaryDataEntity dictionaryEntity1 = dictionaryEntities.get(i-1);
					Integer newOrderNo = dictionaryEntity1.getOrderNo();
					dictionaryEntity1.setOrderNo(oldOrderNo);
					dictionaryService.save(dictionaryEntity1);

					dictionary.setOrderNo(newOrderNo);
					dictionaryService.save(dictionary);
				}else if("down".equals(status) && i+1 < dictionaryEntities.size()){
					//下移
					DictionaryDataEntity dictionaryEntity2 = dictionaryEntities.get(i+1);
					Integer newOrderNo = dictionaryEntity2.getOrderNo();
					dictionaryEntity2.setOrderNo(oldOrderNo);
					dictionaryService.save(dictionaryEntity2);

					dictionary.setOrderNo(newOrderNo);
					dictionaryService.save(dictionary);
				}
			}
		}
	}

	@Override
	public List<DictionaryDataItemModel> childrenDictionaryItemModel(@PathVariable(name = "id", required = true) String id) {
		DictionaryDataItemEntity dictionaryItemEntity = dictionaryService.getDictionaryItemById(id);
		if(dictionaryItemEntity == null){
			throw new IFormException("未找到【" + id + "】对应的数据字典项");
		}
		List<DictionaryDataItemModel> list = new ArrayList<>();
		for(DictionaryDataItemEntity dictionaryItem : dictionaryItemEntity.getChildrenItem()){
			list.add(getItemModelByEntity(dictionaryItem));
		}
		return list;
	}

	/** itemModelId 不为空且 linkageDataUnbind为true时，表示查询联动解绑的数据 */
	@Override
	public List<DictionaryDataItemModel> findItems(@PathVariable(name="id", required = true) String id,
												   @PathVariable(name="itemId", required = true) String itemId,
												   @RequestParam(name="itemModelId", required = false) String itemModelId,
												   @RequestParam(name="linkageDataUnbind", defaultValue = "false") Boolean linkageDataUnbind) {
		DictionaryDataEntity dictionaryEntity = dictionaryService.find(id);
		if (dictionaryEntity == null) {
			return new ArrayList<>();
		}
		if (StringUtils.isNotEmpty(itemModelId) && true == linkageDataUnbind) {
			return queryLinkageDataUnbind(itemModelId);
		}
		DictionaryDataItemEntity dictionaryItemEntity = dictionaryService.getDictionaryItemById(itemId);
		if (dictionaryItemEntity == null) {
			return new ArrayList<>();
		}
		List<DictionaryDataItemModel> dictionaryItemModels = new ArrayList<>();
		if(dictionaryItemEntity != null && dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0 ) {
			List<DictionaryDataItemEntity> dictionaryDataItemEntities = dictionaryItemEntity.getChildrenItem().parallelStream().sorted((d2, d1) -> d2.getOrderNo().compareTo(d1.getOrderNo())).collect(Collectors.toList());
			for(DictionaryDataItemEntity itemEntity : dictionaryDataItemEntities) {
				if(("root").equals(dictionaryItemEntity.getCode()) || ("根节点").equals(dictionaryItemEntity.getName())){
					if(itemEntity.getDictionary() == null || !itemEntity.getDictionary().getId().equals(id)) {
						//根节点
						continue;
					}
				}

				DictionaryDataItemModel dictionaryItemModel = new DictionaryDataItemModel();
				tech.ascs.icity.utils.BeanUtils.copyProperties(itemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});
				if(itemEntity.getDictionary() != null) {
					dictionaryItemModel.setDictionaryId(itemEntity.getDictionary().getId());
				}
				dictionaryItemModel.setParentId(itemId);

				dictionaryItemModels.add(dictionaryItemModel);
			}
		}

		return dictionaryItemModels;
	}

	/**
	 * 关联解绑数据,平铺子代的数据
	 * @param itemModelId
	 * @return
	 */
	private List<DictionaryDataItemModel> queryLinkageDataUnbind(String itemModelId) {
        Map<String, Object> map = itemModelService.findLinkageOriginItemModelEntity(itemModelId);
		List<DictionaryDataItemModel> list = new ArrayList<>();
        if (map==null) {
        	return list;
		}
		Integer level = (Integer)map.get("level");
		SelectItemModelEntity selectItemModel = (SelectItemModelEntity)map.get("item");
		String referenceDictionaryItemId = selectItemModel.getReferenceDictionaryItemId();
		List<DictionaryDataItemModel> treeList = listItem(selectItemModel.getReferenceDictionaryId());
		Map<String, Integer> idLevelMap = treeListLevel(treeList, 1);
		Integer referenceItemIdLevel = idLevelMap.get(referenceDictionaryItemId);
		if (referenceItemIdLevel == null) {
			return new ArrayList<>();
		}
		Map<String, DictionaryDataItemModel> itemMap = treeToList(treeList).stream().collect(Collectors.toMap(DictionaryDataItemModel::getId, item->item));
		level = level + referenceItemIdLevel;
		for (String key:idLevelMap.keySet()) {
			if (level == idLevelMap.get(key) && itemMap.get(key)!=null) {
				DictionaryDataItemModel dictionaryDataItemModel = itemMap.get(key);
				dictionaryDataItemModel.setResources(new ArrayList<>());
				list.add(dictionaryDataItemModel);
			}
		}
		return list;
	}

	private List<DictionaryDataItemModel> treeToList(List<DictionaryDataItemModel> treeList) {
		List<DictionaryDataItemModel> list = new ArrayList();
		if (treeList!=null && treeList.size()>0) {
			for (DictionaryDataItemModel item : treeList) {
				list.add(item);
				list.addAll(treeToList(item.getResources()));
			}
		}
		return list;
	}

	/** 记录每个item的level等级，即排在哪一层 */
	private Map<String, Integer> treeListLevel(List<DictionaryDataItemModel> treeList, int level) {
		Map<String, Integer> map = new HashMap<>();
		if (treeList!=null && treeList.size()>0) {
			for (DictionaryDataItemModel item:treeList) {
				map.put(item.getId(), level);
				if (item.getResources()!=null && item.getResources().size()>0) {
					map.putAll(treeListLevel(item.getResources(), level+1));
				}
			}
		}
		return map;
	}

	@Override
	public List<DictionaryDataItemModel> batchSimpleInfo(@RequestParam(name = "ids", required = false) String[] ids) {
		if (ids==null || ids.length==0) {
			return new ArrayList<>();
		}
		List<DictionaryDataItemEntity> list = dictionaryService.findByItemIds(ids);
		List<DictionaryDataItemModel> returnList = new ArrayList<>();
		for (DictionaryDataItemEntity item:list) {
			DictionaryDataItemModel returnItem = new DictionaryDataItemModel();
			BeanUtils.copyProperties(item, returnItem, new String[]{"dictionary"});
			returnList.add(returnItem);
		}
		return returnList;
	}

}
