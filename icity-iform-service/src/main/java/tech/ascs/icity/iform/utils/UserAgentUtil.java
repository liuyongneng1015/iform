package tech.ascs.icity.iform.utils;

import eu.bitwalker.useragentutils.*;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class UserAgentUtil {

    //获取设备类型
    public static String getDeviceType(String userAgent) {
        if (StringUtils.hasText(userAgent)) {
            try {
                StringBuffer sb = new StringBuffer();
                UserAgent userAgentClient = UserAgent.parseUserAgentString(userAgent);
                OperatingSystem operatingSystem = userAgentClient.getOperatingSystem(); // 操作系统信息
                String osName = operatingSystem.getName();
                if (StringUtils.hasText(osName)) {
                    sb.append(osName);
                }
//            DeviceType deviceType = operatingSystem.getDeviceType();
//            if (deviceType!=null) {
//                sb.append(" "+deviceType.getName().toLowerCase());
//            }
                Browser browser = userAgentClient.getBrowser();
                if (browser != null) {
                    sb.append(" " + browser.getName().toLowerCase());
                }
                Version version = userAgentClient.getBrowserVersion();
                if (version != null) {
                    sb.append(" " + version);
                }
                return sb.toString();
            } catch (Exception e) {

            }
        }
        return null;
    }

}