package tech.ascs.icity.iform.service;

import java.util.List;

import tech.ascs.icity.iform.model.Form;
import tech.ascs.icity.jpa.service.JPAService;
import tech.ascs.icity.model.Page;

public interface FormService extends JPAService<Form> {
	public Page<Form> findByName(String name,int page, int pageSize);
	public List<Form> findByName(String name);
}
