package tech.ascs.icity.iform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.iform.api.model.FormWidget;
import tech.ascs.icity.iform.service.FormWidgetService;
import tech.ascs.icity.jpa.tools.DTOTools;

@RestController
@RequestMapping("/formWidget")
public class FormWidgetController implements
		tech.ascs.icity.iform.api.service.FormWidgetService {

	@Autowired
	private FormWidgetService formWidgetService;

	public void add(@RequestBody FormWidget formWidget) {
		formWidgetService.save(EntityUtil.toFormWidgetEntity(formWidget));
	}

	public void update(@RequestBody FormWidget formWidget) {
		formWidgetService.update(EntityUtil.toFormWidgetEntity(formWidget));
	}

	public void delete(@PathVariable(name = "id") String id) {
		formWidgetService.deleteById(id);
	}

	public FormWidget getById(@PathVariable(name = "id") String id) {
		return EntityUtil.toFormWidgetResponse(formWidgetService.get(id));
	}

	public List<FormWidget> list() {
		return DTOTools.wrapList(formWidgetService.query().list(),
				FormWidget.class);
	}
}
