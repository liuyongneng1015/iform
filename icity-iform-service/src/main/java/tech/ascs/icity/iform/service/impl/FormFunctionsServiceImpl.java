package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.model.ListFunction;
import tech.ascs.icity.iform.service.FormFunctionsService;
import tech.ascs.icity.iform.service.FormSubmitCheckService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.Map;

public class FormFunctionsServiceImpl extends DefaultJPAService<ListFunction> implements FormFunctionsService {

	private JPAManager<ListFunction> listFunctionJPAManager;

	public FormFunctionsServiceImpl() {
		super(ListFunction.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		listFunctionJPAManager = getJPAManagerFactory().getJPAManager(ListFunction.class);
	}


	@Override
	public Integer getMaxOrderNo() {
		 Map<String, Object> map =	listFunctionJPAManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from ifm_list_function ");
		 if(map != null && map.get("order_no") != null){
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		 }
		 return 0;
	}
}
