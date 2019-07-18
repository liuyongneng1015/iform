package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("文件上传")
public class FileUploadModel extends NameEntity {

	@ApiModelProperty(value = "文件上传的key", position = 3)
	private String fileKey;

	@ApiModelProperty(value = "文件大小", position = 3)
	private Integer fileSize;

	@ApiModelProperty(value = "文件上传的地址", position = 4)
	private String url;

	@ApiModelProperty(value = "来源控件或者表单id", position = 5)
	private String fromSource;

	@ApiModelProperty(value = "来源数据id", position = 6)
	private String fromSourceDataId;

	@ApiModelProperty(value = "缩略图", position = 7)
	private String thumbnail;

	@ApiModelProperty(value = "缩略图地址", position = 8)
	private String thumbnailUrl;

	@ApiModelProperty(value = "数据来源类型", position = 9)
	private DataSourceType sourceType = DataSourceType.ItemModel;

	public FileUploadModel() { }

	public FileUploadModel(String id, String name) {
		super(id, name);
	}

	public String getFileKey() {
		return fileKey;
	}

	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}

	public Integer getFileSize() {
		return fileSize;
	}

	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
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

	public String getFromSourceDataId() {
		return fromSourceDataId;
	}

	public void setFromSourceDataId(String fromSourceDataId) {
		this.fromSourceDataId = fromSourceDataId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public DataSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(DataSourceType sourceType) {
		this.sourceType = sourceType;
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
