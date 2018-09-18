package tech.ascs.icity.iform.controller;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.SynLog;
import tech.ascs.icity.iform.model.TabInfo;
import tech.ascs.icity.iform.service.IndexInfoService;
import tech.ascs.icity.iform.service.SynLogService;
import tech.ascs.icity.iform.service.TabInfoService;
//import tech.ascs.icity.iform.table.service.TableUtilService;
import tech.ascs.icity.iform.table.service.TableUtilService;

@RestController
@RequestMapping("/synLog")
public class SynLogController implements
		tech.ascs.icity.iform.api.service.SynLogService {

	@Autowired
	private SynLogService synLogService;

	@Autowired
	private TabInfoService tabInfoService;

	@Autowired
	private IndexInfoService indexInfoService;

	TableUtilService tableUtilService = new TableUtilService();

	@Override
	public void add(@RequestBody SynLog synLog) {

		TabInfo tabInfo = tabInfoService.get(synLog.getTabInfoId());
		if (tabInfo.getSynFlag()) {
			throw new ICityException("已同步,不能再进行同步操作!");
		} else {
			try {

				// syn
				tableUtilService.createTable(tabInfo);

				indexInfoService.createIndex(tabInfo.getTabName());

				tech.ascs.icity.iform.model.SynLog target = EntityUtil
						.toSynLogEntity(synLog);
				target.setSynTime(new Timestamp(System.currentTimeMillis()));
				synLogService.save(target);

				tabInfo.setSynFlag(true);
				tabInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
				tabInfoService.update(tabInfo);

			} catch (Exception e) {
				throw new ICityException("同步失败!", e);
			}
		}

	}
}
