package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.model.PortalModelEntity;
import tech.ascs.icity.iform.service.PortalModelService;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

public class PortalModelServiceImpl extends DefaultJPAService<PortalModelEntity> implements PortalModelService {
    public PortalModelServiceImpl() {
        super(PortalModelEntity.class);
    }
}
