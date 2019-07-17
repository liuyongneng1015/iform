package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("门户建模Item")
public class PortalItemModel extends NameEntity {

    @ApiModelProperty(value = "uuid", position = 3)
    private String uuid;

    @ApiModelProperty(value = "主表单模型", position = 4)
    private FormModel masterForm;

    @ApiModelProperty(value = "标题按钮设置", position = 7)
    private List<FunctionModel> functions = new ArrayList();

    @ApiModelProperty(value = "查询字段列表", position = 8)
    private List<SearchItem> searchItems = new ArrayList();

    @ApiModelProperty(value = "显示字段列表", position = 9)
    private List<ItemModel> displayItems = new ArrayList();

    @ApiModelProperty(value = "快速筛选", position = 10)
    private List<QuickSearchItem> quickSearchItems = new ArrayList();

    @ApiModelProperty(value = "表单绑定的数据模型列表", position = 11)
    private List<DataModel> dataModels = new ArrayList();

    @ApiModelProperty(value="关联关系控件集合",position = 15)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ItemModel> relevanceItemModelList = new ArrayList();

    @ApiModelProperty(value="字段名称是否显示",position = 17)
    private Boolean fieldDisplay;

    @ApiModelProperty(value="显示方向，横向或者竖向",position = 18)
    private DisplayDirection displayDirection;

    @ApiModelProperty(value="栏目高度", position = 19)
    private Integer columnHeight;

    @ApiModelProperty(value="栏目宽度", position = 20)
    private Integer columnWidth;

    @ApiModelProperty(value="栏目位置", position = 21)
    private Location columnLocation;

    @ApiModelProperty(value="快捷菜单设置", position = 22)
    private List<QuickMenuItem> menus = new ArrayList<>();

    @ApiModelProperty(value="新闻条数", position = 23)
    private Integer newsCount = 0;

    /** 门户建模控件，News 新闻 ，QuickMenu 快捷菜单，List 列表，Report 报表 ，通知 Notice */
    @ApiModelProperty(value="控件类型", position = 24)
    private ItemType type;

    public PortalItemModel() { }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public FormModel getMasterForm() {
        return masterForm;
    }

    public void setMasterForm(FormModel masterForm) {
        this.masterForm = masterForm;
    }

    public List<FunctionModel> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionModel> functions) {
        this.functions = functions;
    }

    public List<SearchItem> getSearchItems() {
        return searchItems;
    }

    public void setSearchItems(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }

    public List<ItemModel> getDisplayItems() {
        return displayItems;
    }

    public void setDisplayItems(List<ItemModel> displayItems) {
        this.displayItems = displayItems;
    }

    public List<QuickSearchItem> getQuickSearchItems() {
        return quickSearchItems;
    }

    public void setQuickSearchItems(List<QuickSearchItem> quickSearchItems) {
        this.quickSearchItems = quickSearchItems;
    }

    public List<DataModel> getDataModels() {
        return dataModels;
    }

    public void setDataModels(List<DataModel> dataModels) {
        this.dataModels = dataModels;
    }

    public List<ItemModel> getRelevanceItemModelList() {
        return relevanceItemModelList;
    }

    public void setRelevanceItemModelList(List<ItemModel> relevanceItemModelList) {
        this.relevanceItemModelList = relevanceItemModelList;
    }

    public Boolean getFieldDisplay() {
        return fieldDisplay;
    }

    public void setFieldDisplay(Boolean fieldDisplay) {
        this.fieldDisplay = fieldDisplay;
    }

    public DisplayDirection getDisplayDirection() {
        return displayDirection;
    }

    public void setDisplayDirection(DisplayDirection displayDirection) {
        this.displayDirection = displayDirection;
    }

    public Integer getColumnHeight() {
        return columnHeight;
    }

    public void setColumnHeight(Integer columnHeight) {
        this.columnHeight = columnHeight;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(Integer columnWidth) {
        this.columnWidth = columnWidth;
    }

    public Location getColumnLocation() {
        return columnLocation;
    }

    public void setColumnLocation(Location columnLocation) {
        this.columnLocation = columnLocation;
    }

    public List<QuickMenuItem> getMenus() {
        return menus;
    }

    public void setMenus(List<QuickMenuItem> menus) {
        this.menus = menus;
    }

    public Integer getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(Integer newsCount) {
        this.newsCount = newsCount;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    @Override
    public String getId() {
        String id = super.getId();
        if(StringUtils.isBlank(id)){
            return null;
        }
        return id;
    }
}
