package tech.ascs.icity.iform.utils;

import java.util.regex.Pattern;

/**
 * EL处理器工具类
 *
 * @author renjie
 * @since 0.7.3
 **/
public class ELProcessorUtils {

    private static final String KEY_FILTER_REGEX = "([0-9a-zA-z]{32}(?!\\s*'|\\s*\"))";

    private static final Pattern PATTERN = Pattern.compile(KEY_FILTER_REGEX);

    private static final String PREFIX = "_";

    /**
     *
     * 处理表达式中的32为的key, 对其在前面加入一个_, 预防当使用
     * 数字开头的变量的时候会报错的问题
     * @param expression 表达式
     * @return 返回加工后的表达式
     */
    public static String process(String expression) {
        return expression.replaceAll(KEY_FILTER_REGEX, PREFIX + "$1");
    }
}
