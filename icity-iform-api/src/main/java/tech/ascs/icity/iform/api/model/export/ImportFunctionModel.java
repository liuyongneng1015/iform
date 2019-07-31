package tech.ascs.icity.iform.api.model.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author renjie
 * @since 0.7.3
 **/
@ApiModel("导入功能按钮")
public class ImportFunctionModel {

    @ApiModelProperty("导入文件类型")
    private ImportFileType fileType;

    @ApiModelProperty("导入模型")
    private ImportType type;

    public ImportFileType getFileType() {
        return fileType;
    }

    public void setFileType(ImportFileType fileType) {
        this.fileType = fileType;
    }

    public ImportType getType() {
        return type;
    }

    public void setType(ImportType type) {
        this.type = type;
    }
}
