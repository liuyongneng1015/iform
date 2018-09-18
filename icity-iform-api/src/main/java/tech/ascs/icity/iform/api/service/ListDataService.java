package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tech.ascs.icity.iform.api.model.ListData;
import tech.ascs.icity.model.Page;

@Api(description = "列表建模数据操作接口")
@RequestMapping("/listData")
public interface ListDataService {
	
    @ApiOperation("新增列表建模数据记录")
	@PostMapping
	public void add(@RequestBody ListData ListData);

    @ApiOperation("更新列表建模数据记录")
    @PutMapping()
	public void update(@RequestBody ListData ListData);
    
    @ApiOperation(value = "删除列表建模数据记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "列表建模数据表id", required = true, dataType = "String")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name="id") String id) ;
    
    @ApiOperation("获取列表建模数据记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "列表建模数据表id", required = true, dataType = "String")
	@GetMapping("/{id}")
    public ListData getById(@PathVariable(name="id") String id);

    @ApiOperation(value = "获取所有列表建模数据记录信息")
	@GetMapping("/list")
	public List<ListData> list();
    
    @ApiOperation(value = "根据列表名查询列表信息")
    @ApiImplicitParams({
    @ApiImplicitParam(paramType="query", name = "name", value = "name", required = false, dataType = "String"),
    @ApiImplicitParam(paramType="query", name = "page", value = "页码（默认为1）", required = false),
    @ApiImplicitParam(paramType="query", name = "pageSize", value = "每页记录数（默认为10）", required = false)
    })
    @GetMapping()
    public Page<ListData> findByName(@RequestParam(required=false)  String name,
    		@RequestParam(name="page", defaultValue = "1") int page,
    		@RequestParam(name="pageSize", defaultValue = "10") int pageSize) ;
}
