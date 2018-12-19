package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.FormModel;
import tech.ascs.icity.iform.api.model.ItemModel;
import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.iform.api.model.ListModel.SortItem;
import tech.ascs.icity.iform.api.model.SearchItem;
import tech.ascs.icity.iform.api.model.SearchItem.Search;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.service.ListModelService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "列表模型服务", description = "包含列表模型的增删改查等功能")
@RestController
public class ListModelController implements tech.ascs.icity.iform.api.service.ListModelService {

	@Autowired
	private ListModelService listModelService;

	@Autowired
	private FormModelService formModelService ;

	@Autowired
	private ItemModelService itemModelService ;

	@Override
	public List<ListModel> list(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<ListModelEntity, ListModelEntity> query = listModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId", applicationId);
			}
			List<ListModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public Page<ListModel> page(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name="page", defaultValue="1") int page,
								@RequestParam(name="pagesize", defaultValue="10") int pagesize, @RequestParam(name = "applicationId", required = false) String applicationId) {
		try {
			Query<ListModelEntity, ListModelEntity> query = listModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
			}
			if (StringUtils.hasText(applicationId)) {
				query.filterEqual("applicationId", applicationId);
			}
			Page<ListModelEntity> entities = query.page(page, pagesize).page();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public ListModel get(@PathVariable(name="id") String id) {
		ListModelEntity entity = listModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "列表模型【" + id + "】不存在");
		}
		try {
			return toDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public IdEntity createListModel(@RequestBody ListModel ListModel) {
		if (StringUtils.hasText(ListModel.getId())) {
			throw new IFormException("列表模型ID不为空，请使用更新操作");
		}
		try {
			ListModelEntity entity = wrap(ListModel);
			entity = listModelService.save(entity);
			return new IdEntity(entity.getId());
		} catch (Exception e) {
			throw new IFormException("保存列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void updateListModel(@PathVariable(name = "id") String id, @RequestBody ListModel ListModel) {
		if (!StringUtils.hasText(ListModel.getId()) || !id.equals(ListModel.getId())) {
			throw new IFormException("列表模型ID不一致");
		}
		try {
			ListModelEntity entity = wrap(ListModel);
			listModelService.save(entity);
		} catch (Exception e) {
			throw new IFormException("保存列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void removeListModel(@PathVariable(name="id") String id) {
		listModelService.deleteById(id);
	}

	@Override
	public List<ListModel> findListModelsByTableName(String tableName) {
		return listModelService.findListModelsByTableName(tableName);
	}

	private ListModelEntity wrap(ListModel listModel)  {
		ListModelEntity entity = listModel.isNew() ? new ListModelEntity() : listModelService.get(listModel.getId()) ;
		BeanUtils.copyProperties(listModel, entity, new String[] {"masterForm","slaverForms","sortItems", "searchItems","functions"});

		if(listModel.getMasterForm() != null && !listModel.getMasterForm().isNew()){
			entity.setMasterForm(formModelService.get(listModel.getMasterForm().getId()));
		}

		List<ListSortItem> oldSortItems = entity.getSortItems();
		Map<String, ListSortItem> oldSortMap = new HashMap<>();
		for(ListSortItem sortItem : oldSortItems){
			oldSortMap.put(sortItem.getId(), sortItem);
		}

		List<ListFunction> oldFunctions = entity.getFunctions();
		Map<String, ListFunction> oldFunctionMap = new HashMap<>();
		for(ListFunction function : oldFunctions){
			oldFunctionMap.put(function.getId(), function);
		}

		List<ListSearchItem> oldSearchItems = entity.getSearchItems();
		Map<String, ListSearchItem> oldSearchItemMap = new HashMap<>();
		for(ListSearchItem searchItem : oldSearchItems){
			oldSearchItemMap.put(searchItem.getId(), searchItem);
		}

		if (listModel.getSortItems() != null) {
			List<ListSortItem> sortItems = new ArrayList<ListSortItem>();
			for (SortItem sortItem : listModel.getSortItems()) {
				ListSortItem sortItemEntity = sortItem.isNew() ?  new ListSortItem() : oldSortMap.remove(sortItem.getId());
				sortItemEntity.setListModel(entity);
				sortItemEntity.setItemModel(sortItem.getItemModel() == null || sortItem.getItemModel().isNew() ? null : itemModelService.get(sortItem.getItemModel().getId()));
				sortItemEntity.setAsc(sortItem.isAsc());
				sortItems.add(sortItemEntity);
			}
			entity.setSortItems(sortItems);
		}

		if (listModel.getSearchItems() != null) {
			List<ListSearchItem> searchItems = new ArrayList<ListSearchItem>();
			for (SearchItem searchItem : listModel.getSearchItems()) {
				ListSearchItem searchItemEntity = searchItem.isNew() ? new ListSearchItem() : oldSearchItemMap.remove(searchItem.getId());
				searchItemEntity.setItemModel(searchItem.getItemModel() == null || searchItem.getItemModel().isNew() ? null : itemModelService.get(searchItem.getItemModel().getId()));
				searchItemEntity.setListModel(entity);
				if (searchItem.getSearch() == null) {
					throw new IFormException("控件【" + searchItemEntity.getItemModel().getName() + "】未定义搜索属性");
				}
				ItemSearchInfo searchInfo = new ItemSearchInfo();
				BeanUtils.copyProperties(searchItem.getSearch(), searchInfo);
				searchItemEntity.setSearch(searchInfo);
				searchItems.add(searchItemEntity);
			}
			entity.setSearchItems(searchItems);
		}
		if (listModel.getFunctions() != null) {
			List<ListFunction> functions = new ArrayList<>();
			for (ListModel.Function function : listModel.getFunctions()) {
				ListFunction listFunction = function.isNew() ? new ListFunction() : oldFunctionMap.get(function.getId());
				BeanUtils.copyProperties(function, listFunction, new String[]{"listModel"});
				listFunction.setListModel(entity);
				functions.add(listFunction);
			}
			entity.setFunctions(functions);
		}
		try {
			for(String str : oldSortMap.keySet()){
				listModelService.deleteSort(str);
			}
			for(String str : oldSearchItemMap.keySet()){
				listModelService.deleteSearch(str);
			}
			for(String str : oldFunctionMap.keySet()){
				listModelService.deleteFunction(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entity;
	}

	private ItemModelEntity createItemModelEntity(String id) {
		ItemModelEntity itemModelEntity = new ItemModelEntity();
		itemModelEntity.setId(id);
		return itemModelEntity;
	}

	private Page<ListModel> toDTO(Page<ListModelEntity> entities) throws InstantiationException, IllegalAccessException {
		Page<ListModel> listModels = Page.get(entities.getPage(), entities.getPagesize());
		listModels.data(entities.getTotalCount(), toDTO(entities.getResults()));
		return listModels;
	}

	private List<ListModel> toDTO(List<ListModelEntity> entities) throws InstantiationException, IllegalAccessException {
		List<ListModel> listModels = new ArrayList<ListModel>();
		for (ListModelEntity entity : entities) {
			listModels.add(toDTO(entity));
		}
		return listModels;
	}

	private ListModel toDTO(ListModelEntity entity) {
		ListModel listModel = new ListModel();
		BeanUtils.copyProperties(entity, listModel, new String[] {"masterForm","sortItems", "functions","searchItems","slaverForms"});

		if(entity.getMasterForm() != null){
			FormModel masterForm = new FormModel();
			BeanUtils.copyProperties(entity.getMasterForm(), masterForm, new String[] {"items","dataModels","permissions","submitChecks"});
			listModel.setMasterForm(masterForm);
		}

		if(entity.getSlaverForms() != null){
			List<FormModel> list = new ArrayList<>();
			for(FormModelEntity formModelEntity : entity.getSlaverForms()) {
				FormModel slaverForm = new FormModel();
				BeanUtils.copyProperties(formModelEntity, slaverForm, new String[]{"items", "dataModels", "permissions", "submitChecks"});
				list.add(slaverForm);
			}
			listModel.setSlaverForms(list);
		}

		if(entity.getFunctions() != null){
			List<ListModel.Function> functions = new ArrayList<ListModel.Function>();
			for(ListFunction listFunction : entity.getFunctions()) {
				ListModel.Function function = new ListModel.Function();
				BeanUtils.copyProperties(listFunction, function, new String[]{"listModel"});
				functions.add(function);
			}
			listModel.setFunctions(functions);
		}

		if (entity.getSortItems().size() > 0) {
			List<SortItem> sortItems = new ArrayList<SortItem>();
			for (ListSortItem sortItemEntity: entity.getSortItems()) {
				SortItem sortItem = new SortItem();
				sortItem.setId(sortItemEntity.getItemModel().getId());
				sortItem.setName(sortItemEntity.getItemModel().getName());
				sortItem.setAsc(sortItemEntity.isAsc());
				sortItems.add(sortItem);
			}
			listModel.setSortItems(sortItems);
		}

		if (entity.getSearchItems().size() > 0) {
			List<SearchItem> searchItems = new ArrayList<SearchItem>();
			for (ListSearchItem searchItemEntity : entity.getSearchItems()) {
				SearchItem searchItem = new SearchItem();
				BeanUtils.copyProperties(searchItemEntity.getItemModel(), searchItem, new String[] {"columnModel", "activities"});
				Search search = new Search();
				BeanUtils.copyProperties(searchItemEntity.getSearch(), search);
				searchItem.setSearch(search);
				searchItems.add(searchItem);
			}
			listModel.setSearchItems(searchItems);
		}

		return listModel;
	}
}
