package tech.ascs.icity.iform.utils;

import java.util.Map;

/**
 * @author renjie
 * @since 1.0.0
 **/
public class Reducers {

    /**
     * 将m1 和 m2 合并, 通过 执行 {@code m1.putAll(m2)}来完成
     */
    public static <K,V> Map<K,V> reduceMap(Map<K,V> m1, Map<K,V> m2) {
        m1.putAll(m2);
        return m1;
    }

}
