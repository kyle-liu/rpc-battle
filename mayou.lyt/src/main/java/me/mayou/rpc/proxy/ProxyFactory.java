package me.mayou.rpc.proxy;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

public class ProxyFactory {

	private static final ConcurrentHashMap<Class<?>, Object> clazzMap = new ConcurrentHashMap<Class<?>, Object>();

	public static Object getConsumerProxy(Class<?> clazz, String ip) {
		Object obj = clazzMap.get(clazz);
		if (obj == null) {
			synchronized (clazz) {
				if (clazzMap.contains(clazz)) {
					return clazzMap.get(clazz);
				} else {
					Enhancer enhancer = new Enhancer();
					enhancer.setSuperclass(BaseConsumerProxy.class);
					enhancer.setInterfaces(new Class[] { clazz });
					enhancer.setCallbacks(new Callback[] {
							new MethodInterceptor() {

								@Override
								public Object intercept(Object arg0,
										Method arg1, Object[] arg2,
										MethodProxy arg3) throws Throwable {
									return ((BaseConsumerProxy) arg0)
											.doInterval(arg1
													.getDeclaringClass()
													.getName()
													+ "." + arg1.getName(),
													arg2);
								}

							}, NoOp.INSTANCE });
					enhancer.setCallbackFilter(new CallbackFilter() {

						@Override
						public int accept(Method arg0) {
							if (arg0.getName().equals("doInterval")) {
								return 1;
							} else {
								return 0;
							}
						}

					});
					obj = enhancer.create(new Class[] { String.class },
							new Object[] { ip });
					clazzMap.put(clazz, obj);
					return obj;
				}
			}
		} else {
			return obj;
		}
	}

	public static Object getProviderProxy(Class<?> clazz) {
		return null;
	}

}
