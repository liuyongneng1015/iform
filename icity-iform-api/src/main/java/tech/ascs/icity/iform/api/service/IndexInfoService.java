package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import tech.ascs.icity.iform.api.model.IndexInfo;

@Api(description = "索引表接口")
@RequestMapping("/tabInfo")
public interface IndexInfoService {
	
    @ApiOperation("新增索引记录")
	@PostMapping
	public void add(@RequestBody IndexInfo indexInfo);

    @ApiOperation("更新索引记录")
    @PutMapping()
	public void update(@RequestBody IndexInfo indexInfo);
    
    @ApiOperation(value = "删除索引记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "索引表id", required = true, dataType = "String")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name="id") String id) ;
    
    @ApiOperation("获取索引记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "索引表id", required = true, dataType = "String")
	@GetMapping("/{id}")
    public IndexInfo getById(@PathVariable(name="id") String id);
    
    @ApiOperation(value = "根据动态表名称查询索引表信息")
    @ApiImplicitParam(paramType="path", name = "tabName", value = "tabName", required = true, dataType = "String")
    @GetMapping("/query/{tabName}")
    public List<IndexInfo> findByTabName(@PathVariable(name="tabName") String tabName) ;

}
