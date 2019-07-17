package tech.ascs.icity.iform.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.genericdao.search.Sort;
import io.swagger.annotations.Api;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.ListModel;
import tech.ascs.icity.iform.api.model.PortalItemModel;
import tech.ascs.icity.iform.api.model.PortalModel;
import tech.ascs.icity.iform.model.PortalModelEntity;
import tech.ascs.icity.iform.service.PortalModelService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;
import tech.ascs.icity.utils.BeanUtils;

import java.util.List;

@Api(tags = "门户模型服务", description = "包含门户模型的增删改查等功能")
@RestController
public class PortalModelController implements tech.ascs.icity.iform.api.service.PortalModelService {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private PortalModelService portalModelService;

    @Override
    public List<PortalModel> list(@RequestParam(name = "name", required = false) String name) {
        Query<PortalModelEntity, PortalModelEntity> query = portalModelService.query();
        if (StringUtils.hasText(name)) {
            query.filterLike("name", "%"+name+"%");
        }
        List<PortalModelEntity> list = query.sort(Sort.asc("orderNo")).list();
        return BeanUtils.copyList(list, PortalModel.class, "items");
    }

    @Override
    public Page<PortalModel> page(@RequestParam(name = "name", defaultValue = "") String name,
                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "pagesize", defaultValue = "10") int pagesize) {
        Query<PortalModelEntity, PortalModelEntity> query = portalModelService.query();
        if (StringUtils.hasText(name)) {
            query.filterLike("name", "%"+name+"%");
        }
        Page<PortalModelEntity> pageData = query.sort(Sort.asc("orderNo")).page(page, pagesize).page();
        return BeanUtils.copyPage(pageData, PortalModel.class, "items");
    }

    @Override
    public PortalModel get(@PathVariable(name="id") String id) {
        if (mapper==null) {
            System.out.println("0000000000000000000");
        }
        return null;
    }

    @Override
    public IdEntity createPortalModel(@RequestBody PortalModel portalModel) {


        return null;
    }

    public PortalModelEntity toPortalModelEntity(PortalModel portalModel) {
        Assert.isTrue(StringUtils.hasText(portalModel.getName()), "名称不允许为空");
        List<PortalItemModel> items = portalModel.getItems();
        for (PortalItemModel item:items) {
            if (item.getType()==null) {
                throw new ICityException("有控件的type字段为空");
            }
            switch (item.getType()) {
                case News:
                    break;
                case QuickMenu:
                    break;
                case List:
                    break;
                case Report:
                    break;
                case Notice:
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    public ListModel toListModel(PortalModelEntity entity) {
        try {
            return mapper.readValue(mapper.writeValueAsString(entity), ListModel.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public PortalModel toPortalModel(PortalModelEntity entity) {
        return null;
    }

    @Override
    public void updatePortalModel(@PathVariable(name="id") String id, @RequestBody PortalModel portalModel) {

    }

    @Override
    public void removePortalModel(@PathVariable(name="id") String id) {

    }

    @Override
    public void removePortalModels(@RequestBody List<String> ids) {

    }

    @Override
    public void moveAction(@PathVariable(name="id") String id, @PathVariable(name="action") String action) {

    }
}
