package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.ascs.icity.iform.api.model.PortalModel;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import java.util.List;

@Api(tags = "门户模型服务", description = "包含门户模型的增删改查等功能")
@RestController
public class PortalModelController implements tech.ascs.icity.iform.api.service.PortalModelService {
    @Override
    public List<PortalModel> list(@RequestParam(name = "name", required = false) String name) {
        return null;
    }

    @Override
    public Page<PortalModel> page(@RequestParam(name = "name", defaultValue = "") String name,
                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "pagesize", defaultValue = "10") int pagesize) {
        return null;
    }

    @Override
    public PortalModel get(@PathVariable(name="id") String id) {
        return null;
    }

    @Override
    public IdEntity createPortalModel(@RequestBody PortalModel portalModel) {
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
