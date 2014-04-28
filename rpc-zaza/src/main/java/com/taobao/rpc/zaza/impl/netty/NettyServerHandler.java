package com.taobao.rpc.zaza.impl.netty;

import java.util.List;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.ZazaResponse;
import com.taobao.rpc.zaza.model.ZazaMethodDataModel;
import com.taobao.rpc.zaza.model.ZazaThreadPoolModel;
import com.taobao.rpc.zaza.serialization.KryoSerializer;

public class NettyServerHandler extends SimpleChannelUpstreamHandler {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private int msgCountPerHandler = 0;

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("[channel error]", e);
    }

    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        final Object message = e.getMessage();
        if (message instanceof ZazaRequest) {
            msgCountPerHandler++;
            processOneRequest(ctx, message);
        } else if (message instanceof List) {
            msgCountPerHandler++;
            ZazaThreadPoolModel.intance.clientWorkers[msgCountPerHandler
                    & (ZazaThreadPoolModel.intance.clientWorkers.length - 1)].putTask(new Runnable() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    try {
                        List<ZazaRequest> requests = (List<ZazaRequest>) message;
                        for (ZazaRequest request : requests) {
                            Object object = ZazaMethodDataModel.instance.invoke(request);
                            ZazaResponse response = new ZazaResponse(request.getRequestID(), KryoSerializer
                                    .encode(object), object.getClass().getName().getBytes());
                            ctx.getChannel().write(response);
                        }
                    } catch (Throwable e) {
                        logger.error("[invoke error]", e);
                        // TODO nothing return if error; it is handled only
                        // by client
                        // timeout.
                        // so the client timeout should not be long
                    }
                }
            });
        }
    }

    private void processOneRequest(final ChannelHandlerContext ctx, final Object message) {
        ZazaThreadPoolModel.intance.clientWorkers[msgCountPerHandler
                & (ZazaThreadPoolModel.intance.clientWorkers.length - 1)].putTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ZazaRequest request = (ZazaRequest) message;
                    Object object = ZazaMethodDataModel.instance.invoke(request);
                    ZazaResponse response = new ZazaResponse(request.getRequestID(), KryoSerializer.encode(object),
                            object.getClass().getName().getBytes());
                    ctx.getChannel().write(response);
                } catch (Throwable e) {
                    logger.error("[invoke error]", e);
                    // TODO nothing return if error; it is handled only
                    // by client
                    // timeout.
                    // so the client timeout should not be long
                }
            }
        });
    }

}
