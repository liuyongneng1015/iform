package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.DataTable;

import java.util.List;

/**
 * 数据表生成模型相关服务
 *
 * @author renjie
 * @since 0.7.2
 **/
public interface DataTableBuildService {

    List<DataTable> findNotBuildTable();

    boolean buildDataModelByTableList(String applicationId, List<DataTable> dataTables);
}
