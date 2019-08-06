package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.service.FormSubmitCheckService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.List;
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
		 List<Map<String, Object>> mapDataList = formSubmitCheckManager.getJdbcTemplate().queryForList("select max(order_no) as order_no from ifm_form_submit_checks ");
		 if (mapDataList == null || mapDataList.size() < 1) {
			 return 0;
		 }
		 Map<String, Object> map = mapDataList.get(0);
		 if(map != null && map.get("order_no") != null){
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		 }
		 return 0;
	}
}
