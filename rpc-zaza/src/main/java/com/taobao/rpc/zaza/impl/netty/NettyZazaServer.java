package com.taobao.rpc.zaza.impl.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.taobao.rpc.zaza.NamedThreadFactory;
import com.taobao.rpc.zaza.interfaces.ZazaServer;
import com.taobao.rpc.zaza.model.ZazaMethodDataModel;
import com.taobao.rpc.zaza.util.ZazaConfigUtil;

public class NettyZazaServer implements ZazaServer {
    public static final int MIN_READ_BUFFER_SIZE = 64;
    public static final int INITIAL_READ_BUFFER_SIZE = 16384;
    public static final int MAX_READ_BUFFER_SIZE = 65536;
    public static final int CHANNEL_MEMORY_LIMIT = MAX_READ_BUFFER_SIZE * 2;
    public static final long GLOBAL_MEMORY_LIMIT = Runtime.getRuntime().maxMemory() / 3;

    private ServerBootstrap bootstrap = null;

    private AtomicBoolean startFlag = new AtomicBoolean(false);

    public NettyZazaServer() {
        ThreadFactory serverBossTF = new NamedThreadFactory("NETTYSERVER-BOSS-");
        ThreadFactory serverWorkerTF = new NamedThreadFactory("NETTYSERVER-WORKER-");
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(serverBossTF),
                Executors.newCachedThreadPool(serverWorkerTF)));
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("reuseAddress", true);

        bootstrap.setOption("child.receiveBufferSizePredictorFactory", new AdaptiveReceiveBufferSizePredictorFactory(
                MIN_READ_BUFFER_SIZE, INITIAL_READ_BUFFER_SIZE, MAX_READ_BUFFER_SIZE));

    }

    public void start() {
        if (!startFlag.compareAndSet(false, true)) {
            return;
        }
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = new DefaultChannelPipeline();
                pipeline.addLast("decoder", new NettyProtocolDecoder());
                pipeline.addLast("encoder", new NettyProtocolEncoder());
                pipeline.addLast("handler", new NettyServerHandler());
                return pipeline;
            }
        });

        bootstrap.bind(new InetSocketAddress(ZazaConfigUtil.getPort()));
        System.out.println("server started,listen at: " + ZazaConfigUtil.getPort());
    }

    public void stop() {
        bootstrap.releaseExternalResources();
        startFlag.set(false);
    }

    public <T> void register(Class<T> classType, T rpcInstance) {
        this.start();
        ZazaMethodDataModel.instance.insert(classType, rpcInstance);
    }

}
