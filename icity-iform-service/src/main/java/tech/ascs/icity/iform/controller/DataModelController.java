package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.googlecode.genericdao.search.Filter;

import io.swagger.annotations.Api;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.ColumnModel;
import tech.ascs.icity.iform.api.model.DataModel;
import tech.ascs.icity.iform.api.model.DataModelInfo;
import tech.ascs.icity.iform.api.model.DataModelType;
import tech.ascs.icity.iform.api.model.IndexModel;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.model.IndexModelEntity;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "数据模型服务", description = "包含数据模型的增删改查等功能")
@RestController
public class DataModelController implements tech.ascs.icity.iform.api.service.DataModelService {

	@Autowired
	private DataModelService dataModelService;

	@Override
	public List<DataModel> list(@RequestParam(name="name", required=false) String name, @RequestParam(name = "sync", required=false) String sync) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterOr(Filter.like("name", "%" + name + "%"), Filter.like("tableName", "%" + name + "%"));
			}
			if (StringUtils.hasText(sync)) {
				query.filterEqual("synchronized_", "1".equals(sync));
			}
			List<DataModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public Page<DataModel> page(
			@RequestParam(name="name", required=false) String name,
			@RequestParam(name="sync", required=false) String sync,
			@RequestParam(name="page", defaultValue="1") int page,
			@RequestParam(name="pagesize", defaultValue="10") int pagesize) {
		try {
			Query<DataModelEntity, DataModelEntity> query = dataModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
			}
			Page<DataModelEntity> entities = query.page(page, pagesize).page();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取数据模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public List<DataModelInfo> getMasterModels() {
		try {
			List<DataModelEntity> entities = dataModelService.query().filterNotEqual("modelType", DataModelType.Slaver).list();
			List<DataModelInfo> dataModels = new ArrayList<DataModelInfo>();
			for (DataModelEntity entity : entities) {
				dataModels.add(BeanUtils.copy(entity, DataModelInfo.class));
			}
			return dataModels;
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
		dataModelService.deleteById(id);
	}

	@Override
	public void syncDataModel(@PathVariable(name="id") String id) {
		DataModelEntity entity = dataModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "数据模型【" + id + "】不存在");
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
		DataModel dataModel = BeanUtils.copy(entity, DataModel.class, new String[] {"columns", "indexes"});
		dataModel.setSynchronized(entity.getSynchronized());

		if (entity.getColumns().size() > 0) {
			List<ColumnModel> columns = new ArrayList<ColumnModel>();
			for (ColumnModelEntity columnEntity : entity.getColumns()) {
				ColumnModel column = toDTO(columnEntity);
				column.setDataModel(dataModel);
				columns.add(column);
			}
			dataModel.setColumns(columns);
		}

		if (entity.getIndexes().size() > 0) {
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

	private ColumnModel toDTO(ColumnModelEntity entity) throws InstantiationException, IllegalAccessException {
		return BeanUtils.copy(entity, ColumnModel.class, new String[] {"dataModel"});
	}

	private IndexModel toDTO(IndexModelEntity entity) throws InstantiationException, IllegalAccessException {
		return BeanUtils.copy(entity, IndexModel.class, new String[] {"dataModel"});
	}
}
