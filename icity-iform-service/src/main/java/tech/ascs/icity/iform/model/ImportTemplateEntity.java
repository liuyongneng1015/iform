package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

import javax.persistence.*;

/**
 * @author renjie
 * @since 0.7.3
 **/
@Entity
@Table(name = "ifm_import_template")
public class ImportTemplateEntity extends JPAEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "item_id")
    private ItemModelEntity itemModel;

    @ManyToOne(cascade =  {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "list_id")
    private ListModelEntity listModel;

    @Column(name = "template_name") //模板控件名称
    private String templateName;

    @Column(name = "match_key", columnDefinition = "boolean default false") //匹配主键
    private boolean matchKey = false;

    @Column(name = "template_selected", columnDefinition = "boolean default false") //模板选中, 导出模板字段
    private boolean templateSelected = false;

    @Column(name = "data_imported", columnDefinition = "boolean default false") //是否导入该列数据
    private boolean dataImported = false;

    @Column(name = "example_data")
    private String exampleData;


    public ItemModelEntity getItemModel() {
        return itemModel;
    }

    public void setItemModel(ItemModelEntity itemModel) {
        this.itemModel = itemModel;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isMatchKey() {
        return matchKey;
    }

    public void setMatchKey(boolean matchKey) {
        this.matchKey = matchKey;
    }

    public boolean isTemplateSelected() {
        return templateSelected;
    }

    public void setTemplateSelected(boolean templateSelected) {
        this.templateSelected = templateSelected;
    }

    public boolean isDataImported() {
        return dataImported;
    }

    public void setDataImported(boolean dataImported) {
        this.dataImported = dataImported;
    }

    public String getExampleData() {
        return exampleData;
    }

    public void setExampleData(String exampleData) {
        this.exampleData = exampleData;
    }

    public ListModelEntity getListModel() {
        return listModel;
    }

    public void setListModel(ListModelEntity listModel) {
        this.listModel = listModel;
    }
}
