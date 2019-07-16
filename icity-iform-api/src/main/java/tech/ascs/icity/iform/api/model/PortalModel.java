package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("门户建模PortalModel")
public class PortalModel extends NameEntity {
    @ApiModelProperty(value = "描述", position = 1)
    private List<PortalItemModel> items = new ArrayList<>();

    @ApiModelProperty(value = "描述", position = 2)
    private String description;

    public PortalModel() { }

    public List<PortalItemModel> getItems() {
        return items;
    }

    public void setItems(List<PortalItemModel> items) {
        this.items = items;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
