package tech.ascs.icity.iform.controller;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.iform.api.model.ColumnData;
import tech.ascs.icity.iform.model.TabInfo;
import tech.ascs.icity.iform.service.ColumnDataService;
import tech.ascs.icity.iform.service.TabInfoService;

@RestController
@RequestMapping("/columnData")
public class ColumnDataController implements
		tech.ascs.icity.iform.api.service.ColumnDataService {

	@Autowired
	private ColumnDataService columnDataService;

	@Autowired
	private TabInfoService tabInfoService;

	@Override
	public void add(@RequestBody ColumnData columnData) {
		TabInfo tabInfo = tabInfoService.get(columnData.getTabInfoId());
		
		tech.ascs.icity.iform.model.ColumnData data = EntityUtil
				.toColumnDataEntity(columnData);
		data.setTabName(tabInfo.getTabName());
		data.setCreateTime(new Timestamp(System.currentTimeMillis()));
		columnDataService.save(data);
	}

	@Override
	public void delete(@PathVariable(name = "id") String id) {
		columnDataService.deleteById(id);
	}

	@Override
	public void update(@RequestBody ColumnData columnData) {
		try {
			TabInfo tabInfo = tabInfoService.get(columnData.getTabInfoId());
			tech.ascs.icity.iform.model.ColumnData data = EntityUtil
					.toColumnDataEntity(columnData);
			data.setTabName(tabInfo.getTabName());
			data.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			columnDataService.update(data);

			tabInfo.setSynFlag(false);
			tabInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			tabInfoService.save(tabInfo);

		} catch (Exception e) {
		}

	}

}
