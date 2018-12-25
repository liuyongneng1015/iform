package tech.ascs.icity.iform.api.model;

public enum SelectReferenceType {
    Dictionary("Dictionary"),//数据字典
    Table("Table"),//表
    Fixed("Fixed");//固定值

    private String value;

    private SelectReferenceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
