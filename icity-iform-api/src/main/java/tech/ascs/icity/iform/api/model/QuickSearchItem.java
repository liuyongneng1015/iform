package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("快速筛选QuickSearch")
public class QuickSearchItem extends NameEntity implements Comparable<QuickSearchItem> {
    @ApiModelProperty(value = "启用", position = 1)
    private Boolean use = false;

    @ApiModelProperty(value = "筛选控件", position = 2)
    private ItemModel itemModel;

    @ApiModelProperty(value = "筛选值", position = 3)
    private List<String> searchValues = new ArrayList<>();

    @ApiModelProperty(value = "数量显示", position = 4)
    private Boolean countVisible = false;

    @ApiModelProperty(value ="排序号", position = 5)
    private Integer orderNo = 0;

    public Boolean getUse() {
        return use;
    }

    public void setUse(Boolean use) {
        this.use = use;
    }

    public ItemModel getItemModel() {
        return itemModel;
    }

    public void setItemModel(ItemModel itemModel) {
        this.itemModel = itemModel;
    }

    public List<String> getSearchValues() {
        return searchValues;
    }

    public void setSearchValues(List<String> searchValues) {
        this.searchValues = searchValues;
    }

    public Boolean getCountVisible() {
        return countVisible;
    }

    public void setCountVisible(Boolean countVisible) {
        this.countVisible = countVisible;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    @Override
    public String getId() {
        String id = super.getId();
        if(StringUtils.isBlank(id)){
            return null;
        }
        return id;
    }

    @Override
    public int compareTo(QuickSearchItem o) {
        if (this.getOrderNo() == null && o.getOrderNo() == null) {
            return 0;
        }
        if (this.getOrderNo() == null) {
            return 1;
        }
        if (o.getOrderNo() == null) {
            return -1;
        }
        return this.getOrderNo() - o.getOrderNo();
    }
}
