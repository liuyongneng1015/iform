package tech.ascs.icity.iform.api.model;

public enum ControlType {
    Select("Select"),//选择框
    List("List");//列表

    private String value;

    private ControlType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
