package tech.ascs.icity.iform.api.model;

public enum ParamCondition {
    //选中记录
    SelectRecords("SelectRecords"),

    //查询条件
    QueryConditions("QueryConditions");

    private String value;

    ParamCondition(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
