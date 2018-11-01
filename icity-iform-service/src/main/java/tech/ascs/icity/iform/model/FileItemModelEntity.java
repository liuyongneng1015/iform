package tech.ascs.icity.iform.model;

import javax.persistence.*;

/**
 * 文件表单控件模型
 */
@Entity
@Table(name = "ifm_file_item_model")
@DiscriminatorValue("fileItemModel")
public class FileItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;
	public static enum FileReferenceType {
		Image,//图片
		Video,//视频
		Attachment//附件
	}

	@Column(name="file_reference_type")
	@Enumerated(EnumType.STRING)
	private FileReferenceType fileReferenceType;

	@Column(name="file_path")
	private String filePath;

	public FileReferenceType getFileReferenceType() {
		return fileReferenceType;
	}

	public void setFileReferenceType(FileReferenceType fileReferenceType) {
		this.fileReferenceType = fileReferenceType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}