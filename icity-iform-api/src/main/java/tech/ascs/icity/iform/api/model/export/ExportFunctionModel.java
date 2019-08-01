package tech.ascs.icity.iform.api.model.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author renjie
 * @since 0.7.3
 **/
@ApiModel("导出设置数据模型")
public class ExportFunctionModel {

    @ApiModelProperty("导出类型")
    private ExportType type;

    @ApiModelProperty("导出格式")
    private ExportFormat format;

    @ApiModelProperty("导出控件")
    private ExportControl control;

    @ApiModelProperty("自定义导出控件(后端设置)")
    private List<String> customExport = new ArrayList<>();

    public ExportType getType() {
        return type;
    }

    public void setType(ExportType type) {
        this.type = type;
    }

    public ExportFormat getFormat() {
        return format;
    }

    public void setFormat(ExportFormat format) {
        this.format = format;
    }

    public ExportControl getControl() {
        return control;
    }

    public void setControl(ExportControl control) {
        this.control = control;
    }

    public List<String> getCustomExport() {
        return customExport;
    }

    public void setCustomExport(List<String> customExport) {
        this.customExport = customExport;
    }
}
