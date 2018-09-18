package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.ListData;
import tech.ascs.icity.iform.service.ListDataService;
import tech.ascs.icity.jpa.tools.DTOTools;
import tech.ascs.icity.model.Page;

@RestController
@RequestMapping("/listData")
public class ListDataController implements
		tech.ascs.icity.iform.api.service.ListDataService {

	@Autowired
	private ListDataService listDataService;

	public void add(@RequestBody ListData listData) {

		if (listData == null || "".equals(listData))
			throw new ICityException("对象不能为空!");

		if (listData.getName() == null || "".equals(listData.getName()))
			throw new ICityException("列表名称不能为空!");

		if (listDataService.findByName(listData.getName()).size() >= 1)
			throw new ICityException("列表名称已存在!");

		if (listData.getFormId() == null || "".equals(listData.getFormId()))
			throw new ICityException("表单id不能为空!");

		if (listData.getFormName() == null || "".equals(listData.getFormName()))
			throw new ICityException("表单名称不能为空!");

		if (listData.getMaster() == null || "".equals(listData.getMaster()))
			throw new ICityException("数据库主表名称不能为空!");

		/*
		 * if(listData.getFn()==null || "".equals(listData.getFn())) throw new
		 * ICityException("功能列表不能为空!");
		 * 
		 * if(listData.getColList()==null || listData.getColList().size()==0)
		 * throw new ICityException("字段列表不能为空!");
		 */

		// if(listData.getShowColumns()==null ||
		// "".equals(listData.getShowColumns()))
		// throw new ICityException("显示字段列表不能为空!");

		// ListDataService.save(EntityUtil.toListDataEntity(ListData));
		tech.ascs.icity.iform.model.ListData target = new tech.ascs.icity.iform.model.ListData();
		target = EntityUtil.toListDataEntity(listData);

		// target.setListColData(toListColDataEntity(listData.getListColData()));
		listDataService.save(target);
	}

	public void update(@RequestBody ListData listData) {
		// ListDataService.update(EntityUtil.toListDataEntity(ListData));

		if (listData == null || "".equals(listData))
			throw new ICityException("对象不能为空!");

		if (listData.getName() == null || "".equals(listData.getName()))
			throw new ICityException("列表名称不能为空!");

		// if(listDataService.findByName(listData.getName()).size()>=1)
		// throw new ICityException("列表名称已存在!");

		if (listData.getFormId() == null || "".equals(listData.getFormId()))
			throw new ICityException("表单id不能为空!");

		if (listData.getFormName() == null || "".equals(listData.getFormName()))
			throw new ICityException("表单名称不能为空!");

		// if(listData.getFn()==null || "".equals(listData.getFn()))
		// throw new ICityException("功能列表不能为空!");

		if (listData.getMaster() == null || "".equals(listData.getMaster()))
			throw new ICityException("数据库主表名称不能为空!");

		// if(listData.getColList()==null || listData.getColList().size()==0)
		// throw new ICityException("字段列表不能为空!");

		// if(listData.getShowColumns()==null ||
		// "".equals(listData.getShowColumns()))
		// throw new ICityException("显示字段列表不能为空!");

		if (!listDataService.findByName(listData.getName()).get(0).getName()
				.equals(listData.getName()))
			throw new ICityException("列表名称不能修改!");

		tech.ascs.icity.iform.model.ListData target = new tech.ascs.icity.iform.model.ListData();
		target = EntityUtil.toListDataEntity(listData);

		// target.setListColData(toListColDataEntity(listData.getListColData()));

		listDataService.update(target);
	}

	public void delete(@PathVariable(name = "id") String id) {
		listDataService.deleteById(id);
	}

	public ListData getById(@PathVariable(name = "id") String id) {
		return EntityUtil.toListDataResponse(listDataService.get(id));
	}

	public List<ListData> list() {
		return toColumnDataResponse(listDataService.query().list());
	}

	public Page<ListData> findByName(
			@RequestParam(required = false) String name,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

		return DTOTools.wrapPage(
				listDataService.findByName(name, page, pageSize),
				ListData.class);
	}

	private List<tech.ascs.icity.iform.api.model.ListData> toColumnDataResponse(
			List<tech.ascs.icity.iform.model.ListData> source_ColumnData) {

		List<tech.ascs.icity.iform.api.model.ListData> target_list = new ArrayList<>();
		for (tech.ascs.icity.iform.model.ListData listColData : source_ColumnData) {
			target_list.add(EntityUtil.toListDataResponse(listColData));
		}

		return target_list;
	}
}
