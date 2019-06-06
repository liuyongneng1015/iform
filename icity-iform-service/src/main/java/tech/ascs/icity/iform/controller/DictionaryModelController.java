package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.service.DictionaryModelService;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Api(tags = "字典建模表管理",description = "字典建模表管理服务")
@RestController
public class DictionaryModelController implements tech.ascs.icity.iform.api.service.DictionaryModelService {

	private Logger log = LoggerFactory.getLogger(DictionaryModelController.class);


	@Autowired
	private DictionaryModelService dictionaryService;


	@Override
	public List<DictionaryModel> list() {
		return dictionaryService.findAllDictionary();
	}

	@Override
	public DictionaryModel get(@PathVariable(name = "id") String id) {
		return dictionaryService.getDictionaryById(id);
	}

	@Override
	public Page<DictionaryModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "pagesize", defaultValue = "12") int pagesize,
									  @RequestParam(name = "name",required = false) String name) {
		return dictionaryService.page(page, pagesize, name);
	}

	@Override
	public IdEntity add(@RequestBody(required = true) DictionaryModel dictionaryModel) {
		return dictionaryService.addDictionary(dictionaryModel);
	}

	@Override
	public void update(@PathVariable(name = "id") String id, @RequestBody(required = true) DictionaryModel dictionaryModel) {
		dictionaryService.updateDictionaryModel(dictionaryModel);
	}

	@Override
	public void delete(@PathVariable(name = "id") String id) {
		List<String> idList = new ArrayList<>();
		idList.add(id);
		dictionaryService.deleteDictionary(idList);
	}

	@Override
	public void batchDelete(@RequestBody String[] ids) {
		List<String> idList = Arrays.asList(ids);
		dictionaryService.deleteDictionary(idList);
	}

	@Override
	public void updateOrderNo(@PathVariable(name = "id", required = true) String id, @PathVariable(name = "status", required = true) String status) {
		dictionaryService.updateDictionaryModelOrderNo(id, status);
	}
}
