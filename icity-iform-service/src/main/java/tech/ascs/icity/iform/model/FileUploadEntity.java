package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.FileUploadType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传
 */
@Entity
@Table(name = "ifm_file_upload")
public class FileUploadEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String key;

	//来源对象id
	private String fromSource;

	//来源对象类型
	@JoinColumn(name="upload_type")
	@Enumerated(EnumType.STRING)
	private FileUploadType uploadType;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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
}