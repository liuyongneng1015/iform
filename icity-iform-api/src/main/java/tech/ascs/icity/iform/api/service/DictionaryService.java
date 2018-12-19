package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
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

	@ApiOperation("获取字典表分页数据")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页记录数", required = false, defaultValue = "10")
	})
	@GetMapping("/page")
    Page<DictionaryModel> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pageSize", defaultValue = "10") int pageSize);

	@ApiOperation("新增字典表")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="query", name = "name", value = "名称", required = true),
			@ApiImplicitParam(paramType="query", name = "description", value = "描述", required = false)
	})
	@PostMapping
	void add(@RequestParam(name = "name") String name, @RequestParam(name = "description", required = false) String description);

	@ApiOperation("更新字典表")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="query", name = "name", value = "名称", required = false),
			@ApiImplicitParam(paramType="query", name = "description", value = "描述", required = false)
	})
	@PutMapping("/{id}")
	void update(@PathVariable(name="id") String id, @RequestParam(name="name", required=false) String name, @RequestParam(name="description", required=false) String description);

	@ApiOperation("删除字典表")
	@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	@DeleteMapping("/{id}")
	void delete(@PathVariable(name="id") String id);

	@ApiOperation("获取字典表选项列表")
	@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String")
	@GetMapping("/{id}/items")
	List<DictionaryItemModel> listItem(@PathVariable(name="id") String id);

	@ApiOperation("新增字典表选项")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="query", name = "id", value = "字典表ID", required = false, dataType = "String"),
			@ApiImplicitParam(paramType="query", name = "name", value = "名称", required = true),
			@ApiImplicitParam(paramType="query", name = "code", value = "编码", required = true),
			@ApiImplicitParam(paramType="query", name = "description", value = "描述", required = false),
			@ApiImplicitParam(paramType="query", name = "parentItemId", value = "父级字典项", required = false)
	})
	@PostMapping("/add/items")
	void addItem(
			@RequestParam(name="id") String id,
			@RequestParam(name="name") String name,
			@RequestParam(name="code") String code,
			@RequestParam(name="description", required = false) String description, @RequestParam(name="parentItemId", required = false) String parentItemId);

	@ApiOperation("更新字典表选项")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="query", name = "id", value = "字典表ID", required = false, dataType = "String"),
			@ApiImplicitParam(paramType="path", name = "itemId", value = "字典表选项ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="query", name = "name", value = "名称", required = false),
			@ApiImplicitParam(paramType="query", name = "code", value = "编码", required = false),
			@ApiImplicitParam(paramType="query", name = "description", value = "描述", required = false),
			@ApiImplicitParam(paramType="query", name = "parentItemId", value = "父级字典项", required = false)
	})
	@PutMapping("/update/items/{itemId}")
	void updateItem(@RequestParam(name="id") String id, @PathVariable(name="itemId", required = true) String itemId,
					@RequestParam(name="name", required=false) String name,
					@RequestParam(name="code", required=false) String code,
					@RequestParam(name="description", required=false) String description, @RequestParam(name="parentItemId", required = false) String parentItemId);

	@ApiOperation("删除字典表选项")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="path", name = "itemId", value = "字典表选项ID", required = true, dataType = "String")
	})
	@DeleteMapping("/{id}/items/{itemId}")
	void deleteItem(@PathVariable(name="id") String id, @PathVariable(name="itemId") String itemId);

	@ApiOperation("上下移动系统代码")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "itemId", value = "字典表选项ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="query", name = "orderNo", value = "上移-1，下移+1", required = false)
	})
	@PutMapping("/items/orderno/{itemId}")
	void updateItemOrderNo( @PathVariable(name="itemId",required = true) String itemId, @RequestParam(name="orderNo", defaultValue = "0") int orderNo);

	@ApiOperation("上下移动系统代码分类")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="path", name = "id", value = "字典表选项ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType="query", name = "orderNo", value = "上移-1，下移+1", required = false)
	})
	@PutMapping("/orderno/{id}")
	void updateDictionaryOrderNo( @PathVariable(name="id",required = true) String id, @RequestParam(name="orderNo", defaultValue = "0") int orderNo);
}
