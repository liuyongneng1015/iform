package tech.ascs.icity.iform.controller;

class EntityUtil {

	public static tech.ascs.icity.iform.model.TabInfo toTabInfoEntity(
			tech.ascs.icity.iform.api.model.TabInfo source) {
		tech.ascs.icity.iform.model.TabInfo target = new tech.ascs.icity.iform.model.TabInfo();

		target.setId(source.getId());
		target.setTabName(source.getTabName());
		target.setTabNameDesc(source.getTabNameDesc());
		target.setTableType(source.getTableType());
		target.setMasterTable(source.getMasterTable());
		target.setRemark(source.getRemark());
		target.setSynTime(source.getSynTime());
		target.setSynFlag(source.getSynFlag());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());
		// target.setColumnDatas(source.getColumnDatas());

		return target;
	}

	public static tech.ascs.icity.iform.api.model.TabInfo toTabInfoResponse(
			tech.ascs.icity.iform.model.TabInfo source) {
		tech.ascs.icity.iform.api.model.TabInfo target = new tech.ascs.icity.iform.api.model.TabInfo();

		target.setId(source.getId());
		target.setTabName(source.getTabName());
		target.setTabNameDesc(source.getTabNameDesc());
		target.setTableType(source.getTableType());
		target.setMasterTable(source.getMasterTable());
		target.setRemark(source.getRemark());
		target.setSynTime(source.getSynTime());
		target.setSynFlag(source.getSynFlag());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());
		// target.setColumnDatas(source.getColumnDatas());

		return target;
	}

	public static tech.ascs.icity.iform.model.ColumnData toColumnDataEntity(
			tech.ascs.icity.iform.api.model.ColumnData source) {
		tech.ascs.icity.iform.model.ColumnData target = new tech.ascs.icity.iform.model.ColumnData();

		target.setId(source.getId());
		target.setTabInfoId(source.getTabInfoId());
		target.setTabName(source.getTabName());
		target.setColName(source.getColName());
		target.setColNameDesc(source.getColNameDesc());
		target.setType(source.getType());
		target.setLength(source.getLength());
		target.setDecimalLen(source.getDecimalLen());
		target.setNotNull(source.getNotNull());
		target.setKeyFlag(source.getKeyFlag());
		target.setDefaultValue(source.getDefaultValue());
		target.setRemark(source.getRemark());
		target.setForeignKey(source.getForeignKey());
		target.setForeignTab(source.getForeignTab());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.api.model.ColumnData toColumnDataResponse(
			tech.ascs.icity.iform.model.ColumnData source) {
		tech.ascs.icity.iform.api.model.ColumnData target = new tech.ascs.icity.iform.api.model.ColumnData();

		target.setId(source.getId());
		target.setTabInfoId(source.getTabInfoId());
		target.setTabName(source.getTabName());
		target.setColName(source.getColName());
		target.setColNameDesc(source.getColNameDesc());
		target.setType(source.getType());
		target.setLength(source.getLength());
		target.setDecimalLen(source.getDecimalLen());
		target.setNotNull(source.getNotNull());
		target.setKeyFlag(source.getKeyFlag());
		target.setDefaultValue(source.getDefaultValue());
		target.setRemark(source.getRemark());
		target.setForeignKey(source.getForeignKey());
		target.setForeignTab(source.getForeignTab());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.model.SynLog toSynLogEntity(
			tech.ascs.icity.iform.api.model.SynLog source) {
		tech.ascs.icity.iform.model.SynLog target = new tech.ascs.icity.iform.model.SynLog();

		target.setId(source.getId());
		target.setTabInfoId(source.getTabInfoId());
		target.setTabName(source.getTabName());
		target.setSqlType(source.getSqlType());
		target.setRemark(source.getRemark());
		target.setSynBy(source.getSynBy());
		target.setSynTime(source.getSynTime());

		return target;
	}

	public static tech.ascs.icity.iform.model.IndexInfo toIndexInfoEntity(
			tech.ascs.icity.iform.api.model.IndexInfo source) {
		tech.ascs.icity.iform.model.IndexInfo target = new tech.ascs.icity.iform.model.IndexInfo();

		target.setId(source.getId());
		target.setTabName(source.getTabName());
		target.setIndexName(source.getIndexName());
		target.setIndexColumns(source.getIndexColumns());
		target.setType(source.getType());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.api.model.IndexInfo toIndexInfoResponse(
			tech.ascs.icity.iform.model.IndexInfo source) {
		tech.ascs.icity.iform.api.model.IndexInfo target = new tech.ascs.icity.iform.api.model.IndexInfo();

		target.setId(source.getId());
		target.setTabName(source.getTabName());
		target.setIndexName(source.getIndexName());
		target.setIndexColumns(source.getIndexColumns());
		target.setType(source.getType());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}
}
