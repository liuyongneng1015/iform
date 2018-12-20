package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.api.model.DataModel;
import tech.ascs.icity.iform.api.model.DictionaryItemModel;
import tech.ascs.icity.iform.api.model.DictionaryModel;
import tech.ascs.icity.model.Page;

import java.util.List;


@RestController
@RequestMapping("/dictionary")
public interface DictionaryService {

	@ApiOperation(value = "获取所有字典表")
	@GetMapping
	List<DictionaryModel> list();

	@ApiOperation(value = "查询所有节点")
	@GetMapping("/items")
	List<DictionaryItemModel> listDictionaryItemMode();

	@ApiOperation("获取字典表分页数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页记录数", required = false, defaultValue = "10")
	})
	@GetMapping("/page")
    Page<DictionaryModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pageSize", defaultValue = "10") int pageSize);

	@ApiOperation("新增字典表")
	@ApiImplicitParams({
	})
	@PostMapping
	void add(@RequestBody(required = true) DictionaryModel dictionaryModel);

	@ApiOperation("更新字典表")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	})
	@PutMapping("/{id}")
	void update(@PathVariable(name="id") String id,@RequestBody(required = true) DictionaryModel dictionaryModel);

	@ApiOperation("删除字典表")
	@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	@DeleteMapping("/{id}")
	void delete(@PathVariable(name="id") String id);

	@ApiOperation("获取字典表选项列表")
	@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	@GetMapping("/{id}/items")
	List<DictionaryItemModel> listItem(@PathVariable(name="id") String id);

	@ApiOperation("新增字典表选项")
	@ApiImplicitParams({})
	@PostMapping("/add/items")
	void addItem(@RequestBody DictionaryItemModel dictionaryItemModel);

	@ApiOperation("更新字典表选项")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表选项ID", required = true, dataType = "String")
	})
	@PutMapping("/items/{id}")
	void updateItem(@PathVariable(name="id", required = true) String id,
					@RequestBody(required = true) DictionaryItemModel dictionaryItemModel);

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
}
