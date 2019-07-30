package tech.ascs.icity.iform.service.impl;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.DefaultFunctionType;
import tech.ascs.icity.iform.api.model.FormDataSaveInstance;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.api.model.ReferenceDataInstance;
import tech.ascs.icity.iform.api.model.export.ExportControl;
import tech.ascs.icity.iform.model.ExportListFunction;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ListFunction;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.iform.service.ExportDataService;
import tech.ascs.icity.iform.service.FormInstanceServiceEx;
import tech.ascs.icity.iform.utils.ExportUtils;
import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author renjie
 * @since 0.7.3
 **/
@Service
public class ExportDataServiceImpl implements ExportDataService {

    @Autowired
    private FormInstanceServiceEx formInstanceService;


    @Override
    public Resource exportData(ListModelEntity listEntity, ExportListFunction exportFunction, List<FormDataSaveInstance> datas, Map<String, Object> queryParams) {
        switch (exportFunction.getFormat()) {
            case Excel:
                return exportExcel(listEntity, exportFunction, datas, queryParams);
            case PDF:
                return null;
            default:
                return exportExcel(listEntity, exportFunction, datas, queryParams);
        }
    }

    private Resource exportExcel(ListModelEntity entity, ExportListFunction function, List<FormDataSaveInstance> datas, Map<String, Object> queryParams) {
        ExportControl exportControl = function.getControl();

        List<String> header = ExportHeaderFactory.getInstance(exportControl).buildHeader(entity, function, queryParams);
        Map<String, String> refIdToNameMapping = entity.getMasterForm().getItems().stream().filter(item -> Objects.nonNull(item.getName())).collect(Collectors.toMap(JPAEntity::getId, BaseEntity::getName));

        List<List<Object>> exportDatas = datas.stream()
                .map(instance -> {
                    // 变换为 控件名称 -> 值的map
                    Map<String, Object> nameToValueMapping = instance.getItems().stream().collect(Collectors.toMap(ItemInstance::getItemName, this::findValue));
                    Map<String, Object> refMapping = instance.getReferenceData().stream().collect(Collectors.toMap(ref -> refIdToNameMapping.get(ref.getId()), this::findValue));
                    Map<String, Object> result = new HashMap<>(nameToValueMapping);
                    result.putAll(refMapping);
                    return result;
                })
                .map(data -> header.stream().map(headerName -> data.getOrDefault(headerName, null)).collect(Collectors.toList()))
                .collect(Collectors.toList());

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(entity.getName());

        ExportUtils.outputHeaders(header.toArray(new String[0]), sheet);
        ExportUtils.outputColumn(exportDatas, sheet, 1);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            wb.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayResource(os.toByteArray());
    }

    private interface HeaderBuild {

        List<String> buildHeader(ListModelEntity entity, ExportListFunction function, Map<String, Object> queryParams);

    }

    private Object findValue(ItemInstance instance) {
        return Optional.ofNullable(instance).map(ItemInstance::getDisplayValue).map(this::valueConvert).orElse("");
    }

    private Object findValue(ReferenceDataInstance instance) {
        return Optional.ofNullable(instance).map(ReferenceDataInstance::getDisplayValue).map(this::valueConvert).orElse("");
    }

    @SuppressWarnings("unchecked")
    private Object valueConvert(Object value) {
        if (value instanceof List) {
            return ((List) value).stream().filter(Objects::nonNull).map(Objects::toString).collect(Collectors.joining(","));
        }
        return value;
    }


    private static class ExportHeaderFactory {

        //TODO 存在问题, 当控件有嵌套的时候, 无法正确获得
        private static HeaderBuild ALL_HEADER_BUILD = (entity, func, queryParams) -> entity.getMasterForm().getItems().stream().filter(item -> item.getType().hasContext()).sorted(Comparator.comparing(ItemModelEntity::getOrderNo)).map(BaseEntity::getName).collect(Collectors.toList());

        private static HeaderBuild LIST_HEADER_BUILD = (entity, function, queryParams) -> entity.getDisplayItems().stream().sorted(Comparator.comparing(ItemModelEntity::getOrderNo)).map(ItemModelEntity::getName).collect(Collectors.toList());

        private static HeaderBuild BACK_HEADER_BUILD = (entity, function, queryParams) -> {
            Map<String, String> mapping = entity.getMasterForm().getItems().stream().collect(Collectors.toMap(ItemModelEntity::getId, ItemModelEntity::getName));
            return Stream.of(function.getCustomExport().split(","))
                    .map(mapping::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };

        private static HeaderBuild FRONT_HEADER_BUILD = (entity, function, queryParams) -> {
            // TODO 更详细的检查机制
            Map<String, String> mapping = entity.getMasterForm().getItems().stream().collect(Collectors.toMap(ItemModelEntity::getId, ItemModelEntity::getName));
            return ((List<String>) queryParams.get("exportColumnIds")).stream()
                    .map(mapping::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };

        public static HeaderBuild getInstance(ExportControl control) {
            switch (control) {
                case All:
                    return ALL_HEADER_BUILD;
                case List:
                    return LIST_HEADER_BUILD;
                case BackCustom:
                    return BACK_HEADER_BUILD;
                case FrontCustom:
                    return FRONT_HEADER_BUILD;
                default:
                    return ALL_HEADER_BUILD;
            }
        }

    }

}
