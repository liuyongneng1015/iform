package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.FileReferenceType;

import javax.persistence.*;

/**
 * 文件表单控件模型
 */
@Entity
@Table(name = "ifm_file_item_model")
@DiscriminatorValue("fileItemModel")
public class FileItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 35431L;

	@Column(name="file_reference_type")
	@Enumerated(EnumType.STRING)
	private FileReferenceType fileReferenceType = FileReferenceType.Attachment;

	@Column(name="multiple")//是否允许多传
	private Boolean multiple = false;

	@Column(name="file_size_limit")//文件大小限制M
	private Integer fileSizeLimit = 10;

	@Column(name="file_number_limit")//文件数量限制
	private Integer fileNumberLimit;

	@Column(name="file_format")//文件格式
	private String fileFormat;

	public FileReferenceType getFileReferenceType() {
		return fileReferenceType;
	}

	public void setFileReferenceType(FileReferenceType fileReferenceType) {
		this.fileReferenceType = fileReferenceType;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public Integer getFileSizeLimit() {
		return fileSizeLimit;
	}

	public void setFileSizeLimit(Integer fileSizeLimit) {
		this.fileSizeLimit = fileSizeLimit;
	}

	public Integer getFileNumberLimit() {
		return fileNumberLimit;
	}

	public void setFileNumberLimit(Integer fileNumberLimit) {
		this.fileNumberLimit = fileNumberLimit;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
}