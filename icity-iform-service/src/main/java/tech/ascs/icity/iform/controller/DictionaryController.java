package tech.ascs.icity.iform.controller;

import com.googlecode.genericdao.search.Sort;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tech.ascs.icity.model.Page;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DictionaryItemModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.iform.service.DictionaryService;
import tech.ascs.icity.jpa.tools.DTOTools;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Api(tags = "字典表管理",description = "字典表管理服务")
@RestController
public class DictionaryController implements tech.ascs.icity.iform.api.service.DictionaryService {

	@Autowired
	private DictionaryService dictionaryService;

	@Override
	public List<DictionaryModel> list() {
		List<DictionaryEntity> list = dictionaryService.query().sort(Sort.asc("orderNo")).list();
		for(DictionaryEntity dictionaryEntity : list){
			dictionaryEntity.setDictionaryItems(sortedItem(dictionaryEntity.getDictionaryItems()));
		}
        List<DictionaryModel> dictionaryModels = new ArrayList<>();
		for(DictionaryEntity dictionaryEntity : list){
            dictionaryModels.add(getByEntity(dictionaryEntity));
        }
		return dictionaryModels;
	}

	@Override
	public List<DictionaryItemModel> listDictionaryItemMode() {
		List<DictionaryItemEntity> list = dictionaryService.findAllDictionaryItems();
		List<DictionaryItemModel> itemModels = new ArrayList<>();
		if(list != null) {
			for (DictionaryItemEntity itemEntity : list) {
				itemModels.add(getItemModelByEntity(itemEntity));
			}
		}
		return itemModels;
	}


	private DictionaryModel getByEntity(DictionaryEntity dictionaryEntity){
        DictionaryModel dictionaryModel = new DictionaryModel();
        BeanUtils.copyProperties(dictionaryEntity, dictionaryModel, new String[]{"dictionaryItems"});
        if(dictionaryEntity.getDictionaryItems() != null){
            List<DictionaryItemModel> itemModelList = new ArrayList<>();
            for(DictionaryItemEntity entity : dictionaryEntity.getDictionaryItems()){
                itemModelList.add(getByEntity(entity));
            }
            dictionaryModel.setDictionaryItems(itemModelList);
        }
        return dictionaryModel;
    }

