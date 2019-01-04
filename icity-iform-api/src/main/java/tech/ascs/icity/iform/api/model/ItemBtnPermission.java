package tech.ascs.icity.iform.api.model;

import tech.ascs.icity.model.IdEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表或者表单的功能按钮
 */
public class ItemBtnPermission extends IdEntity {
    public static class Permission extends IdEntity {
        private String name;
        private String code;
        public Permission() { }
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
    private List<Permission> permissions = new ArrayList();

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}