package tech.ascs.icity.iform.api.model.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author renjie
 * @since 0.7.3
 **/
@ApiModel("导入功能按钮")
public class ImportFunctionModel {

    @ApiModelProperty(value = "导入文件类型", position = 0)
    private ImportFileType fileType;

    @ApiModelProperty(value = "导入模型", position = 1)
    private ImportType type;

    @ApiModelProperty(value = "字段名行", position = 2)
    private int headerRow;

    @ApiModelProperty(value = "开始数据行", position = 3)
    private int startRow;

    @ApiModelProperty(value = "结束数据行", position = 4)
    private int endRow;

    @ApiModelProperty(value = "日期排序格式", position = 5)
    private String dateFormatter;

    @ApiModelProperty(value = "日期分隔符", position = 6)
    private String dateSeparator;

    @ApiModelProperty(value = "时间分隔符", position = 7)
    private String timeSeparator;

    private List<ImportTemplateItemModel> templateItemModels;

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

    public int getHeaderRow() {
        return headerRow;
    }

    public void setHeaderRow(int headerRow) {
        this.headerRow = headerRow;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public String getDateFormatter() {
        return dateFormatter;
    }

    public void setDateFormatter(String dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public String getDateSeparator() {
        return dateSeparator;
    }

    public void setDateSeparator(String dateSeparator) {
        this.dateSeparator = dateSeparator;
    }

    public String getTimeSeparator() {
        return timeSeparator;
    }

    public void setTimeSeparator(String timeSeparator) {
        this.timeSeparator = timeSeparator;
    }

    public List<ImportTemplateItemModel> getTemplateItemModels() {
        return templateItemModels;
    }

    public void setTemplateItemModels(List<ImportTemplateItemModel> templateItemModels) {
        this.templateItemModels = templateItemModels;
    }
}
