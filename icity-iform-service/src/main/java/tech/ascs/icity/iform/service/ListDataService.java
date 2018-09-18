package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.model.ListData;
import tech.ascs.icity.jpa.service.JPAService;
import tech.ascs.icity.model.Page;

public interface ListDataService extends JPAService<ListData> {
	
	public Page<ListData> findByName(String name,int page, int pageSize);

}
