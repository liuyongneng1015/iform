package tech.ascs.icity.iform.api.model;

/**
 * 列表建模的功能类型
 */
public enum ListFunctionType {
    // 调用服务
    InvokeService("InvokeService"),

    // 跳转Url
    JumpURL("JumpURL");

    private String value;

    ListFunctionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
