package tech.ascs.icity.iform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.admin.api.model.ListFormIds;
import tech.ascs.icity.admin.client.ResourceService;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.FormInstanceService;
import tech.ascs.icity.iform.service.FormModelService;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.service.ListModelService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ItemModelServiceImpl extends DefaultJPAService<ItemModelEntity> implements ItemModelService {


	private JPAManager<ReferenceItemModelEntity> referenceItemModelEntityManager;


	@Autowired
	private FormModelService formModelService;


	public ItemModelServiceImpl() {
		super(ItemModelEntity.class);
	}

	@Override
	protected void initManager() {
		super.initManager();
		referenceItemModelEntityManager = getJPAManagerFactory().getJPAManager(ReferenceItemModelEntity.class);
	}


	@Override
	public List<ReferenceItemModelEntity> findRefenceItemByFormModelId(String formModelId) {
		return referenceItemModelEntityManager.query().filterEqual("referenceFormId", formModelId).list();
	}

	@Override
	public ItemModelEntity saveItemModelEntity(FormModelEntity formModelEntity, String itemModelName) {
		List<ItemModelEntity> list = formModelService.findAllItems(formModelEntity);
		if (list != null) {
			for (ItemModelEntity itemModelEntity : list) {
				if(itemModelEntity.getName() == null){
					continue;
				}
				if (itemModelEntity.getName().equals(itemModelName)) {
					return itemModelEntity;
				}
			}
		}
		return saveItem(formModelEntity, itemModelName);
	}

	private ItemModelEntity saveItem(FormModelEntity formModelEntity, String itemModelName) {
		ItemModelEntity itemModelEntity = new ItemModelEntity();
		itemModelEntity.setName(itemModelName);
		itemModelEntity.setFormModel(formModelEntity);
		itemModelEntity.setType(ItemType.Input);
		itemModelEntity.setSystemItemType(SystemItemType.Input);
		itemModelEntity.setUuid(UUID.randomUUID().toString().replace("-",""));
		if("流程状态".equals(itemModelName)){
			List<ItemSelectOption> options = new ArrayList<>();
			ItemSelectOption itemSelectOptionA = new ItemSelectOption();
			itemSelectOptionA.setItemModel(itemModelEntity);
			itemSelectOptionA.setValue("ALL");
			itemSelectOptionA.setLabel("全部");
			ItemSelectOption itemSelectOptionW = new ItemSelectOption();
			itemSelectOptionW.setItemModel(itemModelEntity);
			itemSelectOptionW.setValue("WORK");
			itemSelectOptionW.setLabel("待处理");
			ItemSelectOption itemSelectOptionD = new ItemSelectOption();
			itemSelectOptionD.setItemModel(itemModelEntity);
			itemSelectOptionD.setValue("DONE");
			itemSelectOptionD.setLabel("已处理");
			options.add(itemSelectOptionA);
			options.add(itemSelectOptionW);
			options.add(itemSelectOptionD);
			itemModelEntity.setOptions(options);
		}

		formModelEntity.getItems().add(itemModelEntity);
		return itemModelEntity;
	}
}
