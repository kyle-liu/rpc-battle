package com.taobao.rpc.bishan.net.msg;

public abstract class AbstractPackage {

	private int id;
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public abstract int version();
	
}
