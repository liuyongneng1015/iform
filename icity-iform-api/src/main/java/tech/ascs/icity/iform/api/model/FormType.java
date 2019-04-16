package tech.ascs.icity.iform.api.model;

import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * 表单类型
 * 
 * @author Jackie
 *
 */
public enum FormType implements Serializable {

	General("General"),//普通类型

	Function("Function");//功能表单

	private String value;//

	private FormType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static FormType getByType(String type){
		if(!StringUtils.hasText(type)){
			return null;
		}
		for(FormType formType : FormType.values()){
			if(formType.getValue().equals(type)){
				return formType;
			}
		}
		return null;
	}

}
