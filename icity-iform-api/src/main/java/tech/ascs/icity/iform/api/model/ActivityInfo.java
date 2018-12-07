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

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
