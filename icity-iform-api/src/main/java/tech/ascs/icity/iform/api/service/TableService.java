package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tech.ascs.icity.iform.api.model.Table;
import tech.ascs.icity.model.Page;

@Api(description = "数据表操作接口")
@RequestMapping("/table")
public interface TableService {
	
    @ApiOperation("新增数据记录")
	@PostMapping
	public void add(@RequestBody Table table);

    @ApiOperation("更新数据记录")
    @PutMapping()
	public void update(@RequestBody Table table);
    
    @ApiOperation(value = "删除数据记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "数据表id", required = true, dataType = "String")
    @DeleteMapping("/{tableName}/{id}")
    public void delete(@PathVariable(name="tableName") String tableName,@PathVariable(name="id") String id) ;
    
    @ApiOperation("获取数据记录")
//    @ApiImplicitParam(paramType="path", name = "id", value = "数据表id", required = true, dataType = "String")
    @ApiImplicitParams({
    @ApiImplicitParam(paramType="path", name = "listDataId", value = "列表id", required = false, dataType = "String"),
    @ApiImplicitParam(paramType="path", name = "tableName", value = "数据表名称", required = true, dataType = "String"),
    @ApiImplicitParam(paramType="path", name = "id", value = "数据表id", required = true, dataType = "String"),
    })
	@GetMapping("/{listDataId}/{tableName}/{id}")
    public Object getByTableNameAndId(@PathVariable(name="listDataId") String listDataId,@PathVariable(name="tableName") String tableName,@PathVariable(name="id") String id);

    @ApiOperation(value = "获取指定表的数据记录(带分页)")
    @ApiImplicitParams({
    @ApiImplicitParam(paramType="query", name = "listDataId", value = "listDataId", required = false, dataType = "String"),
    @ApiImplicitParam(paramType="query", name = "tableName", value = "tableName", required = true, dataType = "String"),
    @ApiImplicitParam(paramType="query", name = "page", value = "页码（默认为1）", required = false),
    @ApiImplicitParam(paramType="query", name = "pageSize", value = "每页记录数（默认为10）", required = false)
    })
    @GetMapping()
    public Page<Object> findByTableName(@RequestParam(name="listDataId") int listDataId,@RequestParam(required=true)  String tableName,
    		@RequestParam(name="page", defaultValue = "1") int page,
    		@RequestParam(name="pageSize", defaultValue = "10") int pageSize) ;
}
