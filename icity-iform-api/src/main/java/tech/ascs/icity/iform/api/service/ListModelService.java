package tech.ascs.icity.iform.api.service;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

@RestController
@RequestMapping("/list-models")
public interface ListModelService {

	/**
	 * 获取所有列表模型
	 * 
	 * @param name （可选）列表名称
	 * @return
	 */
	@ApiOperation(value = "获取所有列表模型，全部返回简要信息", position = 0)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "列表名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = false)
	})
	@GetMapping
	List<ListModel> list(@RequestParam(name = "name", defaultValue = "") String name,
						 @RequestParam(name = "applicationId", required = false) String applicationId);

	/**
	 * 获取列表模型分页数据
	 * 
	 * @param name （可选）列表名称
	 * @param page （可选）页码，默认为1
	 * @param pagesize （可选）每页记录数，默认为10
	 * @return
	 */
	@ApiOperation(value = "获取列表模型分页数据，全部返回简要信息", position = 1)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "列表名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10"),
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = false)
	})
	@GetMapping("/page")
	Page<ListModel> page(@RequestParam(name = "name", defaultValue = "") String name,
                         @RequestParam(name = "page", defaultValue = "1") int page,
						 @RequestParam(name = "pagesize", defaultValue = "10") int pagesize,
                         @RequestParam(name = "applicationId", required = false) String applicationId);


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
	 * 根据列表模型ID获取列表模型对象
	 *
	 * @param id 列表模型ID（uuid）
	 * @return
	 */
	@ApiOperation(value = "根据列表模型ID获取列表模型对象", position = 2)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "id", value = "列表模型ID", required = true, dataType = "String")
	})
	@GetMapping("/app/{id}")
	ListModel getApp(@PathVariable(name="id") String id);


	/**
	 * 根据唯一编码查询列表建模
	 *
	 * @return
	 */
	@ApiOperation(value = "根据唯一编码查询列表建模", position = 2)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "id", value = "唯一编码", required = true, dataType = "String")
	})
	@GetMapping("/find")
	ListModel find(@RequestParam(name = "uniqueCode") String uniqueCode);

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

	@ApiOperation(value = "批量删除列表", position = 8)
	@DeleteMapping("/batch-delete")
	void removeListModels(@RequestBody List<String> ids);

	/**
	 * 查询列表模型
	 *
	 * @param tableName 数据库表
	 */
	@ApiOperation(value = "查询列表模型", position = 6)
	@ApiImplicitParam(paramType = "query", name = "tableName", value = "数据库表", required = true, dataType = "String")
	@GetMapping("/list")
	List<ListModel> findListModelsByTableName(@PathVariable(name="tableName") String tableName);

	/**
	 * 查询列表应用模型
	 *
	 */
	@ApiOperation(value = "查询列表应用模型，按应用分类返回", position = 6)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "formId", value = "表单ID", required = false, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "functionType", value = "功能类型", required = false, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用ID", required = false, dataType = "String")
	})
	@GetMapping("/application")
	List<ApplicationModel> findListApplicationModel(@RequestParam(name = "formId", required = false) String formId,
													@RequestParam(name = "functionType", required = false) FunctionType functionType,
													@RequestParam(name="applicationId", required = true) String applicationId);

	/**
	 * 查询应用绑定的列表建模和表单建模，如果应用在iform有与列表建模和表单建模绑定，不能删除应用
	 */
	@ApiOperation(value = "查询应用绑定的列表建模和表单建模，如果应用在iform有与列表建模和表单建模绑定，不能删除应用", position = 6)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = true, dataType = "String")
	})
	@GetMapping("/application-reference-list-form")
	AppListForm findAppReferenceListForm(@RequestParam(name="applicationId", required = true) String applicationId);

	/**
	 * 查询列表的权限和列表绑定的表单的权限
	 * @param id
	 * @return
	 */
	@ApiOperation(value = "查询列表的权限和列表绑定的表单的权限", position = 7)
	@ApiImplicitParam(paramType = "path", name = "id", value = "列表模型ID", required = true, dataType = "String")
	@GetMapping("/{id}/list-form-btn-permissions")
	ListFormBtnPermission getListFormBtnPermissions(@PathVariable(name = "id") String id);

	@ApiOperation(value = "批量ID查询概要信息，只返回ID和名称", position = 9)
	@ApiImplicitParam(paramType="query", name = "ids", value = "ID集合", required = false, dataType = "Array")
	@GetMapping("/batch-simple-info")
	List<ListModel> findListModelSimpleByIds(@RequestParam(name = "ids", required = false) String[] ids);

	/**
	 * 根据列表模型ID获取列表模型对象
	 *
	 * @param tableName 列表模型ID（uuid）
	 * @return
	 */
	@ApiOperation(value = "根据数据库表名获取列表模型对象", position = 2)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "tableName", value = "数据库表名", required = true, dataType = "String")
	})
	@GetMapping("/find_by_tableName/{tableName}")
	ListModel getByTableName(@PathVariable(name="tableName") String tableName);
}
