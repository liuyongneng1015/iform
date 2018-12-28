package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("文件上传")
public class FileUploadModel extends NameEntity {

	@ApiModelProperty(value = "文件上传的key", position = 6)
	private String key;

	@ApiModelProperty(value = "文件上传的地址", position = 6)
	private String url;

	@ApiModelProperty(value = "文件上传类型", position = 6)
	private FileUploadType uploadType;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public FileUploadType getUploadType() {
		return uploadType;
	}

	public void setUploadType(FileUploadType uploadType) {
		this.uploadType = uploadType;
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
