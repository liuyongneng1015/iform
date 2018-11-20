package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.util.List;

public interface FormModelService extends JPAService<FormModelEntity> {
     List<ItemModelEntity> getAllColumnItems(List<ItemModelEntity> itemModelEntities);
}
