package tech.ascs.icity.iform.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.config.Constants;
import tech.ascs.icity.rbac.feign.model.UserInfo;
import tech.ascs.icity.rbac.util.Application;

import java.util.concurrent.TimeUnit;

@Component
public class CurrentUserUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static RedisTemplate<String,Object> redisTemplate;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CurrentUserUtils.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String,Object> redisTemplate) {
        CurrentUserUtils.redisTemplate = redisTemplate;
    }

    public static UserInfo getCurrentUser() {
        String token = Application.getRequest().getHeader("token");
        if (StringUtils.isEmpty(token)) {
            throw new ICityException(401, 401, "未登录");
        }

        String getUserIdByTokenKey =  Constants.GET_USERID_BY_TOKEN + token;
        String userId = (String)redisTemplate.opsForValue().get(getUserIdByTokenKey);
        if (StringUtils.hasText(userId)) {
            String getUserInfoByUserIdKey = Constants.GET_USERINFO_BY_USERID + userId;
            UserInfo user = (UserInfo) redisTemplate.opsForValue().get(getUserInfoByUserIdKey);
            if (user!=null) {
                return user;
            } else {
                return assemblyUserInfo();
            }
        } else {
            return assemblyUserInfo();
        }
    }

    public static UserInfo assemblyUserInfo() {
        try {
            String token = Application.getRequest().getHeader("token");
            UserInfo userInfo = Application.getCurrentUser();
            if (userInfo != null) {
                // token拼接的字符串作为redis的key，通过token可以定位到用户ID
                // 用户ID拼接的字符串作为redis的Key，通过用户ID可以定位到用户数据，admin服务通过用户ID修改了用户数据，可以同步更新redis的用户数据
                String userId = userInfo.getId();
                redisTemplate.opsForValue().set(Constants.GET_USERID_BY_TOKEN+token, userId, 60, TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(Constants.GET_USERINFO_BY_USERID+userId, userInfo, 60, TimeUnit.SECONDS);
                return userInfo;
            } else {
                throw new ICityException(401, 401, "您的登录已失效，请重新登录");
            }
        } catch (Exception e) {
            throw new ICityException(401, 401, "您的登录已失效，请重新登录");
        }
    }

    public static String getCurrentUserId() {
        UserInfo user = getCurrentUser();
        if (user!=null) {
            return user.getId();
        } else {
            throw new ICityException(401, 401, "您的登录已失效，请重新登录");
        }
    }
}