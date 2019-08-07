package tech.ascs.icity.iform.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.api.model.FormDataSaveInstance;
import tech.ascs.icity.iform.model.ExportListFunction;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;

import java.io.IOException;
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

    void importData(ListModelEntity listModelEntity, MultipartFile file) throws IOException, InvalidFormatException;
}
