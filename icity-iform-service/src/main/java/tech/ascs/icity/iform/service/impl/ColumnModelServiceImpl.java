package tech.ascs.icity.iform.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import tech.ascs.icity.iform.api.model.ColumnType;
import tech.ascs.icity.iform.api.model.IndexType;
import tech.ascs.icity.iform.api.model.ReferenceModel;
import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.iform.model.*;
import tech.ascs.icity.iform.service.ColumnModelService;
import tech.ascs.icity.iform.service.DataModelService;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnModelServiceImpl extends DefaultJPAService<ColumnModelEntity> implements ColumnModelService {

    private JPAManager<ColumnReferenceEntity> columnReferenceManager;

    private JPAManager<ColumnModelEntity> columnModelManager;

    private JPAManager<IndexModelEntity> indexModelManager;

    private JPAManager<ItemModelEntity> itemModeManager;

    public ColumnModelServiceImpl() {
        super(ColumnModelEntity.class);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DataModelService dataModelService;

    @Override
    protected void initManager() {
        super.initManager();
        columnReferenceManager = getJPAManagerFactory().getJPAManager(ColumnReferenceEntity.class);
        columnModelManager = getJPAManagerFactory().getJPAManager(ColumnModelEntity.class);
        indexModelManager = getJPAManagerFactory().getJPAManager(IndexModelEntity.class);
        itemModeManager = getJPAManagerFactory().getJPAManager(ItemModelEntity.class);
    }

    @Override
    public ColumnModelEntity saveColumnModelEntity(DataModelEntity dataModel, String columnName) {
        List<ColumnModelEntity> list = dataModel.getColumns();
        if (list != null) {
            for (ColumnModelEntity columnModelEntity : list) {
                if (columnModelEntity.getColumnName().equals(columnName)) {
                    return columnModelEntity;
                }
            }
        }
        return saveColumns(dataModel, columnName);
    }

    private ColumnModelEntity saveColumns(DataModelEntity dataModel, String columnName) {
        ColumnModelEntity columnModelEntity = new ColumnModelEntity();
        columnModelEntity.setDataModel(dataModel);
        columnModelEntity.setColumnName(columnName);
        columnModelEntity.setDataType(ColumnType.String);
        columnModelEntity.setLength(255);
        columnModelEntity.setPrecision(255);
        columnModelEntity.setNotNull(false);
        if ("id".equals(columnName)) {
            columnModelEntity.setDescription("主键（自动生成无法删改）");
            columnModelEntity.setName("主键id");
            columnModelEntity.setLength(32);
            columnModelEntity.setPrecision(32);
            columnModelEntity.setNotNull(true);
        } else if ("master_id".equals(columnName)) {
            columnModelEntity.setDescription("关联字段");
            columnModelEntity.setName("关联字段id");
        } else if ("create_at".equals(columnName)) {
            columnModelEntity.setDescription("创建时间");
            columnModelEntity.setName("创建时间");
            columnModelEntity.setDataType(ColumnType.Timestamp);
            columnModelEntity.setLength(0);
            columnModelEntity.setPrecision(0);
        } else if ("update_at".equals(columnName)) {
            columnModelEntity.setDescription("更新时间");
            columnModelEntity.setName("更新时间");
            columnModelEntity.setDataType(ColumnType.Timestamp);
            columnModelEntity.setLength(0);
            columnModelEntity.setPrecision(0);
        } else if ("create_by".equals(columnName)) {
            columnModelEntity.setDescription("创建人id");
            columnModelEntity.setName("创建人");
        } else if ("update_by".equals(columnName)) {
            columnModelEntity.setDescription("更新人id");
            columnModelEntity.setName("更新人");
        } else{
            columnModelEntity.setDescription("其他");
            columnModelEntity.setName("其他");
        }
        columnModelEntity.setScale(null);
        columnModelEntity.setKey(true);
        columnModelEntity.setDefaultValue(null);
        dataModel.getColumns().add(columnModelEntity);
        return columnModelEntity;
    }


    //删除旧的关联关系
    @Override
    public void deleteOldColumnReferenceEntity(ColumnModelEntity columnEntity, List<String> deleteOldToColumnIds, List<ColumnReferenceEntity> oldReferenceEntityList) {
        //删除旧的关联
        List<ColumnModelEntity> toColumnModelEntityList = columnModelManager.query().filterIn("id", deleteOldToColumnIds).list();
        //删除正向关联的关系
        for (int i = 0; i < oldReferenceEntityList.size(); i++) {
            ColumnReferenceEntity referenceEntity = oldReferenceEntityList.get(i);
            if (deleteOldToColumnIds.contains(referenceEntity.getToColumn().getId())) {
                if(referenceEntity.getReferenceType() == ReferenceType.ManyToMany){
                    deleteTable(referenceEntity.getReferenceMiddleTableName()+"_list");
                }
                oldReferenceEntityList.remove(referenceEntity);
                i--;
                columnReferenceManager.delete(referenceEntity);
            }
        }
        //删除反向关联的关系
        for (ColumnModelEntity toEntity : toColumnModelEntityList) {
            List<ColumnReferenceEntity> toReferenceEntities = toEntity.getColumnReferences();
            for (int i = 0; i < toReferenceEntities.size(); i++) {
                ColumnReferenceEntity reference = toReferenceEntities.get(i);
                if (StringUtils.equals(reference.getToColumn().getId(), columnEntity.getId())) {
                    toReferenceEntities.remove(reference);
                    i--;
                    columnReferenceManager.delete(reference);
                }
            }
        }
    }

    @Override
    public void saveColumnReferenceEntity(ColumnModelEntity fromColumnEntity, ColumnModelEntity toColumnEntity, ReferenceType referenceType, String referenceMiddleTableName) {
        //关联关系
        List<ColumnReferenceEntity> columnReferenceEntityList = toColumnEntity.getColumnReferences();
        for (ColumnReferenceEntity referenceEntity : columnReferenceEntityList) {
            if (referenceEntity.getToColumn().getId().equals(fromColumnEntity.getId()) && referenceType == ReferenceType.getReverseReferenceType(referenceType)) {
                return;
            }
        }

        //正向关联
        ColumnReferenceEntity columnReferenceEntity = new ColumnReferenceEntity();
        columnReferenceEntity.setFromColumn(fromColumnEntity);
        columnReferenceEntity.setToColumn(toColumnEntity);
        columnReferenceEntity.setReferenceType(referenceType);
        if (StringUtils.isNotEmpty(referenceMiddleTableName)) {
            columnReferenceEntity.setReferenceMiddleTableName(referenceMiddleTableName);
        }
        fromColumnEntity.getColumnReferences().add(columnReferenceEntity);

        //反向关联
        ColumnReferenceEntity reverseColumnReferenceEntity = new ColumnReferenceEntity();
        reverseColumnReferenceEntity.setFromColumn(toColumnEntity);
        reverseColumnReferenceEntity.setToColumn(fromColumnEntity);
        reverseColumnReferenceEntity.setReferenceType(ReferenceType.getReverseReferenceType(referenceType));
        if (StringUtils.isNotEmpty(referenceMiddleTableName)) {
            reverseColumnReferenceEntity.setReferenceMiddleTableName(referenceMiddleTableName);
        }
        columnReferenceEntityList.add(reverseColumnReferenceEntity);

    }

    @Override
    public void deleteColumnReferenceEntity(ColumnReferenceEntity columnReferenceEntity) {
        ColumnModelEntity fromColumn = columnReferenceEntity.getFromColumn();
        ColumnModelEntity toColumn = columnReferenceEntity.getToColumn();
        for (int i = 0; i < toColumn.getColumnReferences().size(); i++) {
            ColumnReferenceEntity columnReferenceEntity1 = toColumn.getColumnReferences().get(i);
            ColumnModelEntity toColumn1 = columnReferenceEntity1.getToColumn();
            if (toColumn1.getId().equals(fromColumn.getId())) {
                toColumn.getColumnReferences().remove(columnReferenceEntity1);
                i--;
                columnReferenceManager.delete(columnReferenceEntity1);
            }
        }
        columnModelManager.save(toColumn);
        columnReferenceManager.delete(columnReferenceEntity);
    }

    @Override
    public List<ReferenceModel> getReferenceModel(ColumnModelEntity entity) {
        List<ReferenceModel> list = new ArrayList<>();
        if (entity.getColumnReferences() != null) {
            for (ColumnReferenceEntity columnReferenceEntity : entity.getColumnReferences()) {
                if (columnReferenceEntity.getToColumn() == null || columnReferenceEntity.getToColumn().getDataModel() == null) {
                    continue;
                }
                ReferenceModel referenceModel = new ReferenceModel();
                referenceModel.setReferenceTable(columnReferenceEntity.getToColumn().getDataModel().getTableName());
                referenceModel.setReferenceType(columnReferenceEntity.getReferenceType());
                referenceModel.setReferenceValueColumn(columnReferenceEntity.getToColumn().getColumnName());
                referenceModel.setId(columnReferenceEntity.getId());
                referenceModel.setName(columnReferenceEntity.getName());
                referenceModel.setReferenceMiddleTableName(columnReferenceEntity.getReferenceMiddleTableName());
                list.add(referenceModel);
            }
        }
        return list;
    }

    @Override
    public void deleteTableColumn(String tableName, String columnName) {
        if("sys_user".equals(tableName)){
            return;
        }
        String columnSql = "select COLUMN_NAME from information_schema.COLUMNS where table_name = '" + tableName + "'";
        try {
            List<String> colummList = jdbcTemplate.queryForList(columnSql, String.class);
            if (colummList.contains(columnName)) {
                deleteTableColumnIndex(tableName, columnName);
                try {
                    String deleteColumnSql = "ALTER TABLE " + tableName + " DROP " + columnName;
                    jdbcTemplate.execute(deleteColumnSql);
                } catch (DataAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch( DataAccessException e) {
             e.printStackTrace();
        }
    }

    @Override
    public void updateTableColumn(String tableName, String oldColumnName, String newColumnName) {
        String columnSql = "select COLUMN_NAME from information_schema.COLUMNS where table_name = '" + tableName + "'";
        try {
            List<String> colummList = jdbcTemplate.queryForList(columnSql, String.class);
            if (colummList.contains(oldColumnName)) {
                try {
                    String updateColumnSql = "ALTER TABLE "+tableName+" RENAME "+oldColumnName+" TO "+newColumnName;
                    jdbcTemplate.execute(updateColumnSql);
                } catch (DataAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch( DataAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
	public void deleteTable(String tableName) {
        if("sys_user".equals(tableName)){
            return;
        }
		try {
			String deleteTableSql ="DROP TABLE IF exists "+tableName;
			jdbcTemplate.execute(deleteTableSql);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteTableColumnIndex(String tableName, String columnName) {
        if("sys_user".equals(tableName)){
            return;
        }
		/* mysql删除索引
		String indexSql = "show index from "+tableName;
		List<Map<String, Object>> indexList = listIndexBySql(indexSql);
        for(Map<String, Object> map : indexList){
            if(columnName.equals(map.get("Column_name")) && map.get("Key_name") != null){
                try {
                    if(!"PRIMARY".equals(map.get("Key_name"))) {
                        String deleteIndexSql = "alter table  " + tableName + " drop index " + map.get("Key_name");
                        jdbcTemplate.execute(deleteIndexSql);
                    }
                } catch (DataAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        String foreignIndexSql =" select  CONSTRAINT_NAME, COLUMN_NAME   from INFORMATION_SCHEMA.KEY_COLUMN_USAGE  where TABLE_NAME = '"+tableName+"' AND REFERENCED_TABLE_NAME is not null";
        List<Map<String, Object>> foreignIndexList = listForeginIndexBySql(foreignIndexSql);
        for(Map<String, Object> map : foreignIndexList) {
            if (columnName.equals(map.get("COLUMN_NAME"))) {
                try {
                    String deleteForeignIndexSql = "alter table " + tableName + " drop foreign key  " + map.get("CONSTRAINT_NAME");
                    jdbcTemplate.execute(deleteForeignIndexSql);
                } catch (DataAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        */
	}

    @Override
    public void updateColumnModelEntityIndex(ColumnModelEntity columnModelEntity) {
        List<String> idlist = jdbcTemplate.queryForList("select i.index_info  from ifm_index_column as i  where i.column_model='"+columnModelEntity.getId()+"'", String.class);
        if(idlist == null || idlist.size() < 1){
            return;
        }
        List<IndexModelEntity> list = indexModelManager.query().filterIn("id", idlist).list();
        DataModelEntity dataModelEntity = columnModelEntity.getDataModel();
        String tableName = dataModelEntity.getPrefix() == null ? dataModelEntity.getTableName(): dataModelEntity.getPrefix()+dataModelEntity.getTableName();
        List<String> indexNameList = dataModelService.listDataIndexName(tableName);
        if(list != null && list.size() > 0){
            for(IndexModelEntity indexModelEntity : list){
                if(indexNameList.contains(indexModelEntity.getName())) {
                    deleteTableIndex(tableName, indexModelEntity.getName());
                }
                indexModelEntity.getColumns().remove(columnModelEntity);
                indexModelManager.save(indexModelEntity);
            }
        }
    }

    @Override
    public void deleteTableIndex(String tableName, String indexName) {
        if("sys_user".equals(tableName)){
            return;
        }
        try {
            //mysql:"alter table  " + tableName + " drop index " + indexName;
            String deleteIndexSql = " drop index " + indexName;
           jdbcTemplate.execute(deleteIndexSql);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTableIndex(String tableName, IndexModelEntity index) {
        try {
            if(index.getColumns() == null || index.getColumns().size() < 1){
                return;
            }
            String indexSql = null;
            StringBuffer sub = new StringBuffer();
            for(ColumnModelEntity columnModelEntity : index.getColumns()){
                List<ItemModelEntity> itemModelEntityList = itemModeManager.query().filterIn("columnModel.id", columnModelEntity.getId()).list();
                if(itemModelEntityList == null || itemModelEntityList.size() < 1 || !(itemModelEntityList.get(0) instanceof ReferenceItemModelEntity)) {
                    ColumnModelEntity column = columnModelEntity;
                    String columnName = column.getPrefix() == null ? column.getColumnName() : column.getPrefix()+column.getColumnName();
                    sub.append("," + columnName);
                }else{
                    sub.append("," + columnModelEntity.getColumnName());
                }
            }
            String str = sub.toString().substring(1);
            if(index.getIndexType() == IndexType.Unique) {
                indexSql = " CREATE UNIQUE INDEX "+ index.getName() +" on "+tableName+" (" + str+")";
            }else{
                indexSql = " CREATE INDEX "+ index.getName() +" on "+tableName+" (" + str+")";
            }
            jdbcTemplate.execute(indexSql);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, Object>> listIndexBySql(String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = jdbcTemplate.query(sql, new DataRowMapper());
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return list;
	}

    private List<Map<String, Object>> listForeginIndexBySql(String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = jdbcTemplate.query(sql, new ForeginDataRowMapper());
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return list;
    }

	class DataRowMapper implements RowMapper{

		@Override
		//实现mapRow方法
		public Map<String, Object> mapRow(ResultSet rs, int num) throws SQLException {
			//对类进行封装
			Map<String, Object> map = new HashMap<>();
			map.put("Column_name",rs.getString("Column_name"));
			map.put("Key_name", rs.getString("Key_name"));
			return map;
		}
	}

    class ForeginDataRowMapper implements RowMapper{

        @Override
        //实现mapRow方法
        public Map<String, Object> mapRow(ResultSet rs, int num) throws SQLException {
            //对类进行封装
            Map<String, Object> map = new HashMap<>();
            map.put("CONSTRAINT_NAME",rs.getString("CONSTRAINT_NAME"));
            map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
            return map;
        }
    }
}
