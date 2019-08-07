package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import tech.ascs.icity.iform.api.model.DataPermissionsType;
import tech.ascs.icity.iform.api.model.DisplayDirection;
import tech.ascs.icity.iform.api.model.Location;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

/**
 * 列表模型
 */
@Entity
@Table(name = "ifm_list_model",
    indexes={@Index(name="ifm_list_model_unique_code_index", columnList = "uniqueCode", unique=false)})
public class ListModelEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String description;

	private Boolean multiSelect = true;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name="master_form")
	private FormModelEntity masterForm;

	@Column(name = "application_id")
	private String applicationId;//应用id

	private String uniqueCode;

	@ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name = "ifm_list_form",
		joinColumns = @JoinColumn( name="list_model"),
		inverseJoinColumns = @JoinColumn( name="form_model")
	)
	private List<FormModelEntity> slaverForms = new ArrayList<FormModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel")
	private List<ListSortItem> sortItems = new ArrayList<ListSortItem>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel")
	private List<ListFunction> functions = new ArrayList<ListFunction>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel")
	private List<ListSearchItem> searchItems = new ArrayList<ListSearchItem>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel", fetch=FetchType.LAZY)
	private List<QuickSearchEntity> quickSearchItems = new ArrayList<QuickSearchEntity>();

	@ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinTable(
		name = "ifm_list_display_item",
		joinColumns = @JoinColumn(name="list_id"),
		inverseJoinColumns = @JoinColumn(name="item_id")
	)
	private List<ItemModelEntity> displayItems = new ArrayList<ItemModelEntity>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "listModel")
	private List<ImportTemplateEntity> templateEntities = new ArrayList<>();

	// 展示字段排序
	@Column(name = "display_item_sort", length = 4096)
	private String displayItemsSort;

	// 查询字段排序
	@Column(name = "search_item_sort", length = 4096)
	private String searchItemsSort;

	// APP列表展示模板
	@Column(columnDefinition="text")
	private String appListTemplate;

	// 门户列表样式展示模板
	@Column(columnDefinition="text")
	private String protalListTemplate;

	@Enumerated(EnumType.STRING)
	private DataPermissionsType dataPermissions;

	// 字段名称是否显示
	private Boolean fieldDisplay;
	// 显示方向，横向或者竖向

	@Enumerated(EnumType.STRING)
	private DisplayDirection displayDirection;

	private String uuid;
	// 门户列表的icon图标
	private String icon;
	// 栏目高度
	private Integer columnHeight;
	// 栏目宽度
	private Integer columnWidth;
	// 栏目位置
	@Enumerated(EnumType.STRING)
	private Location columnLocation;

	// 展示操作栏
	private Boolean showActionBar = true;

	// 展示序号
	private Boolean showSequenceNumber = true;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getMultiSelect() {
		return multiSelect;
	}

	public void setMultiSelect(Boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public FormModelEntity getMasterForm() {
		return masterForm;
	}

	public void setMasterForm(FormModelEntity masterForm) {
		this.masterForm = masterForm;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getUniqueCode() {
		return uniqueCode;
	}

	public void setUniqueCode(String uniqueCode) {
		this.uniqueCode = uniqueCode;
	}

	public List<FormModelEntity> getSlaverForms() {
		return slaverForms;
	}

	public void setSlaverForms(List<FormModelEntity> slaverForms) {
		this.slaverForms = slaverForms;
	}

	public List<ListSortItem> getSortItems() {
		return sortItems;
	}

	public void setSortItems(List<ListSortItem> sortItems) {
		this.sortItems = sortItems;
	}

	public List<ListFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<ListFunction> functions) {
		this.functions = functions;
	}

	public List<ListSearchItem> getSearchItems() {
		return searchItems;
	}

	public void setSearchItems(List<ListSearchItem> searchItems) {
		this.searchItems = searchItems;
	}

	public List<QuickSearchEntity> getQuickSearchItems() {
		return quickSearchItems;
	}

	public void setQuickSearchItems(List<QuickSearchEntity> quickSearchItems) {
		this.quickSearchItems = quickSearchItems;
	}

	public List<ItemModelEntity> getDisplayItems() {
		return displayItems;
	}

	public void setDisplayItems(List<ItemModelEntity> displayItems) {
		this.displayItems = displayItems;
	}

	public String getDisplayItemsSort() {
		return displayItemsSort;
	}

	public void setDisplayItemsSort(String displayItemsSort) {
		this.displayItemsSort = displayItemsSort;
	}

	public String getSearchItemsSort() {
		return searchItemsSort;
	}

	public void setSearchItemsSort(String searchItemsSort) {
		this.searchItemsSort = searchItemsSort;
	}

	public String getAppListTemplate() {
		return appListTemplate;
	}

	public void setAppListTemplate(String appListTemplate) {
		this.appListTemplate = appListTemplate;
	}

	public String getProtalListTemplate() {
		return protalListTemplate;
	}

	public void setProtalListTemplate(String protalListTemplate) {
		this.protalListTemplate = protalListTemplate;
	}

	public DataPermissionsType getDataPermissions() {
		return dataPermissions;
	}

	public void setDataPermissions(DataPermissionsType dataPermissions) {
		this.dataPermissions = dataPermissions;
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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
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

	public Boolean getShowActionBar() {
		return showActionBar;
	}

	public void setShowActionBar(Boolean showActionBar) {
		this.showActionBar = showActionBar;
	}

	public Boolean getShowSequenceNumber() {
		return showSequenceNumber;
	}

	public void setShowSequenceNumber(Boolean showSequenceNumber) {
		this.showSequenceNumber = showSequenceNumber;
	}

	public List<ImportTemplateEntity> getTemplateEntities() {
		return templateEntities;
	}

	public void setTemplateEntities(List<ImportTemplateEntity> templateEntities) {
		this.templateEntities = templateEntities;
	}
}