package tech.ascs.icity.iform.model;

import org.hibernate.annotations.DiscriminatorOptions;
import tech.ascs.icity.iform.api.model.ItemType;
import tech.ascs.icity.iform.api.model.SystemItemType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 表单控件模型
 */
@Entity
@Table(name = "ifm_item_model")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force=true)
@DiscriminatorValue(value = "baseItemModel")
public class ItemModelEntity extends  BaseEntity{

	private static final long serialVersionUID = 21321L;

	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="form_id")
	private FormModelEntity formModel;

	@ManyToOne(cascade = {CascadeType.REFRESH})
	@JoinColumn(name="column_id")
	private ColumnModelEntity columnModel;

	@JoinColumn(name="type")
	@Enumerated(EnumType.STRING)
	private ItemType type;

	@Column(name="props",length = 2048)
	private String props;

	@Column(name="app_props",length = 2048)
	private String appProps;

	@Column(name="hidden_conditions",columnDefinition="text")//隐藏条件
	private String hiddenCondition;

	@Column(name="order_no",columnDefinition = "int default 0")//排序号
	private Integer orderNo = 0;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "itemModel")
	private List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "itemModel")
	private List<ItemSelectOption> options = new ArrayList<ItemSelectOption>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "itemModel")
	private List<ItemPermissionInfo> permissions = new ArrayList<ItemPermissionInfo>();

	@JoinColumn(name="system_item_type")//系统控件类型
	@Enumerated(EnumType.STRING)
	private SystemItemType systemItemType;

	@Column(name="uuid", length = 255) // 控件标识
	private String uuid;

	@Column(name="reference_uuid", length = 255) // 关联控件标识
	private String referenceUuid;

	@Column(name="uniquene") // 是否唯一
	private Boolean uniquene = false;

	@Column(name="source_form_model_id") // 属于哪个表单控件
	private String sourceFormModelId;

	@Column(name="type_key") // 前端用的控件类型key
	private String typeKey;

	@Column(name="icon") // 控件图片
	private String icon;

	@Column(name = "hide_expression", length = 2048) // 隐藏表达式
	private String hideExpression;

	@Column(name = "evaluate_expression", length = 2048) // 赋值表达式
	private String evaluateExpression;

	@Column(name = "trigger_items") //控件change的时候会触发的控件uuid列表
	private String triggerIds;

	@Column(name = "template_name") //模板控件名称
	private String templateName;

	@Column(name = "match_key", columnDefinition = "boolean default false") //匹配主键
	private boolean matchKey = false;

	@Column(name = "template_selected" , columnDefinition = "boolean default false") //模板选中, 导出模板字段
	private boolean templateSelected = false;

	@Column(name = "data_imported", columnDefinition = "boolean default false") //是否导入该列数据
	private boolean dataImported = false;


	public FormModelEntity getFormModel() {
		return formModel;
	}

	public void setFormModel(FormModelEntity formModel) {
		this.formModel = formModel;
	}

	public ColumnModelEntity getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(ColumnModelEntity columnModel) {
		this.columnModel = columnModel;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public String getAppProps() {
		return appProps;
	}

	public void setAppProps(String appProps) {
		this.appProps = appProps;
	}

	public List<ItemActivityInfo> getActivities() {
		return activities;
	}

	public String getHiddenCondition() {
		return hiddenCondition;
	}

	public void setHiddenCondition(String hiddenCondition) {
		this.hiddenCondition = hiddenCondition;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public void setActivities(List<ItemActivityInfo> activities) {
		this.activities = activities;
	}

	public List<ItemSelectOption> getOptions() {
		return options;
	}

	public void setOptions(List<ItemSelectOption> options) {
		this.options = options;
	}

    public List<ItemPermissionInfo> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<ItemPermissionInfo> permissions) {
        this.permissions = permissions;
    }

    public SystemItemType getSystemItemType() {
		return systemItemType;
	}

	public void setSystemItemType(SystemItemType systemItemType) {
		this.systemItemType = systemItemType;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getReferenceUuid() {
		return referenceUuid;
	}

	public void setReferenceUuid(String referenceUuid) {
		this.referenceUuid = referenceUuid;
	}

	public Boolean getUniquene() {
		return uniquene;
	}

	public void setUniquene(Boolean uniquene) {
		this.uniquene = uniquene;
	}

	public String getSourceFormModelId() {
		return sourceFormModelId;
	}

	public void setSourceFormModelId(String sourceFormModelId) {
		this.sourceFormModelId = sourceFormModelId;
	}

	public String getTypeKey() {
		return typeKey;
	}

	public void setTypeKey(String typeKey) {
		this.typeKey = typeKey;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

    public String getHideExpression() {
        return hideExpression;
    }

    public void setHideExpression(String hideExpression) {
        this.hideExpression = hideExpression;
    }

    public String getEvaluateExpression() {
        return evaluateExpression;
    }

    public void setEvaluateExpression(String evaluateExpression) {
        this.evaluateExpression = evaluateExpression;
    }

	public String getTriggerIds() {
		return triggerIds;
	}

	public void setTriggerIds(String triggerIds) {
		this.triggerIds = triggerIds;
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
}