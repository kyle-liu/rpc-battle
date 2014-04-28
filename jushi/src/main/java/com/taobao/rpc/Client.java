package com.taobao.rpc;

import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.codec.CodecFactory;
import com.taobao.rpc.service.Invoker;
import com.taobao.rpc.service.ServiceDirectory;
import com.taobao.rpc.service.ServiceIndex;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Client implements Shutdownable {


    public static Builder connectTo(InetSocketAddress address) {
        return new Builder(address);
    }

    private final Map<String, Integer> serviceIndices;
    private final Channel channel;
    private final Invoker invoker;

    private Client(Channel channel, Invoker invoker) {
        this.channel = channel;
        this.invoker = invoker;
        serviceIndices = new ConcurrentHashMap<String, Integer>();
    }

    @Override
    public void shutdown() {
        invoker.shutdown();
    }

    @SuppressWarnings("unchecked")
    public <T> T proxy(Class<T> type) {
        appendServiceIndicesOf(type);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return invoker.invoke(channel, serviceIndex(method), args);
            }
        });
    }


    private Integer serviceIndex(Method method) {
        if (method.getDeclaringClass().equals(ServiceDirectory.class)) {
            return 0;
        }
        Integer id = serviceIndices.get(method.toString());
        if (id == null) throw new RpcException(new UnsupportedOperationException(method.toString()));
        return id;
    }

    private void appendServiceIndicesOf(Class<?> type) {
        List<ServiceIndex> serverIndices = getServiceIndexes(type);
        if (serverIndices.isEmpty()) throw new IllegalStateException("Could not find services of " + type);
        for (ServiceIndex serviceIndex : serverIndices) {
            serviceIndices.put(serviceIndex.name(), serviceIndex.index());
        }
    }

    @SuppressWarnings("unchecked")
    private List<ServiceIndex> getServiceIndexes(Class<?> type) {
        return (List<ServiceIndex>) invoker.invoke(channel, 0, new Object[]{type.toString()});
    }

    public static class Builder {
        private final InetSocketAddress address;
        private CodecFactory factory;
        private long timeout;

        private Builder(InetSocketAddress address) {
            this.address = address;
        }

        public Builder codecBy(CodecFactory factory) {
            this.factory = factory;
            return this;
        }

        public Builder withTimeout(long millis) {
            timeout = millis;
            return this;
        }

        public Client build() {
            final Bootstrap bootstrap = new Bootstrap();
            try {
                Invoker invoker = new Invoker(factory, timeout);

                Channel channel = bootstrap.group(new NioEventLoopGroup(1))
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .remoteAddress(address)
                        .handler(invoker)
                        .connect().sync().channel();

                channel.closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        bootstrap.shutdown();
                    }
                });

                return new Client(channel, invoker);
            } catch (InterruptedException e) {
                bootstrap.shutdown();
                throw new RpcException(e);
            }
        }
    }
}
