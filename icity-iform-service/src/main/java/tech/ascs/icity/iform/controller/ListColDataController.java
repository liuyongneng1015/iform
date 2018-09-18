/*package tech.ascs.icity.iform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.iform.api.model.ListColData;
import tech.ascs.icity.iform.service.ListColDataService;
import tech.ascs.icity.jpa.tools.DTOTools;

@RestController
@RequestMapping("/listColData")
public class ListColDataController implements
		tech.ascs.icity.iform.api.service.ListColDataService {

	@Autowired
	private ListColDataService listColDataService;

	public void add(@RequestBody ListColData listColData) {
		listColDataService.save(EntityUtil.toListColDataEntity(listColData));
	}

	public void update(@RequestBody ListColData listColData) {
		listColDataService.update(EntityUtil.toListColDataEntity(listColData));
	}

	public void delete(@PathVariable(name = "id") String id) {
		listColDataService.deleteById(id);
	}

	public ListColData getById(@PathVariable(name = "id") String id) {
		return EntityUtil.toListColDataResponse(listColDataService.get(id));
	}
	
	public List<ListColData> list() {
		return DTOTools.wrapList(listColDataService.query().list(), ListColData.class);
	}
}
*/