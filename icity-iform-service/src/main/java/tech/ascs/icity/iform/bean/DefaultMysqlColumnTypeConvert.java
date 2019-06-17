package tech.ascs.icity.iform.bean;

import tech.ascs.icity.iform.api.model.ColumnType;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供由数据库类型到jpa数据类型的转换
 * @author renjie
 * @since 0.7.2
 **/
public class DefaultMysqlColumnTypeConvert implements ColumnTypeConvert {

    private static final Map<String, ColumnType> mapping;

    static {
        mapping = new HashMap<>();
        mapping.put("DOUBLE", ColumnType.Double);
        mapping.put("BIGINT", ColumnType.Long);
        mapping.put("DATETIME", ColumnType.Timestamp);
        mapping.put("FLOAT", ColumnType.Float);
        mapping.put("VARCHAR", ColumnType.String);
        mapping.put("LONGTEXT", ColumnType.Text);
        mapping.put("TIME", ColumnType.Time);
        mapping.put("BIT", ColumnType.Boolean);
        mapping.put("DATE", ColumnType.Date);
    }

    @Override
    public ColumnType convert(String value) {
        return mapping.getOrDefault(value, ColumnType.String);
    }
}
