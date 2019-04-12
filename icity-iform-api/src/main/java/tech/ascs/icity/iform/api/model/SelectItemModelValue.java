package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

@ApiModel("表单下拉控件模型的取值")
public class SelectItemModelValue extends NameEntity {
    public SelectItemModel itemModel;
    /** 编码比如0,1 对应name字段男女 */
    @ApiModelProperty(value = "字典表的英文Key", position = 7)
    private String code;

    /** 描述 */
    @ApiModelProperty(value = "字典表的中文描述", position = 7)
    private String description;

    public SelectItemModelValue() { }

    public SelectItemModel getItemModel() {
        return itemModel;
    }

    public void setItemModel(SelectItemModel itemModel) {
        this.itemModel = itemModel;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}