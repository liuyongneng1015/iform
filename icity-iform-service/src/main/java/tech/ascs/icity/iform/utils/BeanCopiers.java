package tech.ascs.icity.iform.utils;

import org.springframework.cglib.beans.BeanCopier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于Spring Bean Copier 的bean copy的类
 * @author renjie
 * @since 0.7.3
 **/
public class BeanCopiers {

    private static final Map<String, BeanCopier> noConvertCache = new ConcurrentHashMap<>(32);
    private static final Map<String, BeanCopier> useConvertCache = new ConcurrentHashMap<>(32);

    public static BeanCopier create(Class source, Class target, boolean useConvert) {
        Map<String, BeanCopier> cache = useConvert ? useConvertCache : noConvertCache;
        String key = source.getName() + target.getName();
        if (!cache.containsKey(key)){
            cache.put(key, BeanCopier.create(source, target, useConvert));
        }
        return cache.get(key);
    }


    public static void noConvertCopy(Object source, Object target) {
        if (source!=null && target!=null) {
            create(source.getClass(), target.getClass(), false).copy(source, target, null);
        }
    }
}
