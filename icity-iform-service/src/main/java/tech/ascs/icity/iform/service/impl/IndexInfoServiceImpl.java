package tech.ascs.icity.iform.service.impl;

import java.util.List;

import tech.ascs.icity.iform.model.IndexInfo;
import tech.ascs.icity.iform.service.IndexInfoService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

public class IndexInfoServiceImpl extends DefaultJPAService<IndexInfo>
		implements IndexInfoService {
	private JPAManager<IndexInfo> jPAManager;

	public IndexInfoServiceImpl() {
		super(IndexInfo.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		jPAManager = getJPAManagerFactory().getJPAManager(IndexInfo.class);
	}

	@Override
	public void opIndex(String sql) {
		jPAManager.getJdbcTemplate().execute(sql);
	}

	@Override
	public List<IndexInfo> findByTabName(String tabName) {
		return jPAManager.query().filterEqual("tabName", tabName).list();
	}

	@Override
	public List<IndexInfo> findByTabnameAndIndexName(String tabName,
			String indexName) {
		return jPAManager.query().filterEqual("indexName", indexName)
				.filterEqual("tabName", tabName).list();
	}

	public void createIndex(String tabName) {
		List<IndexInfo> list = findByTabName(tabName);

		for (IndexInfo indexInfo : list) {
			// String tabName = indexInfo.getTabName();
			String indexName = indexInfo.getIndexName();
			String indexColumns = indexInfo.getIndexColumns();
			String type = indexInfo.getType();

			if ("Normal".equalsIgnoreCase(type))
				type = "";
			String sql = "CREATE " + type + " INDEX " + indexName + " ON "
					+ tabName + "(" + indexColumns + ");";
			try {
				opIndex(sql);
			} catch (Exception e) {
				String sql_0 = "DROP INDEX " + indexName + " ON " + tabName;
				opIndex(sql_0);

				opIndex(sql);
			}

		}

	}

}
