package tech.ascs.icity.iform.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormInstanceServiceEx;
import tech.ascs.icity.iform.service.FormModelService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 内嵌关联控件处理工具
 *
 * @author renjie
 * @since 0.7.2
 **/
@Component
public final class InnerItemUtils implements ApplicationContextAware {

    private static JdbcTemplate jdbcTemplate;
    private static FormModelService formModelService;
    private static FormInstanceServiceEx formInstanceServiceEx;
    private static ReferenceDataHandler REF_HANDLER = null;

    public static void setReferenceDataHandler(ReferenceDataHandler referenceDataHandler) {
        REF_HANDLER = referenceDataHandler;
    }

    public static List<Map<String, Object>> findInnerDataInfo(DataModelEntity outsideModel, ItemModelEntity outsideItem, ItemModelEntity displayItem, Object matchValue) {
        return InnerDataInfoFactory.getInstance(displayItem).findInnerDataInfo(outsideModel, outsideItem, displayItem, matchValue);
    }

    /**
     * 构建一个内嵌关联属性的ItemInstance
     *
     * @param modelEntity  控件模型
     * @param displayValue 显示值
     * @return 控件实例
     */
    public static ItemInstance buildItemInstance(ItemModelEntity modelEntity, String displayValue) {
        ItemInstance instance = new ItemInstance();
        instance.setType(ItemType.ReferenceInnerLabel);
        instance.setSystemItemType(SystemItemType.ReferenceInnerLabel);
        instance.setDisplayValue(displayValue);
        instance.setVisible(true);
        instance.setValue(displayValue);
        instance.setCanFill(false);
        instance.setItemName(modelEntity.getName());
        instance.setProps(modelEntity.getProps());
        instance.setId(modelEntity.getId());
        return instance;
    }

    public static String findInnerDisplayValue(String outsideItemId, String displayItemId, Function<String, ItemModelEntity> itemFindFunction, Object innerValue) {
        // 外部关联的控件 TODO 涉及可能是一个关联控件
        ItemModelEntity outsideItem = itemFindFunction.apply(outsideItemId);
        // 外部的显示控件 TODO 涉及可能是一个关联控件
        ItemModelEntity displayItem = itemFindFunction.apply(displayItemId);
        // select displayItem from outsideDataModel where outsideItem = innerValue
        DataModelEntity dataModel = Optional.ofNullable(outsideItem.getFormModel())
                .map(FormModelEntity::getDataModels)
                .map(dataModels -> dataModels.get(0))
                .orElseGet(() -> outsideItem.getColumnModel().getDataModel());
        List<Map<String, Object>> innerMap = InnerItemUtils.findInnerDataInfo(dataModel, outsideItem, displayItem, innerValue);
        return innerMap
                .stream()
                .flatMap(map -> map.values().stream())
                .filter(Objects::nonNull)
                .map(Objects::toString)
                .collect(Collectors.joining(","));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
        formModelService = applicationContext.getBean(FormModelService.class);
        formInstanceServiceEx = applicationContext.getBean(FormInstanceServiceEx.class);
    }

    public interface InnerItemHandler<T extends ItemModelEntity> {
        /**
         * 获取关联属性内嵌控件需要显示的内容
         *
         * @param model            {@link ReferenceInnerItemModelEntity} 内嵌组件的实体
         * @param innerItem        {@link ReferenceInnerItemModelEntity#getReferenceInnerItemUuid()} 对应的控件的实体
         * @param itemFindFunction 获取根据id可以获取到对应控件的方法 如 <code> itemModelService::find </code>
         * @param rowData          该行的数据
         * @return 返回内嵌关联属性的显示值
         */
        String findDisplayValue(ReferenceInnerItemModelEntity model, T innerItem, Function<String, ItemModelEntity> itemFindFunction, Map<String, Object> rowData);
    }

    private interface InnerDataInfoHandler {
        List<Map<String, Object>> findInnerDataInfo(DataModelEntity outsideModel, ItemModelEntity outsideItem, ItemModelEntity displayItem, Object matchValue);
    }

    public interface ReferenceDataHandler {
        ReferenceDataInstance createDataModelInstance(ReferenceItemModelEntity fromItem, FormModelEntity toModelEntity, String id, List<String> itemIds);
    }

    /**
     * 内嵌关联控件处理工厂
     */
    public static class InnerItemHandlerFactory {

        /**
         * 根据内嵌关联组件内部关联的控件的类型来获取对应的处理方法
         *
         * @param entity ReferenceInnerItemModelEntity中referenceInnerItemUuid字段关联的控件
         * @return 返回一个 InnerItemHandler 接口的实现
         */
        public static InnerItemHandler<? extends ItemModelEntity> getHandler(ItemModelEntity entity) {
            switch (entity.getType()) {
                case ReferenceLabel:
                    return REFERENCE_LABEL_HANDLE;
                case ReferenceList:
                    return REFERENCE_LIST_HANDLE;
                default:
                    return OTHER_HANDLE;
            }
        }

