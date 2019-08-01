package tech.ascs.icity.iform.api.model.export;

/**
 * 导出字段设置
 * @author renjie
 * @since 0.7.3
 **/
public enum ExportControl {

    // 列表显示内容导出
    List,

    // 导出全部
    All,

    // 后端自定义导出内容
    BackCustom,

    // 前端自定义导出内容
    FrontCustom;
}
