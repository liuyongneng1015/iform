package tech.ascs.icity.iform;

import tech.ascs.icity.ICityException;

public class IFormException extends ICityException {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_BIZ_CODE = 300;

	public IFormException(String message) {
		super(400, DEFAULT_BIZ_CODE, message);
	}

	public IFormException(String message, Throwable cause) {
		super(400, DEFAULT_BIZ_CODE, message, cause);
	}

	public IFormException(int httpCode, String message) {
		super(httpCode, DEFAULT_BIZ_CODE, message);
	}

	public IFormException(int httpCode, String message, Throwable cause) {
		super(httpCode, DEFAULT_BIZ_CODE, message, cause);
	}

	public IFormException(int httpCode, int bizCode, String message) {
		super(httpCode, bizCode, message);
	}

	public IFormException(int httpCode, int bizCode, String message, Throwable cause) {
		super(httpCode, bizCode, message, cause);
	}
}
