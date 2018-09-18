/*package tech.ascs.icity.iform.api.service;

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

import tech.ascs.icity.iform.api.model.ListColData;

@Api(description = "列表建模详细数据操作接口")
@RequestMapping("/listColData")
public interface ListColDataService {
	
    @ApiOperation("新增列表建模详细数据记录")
	@PostMapping
	public void add(@RequestBody ListColData listColData);

    @ApiOperation("更新列表建模详细数据记录")
    @PutMapping()
	public void update(@RequestBody ListColData listColData);
    
    @ApiOperation(value = "删除列表建模详细数据记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "列表建模详细数据表id", required = true, dataType = "String")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name="id") String id) ;
    
    @ApiOperation("获取列表建模详细数据记录")
    @ApiImplicitParam(paramType="path", name = "id", value = "列表建模详细数据表id", required = true, dataType = "String")
	@GetMapping("/{id}")
    public ListColData getById(@PathVariable(name="id") String id);

    @ApiOperation(value = "获取所有列表建模详细数据记录信息")
	@GetMapping("/list")
	public List<ListColData> list();
}
*/