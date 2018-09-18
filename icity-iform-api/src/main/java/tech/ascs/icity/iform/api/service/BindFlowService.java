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

import tech.ascs.icity.iform.api.model.BindFlow;

@Api(description = "绑定流程操作接口")
@RequestMapping("/bindFlow")
public interface BindFlowService {
	
    @ApiOperation("新增绑定流程记录")
	@PostMapping
	public void add(@RequestBody BindFlow[] bindFlow);

    @ApiOperation("更新绑定流程记录")
    @PutMapping()
	public void update(@RequestBody BindFlow[] bindFlow);
    
    @ApiOperation(value = "删除绑定流程记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "绑定流程表id", required = true, dataType = "String")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name="id") String id) ;
    
    @ApiOperation("获取绑定流程记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "绑定流程表id", required = true, dataType = "String")
	@GetMapping("/{id}")
    public BindFlow getById(@PathVariable(name="id") String id);

    @ApiOperation(value = "获取所有绑定流程记录信息")
	@GetMapping("/list")
	public List<BindFlow> list();
}
