package tech.ascs.icity.iform.utils;

/**
 * EL处理器工具类
 *
 * @author renjie
 * @since 0.7.3
 **/
public class ELProcessorUtils {

    private static final String KEY_FILTER_REGEX = "([0-9a-zA-z]{32}(?!\\s*'|\\s*\"))";

    private static final String UNDEFINED_REGEX = "(undefined)";

    private static final String ABS_EQUAL_REGEX = "(===)";
    private static final String ABS_NO_EQUAL_REGEX = "(!==)";

    private static final String PREFIX = "#_";

    private static final String ARRAY_REGEX = "\\[([\\w,\\s]*)\\]";

    /**
     * 处理表达式中的32为的key, 对其在前面加入一个_, 预防当使用
     * 数字开头的变量的时候会报错的问题
     *
     * @param expression 表达式
     * @return 返回加工后的表达式
     */
    public static String process(String expression) {

        return expression
                //把所有32位id替换为 #_id的形式, #表示Spel里面的变量
                .replaceAll(KEY_FILTER_REGEX, PREFIX + "$1")
                //把 undefined 替换为null
                .replaceAll(UNDEFINED_REGEX, "null")
                //把js 的 === 替换为 ==
                .replaceAll(ABS_EQUAL_REGEX, "==")
                //把js 的 !== 替换为 !=
                .replaceAll(ABS_NO_EQUAL_REGEX, "!=")
                //把js 的数组表示方式 []  替换为 new Object[]{} 的形式
                .replaceAll(ARRAY_REGEX, "new Object[]{$1}");
    }
}
