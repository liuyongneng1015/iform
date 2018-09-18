package tech.ascs.icity.iform.service.impl;

import java.util.List;

import tech.ascs.icity.iform.model.Form;
import tech.ascs.icity.iform.service.FormService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;

public class FormServiceImpl extends DefaultJPAService<Form> implements
		FormService {

	private JPAManager<Form> jPAManager;

	public FormServiceImpl() {
		super(Form.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		jPAManager = getJPAManagerFactory().getJPAManager(Form.class);
	}

	public Page<Form> findByName(String name, int page, int pageSize) {
		tech.ascs.icity.jpa.dao.Query<Form, Form> query = jPAManager.query();
		if (name == null || "".equals(name)) {
		} else {
			query.filterEqual("name", name);
		}
		return query.page(page, pageSize).page();
	}

	public List<Form> findByName(String name) {
		tech.ascs.icity.jpa.dao.Query<Form, Form> query = jPAManager.query();
		if (name == null || "".equals(name)) {
		} else {
			query.filterEqual("name", name);
		}
		return query.list();
	}
}
