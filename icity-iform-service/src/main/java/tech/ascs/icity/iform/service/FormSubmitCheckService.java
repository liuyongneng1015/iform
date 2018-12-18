package tech.ascs.icity.iform.service;

import org.springframework.data.jpa.repository.Query;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.jpa.service.JPAService;


public interface FormSubmitCheckService extends JPAService<FormSubmitCheckInfo> {

    Integer getMaxOrderNo();

}
