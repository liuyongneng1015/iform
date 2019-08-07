package tech.ascs.icity.iform.utils;

import com.google.common.collect.Sets;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.ItemType;
import tech.ascs.icity.iform.model.ItemModelEntity;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * ItemModelEntity 的处理工具类
 * <ul>
 * <li>
 * 遍历出所有的ItemModelEntity
 * </li>
 * </ul>
 *
 * @author renjie
 * @since 0.7.3
 **/
public class ItemModelHandlerUtils {

    /**
     * 遍历ItemModelEntity, 其中的List<ItemModelEntity> 会被被递归遍历,
     * @param entitys 控件实体列表
     * @param ignoreTypes 忽略的控件类型, 当遇到包含的控件类型的时候, 将会跳过该控件的遍历
     * @return 返回遍历后的控件列表, 可能存在重复
     */
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
