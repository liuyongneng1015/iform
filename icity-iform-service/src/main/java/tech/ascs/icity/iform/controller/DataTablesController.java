package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.DataTable;
import tech.ascs.icity.iform.api.service.DataTablesService;
import tech.ascs.icity.iform.service.DataTableBuildService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author renjie
 * @since 0.7.2
 **/
@Api(tags = "数据库表相关接口", value = "数据库表相关接口", description = "获取数据库相关系统表, 来生成模型的借口")
@RestController
public class DataTablesController implements DataTablesService {

    private final DataTableBuildService dataTableBuildService;

    public DataTablesController(DataTableBuildService dataTableBuildService) {
        this.dataTableBuildService = dataTableBuildService;
    }

    @Override
    public List<DataTable> findDataTableList() {
        return dataTableBuildService.findNotBuildTable();
    }

    @Override
    public boolean buildDataModelByTables(@PathVariable("applicationId") String applicationId, @RequestBody ArrayList<DataTable> dataTables) {
        return dataTableBuildService.buildDataModelByTableList(applicationId, dataTables);
    }
}
