package tech.ascs.icity.iform.controller;

import com.googlecode.genericdao.search.Sort;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.api.model.SystemCodeModel;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DictionaryDataItemModel;
import tech.ascs.icity.iform.api.model.DictionaryDataModel;
import tech.ascs.icity.iform.model.DictionaryDataEntity;
import tech.ascs.icity.iform.model.DictionaryDataItemEntity;
import tech.ascs.icity.iform.service.DictionaryDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Api(tags = "字典数据表管理",description = "字典数据表管理服务")
@RestController
public class DictionaryDataController implements tech.ascs.icity.iform.api.service.DictionaryDataService {

	private Logger log = LoggerFactory.getLogger(DictionaryDataController.class);


	@Autowired
	private DictionaryDataService dictionaryService;

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
    	DictionaryDataEntity dictionary = dictionaryService.get(id);
		if(dictionary == null){
			throw new IFormException("未找到【"+id+"】对应的系统代码分类");
		}
    	dictionaryService.delete(dictionary);
    }

	@Override
	public List<DictionaryDataItemModel> listItem(@PathVariable(name="id", required = true) String id) {
		log.error("listItem with id="+id +"begin");
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
		log.error("listItem with id="+id +"end");
		return list;
	}

	@Override
    public void addItem(@RequestBody(required = true) DictionaryDataItemModel dictionaryItemModel ) {
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
		if(StringUtils.equals(dictionaryItemModel.getCode(),"root")){
			throw new IFormException("不允许创建key等于root的节点");
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

	@Override
	public List<DictionaryDataItemModel> findItems(@PathVariable(name="id",required = true) String id, @PathVariable(name="itemId",required = true) String itemId) {
		DictionaryDataEntity dictionaryEntity = dictionaryService.find(id);
		DictionaryDataItemEntity dictionaryItemEntity = dictionaryService.getDictionaryItemById(itemId);
		if(dictionaryEntity == null || dictionaryItemEntity == null) {
			return new ArrayList<>();
		}
		List<DictionaryDataItemModel> dictionaryItemModels = new ArrayList<>();
		if(dictionaryItemEntity != null && dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0 ) {
			for(DictionaryDataItemEntity itemEntity : dictionaryItemEntity.getChildrenItem()) {
				if(itemEntity.getParentItem() != null && ("root").equals(itemEntity.getParentItem().getCode())){
					//根节点
					if(itemEntity.getDictionary() == null || (itemEntity.getDictionary() != null && !itemEntity.getDictionary().getId().equals(id))){
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