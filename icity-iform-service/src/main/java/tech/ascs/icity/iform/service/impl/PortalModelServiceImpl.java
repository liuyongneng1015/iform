package tech.ascs.icity.iform.service.impl;

import com.googlecode.genericdao.search.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import tech.ascs.icity.iform.model.PortalModelEntity;
import tech.ascs.icity.iform.service.PortalModelService;
import tech.ascs.icity.jpa.dao.Query;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.util.Map;
import java.util.Optional;

public class PortalModelServiceImpl extends DefaultJPAService<PortalModelEntity> implements PortalModelService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public PortalModelServiceImpl() {
        super(PortalModelEntity.class);
    }

    @Override
    public Integer maxOrderNo() {
        Map<String, Object> map = jdbcTemplate.queryForMap("select max(order_no) as order_no from ifm_portal_model ");
        if (map != null && map.get("order_no") != null) {
            return Integer.parseInt(String.valueOf(map.get("order_no")));
        }
        return 0;
    }

    @Override
    public void moveModel(String id, String action) {
        PortalModelEntity entity = find(id);
        Assert.isTrue(entity!=null, "该记录不存在或者已删除");
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
}
