package tech.ascs.icity.iform.api.model;

/**
 * 解析区域
 */
public enum ParseArea {
    /** 无 */
    APP("APP"),

    /** 带URL */
    PC("PC");

    private String value;

    ParseArea(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
