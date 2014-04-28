//package me.mayou.rpc.server;
//
//import java.util.concurrent.BlockingDeque;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.locks.LockSupport;
//
//import org.jboss.netty.channel.Channel;
//
//import me.mayou.rpc.common.Entry;
//import me.mayou.rpc.common.Parameters;
//import me.mayou.rpc.common.Result;
//import me.mayou.rpc.compress.Compressor;
//import me.mayou.rpc.method.MethodSupport;
//import me.mayou.rpc.serialize.SerializeException;
//import me.mayou.rpc.serialize.Serializer;
//
//public class ServerWorkerThread extends Thread {
//
//	private final Server server;
//
//	private final MethodSupport methodSupport;
//
//	private BlockingDeque<Entry<Channel, byte[]>> deque;
//
//	private AtomicBoolean isPark;
//
//	public ServerWorkerThread(Server server, MethodSupport methodSupport) {
//		this.server = server;
//		deque = new LinkedBlockingDeque<Entry<Channel, byte[]>>();
//		this.methodSupport = methodSupport;
//		isPark = new AtomicBoolean(false);
//		this.setName("ServerWorkerThread");
//	}
//
//	@Override
//	public void run() {
//		server.scan(this);
//	}
//
//	public void execute(Entry<Channel, byte[]> entry) {
//		while (true) {
//			if (entry == null) {
//				break;
//			}
//
//			// server.registerThread(parameters.getChannel(), this);
//
//			Parameters parameters = null;
//			try {
//				parameters = Serializer.deserializer(
//						Compressor.decompress(entry.getColumn()),
//						Parameters.class);
//			} catch (SerializeException e1) {
//				continue;
//			}
//
//			try {
//				Object retVal = methodSupport.invoke(parameters
//						.getInterfaceName(), parameters.getParameterTypes()
//						.toArray(new Class[1]), parameters.getParameters()
//						.toArray());
//
//				entry.getRow().write(
//						Compressor.compress(Serializer.serialize(Result
//								.success(retVal, parameters.getId()))));
//			} catch (Exception e) {
//				entry.getRow().write(
//						Result.fail(e.getMessage(), parameters.getId()));
//			}
//
//			entry = deque.pollFirst();
//		}
//	}
//
//	public boolean canSteal() {
//		return deque.peekLast() != null;
//	}
//
//	public Entry<Channel, byte[]> steal() {
//		return deque.pollLast();
//	}
//
//	public void push(Entry<Channel, byte[]> parameters) {
//		deque.offerFirst(parameters);
//	}
//
//	public boolean isEmpty() {
//		return deque.isEmpty();
//	}
//
//	public Entry<Channel, byte[]> get() {
//		return deque.pollFirst();
//	}
//
//	public void park() {
//		if (isPark.compareAndSet(false, true)) {
//			LockSupport.park();
//		}
//	}
//
//	public boolean unPark() {
//		if (isPark.compareAndSet(true, false)) {
//			LockSupport.unpark(this);
//			return true;
//		} else {
//			return false;
//		}
//	}
//}
