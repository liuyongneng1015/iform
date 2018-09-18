package tech.ascs.icity.iform.api.model;

import java.util.Set;

public class WidgetStruct {
	
	private String type;
	private Set<String> propSet;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Set<String> getPropSet() {
		return propSet;
	}
	public void setPropSet(Set<String> propSet) {
		this.propSet = propSet;
	}
	
}
