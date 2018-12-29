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
import tech.ascs.icity.iform.api.model.DictionaryItemModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.iform.service.DictionaryService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Api(tags = "字典表管理",description = "字典表管理服务")
@RestController
public class DictionaryController implements tech.ascs.icity.iform.api.service.DictionaryService {

	private Logger log = LoggerFactory.getLogger(DictionaryController.class);


	@Autowired
	private DictionaryService dictionaryService;

	@Override
	public SystemCodeModel list() {
		List<DictionaryEntity> list = dictionaryService.query().sort(Sort.asc("orderNo")).list();
		DictionaryItemEntity  rootDictionaryItem = dictionaryService.findRootDictionaryItem();

		for(DictionaryEntity dictionaryEntity : list){
			rootDictionaryItem.getChildrenItem().addAll(sortedItem(dictionaryEntity.getDictionaryItems()));
		}
        List<DictionaryModel> dictionaryModels = new ArrayList<>();
		for(DictionaryEntity dictionaryEntity : list){
            dictionaryModels.add(getByEntity(dictionaryEntity));
        }
		DictionaryItemModel dictionaryItemModel = getByEntity(rootDictionaryItem);
		SystemCodeModel systemCodeModel = new SystemCodeModel();
		systemCodeModel.setDictionaryModels(dictionaryModels);
		systemCodeModel.setDictionaryItemModel(dictionaryItemModel);
		return systemCodeModel;
	}

	@Override
	public List<DictionaryItemModel> listDictionaryItemModel(@PathVariable(name = "id",required = true) String id) {
		DictionaryEntity dictionaryEntity = dictionaryService.get(id);
		if(dictionaryEntity == null){
			throw new IFormException("未找到【"+id+"】对应的分类");
		}
		List<DictionaryItemEntity> list = new ArrayList<>();
		DictionaryItemEntity rootDictionaryItem = dictionaryService.findRootDictionaryItem();
		rootDictionaryItem.setChildrenItem(dictionaryEntity.getDictionaryItems());
		list.add(rootDictionaryItem);
		List<DictionaryItemModel> itemModels = new ArrayList<>();
		if(list != null) {
			for (DictionaryItemEntity itemEntity : list) {
				itemModels.add(getByEntity(itemEntity));
			}
		}
		return itemModels;
	}


	private DictionaryModel getByEntity(DictionaryEntity dictionaryEntity){
        DictionaryModel dictionaryModel = new DictionaryModel();
        BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
        return dictionaryModel;
    }

	private DictionaryModel getDictionaryModelByEntity(DictionaryEntity dictionaryEntity){
		DictionaryModel dictionaryModel = new DictionaryModel();
		BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
		return dictionaryModel;
	}

