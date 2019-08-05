package tech.ascs.icity.iform.service.impl;

import com.google.common.collect.Sets;
import com.itextpdf.text.DocumentException;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.model.export.ExportControl;
import tech.ascs.icity.iform.api.model.export.ImportType;
import tech.ascs.icity.iform.function.ExcelRowMapper;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
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
import java.util.function.Supplier;
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
    private ColumnModelService columnModelService;

    @Autowired
    private FormInstanceServiceEx formInstanceServiceEx;

    @Autowired
    private FormModelService formModelService;

    @Autowired
    private IFormSessionFactoryBuilder sessionFactoryBuilder;

    @Autowired
    private ListModelService listModelService;

    @Autowired
    private tech.ascs.icity.iform.api.service.FormInstanceService formInstanceService;

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
        if (listModelEntity.getMasterForm().getProcess() != null) {
            throw new ICityException("不能对包含流程的表单导入数据");
        }
        List<ItemModelEntity> itemModelEntities = formModelService.findAllItems(listModelEntity.getMasterForm()).stream()
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

        FormModelEntity formModelEntity = listModelEntity.getMasterForm();
        DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
        List<ReferenceItemModelEntity> refEntities = itemModelService.findRefenceItemByFormModelId(listModelEntity.getMasterForm().getId());
        try (Session session = getSession(dataModelEntity)) {
            List<Map<String, Object>> currentDatas = session.createCriteria(dataModelEntity.getTableName()).list();

            List<Map<String, Object>> saveDatas = computeDatas(importSetting, itemModelEntities, datas, currentDatas, session);

            Transaction transaction = session.beginTransaction();
            try {
                if (importSetting.getType() == ImportType.Rewrite) {
                    // 重写前的删除校验和删除原有数据
                    ColumnModelEntity idColumn = columnModelService.saveColumnModelEntity(dataModelEntity, "id");
                    List<ColumnReferenceEntity> colRefEntitys = idColumn.getColumnReferences();
                    for (Map<String, Object> currentData : currentDatas) {
                        for (ColumnReferenceEntity refEntity : colRefEntitys) {
                            formInstanceServiceEx.deleteVerify(refEntity, currentData, refEntities);
                        }
                        // 业务触发器如果抛出异常则导入失败
                        formInstanceServiceEx.sendWebService(formModelEntity, BusinessTriggerType.Delete_Before, currentData, currentData.get("id").toString());
                        session.delete(dataModelEntity.getTableName(), currentData);
                        formInstanceServiceEx.sendWebService(formModelEntity, BusinessTriggerType.Delete_After, currentData, currentData.get("id").toString());
                    }
                }
                for (Map<String, Object> data : saveDatas) {
                    BusinessTriggerType beforeTriggerType = data.containsKey("id") ? BusinessTriggerType.Update_Before : BusinessTriggerType.Add_Before;
                    BusinessTriggerType afterTriggerType = data.containsKey("id") ? BusinessTriggerType.Update_After : BusinessTriggerType.Delete_After;
                    String id = data.containsKey("id") ? data.get("id").toString() : null;
                    formInstanceServiceEx.sendWebService(formModelEntity, beforeTriggerType, data, id);
                    session.saveOrUpdate(dataModelEntity.getTableName(), data);
                    formInstanceServiceEx.sendWebService(formModelEntity, afterTriggerType, data, id);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    private List<Map<String, Object>> computeDatas(ImportBaseFunctionEntity functionEntity, List<ItemModelEntity> importItems, List<Map<String, Object>> importData, List<Map<String, Object>> currentData, Session session) {
        List<ItemModelEntity> keyEntitys = importItems.stream().filter(ItemModelEntity::isMatchKey).collect(Collectors.toList());
        checkKeys(keyEntitys, currentData, () -> new ICityException("当前的key设置会导致数据库中的数据存在重复, 无法导入"));
        checkKeys(keyEntitys, importData, () -> new ICityException("当前的key设置导致导入数据中存在重复, 无法导入"));

        Map<String, Map<String, Object>> importMappingData = importData.stream().collect(Collectors.toMap(m -> computeKey(keyEntitys, m), m -> m));
        Map<String, Map<String, Object>> currentMappingData = currentData.stream().collect(Collectors.toMap(m -> computeKey(keyEntitys, m), m -> m));

        //TODO 下拉框等数据转换
        return ImportDataComputer.getInstance(functionEntity.getType())
                .andThen(datas -> handleRefItemsCol(importItems, datas, currentMappingData, session))
                .apply(importMappingData, currentMappingData);
    }

    private List<Map<String, Object>> handleRefItemsCol(List<ItemModelEntity> importItems, List<Map<String, Object>> datas, Map<String, Map<String, Object>> currentMappingData, Session session) {
        // 找出所有的关联控件
        Map<String, ColumnModelEntity> colNameToColEntityMapping = importItems.stream()
                .map(ItemModelEntity::getColumnModel)
                .filter(col -> col.getColumnReferences() != null && col.getColumnReferences().size() > 0)
                .collect(Collectors.toMap(ColumnModelEntity::getColumnName, t -> t));

        Map<String, DataModelEntity> colNameToDataModelMapping = colNameToColEntityMapping.values().stream()
                .map(ColumnModelEntity::getColumnReferences)
                .flatMap(Collection::stream)
                .collect(HashMap::new, (m, col) -> m.put(col.getFromColumn().getColumnName(), col.getToColumn().getDataModel()), HashMap::putAll);

        Map<String, List<HashMap>> colNameToDataMapping = colNameToDataModelMapping.entrySet()
                .parallelStream()
                .map(entry -> {
                    List<HashMap> dataMapping = listModelService.findListIdByTableName(entry.getValue().getTableName()).stream().map(id -> formInstanceService.list(id, Collections.emptyMap()))
                            .map(list -> list.stream().collect(HashMap::new, (m, i) -> m.put(i.getLabel(), i), HashMap::putAll))
                            .collect(Collectors.toList());
                    Map<String, List<HashMap>> result = new HashMap<>();
                    result.put(entry.getKey(), dataMapping);
                    return result;
                }).reduce(Reducers::reduceMap)
                .orElseThrow(() -> new ICityException("计算出错"));

        datas.forEach(data -> colNameToDataModelMapping.forEach((colName, dataModel) -> {
            Object displayValue = data.get(colName);
            if (displayValue != null) {
                colNameToDataMapping.get(colName).stream().filter(refData -> refData.containsKey(displayValue)).forEach(refData -> {
                    FormDataSaveInstance instance = (FormDataSaveInstance) refData.get(displayValue);
                    if (instance != null) {
                        // TODO 分类处理???OneToOne, ManyToOne, OneToMany, ManyToMany
                        Map targetData = (Map) session.load(dataModel.getTableName(), instance.getId());
                        if (colNameToColEntityMapping.get(colName).getColumnReferences().get(0).getReferenceType() == ReferenceType.OneToOne && currentMappingData.values().stream().anyMatch(curData -> targetData.equals(curData.get(colName)))) {
                            throw new ICityException("[" + displayValue + "]已经被引用");
                        }
                        data.put(colName, targetData);
                    }
                });
                if (displayValue.equals(data.get(colName))) {
                    throw new ICityException("[" + displayValue + "]无法在对应关联表[" + dataModel.getTableName() + "]中找到目标数据标识");
                }
            }
        }));

        return datas;
    }

    private void checkKeys(List<ItemModelEntity> keys, List<Map<String, Object>> datas, Supplier<ICityException> throwableSupplier) {
        boolean hasRepeat = datas.stream()
                .map(data -> computeKey(keys, data))
                .collect(Collectors.groupingBy(t -> t))
                .values()
                .stream()
                .anyMatch(d -> d.size() > 1);
        if (hasRepeat) {
            throw throwableSupplier.get();
        }
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
