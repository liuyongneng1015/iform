package tech.ascs.icity.iform.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.iflow.api.model.Activity;
import tech.ascs.icity.model.NameEntity;

@ApiModel("表单控件模型ItemModel")
public class ItemModel extends NameEntity {

	@ApiModel("环节摘要信息")
	public static class ActivityInfo extends Activity {

		@ApiModelProperty(value = "是否可见", position = 7)
		private boolean visible;

		@ApiModelProperty(value = "是否只读", position = 7)
		private boolean readonly;

		@JsonIgnore
		@Override
		public String getFormKey() {
			return super.getFormKey();
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public boolean isReadonly() {
			return readonly;
		}

		public void setReadonly(boolean readonly) {
			this.readonly = readonly;
		}
	}

	public static class Option {
		@ApiModelProperty(value = "显示名称", position = 0)
		private String label;
		@ApiModelProperty(value = "值", position = 1)
		private String value;
		public Option() {}
		public Option(String label, String value) {
			this.label = label;
			this.value = value;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	@ApiModelProperty(value = "控件类型", position = 3)
	private ItemType type;

	@ApiModelProperty(value = "前端个性化属性（直接存json字符串，后端不做处理）", position = 4)
	private String props;

	@ApiModelProperty(value = "数据字段模型", position = 5)
	private ColumnModelInfo columnModel;

	@ApiModelProperty(value = "流程环节配置", position = 7)
	private List<ActivityInfo> activities = new ArrayList<ActivityInfo>();

	@ApiModelProperty(value = "Select选项列表", position = 8)
	private List<Option> options = new ArrayList<Option>();

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

	public ColumnModelInfo getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(ColumnModelInfo columnModel) {
		this.columnModel = columnModel;
	}

	public List<ActivityInfo> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityInfo> activities) {
		this.activities = activities;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}
}
