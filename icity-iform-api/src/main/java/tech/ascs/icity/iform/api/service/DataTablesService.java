package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.api.model.DataTable;

import java.util.ArrayList;
import java.util.List;

/**
 * 表相关操作, 获取未生成数据模型的表, 和申请生成数据模型的表的操作
 *
 * @author renjie
 * @since 0.7.2
 **/
@RequestMapping("/data-tables")
@RestController
public interface DataTablesService {

    /**
     * 获取数据库未生成数据模型的表
     *
     * @return 数据库的表基础信息列表
     */
    @ApiOperation("获取数据库中为建模的表")
    @GetMapping("")
    List<DataTable> findDataTableList();

    /**
     * 根据所给的表来生成表模型
     *
     * @param dataTables    需要生成的表的基本信息列表
     * @param applicationId 生成模型的应用ip
     * @return 返回生成结果
     */
    @ApiOperation("生成表模型")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "applicationId", value = "应用id"),
            @ApiImplicitParam(paramType = "body", name = "dataTables")
    })
    @PostMapping(value = "/{applicationId}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    boolean buildDataModelByTables(@PathVariable("applicationId") String applicationId, @RequestBody ArrayList<DataTable> dataTables);
}
