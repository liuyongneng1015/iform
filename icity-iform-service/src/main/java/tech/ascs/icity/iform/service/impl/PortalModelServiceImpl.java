package tech.ascs.icity.iform.service.impl;

import com.googlecode.genericdao.search.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import tech.ascs.icity.iform.model.PortalModelEntity;
import tech.ascs.icity.iform.service.PortalModelService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.List;
import java.util.Map;

public class PortalModelServiceImpl extends DefaultJPAService<PortalModelEntity> implements PortalModelService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public PortalModelServiceImpl() {
        super(PortalModelEntity.class);
    }

    @Override
    public Integer maxOrderNo() {
        List<Map<String, Object>> mapDataList = jdbcTemplate.queryForList("select max(order_no) as order_no from ifm_portal_model ");
        if (mapDataList == null || mapDataList.size() < 1) {
            return  0;
        }
        Map<String, Object> map = mapDataList.get(0);
        if (map != null && map.get("order_no") != null) {
            return Integer.parseInt(String.valueOf(map.get("order_no")));
        }
        return 0;
    }

    @Override
    public void moveModel(String id, String action) {
        PortalModelEntity entity = find(id);
        Assert.notNull(entity, "该记录不存在或者已删除");
        Query<PortalModelEntity, PortalModelEntity> query = query();
        if ("up".equals(action)) {
            query.filterLessThan("orderNo", entity.getOrderNo()).sort(Sort.desc("orderNo"));
        } else {
            query.filterGreaterThan("orderNo", entity.getOrderNo()).sort(Sort.asc("orderNo"));
        }
        PortalModelEntity another = query.page(1,1).first();
        if (another!=null) {
            Integer orderNo = entity.getOrderNo();
            entity.setOrderNo(another.getOrderNo());
            another.setOrderNo(orderNo);
            save(entity);
            save(another);
        }
    }

    @Override
    public PortalModelEntity save(PortalModelEntity entity) {
        if (entity.isNew()) {

        } else {

        }
        return entity;
    }
}
