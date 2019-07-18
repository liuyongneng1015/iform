package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.DataSourceType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 文件上传
 */
@Entity
@Table(name = "ifm_file_upload")
public class FileUploadEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "file_key", length = 256)
	private String fileKey;

	@Column(name = "file_size")
	private Integer fileSize;


	//图片地址
	@Column(name = "file_url", length = 512)
	private String url;

	//来源控件或者表单id
	@Column(name = "from_source", length = 64)
	private String fromSource;

	//来源对象数据id
	@Column(name = "from_source_data_id", length = 64)
	private String fromSourceDataId;

	//缩略图
	@Column(name = "thumbnail", length = 256)
	private String thumbnail;

	@Column(name = "缩略图地址", length = 512)
	private String thumbnailUrl;

	//来源对象类型
	@JoinColumn(name="source_type")
	@Enumerated(EnumType.STRING)
	private DataSourceType sourceType;

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

	public DataSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(DataSourceType sourceType) {
		this.sourceType = sourceType;
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
}