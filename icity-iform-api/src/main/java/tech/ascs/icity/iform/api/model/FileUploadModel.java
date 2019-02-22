package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("文件上传")
public class FileUploadModel extends NameEntity {

	@ApiModelProperty(value = "文件上传的key", position = 3)
	private String fileKey;

	@ApiModelProperty(value = "文件上传的地址", position = 4)
	private String url;

	@ApiModelProperty(value = "来源数据id", position = 5)
	private String fromSource;

	@ApiModelProperty(value = "文件上传类型", position = 6)
	private FileUploadType uploadType = FileUploadType.ItemModel;

	public String getFileKey() {
		return fileKey;
	}

	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFromSource() {
		return fromSource;
	}

	public void setFromSource(String fromSource) {
		this.fromSource = fromSource;
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
