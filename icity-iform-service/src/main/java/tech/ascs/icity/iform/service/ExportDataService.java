package tech.ascs.icity.iform.service;

import org.springframework.core.io.Resource;
import tech.ascs.icity.iform.api.model.FormDataSaveInstance;
import tech.ascs.icity.iform.model.ExportListFunction;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;

import java.util.List;
import java.util.Map;

/**
 * @author renjie
 * @since 0.7.3
 **/
public interface ExportDataService {

    Resource exportData(ListModelEntity listEntity, ExportListFunction exportFunction, List<FormDataSaveInstance> datas, Map<String, Object> queryParams);

    List<ItemModelEntity> eachHasColumnItemModel(List<ItemModelEntity> entity);

    Resource exportTemplate(ListModelEntity listModel);
}
