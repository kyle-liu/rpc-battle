package me.mayou.rpc.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import me.mayou.rpc.common.Entry;
import me.mayou.rpc.common.Parameters;
import me.mayou.rpc.common.Result;
import me.mayou.rpc.compress.Compressor;
import me.mayou.rpc.method.MethodSupport;
import me.mayou.rpc.network.handler.Decoder;
import me.mayou.rpc.network.handler.Encoder;
import me.mayou.rpc.network.handler.ParametersHandler;
import me.mayou.rpc.serialize.SerializeException;
import me.mayou.rpc.serialize.Serializer;
import me.mayou.rpc.util.TimeUtil;

public class Server {

	private final int port;

	private static final byte[] failure;

	static {
		failure = Serializer.serialize(Result.fail(null, null));
	}

	private final List<BlockingDeque<Entry<Channel, byte[]>>> deques;

	private volatile boolean isStart;

	private MethodSupport methodSupport;

	private final int threadCount;

	private ChannelFactory factory;

	private ServerBootstrap serverBootstrap;

	private ExecutorService executor;

	private AtomicLong pos;

	@SuppressWarnings("unchecked")
	public Server(int port, int threadCount) {
		this.port = port;
		this.threadCount = threadCount;
		isStart = false;
		methodSupport = new MethodSupport();
		deques = new ArrayList<BlockingDeque<Entry<Channel, byte[]>>>(8);
		for (int index = 0; index < 8; ++index) {
			deques.add(new LinkedBlockingDeque<Entry<Channel, byte[]>>());
		}
		pos = new AtomicLong(0);
	}

	public synchronized void start() {
		if (!isStart) {
			isStart = true;

			factory = new NioServerSocketChannelFactory(
					Executors.newFixedThreadPool(2),
					Executors.newFixedThreadPool(9), 8);
			serverBootstrap = new ServerBootstrap(factory);
			serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {

				@Override
				public ChannelPipeline getPipeline() throws Exception {
					return Channels.pipeline(new Decoder(),
							new ParametersHandler(Server.this), new Encoder());
				}

			});
			serverBootstrap.setOption("child.keepAlive", true);
			serverBootstrap.bind(new InetSocketAddress(port));
			
			executor = Executors.newFixedThreadPool(threadCount);
			for (int count = 0; count < threadCount; ++count) {
				final int seed = count % 8;
				executor.execute(new Runnable() {

					@Override
					public void run() {
						Server.this.execute(seed);
					}

				});
			}
		} else {
			throw new ServerStartException();
		}
	}

	public synchronized void stop() {
		if (isStart) {
			isStart = false;
		}
	}

	public void execute(int seed) {
		while (isStart) {
			Class<?>[] clazzArray = new Class<?>[1];

			Entry<Channel, byte[]> entry = null;
			try {
				entry = deques.get(seed++ % 8).takeFirst();
			} catch (InterruptedException e) {
				continue;
			}

			try {
				Parameters parameters = Serializer.deserializer(
						entry.getColumn(), Parameters.class);
				Object result = methodSupport.invoke(parameters
						.getInterfaceName(), parameters.getParameterTypes()
						.toArray(clazzArray), parameters.getParameters()
						.toArray());
				entry.getRow().write(
						Serializer.serialize(Result.success(result, null)));
			} catch (Exception e) {
				entry.getRow().write(failure);
			}
		}
	}

	public void putParameters(Channel channel, byte[] parameters) {
		if (isStart) {
			deques.get((int) (pos.getAndIncrement() % 8)).offerLast(
					Entry.<Channel, byte[]> getEntry(channel, parameters));
		} else {
			throw new ServerStopException();
		}
	}

	public void register(String className, Object impl) {
		methodSupport.register(className, impl);
	}
}
