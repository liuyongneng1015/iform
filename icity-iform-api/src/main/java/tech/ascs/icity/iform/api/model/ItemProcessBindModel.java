package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("控件绑定流程")
public class ItemProcessBindModel extends NameEntity {


	@ApiModelProperty(value = "环节id", position = 3)
	private String activityId;

	@ApiModelProperty(value = "环节名称", position = 4)
	private String activityName;


	@ApiModelProperty(value = "控件环节模型", position = 4)
	private List<ItemActivityInfoModel> activityInfoModels = new ArrayList<>();

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

	public List<ItemActivityInfoModel> getActivityInfoModels() {
		return activityInfoModels;
	}

	public void setActivityInfoModels(List<ItemActivityInfoModel> activityInfoModels) {
		this.activityInfoModels = activityInfoModels;
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
