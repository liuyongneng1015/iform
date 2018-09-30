package tech.ascs.icity.iform.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.iform.api.model.ListModel.SortItem;
import tech.ascs.icity.iform.api.model.SearchItem;
import tech.ascs.icity.iform.api.model.SearchItem.Search;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.iform.model.ItemSearchInfo;
import tech.ascs.icity.iform.model.ListFunction;
import tech.ascs.icity.iform.model.ListModelEntity;
import tech.ascs.icity.iform.model.ListSearchItem;
import tech.ascs.icity.iform.model.ListSortItem;
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

	@Override
	public List<ListModel> list(@RequestParam(name="name", defaultValue="") String name) {
		try {
			Query<ListModelEntity, ListModelEntity> query = listModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
			}
			List<ListModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取列表模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public Page<ListModel> page(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name="page", defaultValue="1") int page, @RequestParam(name="pagesize", defaultValue="10") int pagesize) {
		try {
			Query<ListModelEntity, ListModelEntity> query = listModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
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

	private ListModelEntity wrap(ListModel listModel) throws InstantiationException, IllegalAccessException {
		ListModelEntity entity = BeanUtils.copy(listModel, ListModelEntity.class, new String[] {"sortItems", "searchItems"});

		List<ListSortItem> sortItems = new ArrayList<ListSortItem>();
		if (listModel.getSortItems() != null) {
			for (SortItem sortItem : listModel.getSortItems()) {
				ListSortItem sortItemEntity = new ListSortItem();
				sortItemEntity.setItemModel(createItemModelEntity(sortItem.getId()));
				sortItemEntity.setAsc(sortItem.isAsc());
				sortItemEntity.setListModel(entity);
				sortItems.add(sortItemEntity);
			}
		}
		entity.setSortItems(sortItems);

		List<ListSearchItem> searchItems = new ArrayList<ListSearchItem>();
		if (listModel.getSearchItems() != null) {
			for (SearchItem searchItem : listModel.getSearchItems()) {
				ListSearchItem searchItemEntity = new ListSearchItem();
				searchItemEntity.setItemModel(createItemModelEntity(searchItem.getId()));
				if (searchItem.getSearch() == null) {
					throw new IFormException("控件【" + searchItemEntity.getItemModel().getName() + "】未定义搜索属性");
				}
				searchItemEntity.setSearch(BeanUtils.copy(searchItem.getSearch(), ItemSearchInfo.class));
				searchItemEntity.setListModel(entity);
				searchItems.add(searchItemEntity);
			}
		}
		entity.setSearchItems(searchItems);
		
		for (ListFunction function : entity.getFunctions()) {
			function.setListModel(entity);
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

	private ListModel toDTO(ListModelEntity entity) throws InstantiationException, IllegalAccessException {
		ListModel listModel = BeanUtils.copy(entity, ListModel.class, new String[] {"sortItems", "searchItems"});

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
				SearchItem searchItem = BeanUtils.copy(searchItemEntity.getItemModel(), SearchItem.class, new String[] {"columnModel", "activities"});
				searchItem.setSearch(BeanUtils.copy(searchItemEntity.getSearch(), Search.class));
				searchItems.add(searchItem);
			}
			listModel.setSearchItems(searchItems);
		}

		return listModel;
	}
}
