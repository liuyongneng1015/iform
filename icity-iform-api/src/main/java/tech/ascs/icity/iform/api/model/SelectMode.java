package tech.ascs.icity.iform.api.model;

public enum SelectMode {
    Single("Single"),//单选
    Multiple("Multiple"),//多选
    Inverse("Inverse");//反选
    private String value;
    private SelectMode(String value) {
        this.value= value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
