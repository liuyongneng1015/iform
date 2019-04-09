package tech.ascs.icity.iform.api.model;

/**
 * 功能类型
 */
public enum FunctionType {

    /** 列表 */
    list("list"),

    /** 工作流列表 */
    activitiList("activitiList"),

    /** URL */
    url("url");

    private String value;

    FunctionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}