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

import tech.ascs.icity.iform.api.model.ColumnData;
import tech.ascs.icity.iform.api.model.TabInfo;
import tech.ascs.icity.model.Page;

@RequestMapping("/tabInfo")
@Api(description = "数据建模表信息操作接口")
public interface TabInfoService {
	
	    @ApiOperation(value = "获取所有动态表的结构信息")
		@GetMapping("/list")
		public List<TabInfo> list();

	    @ApiOperation("新增动态表记录")
		@PostMapping
		public void add(@RequestBody TabInfo tabInfo);
	    
	    @ApiOperation(value = "获取指定动态表的所有列信息")
	    @ApiImplicitParam(paramType="path", name = "tabInfoId", value = "动态表id", required = true, dataType = "String")
		@GetMapping("/columnlist/{tabInfoId}")
	    public List<ColumnData> list(@PathVariable(name="tabInfoId") String tabInfoId) ;
	    
	    
	    
	    @ApiOperation("更新动态表")
	    @PutMapping
		public void update(@RequestBody TabInfo tabInfo);
	    
	    
	    @ApiOperation(value = "删除动态表")
	    @ApiImplicitParam(paramType="path", name = "id", value = "动态表id", required = true, dataType = "String")
	    @DeleteMapping("/{id}")
	    public void delete(@PathVariable(name="id") String id) ;
	    
	    @ApiOperation(value = "根据表名称查询表信息")
	    @ApiImplicitParam(paramType="path", name = "tabName", value = "tabName", required = true, dataType = "String")
	    @GetMapping("/{tabName}")
	    public TabInfo findByTabName(@PathVariable(name="tabName") String tabName) ;
	    
	    @ApiOperation(value = "根据表名称和同步状态查询表信息")
	    @ApiImplicitParams({
	    @ApiImplicitParam(paramType="query", name = "tabName", value = "tabName", required = false, dataType = "String"),
	    @ApiImplicitParam(paramType="query", name = "synFlag", value = "synFlag", required = false, dataType = "Boolean"),
	    @ApiImplicitParam(paramType="query", name = "page", value = "页码（默认为1）", required = false),
	    @ApiImplicitParam(paramType="query", name = "pagesize", value = "每页记录数（默认为10）", required = false)
	    })
	    @GetMapping()
	    public Page<TabInfo> findByTabNameAndSynFlag(@RequestParam(required=false)  String tabName,@RequestParam(required=false) Boolean synFlag,
	    		@RequestParam(name="page", defaultValue = "1") int page,@RequestParam(name="pagesize", defaultValue = "10") int pagesize) ;

}
