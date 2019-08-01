package tech.ascs.icity.iform.service.impl;

import com.itextpdf.text.DocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.model.FormDataSaveInstance;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.api.model.ReferenceDataInstance;
import tech.ascs.icity.iform.api.model.export.ExportControl;
import tech.ascs.icity.iform.model.ExportListFunction;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.iform.service.ExportDataService;
import tech.ascs.icity.iform.utils.ExportUtils;
import tech.ascs.icity.iform.utils.PdfBuilderUtils;
import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author renjie
 * @since 0.7.3
 **/
@Service
public class ExportDataServiceImpl implements ExportDataService {

    @Override
    public Resource exportData(ListModelEntity entity, ExportListFunction function, List<FormDataSaveInstance> datas, Map<String, Object> queryParams) {

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

        switch (function.getFormat()) {
            case Excel:
                return exportExcel(entity.getName(), header, exportDatas);
            case PDF:
                return exportPdf(entity.getName(), header, exportDatas);
            default:
                return exportExcel(entity.getName(), header, exportDatas);
        }
    }

    private Resource exportPdf(String title, List<String> header, List<List<Object>> exportDatas) {
        try {
            List<List<String>> stringDatas = exportDatas.stream().map(list -> list.stream().map(Objects::toString).collect(Collectors.toList())).collect(Collectors.toList());;
            PdfBuilderUtils builderUtils = new PdfBuilderUtils();
            builderUtils.setTitle(title);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            builderUtils.buildTablePdf(header, stringDatas, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException | DocumentException e) {
            throw new ICityException(e);
        }
    }

    /**
     * 导出excel类型的资源
     * @return 返回Excel的Resource
     */
    private Resource exportExcel(String sheetName, List<String> header, List<List<Object>> exportDatas ) {


        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);

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
            return ((List) value).stream().filter(Objects::nonNull).map(this::objectToString).collect(Collectors.joining(","));
        }
        return value;
    }

    private String objectToString(Object value) {
        if (FileUploadModel.class.isAssignableFrom(value.getClass())) {
            return ((FileUploadModel) value).getUrl();
        } else {
            return Objects.toString(value);
        }
    }


    private static class ExportHeaderFactory {

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

        private static HeaderBuild ALL_HEADER_BUILD = (entity, func, queryParams) ->
                eachItemEntity(entity.getMasterForm().getItems()).stream()
                        .filter(item -> item.getType().hasContext())
                        .sorted(Comparator.comparing(ItemModelEntity::getOrderNo))
                        .map(BaseEntity::getName).collect(Collectors.toList());

        private static HeaderBuild LIST_HEADER_BUILD = (entity, function, queryParams) -> entity.getDisplayItems().stream().sorted(Comparator.comparing(ItemModelEntity::getOrderNo)).map(ItemModelEntity::getName).collect(Collectors.toList());

        private static HeaderBuild BACK_HEADER_BUILD = (entity, function, queryParams) -> {
            Map<String, String> mapping = eachItemEntity(entity.getMasterForm().getItems()).stream().distinct().collect(Collectors.toMap(ItemModelEntity::getId, ItemModelEntity::getName));
            return Stream.of(function.getCustomExport().split(","))
                    .map(mapping::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };

        private static HeaderBuild FRONT_HEADER_BUILD = (entity, function, queryParams) -> {
            Map<String, String> mapping = eachItemEntity(entity.getMasterForm().getItems()).stream().distinct().collect(Collectors.toMap(ItemModelEntity::getId, ItemModelEntity::getName));
            return findFrontItemIds(queryParams).stream()
                    .map(mapping::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };

        private static List<ItemModelEntity> eachItemEntity(List<ItemModelEntity> entitys) {
            List<ItemModelEntity> result = new ArrayList<>();
            for (ItemModelEntity entity : entitys) {
                for (Field field : entity.getClass().getDeclaredFields()) {
                    // 处理 List<ItemModelEntity> 的Field 执行递归
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        Type type = field.getGenericType();
                        if (type instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) type;
                            if (ItemModelEntity.class.isAssignableFrom((Class<?>) parameterizedType.getActualTypeArguments()[0])) {
                                // 递归遍历
                                List<ItemModelEntity> modelEntities = eachItemEntity(getFieldValue(field, entity, List.class));
                                if (modelEntities == null || modelEntities.size() == 0) {
                                    continue;
                                }
                                result.addAll(modelEntities);
                            }
                        }
                    }
                }
                result.add(entity);
            }

            return result;
        }

        private static Collection<String> findFrontItemIds (Map<String, Object> params) {
            final String key = "exportColumnIds";
            if (params == null || !params.containsKey(key)){
                return Collections.emptyList();
            }
            Object obj = params.get(key);
            if (Collection.class.isAssignableFrom(obj.getClass())){
                return (Collection<String>) obj;
            }else {
                return Arrays.asList(Objects.toString(obj).split(","));
            }
        }


        @SuppressWarnings("unchecked")
        private static <T> T getFieldValue(Field field, Object obj, Class<T> clzz) {
            try {
                field.setAccessible(true);
                return (T) field.get(obj);
            } catch (IllegalAccessException e) {
                throw new ICityException(e);
            }
        }



    }

}
