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
import tech.ascs.icity.admin.api.model.TreeSelectData;
import tech.ascs.icity.admin.api.service.GroupService;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.api.model.export.ExportControl;
import tech.ascs.icity.iform.api.model.export.ImportType;
import tech.ascs.icity.iform.api.service.DictionaryModelDataService;
import tech.ascs.icity.iform.bean.ReferenceImportItemWrapperBean;
import tech.ascs.icity.iform.bean.SelectImportItemWrapperBean;
import tech.ascs.icity.iform.function.ExcelRowMapper;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.support.IFormSessionFactoryBuilder;
import tech.ascs.icity.iform.utils.*;
import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
    private tech.ascs.icity.iform.api.service.DictionaryDataService dictionaryDataService;

    @Autowired
    private DictionaryModelDataService dictionaryModelDataService;

    @Autowired
    private GroupService groupService;

    private final String SPLIT_CHAR = ";";

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

        List<String> header = ExportUtils.ExportHeaderFactory.getInstance(exportControl).buildHeader(entity, function, queryParams);
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
                .filter(item -> Objects.nonNull(item.getColumnModel()) || item.getType() == ItemType.ReferenceList)
                // fix 修复遍历可能会出现重复item的问题, 这个重复指的是 对象地址重复, 即是完全相同的一个item
                .distinct()
                .sorted(Comparator.comparing(ItemModelEntity::getOrderNo))
                .collect(Collectors.toList());
    }

    @Override
    public Resource exportTemplate(ListModelEntity listModel) {
        List<ItemModelEntity> modelEntities = eachHasColumnItemModel(listModel.getMasterForm().getItems());
        List<ImportTemplateEntity> templateEntities = listModel.getTemplateEntities();
        List<String> header = new ArrayList<>();
        List<Object> data = new ArrayList<>();
        templateEntities.stream()
                .sorted(Comparator.comparing( template -> template.getItemModel().getOrderNo()))
                .filter(ImportTemplateEntity::isTemplateSelected)
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

        List<ImportTemplateEntity> templateEntities = listModelEntity.getTemplateEntities().stream()
                .filter(ImportTemplateEntity::isDataImported)
                .collect(Collectors.toList());

        ImportBaseFunctionEntity importSetting = listModelEntity.getFunctions().stream().filter(fuc -> DefaultFunctionType.Import.getValue().equals(fuc.getAction()))
                .findAny()
                .map(ListFunction::getImportFunction)
                .orElseThrow(() -> new ICityException("不存在相应的导入配置"));
        if (templateEntities.stream().noneMatch(ImportTemplateEntity::isMatchKey)) {
            throw new ICityException("未设置用于匹配的key, 无法进行导入操作");
        }
        ExcelReaderUtils.ExcelReaderResult<Map<String, Object>> result = ExcelReaderUtils.readExcel(file.getInputStream(), importSetting.getHeaderRow(), importSetting.getStartRow(), importSetting.getEndRow(), mapExcelRowMapper);
        if (result.getHeader().stream().distinct().count() != result.getHeader().size()) {
            throw new ICityException("导入的excel存在相同的标题头, 无法导入");
        }

        Map<String, ItemModelEntity> nameMapping = templateEntities.stream().collect(Collectors.toMap(ImportTemplateEntity::getTemplateName, ImportTemplateEntity::getItemModel));

        List<Map<String, Object>> datas = result.getResult().stream().map(data ->
                result.getHeader().stream().map(header -> {
                    ItemModelEntity matchEntity = nameMapping.get(header);
                    if (matchEntity == null) {
                        return null;
                    }
                    Object tmpD = data.get(header);
                    ColumnTypeClass targetType = ColumnTypeClass.valueOf(Optional.of(matchEntity).map(ItemModelEntity::getColumnModel).map(ColumnModelEntity::getDataType).orElse(ColumnType.String));
                    if (matchEntity.getSystemItemType() == SystemItemType.TimePicker && matchEntity instanceof TimeItemModelEntity && tmpD instanceof Date) {
                        TimeItemModelEntity time = (TimeItemModelEntity) matchEntity;
                        String format = time.getTimeFormat();
                        tmpD = CommonUtils.date2Str((Date) tmpD, format);
                    } else {
                        tmpD = conversionService.convert(tmpD, targetType.getClazz());
                    }
                    String colName = Optional.ofNullable(matchEntity.getColumnModel()).map(ColumnModelEntity::getColumnName)
                            .orElseGet(() -> {
                                ReferenceItemModelEntity refItem = (ReferenceItemModelEntity) matchEntity;
                                return findReferenceListManyToManyColumnName(refItem);
                            });
                    return Collections.singletonMap(colName, tmpD);
                }).filter(Objects::nonNull).reduce(new HashMap<>(32), Reducers::reduceMap)
        ).collect(Collectors.toList());

        FormModelEntity formModelEntity = listModelEntity.getMasterForm();
        DataModelEntity dataModelEntity = formModelEntity.getDataModels().get(0);
        List<ReferenceItemModelEntity> refEntities = itemModelService.findRefenceItemByFormModelId(listModelEntity.getMasterForm().getId());
        try (Session session = getSession(dataModelEntity)) {
            List<Map<String, Object>> currentDatas = session.createCriteria(dataModelEntity.getTableName()).list();

            List<Map<String, Object>> saveDatas = computeDatas(importSetting, templateEntities, datas, currentDatas, session);

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

    private List<Map<String, Object>> computeDatas(ImportBaseFunctionEntity functionEntity, List<ImportTemplateEntity> templateEntities, List<Map<String, Object>> importData, List<Map<String, Object>> currentData, Session session) {
        List<ItemModelEntity> keyEntitys = templateEntities.stream().filter(ImportTemplateEntity::isMatchKey).map(ImportTemplateEntity::getItemModel).collect(Collectors.toList());
        checkKeys(keyEntitys, currentData, () -> new ICityException("当前的key设置会导致数据库中的数据存在重复, 无法导入"));
        checkKeys(keyEntitys, importData, () -> new ICityException("当前的key设置导致导入数据中存在重复, 无法导入"));

        Map<String, Map<String, Object>> importMappingData = toMapping(keyEntitys, importData);
        Map<String, Map<String, Object>> currentMappingData = toMapping(keyEntitys, currentData);

        return ImportDataComputer.getInstance(functionEntity.getType())
                .andThen(datas -> handleRefItemsCol(templateEntities, datas, currentMappingData, session))
                .andThen(datas -> parseSelectItemsCol(templateEntities, datas, session))
                .apply(importMappingData, currentMappingData);
    }

    private List<Map<String, Object>> parseSelectItemsCol(List<ImportTemplateEntity> templateEntities, List<Map<String, Object>> datas, Session session) {
        List<ItemModelEntity> items = templateEntities.stream().map(ImportTemplateEntity::getItemModel).collect(Collectors.toList());
        Map<String, SelectImportItemWrapperBean> wrapperBeanMap = items.stream()
                .filter(item -> item instanceof SelectItemModelEntity || item instanceof TreeSelectItemModelEntity)
                .map(SelectImportItemWrapperBean::new)
                .collect(Collectors.toMap(SelectImportItemWrapperBean::getColumnName, t -> t));

        Map<String, Map<String, String>> colToDataMap = wrapperBeanMap.entrySet().stream()
                .map(entry -> {
                    SelectImportItemWrapperBean bean = entry.getValue();
                    Map<String, Map<String, String>> result = new HashMap<>();
                    if (bean.isTree()) {
                        result.put(entry.getKey(), parseTreeSelectItemModelData(bean));
                    } else {
                        result.put(entry.getKey(), parseSelectItemModelData(bean));
                    }
                    return result;
                }).reduce(Reducers::reduceMap)
                .orElseThrow(() -> new ICityException("获取当前的关联的值失败"));

        datas.forEach(data -> colToDataMap.forEach((col, dictData) -> {
                    Object displayValue = data.get(col);
                    if (displayValue instanceof String) {
                        SelectImportItemWrapperBean bean = wrapperBeanMap.get(col);
                        List<String> values;
                        if (bean.isMultiple()) {
                            values = Arrays.asList(Objects.toString(displayValue).split(";"));
                        } else {
                            values = Collections.singletonList(Objects.toString(displayValue));
                        }
                        StringJoiner joiner = new StringJoiner(",");
                        for (String value : values) {
                            if (!dictData.containsKey(value)) {
                                throw new ICityException("对应的关联数据中找不到值[" + value + "]");
                            }
                            joiner.add(dictData.get(value));
                        }
                        data.put(col, joiner.toString());
                    }
                }
        ));

        return datas;
    }

    private Map<String, String> parseTreeSelectItemModelData(SelectImportItemWrapperBean bean) {
        TreeSelectItemModelEntity treeSelectItemModelEntity = (TreeSelectItemModelEntity) bean.getItemModelEntity();
        String dataRange = treeSelectItemModelEntity.getDataRange();
        String referenceDictionaryId = treeSelectItemModelEntity.getReferenceDictionaryId();
        TreeSelectDataSource dataSource = treeSelectItemModelEntity.getDataSource();
        switch (dataSource) {
            case Role:
            case Position:
            case Department:
            case Personnel:
            case PositionIdentify:
            case DictionaryData:
                List<TreeSelectData> treeData = groupService.getTreeSelectDataSource(dataSource.getValue(), dataRange, treeSelectItemModelEntity.getDataDepth(), treeSelectItemModelEntity.getReferenceDictionaryId());
                return eachTreeData(treeData);
            case DictionaryModel:
                List<DictionaryModelData> modelItems = dictionaryModelDataService.findFirstItems(referenceDictionaryId, dataRange);
                return eachDictionaryModelData(modelItems);
            default:
                return Collections.emptyMap();
        }
    }

    /**
     * label -- id的map
     */
    private Map<String, String> parseSelectItemModelData(SelectImportItemWrapperBean bean) {
        SelectItemModelEntity selectItemModelEntity = (SelectItemModelEntity) bean.getItemModelEntity();
        if (selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.Option) {
            //固定值
            return selectItemModelEntity.getOptions().stream().map(option -> {
                Map<String, String> result = new HashMap<>();
                result.put(option.getLabel(), option.getId());
                return result;
            }).reduce(Reducers::reduceMap).orElseThrow(() -> new ICityException("计算出错"));
        } else if (selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.DictionaryData) {
            //系统代码
            String referenceDictionaryId = selectItemModelEntity.getReferenceDictionaryId();
            String referenceDictionaryItemId = selectItemModelEntity.getReferenceDictionaryItemId();
            List<DictionaryDataItemModel> modelItems = dictionaryDataService.findItems(referenceDictionaryId, referenceDictionaryItemId, null);
            return eachDictionaryData(modelItems);
        } else if (selectItemModelEntity.getSelectDataSourceType() == SelectDataSourceType.DictionaryModel) {
            //字典建模
            String referenceDictionaryId = selectItemModelEntity.getReferenceDictionaryId();
            String referenceDictionaryItemId = selectItemModelEntity.getReferenceDictionaryItemId();
            List<DictionaryModelData> modelItems = dictionaryModelDataService.findFirstItems(referenceDictionaryId, referenceDictionaryItemId);
            return eachDictionaryModelData(modelItems);
        }
        return Collections.emptyMap();
    }

    private Map<String, String> eachDictionaryData(List<DictionaryDataItemModel> models) {
        Map<String, String> result = new HashMap<>();
        for (DictionaryDataItemModel model : models) {
            result.put(model.getName(), model.getId());
            if (model.getResources() != null && model.getResources().size() > 0) {
                result.putAll(eachDictionaryData(model.getResources()));
            }
        }
        return result;
    }

    private Map<String, String> eachDictionaryModelData(List<DictionaryModelData> models) {
        Map<String, String> result = new HashMap<>();
        for (DictionaryModelData model : models) {
            result.put(model.getName(), model.getId());
            if (model.getResources() != null && model.getResources().size() > 0) {
                result.putAll(eachDictionaryModelData(model.getResources()));
            }
        }
        return result;
    }

    private Map<String, String> eachTreeData(List<TreeSelectData> datas) {
        Map<String, String> result = new HashMap<>();
        for (TreeSelectData data : datas) {
            result.put(data.getName(), data.getId());
            if (data.getChildren() != null && data.getChildren().size() > 0) {
                result.putAll(eachTreeData(data.getChildren()));
            }
        }
        return result;
    }

    /**
     * 处理 关联控件, 将关联控件转换为对应的jpa map
     */
    private List<Map<String, Object>> handleRefItemsCol(List<ImportTemplateEntity> templateEntities, List<Map<String, Object>> datas, Map<String, Map<String, Object>> dbMappingData, Session session) {
        List<ItemModelEntity> keyEntitys = templateEntities.stream().filter(ImportTemplateEntity::isMatchKey).map(ImportTemplateEntity::getItemModel).collect(Collectors.toList());
        List<ItemModelEntity> importItems = templateEntities.stream().map(ImportTemplateEntity::getItemModel).collect(Collectors.toList());
        Map<String, ReferenceImportItemWrapperBean> wrapperMapping = importItems.stream()
                .filter(this::hasRefDataItem)
                .map(ReferenceImportItemWrapperBean::new)
                .collect(HashMap::new, (m, b) -> m.put(b.getColumnName(), b), HashMap::putAll);

        // 字段名和对应的映射数据的map
        Map<String, List<HashMap>> colNameToDataMapping = wrapperMapping.entrySet()
                .stream()
                .map(entry -> wrapperBeanToFormData(entry.getKey(), entry.getValue()))
                .reduce(Reducers::reduceMap)
                .orElseThrow(() -> new ICityException("计算出错"));
        // 计算数据 备用
        Map<String, Map<String, Object>> importMappingData = toMapping(keyEntitys, datas);
        List<Map<String, Object>> diffWithDatabaseMapping = ImportDataComputer.getInstance(ImportType.SaveOnly).apply(dbMappingData, importMappingData);

        //数据处理, 把有关联关系的字段, 根据数据标识转换为对应的Jpa entity, 并覆盖关联字段的内容
        datas.forEach(data -> wrapperMapping.forEach((colName, bean) -> {
            Object displayValue = data.get(colName);
            if (displayValue instanceof String) {
                // 当为多对多的时候, 需要对导入数据使用;分割
                if (bean.getReferenceType() == ReferenceType.ManyToMany) {
                    List<Object> targetDatas = new ArrayList<>();
                    for (String value : Objects.toString(displayValue).split(SPLIT_CHAR)) {
                        Map targetData = toJpaData(session, bean, colNameToDataMapping.get(colName), value);
                        targetDatas.add(targetData);
                    }
                    data.put(colName, targetDatas);
                } else {
                    Map targetData = toJpaData(session, bean, colNameToDataMapping.get(colName), Objects.toString(displayValue));
                    if (wrapperMapping.get(colName).getReferenceType() == ReferenceType.OneToOne
                            && diffWithDatabaseMapping.stream().anyMatch(curData -> targetData.equals(curData.get(colName)))
                            && importMappingData.values().stream().anyMatch(curData -> targetData.equals(curData.get(colName)) || displayValue.equals(curData.get(colName)))) {
                        throw new ICityException("[" + displayValue + "]已经被引用");
                    }
                    data.put(colName, targetData);
                }
            }
        }));
        return datas;
    }

    private Optional<FormDataSaveInstance> findInstance(List<HashMap> dataMapping, String value) {
        return dataMapping.stream()
                .filter(refData -> refData.containsKey(value))
                .map(refData -> refData.get(value))
                .map(obj -> (FormDataSaveInstance) obj)
                .findAny();
    }

    private String findReferenceListManyToManyColumnName(ReferenceItemModelEntity referenceItemModelEntity) {
        return referenceItemModelEntity.getReferenceList().getMasterForm().getDataModels().get(0).getTableName() + "_list";
    }

    private boolean hasRefDataItem(ItemModelEntity item) {
        return item.getType() == ItemType.ReferenceList || (item.getColumnModel() != null && item.getColumnModel().getColumnReferences() != null && item.getColumnModel().getColumnReferences().size() > 0);
    }

    /**
     * 根据把表格数据转换为对应的Jpa Map
     */
    private Map toJpaData(Session session, ReferenceImportItemWrapperBean bean, List<HashMap> datas, String cellData) {
        BiFunction<Object, Object, RuntimeException> throwableBiFunction = (value, tableName) -> new ICityException("[" + value + "]无法在对应关联表[" + tableName + "]中找到目标数据标识");
        FormDataSaveInstance instance = findInstance(datas, cellData).orElseThrow(() -> throwableBiFunction.apply(cellData, bean.getDataModel().getTableName()));
        return (Map) session.load(bean.getDataModel().getTableName(), instance.getId());
    }

    private Map<String, Map<String, Object>> toMapping(List<ItemModelEntity> keyEntitys, List<Map<String, Object>> datas) {
        return datas.stream().collect(Collectors.toMap(m -> computeKey(keyEntitys, m), m -> m));
    }

    private Map<String, List<HashMap>> wrapperBeanToFormData(String key, ReferenceImportItemWrapperBean bean) {
        Stream<ListModelEntity> listStream;
        if (bean.getListModelEntity() != null) {
            listStream = Stream.of(bean.getListModelEntity());
        } else {
            listStream = listModelService.findListIdByTableName(bean.getDataModel().getTableName()).stream().map(listModelService::find);
        }
        // 调用 listModelService.findListIdByTableName 的方法, 获取List<FormDataSaveInstance>, 然后转换成 List<Map<String, FormDataSaveInstance>>, map的key为label值
        List<HashMap> dataMapping = listStream
                .map(listModel -> formInstanceServiceEx.pageListInstance(listModel, 1, Integer.MAX_VALUE, Collections.emptyMap()).getResults())
                .map(list -> list.stream().collect(HashMap::new, (m, i) -> m.put(i.getLabel(), i), HashMap::putAll))
                .collect(Collectors.toList());
        Map<String, List<HashMap>> result = new HashMap<>(16);
        result.put(key, dataMapping);
        return result;
    }

    /**
     * 检查是否存在重复的key
     *
     * @param keys              作为key的控件
     * @param datas             数据
     * @param throwableSupplier 当发现重复的时候抛出的异常
     */
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

    /**
     * 根据key控件和给定数据, 计算出当前数据的key
     *
     * @param keys key控件
     * @param data 当前数据
     * @return 返回key
     */
    private String computeKey(List<ItemModelEntity> keys, Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (ItemModelEntity key : keys) {
            builder.append(getOrDefault(data.get(key.getColumnModel().getColumnName()), ""));
        }
        return builder.toString();
    }

    private String getOrDefault(Object value, String defaultValue) {
        return Optional.ofNullable(value)
                .map(Objects::toString)
                .orElse(defaultValue);
    }

    /**
     * copy from {@link FormInstanceServiceExImpl#getSession(DataModelEntity)}
     */
    private Session getSession(DataModelEntity dataModel) {
        SessionFactory sessionFactory = null;
        try {
            sessionFactory = sessionFactoryBuilder.getSessionFactory(dataModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionFactory == null ? null : sessionFactory.openSession();
    }

    /**
     * 导出pdf资源
     *
     * @param title       标题
     * @param header      表格头
     * @param exportDatas 表格数据
     * @return 返回pdf资源
     */
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

    /**
     * 导出excel的资源
     *
     * @param sheetName   第一页的sheet页的名称
     * @param header      标题头
     * @param exportDatas 导出的数据
     * @param cellStyle   单元格格式 可以为null
     * @return 返回excel的资源
     */
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


}
