package tech.ascs.icity.iform.model;

import tech.ascs.icity.iform.api.model.GeographicalMapType;
import tech.ascs.icity.iform.api.model.PositionType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 地图
 */
@Entity
@Table(name = "ifm_geographical_map")
public class GeographicalMapEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	//来源控件或者表单id
	@Column(name = "from_source", length = 64)
	private String fromSource;

	@Column(name="longitude")//经度
	private Double lng;

	@Column(name="latitude")//纬度
	private Double lat;

	@Column(name="detail_address")//详细地址
	private String detailAddress;

	@Column(name="landmark")//地标
	private String landmark;

	@Column(name="level")//地图显示级别
	private Integer level = 12;


	public String getFromSource() {
		return fromSource;
	}

	public void setFromSource(String fromSource) {
		this.fromSource = fromSource;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public String getDetailAddress() {
		return detailAddress;
	}

	public void setDetailAddress(String detailAddress) {
		this.detailAddress = detailAddress;
	}

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}