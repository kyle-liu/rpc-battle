package shenfeng.simplerpc;

import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class RemoteObjectInvocationHandler implements InvocationHandler {

	private final Map<Method, Integer> method2CodeMap;

	private final Serializer serializer;

	private Connection conn;

	public RemoteObjectInvocationHandler(String remoteHost, int remotePort, Class<?> serviceType, Serializer serializer) {
		method2CodeMap = Utils.getMethod2CodeMap(serviceType);
		this.serializer = serializer;
		try {
			conn = new Connection(remoteHost, remotePort);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
		OutputStream out = conn.getOutputStream();
		Serializer outSerializer = serializer.create(out);
		outSerializer.writeObject(Integer.class, method2CodeMap.get(method));
		outSerializer.writeObjects(method.getParameterTypes(), params);
		outSerializer.flush();
		out.flush();

		Class<?> rtype = method.getReturnType();
		if (rtype == void.class) {
			return null;
		}
		Serializer inSerializer = serializer.create(conn.getInputStream());
		return inSerializer.readObject(rtype);
	}

}
