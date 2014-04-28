package shenfeng.simplerpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;

public class AcceptLoop implements Runnable {

	private final Object serviceInstance;

	private final Map<Integer, Method> code2Method;

	private final Serializer serializer;

	private ServerSocketChannel serverChannel;

	private ServerSocket serverSocket;

	public AcceptLoop(int port, Class<?> type, Object serviceInstance, Serializer serializer) {
		this.serviceInstance = serviceInstance;
		code2Method = Utils.getCode2methodMap(type);
		this.serializer = serializer;
		try {
			serverSocket = new ServerSocket();
			serverSocket.setReceiveBufferSize(1024 * 64);
			serverSocket.bind(new InetSocketAddress(port));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Method getMethod(int code) {
		return code2Method.get(code);
	}

	public Object getServiceInstance() {
		return serviceInstance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				new ConnectionHandler(serverSocket.accept(), this);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				serverChannel.close();
			} catch (IOException e) {
			}
		}
	}

	public Serializer serializer(InputStream in) {
		return serializer.create(in);
	}

	public Serializer serializer(OutputStream out) {
		return serializer.create(out);
	}

}
