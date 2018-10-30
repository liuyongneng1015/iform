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
import tech.ascs.icity.iform.api.model.DataModelInfo;
import tech.ascs.icity.iform.api.model.FormModel;
import tech.ascs.icity.iform.api.model.ItemModel;
import tech.ascs.icity.iform.api.model.ItemModel.ActivityInfo;
import tech.ascs.icity.iform.api.model.ItemModel.Option;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.utils.CommonUtils;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

@Api(tags = "表单模型服务", description = "包含表单模型的增删改查等功能")
@RestController
public class FormModelController implements tech.ascs.icity.iform.api.service.FormModelService {

	@Autowired
	private FormModelService formModelService;

	@Override
	public List<FormModel> list(@RequestParam(name="name", defaultValue="") String name) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", "%" + name + "%");
			}
			List<FormModelEntity> entities = query.list();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public Page<FormModel> page(@RequestParam(name="name", defaultValue="") String name, @RequestParam(name="page", defaultValue="1") int page, @RequestParam(name="pagesize", defaultValue="10") int pagesize) {
		try {
			Query<FormModelEntity, FormModelEntity> query = formModelService.query();
			if (StringUtils.hasText(name)) {
				query.filterLike("name", CommonUtils.convertParamOfFuzzySearch(name));
			}
			Page<FormModelEntity> entities = query.page(page, pagesize).page();
			return toDTO(entities);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public FormModel get(@PathVariable(name="id") String id) {
		FormModelEntity entity = formModelService.find(id);
		if (entity == null) {
			throw new IFormException(404, "表单模型【" + id + "】不存在");
		}
		try {
			return toDTO(entity);
		} catch (Exception e) {
			throw new IFormException("获取表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public IdEntity createFormModel(@RequestBody FormModel formModel) {
		if (StringUtils.hasText(formModel.getId())) {
			throw new IFormException("表单模型ID不为空，请使用更新操作");
		}
		try {
			FormModelEntity entity = wrap(formModel);
			entity = formModelService.save(entity);
			return new IdEntity(entity.getId());
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void updateFormModel(@PathVariable(name = "id") String id, @RequestBody FormModel formModel) {
		if (!StringUtils.hasText(formModel.getId()) || !id.equals(formModel.getId())) {
			throw new IFormException("表单模型ID不一致");
		}
		try {
			FormModelEntity entity = wrap(formModel);
			formModelService.save(entity);
		} catch (Exception e) {
			throw new IFormException("保存表单模型列表失败：" + e.getMessage(), e);
		}
	}

	@Override
	public void removeFormModel(@PathVariable(name="id") String id) {
		formModelService.deleteById(id);
	}

	private FormModelEntity wrap(FormModel formModel) throws InstantiationException, IllegalAccessException {
		FormModelEntity entity = BeanUtils.copy(formModel, FormModelEntity.class, new String[] {"items"});

		List<ItemModelEntity> items = new ArrayList<ItemModelEntity>();
		for (ItemModel itemModel : formModel.getItems()) {
			ItemModelEntity itemModelEntity = wrap(itemModel);
			itemModelEntity.setFormModel(entity);
			items.add(itemModelEntity);
		}
		entity.setItems(items);

		return entity;
	}

	private ItemModelEntity wrap(ItemModel itemModel) throws InstantiationException, IllegalAccessException {
		ItemModelEntity entity = BeanUtils.copy(itemModel, ItemModelEntity.class, new String[] {"activities"});

		
		List<ItemActivityInfo> activities = new ArrayList<ItemActivityInfo>();
		if (itemModel.getActivities() != null) {
			for (ActivityInfo activityInfo : itemModel.getActivities()) {
				ItemActivityInfo activityInfoEntity = wrap(activityInfo);
				activityInfoEntity.setItemModel(entity);
				activities.add(activityInfoEntity);
			}
		}
		entity.setActivities(activities);

		if (itemModel.getOptions() != null) {
			for (ItemSelectOption option : entity.getOptions()) {
				option.setItemModel(entity);
			}
		}
		
		return entity;
	}

	private ItemActivityInfo wrap(ActivityInfo activityInfo) {
		ItemActivityInfo activityInfoEntity = new ItemActivityInfo();
		activityInfoEntity.setActivityId(activityInfo.getId());
		activityInfoEntity.setActivityName(activityInfo.getName());
		activityInfoEntity.setVisible(activityInfo.isVisible());
		activityInfoEntity.setReadonly(activityInfo.isReadonly());
		
		return activityInfoEntity;
	}

	private Page<FormModel> toDTO(Page<FormModelEntity> entities) throws InstantiationException, IllegalAccessException {
		Page<FormModel> formModels = Page.get(entities.getPage(), entities.getPagesize());
		formModels.data(entities.getTotalCount(), toDTO(entities.getResults()));
		return formModels;
	}

	private List<FormModel> toDTO(List<FormModelEntity> entities) throws InstantiationException, IllegalAccessException {
		List<FormModel> formModels = new ArrayList<FormModel>();
		for (FormModelEntity entity : entities) {
			formModels.add(toDTO(entity));
		}
		return formModels;
	}

	private FormModel toDTO(FormModelEntity entity) throws InstantiationException, IllegalAccessException {
		FormModel formModel = BeanUtils.copy(entity, FormModel.class, new String[] {"items"});
		if (entity.getItems().size() > 0) {
			List<ItemModel> items = new ArrayList<ItemModel>();
			for (ItemModelEntity itemModelEntity : entity.getItems()) {
				items.add(toDTO(itemModelEntity));
			}
			formModel.setItems(items);
		}
		return formModel;
	}

	private ItemModel toDTO(ItemModelEntity entity) throws InstantiationException, IllegalAccessException {
		ItemModel itemModel = BeanUtils.copy(entity, ItemModel.class, new String[] {"activities", "options"});

		if (entity.getActivities().size() > 0) {
			List<ActivityInfo> activities = new ArrayList<ActivityInfo>();
			for (ItemActivityInfo activityEntity : entity.getActivities()) {
				activities.add(toDTO(activityEntity));
			}
			itemModel.setActivities(activities);
		}

		if (entity.getOptions().size() > 0) {
			List<Option> options = new ArrayList<Option>();
			for (ItemSelectOption optionEntity : entity.getOptions()) {
				options.add(new Option(optionEntity.getLabel(), optionEntity.getValue()));
			}
			itemModel.setOptions(options);
		}
		return itemModel;
	}

	private ActivityInfo toDTO(ItemActivityInfo entity) {
		ActivityInfo activityInfo = new ActivityInfo();
		activityInfo.setId(entity.getActivityId());
		activityInfo.setName(entity.getActivityName());
		activityInfo.setVisible(entity.isVisible());
		activityInfo.setReadonly(entity.isReadonly());
		
		return activityInfo;		
	}
}
