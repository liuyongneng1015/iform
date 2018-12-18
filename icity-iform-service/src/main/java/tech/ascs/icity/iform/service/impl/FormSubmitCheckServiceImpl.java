package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.service.FormSubmitCheckService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.Map;

public class FormSubmitCheckServiceImpl extends DefaultJPAService<FormSubmitCheckInfo> implements FormSubmitCheckService {

	private JPAManager<FormSubmitCheckInfo> formSubmitCheckManager;

	public FormSubmitCheckServiceImpl() {
		super(FormSubmitCheckInfo.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		formSubmitCheckManager = getJPAManagerFactory().getJPAManager(FormSubmitCheckInfo.class);
	}


	@Override
	public Integer getMaxOrderNo() {
		 Map<String, Object> map =	formSubmitCheckManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from ifm_form_submit_checks ");
		 if(map != null && map.get("order_no") != null){
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		 }
		 return 0;
	}
}
