package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		ListModelEntity entity =  new ListModelEntity() ;
		BeanUtils.copyProperties(listModel, entity, new String[] {"masterForm","slaverForms","sortItems", "searchItems","functions","displayItems"});

		if(listModel.getMasterForm() != null && !listModel.getMasterForm().isNew()){
			FormModelEntity formModelEntity = new FormModelEntity();
			BeanUtils.copyProperties(listModel.getMasterForm(), formModelEntity, new String[] {"dataModels","process","items", "permissions","submitChecks"});
			entity.setMasterForm(formModelEntity);
		}

		if(listModel.getSlaverForms() != null) {
			List<FormModelEntity> formModelEntities = new ArrayList<>();
			for (FormModel formModel : listModel.getSlaverForms()){
				FormModelEntity formModelEntity = new FormModelEntity();
				BeanUtils.copyProperties(formModel, formModelEntity, new String[]{"dataModels", "process", "items", "permissions", "submitChecks"});
				formModelEntities.add(formModelEntity);
			}
			entity.setSlaverForms(formModelEntities);
		}

		if(listModel.getDisplayItems() != null) {
			List<ItemModelEntity> itemModelEntities = new ArrayList<>();
			for (ItemModel itemModel : listModel.getDisplayItems()){
				ItemModelEntity itemModelEntity = new ItemModelEntity();
				BeanUtils.copyProperties(itemModel, itemModelEntity, new String[]{"formModel", "columnModel", "activities", "options", "permission","items","parentItem","referenceList"});
				itemModelEntities.add(itemModelEntity);
			}
			entity.setDisplayItems(itemModelEntities);
		}

		if (listModel.getSortItems() != null) {
			List<ListSortItem> sortItems = new ArrayList<ListSortItem>();
			for (SortItem sortItem : listModel.getSortItems()) {
				ListSortItem sortItemEntity = new ListSortItem();
				sortItemEntity.setListModel(entity);
				if(sortItem.getItemModel() != null){
					ItemModelEntity itemModelEntity = new ItemModelEntity();
					BeanUtils.copyProperties(sortItem.getItemModel(), itemModelEntity, new String[]{"formModel", "columnModel", "activities", "options", "permission","items","parentItem","referenceList"});
					sortItemEntity.setItemModel(itemModelEntity);
				}
				sortItemEntity.setAsc(sortItem.isAsc());
				sortItems.add(sortItemEntity);
			}
			entity.setSortItems(sortItems);
		}

		if (listModel.getSearchItems() != null) {
			List<ListSearchItem> searchItems = new ArrayList<ListSearchItem>();
			for (SearchItem searchItem : listModel.getSearchItems()) {
				ListSearchItem searchItemEntity =  new ListSearchItem();
				if(searchItem.getId() != null) {
					ItemModelEntity itemModelEntity = new ItemModelEntity();
					itemModelEntity.setId(searchItem.getId());
					itemModelEntity.setName(searchItem.getName());
					searchItemEntity.setItemModel(itemModelEntity);
				}
				/*if(searchItem.getItemModel() != null){
					ItemModelEntity itemModelEntity = new ItemModelEntity();
					BeanUtils.copyProperties(searchItem.getItemModel(), itemModelEntity, new String[]{"formModel", "columnModel", "activities", "options", "permission","items","parentItem","referenceList"});
					searchItemEntity.setItemModel(itemModelEntity);
				}*/
				searchItemEntity.setListModel(entity);
				if (searchItem.getSearch() == null) {
					throw new IFormException("控件【" + searchItemEntity.getItemModel().getName() + "】未定义搜索属性");
				}
				ItemSearchInfo searchInfo = new ItemSearchInfo();
				BeanUtils.copyProperties(searchItem.getSearch(), searchInfo, new String[]{"defaultValue"});
				if(searchItem.getSearch().getDefaultValue() != null){
					searchInfo.setDefaultValue(org.apache.commons.lang3.StringUtils.join(searchItem.getSearch().getDefaultValue(),","));
				}
				searchItemEntity.setSearch(searchInfo);
				searchItems.add(searchItemEntity);
			}
			entity.setSearchItems(searchItems);
		}
		if (listModel.getFunctions() != null) {
			List<ListFunction> functions = new ArrayList<>();
			for (ListModel.Function function : listModel.getFunctions()) {
				ListFunction listFunction = new ListFunction() ;
				BeanUtils.copyProperties(function, listFunction, new String[]{"listModel"});
				listFunction.setListModel(entity);
				functions.add(listFunction);
			}
			entity.setFunctions(functions);
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
		BeanUtils.copyProperties(entity, listModel, new String[] {"displayItems", "masterForm","sortItems", "functions","searchItems","slaverForms"});

		if(entity.getMasterForm() != null){
			FormModel masterForm = new FormModel();
			BeanUtils.copyProperties(entity.getMasterForm(), masterForm, new String[] {"items","dataModels","permissions","submitChecks"});
			listModel.setMasterForm(masterForm);
		}

		if(entity.getDisplayItems() != null){
			List<ItemModel> list  = new ArrayList<>();
			for(ItemModelEntity itemModelEntity : entity.getDisplayItems()){
				ItemModel itemModel = new ItemModel();
				BeanUtils.copyProperties(itemModelEntity, itemModel, new String[] {"permission", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
				list.add(itemModel);
			}
			listModel.setDisplayItems(list);
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
				BeanUtils.copyProperties(sortItemEntity, sortItem, new String[]{"listModel","itemModel"});
				if(sortItemEntity.getItemModel() != null){
					ItemModel itemModel = new ItemModel();
					BeanUtils.copyProperties(sortItemEntity.getItemModel(), itemModel, new String[] {"permission", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
					sortItem.setItemModel(itemModel);
				}
				sortItems.add(sortItem);
			}
			listModel.setSortItems(sortItems);
		}

		if (entity.getSearchItems().size() > 0) {
			List<SearchItem> searchItems = new ArrayList<SearchItem>();
			for (ListSearchItem searchItemEntity : entity.getSearchItems()) {
				SearchItem searchItem = new SearchItem();
				if(searchItemEntity.getItemModel() != null){
					BeanUtils.copyProperties(searchItemEntity.getItemModel(), searchItem, new String[] {"permission", "items","itemModelList","formModel","dataModel", "columnReferences","referenceTables", "activities","options"});
				}
				if(searchItemEntity.getSearch() != null){
					Search search = new Search();
					BeanUtils.copyProperties(searchItemEntity.getSearch(), search, new String[] {"search","defaultValue"});
					if(StringUtils.hasText(searchItemEntity.getSearch().getDefaultValue())){
						search.setDefaultValue(Arrays.asList(searchItemEntity.getSearch().getDefaultValue().split(",")));
					}
					searchItem.setSearch(search);
				}
				searchItems.add(searchItem);
			}
			listModel.setSearchItems(searchItems);
		}

		return listModel;
	}
}
