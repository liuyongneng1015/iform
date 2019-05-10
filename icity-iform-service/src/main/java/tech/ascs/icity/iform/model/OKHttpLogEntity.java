package tech.ascs.icity.iform.model;

import io.swagger.annotations.ApiModelProperty;
import tech.ascs.icity.iform.api.model.BusinessTriggerType;
import tech.ascs.icity.iform.api.model.DataSourceType;
import tech.ascs.icity.jpa.dao.model.BaseEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 请求外部接口日志
 */
@Entity
@Table(name = "ifm_ok_http_log")
public class OKHttpLogEntity extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@JoinColumn(name="url")//请求url
	private String url;

	@Column(name="parameter", length = 4096)//请求json参数
	private String parameter;

	@Column(name="result_code")//结果编码
	private int resultCode;

	@Column(name="result", length = 4096)//返回结果
	private String result;

	//来源
	@Column(name = "from_source", length = 64)
	private String fromSource;

	//表单实例id
	@Column(name = "from_instance_id", length = 64)
	private String formInstanceId;

	//来源对象类型
	@JoinColumn(name="source_type")
	@Enumerated(EnumType.STRING)
	private DataSourceType sourceType = DataSourceType.FormModel;

	//业务触发类型
	@JoinColumn(name="trigger_type")
	@Enumerated(EnumType.STRING)
	private BusinessTriggerType triggerType;


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getFromSource() {
		return fromSource;
	}

	public void setFromSource(String fromSource) {
		this.fromSource = fromSource;
	}

	public String getFormInstanceId() {
		return formInstanceId;
	}

	public void setFormInstanceId(String formInstanceId) {
		this.formInstanceId = formInstanceId;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public DataSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(DataSourceType sourceType) {
		this.sourceType = sourceType;
	}

	public BusinessTriggerType getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(BusinessTriggerType triggerType) {
		this.triggerType = triggerType;
	}
}