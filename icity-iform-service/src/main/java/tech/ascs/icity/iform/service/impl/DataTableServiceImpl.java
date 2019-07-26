package tech.ascs.icity.iform.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.iform.api.model.DataTable;
import tech.ascs.icity.iform.bean.TableMetaBean;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.iform.service.DataTableBuildService;
import tech.ascs.icity.iform.utils.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataTableService实现类
 *
 * @author renjie
 * @since 0.7.2
 **/
@Service
public class DataTableServiceImpl implements DataTableBuildService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTableServiceImpl.class);

    private final DataModelService dataModelService;

    public DataTableServiceImpl(DataModelService dataModelService) {
        this.dataModelService = dataModelService;
    }

    @Override
    public List<DataTable> findNotBuildTable() {
        Set<String> existsTables = dataModelService.findAll()
                .parallelStream()
                .map(entity -> concat(entity.getPrefix(), entity.getTableName()))
                .collect(Collectors.toSet());
        try {
            return TableUtils.findAllTables()
                    .stream()
                    .filter(bean -> !existsTables.contains(bean.getTableName()))
                    .map(this::toDataTable)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            LOGGER.error("获取数据库表信息出现错误: {}", e.getMessage(), e);
            throw new ICityException(500, 999, "获取数据库表信息失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean buildDataModelByTableList(String applicationId, List<DataTable> dataTables) {
        Map<String, DataTable> buildTables = dataTables.parallelStream()
                .collect(Collectors.toMap(DataTable::getTableName, this::callThis));
        try {
            //生成DataModelEntity模型
            List<DataModelEntity> dataModelEntities = TableUtils.findAllTables()
                    .parallelStream()
                    .filter(bean -> buildTables.containsKey(bean.getTableName()))
                    .map(tableMetaBean -> tableMetaBean.buildSimpleDataModel(applicationId))
                    .peek(entity -> entity.setDescription(buildTables.get(entity.getTableName()).getDescribe()))
                    .collect(Collectors.toList());
            //生成ColumnModelEntity模型, 并且为DataModelEntity设置列模型
            for (DataModelEntity entity : dataModelEntities) {
                List<ColumnModelEntity> columns = TableUtils.findTableColMetaData(entity.getTableName())
                        .parallelStream()
                        .map(bean -> bean.toEntity(entity))
                        .collect(Collectors.toList());
                entity.setColumns(columns);
            }
            dataModelService.save(dataModelEntities.toArray(new DataModelEntity[0]));
            return true;
        } catch (SQLException e) {
            LOGGER.error("创建数据库模型失败: {}", e.getMessage(), e);
            throw new ICityException(500, 999, "创建数据库模型失败");
        }
    }

    private DataTable toDataTable(TableMetaBean tableMetaBean) {
        DataTable dataTable = new DataTable();
        dataTable.setTableName(tableMetaBean.getTableName());
        dataTable.setDataModelType(DataModelType.Single);
        return dataTable;
    }

    private <T> T callThis(T obj) {
        return obj;
    }

    private String concat(String... args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args){
            builder.append(Optional.ofNullable(arg).orElse(""));
        }
        return builder.toString();
    }
}
