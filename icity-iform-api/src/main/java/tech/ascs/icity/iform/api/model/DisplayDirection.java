package tech.ascs.icity.iform.api.model;

/** 展示方向 */
public enum DisplayDirection {
    /** 横向 */
    Horizontal("Horizontal"),

    /** 竖向 */
    Vertical("Vertical");

    private String value;

    DisplayDirection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
