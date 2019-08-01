package tech.ascs.icity.iform.api.model.export;

/**
 * @author renjie
 * @since 0.7.3
 **/
public enum  ImportType {

    /**
     * 追加或更新
     */
    SaveOrUpdate,

    /**
     * 只更新
     */
    UpdateOnly,

    /**
     * 只追加
     */
    SaveOnly,

    /**
     * 覆盖
     */
    Rewrite;

}
