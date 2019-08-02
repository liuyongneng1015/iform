package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.export.ImportFileType;
import tech.ascs.icity.iform.api.model.export.ImportType;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author renjie
 * @since 0.7.3
 **/
@Entity
@Table(name = "ifm_import_base_function")
public class ImportBaseFunctionEntity extends JPAEntity implements Serializable {

    @Enumerated(EnumType.STRING)
    private ImportFileType fileType;

    @Enumerated(EnumType.STRING)
    private ImportType type;

    private int headerRow = 1;

    private int startRow = 2;

    private int endRow;

    private String dateFormatter;

    private String dateSeparator;

    private String timeSeparator;

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
}
