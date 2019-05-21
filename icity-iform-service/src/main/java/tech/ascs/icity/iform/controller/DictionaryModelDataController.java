package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.service.DictionaryModelService;


@Api(tags = "字典建模数据表管理",description = "字典建模数据表管理服务")
@RestController
public class DictionaryModelDataController implements tech.ascs.icity.iform.api.service.DictionaryModelDataService {

	private Logger log = LoggerFactory.getLogger(DictionaryModelDataController.class);


	@Autowired
	private DictionaryModelService dictionaryService;


	@Override
	public DictionaryModelData findAll(@PathVariable(name = "dictionaryId", required = true) String dictionaryId) {
		return dictionaryService.getDictionaryModelDataByDictionaryId(dictionaryId);
	}

	@Override
	public DictionaryModelData get(@PathVariable(name = "dictionaryId") String dictionaryId, @PathVariable(name = "id") Integer id) {
		return dictionaryService.getDictionaryModelDataById(dictionaryId, id);
	}

	@Override
	public void add(@RequestBody DictionaryModelData dictionaryModel) {
		dictionaryService.saveDictionaryModelData(dictionaryModel);
	}

	@Override
	public void update(@PathVariable(name = "id", required = true) Integer id,
					   @RequestBody(required = true) DictionaryModelData dictionaryModel) {
		if(!id.equals(dictionaryModel.getId())){
			throw new IFormException("id不一致");
		}
		dictionaryService.saveDictionaryModelData(dictionaryModel);
	}

	@Override
	public void delete(@PathVariable(name = "dictionaryId") String dictionaryId, @PathVariable(name = "id") Integer id) {
		DictionaryModelData dictionaryModelData = new DictionaryModelData();
		dictionaryModelData.setId(id);
		dictionaryModelData.setDictionaryId(dictionaryId);
		dictionaryService.deleteDictionaryModelData(dictionaryModelData);
	}

	@Override
	public void updateOrderNo(@PathVariable(name = "dictionaryId", required = true) String dictionaryId, @PathVariable(name = "id", required = true) Integer id,
							  @PathVariable(name = "status", required = true) String status) {
		dictionaryService.updateDictionaryModelDataOrderNo(dictionaryId, id, status);
	}
}
