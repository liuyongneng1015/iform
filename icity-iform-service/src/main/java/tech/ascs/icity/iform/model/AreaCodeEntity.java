package tech.ascs.icity.iform.model;

import tech.ascs.icity.jpa.dao.model.BaseEntity;
import tech.ascs.icity.model.Codeable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 行政区划的字典表
 */
@Entity
@Table(name = "ifm_area_code",
	   indexes = {@Index(name="ifm_area_code_name_index", columnList = "name", unique=false),
		          @Index(name="ifm_area_code_code_index", columnList="code", unique=false),
			      @Index(name="ifm_area_code_order_no_index", columnList="order_no", unique=false)})
public class AreaCodeEntity extends BaseEntity implements Codeable {

	private static final long serialVersionUID = 1L;

	@Column(name = "code")
	private String code;

	/**
	 * 排序号
	 */
	@Column(name = "order_no")
	private Integer orderNo = 0;

	/**
	 * 父对象
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name = "parent_id")
	private AreaCodeEntity parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<AreaCodeEntity> children = new ArrayList<>();

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getOrderNo() {
		if(orderNo == null){
			orderNo = 0;
		}
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public AreaCodeEntity getParent() {
		return parent;
	}

	public void setParent(AreaCodeEntity parent) {
		this.parent = parent;
	}

	public List<AreaCodeEntity> getChildren() {
		return children;
	}

	public void setChildren(List<AreaCodeEntity> children) {
		this.children = children;
	}
}