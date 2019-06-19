package tech.ascs.icity.iform.bean;

import tech.ascs.icity.iform.api.model.ColumnType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author renjie
 * @since 1.0.0
 **/
public class DefaultPostgresColumnTypeConvert implements ColumnTypeConvert {

    private static final Map<String, ColumnType> mapping;

    static {
        mapping = new HashMap<>();
        mapping.put("numeric", ColumnType.Double);
        mapping.put("int8", ColumnType.Long);
        mapping.put("timestamp", ColumnType.Timestamp);
        mapping.put("float8", ColumnType.Float);
        mapping.put("varchar", ColumnType.String);
        mapping.put("text", ColumnType.Text);
        mapping.put("time", ColumnType.Time);
        mapping.put("bool", ColumnType.Boolean);
        mapping.put("date", ColumnType.Date);
    }

    @Override
    public ColumnType convert(String value) {
        return mapping.getOrDefault(value, ColumnType.String);
    }
}
