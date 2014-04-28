package shenfeng.simplerpc;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcceptLoop implements Runnable {

	private final Object serviceInstance;

	private final Map<Integer, Method> code2Method;

	private final Map<Method, Class<?>[]> method2ParamsType;

	private final Serializer serializer;

	private final int port;

	private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

	public AcceptLoop(Object serviceInstance, Serializer serializer, int port) {
		this.serviceInstance = serviceInstance;
		code2Method = Utils.getCode2methodMap(serviceInstance.getClass());
		method2ParamsType = Utils.getMethod2ParamsType(serviceInstance.getClass());
		this.serializer = serializer;
		this.port = port;
	}

	public Method getMethod(int code) {
		return code2Method.get(code);
	}

	public Class<?>[] getParameterTypes(Method method) {
		return method2ParamsType.get(method);
	}

	public Object getServiceInstance() {
		return serviceInstance;
	}

	public Object[] readObjects(InputStream in, Class<?>[] types) {
		return serializer.readObjects(in, types);
	}

	@Override
	public void run() {
		try {
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			ServerSocket serverSocket = serverChannel.socket();
			serverSocket.setReceiveBufferSize(1024 * 64);
			serverSocket.bind(new InetSocketAddress(port));
			serverChannel.configureBlocking(false);
			Selector selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			while (true) {
				int n = selector.select();
				if (n == 0) {
					continue;
				}

				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel channel = server.accept();
						channel.socket().setTcpNoDelay(true);
						registerChannel(selector, channel, SelectionKey.OP_READ);
					}
					if (key.isReadable()) {
						transport(key);
					}
					it.remove();
				}
				Thread.yield();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void writeObject(OutputStream outputStream, Class<?> type, Object obj) {
		serializer.writeObject(outputStream, type, obj);
	}

	private void registerChannel(Selector selector, SelectableChannel channel, int ops) throws Exception {
		if (channel == null) {
			return;
		}
		channel.configureBlocking(false);
		channel.register(selector, ops);
	}

	private void transport(SelectionKey key) {
		pool.execute(new ConnectionHandler(key, this));
	}
}
