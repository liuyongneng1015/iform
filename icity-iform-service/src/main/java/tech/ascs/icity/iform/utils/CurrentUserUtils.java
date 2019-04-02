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

import javax.servlet.http.HttpServletRequest;
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
        String userTokenKey = Constants.TOKEN_GET_USERINFO_PREFIX +token;
        UserInfo user = (UserInfo) redisTemplate.opsForValue().get(userTokenKey);
        if (user==null) {
            HttpServletRequest request = Application.getRequest();
            if (request == null) {
                throw new ICityException("无法获取请求");
            }
            try {
                UserInfo userInfo = Application.getCurrentUser();
                if (userInfo != null) {
                    // 用户ID作为缓存的key，通过用户ID可以直接找到token，若后台编辑了用户信息，通过用户ID定位到token，然后通过定位到缓存的用户信息，把缓存的用户信息更改过来
                    redisTemplate.opsForValue().set(Constants.USERID_GET_TOKEN_PREFIX+userInfo.getId(), token, 300, TimeUnit.SECONDS);
                    redisTemplate.opsForValue().set(userTokenKey, userInfo, 300, TimeUnit.SECONDS);
                    return userInfo;
                } else {
                    throw new ICityException(401, 401, "您的登录已失效，请重新登录");
                }
            } catch (Exception e) {
                throw new ICityException(401, 401, "您的登录已失效，请重新登录");
            }
        } else {
            return user;
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