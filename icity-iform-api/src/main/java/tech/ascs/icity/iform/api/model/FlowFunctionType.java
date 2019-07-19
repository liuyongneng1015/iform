package tech.ascs.icity.iform.api.model;

/**
 * 流程功能类型
 */
public enum FlowFunctionType {

    /** 签收 */
    Sign("Sign"),

    /** 流转 */
    Circulation("Circulation"),

    /** 调用微服务 */
    InvokeService("InvokeService"),

    /** 跳转界面 */
    JumpURL("JumpURL"),

    /** 改变状态 */
    ChangeState("ChangeState");

    private String value;

    FlowFunctionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static FlowFunctionType getTypeByValue(String value){
        for(FlowFunctionType type: FlowFunctionType.values()){
            if(type.getValue().equals(value)){
                return type;
            }
        }
        return null;
    }
}