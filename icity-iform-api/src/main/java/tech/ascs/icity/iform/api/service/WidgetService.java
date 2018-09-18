package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import tech.ascs.icity.iform.api.model.Widget;

@Api(description = "表单控件操作接口")
@RequestMapping("/widget")
public interface WidgetService {
	
    @ApiOperation("新增表单控件记录")
	@PostMapping
	public void add(@RequestBody Widget widget);

    @ApiOperation("更新表单控件记录")
    @PutMapping()
	public void update(@RequestBody Widget widget);
    
    @ApiOperation(value = "删除表单控件记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "表单控件表id", required = true, dataType = "String")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name="id") String id) ;
    
    @ApiOperation("获取表单控件记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "表单控件表id", required = true, dataType = "String")
	@GetMapping("/{id}")
    public Widget getById(@PathVariable(name="id") String id);
    
    @ApiOperation(value = "获取所有控件记录信息")
	@GetMapping("/list")
	public List<Widget> list();

    @ApiOperation(value = "获取所有控件的属性信息")
	@GetMapping("/allStruct")
	public Map getAllWidgetStruct();
}
