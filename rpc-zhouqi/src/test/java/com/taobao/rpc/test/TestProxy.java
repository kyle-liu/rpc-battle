package com.taobao.rpc.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.junit.Test;

public class TestProxy {
	
	int size=10000000;
	
	@Test
	public void testJavaProxy(){
		ProxyHandler handler=new ProxyHandler();
		TestProxyInterface proxy=(TestProxyInterface)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{TestProxyInterface.class},handler );
		long start=System.currentTimeMillis();
		for(int i=0;i<size;i++){
			proxy.get(1);
		}
		long cost=System.currentTimeMillis()-start;
		System.out.println("java proxy:"+cost);
	}
	@Test
	public void testCglibProxy(){
		ProxyHandler handler=new ProxyHandler();
		Enhancer enhancer = new Enhancer(); 
        enhancer.setSuperclass(TestProxyInterface.class); 
        enhancer.setCallback(handler); 
        TestProxyInterface proxy=(TestProxyInterface)enhancer.create(); 
        long start=System.currentTimeMillis();
		for(int i=0;i<size;i++){
			proxy.get(1);
		}
		long cost=System.currentTimeMillis()-start;
		System.out.println("cglib proxy:"+cost);
        
	}
	public class ProxyHandler implements InvocationHandler,MethodInterceptor{

		@Override
		public Object intercept(Object obj, Method method, Object[] args,
				MethodProxy proxy) throws Throwable {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	public static interface TestProxyInterface{
		public Object get(int a);
	}
}
