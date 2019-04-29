package tech.ascs.icity.iform.service.impl;

import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.BusinessTriggerEntity;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.OKHttpLogEntity;
import tech.ascs.icity.iform.service.OKHttpLogService;
import tech.ascs.icity.iform.utils.*;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.*;
import java.util.List;

public class OKHttpLogServiceImpl extends DefaultJPAService<OKHttpLogEntity> implements OKHttpLogService {


	private JPAManager<OKHttpLogEntity> okHttpLogManager;

	@Override
	protected void initManager() {
		super.initManager();
		okHttpLogManager = getJPAManagerFactory().getJPAManager(OKHttpLogEntity.class);
	}

	public OKHttpLogServiceImpl() {
		super(OKHttpLogEntity.class);
	}

	@Override
	public OKHttpLogEntity saveOKHttpLog(OKHttpLogEntity entity) {
		return okHttpLogManager.save(entity);
	}

	@Override
	public void sendOKHttpRequest(BusinessTriggerEntity triggerEntity, FormModelEntity formModelEntity, Map<String, Object> map) {
		if(triggerEntity == null || formModelEntity == null){
			return;
		}
		OKHttpLogEntity okHttpLogEntity = new OKHttpLogEntity();
		okHttpLogEntity.setUrl(triggerEntity.getUrl());
		//请求json参数
		okHttpLogEntity.setParameter(OkHttpUtils.mapToJson(map));

		//来源
		okHttpLogEntity.setFromSource(formModelEntity.getId());
		//来源对象类型
		okHttpLogEntity.setSourceType(DataSourceType.FormModel);
		//业务触发类型
		okHttpLogEntity.setTriggerType(triggerEntity.getType());
		okHttpLogManager.save(okHttpLogEntity);

		ResponseResult responseResult = requestWebService(triggerEntity,  map);
		//结果编码
		okHttpLogEntity.setResultCode(responseResult.getCode());
		//返回结果
		okHttpLogEntity.setResult(OkHttpUtils.mapToJson(responseResult.getResult()));
		okHttpLogManager.save(okHttpLogEntity);

		throwWebException(responseResult, triggerEntity);
	}

	private void throwWebException(ResponseResult responseResult, BusinessTriggerEntity triggerEntity){
		List<BusinessTriggerType> businessTriggerTypes = new ArrayList<>();
		businessTriggerTypes.add(BusinessTriggerType.Delete_Before);
		businessTriggerTypes.add(BusinessTriggerType.Update_Before);
		businessTriggerTypes.add(BusinessTriggerType.Add_Before);

		if(businessTriggerTypes.contains(triggerEntity.getType())) {
			if (responseResult.getCode() != 200) {
				new IFormException(responseResult.getMessage());
			} else if (responseResult.getCode() == 200 && !(Boolean) responseResult.getResult().get("continue")) {
				new IFormException((String) responseResult.getResult().get("message"));
			} else if (responseResult.getCode() == 200 && responseResult.getResult() == null && triggerEntity.getReturnResult() == ReturnResult.HAS) {
				new IFormException("系统错误，请稍后再试");
			}
		}
	}

	private ResponseResult requestWebService(BusinessTriggerEntity triggerEntity, Map<String, Object>  map){
		ResponseResult result = null;
		if("get".equals(triggerEntity.getMethod())){
			result = OkHttpUtils.doGet(triggerEntity.getUrl(), map);
		}else if("delete".equals(triggerEntity.getMethod())){
			result = OkHttpUtils.doDelete(triggerEntity.getUrl(), map);
		}else if("put".equals(triggerEntity.getMethod())){
			result = OkHttpUtils.doPut(triggerEntity.getUrl(), map);
		}else if("head".equals(triggerEntity.getMethod())){
			result = OkHttpUtils.doHeader(triggerEntity.getUrl(), map);
		}else{
			result = OkHttpUtils.doPost(triggerEntity.getUrl(), map);
		}
		return result;
	}

}