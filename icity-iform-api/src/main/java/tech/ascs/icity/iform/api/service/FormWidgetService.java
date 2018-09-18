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

import tech.ascs.icity.iform.api.model.FormWidget;

@Api(description = "表单控件数据操作接口")
@RequestMapping("/formWidget")
public interface FormWidgetService {
	
    @ApiOperation("新增表单控件数据记录")
	@PostMapping
	public void add(@RequestBody FormWidget formWidget);

    @ApiOperation("更新表单控件数据记录")
    @PutMapping()
	public void update(@RequestBody FormWidget formWidget);
    
    @ApiOperation(value = "删除表单控件数据记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "表单控件数据表id", required = true, dataType = "String")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name="id") String id) ;
    
    @ApiOperation("获取表单控件数据记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "表单控件数据表id", required = true, dataType = "String")
	@GetMapping("/{id}")
    public FormWidget getById(@PathVariable(name="id") String id);

    @ApiOperation(value = "获取所有表单控件数据记录信息")
	@GetMapping("/list")
	public List<FormWidget> list();
}
