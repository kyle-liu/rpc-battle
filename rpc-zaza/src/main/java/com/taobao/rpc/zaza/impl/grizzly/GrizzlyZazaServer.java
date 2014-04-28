package com.taobao.rpc.zaza.impl.grizzly;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.memory.ByteBufferManager;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.interfaces.ZazaServer;
import com.taobao.rpc.zaza.model.ZazaMethodDataModel;
import com.taobao.rpc.zaza.util.ZazaConfigUtil;

public class GrizzlyZazaServer implements ZazaServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrizzlyZazaServer.class);
    private TCPNIOTransport transport = null;

    private AtomicBoolean startFlag = new AtomicBoolean(false);

    public void start() {
        if (!startFlag.compareAndSet(false, true)) {
            return;
        }
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add(new TransportFilter());
//        filterChainBuilder.add(new LZMAFilter());
        filterChainBuilder.add(new GrizzlyProtocolFilter());
        filterChainBuilder.add(new GrizzlyServerHandler());
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
        builder.setOptimizedForMultiplexing(true);
        builder.setIOStrategy(SameThreadIOStrategy.getInstance());

        transport = builder.build();
        
        transport.setMemoryManager(new ByteBufferManager());

        transport.setProcessor(filterChainBuilder.build());
        try {
            transport.bind(ZazaConfigUtil.getPort());
            transport.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.warn("server started,listen at: " + ZazaConfigUtil.getPort());
    }

    public void stop() {
        if (transport != null) {
            try {
                transport.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startFlag.set(false);
            LOGGER.warn("server stoped!");
        }
    }

    public <T> void register(Class<T> classType, T rpcInstance) {
        this.start();
        ZazaMethodDataModel.instance.insert(classType, rpcInstance);
    }
}