	private DictionaryItemModel getByEntity(DictionaryItemEntity dictionaryItemEntity){
        DictionaryItemModel dictionaryItemModel = new DictionaryItemModel();
        BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});

        if(dictionaryItemEntity.getDictionary() != null){
			dictionaryItemModel.setDictionaryId(dictionaryItemEntity.getDictionary().getId());
		}

		if(dictionaryItemEntity.getParentItem() != null){
			dictionaryItemModel.setParentId(dictionaryItemEntity.getParentItem().getId());
		}

        if(dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0) {
            List<DictionaryItemModel> list = new ArrayList<>();
            for (DictionaryItemEntity childDictionaryItemEntity : dictionaryItemEntity.getChildrenItem()) {
                list.add(getByEntity(childDictionaryItemEntity));
            }
            dictionaryItemModel.setResources(list);
        }
        return dictionaryItemModel;
    }

	private DictionaryItemModel getItemModelByEntity(DictionaryItemEntity dictionaryItemEntity){
		DictionaryItemModel dictionaryItemModel = new DictionaryItemModel();
		BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});
		if(dictionaryItemEntity.getDictionary() != null){
			dictionaryItemModel.setDictionaryId(dictionaryItemEntity.getDictionary().getId());
		}

		if(dictionaryItemEntity.getParentItem() != null){
			dictionaryItemModel.setParentId(dictionaryItemEntity.getParentItem().getId());
		}
		return dictionaryItemModel;
	}

	private List<DictionaryItemEntity> sortedItem(List<DictionaryItemEntity> list){
		if(list == null || list.size() < 1){
			return list;
		}
		List<DictionaryItemEntity> dictionaryItemEntities = list.size() < 2 ? list : list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
		for(DictionaryItemEntity dictionaryItemEntity : dictionaryItemEntities){
			if(dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0){
				dictionaryItemEntity.setChildrenItem(sortedItem(dictionaryItemEntity.getChildrenItem()));
			}
		}
		return dictionaryItemEntities;
	}

	@Override
	public Page<DictionaryModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pageSize", defaultValue = "12") int pageSize) {
		Page<DictionaryEntity> pageEntity = dictionaryService.query().sort(Sort.asc("orderNo")).page(page, pageSize).page();
		List<DictionaryModel> dictionaryModels = new ArrayList<>();
		for(DictionaryEntity dictionaryEntity : pageEntity.getResults()){
			dictionaryModels.add(getDictionaryModelByEntity(dictionaryEntity));
		}
		Page<DictionaryModel> listModels = Page.get(page, pageSize);
		listModels.data(pageEntity.getTotalCount(), dictionaryModels);
		return listModels;
	}

	@Override
    public void add(@RequestBody(required = true) DictionaryModel dictionaryModel) {
		veryDictionaryByName(null, dictionaryModel.getName());
		DictionaryEntity dictionary = new DictionaryEntity();
    	dictionary.setName(dictionaryModel.getName());
    	dictionary.setDescription(dictionaryModel.getDescription());
		dictionary.setOrderNo(dictionaryService.maxDictionaryOrderNo() + 1);
    	dictionaryService.save(dictionary);
    }

    private  void veryDictionaryByName(String id, String name){
		List<DictionaryEntity> list = dictionaryService.findByProperty("name", name);
		if(list == null || list.size() < 1){
			return;
		}
		if(StringUtils.isBlank(id)){
			throw new IFormException("数据字典分类名称不能重复");
		}
		if(StringUtils.isNoneBlank(id) ){
			for(DictionaryEntity dictionaryEntity : list) {
				if (StringUtils.equals(dictionaryEntity.getName(), name) && !StringUtils.equals(dictionaryEntity.getId(), id)) {
					throw new IFormException("数据字典分类名称不能重复");
				}
			}
		}
	}

	@Override
    public void update(@PathVariable(name="id") String id, @RequestBody(required = true) DictionaryModel dictionaryModel) {
		if(!StringUtils.equals(id, dictionaryModel.getId())){
			throw new IFormException("更新系统分类失败，id不一致");
		}
		veryDictionaryByName(id, dictionaryModel.getName());
    	DictionaryEntity dictionary = dictionaryService.get(id);
    	if(dictionary == null){
			throw new IFormException("未查到对应的系统代码分类");
		}
    	if (StringUtils.isNoneBlank(dictionaryModel.getName())) {
        	dictionary.setName(dictionaryModel.getName());
    	}
    	if (StringUtils.isNoneBlank(dictionaryModel.getDescription())) {
        	dictionary.setDescription(dictionaryModel.getDescription());
    	}
    	dictionaryService.save(dictionary);
    }

	@Override
    public void delete(@PathVariable(name="id") String id) {
    	DictionaryEntity dictionary = dictionaryService.get(id);
		if(dictionary == null){
			throw new IFormException("未找到【"+id+"】对应的系统代码分类");
		}
    	dictionaryService.delete(dictionary);
    }

	@Override
	public List<DictionaryItemModel> listItem(@PathVariable(name="id", required = true) String id) {
		log.error("listItem with id="+id +"begin");
    	DictionaryEntity dictionary = dictionaryService.get(id);
    	if(dictionary == null){
			throw new IFormException("未找到【"+id+"】对应的系统代码分类");
		}
		//根节点
		DictionaryItemEntity rootDictionaryItem = dictionaryService.findRootDictionaryItem();
		rootDictionaryItem.setChildrenItem(dictionary.getDictionaryItems());
		List<DictionaryItemEntity> rootItems = new ArrayList<>();
		rootItems.add(rootDictionaryItem);

		dictionary.setDictionaryItems(sortedItem(rootItems));
		List<DictionaryItemModel> list = new ArrayList<>();
		if(dictionary.getDictionaryItems() != null) {
			for (DictionaryItemEntity dictionaryItem : dictionary.getDictionaryItems()){
				list.add(getByEntity(dictionaryItem));
			}
		}
		log.error("listItem with id="+id +"end");
		return list;
	}

	@Override
    public void addItem(@RequestBody(required = true) DictionaryItemModel dictionaryItemModel ) {
		veryDictionaryItemByCode(dictionaryItemModel);
		DictionaryItemEntity parentItemEntity = null;
		if(StringUtils.isNoneBlank(dictionaryItemModel.getParentId())) {
			parentItemEntity = dictionaryService.getDictionaryItemById(dictionaryItemModel.getParentId());
		}
		DictionaryEntity dictionary = null;
		if(StringUtils.isNoneBlank(dictionaryItemModel.getDictionaryId())) {
			dictionary = dictionaryService.get(dictionaryItemModel.getDictionaryId());
		}
		if(parentItemEntity == null && dictionary == null){
			throw new IFormException("查询关联对象失败");
		}
		//根节点
		DictionaryItemEntity root = dictionaryService.findRootDictionaryItem();
		if(parentItemEntity != null && !root.getId().equals(dictionaryItemModel.getParentId())){
			dictionary = null;
		}
    	DictionaryItemEntity item  = new DictionaryItemEntity();
    	item.setName(dictionaryItemModel.getName());
    	Integer maxOrderNo = dictionaryService.maxDictionaryItemOrderNo();
		item.setOrderNo(maxOrderNo == null ? 1 :  maxOrderNo + 1);

		item.setCode(StringUtils.isBlank(dictionaryItemModel.getCode()) ? "key_"+item.getOrderNo() : dictionaryItemModel.getCode());

		item.setDescription(dictionaryItemModel.getDescription());
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

	private  void veryDictionaryItemByCode(DictionaryItemModel dictionaryItemModel){
		if(StringUtils.equals(dictionaryItemModel.getCode(),"root")){
			throw new IFormException("不允许创建key等于root的节点");
		}
		 if(StringUtils.isNoneBlank(dictionaryItemModel.getDictionaryId())){
			DictionaryEntity dictionaryEntity = dictionaryService.get(dictionaryItemModel.getDictionaryId());
			if(dictionaryEntity == null){
				throw new IFormException("数据字典分类未找到");
			}
			List<DictionaryItemEntity> itemEntities =  new ArrayList<>();
			veryDictionaryItem(dictionaryItemModel, getDictionaryItems(itemEntities, dictionaryEntity));
		}
	}

	private List<DictionaryItemEntity> getDictionaryItems(List<DictionaryItemEntity> list, DictionaryEntity dictionaryEntity){
		if(dictionaryEntity != null && dictionaryEntity.getDictionaryItems() != null){
			list.addAll(dictionaryEntity.getDictionaryItems());
			for(DictionaryItemEntity  dictionaryItem : dictionaryEntity.getDictionaryItems()) {
				getAllChildrenItem(list, dictionaryItem);
			}
		}
		log.error("getDictionaryItems size="+list.size());
		return list;
	}

	private void getAllChildrenItem(List<DictionaryItemEntity> list, DictionaryItemEntity dictionaryItemEntity){
		if(dictionaryItemEntity != null && dictionaryItemEntity.getChildrenItem() != null){
			list.addAll(dictionaryItemEntity.getChildrenItem());
			for(DictionaryItemEntity  dictionaryItem : dictionaryItemEntity.getChildrenItem()) {
				getAllChildrenItem(list, dictionaryItem);
			}
		}
	}



	//校验数据字典key
	private void veryDictionaryItem(DictionaryItemModel dictionaryItemModel, List<DictionaryItemEntity> itemEntities){
		if(itemEntities == null || itemEntities.size() < 1){
			return;
		}
		if(StringUtils.isBlank(dictionaryItemModel.getId()) && itemEntities.parallelStream().map(DictionaryItemEntity::getCode).collect(Collectors.toList()).contains(dictionaryItemModel.getCode())){
			throw new IFormException("数据字典key不能重复");
		}
		if(StringUtils.isNoneBlank(dictionaryItemModel.getId())){
			for(DictionaryItemEntity entity : itemEntities) {
				if(StringUtils.equals(entity.getCode(), dictionaryItemModel.getCode()) && !StringUtils.equals(entity.getId(), dictionaryItemModel.getId())) {
					throw new IFormException("数据字典key不能重复");
				}
			}
		}
	}

	@Override
    public void updateItem(@PathVariable(name="id", required = true) String id,
						   @RequestBody(required = true) DictionaryItemModel dictionaryItemModel) {
		if(!StringUtils.equals(id, dictionaryItemModel.getId())){
			throw new IFormException("更新系统变量失败，id不一致");
		}
		veryDictionaryItemByCode(dictionaryItemModel);
		dictionaryService.updateDictionaryItem(dictionaryItemModel.getDictionaryId(), id, dictionaryItemModel.getCode(), dictionaryItemModel.getName(), dictionaryItemModel.getDescription(), dictionaryItemModel.getParentId());
    }

	@Override
    public void deleteItem(@PathVariable(name="id") String id) {
    	dictionaryService.deleteDictionaryItem(id);
    }

	@Override
	public void updateItemOrderNo(@PathVariable(name="id",required = true) String id, @PathVariable(name="status", required = true) String status) {
		DictionaryItemEntity itemEntity = dictionaryService.getDictionaryItemById(id);
		if(itemEntity == null && itemEntity == null){
			throw new IFormException("查询系统分类代码失败");
		}
		Integer oldOrderNo = itemEntity.getOrderNo();
		List<DictionaryItemEntity> list = new ArrayList<>();
		if(itemEntity.getParentItem() == null){
			list = dictionaryService.findDictionaryItems(itemEntity.getDictionary().getId());
		}else{
			list = itemEntity.getParentItem().getChildrenItem();
		}
		if(list == null){
			list = new ArrayList<>();
		}
		List<DictionaryItemEntity> dictionaryItemEntities = list.size() < 2 ? list : list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

		for(int i = 0 ; i < dictionaryItemEntities.size(); i++){
			DictionaryItemEntity dictionaryItemEntity = dictionaryItemEntities.get(i);
			if(dictionaryItemEntity.getId().equals(id)){
				//上移up
				if("up".equals(status) && i > 0){
					DictionaryItemEntity dictionaryItem = dictionaryItemEntities.get(i-1);
					Integer newOrderNo = dictionaryItem.getOrderNo();
					dictionaryItem.setOrderNo(oldOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItem);

					dictionaryItemEntity.setOrderNo(newOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItemEntity);
				}else if("down".equals(status) && i+1 < dictionaryItemEntities.size()){
					//下移
					DictionaryItemEntity dictionaryItem = dictionaryItemEntities.get(i+1);
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
		DictionaryEntity dictionaryEntity = dictionaryService.get(id);
		if(dictionaryEntity == null && dictionaryEntity == null){
			throw new IFormException("未找到【" + id + "】对应的数据字典分类");
		}
		Integer oldOrderNo = dictionaryEntity.getOrderNo();
		List<DictionaryEntity> list = dictionaryService.query().sort(Sort.asc("orderNo")).list();
		if(list == null){
			list = new ArrayList<>();
		}
		List<DictionaryEntity> dictionaryEntities = list.size() < 2 ? list : list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

		for(int i = 0 ; i < dictionaryEntities.size(); i++){
			DictionaryEntity dictionary = dictionaryEntities.get(i);
			if(dictionary.getId().equals(id)){
				//上移up
				if("up".equals(status) && i > 0){
					DictionaryEntity dictionaryEntity1 = dictionaryEntities.get(i-1);
					Integer newOrderNo = dictionaryEntity1.getOrderNo();
					dictionaryEntity1.setOrderNo(oldOrderNo);
					dictionaryService.save(dictionaryEntity1);

					dictionary.setOrderNo(newOrderNo);
					dictionaryService.save(dictionary);
				}else if("down".equals(status) && i+1 < dictionaryEntities.size()){
					//下移
					DictionaryEntity dictionaryEntity2 = dictionaryEntities.get(i+1);
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
	public List<DictionaryItemModel> childrenDictionaryItemModel(@PathVariable(name = "id", required = true) String id) {
		DictionaryItemEntity dictionaryItemEntity = dictionaryService.getDictionaryItemById(id);
		if(dictionaryItemEntity == null){
			throw new IFormException("未找到【" + id + "】对应的数据字典项");
		}
		List<DictionaryItemModel> list = new ArrayList<>();
		for(DictionaryItemEntity dictionaryItem : dictionaryItemEntity.getChildrenItem()){
			list.add(getItemModelByEntity(dictionaryItem));
		}
		return list;
	}
}
