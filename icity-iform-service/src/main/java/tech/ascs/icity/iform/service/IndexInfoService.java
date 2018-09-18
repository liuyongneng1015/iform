package tech.ascs.icity.iform.service;

import java.util.List;

import tech.ascs.icity.iform.model.IndexInfo;
import tech.ascs.icity.jpa.service.JPAService;

public interface IndexInfoService extends JPAService<IndexInfo> {
	
	public List<IndexInfo> findByTabName(String tabName);
	
	public List<IndexInfo> findByTabnameAndIndexName(String tabName,String indexName);
    
	public void opIndex(String sql);
	
	public void createIndex(String tabName);
}
