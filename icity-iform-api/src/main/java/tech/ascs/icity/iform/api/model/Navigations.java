package tech.ascs.icity.iform.api.model;

import tech.ascs.icity.model.NameEntity;

import java.util.Objects;

public class Navigations extends NameEntity {
    private Boolean initialPage;
    private String iconName;
    private String screenKey;
    private String screenType;

    public Navigations() { }

    public Navigations(Object idObj, Object nameObj, Object iconObj, Object screenKeyObj, Object screenTypeObj, Boolean initialPage) {
        if (idObj!=null) {
            this.setId(idObj.toString());
        }
        if (nameObj!=null) {
            this.setName(nameObj.toString());
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
        this.initialPage = initialPage;
    }
    
    public Boolean getInitialPage() {
        return initialPage;
    }

    public void setInitialPage(Boolean initialPage) {
        this.initialPage = initialPage;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Navigations that = (Navigations) o;
        return Objects.equals(getScreenType(), that.getScreenType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScreenType());
    }
}