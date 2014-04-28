package shenfeng.simplerpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Map;

public class RemoteObjectInvocationHandler implements InvocationHandler {

	private final String remoteHost;
	private final int remotePort;

	private final LinkedList<Connection> freeConnections = new LinkedList<Connection>();

	private final Map<Method, Integer> method2CodeMap;

	private final Serializer serializer;

	public RemoteObjectInvocationHandler(String remoteHost, int remotePort, Class<?> serviceType, Serializer serializer) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		method2CodeMap = Utils.getMethod2CodeMap(serviceType);
		this.serializer = serializer;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
		Connection connection = null;
		try {
			connection = getConnection();
			SocketChannel channel = connection.getChannel();
			ByteBuffer buffer = connection.getBuffer();
			buffer.clear();
			buffer.position(4);
			buffer.putInt(method2CodeMap.get(method));
			serializer.writeObjects(new ByteBufferBackedOutputStream(buffer), method.getParameterTypes(), params);
			buffer.putInt(0, buffer.position() - 4);
			buffer.flip();
			channel.write(buffer);
			Thread.yield();

			Class<?> rtype = method.getReturnType();
			if (rtype == void.class) {
				return null;
			}
			buffer.clear();
			channel.read(buffer);
			int length = buffer.getInt(0);
			while (buffer.position() < length + 4) {
				channel.read(buffer);
			}
			buffer.flip();
			buffer.position(4);
			return serializer.readObject(new ByteBufferBackedInputStream(buffer), rtype);
		} finally {
			if (connection != null) {
				synchronized (freeConnections) {
					freeConnections.addLast(connection);
				}
			}
		}
	}

	private Connection getConnection() {
		Connection conn;
		synchronized (freeConnections) {
			conn = freeConnections.pollLast();
		}
		if (conn != null) {
			return conn;
		}
		return new Connection(remoteHost, remotePort);
	}

}
