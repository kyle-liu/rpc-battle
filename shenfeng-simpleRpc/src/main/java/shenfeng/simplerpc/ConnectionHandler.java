package shenfeng.simplerpc;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectionHandler implements Runnable {

	private final ByteBuffer buffer = ByteBuffer.allocate(5125);

	private final AcceptLoop ref;
	private final SelectionKey key;

	public ConnectionHandler(SelectionKey key, AcceptLoop ref) {
		this.key = key;
		this.ref = ref;
		key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
	}

	@Override
	public void run() {
		SocketChannel channel = (SocketChannel) key.channel();
		buffer.clear();
		int count;
		try {
			while ((count = channel.read(buffer)) > 0) {
				int length = buffer.getInt(0);
				if (buffer.position() != length + 4) {
					continue;
				}
				buffer.flip();
				buffer.position(4);
				Method method = ref.getMethod(buffer.getInt());
				Class<?>[] types = ref.getParameterTypes(method);
				Object result = method.invoke(ref.getServiceInstance(),
						ref.readObjects(new ByteBufferBackedInputStream(buffer), types));
				Class<?> rtype = method.getReturnType();
				if (rtype != void.class) {
					buffer.clear();
					buffer.position(4);
					ref.writeObject(new ByteBufferBackedOutputStream(buffer), rtype, result);
					buffer.putInt(0, buffer.position() - 4);
					buffer.flip();
					channel.write(buffer);
				}
			}
			if (count < 0) {
				channel.close();
				return;
			}
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			key.selector().wakeup();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
