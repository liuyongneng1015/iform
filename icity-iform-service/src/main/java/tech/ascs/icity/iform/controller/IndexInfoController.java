package tech.ascs.icity.iform.controller;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.IndexInfo;
import tech.ascs.icity.iform.service.IndexInfoService;
import tech.ascs.icity.iform.service.TabInfoService;
import tech.ascs.icity.iform.table.service.CheckUtil;
import tech.ascs.icity.jpa.tools.DTOTools;

@RestController
@RequestMapping("/indexInfo")
public class IndexInfoController implements
		tech.ascs.icity.iform.api.service.IndexInfoService {

	@Autowired
	private IndexInfoService indexInfoService;

	@Autowired
	private TabInfoService tabInfoService;

	public void add(@RequestBody IndexInfo indexInfo) {

		if (!CheckUtil.checkName(indexInfo.getTabName())
				|| !CheckUtil.checkName(indexInfo.getIndexName()))
			throw new ICityException("表名或索引名称命名非法!");

		if (indexInfoService.findByTabnameAndIndexName(indexInfo.getTabName(),
				indexInfo.getIndexName()).size() >= 1)
			throw new ICityException("索引名称已存存!");

		if (indexInfo.getIndexColumns() == null || indexInfo.getType() == null
				|| "".equals(indexInfo.getIndexColumns())
				|| "".equals(indexInfo.getType() == null))
			throw new ICityException("索引列列表或索引类型不能为空!");

		tech.ascs.icity.iform.model.IndexInfo target = EntityUtil
				.toIndexInfoEntity(indexInfo);
		target.setCreateTime(new Timestamp(System.currentTimeMillis()));

		indexInfoService.save(target);
	}

	public void update(@RequestBody IndexInfo indexInfo) {

		if (!CheckUtil.checkName(indexInfo.getTabName())
				|| !CheckUtil.checkName(indexInfo.getIndexName()))
			throw new ICityException("表名或索引名称命名非法!");

		if (indexInfo.getIndexColumns() == null || indexInfo.getType() == null
				|| "".equals(indexInfo.getIndexColumns())
				|| "".equals(indexInfo.getType()))
			throw new ICityException("索引列列表或索引类型不能为空!");

		tech.ascs.icity.iform.model.IndexInfo indexInfo_old = indexInfoService
				.find(indexInfo.getId());
		if (!indexInfo_old.getTabName().equals(indexInfo.getTabName()))
			throw new ICityException("索引名称对应的表名称不能更改!");
		if (!indexInfo_old.getIndexName().equals(indexInfo.getIndexName())) {
			if (indexInfoService.findByTabnameAndIndexName(
					indexInfo.getTabName(), indexInfo.getIndexName()).size() >= 1)
				throw new ICityException("索引名称已存存!");
		}
		tech.ascs.icity.iform.model.IndexInfo target = EntityUtil
				.toIndexInfoEntity(indexInfo);
		target.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		indexInfoService.update(target);

		String tabName = indexInfo.getTabName();
		tech.ascs.icity.iform.model.TabInfo tabInfo = tabInfoService
				.findByTabName(tabName);

		tabInfo.setSynFlag(false);
		tabInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));

		tabInfoService.update(tabInfo);

	}

	public void delete(@PathVariable(name = "id") String id) {
		tech.ascs.icity.iform.model.IndexInfo indexInfo = indexInfoService
				.get(id);
		String tabName = indexInfo.getTabName();
		String indexName = indexInfo.getIndexName();

		indexInfoService.deleteById(id);
		try {
			// "DROP INDEX test2_index_age ON test2"
			String sql = "DROP INDEX " + indexName + " ON " + tabName;
			indexInfoService.opIndex(sql);
		} catch (Exception e) {
		}

	}

	public IndexInfo getById(@PathVariable(name = "id") String id) {
		return EntityUtil.toIndexInfoResponse(indexInfoService.get(id));
	}

	@Override
	public List<IndexInfo> findByTabName(
			@PathVariable(name = "tabName") String tabName) {
		List<tech.ascs.icity.iform.model.IndexInfo> source = indexInfoService
				.findByTabName(tabName);
		if (source == null)
			return null;
		else {
			return DTOTools.wrapList(source, IndexInfo.class);
		}
	}
}
