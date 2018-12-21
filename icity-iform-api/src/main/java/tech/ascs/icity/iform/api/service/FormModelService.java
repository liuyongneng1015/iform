package tech.ascs.icity.iform.api.service;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import tech.ascs.icity.iform.api.model.ApplicationModel;
import tech.ascs.icity.iform.api.model.FormModel;
import tech.ascs.icity.iform.api.model.ItemModel;
import tech.ascs.icity.iform.api.model.PCFormModel;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

@RestController
@RequestMapping("/form-models")
public interface FormModelService {

	/**
	 * 获取所有表单模型
	 * 
	 * @param name （可选）表单名称
	 * @return
	 */
	@ApiOperation(value = "获取所有表单模型", position = 0)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "表单名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = false)
	})
	@GetMapping
	List<FormModel> list(@RequestParam(name = "name", required = false ) String name, @RequestParam(name = "applicationId", required = false) String applicationId);


	/**
	 * 获取表单模型分页数据
	 * 
	 * @param name （可选）表单名称
	 * @param page （可选）页码，默认为1
	 * @param pagesize （可选）每页记录数，默认为10
	 * @return
	 */
	@ApiOperation(value = "获取表单模型分页数据", position = 1)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "表单名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10"),
		@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = false)
	})
	@GetMapping("/page")
	Page<FormModel> page(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "page", defaultValue = "1") int page,
						 @RequestParam(name="pagesize", defaultValue = "10") int pagesize, @RequestParam(name = "applicationId", required = false) String applicationId);


	/**
	 * 根据表单模型ID获取表单模型对象
	 * 
	 * @param id 表单模型ID（uuid）
	 * @return
	 */
	@ApiOperation(value = "根据表单模型ID获取表单模型对象", position = 2)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "id", value = "表单模型ID", required = true, dataType = "String")
	})
	@GetMapping("/{id}")
	FormModel get(@PathVariable(name="id") String id);

	/**
	 * 保存表单数据模型
	 *
	 * @param formModel 表单模型
	 */
	@ApiOperation(value = "保存表单数据模型", position = 3)
	@PostMapping("/form-data")
	IdEntity saveFormDataModel(@RequestBody FormModel formModel);


	/**
	  * 新建表单模型
	 * 
	 * @param formModel 表单模型
	 */
	@ApiOperation(value = "新建表单模型", position = 3)
	@PostMapping
	IdEntity createFormModel(@RequestBody FormModel formModel);


	/**
	 * 更新表单模型
	 * 
	 * @param id 表单模型ID
	 * @param formModel 表单模型
	 */
	@ApiOperation(value = "更新表单模型", position = 3)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "id", value = "表单模型ID", required = true, dataType = "String")
	})
	@PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	void updateFormModel(@PathVariable(name="id") String id, @RequestBody FormModel formModel);


	/**
	 * 删除流程
	 * 
	 * @param id 表单模型ID
	 */
	@ApiOperation(value = "删除表单模型", position = 6)
    @ApiImplicitParam(paramType = "path", name = "id", value = "表单模型ID", required = true, dataType = "String")
	@DeleteMapping("/{id}")
	void removeFormModel(@PathVariable(name="id") String id);


	/**
	 * 根据表单模型ID获取表单模型对象
	 *
	 * @param id 表单模型ID（uuid）
	 * @return
	 */
	@ApiOperation(value = "根据表单模型ID获取表单模型对象", position = 2)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "id", value = "表单模型ID", required = true, dataType = "String")
	})
	@GetMapping("/pc/{id}")
	PCFormModel getPCFormModelById(@PathVariable(name="id") String id);

	/**
	 * 获取数据标识
	 *
	 * @param itemModelId 控件模型id（uuid）
	 * @return
	 */
	@ApiOperation(value = "获取数据标识", position = 2)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "itemModelId", value = "控件模型id", required = true, dataType = "String")
	})
	@GetMapping("/data_mark")
	FormModel getByItemModelId(@RequestParam(name="itemModelId") String itemModelId);


	/**
	 * 保存表单控件权限
	 *
	 * @param id 表单模型ID
	 * @param formModel 表单模型
	 */
	@ApiOperation(value = "保存表单控件权限", position = 3)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "id", value = "表单模型ID", required = true, dataType = "String")
	})
	@PutMapping(value = "/permission/{id}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	void saveFormModelPermission(@PathVariable(name="id") String id, @RequestBody FormModel formModel);

	/**
	 * 保存表单提交校验
	 *
	 * @param id 表单模型ID
	 * @param formModel 表单模型
	 */
	@ApiOperation(value = "保存表单提交校验", position = 3)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "path", name = "id", value = "表单模型ID", required = true, dataType = "String")
	})
	@PutMapping(value = "/submit_check/{id}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	void saveFormModelSubmitCheck(@PathVariable(name="id") String id, @RequestBody FormModel formModel);


	/**
	 * 应用表单模型
	 *
	 */
	@ApiOperation(value = "应用表单模型", position = 3)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "applicationId", value = "应用id", required = true, dataType = "String")
	})
	@GetMapping(value = "/application")
	List<ApplicationModel> findApplicationFormModel(@RequestParam(name="applicationId", required = true) String applicationId);


	/**
	 * 查询关联表单的控件模型
	 *
	 */
	@ApiOperation(value = "查询关联表单的控件模型", position = 3)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "id", value = "表单模型ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "itemId", value = "控件id", required = false, dataType = "String")
	})
	@GetMapping(value = "/form-item")
	List<ItemModel> findItemsByFormId(@RequestParam(name="id", required = true) String id, @RequestParam(name="itemId", required = false) String itemId);

}
