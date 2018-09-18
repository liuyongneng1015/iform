package tech.ascs.icity.iform.controller;

import java.util.List;

import tech.ascs.icity.ICityException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

	public static tech.ascs.icity.iform.model.Widget toWidgetEntity(
			tech.ascs.icity.iform.api.model.Widget source) {
		tech.ascs.icity.iform.model.Widget target = new tech.ascs.icity.iform.model.Widget();

		// target.setId(source.getId());
		target.setFieldName(source.getFieldName());
		target.setMeaning(source.getMeaning());
		target.setType(source.getType());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.api.model.Widget toWidgetResponse(
			tech.ascs.icity.iform.model.Widget source) {
		tech.ascs.icity.iform.api.model.Widget target = new tech.ascs.icity.iform.api.model.Widget();

		target.setId(source.getId());
		target.setFieldName(source.getFieldName());
		target.setMeaning(source.getMeaning());
		target.setType(source.getType());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.model.FormWidget toFormWidgetEntity(
			tech.ascs.icity.iform.api.model.FormWidget source) {
		tech.ascs.icity.iform.model.FormWidget target = new tech.ascs.icity.iform.model.FormWidget();

		target.setId(source.getId());
		target.setFormId(source.getFormId());
		target.setTabName(source.getTabName());
		target.setColName(source.getColName());
		target.setVisible(source.getVisible());
		target.setDisabledFlag(source.getDisabledFlag());
		target.setReadonlyFlag(source.getReadonlyFlag());
		target.setRequired(source.getRequired());
		target.setWidgetId(source.getWidgetId());
		target.setName(source.getName());
		target.setType(source.getType());
		target.setValue(source.getValue());
		target.setDefaultPara(source.getDefaultPara());
		target.setDefaultValue(source.getDefaultValue());
		target.setLabel(source.getLabel());
		target.setPlaceholder(source.getPlaceholder());
		target.setTipText(source.getTipText());
		target.setSize(source.getSize());
		target.setMaxlength(source.getMaxlength());
		target.setPassWord(source.getPassWord());
		target.setCols(source.getCols());
		target.setRows(source.getRows());
		target.setMultiple(source.getMultiple());
		target.setLabelsValues(source.getLabelsValues());
		target.setDefaultName(source.getDefaultName());
		target.setUrl(source.getUrl());
		target.setChildItem(source.getChildItem());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.api.model.FormWidget toFormWidgetResponse(
			tech.ascs.icity.iform.model.FormWidget source) {
		tech.ascs.icity.iform.api.model.FormWidget target = new tech.ascs.icity.iform.api.model.FormWidget();

		target.setId(source.getId());
		target.setFormId(source.getFormId());
		target.setTabName(source.getTabName());
		target.setColName(source.getColName());
		target.setVisible(source.getVisible());
		target.setDisabledFlag(source.getDisabledFlag());
		target.setReadonlyFlag(source.getReadonlyFlag());
		target.setRequired(source.getRequired());
		target.setWidgetId(source.getWidgetId());
		target.setName(source.getName());
		target.setType(source.getType());
		target.setValue(source.getValue());
		target.setDefaultPara(source.getDefaultPara());
		target.setDefaultValue(source.getDefaultValue());
		target.setLabel(source.getLabel());
		target.setPlaceholder(source.getPlaceholder());
		target.setTipText(source.getTipText());
		target.setSize(source.getSize());
		target.setMaxlength(source.getMaxlength());
		target.setPassWord(source.getPassWord());
		target.setCols(source.getCols());
		target.setRows(source.getRows());
		target.setMultiple(source.getMultiple());
		target.setLabelsValues(source.getLabelsValues());
		target.setDefaultName(source.getDefaultName());
		target.setUrl(source.getUrl());
		target.setChildItem(source.getChildItem());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.model.Form toFormEntity(
			tech.ascs.icity.iform.api.model.Form source) {
		tech.ascs.icity.iform.model.Form target = new tech.ascs.icity.iform.model.Form();

		target.setId(source.getId());
		target.setTabNameList(source.getTabNameList());
		target.setCategory(source.getCategory());
		target.setStatus(source.getStatus());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());
		target.setName(source.getName());
		target.setJsonData(source.getJsonData());

		return target;
	}

	public static tech.ascs.icity.iform.api.model.Form toFormResponse(
			tech.ascs.icity.iform.model.Form source) {
		tech.ascs.icity.iform.api.model.Form target = new tech.ascs.icity.iform.api.model.Form();

		target.setId(source.getId());
		target.setTabNameList(source.getTabNameList());
		target.setCategory(source.getCategory());
		target.setStatus(source.getStatus());
		target.setRemark(source.getRemark());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());
		target.setName(source.getName());
		target.setJsonData(source.getJsonData());

		return target;
	}

	public static tech.ascs.icity.iform.model.ListData toListDataEntity(
			tech.ascs.icity.iform.api.model.ListData source) {
		tech.ascs.icity.iform.model.ListData target = new tech.ascs.icity.iform.model.ListData();

		target.setId(source.getId());
		target.setFormId(source.getFormId());
		target.setFormName(source.getFormName());
		target.setName(source.getName());
		target.setMaster(source.getMaster());
		target.setSlaver(source.getSlaver());
		target.setRemark(source.getRemark());
		target.setOrderColumn(source.getOrderColumn());
		target.setOrderType(source.getOrderType());
		target.setBatchFlag(source.getBatchFlag());
		// target.setFn(source.getFn());
		// target.setSearch(source.getSearch());
		// target.setColList(source.getColList());

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			String colList = mapper.writeValueAsString(source.getColList());
			target.setColList(colList);

			target.setSearch(mapper.writeValueAsString(source.getSearch()));
			target.setFn(mapper.writeValueAsString(source.getFn()));

		} catch (JsonProcessingException e) {
			throw new ICityException("json数据格式有误!");
		}

		target.setShowColumn(source.getShowColumn());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	@SuppressWarnings("unchecked")
	public static tech.ascs.icity.iform.api.model.ListData toListDataResponse(
			tech.ascs.icity.iform.model.ListData source) {
		tech.ascs.icity.iform.api.model.ListData target = new tech.ascs.icity.iform.api.model.ListData();

		target.setId(source.getId());
		target.setFormId(source.getFormId());
		target.setFormName(source.getFormName());
		target.setName(source.getName());
		target.setMaster(source.getMaster());
		target.setSlaver(source.getSlaver());
		target.setRemark(source.getRemark());
		target.setOrderColumn(source.getOrderColumn());
		target.setOrderType(source.getOrderType());
		target.setBatchFlag(source.getBatchFlag());
		// target.setFn(source.getFn());
		// target.setSearch(source.getSearch());
		// target.setColList(source.getColList());

		try {
			ObjectMapper mapper = new ObjectMapper();
			if (source.getColList() == null || "".equals(source.getColList())) {
				target.setColList(null);
			} else {
				// mapper.readTree(file)
				target.setColList(mapper.readValue(source.getColList(),
						List.class));
			}

			if (source.getFn() == null || "".equals(source.getFn())) {
				target.setFn(null);
			} else {
				target.setFn(mapper.readValue(source.getFn(), List.class));
			}

			if (source.getSearch() == null || "".equals(source.getSearch())) {
				target.setSearch(null);
			} else {
				target.setSearch(mapper.readValue(source.getSearch(),
						List.class));
			}

		} catch (Exception e) {
			throw new ICityException("json数据格式有误!");
		}
		target.setShowColumn(source.getShowColumn());
		target.setCreateBy(source.getCreateBy());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	public static tech.ascs.icity.iform.model.ListColData toListColDataEntity(
			tech.ascs.icity.iform.api.model.ListColData source) {
		tech.ascs.icity.iform.model.ListColData target = new tech.ascs.icity.iform.model.ListColData();

		target.setId(source.getId());
		target.setListDataId(source.getListDataId());
		target.setTabName(source.getTabName());
		target.setColName(source.getColName());
		target.setColNameDesc(source.getColNameDesc());
		target.setColAsName(source.getColAsName());
		target.setListFlag(source.getListFlag());
		target.setHiddenFlag(source.getHiddenFlag());
		target.setDetailFlag(source.getDetailFlag());
		target.setQueryFlag(source.getQueryFlag());
		target.setWidth(source.getWidth());
		target.setType(source.getType());
		target.setDefaultPara(source.getDefaultPara());
		target.setDefaultValue(source.getDefaultValue());
		target.setLabelsValues(source.getLabelsValues());
		target.setMultiple(source.getMultiple());
		target.setLabel(source.getLabel());
		target.setPlaceholder(source.getPlaceholder());
		target.setTipText(source.getTipText());
		target.setRemark(source.getRemark());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());
		target.setName(source.getName());
		target.setShowOrder(source.getShowOrder());
		target.setOrderNo(source.getOrderNo());
		target.setOrderType(source.getOrderType());
		target.setQuickFlag(source.getQuickFlag());

		return target;
	}

	public static tech.ascs.icity.iform.api.model.ListColData toListColDataResponse(
			tech.ascs.icity.iform.model.ListColData source) {
		tech.ascs.icity.iform.api.model.ListColData target = new tech.ascs.icity.iform.api.model.ListColData();

		target.setId(source.getId());
		target.setListDataId(source.getListDataId());
		target.setTabName(source.getTabName());
		target.setColName(source.getColName());
		target.setColNameDesc(source.getColNameDesc());
		target.setColAsName(source.getColAsName());
		target.setListFlag(source.getListFlag());
		target.setHiddenFlag(source.getHiddenFlag());
		target.setDetailFlag(source.getDetailFlag());
		target.setQueryFlag(source.getQueryFlag());
		target.setWidth(source.getWidth());
		target.setType(source.getType());
		target.setDefaultPara(source.getDefaultPara());
		target.setDefaultValue(source.getDefaultValue());
		target.setLabelsValues(source.getLabelsValues());
		target.setMultiple(source.getMultiple());
		target.setLabel(source.getLabel());
		target.setPlaceholder(source.getPlaceholder());
		target.setTipText(source.getTipText());
		target.setRemark(source.getRemark());
		target.setUpdateBy(source.getUpdateBy());
		target.setUpdateTime(source.getUpdateTime());
		target.setName(source.getName());
		target.setShowOrder(source.getShowOrder());
		target.setOrderNo(source.getOrderNo());
		target.setOrderType(source.getOrderType());
		target.setQuickFlag(source.getQuickFlag());

		return target;
	}

}
