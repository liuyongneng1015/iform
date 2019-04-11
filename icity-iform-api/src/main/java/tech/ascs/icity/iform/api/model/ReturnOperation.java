package tech.ascs.icity.iform.api.model;

/**
 * 返回操作
 */
public enum ReturnOperation {
    // 刷新
    Refresh("Refresh"),

    // 不刷新
    NoRefresh("NoRefresh"),

    // 跳转新url
    JumpNewUrl("JumpNewUrl"),

    // 关闭当前页
    CloseCurrentPage("CloseCurrentPage");

    private String value;

    ReturnOperation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
