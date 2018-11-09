package tech.ascs.icity.iform.api.service;

import java.util.List;

import org.springframework.http.MediaType;
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
import tech.ascs.icity.iform.api.model.FormModel;
import tech.ascs.icity.iform.api.model.PCFormModel;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

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
		@ApiImplicitParam(paramType = "query", name = "name", value = "表单名称", required = false)
	})
	@GetMapping
	List<FormModel> list(@RequestParam(name = "name", defaultValue = "") String name);


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
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10")
	})
	@GetMapping("/page")
	Page<FormModel> page(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pagesize", defaultValue = "10") int pagesize);


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
	 * 保存列表模型
	 *
	 * @param formModel 表单模型
	 */
	@ApiOperation(value = "保存列表模型", position = 3)
	@PostMapping("/save")
	IdEntity saveFormModel(@RequestBody FormModel formModel);


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
}
