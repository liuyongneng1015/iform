package tech.ascs.icity.iform.utils;

import eu.bitwalker.useragentutils.*;
import org.springframework.util.StringUtils;

public class UserAgentUtil {

    //获取设备类型
    public static String getDeviceType(String userAgent) {
        if (StringUtils.hasText(userAgent)) {
            try {
                StringBuffer sb = new StringBuffer();
                UserAgent userAgentClient = UserAgent.parseUserAgentString(userAgent);
                OperatingSystem operatingSystem = userAgentClient.getOperatingSystem(); // 操作系统信息
                if (operatingSystem!=null && StringUtils.hasText(operatingSystem.getName())
                        && "unknown".equals(operatingSystem.getName().toLowerCase().trim())==false) {
                    sb.append(operatingSystem.getName());
                } else {
                    return userAgent;
                }
                Browser browser = userAgentClient.getBrowser();
                if (browser != null && StringUtils.hasText(browser.getName())
                        && "unknown".equals(browser.getName().toLowerCase().trim())==false) {
                    sb.append(" " + browser.getName().toLowerCase());
                } else {
                    return userAgent;
                }
                Version version = userAgentClient.getBrowserVersion();
                if (version != null && StringUtils.hasText(version.getVersion())
                        && "unknown".equals(version.getVersion().toLowerCase().trim())==false) {
                    sb.append(" " + version.getVersion());
                }
                return sb.toString();
            } catch (Exception e) {

            }
        }
        return userAgent;
    }

}