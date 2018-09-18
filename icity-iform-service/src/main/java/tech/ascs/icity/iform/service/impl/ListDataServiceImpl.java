package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.model.ListData;
import tech.ascs.icity.iform.service.ListDataService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;

public class ListDataServiceImpl extends DefaultJPAService<ListData> implements
		ListDataService {

	private JPAManager<ListData> jPAManager;

	public ListDataServiceImpl() {
		super(ListData.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		jPAManager = getJPAManagerFactory().getJPAManager(ListData.class);
	}

	public Page<ListData> findByName(String name, int page, int pageSize) {
		tech.ascs.icity.jpa.dao.Query<ListData, ListData> query = jPAManager
				.query();
		if (name == null || "".equals(name)) {
		} else {
			query.filterEqual("name", name);
		}
		return query.page(page, pageSize).page();
	}
}
