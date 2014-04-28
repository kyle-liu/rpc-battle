package me.mayou.rpc.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.ChannelFuture;

import com.taobao.rpc.api.RpcFactory;

import me.mayou.rpc.client.ClientFactory;
import me.mayou.rpc.common.Parameters;
import me.mayou.rpc.common.Result;
import me.mayou.rpc.compress.Compressor;
import me.mayou.rpc.serialize.SerializeException;
import me.mayou.rpc.serialize.Serializer;
import me.mayou.rpc.util.IdGenerator;

public abstract class BaseConsumerProxy {

	private static final AtomicLong count = new AtomicLong(0);

	private ThreadLocal<ChannelFuture> channelFutureLocal;

	static {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					System.out.println(count.get());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}

		});
		// thread.start();
	}

	public BaseConsumerProxy(final String remoteAddress) {
		channelFutureLocal = new ThreadLocal<ChannelFuture>() {

			@Override
			protected ChannelFuture initialValue() {
				return ClientFactory.getClient().getConnection(remoteAddress,
						RpcFactory.DEFAULT_PORT);
			}
		};
	}

	protected Object doInterval(String interfactName, Object[] objs) {
		List<Class<?>> clazzs = new ArrayList<Class<?>>(objs.length);
		List<Object> params = new ArrayList<Object>();
		for (Object obj : objs) {
			clazzs.add(obj.getClass());
			params.add(obj);
		}

		Parameters parameters = new Parameters();
		parameters.setInterfaceName(interfactName);
		parameters.setParameterTypes(clazzs);
		parameters.setParameters(params);

		while (!channelFutureLocal.get().getChannel().isConnected())
			;
		try {
			byte[] data = Serializer.serialize(parameters);

			channelFutureLocal.get().getChannel().write(data);
			synchronized (channelFutureLocal.get().getChannel()) {
				channelFutureLocal.get().getChannel().wait();
			}
			data = ClientFactory.getClient().getResult(
					channelFutureLocal.get().getChannel());
			Result result = Serializer.deserializer(data, Result.class);

			if (!result.isSuccess()) {
				System.out.println("³ö´íÀ²");
			}

			return result.getResult();
		} catch (SerializeException e) {
			return null;
		} catch (InterruptedException e) {
			return null;
		}
	}

}
