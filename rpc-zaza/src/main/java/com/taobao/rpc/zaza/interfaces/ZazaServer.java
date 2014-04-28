package com.taobao.rpc.zaza.interfaces;

public interface ZazaServer {
 
	public void start();
	
	public void stop();
	
	public <T> void register(Class<T> classType, T rpcInstance);
	
}
