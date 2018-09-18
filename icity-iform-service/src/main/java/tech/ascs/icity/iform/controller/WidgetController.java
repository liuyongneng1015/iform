package tech.ascs.icity.iform.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.ascs.icity.iform.api.model.Widget;
import tech.ascs.icity.iform.service.WidgetService;
import tech.ascs.icity.jpa.tools.DTOTools;

@RestController
@RequestMapping("/widget")
public class WidgetController implements
		tech.ascs.icity.iform.api.service.WidgetService {

	@Autowired
	private WidgetService widgetService;

	public void add(@RequestBody Widget widget) {
		widgetService.save(EntityUtil.toWidgetEntity(widget));
	}

	public void update(@RequestBody Widget widget) {
		widgetService.update(EntityUtil.toWidgetEntity(widget));
	}

	public void delete(@PathVariable(name = "id") String id) {
		widgetService.deleteById(id);
	}

	public Widget getById(@PathVariable(name = "id") String id) {
		return EntityUtil.toWidgetResponse(widgetService.get(id));
	}
	
	@Override
	public List<Widget> list() {
		return DTOTools.wrapList(widgetService.query().list(), Widget.class);
	}
	
	@SuppressWarnings("all")
	public Map getAllWidgetStruct(){
		Map<String,Set<String>> widgetMap=new HashMap<>();
		List<Widget> list=this.list();
		String type="";
		for (Widget widget : list) {
			type=widget.getType();
			Set set=widgetMap.get(type);
			if(set==null){
				set=new HashSet();
				set.add(widget.getFieldName()+":"+widget.getMeaning());
				widgetMap.put(type, set);
			}else{
				set.add(widget.getFieldName()+":"+widget.getMeaning());
			}
		}
		
		return widgetMap;
	}
}
