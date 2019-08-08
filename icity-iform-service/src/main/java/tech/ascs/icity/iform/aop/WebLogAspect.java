package tech.ascs.icity.iform.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.ascs.icity.admin.api.model.Resource;
import tech.ascs.icity.admin.client.ResourceService;
import tech.ascs.icity.iform.model.LogModelEntity;
import tech.ascs.icity.iform.service.LogModelService;
import tech.ascs.icity.iform.utils.UserAgentUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;

@Aspect
@Configuration
public class WebLogAspect {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private LogModelService logModelService;
    @Autowired
    private ResourceService resourceService;

    @Pointcut("execution(* tech.ascs.icity.iform.controller..*.*(..))") // 成功的
    public void webLog() { }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 判断方法上面是否有 RequestMapping 注解，有的话才拦截
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Resource resource = currentRequestLogResource(request);
        if (resource==null) {
            return;
        }
//        String userId = CurrentUserUtils.getCurrentUserId();
        String userAgentStr = request.getHeader("User-Agent");
        String operate = request.getParameter("operate");
        String menuId = request.getParameter("menuId");
        LogModelEntity entity = new LogModelEntity();
        entity.setDeviceType(UserAgentUtil.getDeviceType(userAgentStr));
        entity.setAppId(resource.getApplicationId());
        entity.setOperate(resource.getResourceName());
        entity.setHttpMethod(request.getMethod());
        entity.setOperateTime(new Date());
        entity.setUserAgent(userAgentStr);
        entity.setOperate(operate);
        entity.setUserId("test-user-id");
        entity.setMenuId(menuId);
        String urlParams = request.getQueryString();
        entity.setUrlParams(StringUtils.hasText(urlParams) && urlParams.length()>4096 ? urlParams.substring(4096):urlParams);
        if (hasRequestBody(request)) {
            Object[] args = joinPoint.getArgs();
            if (args!=null && args.length>0) {
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                Method method = methodSignature.getMethod();
                Object requestBody = getRequestBodyParamsInMethod(method, args);
                entity.setBodyParams(requestBody!=null? mapper.writeValueAsString(requestBody):"{}");
            }
        }
        logModelService.save(entity);
    }

    public Boolean hasRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (request.getContentLength()>0 &&
            StringUtils.hasText(contentType) &&
            contentType.toLowerCase().contains("application/json") ) {
            return true;
        }
        return false;
    }

    public Boolean currentMethodIsRequest() {
        return null;
    }

    /** 在方法上面获取请求体的参数 */
    public static Object getRequestBodyParamsInMethod(Method method, Object[] args) {
        Annotation[][] paramAnnotationArr = method.getParameterAnnotations();
        for (int i = 0; i < paramAnnotationArr.length; i++) {
            Annotation[] paramAnnotations = paramAnnotationArr[i];
            for (int j = 0; j < paramAnnotations.length; j++) {
                if (paramAnnotations[j] instanceof RequestBody) {
                    return args[i];
                }
            }
        }
        return null;
    }

    public Resource currentRequestLogResource(HttpServletRequest request) {
        /**
        List<Resource> resources = resourceService.queryNeedDoWebLogResources();
        if (resources!=null && resources.size()>0) {
            return resources.get(0);
        } else {
            return null;
        }
         */
        Resource resource = new Resource();
        resource.setId("0000000000");
        resource.setParentId("0000000000");
        resource.setApplicationId("000000000000");
        resource.setAssociationListId("00000000000");
        return resource;
    }
}