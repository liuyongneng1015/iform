package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel("文件控件模型ItemModel")
public class FileItemModel extends ItemModel {

	@ApiModelProperty(value = "文件类型", position = 11)
	private FileReferenceType fileReferenceType;

	@ApiModelProperty(value = "文件路径", position = 12)
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
