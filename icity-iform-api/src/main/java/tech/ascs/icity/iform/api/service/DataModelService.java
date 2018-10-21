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
import tech.ascs.icity.iform.api.model.DataModel;
import tech.ascs.icity.iform.api.model.DataModelInfo;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

@RequestMapping("/data-models")
public interface DataModelService {

	/**
	 * 获取所有数据模型
	 * 
	 * @param name （可选）模型名称
	 * @return
	 */
	@ApiOperation(value = "获取所有数据模型", position = 0)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "模型名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "sync", value = "同步状态（0 - 未同步； 1 - 已同步）", required = false)
	})
	@GetMapping
	List<DataModel> list(@RequestParam(name = "name", required = false) String name, @RequestParam(name = "sync", required = false) String sync);


	/**
	 * 获取数据模型分页数据
	 * 
	 * @param name （可选）模型名称
	 * @param page （可选）页码，默认为1
	 * @param pagesize （可选）每页记录数，默认为10
	 * @return
	 */
	@ApiOperation(value = "获取数据模型分页数据", position = 1)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "name", value = "模型名称", required = false),
		@ApiImplicitParam(paramType = "query", name = "sync", value = "同步状态（0 - 未同步； 1 - 已同步）", required = false),
		@ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
		@ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10")
	})
	@GetMapping("/page")
	Page<DataModel> page(
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "sync", required = false) String sync,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name="pagesize", defaultValue = "10") int pagesize);


	/**
	 * 获取主数据模型
	 * 
	 * <p>获取所有类型为主表或单表的数据模型，用于设计从表时选择主表
	 * 
	 * @return
	 */
	@ApiOperation(value = "获取主数据模型", notes = "获取所有类型为主表或单表的数据模型，用于设计从表时选择主表", position = 0)
	@GetMapping("/master-models")
	List<DataModelInfo> getMasterModels();


	/**
	 * 根据数据模型ID获取数据模型对象
	 * 
	 * @param id 数据模型ID（uuid）
	 * @return
	 */
	@ApiOperation(value = "根据数据模型ID获取数据模型对象", position = 2)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "id", value = "数据模型ID", required = true, dataType = "String")
	})
	@GetMapping("/{id}")
	DataModel get(@PathVariable(name="id") String id);


	/**
	  * 新建数据模型
	 * 
	 * @param formModel 数据模型
	 */
	@ApiOperation(value = "新建数据模型", position = 3)
	@PostMapping
	IdEntity createDataModel(@RequestBody DataModel formModel);


	/**
	 * 更新数据模型
	 * 
	 * @param id 数据模型ID
	 * @param formModel 数据模型
	 */
	@ApiOperation(value = "更新数据模型", position = 3)
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "path", name = "id", value = "数据模型ID", required = true, dataType = "String")
	})
	@PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	void updateDataModel(@PathVariable(name="id") String id, @RequestBody DataModel formModel);


	/**
	 * 同步数据模型
	 * 
	 * @param id 数据模型ID
	 */
	@ApiOperation(value = "同步数据模型", position = 6)
    @ApiImplicitParam(paramType = "path", name = "id", value = "数据模型ID", required = true, dataType = "String")
	@PostMapping("/{id}/sync")
	void syncDataModel(@PathVariable(name="id") String id);


	/**
	 * 删除流程
	 * 
	 * @param id 数据模型ID
	 */
	@ApiOperation(value = "删除数据模型", position = 6)
    @ApiImplicitParam(paramType = "path", name = "id", value = "数据模型ID", required = true, dataType = "String")
	@DeleteMapping("/{id}")
	void removeDataModel(@PathVariable(name="id") String id);
}
