package tech.ascs.icity.iform.controller;

import java.util.*;
import java.util.regex.Pattern;

import com.googlecode.genericdao.search.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "数据模型服务", description = "包含数据模型的增删改查等功能")
@RestController
public class DataModelController implements tech.ascs.icity.iform.api.service.DataModelService {
	private final Logger log = LoggerFactory.getLogger(DataModelController.class);

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
	public List<DataModel> findAllList(String name, String sync, String modelType, String applicationId) {
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
			return toSimpleDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<ApplicationModel> listReferenceDataModel(@RequestParam(name = "tableName", required = false) String tableName,
													 	 @RequestParam(name = "modelType", required = false) String modelType,
														 @RequestParam(name = "applicationId", required = true) String applicationId) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			if (StringUtils.hasText(tableName)) {
				query.filterNotEqual("tableName",  tableName );
			}
			if (StringUtils.hasText(modelType)) {
				query.filterIn("modelType", getDataModelType(modelType));
			}
			List<DataModelEntity> entities = query.list();
			return list(applicationId, toDTO(entities));
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	private List<ApplicationModel> list(String applictionId, List<DataModel> entities){
		if(entities == null){
			return new ArrayList<>();
		}
		Map<String, List<DataModel>> map = new HashMap<>();
		for(DataModel entity : entities){
			if(!StringUtils.hasText(entity.getApplicationId())){
				continue;
			}
			List<DataModel> list = map.get(entity.getApplicationId());
			if(list == null){
				list = new ArrayList<>();
			}
			entity.setName(entity.getTableName());
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
			if(applicationList != null) {
				for (Application application : applicationList) {
					if(application.getId().equals(applictionId)){
						applicationFormModels.add(createApplicationModel(application, map));
						break;
					}
				}

				for (Application application : applicationList) {
					if(application.getId().equals(applictionId)){
						continue;
					}
					applicationFormModels.add(createApplicationModel(application, map));
				}
			}
		}

		return applicationFormModels;
	}

	private ApplicationModel createApplicationModel(Application application, Map<String, List<DataModel>> map){
		ApplicationModel applicationFormModel = new ApplicationModel();
		applicationFormModel.setId(application.getId());
		applicationFormModel.setName(application.getApplicationName());
		applicationFormModel.setDataModels(map.get(application.getId()));
		return applicationFormModel;
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
			Page<DataModelEntity> entities = query.sort(Sort.desc("id")).page(page, pagesize).page();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<DataModel> getMasterModels(@RequestParam(name = "applicationId", required=false) String applicationId) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			query.filterNotEqual("modelType", DataModelType.Slaver);

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

	private String regEx = "[a-zA-Z]{1,}[a-zA-Z0-9_]{0,}";
	@Override
	public IdEntity createDataModel(@RequestBody DataModel dataModel) {
		if (StringUtils.hasText(dataModel.getId())) {
			throw new IFormException("数据模型ID不为空，请使用更新操作");
		}
		if (StringUtils.isEmpty(dataModel.getTableName())) {
			throw new IFormException("表名不允许为空");
		}
		if (Pattern.matches(regEx, dataModel.getTableName()) == false) {
			throw new IFormException("表名必须以字母开头，只能包含数字，字母，下划线，不能包含中文，横杆等特殊字符");
		}
		if (dataModel.getColumns()==null || dataModel.getColumns().size()==0) {
			throw new IFormException("至少包含一个字段");
		}
		for (ColumnModel column:dataModel.getColumns()) {
			if (StringUtils.isEmpty(column.getColumnName())) {
				throw new IFormException("字段名称不允许为空");
			}
			if (Pattern.matches(regEx, column.getColumnName()) == false) {
				throw new IFormException("字段名称必须以字母开头，只能包含数字，字母，下划线，不能包含中文，横杆等特殊字符");
			}
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
		if (dataModel.getColumns()==null || dataModel.getColumns().size()==0) {
			throw new IFormException("至少包含一个字段");
		}
		if (dataModel.getColumns()==null || dataModel.getColumns().size()==0) {
			throw new IFormException("至少包含一个字段");
		}
		for (ColumnModel column:dataModel.getColumns()) {
			if (StringUtils.isEmpty(column.getColumnName())) {
				throw new IFormException("字段名称不允许为空");
			}
			if (Pattern.matches(regEx, column.getColumnName()) == false) {
				throw new IFormException("字段名称必须以字母开头，只能包含数字，字母，下划线，不能包含中文，横杆等特殊字符");
			}
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

	private Page<DataModel> toDTO(Page<DataModelEntity> entities) {
		Page<DataModel> dataModels = Page.get(entities.getPage(), entities.getPagesize());
		dataModels.data(entities.getTotalCount(), toDTO(entities.getResults()));
		return dataModels;
	}

	private List<DataModel> toDTO(List<DataModelEntity> entities) {
		List<DataModel> dataModels = new ArrayList<DataModel>();
		for (DataModelEntity entity : entities) {
			dataModels.add(toDTO(entity));
		}
		return dataModels;
	}

	private List<DataModel> toSimpleDTO(List<DataModelEntity> entities) {
		List<DataModel> dataModels = new ArrayList<DataModel>();
		for (DataModelEntity entity : entities) {
			dataModels.add(toSimpleDataModelDTO(entity));
		}
		return dataModels;
	}

	private DataModel toDTO(DataModelEntity entity) {
		DataModel dataModel = toDataModelDTO(entity);

		if (entity.getColumns() != null && !entity.getColumns().isEmpty()) {
			List<ColumnModel> columns = new ArrayList<ColumnModel>();
			for (ColumnModelEntity columnEntity : entity.getColumns()) {
				ColumnModel column = toDTO(columnEntity);
				columns.add(column);
			}
			dataModel.setColumns(columns);
		}

		if (entity.getIndexes() != null && !entity.getIndexes().isEmpty()) {
			List<IndexModel> indexes = new ArrayList<IndexModel>();
			for (IndexModelEntity indexEntity : entity.getIndexes()) {
				IndexModel index = toDTO(indexEntity);
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
			BeanUtils.copyProperties(entity.getMasterModel(), masterModel, new String[]{"masterModel","slaverModels","columns", "indexes","referencesDataModel"});
			dataModel.setMasterModel(masterModel);
		}

		if(entity.getSlaverModels() != null){
			List<DataModelInfo> slaverModels = new ArrayList<>();
			for(DataModelEntity dataModelEntity : entity.getSlaverModels()) {
				DataModelInfo dataModelInfo = new DataModelInfo();
				BeanUtils.copyProperties(dataModelEntity, dataModelInfo, new String[]{"masterModel","slaverModels","columns", "indexes","referencesDataModel"});
				slaverModels.add(dataModelInfo);
			}
			dataModel.setSlaverModels(slaverModels);
		}
		return dataModel;
	}

	private DataModel toSimpleDataModelDTO(DataModelEntity entity)  {
		DataModel dataModel = new DataModel();
		BeanUtils.copyProperties(entity, dataModel, new String[] {"masterModel","slaverModels","columns", "indexes","referencesDataModel"});
		dataModel.setSynchronized(entity.getSynchronized());

		if(entity.getMasterModel() != null){
			DataModelInfo masterModel = new DataModelInfo();
			BeanUtils.copyProperties(entity.getMasterModel(), masterModel, new String[]{"masterModel","slaverModels","columns", "indexes","referencesDataModel"});
			dataModel.setMasterModel(masterModel);
		}
		return dataModel;
	}

	private ColumnModel toDTO(ColumnModelEntity entity)  {
		ColumnModel columnModel = new ColumnModel();
		BeanUtils.copyProperties(entity, columnModel, new String[] {"dataModel","columnReferences"});
		columnModel.setReferenceTables(columnModelService.getReferenceModel(entity));
		return columnModel;
	}

	private IndexModel toDTO(IndexModelEntity entity) {
		IndexModel indexModel = new IndexModel();
		BeanUtils.copyProperties(entity, indexModel, new String[] {"dataModel","columns"});
		return indexModel;
	}
}
