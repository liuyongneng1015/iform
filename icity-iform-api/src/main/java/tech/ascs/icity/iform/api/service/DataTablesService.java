package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import tech.ascs.icity.iform.api.model.DataTable;

import java.util.List;

/**
 * 表相关操作, 获取未生成数据模型的表, 和申请生成数据模型的表的操作
 * @author renjie
 * @since 0.7.2
 **/
@Api(value = "数据库表相关接口", description = "获取数据库相关系统表, 来生成模型的借口")
@RequestMapping("/data-tables")
public interface DataTablesService {

    /**
     * 获取数据库未生成数据模型的表
     * @return 数据库的表基础信息列表
     */
    @ApiOperation("获取数据库中为建模的表")
    @GetMapping("")
    List<DataTable> findDataTableList();

    /**
     * 根据所给的表来生成表模型
     * @param dataTables 需要生成的表的基本信息列表
     * @return 返回生成结果
     */
    @ApiOperation("生成表模型")
    @PostMapping("")
    boolean buildDataModelByTables(List<DataTable> dataTables);
}
