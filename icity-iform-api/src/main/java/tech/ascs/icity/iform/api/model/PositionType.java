package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * 定位摘取方式类型
 * 
 * @author Jackie
 *
 */
public enum PositionType implements Serializable {

	Drag("Drag"),//拖拽获取

	Not_Drag("Not_Drag");//不允许拖拽获取

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