        /**
         * 关联属性处理
         */
        private static InnerItemHandler<ReferenceItemModelEntity> REFERENCE_LABEL_HANDLE = (model, innerItem, itemFunction, rowData) -> {
            ReferenceItemModelEntity parentItem = innerItem.getParentItem();
            String parentColumnName = parentItem.getColumnModel().getColumnName();
            // TODO 可能这个控件执行的控件也是一个关联控件
            return Optional.ofNullable(itemFunction.apply(innerItem.getReferenceItemId()))
                    .map(ItemModelEntity::getColumnModel)
                    .map(ColumnModelEntity::getColumnName)
                    .flatMap(innerColumnName -> Optional.ofNullable((Map) rowData.get(parentColumnName)).map(innerTable -> innerTable.get(innerColumnName)))
                    .map(innerValue -> InnerItemUtils.findInnerDisplayValue(model.getReferenceOutsideItemId(), model.getReferenceItemId(), itemFunction, innerValue))
                    .orElse("");
        };

        /**
         * 关联表单单选处理
         */
        private static InnerItemHandler<ReferenceItemModelEntity> REFERENCE_LIST_HANDLE = (model, innerItem, itemFindFunction, rowData) -> {
            String innerColumnName = "id";
            //TODO 如果不是单选
            if (innerItem.getSelectMode() != SelectMode.Single) {
                return "";
            }
            return Optional.ofNullable(innerItem.getColumnModel())
                    .map(ColumnModelEntity::getColumnName)
                    .flatMap(parentColumnName -> Optional.ofNullable((Map) rowData.get(parentColumnName)).map(innerTable -> innerTable.get(innerColumnName)))
                    .map(innerValue -> InnerItemUtils.findInnerDisplayValue(model.getReferenceOutsideItemId(), model.getReferenceItemId(), itemFindFunction, innerValue))
                    .orElse("");
        };

        /**
         * 普通的控件处理
         */
        private static InnerItemHandler<ItemModelEntity> OTHER_HANDLE = (model, innerItem, itemFindFunction, rowData) -> Optional.ofNullable(innerItem.getColumnModel())
                .map(ColumnModelEntity::getColumnName)
                .map(rowData::get)
                .map(innerData -> InnerItemUtils.findInnerDisplayValue(model.getReferenceOutsideItemId(), model.getReferenceItemId(), itemFindFunction, innerData))
                .orElse("");

    }

    private static class InnerDataInfoFactory {

        private static final InnerDataInfoHandler DEFAULT = (outsideModel, outsideItem, displayItem, matchValue) -> jdbcTemplate.queryForList(String.format("SELECT %s FROM %s WHERE %s = ?", displayItem.getColumnModel().getColumnName(), outsideModel.getTableName(), outsideItem.getColumnModel().getColumnName()), matchValue);

        private static final InnerDataInfoHandler SELECT = (outsideModel, outsideItem, displayItem, matchValue) -> {
            List<String> selectIds = DEFAULT.findInnerDataInfo(outsideModel, outsideItem, displayItem, matchValue)
                    .stream()
                    .flatMap(map -> map.values().stream())
                    .filter(Objects::nonNull)
                    .map(Objects::toString)
                    .collect(Collectors.toList());
            String value = String.join(",", formInstanceServiceEx.setSelectItemDisplayValue(null, (SelectItemModelEntity) displayItem, selectIds));
            Map<String, Object> result = new HashMap<>();
            result.put("value", value);
            return Collections.singletonList(result);
        };

        private static final InnerDataInfoHandler REFERENCE_LIST = (outsideModel, outsideItem, displayItem, matchValue) -> {
            if (REF_HANDLER == null) {
                return DEFAULT.findInnerDataInfo(outsideModel, outsideItem, displayItem, matchValue);
            }
            ReferenceItemModelEntity refDisplayItem = ((ReferenceItemModelEntity) displayItem);
            FormModelEntity formModelEntity = formModelService.find(refDisplayItem.getReferenceFormId());
            String ids = formModelEntity.getItemModelIds();
            if (!StringUtils.hasText(ids)) {
                return DEFAULT.findInnerDataInfo(outsideModel, outsideItem, displayItem, matchValue);
            }

            List<String> selectIds = DEFAULT.findInnerDataInfo(outsideModel, outsideItem, displayItem, matchValue)
                    .stream()
                    .flatMap(map -> map.values().stream())
                    .filter(Objects::nonNull)
                    .map(Objects::toString)
                    .collect(Collectors.toList());

            ReferenceDataInstance referenceDataInstance = REF_HANDLER.createDataModelInstance(refDisplayItem, formModelEntity, selectIds.get(0), Arrays.asList(ids.split(",")));

            Map<String, Object> result = new HashMap<>();
            result.put("value", referenceDataInstance.getDisplayValue());
            return Collections.singletonList(result);

        };

        public static InnerDataInfoHandler getInstance(ItemModelEntity displayItem) {
            switch (displayItem.getType()) {
                case Select:
                    return SELECT;
                case ReferenceList:
                    return REFERENCE_LIST;
                default:
                    return DEFAULT;
            }
        }
    }
}
