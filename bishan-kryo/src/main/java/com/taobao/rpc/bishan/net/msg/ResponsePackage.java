package com.taobao.rpc.bishan.net.msg;

public class ResponsePackage  extends AbstractPackage{

	private Object responseObj;
	
	@Override
	public int version() {
		return 0;
	}

	private boolean isSuccess;
	private Exception e;
	
	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public Exception getE() {
		return e;
	}

	public void setE(Exception e) {
		this.e = e;
	}

	public Object getResponseObj() {
		return responseObj;
	}

	public void setResponseObj(Object responseObj) {
		this.responseObj = responseObj;
	}

}
