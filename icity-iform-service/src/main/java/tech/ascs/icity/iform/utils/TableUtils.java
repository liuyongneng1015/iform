package tech.ascs.icity.iform.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import tech.ascs.icity.iform.bean.ColMetaBean;
import tech.ascs.icity.iform.bean.TableMetaBean;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author renjie
 * @since 1.0.0
 **/
@Component
public class TableUtils implements ApplicationContextAware {

    private static DataSource dataSource;

    public static List<TableMetaBean> findAllTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tableResult = metaData.getTables(connection.getCatalog(), "%", "%", new String[]{"TABLE"})) {
                List<TableMetaBean> result = new ArrayList<>();
                while (tableResult.next()) {
                    result.add(TableMetaBean.from(tableResult));
                }
                return result;
            }
        }
    }


    /**
     * 生成
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static TableMetaBean findTableMetaData(String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tableResult = metaData.getTables(connection.getCatalog(), "%", tableName, new String[]{"TABLE"})) {
                if (tableResult.next()) {
                    return TableMetaBean.from(tableResult);
                } else {
                    throw new RuntimeException("表: " + tableName + ",在数据库:" + connection.getCatalog() + "中不存在");
                }
            }
        }
    }


    /**
     * @param tableName 生成元数据的表名
     * @return 返回元数据bean的列表
     */
    public static List<ColMetaBean> findTableColMetaData(String tableName) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rsColimns = metaData.getColumns(connection.getCatalog(), "%", tableName, "%")) {
                List<ColMetaBean> colMetaBeans = new ArrayList<>();
                while (rsColimns.next()) {
                    colMetaBeans.add(ColMetaBean.from(rsColimns));
                }
                return colMetaBeans;
            }
        }
    }

    private static void printMetaCol(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount();
        List<String> colNames = Stream.iterate(1, i -> i + 1)
                .limit(colCount)
                .map(index -> {
                    try {
                        return resultSetMetaData.getColumnName(index);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        while (resultSet.next()) {
            String msg = colNames.stream()
                    .map(colName -> {
                        try {
                            return colName + ":" + resultSet.getString(colName);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));

            System.out.println(msg);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        dataSource = applicationContext.getBean(DataSource.class);
    }
}
