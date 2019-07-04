package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.model.NameEntity;

import java.util.List;

@ApiModel("门户建模PortalModel")
public class PortalModel extends NameEntity {
    @ApiModelProperty(value = "描述", position = 1)
    private List<Object> items;

    @ApiModelProperty(value = "描述", position = 2)
    private String description;

    public PortalModel() { }

    public List<Object> getItems() {
        return items;
    }

    public void setItems(List<Object> items) {
        this.items = items;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
