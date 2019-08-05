package tech.ascs.icity.iform.service.impl;

import com.google.common.collect.Sets;
import com.itextpdf.text.DocumentException;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.model.export.ExportControl;
import tech.ascs.icity.iform.function.ExcelRowMapper;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.ExportDataService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.iform.utils.*;
import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author renjie
 * @since 0.7.3
 **/
@Service
public class ExportDataServiceImpl implements ExportDataService {

    @Autowired
    private ItemModelService itemModelService;

    @Autowired
    private IFormSessionFactoryBuilder sessionFactoryBuilder;

    private ConversionService conversionService = new DefaultConversionService();

    private ExcelRowMapper<Map<String, Object>> mapExcelRowMapper = ((data, header, rowNum) -> {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            Object obj = null;
            if (data.size() > i) {
                obj = data.get(i);
            }
            map.put(header.get(i), obj);
        }
        return map;
    });


    @Override
    public Resource exportData(ListModelEntity entity, ExportListFunction function, List<FormDataSaveInstance> datas, Map<String, Object> queryParams) {

        ExportControl exportControl = function.getControl();

        List<String> header = ExportHeaderFactory.getInstance(exportControl).buildHeader(entity, function, queryParams);
        Map<String, String> refIdToNameMapping = entity.getMasterForm().getItems().stream().filter(item -> Objects.nonNull(item.getName())).collect(Collectors.toMap(JPAEntity::getId, BaseEntity::getName));

        List<List<Object>> exportDatas = datas.stream()
                .map(instance -> {
                    // 变换为 控件名称 -> 值的map
                    Map<String, Object> nameToValueMapping = instance.getItems().stream().filter(item -> item.getType().hasContext()).collect(Collectors.toMap(ItemInstance::getItemName, this::findValue));
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

    @Override
    public List<ItemModelEntity> eachHasColumnItemModel(List<ItemModelEntity> entitys) {
        return ItemModelHandlerUtils.eachItemEntity(entitys, Sets.newHashSet(ItemType.SubForm))
                .stream()
                .filter(item -> Objects.nonNull(item.getColumnModel()))
                // fix 修复遍历可能会出现重复item的问题, 这个重复指的是 对象地址重复, 即是完全相同的一个item
                .distinct()
                .sorted(Comparator.comparing(ItemModelEntity::getOrderNo))
                .collect(Collectors.toList());
    }

    @Override
    public Resource exportTemplate(ListModelEntity listModel) {
        List<ItemModelEntity> modelEntities = eachHasColumnItemModel(listModel.getMasterForm().getItems());
        List<String> header = new ArrayList<>();
        List<Object> data = new ArrayList<>();
        modelEntities.stream()
                .sorted(Comparator.comparing(ItemModelEntity::getOrderNo))
                .filter(ItemModelEntity::isTemplateSelected)
                .forEach(item -> {
                    header.add(item.getTemplateName());
                    data.add(item.getExampleData());
                });

        return exportExcel(listModel.getName(), header, Collections.singletonList(data), (style, font) -> {
            font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
            font.setItalic(true);
        });
    }

    @Override
    public void importData(ListModelEntity listModelEntity, MultipartFile file) throws IOException, InvalidFormatException {
        List<ItemModelEntity> itemModelEntities = itemModelService.findByProperty("formModel", listModelEntity.getMasterForm()).stream()
                .filter(ItemModelEntity::isDataImported)
                .collect(Collectors.toList());
        ImportBaseFunctionEntity importSetting = listModelEntity.getFunctions().stream().filter(fuc -> DefaultFunctionType.Import.getValue().equals(fuc.getAction()))
                .findAny()
                .map(ListFunction::getImportFunction)
                .orElseThrow(() -> new ICityException("不存在相应的导入配置"));
        if (itemModelEntities.stream().noneMatch(ItemModelEntity::isMatchKey)) {
            throw new ICityException("未设置用于匹配的key, 无法进行导入操作");
        }
        ExcelReaderUtils.ExcelReaderResult<Map<String, Object>> result = ExcelReaderUtils.readExcel(file.getInputStream(), importSetting.getHeaderRow(), importSetting.getStartRow(), importSetting.getEndRow(), mapExcelRowMapper);
        if (result.getHeader().stream().distinct().count() != result.getHeader().size()) {
            throw new ICityException("导入的excel存在相同的标题头, 无法导入");
        }

        Map<String, ItemModelEntity> nameMapping = itemModelEntities.stream().collect(Collectors.toMap(ItemModelEntity::getTemplateName, i -> i));

        List<Map<String, Object>> datas = result.getResult().stream().map(data ->
                result.getHeader().stream().map(header -> {
                    ItemModelEntity matchEntity = nameMapping.get(header);
                    Object tmpD = data.get(header);
                    ColumnTypeClass targetType = ColumnTypeClass.valueOf(matchEntity.getColumnModel().getDataType());
                    if (matchEntity.getSystemItemType() == SystemItemType.TimePicker && matchEntity instanceof TimeItemModelEntity && tmpD instanceof Date) {
                        TimeItemModelEntity time = (TimeItemModelEntity) matchEntity;
                        String format = time.getTimeFormat();
                        tmpD = CommonUtils.date2Str((Date) tmpD, format);
                    } else {
                        tmpD = conversionService.convert(tmpD, targetType.getClazz());
                    }
                    String colName = matchEntity.getColumnModel().getColumnName();
                    return Collections.singletonMap(colName, tmpD);
                }).reduce(new HashMap<>(), Reducers::reduceMap)
        ).collect(Collectors.toList());

        DataModelEntity dataModelEntity = listModelEntity.getMasterForm().getDataModels().get(0);
        try (Session session = getSession(dataModelEntity)) {
            // TODO 更新等导入操作类型补全
            // TODO 当前数据key 查重
            List<Map<String, Object>> currentDatas = session.createCriteria(dataModelEntity.getTableName()).list();

            List<Map<String, Object>> saveDatas = computeDatas(importSetting, itemModelEntities, datas, currentDatas);

            // TODO 删除需要校验是否被引用
            Transaction transaction = session.beginTransaction();
            try {
                for (Object data : saveDatas) {
                    session.saveOrUpdate(dataModelEntity.getTableName(), data);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    private List<Map<String, Object>> computeDatas(ImportBaseFunctionEntity functionEntity, List<ItemModelEntity> importItems, List<Map<String, Object>> importData, List<Map<String, Object>> currentData) {
        List<ItemModelEntity> keyEntitys = importItems.stream().filter(ItemModelEntity::isMatchKey).collect(Collectors.toList());
        switch (functionEntity.getType()) {
            case Rewrite:
                throw new ICityException("暂未实现");
            case SaveOnly:
                return computeSaveOnly(keyEntitys, importData, currentData);
            case UpdateOnly:
                throw new ICityException("暂未实现");
            case SaveOrUpdate:
                throw new ICityException("暂未实现");
            default:
                throw new ICityException("暂未实现");
        }
    }

    private List<Map<String, Object>> computeSaveOnly(List<ItemModelEntity> keys, List<Map<String, Object>> importData, List<Map<String, Object>> currentData) {
        // 寻找出key中不存在的, 此时data中的数据都变成了 col_name : data 的形式
        // 当出现重复key的时候会抛出异常
        Map<String, Map<String, Object>> importMappingData = importData.stream().collect(Collectors.toMap(m -> computeKey(keys, m), m -> m));
        Map<String, Map<String, Object>> currentMappingData = currentData.stream().collect(Collectors.toMap(m -> computeKey(keys, m), m -> m));

        return Sets.difference(importMappingData.keySet(), currentMappingData.keySet())
                .stream()
                .map(importMappingData::get)
                .collect(Collectors.toList());
    }

    private String computeKey(List<ItemModelEntity> keys, Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (ItemModelEntity key : keys) {
            builder.append(data.getOrDefault(key.getColumnModel().getColumnName(), "").toString());
        }
        return builder.toString();
    }

    private Session getSession(DataModelEntity dataModel) {
        SessionFactory sessionFactory = null;
        try {
            sessionFactory = sessionFactoryBuilder.getSessionFactory(dataModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionFactory == null ? null : sessionFactory.openSession();
    }

    private Resource exportPdf(String title, List<String> header, List<List<Object>> exportDatas) {
        try {
            List<List<String>> stringDatas = exportDatas.stream().map(list -> list.stream().map(Objects::toString).collect(Collectors.toList())).collect(Collectors.toList());
            PdfBuilderUtils builderUtils = new PdfBuilderUtils();
            builderUtils.setTitle(title);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            builderUtils.buildTablePdf(header, stringDatas, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException | DocumentException e) {
            throw new ICityException(e);
        }
    }

    private Resource exportExcel(String sheetName, List<String> header, List<List<Object>> exportDatas, BiConsumer<CellStyle, Font> cellStyle) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);

        ExportUtils.outputHeaders(header.toArray(new String[0]), sheet);
        ExportUtils.outputColumn(exportDatas, sheet, 1, cellStyle);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            wb.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayResource(os.toByteArray());
    }

    /**
     * 导出excel类型的资源
     *
     * @return 返回Excel的Resource
     */
    private Resource exportExcel(String sheetName, List<String> header, List<List<Object>> exportDatas) {
        return exportExcel(sheetName, header, exportDatas, null);
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

    /**
     * ItemModelEntity 的处理工具类
     * <ul>
     * <li>
     * 遍历出所有的ItemModelEntity
     * </li>
     * </ul>
     */
    private static class ItemModelHandlerUtils {

        public static List<ItemModelEntity> eachItemEntity(List<ItemModelEntity> entitys, Set<ItemType> ignoreTypes) {
            List<ItemModelEntity> result = new ArrayList<>();
            for (ItemModelEntity entity : entitys) {
                // 如果包含不遍历的控件类型, 则跳过
                if (!ignoreTypes.isEmpty() && ignoreTypes.contains(entity.getType())) {
                    continue;
                }
                for (Field field : entity.getClass().getDeclaredFields()) {
                    // 处理 List<ItemModelEntity> 的Field 执行递归
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        Type type = field.getGenericType();
                        if (type instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) type;
                            if (ItemModelEntity.class.isAssignableFrom((Class<?>) parameterizedType.getActualTypeArguments()[0])) {
                                // 递归遍历
                                List<ItemModelEntity> modelEntities = eachItemEntity(getFieldValue(field, entity, List.class), ignoreTypes);
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

        /**
         * 遍历出所有ItemModelEntity
         *
         * @param entitys 需要遍历的ItemModelEntity
         * @return 返回遍历后的结果, 但是不会包含分支节点, 只会有叶子节点
         */
        public static List<ItemModelEntity> eachItemEntity(List<ItemModelEntity> entitys) {
            return eachItemEntity(entitys, Collections.emptySet());
        }

        public static List<ItemModelEntity> eachItemEntity(List<ItemModelEntity> entitys, ItemType... types) {
            return eachItemEntity(entitys, Sets.newHashSet(types));
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

    private interface HeaderBuild {

        List<String> buildHeader(ListModelEntity entity, ExportListFunction function, Map<String, Object> queryParams);

    }

    /**
     * 用于生成导出表格的标题头工厂
     */
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
                ItemModelHandlerUtils.eachItemEntity(entity.getMasterForm().getItems(), ItemType.SubForm).stream()
                        .filter(item -> item.getType().hasContext())
                        .sorted(Comparator.comparing(ItemModelEntity::getOrderNo))
                        .map(BaseEntity::getName).collect(Collectors.toList());

        private static HeaderBuild LIST_HEADER_BUILD = (entity, function, queryParams) -> entity.getDisplayItems().stream().sorted(Comparator.comparing(ItemModelEntity::getOrderNo)).map(ItemModelEntity::getName).collect(Collectors.toList());

        private static HeaderBuild BACK_HEADER_BUILD = (entity, function, queryParams) -> {
            Map<String, String> mapping = ItemModelHandlerUtils.eachItemEntity(entity.getMasterForm().getItems(), ItemType.SubForm).stream().distinct().collect(Collectors.toMap(ItemModelEntity::getId, ItemModelEntity::getName));
            return Stream.of(function.getCustomExport().split(","))
                    .map(mapping::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };

        private static HeaderBuild FRONT_HEADER_BUILD = (entity, function, queryParams) -> {
            Map<String, String> mapping = ItemModelHandlerUtils.eachItemEntity(entity.getMasterForm().getItems(), ItemType.SubForm).stream().distinct().collect(Collectors.toMap(ItemModelEntity::getId, ItemModelEntity::getName));
            return findFrontItemIds(queryParams).stream()
                    .map(mapping::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };

        private static Collection<String> findFrontItemIds(Map<String, Object> params) {
            final String key = "exportColumnIds";
            if (params == null || !params.containsKey(key)) {
                return Collections.emptyList();
            }
            Object obj = params.get(key);
            if (Collection.class.isAssignableFrom(obj.getClass())) {
                return (Collection<String>) obj;
            } else {
                return Arrays.asList(Objects.toString(obj).split(","));
            }
        }


    }

}
