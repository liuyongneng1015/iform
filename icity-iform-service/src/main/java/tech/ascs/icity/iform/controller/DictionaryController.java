package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.DictionaryItemModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.model.DictionaryEntity;
import tech.ascs.icity.iform.model.DictionaryItemEntity;
import tech.ascs.icity.iform.service.DictionaryService;
import tech.ascs.icity.jpa.tools.DTOTools;
import tech.ascs.icity.model.Page;

import javax.transaction.Transactional;

@RestController
@Api(tags = "字典表管理服务",description = "字典表管理")
public class DictionaryController implements tech.ascs.icity.iform.api.service.DictionaryService {

	@Autowired
	private DictionaryService dictionaryService;

	@Override
	public List<DictionaryModel> list() {
		List<DictionaryEntity> list = dictionaryService.query().list();
		for(DictionaryEntity dictionaryEntity : list){
			dictionaryEntity.setDictionaryItems(sortedItem(dictionaryEntity.getDictionaryItems()));
		}
		return DTOTools.wrapList(list, DictionaryModel.class);
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
	public Page<DictionaryModel> page(int page,int pageSize) {
		return DTOTools.wrapPage(dictionaryService.query().page(page, pageSize).page(), DictionaryModel.class);
	}

	@Override
    public void add(String name, String description) {
		veryDictionaryByName(null, name);
		DictionaryEntity dictionary = new DictionaryEntity();
    	dictionary.setName(name);
    	dictionary.setDescription(description);
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
    public void update(String id, String name, String description) {
		veryDictionaryByName(id, name);
    	DictionaryEntity dictionary = dictionaryService.get(id);
    	if (name != null) {
        	dictionary.setName(name);
    	}
    	if (description != null) {
        	dictionary.setDescription(description);
    	}
    	dictionaryService.save(dictionary);
    }

	@Override
    public void delete(String id) {
    	DictionaryEntity dictionary = dictionaryService.get(id);
    	dictionaryService.delete(dictionary);
    }

	@Override
	public List<DictionaryItemModel> listItem(String id) {
    	DictionaryEntity dictionary = dictionaryService.get(id);
		return DTOTools.wrapList(dictionary.getDictionaryItems(), DictionaryItemModel.class);
	}

	@Override
    public void addItem(String id, String name, String code, String description, String parentItemId) {
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
		item.setOrderNo(dictionaryService.maxOrderNo() + 1);
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
    public void updateItem(String id, String itemId, String name, String code, String description, String parentItemId) {
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
    public void deleteItem(String id, String itemId) {
    	dictionaryService.deleteDictionaryItem(id, itemId);
    }

	@Override
	@Transactional
	public void updateItemOrderNo(String itemId, int number) {
		DictionaryItemEntity itemEntity = dictionaryService.getDictionaryItemById(itemId);
		if(itemEntity == null && itemEntity == null){
			throw new IFormException("查询关联对象失败");
		}
		Integer orderNo = itemEntity.getOrderNo();
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
				if(number < 0 && i > 0){
					DictionaryItemEntity dictionaryItem = dictionaryItemEntities.get(i-1);
					dictionaryItem.setOrderNo(orderNo);
					dictionaryService.saveDictionaryItem(dictionaryItem);

					dictionaryItemEntity.setOrderNo(dictionaryItem.getOrderNo());
					dictionaryService.saveDictionaryItem(dictionaryItemEntity);
				}else if(number > 0 && i+1 < dictionaryItemEntities.size()){
					//下移
					DictionaryItemEntity dictionaryItem = dictionaryItemEntities.get(i+1);
					dictionaryItem.setOrderNo(orderNo);
					dictionaryService.saveDictionaryItem(dictionaryItem);

					dictionaryItemEntity.setOrderNo(dictionaryItem.getOrderNo());
					dictionaryService.saveDictionaryItem(dictionaryItemEntity);
				}
			}
		}

	}
}
