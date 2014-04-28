package com.taobao.rpc.zaza.impl.netty;

import java.util.List;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.ZazaResponse;

public class NettyClientHandler extends SimpleChannelUpstreamHandler {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private NettyZazaClient client;

    public void setClient(NettyZazaClient client) {
        this.client = client;
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof List) {
            @SuppressWarnings("unchecked")
            List<ZazaResponse> responses = (List<ZazaResponse>) e.getMessage();
            client.putResponses(responses);
        } else if (e.getMessage() instanceof ZazaResponse) {
            ZazaResponse response = (ZazaResponse) e.getMessage();
            client.putResponse(response);
        } else {
            logger.error("receive message error,only support List || ZazaResponse");
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("[channel error]", e);
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    }

}
