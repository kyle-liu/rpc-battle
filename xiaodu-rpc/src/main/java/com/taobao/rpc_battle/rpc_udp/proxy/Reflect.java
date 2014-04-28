
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.proxy;

import java.util.HashMap;
import java.util.Map;


/**
 * @author xiaodu
 *
 * 下午1:21:49
 */
public class Reflect {

	 private static Map<Class<?>, Proxy<?>> proxyCache = new HashMap<Class<?>, Proxy<?>>();
	 
	 private static ReflectionProxyBuilder proxyBuilder = new ReflectionProxyBuilder();
	 
	 public static synchronized <T> Proxy<T> getProxy(Class<T> iface) {
	        Proxy<?> proxy = proxyCache.get(iface);
	        if (proxy == null) {
	            proxy = proxyBuilder.buildProxy(iface);
	            proxyCache.put(iface, proxy);
	        }
	        return (Proxy<T>) proxy;
	    }

}
