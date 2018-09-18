package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.model.TabInfo;
import tech.ascs.icity.iform.service.TabInfoService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;

public class TabInfoServiceImpl extends DefaultJPAService<TabInfo> implements
		TabInfoService {
	private JPAManager<TabInfo> jPAManager;

	public TabInfoServiceImpl() {
		super(TabInfo.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		jPAManager = getJPAManagerFactory().getJPAManager(TabInfo.class);
	}

	public TabInfo findByTabName(String tabName) {
		return jPAManager.query().filterEqual("tabName", tabName).first();
	}

	public Page<TabInfo> findByTabNameAndSynFlag(String tabName,
			Boolean synFlag, int page, int pageSize) {
		tech.ascs.icity.jpa.dao.Query<TabInfo, TabInfo> query = jPAManager
				.query();
		if (tabName == null || "".equals(tabName)) {
		} else {
			query.filterEqual("tabName", tabName);
		}

		if (synFlag != null) {
			query.filterEqual("synFlag", synFlag);
		}

		return query.page(page, pageSize).page();

	}

}
