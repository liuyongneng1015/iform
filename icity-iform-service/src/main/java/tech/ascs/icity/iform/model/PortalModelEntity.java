package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 门户建模
 */
@Entity
@Table(name = "ifm_portal_model")
public class PortalModelEntity extends BaseEntity {
    /**
     * 对象可能是列表，报表，通知，新闻，流程时间，快捷表单，邮件，资料栏，消息提醒，日历，通讯录，布局控件标题，描述，一行多列，标签页
     * private List<Object> items;
     */
    @Column(columnDefinition="text")
    private String items = "[]";  // 一定是List集合的json字符串，每个item里面一定要有 id, type
    private String description;

    /** 排序号 */
    @Column(name = "order_no")
    private Integer orderNo = 0;

    public PortalModelEntity() { }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }
}
