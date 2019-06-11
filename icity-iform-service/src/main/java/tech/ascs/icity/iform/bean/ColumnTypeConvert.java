package tech.ascs.icity.iform.bean;

import tech.ascs.icity.iform.api.model.ColumnType;

/**
 * 提供由数据库类型到jpa数据类型的转换
 * @author renjie
 * @since 0.7.2
 **/
public interface ColumnTypeConvert {

    /**
     * 转换类型值, 由数据库的类转换为对应的jpa字段值
     * @param value 数据库类型值
     * @return 返回字段类型
     */
    ColumnType convert(String value);
}
