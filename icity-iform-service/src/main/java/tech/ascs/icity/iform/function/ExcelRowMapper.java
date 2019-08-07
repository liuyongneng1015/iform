package tech.ascs.icity.iform.function;

import java.util.List;

/**
 * @author renjie
 * @since 0.7.3
 **/
public interface ExcelRowMapper<T> {

    T mapRow(List<Object> data, List<String> header, int rowNum);

}
