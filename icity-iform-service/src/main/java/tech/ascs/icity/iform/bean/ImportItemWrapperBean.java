package tech.ascs.icity.iform.bean;

import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.model.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author renjie
 * @since 0.7.3
 **/
public class ImportItemWrapperBean {

    private String columnName;

    private ItemModelEntity itemModelEntity;

    private DataModelEntity dataModel;

    private List<HashMap> datas;

    private ReferenceType referenceType;

    private ListModelEntity listModelEntity;

    public ImportItemWrapperBean(ItemModelEntity itemModelEntity) {
        this.itemModelEntity = itemModelEntity;
        if (itemModelEntity instanceof ReferenceItemModelEntity) {
            ReferenceItemModelEntity referenceItemModelEntity = (ReferenceItemModelEntity) itemModelEntity;
            if (referenceItemModelEntity.getColumnModel() != null) {
                this.setColumnName(itemModelEntity.getColumnModel().getColumnName());
            }else {
                this.setColumnName(findReferenceListManyToManyColumnName(referenceItemModelEntity));
            }
            this.setDataModel(referenceItemModelEntity.getReferenceList().getMasterForm().getDataModels().get(0));
            this.setReferenceType(referenceItemModelEntity.getReferenceType());
            this.setListModelEntity(referenceItemModelEntity.getReferenceList());
        }else if (itemModelEntity.getColumnModel() != null) {
            this.setColumnName(itemModelEntity.getColumnModel().getColumnName());
            ColumnReferenceEntity referenceEntity = itemModelEntity.getColumnModel().getColumnReferences().get(0);
            this.setDataModel(referenceEntity.getToColumn().getDataModel());
            this.setReferenceType(referenceEntity.getReferenceType());
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public DataModelEntity getDataModel() {
        return dataModel;
    }

    public void setDataModel(DataModelEntity dataModel) {
        this.dataModel = dataModel;
    }

    public List<HashMap> getDatas() {
        return datas;
    }

    public void setDatas(List<HashMap> datas) {
        this.datas = datas;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public ItemModelEntity getItemModelEntity() {
        return itemModelEntity;
    }

    public void setItemModelEntity(ItemModelEntity itemModelEntity) {
        this.itemModelEntity = itemModelEntity;
    }

    public ListModelEntity getListModelEntity() {
        return listModelEntity;
    }

    public void setListModelEntity(ListModelEntity listModelEntity) {
        this.listModelEntity = listModelEntity;
    }

    private String findReferenceListManyToManyColumnName(ReferenceItemModelEntity referenceItemModelEntity) {
        return referenceItemModelEntity.getReferenceList().getMasterForm().getDataModels().get(0).getTableName() + "_list";
    }
}
