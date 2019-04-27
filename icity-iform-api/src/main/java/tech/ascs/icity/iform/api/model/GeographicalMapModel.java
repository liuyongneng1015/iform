package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import tech.ascs.icity.model.NameEntity;

@ApiModel("地图信息")
public class GeographicalMapModel extends NameEntity {

	@ApiModelProperty(value = "来源控件id", position = 5)
	private String fromSource;

	@ApiModelProperty(value = "地理地图类型", position = 6)
	private GeographicalMapType mapType;

	@ApiModelProperty(value = "定位类型", position = 7)
	private PositionType positionType;


	@ApiModelProperty(value = "经度", position = 8)
	private Double longitude;

	@ApiModelProperty(value = "纬度", position = 9)
	private Double latitude;

	@ApiModelProperty(value = "详细地址", position = 10)
	private String detailAddress;

	@ApiModelProperty(value = "地标", position = 11)
	private String landmark;

	@ApiModelProperty(value = "地图显示级别", position = 12)
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String getId() {
		String id = super.getId();
		if(StringUtils.isBlank(id)){
			return null;
		}
		return id;
	}
}
