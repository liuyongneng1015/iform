package tech.ascs.icity.iform.api.model;

import tech.ascs.icity.iflow.api.model.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewDataList extends Activity {

	private String tableName;

	private String key;

	private List<Map<String, Object>> dataListMap = new ArrayList<>();

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Map<String, Object>> getDataListMap() {
		return dataListMap;
	}

	public void setDataListMap(List<Map<String, Object>> dataListMap) {
		this.dataListMap = dataListMap;
	}
}
