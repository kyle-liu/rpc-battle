package com.taobao.rpc.zaza.impl.netty;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.taobao.rpc.zaza.NamedThreadFactory;
import com.taobao.rpc.zaza.ZazaRpcFactory;
import com.taobao.rpc.zaza.interfaces.ZazaClient;
import com.taobao.rpc.zaza.util.ZazaConfigUtil;

public class NettyClientFactory {
    public static final int MIN_READ_BUFFER_SIZE = 64;
    public static final int INITIAL_READ_BUFFER_SIZE = 16384;
    public static final int MAX_READ_BUFFER_SIZE = 65536;

    private static NettyClientFactory instance = new NettyClientFactory();
    private static List<ZazaClient> clients = new ArrayList<ZazaClient>(ZazaRpcFactory.CLIENT_NUM);
    private static AtomicInteger i = new AtomicInteger(0);
    private static ThreadLocal<ZazaClient> threadLocalClient = new ThreadLocal<ZazaClient>() {
        @Override
        protected ZazaClient initialValue() {
            if (ZazaRpcFactory.CLIENT_NUM == 1) {
                return clients.get(0);
            }

            int value = i.getAndIncrement();
            if (value >= ZazaRpcFactory.CLIENT_NUM) {
                i.set(0);
                value = 0;
            }
            return clients.get(value);
        }

    };

    private NettyClientFactory() {
    }

    public static NettyClientFactory getInstance() {
        return instance;
    }

    public void init(String ip) throws Exception {
        ThreadFactory bossThreadFactory = new NamedThreadFactory("NETTYCLIENT-BOSS-");
        ThreadFactory workerThreadFactory = new NamedThreadFactory("NETTYCLIENT-WORKER-");
        ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(bossThreadFactory), Executors.newCachedThreadPool(workerThreadFactory)));
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("receiveBufferSizePredictorFactory", new AdaptiveReceiveBufferSizePredictorFactory(
                MIN_READ_BUFFER_SIZE, INITIAL_READ_BUFFER_SIZE, MAX_READ_BUFFER_SIZE));
        bootstrap.setOption("reuseAddress", true);
        int connectTimeout = ZazaConfigUtil.getConnectionTimeout();
        bootstrap.setOption("connectTimeoutMillis", connectTimeout);
        for (int i = 0; i < ZazaRpcFactory.CLIENT_NUM; i++) {
            clients.add(createClient(bootstrap, ip));
        }
    }

    protected ZazaClient createClient(ClientBootstrap bootstrap, String targetIP) throws Exception {
        int targetPort = ZazaConfigUtil.getPort();
        NettyClientHandler handler = new NettyClientHandler();
        bootstrap.setPipelineFactory(new NettyClientPipelineFactory(handler));
        final ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort));
        future.awaitUninterruptibly(ZazaConfigUtil.getConnectionTimeout());
        if (!future.isDone()) {
            throw new Exception("Create connection to " + targetIP + ":" + targetPort + " timeout!");
        }
        if (future.isCancelled()) {
            throw new Exception("Create connection to " + targetIP + ":" + targetPort + " cancelled by user!");
        }
        if (!future.isSuccess()) {
            throw new Exception("Create connection to " + targetIP + ":" + targetPort + " error", future.getCause());
        }
        NettyZazaClient client = new NettyZazaClient(future);
        handler.setClient(client);
        return client;
    }

    public ZazaClient get() throws Exception {
        if (ZazaRpcFactory.CLIENT_NUM == 1) {
            return clients.get(0);
        }
        return threadLocalClient.get();
    }

}
