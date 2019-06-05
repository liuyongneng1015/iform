package tech.ascs.icity.iform.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONPath;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import tech.ascs.icity.admin.api.model.Position;
import tech.ascs.icity.admin.client.UserService;
import tech.ascs.icity.iflow.api.model.ProcessInstance;
import tech.ascs.icity.iflow.client.ProcessInstanceService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.utils.*;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

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

	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	private UploadService uploadService;

	@Autowired
	private DataModelService dataModelService;

	@Autowired
	private UserService userService;

	@Autowired
	private DictionaryDataService dictionaryService;
	@Autowired
	private ProcessInstanceService processInstanceService;


	@Value("${icity.iform.qrcode.base-url}")
	private String qrcodeBaseUrl;
	@Value("${icity.iform.qrcode.name}")
	private String qrcodeName;

	@Override
	public List<FormDataSaveInstance> list(@PathVariable(name="listId") String listId, @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}

		Page<FormDataSaveInstance> page = formInstanceService.pageFormInstance(listModel,1,Integer.MAX_VALUE, parameters);
		return page.getResults();
	}

	@Override
	public List<FormDataSaveInstance> simplifyList(@PathVariable(name="listId") String listId, @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}

		Page<FormDataSaveInstance> page = formInstanceService.pageFormInstance(listModel,1,Integer.MAX_VALUE, parameters);
		List<FormDataSaveInstance> list = new ArrayList<>();
		for(FormDataSaveInstance instance : page.getResults()){
			instance.setFormId(null);
			instance.setReferenceData(null);
			instance.setFileUploadModel(null);
			instance.setCanEdit(null);
			instance.setActivityId(null);
			instance.setActivityInstanceId(null);
			instance.setProcessId(null);
			instance.setProcessInstanceId(null);
			instance.setData(null);
			instance.setItems(null);
			instance.setSubFormData(null);
			list.add(instance);
		}
		return list;
	}

	@Override
	public List<DataInstance> listRefereceData(@PathVariable(name="listId") String listId, @PathVariable(name="itemId") String itemId, @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}
		ItemModelEntity itemModelEntity = itemModelService.get(itemId);
		List<DataInstance> dataInstances = new ArrayList<>();
		if(itemModelEntity != null && itemModelEntity instanceof ReferenceItemModelEntity){
			List<FormInstance> list = formInstanceService.listInstance(listModel, parameters);
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
	@Override
	public Page<FormDataSaveInstance> page(@PathVariable(name="listId") String listId,
										   @RequestParam(name="page", defaultValue = "1") int page,
										   @RequestParam(name="pagesize", defaultValue = "10") int pagesize,
										   @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}
		Map<String, Object> queryParameters = assemblyQueryParameters(parameters);
		FormModelEntity formModelEntity = listModel.getMasterForm();
		if (formModelHasProcess(formModelEntity)) {
			return queryIflowList(queryParameters, page, pagesize, formModelEntity, listModel);
		}
		return formInstanceService.pageFormInstance(listModel, page, pagesize, queryParameters);
	}

	private  Page<FormDataSaveInstance> queryIflowList(Map<String, Object> queryParameters,  int page, int pagesize, FormModelEntity formModelEntity, ListModelEntity listModel) {
		List<ItemModelEntity> items = formModelEntity.getItems();
		Optional<ItemModelEntity> optional = items.stream().filter(item-> (item.getColumnModel()!=null&&"process_state".equals(item.getColumnModel().getColumnName()))).findFirst();
		int status = -1;
		if (optional.isPresent()) {
			ItemModelEntity itemModelEntity = optional.get();
			if (itemModelEntity instanceof SelectItemModelEntity) {
				SelectItemModelEntity selectItem = (SelectItemModelEntity)itemModelEntity;
				Object value = queryParameters.get(selectItem.getId());
				if (value!=null && StringUtils.hasText(value.toString())) {
					queryParameters.remove(selectItem.getId());
					List<ItemSelectOption> options = selectItem.getOptions();
					if (options!=null) {
						Map<String, String> map = options.stream().collect(Collectors.toMap(ItemSelectOption::getId, ItemSelectOption::getValue));
						if (map.get(value.toString())!=null) {
							status = assemblyProcessDictionaryStatus(map.get(value.toString()));
						}
					}
				}
			}
		}

		Map<String, Object> iflowQueryParams = assemblyIflowQueryParams(items, queryParameters);
		// 查工作流
		Page<ProcessInstance> pageProcess = null;
		try {
			pageProcess = processInstanceService.page(page, pagesize, formModelEntity.getProcess().getKey(), status, iflowQueryParams);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException("查询数据失败了");
		}
		Map<String, ProcessInstance> instanceIdAndEditMap = pageProcess.getResults().stream().collect(Collectors.toMap(ProcessInstance::getFormInstanceId, processInstance -> processInstance));
		String[] formInstanceIds = pageProcess.getResults().stream().map(item->item.getFormInstanceId()).toArray(String[]::new);
		if (formInstanceIds!=null && formInstanceIds.length>0) {
			Optional<ItemModelEntity> idItemOption = formModelEntity.getItems().stream().filter(item->SystemItemType.ID == item.getSystemItemType()).findFirst();
			queryParameters = new HashMap<>();
			if (idItemOption.isPresent()) {
				queryParameters.put(idItemOption.get().getId(), formInstanceIds);
			}
			List<FormDataSaveInstance> list = formInstanceService.formInstance(formModelEntity, queryParameters);
			for (FormDataSaveInstance instance:list) {
				ProcessInstance processInstance = instanceIdAndEditMap.get(instance.getId());
				if (processInstance.getStatus()==ProcessInstance.Status.Running && processInstance.isMyTask()) {
					instance.setCanEdit(true);
				} else {
					instance.setCanEdit(false);
				}
				instance.setMyTask(processInstance.isMyTask());
				instance.setFunctions(processInstance.getCurrentTaskInstance() == null ? null : processInstance.getCurrentTaskInstance().getOperations());
				List<Map<String, Object>> stringObjectMap = processInstance.getCurrentTaskInstance() == null ? null : (List<Map<String, Object>>)(processInstance.getCurrentTaskInstance().getOperations());
				if(stringObjectMap != null) {
					Map<String, Map<String, Object>> map = new HashMap<>();
					for(Map<String, Object> objectMap : stringObjectMap){
						map.put((String)objectMap.get("id"), objectMap);
					}
					for(ItemInstance itemInstance : instance.getItems()){
						itemInstance.setProcessInstanceId(processInstance.getId());
						Map<String, Object> instanceMap = map.get(itemInstance.getId());
						if(instanceMap != null) {
							itemInstance.setVisible(instanceMap.get("visible") == null ? false : (Boolean)instanceMap.get("visible"));
							itemInstance.setCanFill(instanceMap.get("canFill") == null ? false : (Boolean)instanceMap.get("canFill"));
							itemInstance.setRequired(instanceMap.get("required") == null ? false : (Boolean)instanceMap.get("required"));
						}
					}
				}
			}
			Page<FormDataSaveInstance> pageInstance = Page.get(page, pagesize);
			pageInstance.data(pageProcess.getTotalCount(), list);
			return pageInstance;
		} else {
			return Page.get(page, pagesize);
		}
	}

	private Map<String, Object> assemblyIflowQueryParams(List<ItemModelEntity> items, Map<String, Object> queryParameters) {
		Map<String, Object> iflowQueryParams = new HashMap<>();
		for (ItemModelEntity item : items) {
			Object value = queryParameters.get(item.getId());
			if (value == null || item.getColumnModel() == null) {
				continue;
			}
			ColumnModelEntity columnModel = item.getColumnModel();
			if (isCommonItemType(item)) {
				iflowQueryParams.put(columnModel.getColumnName(), value);
			} else if (item instanceof SelectItemModelEntity) {
				// 如果是单选框，多选框，下拉框，手动提取对应的中文出来
				SelectItemModelEntity selectItem = (SelectItemModelEntity) item;
				if (value instanceof String[]) {
					String[] valueArr = (String[]) value;
					assemblyReferenceArrParams(valueArr, selectItem, iflowQueryParams, columnModel);
				} else {
					String valueStr = value.toString();
					assemblyReferenceStringParams(valueStr, selectItem, iflowQueryParams, columnModel);
				}
			}
		}
		return iflowQueryParams;
	}

	private void assemblyReferenceArrParams(String[] valueArr, SelectItemModelEntity selectItem, Map<String, Object> iflowQueryParams, ColumnModelEntity columnModel) {
		StringBuffer queryNames = new StringBuffer();
		for (String itemValue : valueArr) {
			if (SelectReferenceType.Table == selectItem.getSelectReferenceType()) {
				List<ItemSelectOption> options = selectItem.getOptions();
				for (ItemSelectOption selectOption : options) {
					if (selectOption.getId().equals(itemValue)) {
						queryNames.append(selectOption.getLabel() + ",");
					}
				}
			} else if (SelectReferenceType.Dictionary == selectItem.getSelectReferenceType()) {
				DictionaryDataItemEntity dictionaryItem = dictionaryService.getDictionaryItemById(itemValue);
				if (dictionaryItem != null) {
					queryNames.append(dictionaryItem.getName() + ",");
				}
			}
		}
		if (queryNames.toString().length() > 0) {
			iflowQueryParams.put(columnModel.getColumnName(), queryNames.toString());
		}
	}

	private void assemblyReferenceStringParams(String valueStr, SelectItemModelEntity selectItem, Map<String, Object> iflowQueryParams, ColumnModelEntity columnModel) {
		if (SelectReferenceType.Table == selectItem.getSelectReferenceType()) {
			List<ItemSelectOption> options = selectItem.getOptions();
			for (ItemSelectOption selectOption : options) {
				if (selectOption.getId().equals(valueStr)) {
					iflowQueryParams.put(columnModel.getColumnName(), selectOption.getLabel());
				}
			}
		} else if (SelectReferenceType.Dictionary == selectItem.getSelectReferenceType()) {
			DictionaryDataItemEntity dictionaryItem = dictionaryService.getDictionaryItemById(valueStr);
			if (dictionaryItem != null) {
				iflowQueryParams.put(columnModel.getColumnName(), dictionaryItem.getName());
			}
		}
	}

	public int assemblyActivitiStatus(SelectItemModelEntity selectItem, String valueId) {
		int status = -2; // -1表示查所有，0表示查未处理，1表示已处理，若最后status的值还是-2，表示不用查工作流
		if (SelectReferenceType.Dictionary == selectItem.getSelectReferenceType()) {
			DictionaryDataItemEntity dictionaryItem = dictionaryService.getDictionaryItemById(valueId);
			if (dictionaryItem != null) {
				status = assemblyProcessDictionaryStatus(dictionaryItem.getName());
			}
		} else {
			List<ItemSelectOption> options = selectItem.getOptions();
			Optional<ItemSelectOption> itemSelectOptionOption = options.stream().filter(item->valueId.equals(item.getId())).findFirst();
			if (itemSelectOptionOption.isPresent()) {
				status = assemblyProcessStatus(itemSelectOptionOption.get().getValue());
			}
		}
		return status;
	}

	@Override
	public void export(HttpServletResponse response,
					   @PathVariable(name="listId") String listId,
					   @RequestParam Map<String, Object> parameters) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel==null || listModel.getMasterForm()==null || StringUtils.isEmpty(listModel.getDisplayItemsSort())) {
			return;
		}
		List<String> ids = Arrays.asList(listModel.getDisplayItemsSort().split(","));
		List<ItemModelEntity> items = listModel.getDisplayItems();
		List<ItemModelEntity> sortList = new ArrayList<>();
		for (String id:ids) {
			Optional<ItemModelEntity> optional = items.stream().filter(item->id.equals(item.getId())).findFirst();
			if (optional.isPresent()) {
				sortList.add(optional.get());
			}
		}
		if (sortList==null || sortList.size()==0) {
			return;
		}
		ids = sortList.stream().map(ItemModelEntity::getId).collect(Collectors.toList());
		List<FormDataSaveInstance> data = page(listId, 1, 10000, parameters).getResults();

		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet(listModel.getName());
			response.setContentType("application/vnd.ms-excel");
			String filename = listModel.getName()+CommonUtils.currentTimeStr("-yyyy年MM月dd日-HHmmss")+".xlsx";
			filename = new String(filename.getBytes("utf-8"), "ISO8859-1");
			ExportUtils.outputHeaders(sortList.stream().map(ItemModelEntity::getName).toArray(String[]::new), sheet);
			response.setHeader("Content-Disposition", "attachment;filename="+filename);
			List<List<Object>> listData = new ArrayList<>();
			for (FormDataSaveInstance dataInstance:data) {
				List<ItemInstance> itemInstances = dataInstance.getItems();
				List<Object> lineList = new ArrayList<>();
				for (String id:ids) {
					Optional<ItemInstance> optional = itemInstances.stream().filter(item->id.equals(item.getId())).findFirst();
					if (optional.isPresent()) {
						lineList.add(optional.get().getDisplayValue());
					} else {
						lineList.add(null);
					}
				}
				listData.add(lineList);
			}
			ExportUtils.outputColumn(listData, sheet, 1);
			ServletOutputStream out = response.getOutputStream();
			wb.write(out);
			out.flush();
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 判断该表单是否绑定了工作流
	 */
	public boolean formModelHasProcess(FormModelEntity formModelEntity) {
		if (formModelEntity.getProcess() != null && StringUtils.hasText(formModelEntity.getProcess().getKey())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 是否是平常组件
	 * @param item
	 * @return
	 */
	public boolean isCommonItemType(ItemModelEntity item) {
		if (ItemType.Input == item.getType() ||
				ItemType.DatePicker == item.getType() ||
				ItemType.DatePicker == item.getType() ||
				ItemType.Editor == item.getType() ||
				ItemType.TimePicker == item.getType()) {
			return true;
		} else {
			return false;
		}
	}

	public int assemblyProcessDictionaryStatus(String valueStr) {
		if (StringUtils.isEmpty(valueStr)) {
			return -1;
		}
		switch (valueStr) {
			case "ALL":
				return -1;
			case "WORK":
				return 0;
			case "DONE":
				return 1;
		}
		return -1;
	}

	public int assemblyProcessStatus(String valueStr) {
		if (StringUtils.isEmpty(valueStr)) {
			return -2;
		}
		valueStr = valueStr.trim();
		switch (valueStr) {
			case "ALL":
				return -1;
			case "WORK":
				return 0;
			case "DONE":
				return 1;
		}
		return -2;
	}

	public Page<FormDataSaveInstance> formPage(@PathVariable(name="formId") String formId,
											   @RequestParam(name="page", defaultValue = "1") int page,
											   @RequestParam(name="pagesize", defaultValue = "10") int pagesize,
											   @RequestParam Map<String, Object> parameters) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel==null) {
			return Page.get(page, pagesize);
		}
		Map<String, Object> queryParameters = assemblyQueryParameters(parameters);
		return formInstanceService.pageFormInstance(formModel, page, pagesize, queryParameters);
	}

	public Map<String, Object> assemblyQueryParameters(@RequestParam Map<String, Object> parameters) {
		Map<String, Object> queryParameters = new HashMap<>();
		for (Map.Entry<String, Object> entry:parameters.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				if (!StringUtils.isEmpty(value)) {
					String valueStr = value.toString();
					if ("fullTextSearch".equals(entry.getKey())) {
						queryParameters.put("fullTextSearch", entry.getValue());
					} else if (valueStr.length()>32 && valueStr.substring(32,33).equals(",")) { // 如果传过来的参数是数组且以逗号划分开的话,组件ID的长度是32位，若第33位是逗号，当作数组处理
						queryParameters.put(entry.getKey(), valueStr.split(","));
					} else {
						queryParameters.put(entry.getKey(), valueStr);
					}
				}
			}
		}
		return queryParameters;
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
	public FormInstance getEmptyInstanceByTableName(@RequestParam(name="tableName", required = true) String tableName) {
		FormModelEntity entity = formModelService.findByTableName(tableName);
		if (entity == null) {
			return null;
		}
		return formInstanceService.newFormInstance(entity);
	}

	@Override
	public FormDataSaveInstance get(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		//return formInstanceService.getFormInstance(formModel, id);
		return formInstanceService.getFormDataSaveInstance(formModel, id);
	}

	@Override
	public IdEntity startProcess(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		return formInstanceService.startFormInstanceProcess(formModel, id);
	}

	/** 根据表单实例ID获取表单columnName与对应的取值value */
	@Override
	public Map getColumnNameValue(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id) {
		FormModelEntity formModel = formModelService.find(formId);
		FormDataSaveInstance formDataSaveInstance = formInstanceService.getFormDataSaveInstance(formModel, id);
		if (formDataSaveInstance!=null) {
		    Map map = toColumnNameValueDTO(formDataSaveInstance);
            // 添加一个formId，如果在键值对的详情页面要编辑数据时，前端可以通过formId获取表单详情
            map.put("formId", formDataSaveInstance.getFormId());
			return map;
		} else {
			return null;
		}
	}

	public Map toColumnNameValueDTO(FormDataSaveInstance formDataSaveInstance) {
		Map map = new HashMap();
		for (ItemInstance item:formDataSaveInstance.getItems()) {
			map.put(item.getColumnModelName(), item);
			item.setType(null);
			item.setVisible(null);
			item.setColumnModelId(null);
			item.setColumnModelName(null);
		}
		for (SubFormItemInstance sumForm:formDataSaveInstance.getSubFormData()) {
			map.put(sumForm.getTableName(), getSubFormItemInstance(sumForm));
		}
		return map;
	}

	public List<Map> getSubFormItemInstance(SubFormItemInstance sumForm) {
		List<Map> list = new ArrayList<>();
		for (SubFormDataItemInstance itemInstance:sumForm.getItemInstances()) {
			Map map = new HashMap();
			for (SubFormRowItemInstance rowItemInstance:itemInstance.getItems()) {
				for (ItemInstance item:rowItemInstance.getItems()) {
					map.put(item.getColumnModelName(), item);
					item.setColumnModelName(null);
					item.setColumnModelId(null);
					item.setVisible(null);
					item.setType(null);
				}
			}
			list.add(map);
		}
		return list;
	}

	@Override
	public FormDataSaveInstance getFormDataByListId(@PathVariable(name="listId") String listId, @PathVariable(name="id") String id) {
		ListModelEntity listModel = listModelService.find(listId);
		if (listModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}

		return formInstanceService.getFormDataSaveInstance(listModel.getMasterForm(), id);
	}

	@Override
	public FormDataSaveInstance getQrCode(@PathVariable(name="listId") String listId, @PathVariable(name="id") String id) {
		ListModelEntity formModel = listModelService.find(listId);
		if (formModel == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}
		return formInstanceService.getQrCodeFormDataSaveInstance(formModel, id);
	}

	@Override
	public IdEntity createFormInstance(@PathVariable(name="formId", required = true) String formId, @RequestBody FormDataSaveInstance formInstance) {
		if (!formId.equals(formInstance.getFormId())) {
			throw new IFormException("表单id不一致");
		}
		formInstance.setFormId(formId);
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		String id = formInstanceService.createFormInstance(formModel, formInstance);
		//TODO 上传数据二维码
		//uploadDataQrCode( formModel, id);
		return new IdEntity(id);
	}

	private FileUploadModel createDataQrCode(String listId, FormModelEntity formModel, String id){
		FileUploadModel qrCodeFileUploadModel = null;
        String httpHead = qrcodeBaseUrl != null && qrcodeBaseUrl.contains("http") ? qrcodeBaseUrl : "http://"+qrcodeBaseUrl;
		try {
			InputStream inputStream = getInputStream(httpHead+"?status=check&listId="+listId+"&formId="+formModel.getId()+"&listRowId="+id, new String(qrcodeName.getBytes("UTF-8"),"UTF-8"));
            FileUploadModel fileUploadModel = uploadService.uploadOneFileByInputstream(formModel.getName()+"_"+id+".png" ,inputStream,"image/png");
			fileUploadModel.setSourceType(DataSourceType.FormModel);
			fileUploadModel.setFromSource(formModel.getId());
			fileUploadModel.setFromSourceDataId(id);
			FileUploadEntity fileUploadEntity = uploadService.saveFileUploadEntity(fileUploadModel);
			qrCodeFileUploadModel = new FileUploadModel();
			BeanUtils.copyProperties(fileUploadEntity, qrCodeFileUploadModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return qrCodeFileUploadModel;
	}

	private InputStream getInputStream(String skipUrl, String note) throws  Exception{
		URL logoUrl = new URL(minioConfig.getLogoUrl());
		URL backUrl = new URL(minioConfig.getBackUrl());
		InputStream is = ZXingCodeUtils.createLogoQRCode(logoUrl, skipUrl, note);
		InputStream inputStream = null;
		if(backUrl != null && StringUtils.hasText(backUrl.getFile())) {
			inputStream = MergedQrCodeImages.mergeImage(backUrl.openStream(), is, "63", "163");
		}else{
			inputStream = is;
		}
        return inputStream;
	}

	@Override
	public void updateFormInstance(@PathVariable(name="formId", required = true) String formId, @PathVariable(name="id" , required = true) String id, @RequestBody FormDataSaveInstance formInstance) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		if (!formId.equals(formInstance.getFormId()) || !id.equals(formInstance.getId())) {
			throw new IFormException(404, "表单模型id不一致");
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

	@Override
	public FileUploadModel resetQrCode(@PathVariable(name="listId", required = true) String listId, @PathVariable(name="id", required = true) String id) {
		ListModelEntity listModelEntity = listModelService.get(listId);
		if (listModelEntity == null) {
			throw new IFormException(404, "列表模型【" + listId + "】不存在");
		}
		FormModelEntity formModel = listModelEntity.getMasterForm();
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formModel.getId() + "】不存在");
		}
		List<FileUploadEntity> fileUploadEntityList = uploadService.getFileUploadEntity(DataSourceType.FormModel, formModel.getId(), id);
		List<FileUploadModel> fileUploadModels = new ArrayList<>();
		String httpHead = qrcodeBaseUrl != null && qrcodeBaseUrl.contains("http") ? qrcodeBaseUrl : "http://"+qrcodeBaseUrl;
		try {
			for(FileUploadEntity fileUploadEntity : fileUploadEntityList) {
				InputStream inputStream = getInputStream(httpHead+"?status=check&listId="+listId+"&formId="+formModel.getId()+"&listRowId="+id, new String(qrcodeName.getBytes("UTF-8"),"UTF-8"));
				uploadService.resetUploadOneFileByInputstream(fileUploadEntity.getFileKey(), inputStream, "image/png");
				FileUploadModel fileUploadModel = new FileUploadModel();
				BeanUtils.copyProperties(fileUploadEntity, fileUploadModel);
				fileUploadModels.add(fileUploadModel);
			}
			if(fileUploadEntityList == null || fileUploadEntityList.size() < 1) {
				fileUploadModels.add(createDataQrCode(listId, formModel, id));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException(404, "表单模型【" + formModel.getId() + "】,生成【" + id + "】二维码失败");
		}
		return fileUploadModels == null || fileUploadModels.size() < 1 ? null : fileUploadModels.get(0);
	}

	@Override
	public Page<FormDataSaveInstance> findByTableNameAndColumnValue(@RequestParam(name="page", defaultValue = "1") int page,
																	@RequestParam(name="pagesize", defaultValue = "10") int pagesize,
																	@RequestParam(name="tableName", defaultValue = "") String tableName,
																	@RequestParam Map<String, Object> parameters) {
		if (StringUtils.isEmpty(tableName)) {
			return Page.get(page, pagesize);
		}
		parameters = parameters==null? new HashMap():parameters;
		parameters.remove(tableName);
		FormModelEntity formModelEntity = formModelService.query().filterEqual("dataModels.tableName", tableName).first();
		if (formModelEntity!=null) {
			List<ItemModelEntity> items = formModelEntity.getItems();
			if (items==null || items.size()==0) {
				return Page.get(page, pagesize);
			}
			Map<String, String> columnNameAndItemIdMap = formInstanceService.columnNameAndItemIdMap(items);
			if (columnNameAndItemIdMap==null || columnNameAndItemIdMap.size()==0) {
				return Page.get(page, pagesize);
			}
			Map<String, Object> itemIdParameters = new HashMap();
			for (String key:parameters.keySet()) {
				if (columnNameAndItemIdMap.containsKey(key)) {
					itemIdParameters.put(columnNameAndItemIdMap.get(key), parameters.get(key));
				}
			}
			return formPage(formModelEntity.getId(), page, pagesize, itemIdParameters);
		}
		return Page.get(page, pagesize);
	}

	@Override
	public Page<Map> getColumnNameValueByTable(@RequestParam(name="page", defaultValue = "1") int page,
											   @RequestParam(name="pagesize", defaultValue = "10") int pagesize,
											   @RequestParam(name="tableName", defaultValue = "") String tableName,
											   @RequestParam Map<String, Object> parameters) {
		Page pageInstance = findByTableNameAndColumnValue(page,  pagesize, tableName, parameters);
		List<FormDataSaveInstance> results = pageInstance.getResults();
		if (results!=null && results.size()>0) {
			List<Map> list = new ArrayList();
			for (FormDataSaveInstance formDataSaveInstance:results) {
				Map map = toColumnNameValueDTO(formDataSaveInstance);
				// 添加一个formId，如果在键值对的详情页面要编辑数据时，前端可以通过formId获取表单详情
				map.put("formId", formDataSaveInstance.getFormId());
				list.add(map);
			}
			pageInstance.setContent(list);
		}
		return pageInstance;
	}

	/**
	{
		"navigations": [
			{ "name": "地图", "iconName": "ditu3", "screenKey": "Map", "screenType": "Inherent", initialPage: true },
			{ "name": "工作台", "iconName": "gongzuotai1", "screenKey": "Dashboard", "screenType": "Inherent", initialPage: false },
			{ "name": "个人中心", "iconName": "gerenzhongxin", "screenKey": "User", "screenType": "Inherent", initialPage: false }
		],
		"dashboard":[{
			"id": "id", "name": "巡河上报",
			"children": [
				{ "id": "id", "name": "巡河上报", "screenKey": "RiverPatrol", "screenType": "Inherent" },
				{ "id": "PatrolHistory", "name": "巡河历史", "screenKey": "PatrolHistory", "screenType": "Inherent" },
				{ "id": "ReportHistory", "name": "上报历史", "screenKey": "ReportHistory", "screenType": "Inherent" }
			]
		}, {
			"id": "id", "name": "河流动态",
			"children": [{ "id": "SixOne", "name": "河流六个一", "screenKey": "SixOne", "screenType": "Inherent" }]
		}, {
			"id": "id", "name": "河长制业务",
			"children": [
				{ "id": "EventManagent", "name": "事件管理", "screenKey": "EventManagent", "screenType": "Inherent" },
				{ "id": "SixOne", "name": "河长交办", "screenKey": "RiverManagerAssign", "screenType": "Inherent" },
				{ "id": "NoticeNews", "name": "通知新闻", "screenKey": "NoticeNews", "screenType": "Inherent" },
				{ "id": "OneWeekOneReport", "name": "一周一报", "screenKey": "OneWeekOneReport", "screenType": "Inherent" },
				{ "id": "RiverManagerPat", "name": "河长拍", "screenKey": "RiverManagerPat", "screenType": "Inherent" }
			]
		}, {
			"id": "id", "name": "统计分析",
			"children": [{ "id": "StatisticsAnalysis", "name": "统计分析", "screenKey": "StatisticsAnalysis", "screenType": "Inherent" }]
		}]
	 }
	 */
	@Override
	public Map dashboard(@PathVariable(name="userId", required = true) String userId) throws IOException {
		Map<String, List> dataMap = new HashMap();
		FormModelEntity formModelEntity = formModelService.findByTableName("strategy_group");
		if (formModelEntity==null) {
			throw new IFormException(404, "没有名称为strategy_group的策略组的数据建模");
		}
		List<Position> positions = userService.queryUserPositions(userId);
		if (positions==null || positions.size()==0) {
			// 该用户没有导航和应用分类时时，也要返回 navigations和dashboard 的字段，保持结构一致
			dataMap.put("navigations", new ArrayList());
			dataMap.put("dashboard", new ArrayList());
			return dataMap;
		}
		Set<String> positionIdSet = positions.stream().map(item->item.getId()).collect(Collectors.toSet());
		Page<FormDataSaveInstance> pageData = formInstanceService.pageFormInstance(formModelEntity, 1, 100, new HashMap());
		List<Map> list = new ArrayList();
		// 转成columnName与value对应关系
		for (FormDataSaveInstance formDataSaveInstance:pageData.getResults()) {
			list.add(toColumnNameValueDTO(formDataSaveInstance));
		}

		JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(list));
		Map<String,List> positionMap = new HashMap();

		// 封装成
		for (int i = 0; i < (Integer)JSONPath.eval(jsonArray, "$.size()"); i++) {
			Object positionObjects = JSONPath.eval(jsonArray, "$["+i+"].position.value");
			if (positionObjects==null || (positionObjects instanceof List)==false) {
				continue;
			}
			List<Navigations> navigations = new ArrayList();
			for (int j = 0; j < (Integer) JSONPath.eval(jsonArray, "$[" + i + "].navigations.size()"); j++) {
				Object idObj = JSONPath.eval(jsonArray, "$[" + i + "].navigations[" + j + "].id.value");
				Object nameObj = JSONPath.eval(jsonArray, "$[" + i + "].navigations[" + j + "].name.displayObject[0].description");
				Object iconObj = JSONPath.eval(jsonArray, "$[" + i + "].navigations[" + j + "].name.displayObject[0].icon");
				Object screenKeyObj = JSONPath.eval(jsonArray, "$[" + i + "].navigations[" + j + "].name.displayObject[0].code");
				Object screenTypeObj = JSONPath.eval(jsonArray, "$[" + i + "].navigations[" + j + "].screenType.displayObject[0].code");
				Boolean initialPage = convertInitialPage(JSONPath.eval(jsonArray, "$[" + i + "].navigations[" + j + "].initialPage.displayObject[0].code"));
				navigations.add(new Navigations(idObj, nameObj, iconObj, screenKeyObj, screenTypeObj, initialPage));
			}

			List<Dashboard> dashboard = new ArrayList();
			for (int j = 0; j < (Integer) JSONPath.eval(jsonArray, "$[" + i + "].dashboard.size()"); j++) {
				Object idObj = JSONPath.eval(jsonArray, "$[" + i + "].dashboard[" + j + "].id.value");
				Object nameObj = JSONPath.eval(jsonArray, "$[" + i + "].dashboard[" + j + "].name.displayObject[0].description");
				Object iconObj = JSONPath.eval(jsonArray, "$[" + i + "].dashboard[" + j + "].name.displayObject[0].icon");
				Object screenKeyObj = JSONPath.eval(jsonArray, "$[" + i + "].dashboard[" + j + "].name.displayObject[0].code");
				Object screenTypeObj = JSONPath.eval(jsonArray, "$[" + i + "].dashboard[" + j + "].screenType.displayObject[0].code");
				Object categoryCodeObj = JSONPath.eval(jsonArray, "$[" + i + "].dashboard[" + j + "].businessCategories.displayObject[0].code");
				Object categoryNameObj = JSONPath.eval(jsonArray, "$[" + i + "].dashboard[" + j + "].businessCategories.displayObject[0].description");
				dashboard.add(new Dashboard(idObj, nameObj, iconObj, screenKeyObj, screenTypeObj, categoryCodeObj, categoryNameObj));
			}

			List<String> positionIds = (List<String>)positionObjects;
			for (String positionId:positionIds) {
				assemblyMapData(positionId+"-navigations", positionMap, navigations);
				assemblyMapData(positionId+"-dashboard", positionMap, dashboard);
			}
		}
		for (String positionId:positionIdSet) {
			assemblyMapData("navigations", dataMap, positionMap.get(positionId+"-navigations"));
			assemblyMapData("dashboard", dataMap, positionMap.get(positionId+"-dashboard"));
		}
		Map returnMap = new HashMap();
		// navigations去重
		if (dataMap.get("navigations")!=null) {
			returnMap.put("navigations", new LinkedHashSet(dataMap.get("navigations")));
		} else {
		// 该用户没有导航时，也要返回 navigations 的字段，保持结构一致
			returnMap.put("navigations", new ArrayList());
		}

		// dashboard去重
		if (dataMap.get("dashboard")!=null) {
			List<Dashboard> dashboard = dataMap.get("dashboard");
			Set<Dashboard> parents = new LinkedHashSet();
			for (Dashboard item:new LinkedHashSet<>(dashboard)) {
				if (StringUtils.hasText(item.getCategoryCode())) {
					parents.add(new Dashboard(item.getCategoryCode(), item.getCategoryName(), item.getCategoryCode()));
				}
			}
			for (Dashboard parent:parents) {
				List<Dashboard> items = new ArrayList();
				Set<String> set = new HashSet();
				for (Dashboard item:dashboard) {
					if (!set.contains(item.getScreenKey()) && parent.getCategoryCode().equals(item.getCategoryCode())) {
						set.add(item.getScreenKey());
						items.add(item);
					}
				}
				parent.setChildren(items);
			}
			returnMap.put("dashboard", parents);
		} else {
		// 该用户没有应用分类时时，也要返回 dashboard 的字段，保持结构一致
			returnMap.put("dashboard", new ArrayList());
		}
		return returnMap;
	}

	/**
	 * 在map中对应的keyPath路径追加needPutData数据
	 * @param keyPath
	 * @param map
	 * @param needAndData
	 */
	public void assemblyMapData(String keyPath, Map<String, List> map, List needAndData) {
		if (needAndData!=null) {
			List<Map> nav = map.get(keyPath);
			if (nav == null) {
				nav = new ArrayList();
				map.put(keyPath, nav);
			}
			nav.addAll(needAndData);
		}
	}

	public Boolean convertInitialPage(Object initialPage) {
		if (initialPage!=null && "true".equals(initialPage.toString())) {
			return true;
		} else {
			return false;
		}
	}
}
