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

	@Column(name="map_type")//地理地图类型
	@Enumerated(EnumType.STRING)
	private GeographicalMapType mapType;

	@Column(name="position_type")//定位类型
	@Enumerated(EnumType.STRING)
	private PositionType positionType;

	@Column(name="longitude")//级度
	private Double longitude;

	@Column(name="latitude")//纬度
	private Double latitude;

	@Column(name="detail_address")//详细地址
	private String detailAddress;

	@Column(name="landmark")//地标
	private String landmark;

	@Column(name="level")//地图显示级别
	private int level = 12;


	public String getFromSource() {
		return fromSource;
	}

	public void setFromSource(String fromSource) {
		this.fromSource = fromSource;
	}

	public GeographicalMapType getMapType() {
		return mapType;
	}

	public void setMapType(GeographicalMapType mapType) {
		this.mapType = mapType;
	}

	public PositionType getPositionType() {
		return positionType;
	}

	public void setPositionType(PositionType positionType) {
		this.positionType = positionType;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
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

	public void setLevel(int level) {
		this.level = level;
	}
}