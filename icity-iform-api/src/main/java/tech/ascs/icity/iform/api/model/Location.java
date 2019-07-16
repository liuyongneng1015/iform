package tech.ascs.icity.iform.api.model;

/**
 * 栏目位置
 */
public enum Location {

    /** 靠左 */
    Left("Left"),
    /** 靠右 */
    Right("Right"),
    /** 居中 */
    Center("Center");

    private String value;
    private Location(String value) {
        this.value= value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
