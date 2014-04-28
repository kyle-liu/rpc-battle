
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.taobao.rpc_battle.rpc_udp.UdpSender;
import com.taobao.rpc_battle.rpc_udp.UdpSenderPool;





/**
 * @author xiaodu
 *
 * 下午1:43:40
 */
public class ReflectionProxyBuilder extends ProxyBuilder{

	@Override
	public <T> Proxy<T> buildProxy(Class<T> iface, MethodEntry[] entries) {
		for(MethodEntry e : entries) {
			Method method = e.getMethod();
			int mod = method.getModifiers();
			if(!Modifier.isPublic(mod)) {
				method.setAccessible(true);
			}
		}

		Map<Method, MethodEntry> entryMap = new HashMap<Method, MethodEntry>();
		for(int i=0; i < entries.length; i++) {
			MethodEntry e = entries[i];
			
			entryMap.put(e.getMethod(), e);
		}

		return new ReflectionProxy<T>(iface, entryMap);
	}
	

	public class ReflectionProxy<T> implements Proxy<T> {
		private Class<T> iface;
		private Map<Method, MethodEntry> entryMap;

		public ReflectionProxy(Class<T> iface, Map<Method, MethodEntry> entryMap) {
			this.iface = iface;
			this.entryMap = entryMap;
		}

		public T newProxyInstance(String ip) {
			ReflectionHandler handler = new ReflectionHandler(ip, entryMap);
			return (T)java.lang.reflect.Proxy.newProxyInstance(
					iface.getClassLoader(), new Class[] { iface }, handler);
		}
	}
	
	
	public class ReflectionHandler implements InvocationHandler {
		private Map<Method, MethodEntry> entryMap;

		private String ip;
		public ReflectionHandler( String ip,Map<Method, MethodEntry> entryMap) {
			this.entryMap = entryMap;
			this.ip = ip;
		}

		private  ThreadLocal<UdpSender> mapThread = new ThreadLocal<UdpSender>();
		
		public Object invoke(Object proxy, Method method, Object[] args) {
			
			UdpSender upd = mapThread.get();
			if(upd ==null){
				upd = new UdpSender(ip);
				mapThread.set(upd);
			}
			MethodEntry e = entryMap.get(method);
			return upd.call(e.getRpcName(), args);
		}
	}

}
