package tech.ascs.icity.iform.controller;

import java.util.List;

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

@RestController
@Api(tags = "字典表管理服务",description = "字典表管理")
public class DictionaryController implements tech.ascs.icity.iform.api.service.DictionaryService {

	@Autowired
	private DictionaryService dictionaryService;

	@Override
	public List<DictionaryModel> list() {
		return DTOTools.wrapList(dictionaryService.query().list(), DictionaryModel.class);
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
}
