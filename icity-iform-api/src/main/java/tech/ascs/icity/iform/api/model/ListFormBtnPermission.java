package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 用于给admin服务传递封装好的功能按钮的权限格式
 */
public class ListFormBtnPermission {
    @ApiModelProperty(value = "列表ID", position = 1)
    private String listId;
    @ApiModelProperty(value = "表单ID", position = 2)
    private String formId;
    @ApiModelProperty(value = "列表按钮权限", position = 3)
    private List<BtnPermission> listPermissions;
    @ApiModelProperty(value = "表单按钮权限", position = 4)
    private List<BtnPermission> formPermissions;

    public ListFormBtnPermission() { }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public List<BtnPermission> getListPermissions() {
        return listPermissions;
    }

    public void setListPermissions(List<BtnPermission> listPermissions) {
        this.listPermissions = listPermissions;
    }

    public List<BtnPermission> getFormPermissions() {
        return formPermissions;
    }

    public void setFormPermissions(List<BtnPermission> formPermissions) {
        this.formPermissions = formPermissions;
    }
}
