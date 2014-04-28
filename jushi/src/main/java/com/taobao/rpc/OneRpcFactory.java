package com.taobao.rpc;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.codec.KryoCodecFactory;
import java.net.InetSocketAddress;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class OneRpcFactory implements RpcFactory {
    private static final int PORT = 12345;

    @Override
    public <T> void export(Class<T> type, T serviceObject) {
        InetSocketAddress address = new InetSocketAddress(PORT);
        final Server server = Server.listen(address).register(type, serviceObject).codecBy(new KryoCodecFactory()).build();
        registerShutdownHook(server);
    }

    @Override
    public <T> T getReference(Class<T> type, String ip) {
        InetSocketAddress address = new InetSocketAddress(ip, PORT);
        Client client = Client.connectTo(address).codecBy(new KryoCodecFactory()).withTimeout(5000).build();
        registerShutdownHook(client);
        return client.proxy(type);
    }

    @Override
    public int getClientThreads() {
        return Runtime.getRuntime().availableProcessors() * 8;
    }

    @Override
    public String getAuthorId() {
        return "jushi";
    }

    private static void registerShutdownHook(final Shutdownable st) {
        Runtime.getRuntime().addShutdownHook(new Thread("jushi-rpc-shutdown-hook") {
            @Override
            public void run() {
                st.shutdown();
            }
        });
    }
}
