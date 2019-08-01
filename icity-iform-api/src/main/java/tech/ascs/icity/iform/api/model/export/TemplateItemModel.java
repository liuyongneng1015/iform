package tech.ascs.icity.iform.api.model.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author renjie
 * @since 0.7.3
 **/
@ApiModel("控件导入模板模型")
public class TemplateItemModel {

    @ApiModelProperty("控件id")
    private String id;

    @ApiModelProperty("控件名称")
    private String itemName;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("是否被选中")
    private boolean selected;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
