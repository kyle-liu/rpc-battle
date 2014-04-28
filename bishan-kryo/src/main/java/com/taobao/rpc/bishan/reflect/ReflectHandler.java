package com.taobao.rpc.bishan.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.taobao.rpc.bishan.net.msg.RequstPackage;

public class ReflectHandler implements InvocationHandler {
	private String ip;
	private final int cs=9;//9个client连接，1K最佳
	private BsServiceClient[] clients=new BsServiceClient[cs];
	private int routIndex=0;
	public ReflectHandler(String ip){
		this.ip=ip;
		if(ip==null){
			return;
		}
		for(int i=0;i<cs;i++){
			clients[i]=new BsServiceClient();
			try {
				clients[i].initConnect(ip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
	throws Throwable {
		RequstPackage request=new RequstPackage(
			proxy.getClass().getInterfaces()[0].getName(),method,args);
		return clients[getIndex()%cs].send(request);
	}
	private int getIndex(){
		int i=routIndex++;
		if(i<0)i=0;
		return i;
	}
}
