package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.api.model.DictionaryDataItemModel;
import tech.ascs.icity.iform.api.model.DictionaryDataModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.iform.api.model.SystemCodeModel;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import java.util.List;


@RestController
@RequestMapping("/dictionary-models")
public interface DictionaryModelService {

	@ApiOperation(value = "获取所有字典建模表")
	@GetMapping
	List<DictionaryModel> list();

	@ApiOperation(value = "获取字典建模表")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典建模表ID", required = true, dataType = "String")
	})
	@GetMapping("/{id}")
	DictionaryModel get(@PathVariable(name = "id") String id);


	@ApiOperation("获取字典建模表分页数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
			@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "12"),
			@ApiImplicitParam(paramType = "query", name = "name", value = "字典名称", required = false)
			})
	@GetMapping("/page")
    Page<DictionaryModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "pagesize", defaultValue = "12") int pagesize,
							   @RequestParam(name = "name",required = false) String name);


	@ApiOperation("新增字典建模表")
	@PostMapping
	IdEntity add(@RequestBody(required = true) DictionaryModel dictionaryModel);

	@ApiOperation("更新字典建模表")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典建模表ID", required = true, dataType = "String")
	})
	@PutMapping("/{id}")
	void update(@PathVariable(name = "id") String id, @RequestBody(required = true) DictionaryModel dictionaryModel);

	@ApiOperation("删除字典建模表")
	@ApiImplicitParam(paramType="path", name = "id", value = "字典建模表ID", required = true, dataType = "String")
	@DeleteMapping("/{id}")
	void delete(@PathVariable(name = "id") String id);

}
