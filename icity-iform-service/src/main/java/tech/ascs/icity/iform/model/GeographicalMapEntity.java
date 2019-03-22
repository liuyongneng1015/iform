package tech.ascs.icity.iform.model;

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

	@Column(name="longitude")//级度
	private String longitude;

	@Column(name="latitude")//纬度
	private String latitude;

	@Column(name="map_desc")//地图描述
	private String mapDesc;

	public String getFromSource() {
		return fromSource;
	}

	public void setFromSource(String fromSource) {
		this.fromSource = fromSource;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getMapDesc() {
		return mapDesc;
	}

	public void setMapDesc(String mapDesc) {
		this.mapDesc = mapDesc;
	}
}