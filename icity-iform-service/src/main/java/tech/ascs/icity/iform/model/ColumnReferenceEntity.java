package tech.ascs.icity.iform.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import tech.ascs.icity.jpa.dao.model.JPAEntity;

/**
 * 数据字段模型关联关系
 */
@Entity
@Table(name = "ifm_column_model")
public class ColumnReferenceEntity extends JPAEntity implements Serializable {

	public static enum ReferenceType {
		OneToOne,
		OneToMany,
		ManyToOne,
		ManyToMany
	}

	private static final long serialVersionUID = 1L;

	@ManyToOne
	private ColumnModelEntity fromColumn;

	@ManyToOne
	private ColumnModelEntity toColumn;

	@Enumerated(EnumType.STRING)
	private ReferenceType referenceType;

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
}
