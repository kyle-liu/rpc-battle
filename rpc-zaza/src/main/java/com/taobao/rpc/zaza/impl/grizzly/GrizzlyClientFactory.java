package com.taobao.rpc.zaza.impl.grizzly;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;

import com.taobao.rpc.zaza.ZazaRpcFactory;
import com.taobao.rpc.zaza.interfaces.ZazaClient;
import com.taobao.rpc.zaza.util.ZazaConfigUtil;

public class GrizzlyClientFactory {

    private static final GrizzlyClientFactory instance = new GrizzlyClientFactory();

    private static List<ZazaClient> clients = new ArrayList<ZazaClient>(ZazaRpcFactory.CLIENT_NUM);

    private GrizzlyClientFactory() {
    }

    public static GrizzlyClientFactory getInstance() {
        return instance;
    }

    public void init(String ip) throws Exception {
        GrizzlyClientHandler handler = new GrizzlyClientHandler();
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add(new TransportFilter());
//        filterChainBuilder.add(new LZMAFilter());
        filterChainBuilder.add(new GrizzlyProtocolFilter());
        filterChainBuilder.add(handler);

        final TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
        transportBuilder.setOptimizedForMultiplexing(true);
        transportBuilder.setIOStrategy(SameThreadIOStrategy.getInstance());
        final TCPNIOTransport transport = transportBuilder.build();
        transport.setTcpNoDelay(true);
        transport.setReuseAddress(true);
        transport.setProcessor(filterChainBuilder.build());

        transport.start();
        for (int i = 0; i < ZazaRpcFactory.CLIENT_NUM; i++) {
            clients.add(createClient(transport, handler, ip, ZazaConfigUtil.getPort(),
                    ZazaConfigUtil.getConnectionTimeout()));
        }
    }

    @SuppressWarnings("rawtypes")
    protected ZazaClient createClient(final TCPNIOTransport transport, GrizzlyClientHandler handler, String targetIP,
            int targetPort, int connectTimeout) throws Exception {
        Future<Connection> future = transport.connect(targetIP, targetPort);
        if (connectTimeout < 1000) {
            connectTimeout = 1000;
        }
        Connection connection = future.get(connectTimeout, TimeUnit.MILLISECONDS);
        @SuppressWarnings("unchecked")
        GrizzlyZazaClient client = new GrizzlyZazaClient(connection);
        handler.setClient(client);
        return client;
    }

    public ZazaClient get() throws Exception {
        return clients.get(ZazaClient.getIndexOfThread() % ZazaConfigUtil.getClentNum());
    }
}
