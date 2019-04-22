package tech.ascs.icity.iform.api.model;

/**
 * 服务返回结果
 */
public enum ReturnResult {
    /** 无 */
    NONE("NONE"),

    /** 带URL */
    URL("URL");

    private String value;

    ReturnResult(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
