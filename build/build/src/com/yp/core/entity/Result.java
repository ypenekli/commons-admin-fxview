package com.yp.core.entity;

public class Result<T> implements IResult<T> {

	private static final long serialVersionUID = -24905431532897925L;

	protected boolean success;

	protected String message, errorcode;

	protected T data;

	public Result() {
		success = false;
		message = "";
	}

	public Result(boolean pSuccess, String pMessage) {
		success = pSuccess;
		message = pMessage;
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

	@Override
	public void setSuccess(boolean pSuccess) {
		success = pSuccess;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public void setMessage(String pMessage) {
		message = pMessage;
	}

	@Override
	public T getData() {
		return data;
	}

	@Override
	public void setData(T pData) {
		data = pData;
	}

	@Override
	public String getErrorcode() {
		return errorcode;
	}

	@Override
	public void setErrorcode(String pErrorcode) {
		errorcode = pErrorcode;
	}
}
