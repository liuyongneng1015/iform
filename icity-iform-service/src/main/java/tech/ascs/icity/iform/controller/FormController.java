package tech.ascs.icity.iform.controller;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.Form;
import tech.ascs.icity.iform.service.FormService;
import tech.ascs.icity.jpa.tools.DTOTools;
import tech.ascs.icity.model.Page;

@RestController
@RequestMapping("/form")
public class FormController implements
		tech.ascs.icity.iform.api.service.FormService {

	@Autowired
	private FormService formService;

	@PostMapping
	public void add(@RequestBody Form form) {
		tech.ascs.icity.iform.model.Form target = EntityUtil.toFormEntity(form);
		target.setCreateTime(new Timestamp(System.currentTimeMillis()));

		if (formService.findByName(target.getName()).size() == 1)
			throw new ICityException("表单名称已存在!");

		formService.save(target);
	}

	public void update(@RequestBody Form form) {
		tech.ascs.icity.iform.model.Form target = EntityUtil.toFormEntity(form);
		target.setUpdateTime(new Timestamp(System.currentTimeMillis()));

		formService.update(target);
	}

	public void delete(@PathVariable(name = "id") String id) {
		formService.deleteById(id);
	}

	public Form getById(@PathVariable(name = "id") String id) {
		return EntityUtil.toFormResponse(formService.get(id));
	}

	public List<Form> list() {
		return DTOTools.wrapList(formService.query().list(), Form.class);
	}

	public Page<Form> findByName(@RequestParam(required = false) String name,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

		return DTOTools.wrapPage(formService.findByName(name, page, pageSize),
				Form.class);
	}
}
