package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.model.ListFunction;
import tech.ascs.icity.jpa.service.JPAService;


public interface FormFunctionsService extends JPAService<ListFunction> {

    Integer getMaxOrderNo();

}
