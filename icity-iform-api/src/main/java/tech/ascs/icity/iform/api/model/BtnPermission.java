package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.IdEntity;

/**
 * 按钮权限
 */
@ApiModel("按钮权限")
public class BtnPermission extends IdEntity {
    @ApiModelProperty(value = "权限名称", position = 1)
    private String name;
    @ApiModelProperty(value = "权限码", position = 2)
    private String code;
    public BtnPermission() { }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
}