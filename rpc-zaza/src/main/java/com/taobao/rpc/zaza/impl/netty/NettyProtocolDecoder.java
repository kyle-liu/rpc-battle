package com.taobao.rpc.zaza.impl.netty;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.LoggerFactory;


public class NettyProtocolDecoder extends FrameDecoder {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(NettyProtocolDecoder.class);

    private ChannelBuffer cumulation;

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object m = e.getMessage();
        if (!(m instanceof ChannelBuffer)) {
            ctx.sendUpstream(e);
            return;
        }

        ChannelBuffer input = (ChannelBuffer) m;
        if (!input.readable()) {
            return;
        }
        ChannelBuffer cumulation = cumulation(ctx);
        if (cumulation.readable()) {
            cumulation.discardReadBytes();
            cumulation.writeBytes(input);
            callDecode(ctx, e.getChannel(), cumulation, e.getRemoteAddress());
        } else {
            callDecode(ctx, e.getChannel(), input, e.getRemoteAddress());
            if (input.readable()) {
                cumulation.writeBytes(input);
            }
        }
    }

    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer in) {
        return NettyZazaProtocol.decode(in);
    }

    private void callDecode(ChannelHandlerContext context, Channel channel, ChannelBuffer cumulation,
            SocketAddress remoteAddress) throws Exception {
        List<Object> results = new ArrayList<Object>();
        while (cumulation.readable()) {
            int oldReaderIndex = cumulation.readerIndex();
            Object frame = NettyZazaProtocol.decode(cumulation);
            if (frame == null) {
                if (oldReaderIndex == cumulation.readerIndex()) {
                    break;
                } else {
                    continue;
                }
            } else if (oldReaderIndex == cumulation.readerIndex()) {
                logger.error("[message decode error]", "decode() method must read at least one byte ");
                return;
            }

            results.add(frame);
        }
        if (results.size() > 0)
            fireMessageReceived(context, remoteAddress, results);
    }

    private void fireMessageReceived(ChannelHandlerContext context, SocketAddress remoteAddress, Object result) {
        Channels.fireMessageReceived(context, result, remoteAddress);
    }

    private ChannelBuffer cumulation(ChannelHandlerContext ctx) {
        ChannelBuffer c = cumulation;
        if (c == null) {
            c = ChannelBuffers.dynamicBuffer(ctx.getChannel().getConfig().getBufferFactory());
            cumulation = c;
        }
        return c;
    }

}
