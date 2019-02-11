package tech.ascs.icity.iform.model;

import javax.persistence.*;

/**
 * 数字表单控件模型
 */
@Entity
@Table(name = "ifm_number_item_model")
@DiscriminatorValue("numberItemModel")
public class NumberItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@JoinColumn(name="decimal_digits")//数字位数
	private Integer decimalDigits;

	@JoinColumn(name="prefix")//千分位分隔符
	private Boolean thousandSeparator;

	@JoinColumn(name="suffix")//后缀单位
	private Integer suffix;

	@Column(name="calculation_formula") // 计算公式
	private String calculationFormula;

	public Integer getDecimalDigits() {
		return decimalDigits;
	}

	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public Boolean getThousandSeparator() {
		return thousandSeparator;
	}

	public void setThousandSeparator(Boolean thousandSeparator) {
		this.thousandSeparator = thousandSeparator;
	}

	public Integer getSuffix() {
		return suffix;
	}

	public void setSuffix(Integer suffix) {
		this.suffix = suffix;
	}

	public String getCalculationFormula() {
		return calculationFormula;
	}

	public void setCalculationFormula(String calculationFormula) {
		this.calculationFormula = calculationFormula;
	}
}