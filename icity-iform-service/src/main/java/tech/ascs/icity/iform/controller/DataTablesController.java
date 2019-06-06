package tech.ascs.icity.iform.controller;

import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.DataTable;
import tech.ascs.icity.iform.api.service.DataTablesService;

import java.util.List;

/**
 * @author renjie
 * @since 0.7.2
 **/
@RestController
public class DataTablesController implements DataTablesService {


    @Override
    public List<DataTable> findDataTableList() {
        throw new ICityException(500, 999, "暂时没有实现");
    }

    @Override
    public boolean buildDataModelByTables(List<DataTable> dataTables) {
        throw new ICityException(500, 999, "暂时没有实现");
    }
}
