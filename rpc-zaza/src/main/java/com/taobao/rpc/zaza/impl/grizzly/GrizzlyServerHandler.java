package com.taobao.rpc.zaza.impl.grizzly;

import java.io.IOException;
import java.util.List;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.ZazaResponse;
import com.taobao.rpc.zaza.model.ZazaMethodDataModel;
import com.taobao.rpc.zaza.model.ZazaThreadPoolModel;
import com.taobao.rpc.zaza.serialization.KryoSerializer;

public class GrizzlyServerHandler extends BaseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrizzlyServerHandler.class);
    private int msgCountPerHandler = 0;

    @SuppressWarnings("rawtypes")
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final Object message = ctx.getMessage();
        final Connection connection = ctx.getConnection();

        if (message instanceof ZazaRequest) {
            msgCountPerHandler++;
            processOneRequest(connection, message);
        } else if (message instanceof List) {
            msgCountPerHandler++;
            ZazaThreadPoolModel.intance.clientWorkers[msgCountPerHandler
                    & (ZazaThreadPoolModel.intance.clientWorkers.length - 1)].putTask(new Runnable() {
                @SuppressWarnings({ "unchecked" })
                @Override
                public void run() {
                    try {
                        List<ZazaRequest> requests = (List<ZazaRequest>) message;
                        for (ZazaRequest request : requests) {
                            Object object = ZazaMethodDataModel.instance.invoke(request);
                            ZazaResponse response = new ZazaResponse(request.getRequestID(), KryoSerializer
                                    .encode(object), object.getClass().getName().getBytes());
                            connection.write(response, new EmptyCompletionHandler<WriteResult>() {
                                @Override
                                public void failed(Throwable throwable) {
                                }
                            });
                        }
                    } catch (Throwable e) {
                        LOGGER.error("[invoke error]", e);
                        // TODO nothing return if error; it is handled only
                        // by client
                        // timeout.
                        // so the client timeout should not be long
                    }
                }
            });
        }

        return ctx.getStopAction();
    }

    @SuppressWarnings("rawtypes")
    private void processOneRequest(final Connection connection, final Object message) {
        ZazaThreadPoolModel.intance.clientWorkers[msgCountPerHandler
                & (ZazaThreadPoolModel.intance.clientWorkers.length - 1)].putTask(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                try {
                    ZazaRequest request = (ZazaRequest) message;
                    Object object = ZazaMethodDataModel.instance.invoke(request);
                    ZazaResponse response = new ZazaResponse(request.getRequestID(), KryoSerializer.encode(object),
                            object.getClass().getName().getBytes());
                    connection.write(response, new EmptyCompletionHandler<WriteResult>() {
                        @Override
                        public void failed(Throwable throwable) {
                        }
                    });
                } catch (Throwable e) {
                    LOGGER.error("[invoke error]", e);
                    // TODO nothing return if error; it is handled only
                    // by client
                    // timeout.
                    // so the client timeout should not be long
                }
            }
        });
    }

}
