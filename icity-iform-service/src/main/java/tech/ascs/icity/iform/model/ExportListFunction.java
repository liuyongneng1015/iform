package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.export.ExportControl;
import tech.ascs.icity.iform.api.model.export.ExportFormat;
import tech.ascs.icity.iform.api.model.export.ExportType;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author renjie
 * @since 0.7.3
 **/
@Entity
@Table(name = "ifm_export_list_function")
public class ExportListFunction extends JPAEntity implements Serializable {

    @Enumerated(EnumType.STRING)
    private ExportType type;

    @Enumerated(EnumType.STRING)
    private ExportFormat format;

    @Enumerated(EnumType.STRING)
    private ExportControl control;

    @Column(length = 4096)
    private String customExport;


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

    public String getCustomExport() {
        return customExport;
    }

    public void setCustomExport(String customExport) {
        this.customExport = customExport;
    }
}
