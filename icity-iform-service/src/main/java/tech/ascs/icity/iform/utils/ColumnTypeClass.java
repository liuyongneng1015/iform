package tech.ascs.icity.iform.utils;


import tech.ascs.icity.iform.api.model.ColumnType;

/**
 * @author renjie
 * @since 0.7.3
 **/
public enum ColumnTypeClass {

    /**
     * 整型 32位
     */
    Integer(ColumnType.Integer, Integer.class),

    /**
     * 长整型 64位
     */
    Long(ColumnType.Long, java.lang.Long.class),

    /**
     * 单精度浮点型
     */
    Float(ColumnType.Float,java.lang.Float.class),

    /**
     * 双精度浮点型
     */
    Double(ColumnType.Double, java.lang.Double.class),

    /**
     * 字符串
     */
    String(ColumnType.String, java.lang.String.class),


    Text(ColumnType.Text, java.lang.String.class),

    Date(ColumnType.Date, java.util.Date.class),

    Time(ColumnType.Time, java.lang.String.class),

    Timestamp(ColumnType.Timestamp, java.util.Date.class),

    Boolean(ColumnType.Boolean, java.lang.Boolean.class);


    private ColumnType type;

    private Class<?> clazz;

    public ColumnType getType() {
        return type;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    private ColumnTypeClass(ColumnType type, Class<?> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    public static ColumnTypeClass valueOf(ColumnType type) {
        for (ColumnTypeClass t : values()) {
            if (t.type == type) {
                return t;
            }
        }
        return String;
    }

}
