package tech.ascs.icity.iform.api.model;

/**
 * 解析区域
 */
public enum ParseArea {
    /** 表单设计器用的 */

    /** APP列表页 */
    appList("appList"),

    /** APP详情页 */
    appCheck("appCheck"),

    /** APP更新页 */
    appUpdate("appUpdate"),

    /** APP新增页 */
    appAdd("appAdd"),

    /** PC列表页 */
    pcList("pcList"),

    /** PC详情页 */
    pcCheck("pcCheck"),

    /** PC更新页 */
    pcUpdate("pcUpdate"),

    /** PC新增页 */
    pcAdd("pcAdd"),

    /** 列表设计器用的 */
    /** APP */
    APP("APP"),

    /** PC */
    PC("PC"),

    /** 模糊搜索 */
    FuzzyQuery("FuzzyQuery");

    private String value;

    ParseArea(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
