package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.service.DictionaryModelService;

import java.util.ArrayList;
import java.util.List;


@Api(tags = "字典建模数据表管理",description = "字典建模数据表管理服务")
@RestController
public class DictionaryModelDataController implements tech.ascs.icity.iform.api.service.DictionaryModelDataService {

	private Logger log = LoggerFactory.getLogger(DictionaryModelDataController.class);


	@Autowired
	private DictionaryModelService dictionaryService;


	@Override
	public List<DictionaryModelData> findAll(@PathVariable(name = "dictionaryId", required = true) String dictionaryId) {
		List<DictionaryModelData> list = new ArrayList<>();
		DictionaryModelData dictionaryModelData = dictionaryService.findDictionaryModelDataByDictionaryId(dictionaryId);
		if(dictionaryModelData != null) {
			list.add(dictionaryModelData);
		}
		return list;
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
	public void batchDelete(@PathVariable(name = "dictionaryId") String dictionaryId, @RequestParam(name = "ids", required = true) String[] ids) {
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
