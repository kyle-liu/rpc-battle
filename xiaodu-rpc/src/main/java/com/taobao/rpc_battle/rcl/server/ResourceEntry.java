package com.taobao.rpc_battle.rcl.server;

import java.io.Serializable;

/**
 * 
 * @author xiaodu
 * @version 2011-7-21 ÉÏÎç11:38:27
 */
public class ResourceEntry implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1586683687083828142L;


	private String resourceName;
	
	private byte[] binaryContent = null;
	
	private int binaryLength = 0;
	
	private transient Class<?> clazz;
	

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public byte[] getBinaryContent() {
		return binaryContent;
	}

	public void setBinaryContent(byte[] binaryContent) {
		this.binaryContent = binaryContent;
	}

	public int getBinaryLength() {
		return binaryLength;
	}

	public void setBinaryLength(int binaryLength) {
		this.binaryLength = binaryLength;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	

}
