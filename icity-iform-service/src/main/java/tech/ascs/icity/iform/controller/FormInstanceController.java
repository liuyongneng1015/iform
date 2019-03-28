package tech.ascs.icity.iform.controller;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.*;
import tech.ascs.icity.iform.service.impl.UploadServiceImpl;
import tech.ascs.icity.iform.utils.*;
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

	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	private UploadService uploadService;

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

	@Autowired
	private DictionaryService dictionaryService;

	// url?param1=value1&param2=value2&param2=value3,value4&param2=value5
	// @RequestParam Map<String, Object> parameters 有两个问题
	// 1) 因为Object没有指定具体类型，接收后会变成字符串
	// 2) 接收数组时，相同的Key会被覆盖掉，接收 param1=value1&param2=value2 的 param1参数，map的键值对会覆盖掉相同的Key
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
		Set<String> itemIds = queryParameters.keySet();
		if (itemIds!=null && itemIds.size()>0) {
			List<ItemModelEntity> items = itemModelService.query().filterIn("id", itemIds).list();
			Optional<ItemModelEntity> optional = items.stream().filter(item->(
								(item instanceof SelectItemModelEntity && (((SelectItemModelEntity)item).getMultiple()==null || ((SelectItemModelEntity)item).getMultiple()==false))
							) && "处理状态".equals(item.getName())).findFirst();
			if (optional.isPresent()) {
				ItemModelEntity statusItem = optional.get();
				String valueId = queryParameters.get(statusItem.getId())!=null ? queryParameters.get(statusItem.getId()).toString() : null;
				if (StringUtils.hasText(valueId)) {
					int status = 0; // 1表示查所有，2表示查待办理，3表示已办理，若最后status的值还是0，表示不用查工作流
					SelectItemModelEntity selectItem = (SelectItemModelEntity) statusItem;
					if (SelectReferenceType.Table == selectItem.getSelectReferenceType()) {
						List<ItemSelectOption> options = selectItem.getOptions();
						for (ItemSelectOption selectOption:options) {
							status = assemblyProcessStatus(selectOption.getLabel());
							if (status!=1 && status!=2 && status!=3) {
								break;
							}
						}
					} else if (SelectReferenceType.Dictionary == selectItem.getSelectReferenceType()) {
						DictionaryItemEntity dictionaryItem = dictionaryService.getDictionaryItemById(valueId);
						if (dictionaryItem!=null) {
							status = assemblyProcessStatus(dictionaryItem.getName());
						}
					}
					if (status!=0) {
						Map<String, Object> iflowQueryParams = new HashMap<>();
						for (ItemModelEntity item:items) {
							Object value = queryParameters.get(item.getId());
							if (value==null) {
								continue;
							}
							ColumnModelEntity columnModel = item.getColumnModel();
							if (columnModel==null) {
								continue;
							}
//							if (SystemItemType.Input==item.getSystemItemType() ||
//								SystemItemType.MoreInput==item.getSystemItemType() ||
//								SystemItemType.InputNumber==item.getSystemItemType() ||
//								SystemItemType.TimePicker==item.getSystemItemType() ||
//								SystemItemType.Editor==item.getSystemItemType() ||
//								SystemItemType.Label==item.getSystemItemType() ||
//								SystemItemType.Description==item.getSystemItemType()) {
//								iflowQueryParams.put(columnModel.getColumnName(), value);
//							}
							if (ItemType.Input==item.getType() ||
								ItemType.DatePicker==item.getType() ||
								ItemType.DatePicker==item.getType() ||
								ItemType.Editor==item.getType() ||
								ItemType.TimePicker==item.getType()) {
								iflowQueryParams.put(columnModel.getColumnName(), value);
							} else if (item instanceof SelectItemModelEntity) {
								selectItem = (SelectItemModelEntity) item;
								if (value instanceof String[]) {
									String[] valueArr = (String[])value;
									StringBuffer queryNames = new StringBuffer();
									for (String valueItem:valueArr) {
										if (SelectReferenceType.Table == selectItem.getSelectReferenceType()) {
											List<ItemSelectOption> options = selectItem.getOptions();
											for (ItemSelectOption selectOption : options) {
												if (selectOption.equals(valueItem)) {
													queryNames.append(selectOption.getLabel()+",");
												}
											}
										} else if (SelectReferenceType.Dictionary == selectItem.getSelectReferenceType()) {
											DictionaryItemEntity dictionaryItem = dictionaryService.getDictionaryItemById(valueItem);
											if (dictionaryItem != null) {
												queryNames.append(dictionaryItem.getName()+",");
											}
										}
									}
									if (queryNames.toString().length()>0) {
										iflowQueryParams.put(columnModel.getColumnName(), queryNames.toString());
									}
								} else {
									String valueStr = value.toString();
									if (SelectReferenceType.Table == selectItem.getSelectReferenceType()) {
										List<ItemSelectOption> options = selectItem.getOptions();
										for (ItemSelectOption selectOption:options) {
											if (selectOption.equals(valueStr)) {
												iflowQueryParams.put(columnModel.getColumnName(), selectOption.getLabel());
											}
										}
									} else if (SelectReferenceType.Dictionary == selectItem.getSelectReferenceType()) {
										DictionaryItemEntity dictionaryItem = dictionaryService.getDictionaryItemById(valueStr);
										if (dictionaryItem!=null) {
											iflowQueryParams.put(columnModel.getColumnName(), dictionaryItem.getName());
										}
									}
								}
							}
						}

						if (status==1) {
							// 1表示查所有，2表示查待办理，3表示已办理，若最后status的值还是0，表示不用查工作流
						} else if (status==2) {
							// 1表示查所有，2表示查待办理，3表示已办理，若最后status的值还是0，表示不用查工作流
						} else if (status==3) {
							// 1表示查所有，2表示查待办理，3表示已办理，若最后status的值还是0，表示不用查工作流
						}
					}
				}
			}
		}
		return formInstanceService.pageFormInstance(listModel, page, pagesize, queryParameters);
	}

	public int assemblyProcessStatus(String statusStr) {
		if (StringUtils.isEmpty(statusStr)) {
			return 0;
		}
		switch (statusStr) {
			case "全部":
				return 1;
			case "待处理":
				return 2;
			case "已处理":
				return 3;
		}
		return 0;
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
					// 如果传过来的参数是数组且以逗号划分开的话,组件ID的长度是32位，若第33位是逗号，当作数组处理
					if (valueStr.length()>32 && valueStr.substring(32,33).equals(",")) {
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
	public FormInstance get(@PathVariable(name="formId") String formId, @PathVariable(name="id") String id) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		return formInstanceService.getFormInstance(formModel, id);
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
		formInstance.setId(formId);
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		String id = formInstanceService.createFormInstance(formModel, formInstance);
		//TODO 上传数据二维码
		//uploadDataQrCode( formModel, id);
		return new IdEntity(id);
	}

	private FileUploadModel createDataQrCode(FormModelEntity formModel, String id){
		FileUploadModel qrCodeFileUploadModel = null;
		try {
			InputStream inputStream = getInputStream("www.baidu.com", new String("航天智慧cityworks".getBytes("UTF-8"),"UTF-8"));
			FileUploadModel fileUploadModel = uploadService.uploadOneFileByInputstream(formModel.getName()+"_"+id+".png" ,inputStream,"image/png");
			fileUploadModel.setUploadType(FileUploadType.FormModel);
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

	@Override
	public FileUploadModel resetQrCode(@PathVariable(name="formId", required = true) String formId, @PathVariable(name="id", required = true) String id) {
		FormModelEntity formModel = formModelService.find(formId);
		if (formModel == null) {
			throw new IFormException(404, "表单模型【" + formId + "】不存在");
		}
		List<FileUploadEntity> fileUploadEntityList = uploadService.getFileUploadEntity(FileUploadType.FormModel, formId, id);
		List<FileUploadModel> fileUploadModels = new ArrayList<>();
		try {
			for(FileUploadEntity fileUploadEntity : fileUploadEntityList) {
				InputStream inputStream = getInputStream("https://www.baidu.com", new String("航天智慧cityworks".getBytes("UTF-8"),"UTF-8"));
				uploadService.resetUploadOneFileByInputstream(fileUploadEntity.getFileKey(), inputStream, "image/png");
				FileUploadModel fileUploadModel = new FileUploadModel();
				BeanUtils.copyProperties(fileUploadEntity, fileUploadModel);
				fileUploadModels.add(fileUploadModel);
			}
			if(fileUploadEntityList == null || fileUploadEntityList.size() < 1) {
				fileUploadModels.add(createDataQrCode(formModel, id));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IFormException(404, "表单模型【" + formId + "】,生成【" + id + "】二维码失败");
		}
		return fileUploadModels == null || fileUploadModels.size() < 1 ? null : fileUploadModels.get(0);
	}
}
