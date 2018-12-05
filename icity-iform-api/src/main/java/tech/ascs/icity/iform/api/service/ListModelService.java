package tech.ascs.icity.iform.api.service;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

@RequestMapping("/list-models")
public interface ListModelService {

	/**
	 * 获取所有列表模型
	 * 
	 * @param name （可选）列表名称
	 * @return
	 */
	@ApiOperation(value = "获取所有列表模型", position = 0)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "列表名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = false)
	})
	@GetMapping
	List<ListModel> list(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "applicationId", required = false) String applicationId);


	/**
	 * 获取列表模型分页数据
	 * 
	 * @param name （可选）列表名称
	 * @param page （可选）页码，默认为1
	 * @param pagesize （可选）每页记录数，默认为10
	 * @return
	 */
	@ApiOperation(value = "获取列表模型分页数据", position = 1)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "列表名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10"),
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = false)
	})
	@GetMapping("/page")
	Page<ListModel> page(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "page", defaultValue = "1") int page,
						 @RequestParam(name="pagesize", defaultValue = "10") int pagesize, @RequestParam(name = "applicationId", required = false) String applicationId);


	/**
	 * 根据列表模型ID获取列表模型对象
	 * 
	 * @param id 列表模型ID（uuid）
	 * @return
	 */
	@ApiOperation(value = "根据列表模型ID获取列表模型对象", position = 2)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "id", value = "列表模型ID", required = true, dataType = "String")
	})
	@GetMapping("/{id}")
	ListModel get(@PathVariable(name="id") String id);


	/**
	  * 新建列表模型
	 * 
	 * @param listModel 列表模型
	 */
	@ApiOperation(value = "新建列表模型", position = 3)
	@PostMapping
	IdEntity createListModel(@RequestBody ListModel listModel);


	/**
	 * 更新列表模型
	 * 
	 * @param id 列表模型ID
	 * @param listModel 列表模型
	 */
	@ApiOperation(value = "更新列表模型", position = 3)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "id", value = "列表模型ID", required = true, dataType = "String")
	})
	@PutMapping("/{id}")
	void updateListModel(@PathVariable(name="id") String id, @RequestBody ListModel listModel);


	/**
	 * 删除列表模型
	 * 
	 * @param id 列表模型ID
	 */
	@ApiOperation(value = "删除列表模型", position = 6)
    @ApiImplicitParam(paramType = "path", name = "id", value = "列表模型ID", required = true, dataType = "String")
	@DeleteMapping("/{id}")
	void removeListModel(@PathVariable(name="id") String id);

	/**
	 * 查询列表模型
	 *
	 * @param tableName 数据库表
	 */
	@ApiOperation(value = "查询列表模型", position = 6)
	@ApiImplicitParam(paramType = "query", name = "tableName", value = "数据库表", required = true, dataType = "String")
	@GetMapping("/list")
	List<ListModel> findListModelsByTableName(@PathVariable(name="tableName") String tableName);
}
