package com.taobao.rpc.zaza.impl.grizzly;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;

import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.interfaces.ZazaClient;

public class GrizzlyZazaClient extends ZazaClient {
    private Connection<Object> connection;

    public GrizzlyZazaClient(Connection<Object> connection) {
        this.connection = connection;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void sendRequest(final ZazaRequest wrapper, final int timeout) throws Exception {
        connection.write(wrapper, new CompletionHandler() {

            public void cancelled() {
            }

            public void failed(Throwable throwable) {
            }

            public void completed(Object result) {
            }

            public void updated(Object result) {
            }
        });
    }
}
