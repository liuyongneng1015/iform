package tech.ascs.icity.iform.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {

    private final static String time_format = "yyyy-MM-dd";
    public final static String regEx = "[a-zA-Z]{1,}[a-zA-Z0-9_-]{0,}";
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

    public static String currentDateStr(){
        return date2Str(new Date(), time_format);
    }


    //异常编码
    public static int exceptionCode = 404;
}
