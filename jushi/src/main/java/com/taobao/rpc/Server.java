package com.taobao.rpc;

import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.codec.CodecFactory;
import com.taobao.rpc.service.Service;
import com.taobao.rpc.service.Services;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Server implements Shutdownable {

    private final ServerBootstrap bootstrap;

    public Server(ServerBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public static Builder listen(InetSocketAddress address) {
        return new Builder(address);
    }

    @Override
    public void shutdown() {
        bootstrap.shutdown();
    }

    public static class Builder {

        private final List<Service> services;
        private final InetSocketAddress address;
        private CodecFactory factory;

        public Builder(InetSocketAddress address) {
            this.address = address;
            services = new ArrayList<Service>();
        }

        public <T> Builder register(Class<T> type, T service) {
            services.addAll(Service.services(type, service));
            return this;
        }

        public Builder codecBy(CodecFactory factory) {
            this.factory = factory;
            return this;
        }

        public Server build() {
            NioEventLoopGroup group = new NioEventLoopGroup(1);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group, group)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 65536)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .localAddress(address)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Services(services, factory));
                        }
                    });

            try {
                bootstrap.bind().sync();
                return new Server(bootstrap);
            } catch (InterruptedException e) {
                bootstrap.shutdown();
                throw new RpcException(e);
            }
        }

    }
}
