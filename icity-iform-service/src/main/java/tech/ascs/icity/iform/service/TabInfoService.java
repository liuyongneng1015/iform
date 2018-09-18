package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.model.TabInfo;
import tech.ascs.icity.jpa.service.JPAService;
import tech.ascs.icity.model.Page;

public interface TabInfoService extends JPAService<TabInfo> {
   
	public TabInfo findByTabName(String tabName);
	public Page<TabInfo> findByTabNameAndSynFlag(String tabName,Boolean synFlag,int page, int pageSize);

}
