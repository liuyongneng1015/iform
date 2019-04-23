package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dashboard extends NameEntity {
    private String iconName;
    private String screenKey;
    private String screenType;
    private String categoryCode;
    private String categoryName;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Dashboard> children = new ArrayList();

    public Dashboard() { }

    public Dashboard(String id, String name) {
        setId(id);
        setName(name);
    }

    public Dashboard(Object idObj, Object nameObj, Object iconObj, Object screenKeyObj,
                     Object screenTypeObj, Object categoryCodeObj, Object categoryNameObj) {
        if (idObj!=null) {
            setId(idObj.toString());
        }
        if (nameObj!=null) {
            setName(nameObj.toString());
        }
        if (iconObj!=null) {
            this.iconName = iconObj.toString();
        }
        if (screenKeyObj!=null) {
            this.screenKey = screenKeyObj.toString();
        }
        if (screenTypeObj!=null) {
            this.screenType = screenTypeObj.toString();
        }
        if (categoryCodeObj!=null) {
            this.categoryCode = categoryCodeObj.toString();
        }
        if (categoryNameObj!=null) {
            this.categoryName = categoryNameObj.toString();
        }
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getScreenKey() {
        return screenKey;
    }

    public void setScreenKey(String screenKey) {
        this.screenKey = screenKey;
    }

    public String getScreenType() {
        return screenType;
    }

    public void setScreenType(String screenType) {
        this.screenType = screenType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<Dashboard> getChildren() {
        return children;
    }

    public void setChildren(List<Dashboard> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dashboard dashboard = (Dashboard) o;
        return Objects.equals(getCategoryCode(), dashboard.getCategoryCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCategoryCode());
    }
}
