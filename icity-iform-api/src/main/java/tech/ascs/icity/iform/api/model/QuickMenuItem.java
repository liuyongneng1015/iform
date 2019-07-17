package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

/**
 * 快捷菜单设置
 */
@ApiModel("快捷菜单设置")
public class QuickMenuItem extends NameEntity {
    @ApiModelProperty(value = "描述", position = 2)
    private String description;
    @ApiModelProperty(value = "访问地址", position = 3)
    private String url;
    @ApiModelProperty(value = "icon图标", position = 4)
    private String icon;
    @ApiModelProperty(value = "颜色和样式", position = 5)
    private String style;
    @ApiModelProperty(value = "打开方式", position = 6)
    private ReturnOperation openWay;
    public QuickMenuItem() { }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public ReturnOperation getOpenWay() {
        return openWay;
    }

    public void setOpenWay(ReturnOperation openWay) {
        this.openWay = openWay;
    }
}
