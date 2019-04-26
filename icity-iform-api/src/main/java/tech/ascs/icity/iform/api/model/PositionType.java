package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * 定位类型
 * 
 * @author Jackie
 *
 */
public enum PositionType implements Serializable {

	AUTO("AUTO"),//自动

	HAND("HAND");//手动经纬度

	private String value;//

	private PositionType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static PositionType getByType(String type){
		if(!StringUtils.hasText(type)){
			return null;
		}
		for(PositionType positionType : PositionType.values()){
			if(positionType.getValue().equals(type)){
				return positionType;
			}
		}
		return null;
	}
}
