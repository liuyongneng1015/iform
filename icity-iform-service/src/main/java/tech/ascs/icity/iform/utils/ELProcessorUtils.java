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

    private static final String PREFIX = "_";

    private static final String EL_PREFIX = "${";
    private static final String EL_SUFFIX = "}";

    /**
     * 处理表达式中的32为的key, 对其在前面加入一个_, 预防当使用
     * 数字开头的变量的时候会报错的问题
     *
     * @param expression 表达式
     * @return 返回加工后的表达式
     */
    public static String process(String expression) {
        String newExpression = expression.replaceAll(KEY_FILTER_REGEX, PREFIX + "$1")
                .replaceAll(UNDEFINED_REGEX, "null")
                .replaceAll(ABS_EQUAL_REGEX, "==")
                .replaceAll(ABS_NO_EQUAL_REGEX, "!=");

        if (!newExpression.trim().startsWith(EL_PREFIX)) {
            newExpression = EL_PREFIX + newExpression;
        }
        if (!newExpression.trim().endsWith(EL_SUFFIX)) {
            newExpression = newExpression + EL_SUFFIX;
        }

        return newExpression;

    }
}
