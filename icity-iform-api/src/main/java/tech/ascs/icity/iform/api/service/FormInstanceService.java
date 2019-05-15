package tech.ascs.icity.iform.api.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import tech.ascs.icity.iform.api.model.DataInstance;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.model.FormDataSaveInstance;
import tech.ascs.icity.iform.api.model.FormInstance;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/form-instances")
public interface FormInstanceService {

	/**
	 * 获取表单实例列表
	 * 
	 * @param listId 列表模型ID
	 * @return
	 */
	@ApiOperation(value = "获取表单实例列表", notes = "附加查询条件（可选）：列表建模中的查询条件，以key=value的形式拼接到url，其中key为字段模型ID", position = 0)
	@GetMapping("/{listId}")
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "parameters", value = "查询参数", required = false)
	})
	List<FormDataSaveInstance> list(@PathVariable(name="listId") String listId, @RequestParam Map<String, Object> parameters);

	/**
	 * 获取简化的表单实例列表
	 *
	 * @param listId 列表模型ID
	 * @return
	 */
	@ApiOperation(value = "获取简化的表单实例列表", notes = "附加查询条件（可选）：列表建模中的查询条件，以key=value的形式拼接到url，其中key为字段模型ID", position = 0)
	@GetMapping("/simplify/{listId}")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "parameters", value = "查询参数", required = false)
	})
	List<FormDataSaveInstance> simplifyList(@PathVariable(name="listId") String listId, @RequestParam Map<String, Object> parameters);

	/**
	 * 获取表单实例列表
	 *
	 * @param listId 列表模型ID
	 * @return
	 */
	@ApiOperation(value = "获取表单实例列表", notes = "附加查询条件（可选）：列表建模中的查询条件，以key=value的形式拼接到url，其中key为字段模型ID", position = 0)
	@GetMapping("/reference-data/{itemId}")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "path", name = "itemId", value = "控件模型ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "parameters", value = "查询参数", required = false)
	})
	List<DataInstance> listRefereceData(@PathVariable(name="listId") String listId, @PathVariable(name="itemId") String itemId, @RequestParam Map<String, Object> parameters);


	/**
	 * 获取表单实例分页数据
	 * 
	 * @param listId 列表模型ID
	 * @param page （可选）页码，默认为1
	 * @param pagesize （可选）每页记录数，默认为10
	 * @return
	 */
	@ApiOperation(value = "获取表单实例分页数据（列表查询）", notes = "附加查询条件（可选）：列表建模中的查询条件，以key=value的形式拼接到url，其中key为字段模型ID", position = 1)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10"),
		@ApiImplicitParam(paramType = "query", name = "parameters", value = "查询参数", required = false)
	})
	@GetMapping("/{listId}/page")
	Page<FormDataSaveInstance> page(
			@PathVariable(name = "listId") String listId,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pagesize", defaultValue = "10") int pagesize,
			@RequestParam Map<String, Object> parameters);

	/**
	 * 导出表单实例数据
	 * @param listId 列表模型ID
	 * @return
	 */
	@ApiOperation(value = "导出表单实例数据", position = 1)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "parameters", value = "查询参数", required = false)
	})
	@GetMapping("/{listId}/export")
	void export(HttpServletResponse response,
				@PathVariable(name="listId") String listId,
				@RequestParam Map<String, Object> parameters);

	/**
	 * 通过表单ID和条件分页查询表单实例数据
	 *
	 * @param formId 表单模型ID
	 * @param page （可选）页码，默认为1
	 * @param pagesize （可选）每页记录数，默认为10
	 * @return
	 */
	@ApiOperation(value = "通过表单ID和条件分页查询表单实例数据", notes = "附加查询条件（可选）：列表建模中的查询条件，以key=value的形式拼接到url，其中key为字段模型ID", position = 1)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10"),
		@ApiImplicitParam(paramType = "query", name = "parameters", value = "查询参数", required = false)
	})
	@GetMapping("/form/{formId}/page")
	Page<FormDataSaveInstance> formPage(
				@PathVariable(name="formId") String formId,
				@RequestParam(name = "page", defaultValue = "1") int page,
				@RequestParam(name="pagesize", defaultValue = "10") int pagesize,
				@RequestParam Map<String, Object> parameters);

	/**
	 * 获取表单实例分页数据
	 *
	 * @param tableName 表名
	 * @param page （可选）页码，默认为1
	 * @param pagesize （可选）每页记录数，默认为10
	 * @return
	 */
	@ApiOperation(value = "获取表单实例分页数据（列表查询）", notes = "附加查询条件（可选）：列表建模中的查询条件，以key=value的形式拼接到url，其中key为字段模型ID", position = 1)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "tableName", value = "表名", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
			@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10")
	})
	@GetMapping("/table/page")
	Page<String> pageByTableName(
			@PathVariable(name="tableName") String tableName,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name="pagesize", defaultValue = "10") int pagesize);


	/**
	 * 获取空的表单实例对象
	 * 
	 * @param formId 表单模型ID
	 * @return
	 */
	@ApiOperation(value = "获取空的表单实例对象", position = 2)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String")
	})
	@GetMapping("/{formId}/empty")
	FormInstance getEmptyInstance(@PathVariable(name="formId") String formId);


	/**
	 * 通过数据建模表名获取空的表单实例对象
	 *
	 * @param tableName 数据建模表名
	 * @return
	 */
	@ApiOperation(value = "通过数据建模表名获取空的表单实例对象", position = 2)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "tableName", value = "表名", required = true, dataType = "String")
	})
	@GetMapping("/get-by-table-name/empty")
	FormInstance getEmptyInstanceByTableName(@RequestParam(name="tableName", required = true) String tableName);


	/**
     * 根据表单实例ID获取表单实例对象
     *
     * @param formId 表单模型ID
     * @param id 表单实例ID
     * @return
     */
    @ApiOperation(value = "根据表单实例ID获取表单实例对象", position = 2)
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "id", value = "表单实例ID", required = true, dataType = "String")
    })
    @GetMapping("/{formId}/{id}")
    FormDataSaveInstance get(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id);


	/**
	 * 根据表单实例ID获取表单columnName与对应的取值value
	 *
	 * @param formId 表单模型ID
	 * @param id 表单实例ID
	 * @return
	 */
	@ApiOperation(value = "根据表单实例ID获取表单columnName与对应的取值value", position = 2)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "path", name = "id", value = "表单实例ID", required = true, dataType = "String")
	})
	@GetMapping("/{formId}/{id}/column-name-value")
	Map getColumnNameValue(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id);

	/**
     * 根据表单实例ID获取表单实例对象
     *
     * @param listId 表单模型ID
     * @param id 表单实例ID
     * @return
     */
    @ApiOperation(value = "根据列表id和实例ID获取表单实例对象", position = 2)
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "id", value = "表单实例ID", required = true, dataType = "String")
    })
    @GetMapping("/formData/{listId}/{id}")
    FormDataSaveInstance getFormDataByListId(@PathVariable(name="listId") String listId, @PathVariable(name="id") String id);


	/**
	 * 根据表单实例ID获取表单二维码实例对象
	 *
	 * @param listId 表单模型ID
	 * @param id 表单实例ID
	 * @return
	 */
	@ApiOperation(value = "根据表单实例ID获取表单二维码实例对象", position = 2)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "path", name = "id", value = "表单实例ID", required = true, dataType = "String")
	})
	@GetMapping("/qrcode/{listId}/{id}")
	FormDataSaveInstance getQrCode(@PathVariable(name="listId") String listId, @PathVariable(name="id") String id);

	/**
	  * 新建表单实例
	 * 
	 * @param formId 表单模型ID
	 * @param formInstance 表单实例
	 */
	@ApiOperation(value = "新建表单实例", position = 3)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String")
	})
	@PostMapping("/{formId}")
	IdEntity createFormInstance(@PathVariable(name="formId", required = true) String formId, @RequestBody FormDataSaveInstance formInstance);


	/**
	 * 更新表单实例
	 * 
	 * @param formId 表单模型ID
	 * @param id 表单实例ID
	 * @param formInstance 表单实例
	 */
	@ApiOperation(value = "更新表单实例", position = 3)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "path", name = "id", value = "表单实例ID", required = true, dataType = "String")
	})
	@PutMapping("/{formId}/{id}")
	void updateFormInstance(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id, @RequestBody FormDataSaveInstance formInstance);


	/**
	 * 删除流程
	 * 
	 * @param formId 表单模型ID
	 * @param id 表单实例ID
	 */
	@ApiOperation(value = "删除表单实例", position = 6)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "path", name = "id", value = "表单实例ID", required = true, dataType = "String")
	})
	@DeleteMapping("/{formId}/{id}")
	void removeFormInstance(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id);


	/**
	 * 批量删除表单实例
	 *
	 * @param formId 表单模型ID
	 */
	@ApiOperation(value = "批量删除表单实例", position = 6)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "formId", value = "表单模型ID", required = true, dataType = "String")
	})
	@DeleteMapping("/{formId}/batchDelete")
	void removeFormInstance(@PathVariable(name="formId") String formId, @RequestBody List<String> ids);

	/**
	 * 重置表单二维码
	 *
	 * @param listId 列表模型ID
	 */
	@ApiOperation(value = "重置表单二维码", position = 6)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "listId", value = "列表模型ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "path", name = "id", value = "表单实例ID", required = true, dataType = "String")
	})
	@PutMapping("/{listId}/{id}/reset_qrcode")
	FileUploadModel resetQrCode(@PathVariable(name="listId", required = true) String listId, @PathVariable(name="id", required = true) String id);

	/**
	 * 通过表名和该表的itemModel的columnName字段的取值分页搜索表单实例
	 */
	@ApiOperation(value = "通过表名和该表的字段对应的value获取表单实例，columnValues的columnName必须是属于该表的字段", position = 6)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "tableName", value = "表名", required = false),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10")
	})
	@GetMapping("/find-by-table-and-column")
	Page<FormDataSaveInstance> findByTableNameAndColumnValue(@RequestParam(name="page", defaultValue = "1") int page,
															 @RequestParam(name="pagesize", defaultValue = "10") int pagesize,
															 @RequestParam(name="tableName", defaultValue = "") String tableName,
															 @RequestParam Map<String, Object> parameters);

	/**
	 * 根据表名获取表单columnName与对应的取值，即columnName与columnValue
	 *
	 * @param page
	 * @param pagesize
	 * @param tableName
	 * @param parameters
	 * @return
	 */
	@ApiOperation(value = "根据表名和列名取值查询表单columnName与对应的取值value", position = 2)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "tableName", value = "表名", required = false),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10")
	})
	@GetMapping("/get-column-name-value-by-table")
	Page<Map> getColumnNameValueByTable(@RequestParam(name="page", defaultValue = "1") int page,
										@RequestParam(name="pagesize", defaultValue = "10") int pagesize,
										@RequestParam(name="tableName", defaultValue = "") String tableName,
										@RequestParam Map<String, Object> parameters);

	/**
	 * 静态的策略组接口
	 *
	 * @param userId 用户ID
	 */
	@ApiOperation(value = "静态的策略组接口", position = 6)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "userId", value = "用户ID", required = true, dataType = "String")
	})
	@GetMapping("/users/{userId}/strategyGroup")
	Map dashboard(@PathVariable(name="userId", required = true) String userId) throws IOException;
}
