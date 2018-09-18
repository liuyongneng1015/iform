package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import tech.ascs.icity.iform.api.model.ColumnData;

@Api(description = "数据建模列数据操作接口")
@RequestMapping("/columnData")
public interface ColumnDataService {

	    @ApiOperation("新增字段数据")
		@PostMapping("/add")
		public void add(@RequestBody ColumnData columnData);
	    

	    @ApiOperation("更新字段数据表记录")
	    @PutMapping("/update")
		public void update(@RequestBody ColumnData columnData);
	    
	    
	    @ApiOperation(value = "删除字段数据表记录")
	    @ApiImplicitParam(paramType="path", name = "id", value = "字段数据表id", required = true, dataType = "String")
	    @DeleteMapping("/{id}")
	    public void delete(@PathVariable(name="id") String id) ;
}
