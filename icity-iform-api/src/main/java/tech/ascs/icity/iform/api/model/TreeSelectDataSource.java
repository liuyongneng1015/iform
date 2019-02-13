package tech.ascs.icity.iform.api.model;

public enum TreeSelectDataSource {
    Department("Department"),//部门
    Station("Station"),//岗位
    Personnel("Personnel"),//反选
    Position("Position"),//岗位标识
    Other("Other");//其他
    private String value;
    private TreeSelectDataSource(String value) {
        this.value= value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
