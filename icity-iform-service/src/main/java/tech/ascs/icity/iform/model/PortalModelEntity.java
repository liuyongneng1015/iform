package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * 门户建模
 */
@Entity
@Table(name = "ifm_portal_model")
public class PortalModelEntity extends BaseEntity {
    /**
     * 对象可能是列表，报表，通知，新闻，流程时间，快捷表单，邮件，资料栏，消息提醒，日历，通讯录，布局控件标题，描述，一行多列，标签页
    private List<Object> items;
     */
    private String description;

    // 控件的排序顺序字段
    @Column(name = "item_sort", length = 4096)
    private String itemsSort;

    public PortalModelEntity() { }

    public PortalModelEntity(String description, String itemsSort) {
        this.description = description;
        this.itemsSort = itemsSort;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemsSort() {
        return itemsSort;
    }

    public void setItemsSort(String itemsSort) {
        this.itemsSort = itemsSort;
    }
}
