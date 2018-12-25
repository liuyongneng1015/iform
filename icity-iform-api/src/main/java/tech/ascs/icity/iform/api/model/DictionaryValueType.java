package tech.ascs.icity.iform.api.model;

public enum DictionaryValueType {
    Linkage("Linkage"),//联动值
    Fixed("Fixed");//固定值

    private String value;

    private DictionaryValueType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
