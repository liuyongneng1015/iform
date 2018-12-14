package tech.ascs.icity.iform.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.iflow.api.model.Activity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("环节摘要信息")
public class ActivityInfo extends Activity {

	@ApiModelProperty(value = "是否可见", position = 2)
	private boolean visible;

	@ApiModelProperty(value = "是否只读", position = 3)
	private boolean readonly;

	@ApiModelProperty(value = "流程id", position = 4)
	private String activityId;

	@ApiModelProperty(value = "流程名称", position = 5)
	private String activityName;

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

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
}
