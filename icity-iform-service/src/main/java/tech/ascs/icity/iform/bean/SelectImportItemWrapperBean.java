package tech.ascs.icity.iform.bean;

import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.SelectDataSourceType;
import tech.ascs.icity.iform.api.model.TreeSelectDataSource;
import tech.ascs.icity.iform.model.*;

import java.util.List;
import java.util.Map;

/**
 * Select 和 TreeSelect 的Proxy
 *
 * @author renjie
 * @since 0.7.3
 **/
public class SelectImportItemWrapperBean {

    private ItemModelEntity itemModelEntity;

    private ColumnModelEntity columnModel;

    private String columnName;

    private Map<String, Object> data;

    private boolean multiple = false;

    private boolean tree = false;

    public SelectImportItemWrapperBean(ItemModelEntity item) {
        this.itemModelEntity = item;
        this.columnModel = item.getColumnModel();
        this.columnName = columnModel.getColumnName();
        if (item instanceof SelectItemModelEntity) {
            SelectItemModelEntity selectItem = (SelectItemModelEntity) item;
            this.multiple = selectItem.getMultiple();
        } else if (item instanceof TreeSelectItemModelEntity) {
            TreeSelectItemModelEntity treeEntity = (TreeSelectItemModelEntity) item;
            this.tree = true;
            this.multiple = treeEntity.getMultiple();
        } else {
            throw new ICityException("不支持的控件类型");
        }
    }

//    private static class SelectItemWrapper {
//
//        private SelectDataSourceType dataSourceType;
//
//        private String referenceDictionaryId;
//
//        private String referenceDictionaryItemId;
//
//    }
//
//    private static class TreeSelectItemWrapper {
//
//        private TreeSelectDataSource dataSourceType;
//
//        private String dataRange;
//    }


    public ItemModelEntity getItemModelEntity() {
        return itemModelEntity;
    }

    public void setItemModelEntity(ItemModelEntity itemModelEntity) {
        this.itemModelEntity = itemModelEntity;
    }

    public ColumnModelEntity getColumnModel() {
        return columnModel;
    }

    public void setColumnModel(ColumnModelEntity columnModel) {
        this.columnModel = columnModel;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isTree() {
        return tree;
    }

    public void setTree(boolean tree) {
        this.tree = tree;
    }
}
