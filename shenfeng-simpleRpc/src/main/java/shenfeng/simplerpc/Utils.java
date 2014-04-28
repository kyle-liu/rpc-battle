package shenfeng.simplerpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;

public class Utils {

	public static int getCode(Method method) {
		Adler32 ader = new Adler32();
		ader.update(method.toGenericString().getBytes());
		return (int) ader.getValue();
	}

	public static Map<Integer, Method> getCode2methodMap(Class<?> cl) {
		Map<Integer, Method> result = new HashMap<Integer, Method>();

		do {
			for (Class<?> intf : cl.getInterfaces()) {
				for (Method method : intf.getMethods()) {
					result.put(getCode(method), method);
				}
			}
			cl = cl.getSuperclass();
		} while (cl != null);
		return result;
	}

	public static Map<Method, Integer> getMethod2CodeMap(Class<?> cl) {
		Map<Method, Integer> result = new HashMap<Method, Integer>();

		if (cl.isInterface()) {
			for (Method method : cl.getMethods()) {
				result.put(method, getCode(method));
			}
		}

		do {
			for (Class<?> intf : cl.getInterfaces()) {
				for (Method method : intf.getMethods()) {
					result.put(method, getCode(method));
				}
			}
			cl = cl.getSuperclass();
		} while (cl != null);
		return result;
	}

	public static Map<Method, Class<?>[]> getMethod2ParamsType(Class<? extends Object> cl) {
		Map<Method, Class<?>[]> result = new HashMap<Method, Class<?>[]>();

		if (cl.isInterface()) {
			for (Method method : cl.getMethods()) {
				result.put(method, method.getParameterTypes());
			}
		}

		do {
			for (Class<?> intf : cl.getInterfaces()) {
				for (Method method : intf.getMethods()) {
					result.put(method, method.getParameterTypes());
				}
			}
			cl = cl.getSuperclass();
		} while (cl != null);
		return result;
	}

}
