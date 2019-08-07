package tech.ascs.icity.iform.function;

import tech.ascs.icity.iform.model.ExportListFunction;
import tech.ascs.icity.iform.model.ListModelEntity;

import java.util.List;
import java.util.Map;

/**
 * @author renjie
 * @since 0.7.3
 **/
public interface HeaderBuild {

    List<String> buildHeader(ListModelEntity entity, ExportListFunction function, Map<String, Object> queryParams);

}
