package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.Map;

@ApiModel("返回结果")
public class ResponseResult extends NameEntity {

	@ApiModelProperty(value = "返回结果编码", position = 3)
	private int code;

	@ApiModelProperty(value = "返回结果消息", position = 4)
	private String message;

	@ApiModelProperty(value = "返回结果内容", position = 5)
	private Map<String, Object> result;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, Object> getResult() {
		return result;
	}

	public void setResult(Map<String, Object> result) {
		this.result = result;
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
