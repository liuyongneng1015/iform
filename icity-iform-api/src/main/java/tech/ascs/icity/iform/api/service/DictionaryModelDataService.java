package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.api.model.DictionaryModelData;

import java.util.List;


@RestController
@RequestMapping("/dictionary-models/data")
public interface DictionaryModelDataService {

	@ApiOperation(value = "获取树形结构字典建模数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "dictionaryId", value = "字典建模ID", required = true, dataType = "String")
	})
	@GetMapping("/all/{dictionaryId}")
	List<DictionaryModelData> findAll(@PathVariable(name = "dictionaryId", required = true) String dictionaryId);

	@ApiOperation(value = "获取单条字典建模数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "dictionaryId", value = "字典建模ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="path", name = "id", value = "字典建模数据ID", required = true, dataType = "Integer")
	})
	@GetMapping("/{dictionaryId}/{id}")
	DictionaryModelData get(@PathVariable(name = "dictionaryId") String dictionaryId, @PathVariable(name = "id") Integer id);

	@ApiOperation("新增字典建模数据")
	@PostMapping(consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	void add(@RequestBody(required = true) DictionaryModelData dictionaryModel);

	@ApiOperation("更新字典建模数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典建模数据ID", required = true, dataType = "Integer")
	})
	@PutMapping("/{id}")
	void update(@PathVariable(name = "id", required = true) Integer id,
				@RequestBody(required = true) DictionaryModelData dictionaryModel);

	@ApiOperation("删除字典建模数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "dictionaryId", value = "字典建模ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="path", name = "id", value = "字典建模数据ID", required = true, dataType = "Integer")
	})
	@DeleteMapping("/{dictionaryId}/{id}")
	void delete(@PathVariable(name = "dictionaryId") String dictionaryId, @PathVariable(name = "id") Integer id);

	@ApiOperation("上下移动字典建模数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "dictionaryId", value = "字典建模ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="path", name = "id", value = "字典建模数据ID", required = true, dataType = "Integer"),
			@ApiImplicitParam(paramType="path", name = "status", value = "上移up，下移down", required = true)
	})
	@PutMapping("/{dictionaryId}/{id}/{status}")
	void updateOrderNo(@PathVariable(name = "dictionaryId", required = true) String dictionaryId, @PathVariable(name = "id", required = true) Integer id,
					   @PathVariable(name = "status", required = true) String status);

}
