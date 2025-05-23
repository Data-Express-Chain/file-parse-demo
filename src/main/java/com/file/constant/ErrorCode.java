package com.file.constant;

public enum ErrorCode {
	SUCCESS(0, "success"),

	//解析失败，比如流水改版，需要修改代码恢复
	FILE_PARSE_EXCEPTION(-44001, "file parse exception"),

	JSON_CONVERT_EXCEPTION(-44052, "json convert fail exception"),

	FILE_PARSE_NULL_ALERT_EXCEPTION(-44053, "file parse null alert exception"),

	NO_PROVINCE_DATA_EXCEPTION(-44120, "no province data exception"),
	;

	/**
	 * error code
	 */
	private int code;

	/**
	 * error message
	 */
	private String msg;

	/**
	 * Error Code Constructor.
	 *
	 * @param code The ErrorCode
	 * @param msg The ErrorCode Description
	 */
	ErrorCode(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	/**
	 * Get the Error Code.
	 *
	 * @return the ErrorCode
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Gets the ErrorCode Description.
	 *
	 * @return the ErrorCode Description
	 */
	public String getMsg() {
		return msg;
	}
}
