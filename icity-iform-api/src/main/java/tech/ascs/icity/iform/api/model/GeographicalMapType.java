package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * 地图选择类型
 * 
 * @author Jackie
 *
 */
public enum GeographicalMapType implements Serializable {

	MAP_BAIDU("MAP_BAIDU"),//百度地图

	MAP_WORLD("MAP_WORLD");//天地图

	private String value;//

	private GeographicalMapType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static GeographicalMapType getByType(String type){
		if(!StringUtils.hasText(type)){
			return null;
		}
		for(GeographicalMapType mapType : GeographicalMapType.values()){
			if(mapType.getValue().equals(type)){
				return mapType;
			}
		}
		return null;
	}

}
