package tech.ascs.icity.iform.controller;

import java.util.*;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.googlecode.genericdao.search.Filter;

import io.swagger.annotations.Api;
import tech.ascs.icity.admin.api.model.Application;
import tech.ascs.icity.admin.client.ApplicationService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.ColumnModelService;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.jpa.tools.DTOTools;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "数据模型服务", description = "包含数据模型的增删改查等功能")
@RestController
public class DataModelController implements tech.ascs.icity.iform.api.service.DataModelService {

	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private ColumnModelService columnModelService;

	@Autowired
	private ApplicationService applicationService;


	@Override
	public List<DataModel> list(@RequestParam(name="name", required=false) String name, @RequestParam(name = "sync", required=false) String sync,
								@RequestParam(name = "modelType", required=false) String modelType, @RequestParam(name = "applicationId", required=false) String applicationId) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterOr(Filter.like("name", "%" + name + "%"), Filter.like("tableName", "%" + name + "%"));
			}
			if (StringUtils.hasText(sync)) {
				query.filterEqual("synchronized_", "1".equals(sync));
			}
			if (StringUtils.hasText(modelType)) {
				query.filterIn("modelType",  getDataModelType(modelType));
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}

			List<DataModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<ApplicationModel> listReferenceDataModel(@RequestParam(name = "tableName", required = false) String tableName,
													  @RequestParam(name = "modelType", required = false) String modelType, @RequestParam(name = "applicationId", required=false) String applicationId) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			if (StringUtils.hasText(tableName)) {
				query.filterNotEqual("tableName",  tableName );
			}
			if (StringUtils.hasText(modelType)) {
				query.filterIn("modelType", getDataModelType(modelType));
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}
			List<DataModelEntity> entities = query.list();
			return list(DTOTools.wrapList(entities, DataModelInfo.class));
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	private List<ApplicationModel> list(List<DataModelInfo> entities){
		Map<String, List<DataModelInfo>> map = new HashMap<>();
		for(DataModelInfo entity : entities){

			if(!StringUtils.hasText(entity.getApplicationId())){
				continue;
			}
			List<DataModelInfo> list = map.get(entity.getApplicationId());
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(entity);
			map.put(entity.getApplicationId(), list);
		}
		List<ApplicationModel> applicationFormModels = new ArrayList<>();
		if(map != null && map.size() > 0) {
			//TODO 查询应用
			Set<String> c = map.keySet();
			String[] applicationIds =  new String[c.size()];
			c.toArray(applicationIds);
			List<Application> applicationList = applicationService.queryAppsByIds(new ArrayList<>(c));
			for(Application application : applicationList){
				ApplicationModel applicationFormModel = new ApplicationModel();
				applicationFormModel.setId(application.getId());
				applicationFormModel.setName(application.getApplicationName());
				applicationFormModel.setDataModels(map.get(application.getId()));
				applicationFormModels.add(applicationFormModel);
			}
		}

		return applicationFormModels;
	}

	private List<DataModelType> getDataModelType(String modelType){
		String[] modelTypeArray = modelType.split(",");
		List<DataModelType> list = new ArrayList<>();
		for(String str : modelTypeArray){
			list.add(DataModelType.valueOf(str));
		}
		return list;
	}

	@Override
	public Page<DataModel> page(
			@RequestParam(name="name", required=false) String name,
			@RequestParam(name="sync", required=false) String sync,
			@RequestParam(name="page", defaultValue="1") int page,
			@RequestParam(name="pagesize", defaultValue="10") int pagesize, @RequestParam(name = "applicationId", required=false) String applicationId) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterOr(Filter.like("name", "%" + name + "%"), Filter.like("tableName", "%" + name + "%"));
			}
			if (StringUtils.hasText(sync)) {
				query.filterEqual("synchronized_", "1".equals(sync));
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}
			Page<DataModelEntity> entities = query.page(page, pagesize).page();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<DataModelInfo> getMasterModels(@RequestParam(name = "applicationId", required=false) String applicationId) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			query.filterNotEqual("modelType", DataModelType.Slaver);

			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId",  applicationId);
			}
			List<DataModelEntity> entities = query.list();
			return DTOTools.wrapList(entities, DataModelInfo.class);
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public DataModel get(@PathVariable(name="id") String id) {
		DataModelEntity entity = dataModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "数据模型【" + id + "】不存在");
		}
		try {
			return toDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取数据模型失败：" + e.getMessage(), e);
		}
	}

	@Override
	public IdEntity createDataModel(@RequestBody DataModel dataModel) {
		if (StringUtils.hasText(dataModel.getId())) {
			throw new IFormException("数据模型ID不为空，请使用更新操作");
		}
		try {
			DataModelEntity entity = dataModelService.save(dataModel);
			return new IdEntity(entity.getId());
		} catch (Exception e) {
			throw new IFormException("保存数据模型失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void updateDataModel(@PathVariable(name = "id") String id, @RequestBody DataModel dataModel) {
		if (!StringUtils.hasText(dataModel.getId()) || !id.equals(dataModel.getId())) {
			throw new IFormException("数据模型ID不一致");
		}
		try {
			dataModelService.save(dataModel);
		} catch (Exception e) {
			throw new IFormException("保存数据模型失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void removeDataModel(@PathVariable(name="id") String id) {
		DataModelEntity dataModelEntity = dataModelService.get(id);
		if(dataModelEntity != null){
			dataModelService.deleteDataModel(dataModelEntity);
		}
	}

	@Override
	public List<DataModel> findDataModelByFormId(@PathVariable(name="formId") String formId) {
		return dataModelService.findDataModelByFormId(formId);
	}

	@Override
	public void syncDataModel(@PathVariable(name="id") String id) {
		DataModelEntity entity = dataModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "数据模型【" + id + "】不存在");
		}
		if (entity.getModelType() == DataModelType.Slaver) {
			throw new IFormException("不能直接同步从表，请同步所属主表【" + entity.getMasterModel().getName() + "】");
		}

		dataModelService.sync(entity);
	}

	private Page<DataModel> toDTO(Page<DataModelEntity> entities) throws InstantiationException, IllegalAccessException {
		Page<DataModel> dataModels = Page.get(entities.getPage(), entities.getPagesize());
		dataModels.data(entities.getTotalCount(), toDTO(entities.getResults()));
		return dataModels;
	}

	private List<DataModel> toDTO(List<DataModelEntity> entities) throws InstantiationException, IllegalAccessException {
		List<DataModel> dataModels = new ArrayList<DataModel>();
		for (DataModelEntity entity : entities) {
			dataModels.add(toDTO(entity));
		}
		return dataModels;
	}

	private DataModel toDTO(DataModelEntity entity) throws InstantiationException, IllegalAccessException {
		DataModel dataModel = toDataModelDTO(entity);

		if (entity.getColumns() != null && !entity.getColumns().isEmpty()) {
			List<ColumnModel> columns = new ArrayList<ColumnModel>();
			for (ColumnModelEntity columnEntity : entity.getColumns()) {
				ColumnModel column = toDTO(columnEntity);
				column.setDataModel(dataModel);
				columns.add(column);
			}
			dataModel.setColumns(columns);
		}

		if (entity.getIndexes() != null && !entity.getIndexes().isEmpty()) {
			List<IndexModel> indexes = new ArrayList<IndexModel>();
			for (IndexModelEntity indexEntity : entity.getIndexes()) {
				IndexModel index = toDTO(indexEntity);
				index.setDataModel(dataModel);
				indexes.add(index);
			}
			dataModel.setIndexes(indexes);
		}
		return dataModel;
	}

	private DataModel toDataModelDTO(DataModelEntity entity)  {
		DataModel dataModel = new DataModel();
		BeanUtils.copyProperties(entity, dataModel, new String[] {"masterModel","slaverModels","columns", "indexes","referencesDataModel"});
		dataModel.setSynchronized(entity.getSynchronized());

		if(entity.getMasterModel() != null){
			DataModelInfo masterModel = new DataModelInfo();
			BeanUtils.copyProperties(entity.getMasterModel(), masterModel, new String[]{});
			dataModel.setMasterModel(masterModel);
		}

		if(entity.getSlaverModels() != null){
			List<DataModelInfo> slaverModels = new ArrayList<>();
			for(DataModelEntity dataModelEntity : entity.getSlaverModels()) {
				DataModelInfo dataModelInfo = new DataModelInfo();
				BeanUtils.copyProperties(dataModelEntity, dataModelInfo, new String[]{});
				slaverModels.add(dataModelInfo);
			}
			dataModel.setSlaverModels(slaverModels);
		}
		return dataModel;
	}

	private ColumnModel toDTO(ColumnModelEntity entity) throws InstantiationException, IllegalAccessException {
		ColumnModel columnModel = new ColumnModel();
		BeanUtils.copyProperties(entity, columnModel, new String[] {"dataModel","columnReferences"});
		columnModel.setReferenceTables(columnModelService.getReferenceModel(entity));
		columnModel.setDataModel(toDataModelDTO(entity.getDataModel()));
		return columnModel;
	}

	private IndexModel toDTO(IndexModelEntity entity) throws InstantiationException, IllegalAccessException {
		IndexModel indexModel = new IndexModel();
		BeanUtils.copyProperties(entity, indexModel, new String[] {"dataModel","columns"});
		return indexModel;
	}
}
