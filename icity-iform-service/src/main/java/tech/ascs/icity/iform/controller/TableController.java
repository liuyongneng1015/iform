package tech.ascs.icity.iform.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.iform.api.model.Table;
import tech.ascs.icity.model.Page;

@RestController
@RequestMapping("/table")
public class TableController implements
		tech.ascs.icity.iform.api.service.TableService {
	public void add(@RequestBody Table table) {

	}

	public void update(@RequestBody Table table) {

	}

	public void delete(@PathVariable(name = "tableName") String tableName,
			@PathVariable(name = "id") String id) {

	}

	public Object getByTableNameAndId(
			@PathVariable(name = "listDataId") String listDataId,
			@PathVariable(name = "tableName") String tableName,
			@PathVariable(name = "id") String id) {
		return null;
	}

	public Page<Object> findByTableName(
			@RequestParam(name = "listDataId") int listDataId,
			@RequestParam(required = true) String tableName,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
		return null;
	}
}
