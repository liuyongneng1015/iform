package tech.ascs.icity.iform.model;

import javax.persistence.Embeddable;

/**
 * 门户建模控件
 */
@Embeddable
public class PortalModelItem {
    private String id;
    private String type;
    private String name;

    public PortalModelItem() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
