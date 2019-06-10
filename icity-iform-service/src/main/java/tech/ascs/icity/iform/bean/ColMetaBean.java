package tech.ascs.icity.iform.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.DataModelEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * DECIMAL_DIGITS  : 小数位数
 * COLUMN_SIZE : 长度
 * COLUMN_NAME : 列名
 * TYPE_NAME : 类型名称
 * NULLABLE : 是否可以为空
 * REMARKS : 备注
 * ORDINAL_POSITION : 位置
 *
 * @author renjie
 * @since 1.0.0
 */
@Builder
@AllArgsConstructor
@Data
public class ColMetaBean implements Comparable<ColMetaBean> {

    /**
     * 列名
     */
    private static final String COLUMN_NAME = "COLUMN_NAME";
    /**
     * 列长度
     */
    private static final String COLUMN_SIZE = "COLUMN_SIZE";
    /**
     * 类型名称
     */
    private static final String TYPE_NAME = "TYPE_NAME";
    /**
     * 备注
     */
    private static final String REMARKS = "REMARKS";
    /**
     * 字段位置
     */
    private static final String ORDINAL_POSITION = "ORDINAL_POSITION";
    /**
     * 小数位数
     */
    private static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
    /**
     * 是否可以为null
     */
    private static final String NULLABLE = "NULLABLE";

    private static ColumnTypeConvert TYPE_CONVERT = new DefaultColumnTypeConvert();

    private String columnName;
    private int columnSize;
    private String typeName;
    private String remarks;
    private int ordinalPosition;
    private int decimalDigits;
    private boolean nullable;
    private boolean key;

    public static ColMetaBean from(ResultSet resultSet) throws SQLException {
        ColMetaBean bean = ColMetaBean.builder()
                .columnName(resultSet.getString(COLUMN_NAME))
                .columnSize(resultSet.getInt(COLUMN_SIZE))
                .typeName(resultSet.getString(TYPE_NAME))
                .remarks(resultSet.getString(REMARKS))
                .ordinalPosition(resultSet.getInt(ORDINAL_POSITION))
                .decimalDigits(resultSet.getInt(DECIMAL_DIGITS))
                .nullable(resultSet.getBoolean(NULLABLE))
                .build();
        if ("id".equals(bean.getColumnName())) {
            bean.setKey(true);
        }
        return bean;
    }

    public ColumnModelEntity toEntity(DataModelEntity dataModelEntity) {
        ColumnModelEntity entity = new ColumnModelEntity();
        entity.setDataModel(dataModelEntity);
        entity.setColumnName(this.getColumnName());
        entity.setDescription(this.getColumnName());
        entity.setDataType(ColMetaBean.getTypeConvert().convert(this.getTypeName()));
        entity.setLength(this.getColumnSize());
        entity.setPrecision(0);
        entity.setScale(this.getDecimalDigits());
        entity.setNotNull(!this.isNullable());
        entity.setKey(this.isKey());
        return entity;
    }

    public static ColumnTypeConvert getTypeConvert() {
        return TYPE_CONVERT;
    }

    public static void setTypeConvert(ColumnTypeConvert typeConvert) {
        TYPE_CONVERT = typeConvert;
    }

    @Override
    public int compareTo(ColMetaBean o) {
        if (Objects.isNull(o)) {
            return 1;
        }
        return this.getOrdinalPosition() - o.getOrdinalPosition();
    }
}
