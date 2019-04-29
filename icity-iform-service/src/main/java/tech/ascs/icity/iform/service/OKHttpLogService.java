package tech.ascs.icity.iform.service;


import tech.ascs.icity.iform.api.model.BusinessTriggerType;
import tech.ascs.icity.iform.model.BusinessTriggerEntity;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.OKHttpLogEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.Map;


public interface OKHttpLogService extends JPAService<OKHttpLogEntity> {

   OKHttpLogEntity saveOKHttpLog(OKHttpLogEntity entity);

    void sendOKHttpRequest(BusinessTriggerEntity triggerEntity, FormModelEntity formModelEntity, Map<String, Object> map);

}
