
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rcl;

import java.io.Serializable;

/**
 * @author xiaodu
 *
 * ÏÂÎç3:01:48
 */
public class ClassEntry implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6376420971200051288L;

	private String className;
	
	private byte[] classContent;
	
	private int contentLen;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public byte[] getClassContent() {
		return classContent;
	}

	public void setClassContent(byte[] classContent) {
		this.classContent = classContent;
	}

	public int getContentLen() {
		return contentLen;
	}

	public void setContentLen(int contentLen) {
		this.contentLen = contentLen;
	}
	
	
	

}
