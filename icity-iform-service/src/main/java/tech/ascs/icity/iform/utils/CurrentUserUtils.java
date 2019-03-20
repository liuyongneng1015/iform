package tech.ascs.icity.iform.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.rbac.feign.model.UserInfo;
import tech.ascs.icity.rbac.util.Application;

import javax.servlet.http.HttpServletRequest;

public class CurrentUserUtils {

    public static String getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            throw new ICityException(401, 401, "未登录");
        }
        try {
            String userId = queryCurrentUserId(token);
            if (userId!=null) {
                return userId;
            } else {
                throw new ICityException(401, 401, "您的登录已失效，请重新登录");
            }
        } catch (Exception e) {
            throw new ICityException(401, 401, "您的登录已失效，请重新登录");
        }
    }

    private static String queryCurrentUserId(String token) {
        String userId = CacheUtils.get(token+"-userId");
        if (userId!=null) {
            return userId;
        } else {
            UserInfo user = Application.getCurrentUser();
            if (user!=null) {
                userId = user.getId();
                CacheUtils.put(token+"-userId", userId, 3600000);
                return userId;
            } else {
                return null;
            }
        }
    }

    public static String getUserHeadPortraitFileId(String userId) {
        String fileId = CacheUtils.get(userId+"-userId");
        return fileId;
    }
}
