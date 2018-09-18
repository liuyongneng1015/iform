package tech.ascs.icity.iform.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.ColumnData;
import tech.ascs.icity.iform.api.model.TabInfo;
import tech.ascs.icity.iform.service.TabInfoService;
import tech.ascs.icity.iform.table.service.CheckUtil;
import tech.ascs.icity.jpa.tools.DTOTools;
import tech.ascs.icity.model.Page;

@RestController
@RequestMapping("/tabInfo")
public class TabInfoController implements
		tech.ascs.icity.iform.api.service.TabInfoService {

	@Autowired
	private TabInfoService tabInfoService;

	@Override
	public void add(@RequestBody TabInfo tabInfo) {

		// tech.ascs.icity.iform.model.TabInfo target=new
		// tech.ascs.icity.iform.model.TabInfo();
		// target.setId(tabInfo.getId());
		// target.setTabName(tabInfo.getTabName());
		// target.setTabNameDesc(tabInfo.getTabNameDesc());
		// target.setTableType(tabInfo.getTableType());
		// target.setSynFlag(false);

		if (tabInfo == null)
			throw new ICityException("对象不能为空!");

		if (!CheckUtil.checkName(tabInfo.getTabName()))
			throw new ICityException("表名称命名非法!");

		if (tabInfo != null
				&& (tabInfo.getColumnDatas() == null
						|| tabInfo.getColumnDatas().size() == 0 || ""
							.equals(tabInfo.getColumnDatas())))
			throw new ICityException("字段列表不能为空!");

		tech.ascs.icity.iform.model.TabInfo tabInfo_old = tabInfoService
				.findByTabName(tabInfo.getTabName());
		if (tabInfo_old != null)
			throw new ICityException("表名称已存在!");

		// tabInfo

		tech.ascs.icity.iform.model.TabInfo target = new tech.ascs.icity.iform.model.TabInfo();
		target = EntityUtil.toTabInfoEntity(tabInfo);
		target.setCreateTime(new Timestamp(System.currentTimeMillis()));
		target.setSynFlag(false);

		// target.setColumnDatas(tabInfo.getColumnDatas());
		// //list里的结构是api结构,类型错误

		target.setColumnDatas(toColumnDataEntity(tabInfo.getColumnDatas(),
				"add"));

		tabInfoService.save(target);
		// tabInfoService.save(DTOTools.toEntity(tabInfo,tech.ascs.icity.iform.model.TabInfo.class));
	}

	@Override
	public List<TabInfo> list() {
		return DTOTools.wrapList(tabInfoService.query().list(), TabInfo.class);
	}

	@Override
	public List<ColumnData> list(
			@PathVariable(name = "tabInfoId") String tabInfoId) { // 方法里的spring标签不能少

		return DTOTools.wrapList(
				tabInfoService.get(tabInfoId).getColumnDatas(),
				ColumnData.class);
	}

	@Override
	public void delete(@PathVariable(name = "id") String id) {
		tabInfoService.deleteById(id);
	}

	@Override
	public TabInfo findByTabName(@PathVariable(name = "tabName") String tabName) {
		tech.ascs.icity.iform.model.TabInfo source = tabInfoService
				.findByTabName(tabName);
		if (source == null)
			return null;
		else {
			tech.ascs.icity.iform.api.model.TabInfo target = EntityUtil
					.toTabInfoResponse(source);
			target.setColumnDatas(toColumnDataResponse(source.getColumnDatas()));

			return target;
		}
	}

	public Page<TabInfo> findByTabNameAndSynFlag(
			@RequestParam(required = false) String tabName,
			@RequestParam(required = false) Boolean synFlag,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

		return DTOTools.wrapPage(tabInfoService.findByTabNameAndSynFlag(
				tabName, synFlag, page, pageSize), TabInfo.class);
	}

	@Override
	public void update(@RequestBody TabInfo tabInfo) {

		if (tabInfo == null)
			throw new ICityException("表对象不能为空!");

		if (!CheckUtil.checkName(tabInfo.getTabName()))
			throw new ICityException("表名称命名非法!");

		tech.ascs.icity.iform.model.TabInfo tabInfo_old = tabInfoService
				.find(tabInfo.getId());
		if (tabInfo_old != null
				&& !tabInfo_old.getTabName().equals(tabInfo.getTabName()))
			throw new ICityException("表名称不允许修改!");

		if (tabInfo != null && tabInfo.getColumnDatas() == null)
			throw new ICityException("字段列表不能为空!");

		tech.ascs.icity.iform.model.TabInfo target = new tech.ascs.icity.iform.model.TabInfo();
		target = EntityUtil.toTabInfoEntity(tabInfo);

		target.setSynFlag(false);
		target.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		target.setColumnDatas(toColumnDataEntity(tabInfo.getColumnDatas(),
				"update"));

		tabInfoService.update(target);
	}

	private List<tech.ascs.icity.iform.model.ColumnData> toColumnDataEntity(
			List<tech.ascs.icity.iform.api.model.ColumnData> source_ColumnData,
			String methodType) {

		List<tech.ascs.icity.iform.model.ColumnData> target_list = new ArrayList<>();
		for (ColumnData columnData : source_ColumnData) {

			if ((columnData.getForeignTab() == null && columnData
					.getForeignKey() == null)) {

			} else if ("".equals(columnData.getForeignTab())
					&& "".equals(columnData.getForeignKey())) {

			} else if (((columnData.getForeignTab() == null || ""
					.equals(columnData.getForeignTab())) && ((columnData
					.getForeignKey() != null) && !"".equals(columnData
					.getForeignKey())))
					|| ((columnData.getForeignKey() == null || ""
							.equals(columnData.getForeignKey())) && (columnData
							.getForeignTab() != null && !"".equals(columnData
							.getForeignTab()))))
				throw new ICityException("外键关联表和外键关联字段要同时存在!");

			if ("add".equals(methodType))
				columnData.setCreateTime(new Timestamp(System
						.currentTimeMillis()));
			else if ("update".equals(methodType))
				columnData.setUpdateTime(new Timestamp(System
						.currentTimeMillis()));
			target_list.add(EntityUtil.toColumnDataEntity(columnData));

		}

		return target_list;
	}

	private List<tech.ascs.icity.iform.api.model.ColumnData> toColumnDataResponse(
			List<tech.ascs.icity.iform.model.ColumnData> source_ColumnData) {

		List<tech.ascs.icity.iform.api.model.ColumnData> target_list = new ArrayList<>();
		for (tech.ascs.icity.iform.model.ColumnData columnData : source_ColumnData) {
			target_list.add(EntityUtil.toColumnDataResponse(columnData));
		}

		return target_list;
	}

}
