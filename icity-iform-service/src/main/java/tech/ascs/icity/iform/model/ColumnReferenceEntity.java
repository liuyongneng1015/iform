package tech.ascs.icity.iform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.iform.api.model.ReferenceType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 数据字段模型关联关系
 */
@Entity
@Table(name = "ifm_column_reference")
public class ColumnReferenceEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })//被关联字段
	private ColumnModelEntity fromColumn;

	@ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })//目标
	private ColumnModelEntity toColumn;

	@Enumerated(EnumType.STRING)
	private ReferenceType referenceType;

	private String referenceMiddleTableName;

	@Transient
	private String inverse;

	public ColumnModelEntity getFromColumn() {
		return fromColumn;
	}

	public void setFromColumn(ColumnModelEntity fromColumn) {
		this.fromColumn = fromColumn;
	}

	public ColumnModelEntity getToColumn() {
		return toColumn;
	}

	public void setToColumn(ColumnModelEntity toColumn) {
		this.toColumn = toColumn;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public String getReferenceMiddleTableName() {
		if(StringUtils.isEmpty(referenceMiddleTableName) && this.referenceType == ReferenceType.ManyToMany){
			List<String> list = getTableNames();
			return list.get(0)+"_"+list.get(1);
		}
		return referenceMiddleTableName;
	}

	private List<String> getTableNames(){
		List<String> list = new ArrayList<>();
		list.add(fromColumn.getDataModel().getTableName());
		list.add(toColumn.getDataModel().getTableName());
		Collections.sort(list);
		return list;
	}

	public void setReferenceMiddleTableName(String referenceMiddleTableName) {
		this.referenceMiddleTableName = referenceMiddleTableName;
	}

	public String getInverse() {
		if(StringUtils.isEmpty(referenceMiddleTableName) && this.referenceType == ReferenceType.ManyToMany){
			List<String> list = getTableNames();
			if(list.get(0).equals(fromColumn.getDataModel().getTableName())){
				return "true";
			}else{
				return "false";
			}
		}
		return null;
	}

	public void setInverse(String inverse) {
		this.inverse = inverse;
	}
}
