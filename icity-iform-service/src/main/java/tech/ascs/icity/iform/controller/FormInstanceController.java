package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.iform.model.ReferenceItemModelEntity;
import tech.ascs.icity.iform.service.FormInstanceServiceEx;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.service.ListModelService;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "表单实例服务", description = "包含业务表单数据的增删改查等功能")
@RestController
public class FormInstanceController implements tech.ascs.icity.iform.api.service.FormInstanceService {

	@Autowired
	private ListModelService listModelService;

	@Autowired
	private ItemModelService itemModelService;

	@Autowired
	private FormModelService formModelService;

	@Autowired
	private FormInstanceServiceEx formInstanceService;

	@Override
	public List<FormInstance> list(@PathVariable(name="listId") String listId, @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}

		return formInstanceService.listFormInstance(listModel, parameters);
	}

	@Override
	public List<DataInstance> listRefereceData(@PathVariable(name="listId") String listId, @PathVariable(name="itemId") String itemId, @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}
		ItemModelEntity itemModelEntity = itemModelService.get(itemId);
		List<DataInstance> dataInstances = new ArrayList<>();
		if(itemModelEntity != null && itemModelEntity instanceof ReferenceItemModelEntity && ((ReferenceItemModelEntity) itemModelEntity).getItemTableColunmName() != null){
			List<FormInstance> list = formInstanceService.listFormInstance(listModel, parameters);
			List<ItemModelEntity> itemModelEntities = formModelService.getReferenceItemModelList((ReferenceItemModelEntity)itemModelEntity);
			if(itemModelEntities != null && itemModelEntities.size() > 0){
				Map<String, ItemModelEntity> map = new HashMap<>();
				for(ItemModelEntity itemModelEntity1 : itemModelEntities){
					map.put(itemModelEntity1.getId(), itemModelEntity);
				}
				for(FormInstance formInstance : list){
					DataInstance dataInstance = new DataInstance();
					List<String> displayValue = new ArrayList<>();
					for(ItemInstance itemInstance : formInstance.getItems()){
						if(itemInstance.getSystemItemType() == SystemItemType.ID){
							dataInstance.setId(itemInstance.getId());
						}
						if(map.keySet().contains(itemInstance.getId())){
							displayValue.add((String)itemInstance.getDisplayValue());
						}
					}
					dataInstance.setDisplayValue(String.join(",", displayValue));
					dataInstances.add(dataInstance);
				}
			}
		}
		return dataInstances;
	}

	// url?param1=value1&param2=value2&param2=value3,value4&param2=value5
	// @RequestParam Map<String, Object> parameters 有两个问题
	// 1) 因为Object没有指定具体类型，接收后会变成字符串
	// 2) 接收数组时，相同的Key会被覆盖掉，接收 param1=value1&param2=value2 的 param1参数，map的键值对会覆盖掉相同的Key
	@Override
	public Page<FormInstance> page(@PathVariable(name="listId") String listId, @RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pagesize", defaultValue = "10") int pagesize, @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}

		Map<String, Object> queryParameters = new HashMap<>();
		for (Map.Entry<String, Object> entry:parameters.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				if (!StringUtils.isEmpty(value)) {
					String valueStr = value.toString();
					// 如果传过来的参数是数组且以逗号划分开的话,组件ID的长度是32位，若32位是逗号，当作数组处理
					if (valueStr.length()>32 && valueStr.substring(32,33).equals(",")) {
						queryParameters.put(entry.getKey(), valueStr.split(","));
					} else {
						queryParameters.put(entry.getKey(), valueStr);
					}
				}
			}
		}
		return formInstanceService.pageFormInstance(listModel, page, pagesize, queryParameters);
	}

	@Override
	public Page<String> pageByTableName(@PathVariable(name="tableName") String tableName, @RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name="pagesize", defaultValue = "10") int pagesize) {
		return formInstanceService.pageByTableName(tableName, page, pagesize);
	}


	@Override
	public FormInstance getEmptyInstance(@PathVariable(name="formId") String formId) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		return formInstanceService.newFormInstance(formModel);
	}

	@Override
	public FormInstance get(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		return formInstanceService.getFormInstance(formModel, id);
	}

	@Override
	public IdEntity createFormInstance(@PathVariable(name="formId", required = true) String formId, @RequestBody FormDataSaveInstance formInstance) {
		if (!formId.equals(formInstance.getFormId())) {
			throw new IFormException("表单id不一致");
		}
		formInstance.setId(formId);
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}

		return new IdEntity(formInstanceService.createFormInstance(formModel, formInstance));
	}

	@Override
	public void updateFormInstance(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id, @RequestBody FormDataSaveInstance formInstance) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}

		formInstanceService.updateFormInstance(formModel, id, formInstance);
	}

	@Override
	public void removeFormInstance(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}

		formInstanceService.deleteFormInstance(formModel, id);
	}

	@Override
	public void removeFormInstance(@PathVariable(name="formId") String formId, @RequestBody List<String> ids) {
		if (ids!=null && ids.size()>0) {
			FormModelEntity formModel = formModelService.find(formId);
			if (formModel == null) {
				throw new IFormException(404, "表单模型【" + formId + "】不存在");
			}
			for (String id:ids) {
				formInstanceService.deleteFormInstance(formModel, id);
			}
		}
	}
}
