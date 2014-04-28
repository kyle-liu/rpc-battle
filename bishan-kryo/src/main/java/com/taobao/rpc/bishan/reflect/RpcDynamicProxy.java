package com.taobao.rpc.bishan.reflect;

import java.io.IOException;
import java.lang.reflect.Proxy;

import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.bishan.net.reactor.BsFactory;

/**
 * 获得nio代理的对象
 * 
 * @author bishan.ct
 *
 */
public class RpcDynamicProxy {

	private Class clazz;
	private String ip;
	public RpcDynamicProxy(Class clazz,String ip) throws IOException{
		this.clazz=clazz;
		
		this.ip=ip;
		
		BsFactory.initClinet();
	}
	
	public Object getProxyObject() throws Exception {
	
		return Proxy.newProxyInstance(RpcDynamicProxy.class.getClassLoader(),
				new Class[] { clazz }, new ReflectHandler(ip));
	}
}
