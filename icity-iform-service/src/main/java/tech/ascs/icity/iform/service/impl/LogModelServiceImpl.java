package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.model.LogModelEntity;
import tech.ascs.icity.iform.service.LogModelService;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

public class LogModelServiceImpl extends DefaultJPAService<LogModelEntity> implements LogModelService {
    public LogModelServiceImpl() {
        super(LogModelEntity.class);
    }
}
