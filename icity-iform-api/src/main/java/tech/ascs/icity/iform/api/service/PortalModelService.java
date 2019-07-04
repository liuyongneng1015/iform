package tech.ascs.icity.iform.api.service;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import tech.ascs.icity.iform.api.model.PortalModel;
import tech.ascs.icity.model.IdEntity;
import tech.ascs.icity.model.Page;

import java.util.List;

@RestController
@RequestMapping("/portal-models")
public interface PortalModelService {
    /**
     * 获取所有门户模型的简要信息
     * @param name （可选）门户模型
     * @return
     */
    @ApiOperation(value = "获取所有门户模型，全部返回简要信息", position = 1)
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "query", name = "name", value = "门户模型名称", required = false)
    })
    @GetMapping
    List<PortalModel> list(@RequestParam(name = "name", required = false) String name);

    /**
     * 获取门户模型分页数据
     * @param name （可选）列表名称
     * @param page （可选）页码，默认为1
     * @param pagesize （可选）每页记录数，默认为10
     * @return
     */
    @ApiOperation(value = "获取门户模型分页数据，全部返回简要信息", position = 2)
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "query", name = "name", value = "门户模型名称", required = false),
        @ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, defaultValue = "1"),
        @ApiImplicitParam(paramType = "query", name = "pagesize", value = "每页记录数", required = false, defaultValue = "10")
    })
    @GetMapping("/page")
    Page<PortalModel> page(@RequestParam(name = "name", defaultValue = "") String name,
                           @RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "pagesize", defaultValue = "10") int pagesize);

    /**
     * 根据门户模型ID获取列表模型对象
     * @param id 列表模型ID（uuid）
     * @return
     */
    @ApiOperation(value = "根据门户模型ID获取列表模型对象", position = 3)
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", name = "id", value = "门户模型ID", required = true, dataType = "String")
    })
    @GetMapping("/{id}")
    PortalModel get(@PathVariable(name="id") String id);

    /**
     * 新建门户模型
     * @param portalModel 门户模型
     */
    @ApiOperation(value = "新建门户模型", position = 4)
    @PostMapping
    IdEntity createPortalModel(@RequestBody PortalModel portalModel);

    /**
     * 更新门户模型
     * @param id 门户模型ID
     * @param portalModel 门户模型
     */
    @ApiOperation(value = "更新门户模型", position = 5)
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", name = "id", value = "门户模型ID", required = true, dataType = "String")
    })
    @PutMapping("/{id}")
    void updatePortalModel(@PathVariable(name="id") String id, @RequestBody PortalModel portalModel);

    /**
     * 删除门户模型
     * @param id 门户模型ID
     */
    @ApiOperation(value = "删除门户模型", position = 6)
    @ApiImplicitParam(paramType = "path", name = "id", value = "门户模型ID", required = true, dataType = "String")
    @DeleteMapping("/{id}")
    void removePortalModel(@PathVariable(name="id") String id);

    @ApiOperation(value = "批量删除门户模型", position = 7)
    @DeleteMapping("/batch-delete")
    void removePortalModels(@RequestBody List<String> ids);

    @ApiOperation(value = "上下移动门户模型", position = 8)
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", name = "id", value = "门户模型ID", required = true, dataType = "String"),
        @ApiImplicitParam(paramType = "path", name = "action", value = "up表示上移，down表示下移", required = true, dataType = "String")
    })
    @DeleteMapping("/{id}/{action}")
    void moveAction(@PathVariable(name="id") String id, @PathVariable(name="action") String action);
}
