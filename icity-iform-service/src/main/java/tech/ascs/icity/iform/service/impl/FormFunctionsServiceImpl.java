package tech.ascs.icity.iform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import tech.ascs.icity.iform.api.model.DefaultFunctionType;
import tech.ascs.icity.iform.api.model.ParseArea;
import tech.ascs.icity.iform.api.model.ReturnResult;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ListFunction;
import tech.ascs.icity.iform.service.FormFunctionsService;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormFunctionsServiceImpl extends DefaultJPAService<ListFunction> implements FormFunctionsService {

	private JPAManager<ListFunction> listFunctionJPAManager;

	private JPAManager<FormModelEntity> formModelEntityJPAManager;

	@Autowired
	private FormModelService formModelService;

	public FormFunctionsServiceImpl() {
		super(ListFunction.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		listFunctionJPAManager = getJPAManagerFactory().getJPAManager(ListFunction.class);
		formModelEntityJPAManager = getJPAManagerFactory().getJPAManager(FormModelEntity.class);
	}


	@Override
	public Integer getMaxOrderNo() {
		 Map<String, Object> map =	listFunctionJPAManager.getJdbcTemplate().queryForMap("select max(order_no) as order_no from ifm_list_function ");
		 if(map != null && map.get("order_no") != null){
			return Integer.parseInt(String.valueOf(map.get("order_no")));
		 }
		 return 0;
	}

    private DefaultFunctionType[] functionDefaultTypes = {DefaultFunctionType.Edit, DefaultFunctionType.Delete, DefaultFunctionType.QrCode, DefaultFunctionType.TempStore, DefaultFunctionType.Manage, DefaultFunctionType.Download};
	private String[] functionDefaultIcons = new String[]{"icon-xuanzhong1", "icon-xuanzhong", "icon-erweima", null};
	private String[] functionDefaultMethods = new String[]{"PUT", "DELETE", "GET", null};
	private Boolean[] functionVisibles = {true, true, false, false};

	@Override
	public void createDefaultFormFunctions(FormModelEntity formModelEntity) {
		// 表单创建时，默认创建的权限码：编辑，删除，二维码，暂存
//		boolean[] functionVisibles = new boolean[]{true, true, false, false, false, false};
//		for(int i = 0; i < formModelEntity.getFunctions().size() ; i++){
//			ListFunction listFunction = formModelEntity.getFunctions().get(i);
//			formModelEntity.getFunctions().remove(listFunction);
//			listFunctionJPAManager.delete(listFunction);
//			i--;
//		}
		List<ListFunction> listFunctions = new ArrayList<>();
		for (int i = 0; i < functionDefaultTypes.length; i++) {
			DefaultFunctionType functionType =  functionDefaultTypes[i];
			ListFunction listFunction = new ListFunction();
			listFunction.setFormModel(formModelEntity);
			listFunction.setOrderNo(i+1);
			listFunction.setSystemBtn(true);
			listFunction.setVisible(functionVisibles[i]);
			listFunction.setIcon(functionDefaultIcons[i]);
			listFunction.setLabel(functionType.getDesc());
			listFunction.setParseArea(ParseArea.PC.value());
			listFunction .setReturnResult(ReturnResult.NONE);
			listFunction.setAction(functionType.getValue());
			listFunction.setMethod(functionDefaultMethods[i]);
			listFunctions.add(listFunction);
		}
		formModelEntity.setFunctions(listFunctions);
		formModelEntityJPAManager.save(formModelEntity);
	}
}
