package tech.ascs.icity.iform.model;

import javax.persistence.*;

/**
 * 地图控件
 */
@Entity
@Table(name = "ifm_map_item_model")
@DiscriminatorValue("mapItemModel")
public class MapItemModelEntity extends ItemModelEntity  {

	private static final long serialVersionUID = 1L;

	@Column(name="longitude")//级度
	private String longitude;

	@Column(name="latitude")//纬度
	private String latitude;

	@Column(name="map_desc")//描述
	private String mapDesc;

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