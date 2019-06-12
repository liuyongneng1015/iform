package tech.ascs.icity.iform.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author renjie
 * @since 0.7.2
 **/
@Entity
@Table(name = "ifm_reference_inner_item_model")
@DiscriminatorValue("referenceInnerItemModel")
public class ReferenceInnerItemModelEntity extends ItemModelEntity{

    private static final long serialVersionUID = 742421L;

    /**
     * 关联表单 - 外部表单
     */
    @Column(name="reference_outside_form_id")// 关联表单模型id
    private String referenceOutsideFormId;

    /**
     * 关联表单控件 - 外部表单控件, 用作匹配
     */
    @Column(name="reference_outside_item_id")// 关联控件模型id
    private String referenceOutsideItemId;

    @Column(name = "reference_outside_item_uuid")
    private String referenceOutsideItemUuid;

    /**
     * 关联的展示控件
     */
    @Column(name="reference_item_id")// 关联控件模型id
    private String referenceItemId;

    @Column(name = "reference_item_uuid")
    private String referenceItemUuid;

    /**
     * 本表关联控件
     */
    @Column(name = "reference_inner_item_id")
    private String referenceInnerItemId;

    @Column(name = "reference_inner_item_uuid")
    private String referenceInnerItemUuid;

    public String getReferenceOutsideFormId() {
        return referenceOutsideFormId;
    }

    public void setReferenceOutsideFormId(String referenceOutsideFormId) {
        this.referenceOutsideFormId = referenceOutsideFormId;
    }

    public String getReferenceOutsideItemId() {
        return referenceOutsideItemId;
    }

    public void setReferenceOutsideItemId(String referenceOutsideItemId) {
        this.referenceOutsideItemId = referenceOutsideItemId;
    }

    public String getReferenceItemId() {
        return referenceItemId;
    }

    public void setReferenceItemId(String referenceItemId) {
        this.referenceItemId = referenceItemId;
    }

    public String getReferenceInnerItemId() {
        return referenceInnerItemId;
    }

    public void setReferenceInnerItemId(String referenceInnerItemId) {
        this.referenceInnerItemId = referenceInnerItemId;
    }

    public String getReferenceOutsideItemUuid() {
        return referenceOutsideItemUuid;
    }

    public void setReferenceOutsideItemUuid(String referenceOutsideItemUuid) {
        this.referenceOutsideItemUuid = referenceOutsideItemUuid;
    }

    public String getReferenceItemUuid() {
        return referenceItemUuid;
    }

    public void setReferenceItemUuid(String referenceItemUuid) {
        this.referenceItemUuid = referenceItemUuid;
    }

    public String getReferenceInnerItemUuid() {
        return referenceInnerItemUuid;
    }

    public void setReferenceInnerItemUuid(String referenceInnerItemUuid) {
        this.referenceInnerItemUuid = referenceInnerItemUuid;
    }
}
