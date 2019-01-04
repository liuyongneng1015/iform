package tech.ascs.icity.iform.api.model;

/**
 * 用于给admin服务传递封装好的功能按钮的权限格式
 */
public class ListFormBtnPermission {
    private ItemBtnPermission listPermissions;
    private ItemBtnPermission formPermissions;

    public ListFormBtnPermission() { }

    public ItemBtnPermission getListPermissions() {
        return listPermissions;
    }

    public void setListPermissions(ItemBtnPermission listPermissions) {
        this.listPermissions = listPermissions;
    }

    public ItemBtnPermission getFormPermissions() {
        return formPermissions;
    }

    public void setFormPermissions(ItemBtnPermission formPermissions) {
        this.formPermissions = formPermissions;
    }
}