	private DictionaryItemModel getByEntity(DictionaryItemEntity dictionaryItemEntity){
        DictionaryItemModel dictionaryItemModel = new DictionaryItemModel();
        BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});
        if(dictionaryItemEntity.getChildrenItem() != null) {
            List<DictionaryItemModel> list = new ArrayList<>();
            for (DictionaryItemEntity childDictionaryItemEntity : dictionaryItemEntity.getChildrenItem()) {
                list.add(getByEntity(childDictionaryItemEntity));
            }
            dictionaryItemModel.setChildrenItem(list);
        }
        return dictionaryItemModel;
    }

	private DictionaryItemModel getItemModelByEntity(DictionaryItemEntity dictionaryItemEntity){
		DictionaryItemModel dictionaryItemModel = new DictionaryItemModel();
		BeanUtils.copyProperties(dictionaryItemEntity, dictionaryItemModel, new String[]{"dictionary", "paraentItem", "childrenItem"});
		return dictionaryItemModel;
	}

	private List<DictionaryItemEntity> sortedItem(List<DictionaryItemEntity> list){
		if(list == null || list.size() < 1){
			return null;
		}
		List<DictionaryItemEntity> dictionaryItemEntities = list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());
		for(DictionaryItemEntity dictionaryItemEntity : dictionaryItemEntities){
			if(dictionaryItemEntity.getChildrenItem() != null && dictionaryItemEntity.getChildrenItem().size() > 0){
				dictionaryItemEntity.setChildrenItem(sortedItem(dictionaryItemEntity.getChildrenItem()));
			}
		}
		return dictionaryItemEntities;
	}

	@Override
	public Page<DictionaryModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pageSize", defaultValue = "10") int pageSize) {
		Page<DictionaryEntity> pageEntity = dictionaryService.query().sort(Sort.asc("orderNo")).page(page, pageSize).page();
		List<DictionaryEntity> dictionaryModels = new ArrayList<>();
		for(DictionaryEntity dictionaryEntity : pageEntity.getResults()){
			dictionaryEntity.setDictionaryItems(sortedItem(dictionaryEntity.getDictionaryItems()));
			dictionaryModels.add(dictionaryEntity);
		}
		pageEntity.setContent(dictionaryModels);
		return DTOTools.wrapPage(pageEntity, DictionaryModel.class);
	}

	@Override
    public void add(@RequestParam(name = "name") String name, @RequestParam(name = "description", required = false) String description) {
		veryDictionaryByName(null, name);
		DictionaryEntity dictionary = new DictionaryEntity();
    	dictionary.setName(name);
    	dictionary.setDescription(description);
		dictionary.setOrderNo(dictionaryService.maxDictionaryOrderNo() + 1);
    	dictionaryService.save(dictionary);
    }

    private  void veryDictionaryByName(String id, String name){
		List<DictionaryEntity> list = dictionaryService.findByProperty("name", name);
		if(StringUtils.isBlank(id) && list != null && list.size() > 0){
			throw new IFormException("数据字典名称不能重复");
		}
		if(StringUtils.isNoneBlank(id) ){
			if(list == null || list.size() > 1) {
				throw new IFormException("数据字典名称不能重复");
			}
			for(DictionaryEntity entity : list){
				if(!entity.getId().equals(id)){
					throw new IFormException("数据字典名称不能重复");
				}
			}
		}
	}

	@Override
    public void update(@PathVariable(name="id") String id, @RequestParam(name="name", required=false) String name, @RequestParam(name="description", required=false) String description) {
		veryDictionaryByName(id, name);
    	DictionaryEntity dictionary = dictionaryService.get(id);
    	if(dictionary == null){
			throw new IFormException("未查到对应的系统代码分类");
		}
    	if (name != null) {
        	dictionary.setName(name);
    	}
    	if (description != null) {
        	dictionary.setDescription(description);
    	}
    	dictionaryService.save(dictionary);
    }

	@Override
    public void delete(@PathVariable(name="id") String id) {
    	DictionaryEntity dictionary = dictionaryService.get(id);
    	dictionaryService.delete(dictionary);
    }

	@Override
	public List<DictionaryItemModel> listItem(@PathVariable(name="id") String id) {
    	DictionaryEntity dictionary = dictionaryService.get(id);
		dictionary.setDictionaryItems(sortedItem(dictionary.getDictionaryItems()));
		List<DictionaryItemModel> list = DTOTools.wrapList(dictionary.getDictionaryItems(), DictionaryItemModel.class);
		return list;
	}

	@Override
    public void addItem(@RequestParam(name="id") String id,
						@RequestParam(name="name") String name,
						@RequestParam(name="code") String code,
						@RequestParam(name="description", required = false) String description, @RequestParam(name="parentItemId", required = false) String parentItemId) {
		DictionaryItemEntity parentItemEntity = dictionaryService.getDictionaryItemById(parentItemId);
		DictionaryEntity dictionary = null;
		if(StringUtils.isNoneBlank(id)) {
			dictionary = dictionaryService.get(id);
		}
		if(parentItemEntity == null && dictionary == null){
			throw new IFormException("查询关联对象失败");
		}
    	DictionaryItemEntity item  = new DictionaryItemEntity();
    	item.setName(name);
    	item.setCode(code);
    	Integer maxOrderNo = dictionaryService.maxDictionaryItemOrderNo();
		item.setOrderNo(maxOrderNo == null ? 1 :  maxOrderNo + 1);
    	item.setDescription(description);
    	if(dictionary != null) {
			item.setDictionary(dictionary);
			dictionary.getDictionaryItems().add(item);
    		dictionaryService.save(dictionary);
		}else{
			item.setParentItem(parentItemEntity);
			parentItemEntity.getChildrenItem().add(item);
			dictionaryService.saveDictionaryItem(parentItemEntity);
		}
    }

	@Override
    public void updateItem(@RequestParam(name="id") String id, @PathVariable(name="itemId", required = true) String itemId,
						   @RequestParam(name="name", required=false) String name,
						   @RequestParam(name="code", required=false) String code,
						   @RequestParam(name="description", required=false) String description, @RequestParam(name="parentItemId", required = false) String parentItemId) {
		DictionaryItemEntity itemEntity = dictionaryService.getDictionaryItemById(parentItemId);
		DictionaryEntity dictionary = null;
		if(StringUtils.isNoneBlank(id)) {
			dictionary = dictionaryService.get(id);
		}
		if(itemEntity == null && dictionary == null){
			throw new IFormException("查询关联对象失败");
		}
    	dictionaryService.updateDictionaryItem(id, itemId, code, name, description, parentItemId);
    }

	@Override
    public void deleteItem(@PathVariable(name="id") String id, @PathVariable(name="itemId") String itemId) {
    	dictionaryService.deleteDictionaryItem(id, itemId);
    }

	@Override
	public void updateItemOrderNo(@PathVariable(name="itemId",required = true) String itemId, @RequestParam(name="orderNo", defaultValue = "0") int orderNo) {
		DictionaryItemEntity itemEntity = dictionaryService.getDictionaryItemById(itemId);
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
		List<DictionaryItemEntity> dictionaryItemEntities = list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

		for(int i = 0 ; i < dictionaryItemEntities.size(); i++){
			DictionaryItemEntity dictionaryItemEntity = dictionaryItemEntities.get(i);
			if(dictionaryItemEntity.getId().equals(itemId)){
				//上移-1
				if(orderNo < 0 && i > 0){
					DictionaryItemEntity dictionaryItem = dictionaryItemEntities.get(i-1);
					Integer newOrderNo = dictionaryItem.getOrderNo();
					dictionaryItem.setOrderNo(oldOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItem);

					dictionaryItemEntity.setOrderNo(newOrderNo);
					dictionaryService.saveDictionaryItem(dictionaryItemEntity);
				}else if(orderNo > 0 && i+1 < dictionaryItemEntities.size()){
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
	public void updateDictionaryOrderNo(@PathVariable(name="id",required = true) String id, @RequestParam(name="orderNo", defaultValue = "0") int orderNo) {
		DictionaryEntity dictionaryEntity = dictionaryService.get(id);
		if(dictionaryEntity == null && dictionaryEntity == null){
			throw new IFormException("查询系统分类失败");
		}
		Integer oldOrderNo = dictionaryEntity.getOrderNo();
		List<DictionaryEntity> list = dictionaryService.query().sort(Sort.asc("orderNo")).list();

		List<DictionaryEntity> dictionaryEntities = list.parallelStream().sorted((d1, d2) -> d1.getOrderNo().compareTo(d2.getOrderNo())).collect(Collectors.toList());

		for(int i = 0 ; i < dictionaryEntities.size(); i++){
			DictionaryEntity dictionary = dictionaryEntities.get(i);
			if(dictionary.getId().equals(id)){
				//上移-1
				if(orderNo < 0 && i > 0){
					DictionaryEntity dictionaryEntity1 = dictionaryEntities.get(i-1);
					Integer newOrderNo = dictionaryEntity1.getOrderNo();
					dictionaryEntity1.setOrderNo(oldOrderNo);
					dictionaryService.save(dictionaryEntity1);

					dictionary.setOrderNo(newOrderNo);
					dictionaryService.save(dictionary);
				}else if(orderNo > 0 && i+1 < dictionaryEntities.size()){
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
}
