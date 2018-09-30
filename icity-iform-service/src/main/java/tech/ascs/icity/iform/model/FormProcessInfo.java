package tech.ascs.icity.iform.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 表单绑定流程信息
 */
@Embeddable
public class FormProcessInfo {

	@Column(name="process_id")
	private String id;

	@Column(name="process_name")
	private String name;

	@Column(name="process_key")
	private String key;

	@Column(name="process_start_activity")
	private String startActivity;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getStartActivity() {
		return startActivity;
	}

	public void setStartActivity(String startActivity) {
		this.startActivity = startActivity;
	}
}