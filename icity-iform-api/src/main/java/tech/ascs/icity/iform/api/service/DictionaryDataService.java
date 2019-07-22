package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.api.model.DictionaryDataItemModel;
import tech.ascs.icity.iform.api.model.DictionaryDataModel;
import tech.ascs.icity.iform.api.model.SystemCodeModel;
import tech.ascs.icity.model.Page;

import java.util.List;


@RestController
@RequestMapping("/dictionary")
public interface DictionaryDataService {

	@ApiOperation(value = "获取所有字典表")
	@GetMapping
	SystemCodeModel list();

	@ApiOperation(value = "查询所有节点")
	@ApiImplicitParam(paramType="path", name = "id", value = "系统分类字典id", required = true, dataType = "String")
	@GetMapping("/{id}/dictionary-items")
	List<DictionaryDataItemModel> listDictionaryItemModel(@PathVariable(name = "id", required = true) String id);

	@ApiOperation("获取字典表分页数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
			@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "12"),
			@ApiImplicitParam(paramType = "query", name = "name", value = "字典名称", required = false)
	})
	@GetMapping("/page")
    Page<DictionaryDataModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pagesize", defaultValue = "12") int pagesize,
								   @RequestParam(name = "name",required = false) String name);

	@ApiOperation("获取字典表及子项数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "name", value = "名称", required = false),
			@ApiImplicitParam(paramType = "query", name = "code", value = "编码", required = false)
	})
	@GetMapping("/model")
    DictionaryDataModel getByNameAndCode(@RequestParam(name = "name", required = false) String name, @RequestParam(name="code", required = false) String code);

	@ApiOperation("新增字典表")
	@PostMapping
	void add(@RequestBody(required = true) DictionaryDataModel dictionaryModel);

	@ApiOperation("更新字典表")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	})
	@PutMapping("/{id}")
	void update(@PathVariable(name="id") String id,@RequestBody(required = true) DictionaryDataModel dictionaryModel);

	@ApiOperation("删除字典表")
	@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	@DeleteMapping("/{id}")
	void delete(@PathVariable(name="id") String id);

	@ApiOperation("获取字典表选项列表")
	@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	@GetMapping("/{id}/items")
	List<DictionaryDataItemModel> listItem(@PathVariable(name="id",required = true) String id);

	@ApiOperation("新增字典表选项")
	@ApiImplicitParams({})
	@PostMapping("/add/items")
	void addItem(@RequestBody DictionaryDataItemModel dictionaryItemModel);

	@ApiOperation("更新字典表选项")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表选项ID", required = true, dataType = "String")
	})
	@PutMapping("/items/{id}")
	void updateItem(@PathVariable(name="id", required = true) String id,
					@RequestBody(required = true) DictionaryDataItemModel dictionaryItemModel);

	@ApiOperation("删除字典表选项")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表选项ID", required = true, dataType = "String")
	})
	@DeleteMapping("/items/{id}")
	void deleteItem(@PathVariable(name="id") String id);

	@ApiOperation("上下移动系统代码")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表选项ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="path", name = "status", value = "上移up，下移down", required = true)
	})
	@PutMapping("/items/{id}/{status}")
	void updateItemOrderNo( @PathVariable(name="id",required = true) String id, @PathVariable(name="status", required = true) String status);

	@ApiOperation("上下移动系统代码分类")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表选项ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="path", name = "status", value = "上移up，下移down", required = true)
	})
	@PutMapping("/{id}/{status}")
	void updateDictionaryOrderNo( @PathVariable(name="id",required = true) String id, @PathVariable(name="status", required = true) String status);

	@ApiOperation(value = "查询第一级子节点")
	@ApiImplicitParam(paramType="path", name = "id", value = "查询第一级子节点", required = true, dataType = "String")
	@GetMapping("/items/{id}/children")
	List<DictionaryDataItemModel> childrenDictionaryItemModel(@PathVariable(name = "id", required = true) String id);

	@ApiOperation("获取数据字典表选项列表")
	@ApiImplicitParams({
		@ApiImplicitParam(paramType="path", name = "id", value = "字典表分类ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType="path", name = "itemId", value = "字典表选项ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType="query", name = "itemModelId", value = "字典表选项ID", required = false, dataType = "String"),
		@ApiImplicitParam(paramType="query", name = "linkageDataUnbind", value = "联动数据解绑", required = false, dataType = "Boolean")
	})
	@GetMapping("/{id}/{itemId}/items")
	List<DictionaryDataItemModel> findItems(@PathVariable(name="id", required = true) String id,
											@PathVariable(name="itemId", required = true) String itemId,
											@RequestParam(name="itemModelId", required = false) String itemModelId,
											@RequestParam(name="linkageDataUnbind", defaultValue = "false") Boolean linkageDataUnbind);


	@ApiOperation(value = "通过批量ID获取字典表选项的详情")
	@ApiImplicitParam(paramType = "query", name = "ids",  value = "ID集合", allowMultiple=true)
	@GetMapping("/items/batch-simple-info")
	List<DictionaryDataItemModel> batchSimpleInfo(@RequestParam(name = "ids", required = false) String[] ids);
}
