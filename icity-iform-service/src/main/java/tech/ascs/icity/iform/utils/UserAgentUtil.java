package tech.ascs.icity.iform.utils;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class UserAgentUtil {
    public static List<String> devices = Arrays.asList("postman", "android", "iphone", "chrome", "ipad");
    public static String noDevice="未知设备";

    //获取用户操作系统信息
    public static String getClientType(String userAgentStr){
        if (StringUtils.isEmpty(userAgentStr)) return noDevice;
        for (String device:devices) {
            if (userAgentStr.toLowerCase().contains(device)) {
                return device;
            }
        }
        return noDevice;
    }

    //获取系统类型
    public static String getSystemTypeVersion(String userAgent) {
        try {
            if (StringUtils.isEmpty(userAgent)) return noDevice;
            String clientType = UserAgentUtil.getClientType(userAgent);
            if (clientType.equals("android")) {
                // Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30
                // 以括号切割提取  Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D
                String rex = "[()]+";
                String[] deviceInfo = userAgent.split(rex);
                if (deviceInfo.length < 2) return null;
                deviceInfo = deviceInfo[1].split(";");
                if (deviceInfo.length < 3) return null;
                String androidVersion = deviceInfo[2];
                if (StringUtils.hasText(androidVersion)) {
                    return androidVersion.trim();
                }
                return null;
            } else if (clientType.equals("iphone")) {
                // Mozilla/5.0 (iPhone; CPU iPhone clientType 11_0 like Mac clientType X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1
                // 以括号切割提取  iPhone; CPU iPhone clientType 11_0 like Mac clientType X
                String[] deviceInfo = userAgent.split("[()]+");
                if (deviceInfo.length < 2) return null;
                deviceInfo = deviceInfo[1].split(" ");
                if (deviceInfo.length<5) return null;
                String iosVersion = deviceInfo[4];
                return "ios "+iosVersion.replaceAll("_",".");
            } else if (clientType.equals("ipad")) {
                // Mozilla/5.0 (iPad; CPU clientType 11_0 like Mac clientType X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1
                // 以括号切割提取  iPhone; CPU iPhone clientType 11_0 like Mac clientType X
                String[] deviceInfo = userAgent.split("[()]+");
                if (deviceInfo.length < 2) return null;
                deviceInfo = deviceInfo[1].split(" ");
                if (deviceInfo.length<4) return null;
                String iosVersion = deviceInfo[3];
                return "ios "+iosVersion.replaceAll("_",".");
            } else if (clientType.equals("chrome")) {
                // Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36
                String[] deviceInfo = userAgent.split("[()]+");
                if (deviceInfo.length < 5) return null;
                deviceInfo = deviceInfo[4].trim().split(" ");
                return deviceInfo[0].trim().replaceAll("/", " ");
            }
        } catch (Exception e) { }
        return userAgent;
    }

}