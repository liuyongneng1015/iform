package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * 操作日志
 */
@Entity
@Table(name = "ifm_log_model",
       indexes = {@Index(name="ifm_log_model_app_id_index", columnList="appId", unique=false),
                  @Index(name="ifm_log_model_user_id_index", columnList="userId", unique=false),
                  @Index(name="ifm_log_model_menu_id_index", columnList="menuId", unique=false)})
public class LogModelEntity extends JPAEntity {
    private String userId;        // 用户ID
    private String appId;         // 应用
    private String menuId;        // 菜单
    private String operate;       // 操作
    private String httpMethod;    // 请求方式
    @Column(length = 4096)
    private String urlParams;     // url参数
    private String deviceType;    // 终端类型
    private String userAgent;     // userAgent
    @Column(columnDefinition="text")
    private String bodyParams;    // 请求体参数
    private Date operateTime;     // 操作时间
    private String operateSystem; // 操作系统

    public LogModelEntity() { }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(String urlParams) {
        this.urlParams = urlParams;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(String bodyParams) {
        this.bodyParams = bodyParams;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperateSystem() {
        return operateSystem;
    }

    public void setOperateSystem(String operateSystem) {
        this.operateSystem = operateSystem;
    }
}