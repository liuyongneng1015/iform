package tech.ascs.icity.iform.utils;

public class CommonUtils {
    /**
     * hql模糊搜索参数转换,包含添加通配符和转义通配符
     * @param parameter
     * @return
     */
    public static String convertParamOfFuzzySearch(String parameter) {
        return "%" + parameter.replace("%","\\%").replace("_","\\_") + "%";
    }
}
