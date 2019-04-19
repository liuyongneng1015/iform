package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * 设备类型
 * 
 * @author Jackie
 *
 */
public enum DeviceType implements Serializable {

	PC("PC"),

	APP("APP"),

	Other("Other");//其他

	private String value;

	private DeviceType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static DeviceType getByType(String type){
		if(!StringUtils.hasText(type)){
			return null;
		}
		for(DeviceType deviceType : DeviceType.values()){
			if(deviceType.equals(type)){
				return deviceType;
			}
		}
		return null;
	}

}
