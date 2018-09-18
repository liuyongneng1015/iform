package tech.ascs.icity.iform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.iform.api.model.BindFlow;
import tech.ascs.icity.iform.service.BindFlowService;

@RestController
@RequestMapping("/bindFlow")
public class BindFlowController implements
		tech.ascs.icity.iform.api.service.BindFlowService {

	@Autowired
	private BindFlowService bindFlowService;

	public void add(@RequestBody BindFlow[] bindFlow) {

	}

	public void update(@RequestBody BindFlow[] bindFlow) {
	}

	public void delete(@PathVariable(name = "id") String id) {
		bindFlowService.deleteById(id);
	}

	public BindFlow getById(@PathVariable(name = "id") String id) {
		return null;
	}

	public List<BindFlow> list() {
		return null;
	}
}
