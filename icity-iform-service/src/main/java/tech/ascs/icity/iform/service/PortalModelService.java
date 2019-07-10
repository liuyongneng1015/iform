package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.model.PortalModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

public interface PortalModelService extends JPAService<PortalModelEntity> {
    Integer maxOrderNo();
    void moveModel(String id, String action);
}
