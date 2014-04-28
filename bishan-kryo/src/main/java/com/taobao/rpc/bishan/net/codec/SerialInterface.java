package com.taobao.rpc.bishan.net.codec;

/**
 * 序列化接口
 * @author bishan.ct
 *
 */
public interface SerialInterface {

	/**
	 * 某些序列化框架
	 */
	public void init();
	
	public byte[] encode(Object obj);
	
	public Object decode(byte[] bts,Class className);	
}
