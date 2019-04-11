package tech.ascs.icity.iform.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
    /**
     * hql模糊搜索参数转换,包含添加通配符和转义通配符
     * @param parameter
     * @return
     */
    public static String convertParamOfFuzzySearch(String parameter) {
        return "%" + parameter.replace("%","\\%").replace("_","\\_") + "%";
    }

    public static String date2Str(Date date, String dateFormat){
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(date);
    }

    public static String currentTimeStr(String dateFormat){
        return date2Str(new Date(), dateFormat);
    }

    //异常编码
    public static int exceptionCode = 404;
}
