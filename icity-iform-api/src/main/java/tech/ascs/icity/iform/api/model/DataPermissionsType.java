package tech.ascs.icity.iform.api.model;

public enum DataPermissionsType {

	/** 全部可见 */
	AllPeople("AllPeople"),

	/** 仅可见我创建的 */
	MySelf("MySelf");

	private String value;

	private DataPermissionsType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}