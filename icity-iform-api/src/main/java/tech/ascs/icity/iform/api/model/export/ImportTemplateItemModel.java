package tech.ascs.icity.iform.api.model.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author renjie
 * @since 0.7.3
 **/
@ApiModel("导入模板字段设置模型")
public class ImportTemplateItemModel {

    @ApiModelProperty("控件id")
    private String id;

    @ApiModelProperty("控件名称")
    private String itemName;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("是否被选中")
    private boolean key;

    @ApiModelProperty("是否导入")
    private boolean imported;

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

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }
}
