package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.SelectItemModelEntity;
import tech.ascs.icity.iform.service.DictionaryModelService;
import tech.ascs.icity.iform.service.ItemModelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Api(tags = "字典建模数据表管理",description = "字典建模数据表管理服务")
@RestController
public class DictionaryModelDataController implements tech.ascs.icity.iform.api.service.DictionaryModelDataService {

	private Logger log = LoggerFactory.getLogger(DictionaryModelDataController.class);

	@Autowired
	private DictionaryModelService dictionaryService;
	@Autowired
	private ItemModelService itemModelService;

	@Override
	public List<DictionaryModelData> findAll(@PathVariable(name = "dictionaryId", required = true) String dictionaryId,
											 @RequestParam(name = "itemModelId", required = false) String itemModelId,
											 @RequestParam(name = "linkageDataUnbind", defaultValue = "false") Boolean linkageDataUnbind) {
		if (linkageDataUnbind && StringUtils.hasText(itemModelId)) {
			return queryLinkageDataUnbind(itemModelId);
		}
		List<DictionaryModelData> list = new ArrayList<>();
		DictionaryModelData dictionaryModelData = dictionaryService.findDictionaryModelDataByDictionaryId(dictionaryId);
		if(dictionaryModelData != null) {
			list.add(dictionaryModelData);
		}
		return list;
	}

	private List<DictionaryModelData> queryLinkageDataUnbind(String itemModelId) {
		Map<String, Object> map = itemModelService.findLinkageOriginItemModelEntity(itemModelId);
		List<DictionaryModelData> list = new ArrayList<>();
		if (map==null) {
			return list;
		}
		Integer level = (Integer)map.get("level");
		SelectItemModelEntity selectItemModel = (SelectItemModelEntity)map.get("item");
		String referenceDictionaryItemId = selectItemModel.getReferenceDictionaryItemId();
		return list;
	}


	@Override
	public List<DictionaryModelData> findFirstItems(@PathVariable(name = "id", required = true) String id, @PathVariable(name = "itemId", required = true) String itemId) {
		return dictionaryService.findFirstItems(id, itemId);
	}

	@Override
	public DictionaryModelData get(@PathVariable(name = "dictionaryId") String dictionaryId, @PathVariable(name = "id") String id) {
		return dictionaryService.getDictionaryModelDataById(dictionaryId, id);
	}

	@Override
	public void add(@RequestBody(required = true) DictionaryModelData dictionaryModel) {
		dictionaryService.saveDictionaryModelData(dictionaryModel);
	}

	@Override
	public void update(@PathVariable(name = "id", required = true) String id,
					   @RequestBody(required = true) DictionaryModelData dictionaryModel) {
		if(!id.equals(dictionaryModel.getId())){
			throw new IFormException("id不一致");
		}
		if("root".equals(dictionaryModel.getCode()) || "根节点".equals(dictionaryModel.getName())){
			throw new IFormException("不允许创建key为root或名称为根节点字典代码");
		}
		dictionaryService.saveDictionaryModelData(dictionaryModel);
	}

	@Override
	public void delete(@PathVariable(name = "dictionaryId") String dictionaryId, @PathVariable(name = "id") String id) {
		List<DictionaryModelData> dictionaryModelDataList = new ArrayList<>();
		dictionaryModelDataList.add(getDictionaryModelData(dictionaryId,id));
		dictionaryService.deleteDictionaryModelData(dictionaryModelDataList);
	}

	private DictionaryModelData getDictionaryModelData(String dictionaryId,String id){
		DictionaryModelData dictionaryModelData = new DictionaryModelData();
		dictionaryModelData.setId(id);
		dictionaryModelData.setDictionaryId(dictionaryId);
		return dictionaryModelData;
	}

	@Override
	public void batchDelete(@PathVariable(name = "dictionaryId") String dictionaryId, @RequestBody String[] ids) {
		List<DictionaryModelData> dictionaryModelDataList = new ArrayList<>();
		for(String id: ids) {
			dictionaryModelDataList.add(getDictionaryModelData(dictionaryId, id));
		}
		dictionaryService.deleteDictionaryModelData(dictionaryModelDataList);
	}

	@Override
	public void updateOrderNo(@PathVariable(name = "dictionaryId", required = true) String dictionaryId, @PathVariable(name = "id", required = true) String id,
							  @PathVariable(name = "status", required = true) String status) {
		dictionaryService.updateDictionaryModelDataOrderNo(dictionaryId, id, status);
	}
}
